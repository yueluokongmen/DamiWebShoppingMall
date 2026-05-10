package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.Result;
import com.example.mall.entity.*;
import com.example.mall.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐系统控制器
 * 需求：3.3 简单推荐（"浏览过此商品的人也买了..."）+ 协同过滤推荐
 */
@RestController
@RequestMapping("/recommend")
public class RecommendController {

    @Autowired
    private UserBrowseLogMapper userBrowseLogMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SkuMapper skuMapper;

    /**
     * 简单推荐："浏览过此商品的人也买了..."
     * 逻辑：找浏览过该商品的其他用户 → 看他们买了什么 → 排序返回
     * URL: GET /recommend/similar?productId=1&limit=6
     */
    @GetMapping("/similar")
    public Result<List<Spu>> similarRecommend(@RequestParam Long productId,
                                               @RequestParam(defaultValue = "6") int limit) {
        // 1. 找浏览过该商品的用户
        LambdaQueryWrapper<UserBrowseLog> browseWrapper = new LambdaQueryWrapper<>();
        browseWrapper.eq(UserBrowseLog::getProductId, productId);
        browseWrapper.select(UserBrowseLog::getUserId);
        List<UserBrowseLog> browseLogs = userBrowseLogMapper.selectList(browseWrapper);

        if (browseLogs.isEmpty()) {
            return Result.success(getHotProducts(limit)); // 无浏览数据时返回热门商品
        }

        // 2. 这些用户买过的商品
        Set<Long> userIds = browseLogs.stream().map(UserBrowseLog::getUserId).collect(Collectors.toSet());
        LambdaQueryWrapper<Orders> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.in(Orders::getUserId, userIds);
        orderWrapper.in(Orders::getOrderStatus, 1, 2, 3); // 已付款
        orderWrapper.select(Orders::getOrderId);
        List<Orders> orders = ordersMapper.selectList(orderWrapper);

        if (orders.isEmpty()) {
            return Result.success(getHotProducts(limit));
        }

        List<Long> orderIds = orders.stream().map(Orders::getOrderId).toList();
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.in(OrderItem::getOrderId, orderIds);
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);

        // 3. 按购买次数统计，排除当前商品
        Map<Long, Integer> productBuyCount = new HashMap<>();
        for (OrderItem item : items) {
            Long skuId = item.getSkuId();
            Sku sku = skuMapper.selectById(skuId);
            if (sku != null) {
                Long pid = sku.getProductId();
                if (!pid.equals(productId)) {
                    productBuyCount.merge(pid, item.getItemQuantity(), Integer::sum);
                }
            }
        }

        // 4. 排序取topN
        List<Long> recommendedProductIds = productBuyCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();

        if (recommendedProductIds.isEmpty()) {
            return Result.success(getHotProducts(limit));
        }

        LambdaQueryWrapper<Spu> spuWrapper = new LambdaQueryWrapper<>();
        spuWrapper.in(Spu::getProductId, recommendedProductIds);
        spuWrapper.eq(Spu::getProductStatus, 1); // 只推荐上架的
        List<Spu> products = spuMapper.selectList(spuWrapper);

        // 按推荐排序
        Map<Long, Integer> orderMap = new HashMap<>();
        for (int i = 0; i < recommendedProductIds.size(); i++) {
            orderMap.put(recommendedProductIds.get(i), i);
        }
        products.sort(Comparator.comparingInt(p -> orderMap.getOrDefault(p.getProductId(), Integer.MAX_VALUE)));

