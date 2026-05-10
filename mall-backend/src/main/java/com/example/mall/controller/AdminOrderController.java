package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.Result;
import com.example.mall.entity.OrderItem;
import com.example.mall.entity.Orders;
import com.example.mall.mapper.OrderItemMapper;
import com.example.mall.mapper.OrdersMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/order")
public class AdminOrderController {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    //获取所有订单列表 (包含商品详情)
    @GetMapping("/list")
    public Result<List<Orders>> list() {
        //按时间倒序排列
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Orders::getCreateTime);
        List<Orders> list = ordersMapper.selectList(wrapper);

        // 遍历填充每个订单买了什么商品 (OrderItem)
        for (Orders order : list) {
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(OrderItem::getOrderId, order.getOrderId());
            List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
            order.setOrderItems(items);
        }

        return Result.success(list);
    }

    //订单发货
    @PostMapping("/delivery/{orderNo}")
    public Result<String> delivery(@PathVariable String orderNo) {
        // 查订单
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getOrderNo, orderNo);
        Orders order = ordersMapper.selectOne(wrapper);

        if (order == null) {
            return Result.error("订单不存在");
        }

        //只有1(已支付)的状态才能发货
        if (order.getOrderStatus() != 1) {
            return Result.error("订单状态不正确，无法发货");
        }

        //修改状态为2(已发货)
        order.setOrderStatus(2);
        ordersMapper.updateById(order);

        return Result.success("发货成功");
    }

    //确认完成订单
    @PostMapping("/complete/{orderNo}")
    public Result<String> complete(@PathVariable String orderNo) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getOrderNo, orderNo);
        Orders order = ordersMapper.selectOne(wrapper);

        if (order == null) {
            return Result.error("订单不存在");
        }

        if (order.getOrderStatus() != 2) {
            return Result.error("订单状态不正确，无法完成");
        }

        order.setOrderStatus(3); // 已完成
        order.setCompleteTime(LocalDateTime.now());
        ordersMapper.updateById(order);

        return Result.success("订单已完成");
    }
}