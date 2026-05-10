package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.Result;
import com.example.mall.entity.*;
import com.example.mall.mapper.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 销售人员（Sales）后台控制器
 * 需求：3.1 销售人员 - 商品目录管理、商品信息修改、销售状态监控、用户浏览/购买日志
 */
@RestController
@RequestMapping("/sales")
public class SalesController {

    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private UserBrowseLogMapper userBrowseLogMapper;
    @Autowired
    private OperationLogMapper operationLogMapper;

    // ========== 商品目录管理 ==========

    /** 获取分类列表 */
    @GetMapping("/category/list")
    public Result<List<Category>> categoryList() {
        return Result.success(categoryMapper.selectList(null));
    }

    /** 添加分类 */
    @PostMapping("/category/add")
    public Result<String> addCategory(@RequestBody Category category, HttpServletRequest request) {
        categoryMapper.insert(category);
        logOperation(request, "销售人员添加分类: " + category.getCategoryName(), "ADD", "CATEGORY", Long.valueOf(category.getCategoryId()));
        return Result.success("添加成功");
    }

    /** 删除分类 */
    @DeleteMapping("/category/delete/{id}")
    public Result<String> deleteCategory(@PathVariable Integer id, HttpServletRequest request) {
        categoryMapper.deleteById(id);
        logOperation(request, "销售人员删除分类ID: " + id, "DELETE", "CATEGORY", Long.valueOf(id));
        return Result.success("删除成功");
    }

    // ========== 商品信息修改 ==========