        return Result.success(products);
    }

    /**
     * 协同过滤推荐：基于用户的购买行为相似度
     * 逻辑：找到和当前用户购买行为相似的用户 → 推荐他们买过但当前用户没买过的商品
     * URL: GET /recommend/cf?userId=1&limit=6
     */
    @GetMapping("/cf")
    public Result<List<Spu>> collaborativeFilter(@RequestParam Long userId,
                                                   @RequestParam(defaultValue = "6") int limit) {
        // 1. 构建用户-商品购买矩阵
        // 获取当前用户买过的商品
        Set<Long> myProducts = getUserPurchasedProducts(userId);

        if (myProducts.isEmpty()) {
            return Result.success(getHotProducts(limit));
        }

        // 2. 找所有有购买行为的用户
        LambdaQueryWrapper<Orders> allOrdersWrapper = new LambdaQueryWrapper<>();
        allOrdersWrapper.in(Orders::getOrderStatus, 1, 2, 3);
        allOrdersWrapper.select(Orders::getOrderId, Orders::getUserId);
        List<Orders> allOrders = ordersMapper.selectList(allOrdersWrapper);

        // 按userId分组
        Map<Long, List<Long>> userOrderMap = allOrders.stream()
                .collect(Collectors.groupingBy(Orders::getUserId,
                        Collectors.mapping(Orders::getOrderId, Collectors.toList())));

        // 构建每个用户买过的商品集合
        Map<Long, Set<Long>> userProductMap = new HashMap<>();
        for (Map.Entry<Long, List<Long>> entry : userOrderMap.entrySet()) {
            Set<Long> products = new HashSet<>();
            LambdaQueryWrapper<OrderItem> itemW = new LambdaQueryWrapper<>();
            itemW.in(OrderItem::getOrderId, entry.getValue());
            List<OrderItem> items = orderItemMapper.selectList(itemW);
            for (OrderItem item : items) {
                Sku sku = skuMapper.selectById(item.getSkuId());
                if (sku != null) {
                    products.add(sku.getProductId());
                }
            }
            userProductMap.put(entry.getKey(), products);
        }

        // 3. 计算Jaccard相似度，找最相似的用户
        List<Map.Entry<Long, Double>> similarities = new ArrayList<>();
        for (Map.Entry<Long, Set<Long>> entry : userProductMap.entrySet()) {
            if (entry.getKey().equals(userId)) continue;
            double sim = jaccardSimilarity(myProducts, entry.getValue());
            if (sim > 0) {
                similarities.add(Map.entry(entry.getKey(), sim));
            }
        }

        if (similarities.isEmpty()) {
            return Result.success(getHotProducts(limit));
        }

        // 按相似度排序
        similarities.sort(Map.Entry.<Long, Double>comparingByValue().reversed());

        // 4. 取相似用户买过、但当前用户没买过的商品
        Map<Long, Double> recommendScore = new HashMap<>();
        int topK = Math.min(5, similarities.size());
        for (int i = 0; i < topK; i++) {
            Long simUserId = similarities.get(i).getKey();
            Double simScore = similarities.get(i).getValue();
            Set<Long> simProducts = userProductMap.get(simUserId);

            for (Long pid : simProducts) {
                if (!myProducts.contains(pid)) {
                    recommendScore.merge(pid, simScore, Double::sum);
                }
            }
        }

        List<Long> recommendedIds = recommendScore.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();

        if (recommendedIds.isEmpty()) {
            return Result.success(getHotProducts(limit));
        }

        LambdaQueryWrapper<Spu> spuWrapper = new LambdaQueryWrapper<>();
        spuWrapper.in(Spu::getProductId, recommendedIds);
        spuWrapper.eq(Spu::getProductStatus, 1);
        List<Spu> products = spuMapper.selectList(spuWrapper);

        // 按推荐分数排序
        Map<Long, Integer> orderMap = new HashMap<>();
        for (int i = 0; i < recommendedIds.size(); i++) {
            orderMap.put(recommendedIds.get(i), i);
        }
        products.sort(Comparator.comparingInt(p -> orderMap.getOrDefault(p.getProductId(), Integer.MAX_VALUE)));

        return Result.success(products);
    }

    /**
     * 猜你喜欢（首页推荐）- 综合协同过滤 + 热门兜底
     * URL: GET /recommend/for-you?userId=1&limit=8
     */
    @GetMapping("/for-you")
    public Result<List<Spu>> forYou(@RequestParam(required = false) Long userId,
                                     @RequestParam(defaultValue = "8") int limit) {
        // 未登录或无用户ID时直接返回热门商品
        if (userId == null) {
            return Result.success(getHotProducts(limit));
        }

        // 先尝试协同过滤
        Set<Long> myProducts = getUserPurchasedProducts(userId);

        if (!myProducts.isEmpty()) {
            // 有购买历史，用协同过滤
            // 简化：直接调用similar的逻辑，基于用户最近浏览的商品
            LambdaQueryWrapper<UserBrowseLog> browseWrapper = new LambdaQueryWrapper<>();
            browseWrapper.eq(UserBrowseLog::getUserId, userId);
            browseWrapper.orderByDesc(UserBrowseLog::getBrowseTime);
            browseWrapper.last("LIMIT 3");
            List<UserBrowseLog> recentBrowses = userBrowseLogMapper.selectList(browseWrapper);

            Set<Long> recommendedIds = new LinkedHashSet<>();
            for (UserBrowseLog log : recentBrowses) {
                // 对每个最近浏览的商品做简单推荐
                LambdaQueryWrapper<UserBrowseLog> bw = new LambdaQueryWrapper<>();
                bw.eq(UserBrowseLog::getProductId, log.getProductId());
                bw.select(UserBrowseLog::getUserId);
                List<UserBrowseLog> browses = userBrowseLogMapper.selectList(bw);

                Set<Long> otherUsers = browses.stream().map(UserBrowseLog::getUserId).collect(Collectors.toSet());
                otherUsers.remove(userId);

                if (!otherUsers.isEmpty()) {
                    LambdaQueryWrapper<Orders> ow = new LambdaQueryWrapper<>();
                    ow.in(Orders::getUserId, otherUsers);
                    ow.in(Orders::getOrderStatus, 1, 2, 3);
                    List<Orders> orders = ordersMapper.selectList(ow);
                    if (!orders.isEmpty()) {
                        List<Long> oids = orders.stream().map(Orders::getOrderId).toList();
                        LambdaQueryWrapper<OrderItem> iw = new LambdaQueryWrapper<>();
                        iw.in(OrderItem::getOrderId, oids);
                        List<OrderItem> items = orderItemMapper.selectList(iw);
                        for (OrderItem item : items) {
                            Sku sku = skuMapper.selectById(item.getSkuId());
                            if (sku != null && !myProducts.contains(sku.getProductId())) {
                                recommendedIds.add(sku.getProductId());
                            }
                        }
                    }
                }

                if (recommendedIds.size() >= limit) break;
            }

            if (!recommendedIds.isEmpty()) {
                LambdaQueryWrapper<Spu> spuW = new LambdaQueryWrapper<>();
                spuW.in(Spu::getProductId, recommendedIds);
                spuW.eq(Spu::getProductStatus, 1);
                spuW.last("LIMIT " + limit);
                return Result.success(spuMapper.selectList(spuW));
            }
        }

        // 兜底：热门商品
        return Result.success(getHotProducts(limit));
    }

    // ========== 工具方法 ==========

    /** 获取用户购买过的商品ID集合 */
    private Set<Long> getUserPurchasedProducts(Long userId) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getUserId, userId);
        wrapper.in(Orders::getOrderStatus, 1, 2, 3);
        List<Orders> orders = ordersMapper.selectList(wrapper);

        Set<Long> products = new HashSet<>();
        for (Orders order : orders) {
            LambdaQueryWrapper<OrderItem> iw = new LambdaQueryWrapper<>();
            iw.eq(OrderItem::getOrderId, order.getOrderId());
            List<OrderItem> items = orderItemMapper.selectList(iw);
            for (OrderItem item : items) {
                Sku sku = skuMapper.selectById(item.getSkuId());
                if (sku != null) {
                    products.add(sku.getProductId());
                }
            }
        }
        return products;
    }

    /** 热门商品（按订单量排序）作为兜底推荐 */
    private List<Spu> getHotProducts(int limit) {
        LambdaQueryWrapper<Spu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Spu::getProductStatus, 1);
        wrapper.orderByDesc(Spu::getProductId);
        wrapper.last("LIMIT " + limit);
        return spuMapper.selectList(wrapper);
    }

    /** Jaccard相似度 = 交集/并集 */
    private double jaccardSimilarity(Set<Long> setA, Set<Long> setB) {
        if (setA.isEmpty() || setB.isEmpty()) return 0.0;
        Set<Long> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);
        Set<Long> union = new HashSet<>(setA);
        union.addAll(setB);
        return (double) intersection.size() / union.size();
    }
}
