package com.example.mall.controller;
import com.example.mall.entity.OrderDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.Result;
import com.example.mall.entity.*;
import com.example.mall.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.example.mall.entity.Cart;
import java.math.BigDecimal;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private SpuMapper spuMapper;

    //增加status参数 (required = false 表示可选，不传就查全部)
    // URL: http://localhost:8081/order/list?userId=1&status=0
    @GetMapping("/list")
    public Result<List<Orders>> list(@RequestParam Long userId, @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getUserId, userId);

        //=如果前端传了 status，就按状态查；没传就查所有
        if (status != null) {
            wrapper.eq(Orders::getOrderStatus, status);
        }

        wrapper.orderByDesc(Orders::getCreateTime);

        List<Orders> ordersList = ordersMapper.selectList(wrapper);

        //遍历填充商品详情
        for (Orders order : ordersList) {
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(OrderItem::getOrderId, order.getOrderId());
            List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
            order.setOrderItems(items);
        }

        return Result.success(ordersList);
    }


    @PostMapping("/create")
    @Transactional //开启事务
    public Result<String> createOrder(@RequestBody OrderDTO params) {

        //准备数据
        //查地址
        Address address = addressMapper.selectById(params.getAddressId());
        //查商品
        Sku sku = skuMapper.selectById(params.getSkuId());

        if (address == null || sku == null) {
            return Result.error("地址或商品不存在");
        }

        //创建主订单
        Orders order = new Orders();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", "")); //生成唯一订单号
        order.setUserId(params.getUserId());
        order.setOrderStatus(0); // 0=待付款
        order.setCreateTime(LocalDateTime.now());

        //存地址快照
        order.setReceiverInfoSnapshot(address);

        //计算总价
        BigDecimal total = sku.getSkuPrice().multiply(new BigDecimal(params.getQuantity()));
        order.setTotalAmount(total);

        ordersMapper.insert(order); //插入数据库拿到orderId

        //创建订单详情
        OrderItem item = new OrderItem();
        item.setOrderId(order.getOrderId()); //关联刚才的主订单ID
        item.setSkuId(sku.getSkuId());
        item.setSkuName(sku.getSkuName());
        item.setSkuImage(sku.getSkuImage());
        item.setCurrentPrice(sku.getSkuPrice());
        item.setItemQuantity(params.getQuantity());
        item.setItemTotalPrice(total);

        // 回填category_id
        Spu spu = spuMapper.selectById(sku.getProductId());
        if (spu != null) {
            item.setCategoryId(spu.getCategoryId());
        }

        orderItemMapper.insert(item);

        return Result.success("下单成功，订单号：" + order.getOrderNo());
    }

    // URL: POST http://localhost:8081/order/submit
    // 参数: {"userId": 1, "addressId": 1}
    @PostMapping("/submit")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> submitOrder(@RequestBody OrderDTO params) {

        //校验地址
        Address address = addressMapper.selectById(params.getAddressId());
        if (address == null) {
            return Result.error("收货地址不存在");
        }

        //查出购物车里已勾选的商品 (selected = 1)
        LambdaQueryWrapper<Cart> cartWrapper = new LambdaQueryWrapper<>();
        cartWrapper.eq(Cart::getUserId, params.getUserId());
        cartWrapper.eq(Cart::getSelected, 1);
        List<Cart> cartList = cartMapper.selectList(cartWrapper);

        if (cartList.isEmpty()) {
            return Result.error("购物车没有选中的商品，无法下单");
        }

        //准备主订单数据
        Orders order = new Orders();
        String orderNo = UUID.randomUUID().toString().replace("-", "");
        order.setOrderNo(orderNo);
        order.setUserId(params.getUserId());
        order.setOrderStatus(0); // 0=待付款
        order.setCreateTime(LocalDateTime.now());
        order.setReceiverInfoSnapshot(address); //保存地址快照

        //计算总价并生成订单项
        BigDecimal totalAmount = BigDecimal.ZERO;

        order.setTotalAmount(BigDecimal.ZERO);
        ordersMapper.insert(order);

        //遍历购物车：插入子订单 (OrderItems)->计算总价->清空购物车
        for (Cart cart : cartList) {
            Sku sku = skuMapper.selectById(cart.getSkuId());
            if (sku == null) continue;
            if (sku.getStock() < cart.getCartQuantity()) {
                //抛出异常会触发回滚，创建的主订单也会被撤销
                throw new RuntimeException("商品 " + sku.getSkuName() + " 库存不足");
            }
            //扣减库存
            sku.setStock(sku.getStock() - cart.getCartQuantity());
            skuMapper.updateById(sku); //更新数据库
            //创建详情记录
            OrderItem item = new OrderItem();
            item.setOrderId(order.getOrderId()); //关联主订单ID
            item.setSkuId(sku.getSkuId());
            item.setSkuName(sku.getSkuName());
            item.setSkuImage(sku.getSkuImage());
            item.setCurrentPrice(sku.getSkuPrice()); //下单时的单价（snapshot)
            item.setItemQuantity(cart.getCartQuantity());

            // 回填category_id
            Spu spu = spuMapper.selectById(sku.getProductId());
            if (spu != null) {
                item.setCategoryId(spu.getCategoryId());
            }

            BigDecimal itemTotal = sku.getSkuPrice().multiply(new BigDecimal(cart.getCartQuantity()));
            item.setItemTotalPrice(itemTotal);

            orderItemMapper.insert(item);

            //累加总价
            totalAmount = totalAmount.add(itemTotal);

            //下单成功后删除购物车里对应的记录
            cartMapper.deleteById(cart.getCartId());
        }

        //更新主订单的总价
        order.setTotalAmount(totalAmount);
        ordersMapper.updateById(order);

        return Result.success(order.getOrderNo()); //返回订单号给前端跳转支付
    }


    // URL: POST http://localhost:8081/order/pay?orderNo=xxxxxxxx
    @PostMapping("/pay")
    public Result<String> pay(@RequestParam String orderNo) {
        //查订单
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getOrderNo, orderNo);
        Orders order = ordersMapper.selectOne(wrapper);

        //校验
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (order.getOrderStatus() != 0) {
            return Result.error("订单已支付或已取消");
        }

        //修改状态 (0到1)
        order.setOrderStatus(1); // 1=已支付
        order.setPayTime(LocalDateTime.now()); //记录支付时间

        ordersMapper.updateById(order); //更新数据库

        return Result.success("支付成功！等待商家发货");
    }

    // 查询订单详情
    // URL: http://localhost:8081/order/detail?orderNo=xxxx
    @GetMapping("/detail")
    public Result<Orders> getDetail(@RequestParam String orderNo) {
        //查主订单
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getOrderNo, orderNo);
        Orders order = ordersMapper.selectOne(wrapper);

        if(order == null) {
            return Result.error("订单不存在");
        }

        //查关联的商品项 (OrderItem)
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, order.getOrderId());
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);

        //放入实体对象
        order.setOrderItems(items);

        return Result.success(order);
    }

    // 确认收货
    // URL: POST http://localhost:8081/order/confirm?orderNo=xxxx
    @PostMapping("/confirm")
    public Result<String> confirm(@RequestParam String orderNo) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getOrderNo, orderNo);
        Orders order = ordersMapper.selectOne(wrapper);

        if (order == null) {
            return Result.error("订单不存在");
        }
        if (order.getOrderStatus() != 2) {
            return Result.error("订单状态不正确，无法确认收货");
        }

        order.setOrderStatus(3); // 已完成
        order.setCompleteTime(LocalDateTime.now());
        ordersMapper.updateById(order);

        return Result.success("确认收货成功");
    }
}