    /** 获取自己负责的商品列表 */
    @GetMapping("/product/list")
    public Result<List<Spu>> productList(@RequestParam Long salesId) {
        LambdaQueryWrapper<Spu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Spu::getSalesId, salesId);
        wrapper.orderByDesc(Spu::getProductId);
        return Result.success(spuMapper.selectList(wrapper));
    }

    /** 修改商品价格 */
    @PostMapping("/product/update-price/{productId}")
    public Result<String> updatePrice(@PathVariable Long productId, @RequestBody Map<String, Object> params, HttpServletRequest request) {
        Spu spu = new Spu();
        spu.setProductId(productId);
        if (params.containsKey("spuPrice")) {
            spu.setSpuPrice(new java.math.BigDecimal(params.get("spuPrice").toString()));
        }
        spu.setUpdateTime(LocalDateTime.now());
        spuMapper.updateById(spu);

        logOperation(request, "销售人员修改商品价格, productID: " + productId, "UPDATE", "PRODUCT", productId);
        return Result.success("修改成功");
    }

    /** 修改SKU库存 */
    @PostMapping("/sku/update-stock/{skuId}")
    public Result<String> updateStock(@PathVariable Long skuId, @RequestBody Map<String, Integer> params, HttpServletRequest request) {
        Sku sku = new Sku();
        sku.setSkuId(skuId);
        sku.setStock(params.get("stock"));
        skuMapper.updateById(sku);

        logOperation(request, "销售人员修改SKU库存, skuID: " + skuId, "UPDATE", "PRODUCT", skuId);
        return Result.success("修改成功");
    }

    // ========== 销售状态监控 ==========

    /** 自己负责商品的订单列表 */
    @GetMapping("/order/list")
    public Result<List<Orders>> orderList(@RequestParam Long salesId) {
        // 先查自己负责的商品ID
        LambdaQueryWrapper<Spu> spuWrapper = new LambdaQueryWrapper<>();
        spuWrapper.eq(Spu::getSalesId, salesId);
        spuWrapper.select(Spu::getProductId);
        List<Spu> myProducts = spuMapper.selectList(spuWrapper);

        if (myProducts.isEmpty()) {
            return Result.success(List.of());
        }

        // 查这些商品的SKU
        List<Long> productIds = myProducts.stream().map(Spu::getProductId).toList();
        LambdaQueryWrapper<Sku> skuWrapper = new LambdaQueryWrapper<>();
        skuWrapper.in(Sku::getProductId, productIds);
        skuWrapper.select(Sku::getSkuId);
        List<Sku> mySkus = skuMapper.selectList(skuWrapper);

        if (mySkus.isEmpty()) {
            return Result.success(List.of());
        }

        // 查包含这些SKU的订单项
        List<Long> skuIds = mySkus.stream().map(Sku::getSkuId).toList();
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.in(OrderItem::getSkuId, skuIds);
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);

        if (items.isEmpty()) {
            return Result.success(List.of());
        }

        // 获取去重的orderId列表
        List<Long> orderIds = items.stream().map(OrderItem::getOrderId).distinct().toList();
        LambdaQueryWrapper<Orders> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.in(Orders::getOrderId, orderIds);
        orderWrapper.orderByDesc(Orders::getCreateTime);
        List<Orders> orders = ordersMapper.selectList(orderWrapper);

        // 填充orderItems
        for (Orders order : orders) {
            LambdaQueryWrapper<OrderItem> iw = new LambdaQueryWrapper<>();
            iw.eq(OrderItem::getOrderId, order.getOrderId());
            order.setOrderItems(orderItemMapper.selectList(iw));
        }

        return Result.success(orders);
    }

    /** 销售统计概览 - 自己负责的商品 */
    @GetMapping("/stats/overview")
    public Result<Map<String, Object>> statsOverview(@RequestParam Long salesId) {
        Map<String, Object> data = new HashMap<>();

        // 我负责的商品数
        LambdaQueryWrapper<Spu> spuWrapper = new LambdaQueryWrapper<>();
        spuWrapper.eq(Spu::getSalesId, salesId);
        long productCount = spuMapper.selectCount(spuWrapper);
        data.put("productCount", productCount);

        // 我的商品带来的订单数和销售额
        LambdaQueryWrapper<Spu> spuListWrapper = new LambdaQueryWrapper<>();
        spuListWrapper.eq(Spu::getSalesId, salesId);
        spuListWrapper.select(Spu::getProductId);
        List<Spu> myProducts = spuMapper.selectList(spuListWrapper);

        int orderCount = 0;
        java.math.BigDecimal totalSales = java.math.BigDecimal.ZERO;

        if (!myProducts.isEmpty()) {
            List<Long> productIds = myProducts.stream().map(Spu::getProductId).toList();
            LambdaQueryWrapper<Sku> skuW = new LambdaQueryWrapper<>();
            skuW.in(Sku::getProductId, productIds);
            skuW.select(Sku::getSkuId);
            List<Sku> mySkus = skuMapper.selectList(skuW);

            if (!mySkus.isEmpty()) {
                List<Long> skuIds = mySkus.stream().map(Sku::getSkuId).toList();
                LambdaQueryWrapper<OrderItem> itemW = new LambdaQueryWrapper<>();
                itemW.in(OrderItem::getSkuId, skuIds);
                List<OrderItem> items = orderItemMapper.selectList(itemW);
                orderCount = (int) items.stream().map(OrderItem::getOrderId).distinct().count();
                totalSales = items.stream().map(OrderItem::getItemTotalPrice).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            }
        }

        data.put("orderCount", orderCount);
        data.put("totalSales", totalSales);

        return Result.success(data);
    }

    // ========== 用户浏览/购买日志 ==========

    /** 查看浏览日志 */
    @GetMapping("/log/browse")
    public Result<List<UserBrowseLog>> browseLogList(@RequestParam(required = false) Long salesId) {
        LambdaQueryWrapper<UserBrowseLog> wrapper = new LambdaQueryWrapper<>();
        // 如果指定了salesId，只看自己负责的商品的日志
        if (salesId != null) {
            LambdaQueryWrapper<Spu> spuW = new LambdaQueryWrapper<>();
            spuW.eq(Spu::getSalesId, salesId);
            spuW.select(Spu::getProductId);
            List<Spu> myProducts = spuMapper.selectList(spuW);
            if (!myProducts.isEmpty()) {
                List<Long> productIds = myProducts.stream().map(Spu::getProductId).toList();
                wrapper.in(UserBrowseLog::getProductId, productIds);
            } else {
                return Result.success(List.of());
            }
        }
        wrapper.orderByDesc(UserBrowseLog::getBrowseTime);
        wrapper.last("LIMIT 200");
        return Result.success(userBrowseLogMapper.selectList(wrapper));
    }

    /** 操作日志记录 */
    private void logOperation(HttpServletRequest request, String operation, String opType, String targetType, Long targetId) {
        OperationLog log = new OperationLog();
        log.setRole(2); // Sales
        log.setOperation(operation);
        log.setOperationType(opType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setOpTime(LocalDateTime.now());

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) ip = request.getRemoteAddr();
        log.setOpIp(ip);

        operationLogMapper.insert(log);
    }
}
