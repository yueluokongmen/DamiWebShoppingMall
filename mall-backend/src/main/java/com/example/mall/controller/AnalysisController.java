package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.mall.common.Result;
import com.example.mall.entity.*;
import com.example.mall.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户画像 + 数据分析控制器
 * 需求：3.3 用户画像（地域、购买力、偏好分类）
 */
@RestController
@RequestMapping("/admin/analysis")
public class AnalysisController {

    @Autowired
    private UserProfileMapper userProfileMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private UserBrowseLogMapper userBrowseLogMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SkuMapper skuMapper;

    // ========== 用户画像 ==========

    /** 生成/更新所有用户画像（批量） */
    @PostMapping("/profile/refresh-all")
    public Result<String> refreshAllProfiles() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, 0); // 只处理普通用户
        List<User> users = userMapper.selectList(wrapper);

        int count = 0;
        for (User user : users) {
            generateProfile(user.getUserId());
            count++;
        }
        return Result.success("已刷新 " + count + " 个用户画像");
    }

    /** 生成/更新单个用户画像 */
    @PostMapping("/profile/refresh/{userId}")
    public Result<UserProfile> refreshProfile(@PathVariable Long userId) {
        UserProfile profile = generateProfile(userId);
        return Result.success(profile);
    }

    /** 获取用户画像 */
    @GetMapping("/profile/{userId}")
    public Result<UserProfile> getProfile(@PathVariable Long userId) {
        LambdaQueryWrapper<UserProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserProfile::getUserId, userId);
        UserProfile profile = userProfileMapper.selectOne(wrapper);
        if (profile == null) {
            profile = generateProfile(userId);
        }
        return Result.success(profile);
    }

    /** 获取所有用户画像列表 */
    @GetMapping("/profile/list")
    public Result<List<UserProfile>> profileList() {
        return Result.success(userProfileMapper.selectList(null));
    }

    /** 按购买力分布统计 */
    @GetMapping("/profile/purchase-distribution")
    public Result<Map<String, Long>> purchaseDistribution() {
        List<UserProfile> profiles = userProfileMapper.selectList(null);
        Map<String, Long> distribution = profiles.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getPurchaseLevel() != null ? p.getPurchaseLevel() : "UNKNOWN",
                        Collectors.counting()
                ));
        return Result.success(distribution);
    }

    /** 按地域分布统计 */
    @GetMapping("/profile/region-distribution")
    public Result<Map<String, Long>> regionDistribution() {
        List<UserProfile> profiles = userProfileMapper.selectList(null);
        Map<String, Long> distribution = profiles.stream()
                .filter(p -> p.getProvince() != null && !p.getProvince().isEmpty())
                .collect(Collectors.groupingBy(UserProfile::getProvince, Collectors.counting()));
        return Result.success(distribution);
    }

    /**
     * 核心方法：生成用户画像
     */
    private UserProfile generateProfile(Long userId) {
        // 查是否已有画像
        LambdaQueryWrapper<UserProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserProfile::getUserId, userId);
        UserProfile profile = userProfileMapper.selectOne(wrapper);

        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
        }

        // 1. 地域：取用户最常用的收货地址省份
        LambdaQueryWrapper<Address> addrWrapper = new LambdaQueryWrapper<>();
        addrWrapper.eq(Address::getUserId, userId);
        List<Address> addresses = addressMapper.selectList(addrWrapper);
        if (!addresses.isEmpty()) {
            // 取默认地址，否则取第一个
            Address primary = addresses.stream()
                    .filter(a -> a.getIsDefault() != null && a.getIsDefault() == 1)
                    .findFirst().orElse(addresses.get(0));
            profile.setProvince(primary.getProvince());
            profile.setCity(primary.getCity());
        }

        // 2. 消费统计
        LambdaQueryWrapper<Orders> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Orders::getUserId, userId);
        orderWrapper.in(Orders::getOrderStatus, 1, 2, 3); // 已付款的订单
        List<Orders> paidOrders = ordersMapper.selectList(orderWrapper);

        BigDecimal totalSpent = BigDecimal.ZERO;
        int totalOrders = paidOrders.size();
        LocalDateTime lastPurchaseTime = null;

        for (Orders order : paidOrders) {
            totalSpent = totalSpent.add(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);
            if (lastPurchaseTime == null || (order.getPayTime() != null && order.getPayTime().isAfter(lastPurchaseTime))) {
                lastPurchaseTime = order.getPayTime();
            }
        }

        profile.setTotalSpent(totalSpent);
        profile.setTotalOrders(totalOrders);
        profile.setAvgOrderAmount(totalOrders > 0 ? totalSpent.divide(new BigDecimal(totalOrders), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        profile.setLastPurchaseTime(lastPurchaseTime);

        // 3. 购买力分级
        if (totalSpent.compareTo(new BigDecimal(5000)) >= 0) {
            profile.setPurchaseLevel("VIP");
        } else if (totalSpent.compareTo(new BigDecimal(2000)) >= 0) {
            profile.setPurchaseLevel("HIGH");
        } else if (totalSpent.compareTo(new BigDecimal(500)) >= 0) {
            profile.setPurchaseLevel("MEDIUM");
        } else {
            profile.setPurchaseLevel("LOW");
        }

        // 4. 偏好分类：浏览最多的商品类别
        LambdaQueryWrapper<UserBrowseLog> browseWrapper = new LambdaQueryWrapper<>();
        browseWrapper.eq(UserBrowseLog::getUserId, userId);
        browseWrapper.isNotNull(UserBrowseLog::getCategoryId);
        List<UserBrowseLog> browseLogs = userBrowseLogMapper.selectList(browseWrapper);

        if (!browseLogs.isEmpty()) {
            Map<Integer, Long> categoryCount = browseLogs.stream()
                    .filter(log -> log.getCategoryId() != null)
                    .collect(Collectors.groupingBy(UserBrowseLog::getCategoryId, Collectors.counting()));

            Optional<Map.Entry<Integer, Long>> maxEntry = categoryCount.entrySet().stream()
                    .max(Map.Entry.comparingByValue());

            if (maxEntry.isPresent()) {
                Integer categoryId = maxEntry.get().getKey();
                profile.setPreferredCategoryId(categoryId);
                Category category = categoryMapper.selectById(categoryId);
                if (category != null) {
                    profile.setPreferredCategoryName(category.getCategoryName());
                }
            }
        }

        profile.setUpdateTime(LocalDateTime.now());

        // 保存
        if (profile.getProfileId() == null) {
            userProfileMapper.insert(profile);
        } else {
            userProfileMapper.updateById(profile);
        }

        return profile;
    }

    // ========== 商品销售排行榜 ==========

    /** 商品销售排行榜（按销量） */
    @GetMapping("/product/rank")
    public Result<List<Map<String, Object>>> productRank(@RequestParam(defaultValue = "10") int limit) {
        // 从order_item聚合查询
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(OrderItem::getSkuId, OrderItem::getItemQuantity, OrderItem::getItemTotalPrice, OrderItem::getSkuName, OrderItem::getSkuImage);
        List<OrderItem> allItems = orderItemMapper.selectList(wrapper);

        // 按skuId聚合
        Map<Long, Integer> salesMap = new HashMap<>();
        Map<Long, BigDecimal> revenueMap = new HashMap<>();
        Map<Long, String> nameMap = new HashMap<>();
        Map<Long, String> imageMap = new HashMap<>();

        for (OrderItem item : allItems) {
            Long skuId = item.getSkuId();
            salesMap.merge(skuId, item.getItemQuantity(), Integer::sum);
            revenueMap.merge(skuId, item.getItemTotalPrice() != null ? item.getItemTotalPrice() : BigDecimal.ZERO, BigDecimal::add);
            nameMap.putIfAbsent(skuId, item.getSkuName());
            imageMap.putIfAbsent(skuId, item.getSkuImage());
        }

        // 排序
        List<Map<String, Object>> rankList = salesMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("skuId", entry.getKey());
                    item.put("skuName", nameMap.get(entry.getKey()));
                    item.put("skuImage", imageMap.get(entry.getKey()));
                    item.put("totalSales", entry.getValue());
                    item.put("totalRevenue", revenueMap.getOrDefault(entry.getKey(), BigDecimal.ZERO));
                    return item;
                })
                .collect(Collectors.toList());

        return Result.success(rankList);
    }

    // ========== 多维统计报表 ==========

    /** 按类别统计销售 */
    @GetMapping("/stats/by-category")
    public Result<List<Map<String, Object>>> statsByCategory() {
        List<OrderItem> allItems = orderItemMapper.selectList(null);
        List<Category> categories = categoryMapper.selectList(null);
        Map<Integer, String> categoryNameMap = categories.stream()
                .collect(Collectors.toMap(Category::getCategoryId, Category::getCategoryName));

        // 按categoryId聚合
        Map<Integer, Integer> salesMap = new HashMap<>();
        Map<Integer, BigDecimal> revenueMap = new HashMap<>();

        for (OrderItem item : allItems) {
            Integer catId = item.getCategoryId();
            if (catId != null) {
                salesMap.merge(catId, item.getItemQuantity(), Integer::sum);
                revenueMap.merge(catId, item.getItemTotalPrice() != null ? item.getItemTotalPrice() : BigDecimal.ZERO, BigDecimal::add);
            }
        }

        List<Map<String, Object>> result = salesMap.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("categoryId", entry.getKey());
                    item.put("categoryName", categoryNameMap.getOrDefault(entry.getKey(), "未知"));
                    item.put("totalSales", entry.getValue());
                    item.put("totalRevenue", revenueMap.getOrDefault(entry.getKey(), BigDecimal.ZERO));
                    return item;
                })
                .sorted((a, b) -> ((BigDecimal) b.get("totalRevenue")).compareTo((BigDecimal) a.get("totalRevenue")))
                .collect(Collectors.toList());

        return Result.success(result);
    }

    /** 按订单状态统计 */
    @GetMapping("/stats/by-status")
    public Result<Map<String, Object>> statsByStatus() {
        Map<String, Object> data = new HashMap<>();
        String[] statusNames = {"待付款", "待发货", "已发货", "已完成"};

        for (int i = 0; i < 4; i++) {
            LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Orders::getOrderStatus, i);
            data.put("status" + i + "Count", ordersMapper.selectCount(wrapper));
            data.put("status" + i + "Name", statusNames[i]);
        }
        return Result.success(data);
    }

    /** 库存统计（低库存预警） */
    @GetMapping("/stats/stock-warning")
    public Result<List<Sku>> stockWarning(@RequestParam(defaultValue = "50") int threshold) {
        LambdaQueryWrapper<Sku> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(Sku::getStock, threshold);
        wrapper.orderByAsc(Sku::getStock);
        return Result.success(skuMapper.selectList(wrapper));
    }

    // ========== 日/周/月趋势 ==========

    /** 销售趋势 - 支持日/周/月维度 */
    @GetMapping("/stats/trend")
    public Result<Map<String, Object>> salesTrend(@RequestParam(defaultValue = "day") String dimension,
                                                   @RequestParam(defaultValue = "7") int count) {
        Map<String, Object> data = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<BigDecimal> salesList = new ArrayList<>();
        List<Long> orderList = new ArrayList<>();

        LocalDate today = LocalDate.now();

        switch (dimension) {
            case "week" -> {
                // 按周统计
                for (int i = count - 1; i >= 0; i--) {
                    LocalDate weekStart = today.minusWeeks(i);
                    LocalDate weekEnd = weekStart.plusDays(6);
                    labels.add(weekStart.format(DateTimeFormatter.ofPattern("MM-dd")));

                    LocalDateTime start = LocalDateTime.of(weekStart, LocalTime.MIN);
                    LocalDateTime end = LocalDateTime.of(weekEnd, LocalTime.MAX);

                    salesList.add(getSalesBetween(start, end));
                    orderList.add(getOrderCountBetween(start, end));
                }
            }
            case "month" -> {
                // 按月统计
                for (int i = count - 1; i >= 0; i--) {
                    LocalDate monthStart = today.minusMonths(i).withDayOfMonth(1);
                    LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
                    labels.add(monthStart.format(DateTimeFormatter.ofPattern("yyyy-MM")));

                    LocalDateTime start = LocalDateTime.of(monthStart, LocalTime.MIN);
                    LocalDateTime end = LocalDateTime.of(monthEnd, LocalTime.MAX);

                    salesList.add(getSalesBetween(start, end));
                    orderList.add(getOrderCountBetween(start, end));
                }
            }
            default -> {
                // 按日统计
                for (int i = count - 1; i >= 0; i--) {
                    LocalDate date = today.minusDays(i);
                    labels.add(date.format(DateTimeFormatter.ofPattern("MM-dd")));

                    LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
                    LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);

                    salesList.add(getSalesBetween(start, end));
                    orderList.add(getOrderCountBetween(start, end));
                }
            }
        }

        data.put("labels", labels);
        data.put("sales", salesList);
        data.put("orders", orderList);
        return Result.success(data);
    }

    private BigDecimal getSalesBetween(LocalDateTime start, LocalDateTime end) {
        QueryWrapper<Orders> wrapper = new QueryWrapper<>();
        wrapper.select("IFNULL(sum(total_amount), 0) as total")
                .ge("order_create_time", start)
                .le("order_create_time", end)
                .in("order_status", 1, 2, 3); // 只统计已付款
        Map<String, Object> res = ordersMapper.selectMaps(wrapper).get(0);
        Object total = res.get("total");
        return total instanceof BigDecimal ? (BigDecimal) total : BigDecimal.ZERO;
    }

    private Long getOrderCountBetween(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Orders::getCreateTime, start)
                .le(Orders::getCreateTime, end)
                .in(Orders::getOrderStatus, 1, 2, 3);
        return ordersMapper.selectCount(wrapper);
    }

    // ========== 销售异常判别 ==========

    /** 销售异常检测 */
    @GetMapping("/anomaly/detect")
    public Result<List<Map<String, Object>>> detectAnomaly() {
        List<Map<String, Object>> anomalies = new ArrayList<>();

        // 1. 订单金额异常（单笔金额远超平均值）
        QueryWrapper<Orders> avgWrapper = new QueryWrapper<>();
        avgWrapper.select("IFNULL(avg(total_amount), 0) as avg_amount");
        Map<String, Object> avgRes = ordersMapper.selectMaps(avgWrapper).get(0);
        BigDecimal avgAmount = new BigDecimal(avgRes.get("avg_amount").toString());

        LambdaQueryWrapper<Orders> spikeWrapper = new LambdaQueryWrapper<>();
        spikeWrapper.gt(Orders::getTotalAmount, avgAmount.multiply(new BigDecimal("5"))); // 超过均值5倍
        List<Orders> spikeOrders = ordersMapper.selectList(spikeWrapper);
        for (Orders order : spikeOrders) {
            Map<String, Object> anomaly = new HashMap<>();
            anomaly.put("type", "AMOUNT_SPIKE");
            anomaly.put("description", "订单金额异常偏高");
            anomaly.put("orderId", order.getOrderId());
            anomaly.put("orderNo", order.getOrderNo());
            anomaly.put("metricValue", order.getTotalAmount());
            anomaly.put("thresholdValue", avgAmount.multiply(new BigDecimal("5")));
            anomaly.put("time", order.getCreateTime());
            anomalies.add(anomaly);
        }

        // 2. 库存告急（库存低于10）
        LambdaQueryWrapper<Sku> stockWrapper = new LambdaQueryWrapper<>();
        stockWrapper.le(Sku::getStock, 10);
        List<Sku> lowStockSkus = skuMapper.selectList(stockWrapper);
        for (Sku sku : lowStockSkus) {
            Map<String, Object> anomaly = new HashMap<>();
            anomaly.put("type", "LOW_STOCK");
            anomaly.put("description", "商品库存告急");
            anomaly.put("skuId", sku.getSkuId());
            anomaly.put("skuName", sku.getSkuName());
            anomaly.put("metricValue", sku.getStock());
            anomaly.put("thresholdValue", 10);
            anomalies.add(anomaly);
        }

        // 3. 今日销量骤降（与近7天日均对比）
        LocalDate today = LocalDate.now();
        BigDecimal todaySales = getSalesBetween(
                LocalDateTime.of(today, LocalTime.MIN),
                LocalDateTime.of(today, LocalTime.MAX));

        BigDecimal weekTotal = BigDecimal.ZERO;
        for (int i = 1; i <= 7; i++) {
            LocalDate d = today.minusDays(i);
            weekTotal = weekTotal.add(getSalesBetween(
                    LocalDateTime.of(d, LocalTime.MIN),
                    LocalDateTime.of(d, LocalTime.MAX)));
        }
        BigDecimal dailyAvg = weekTotal.divide(new BigDecimal("7"), 2, RoundingMode.HALF_UP);

        if (dailyAvg.compareTo(BigDecimal.ZERO) > 0 && todaySales.compareTo(dailyAvg.multiply(new BigDecimal("0.3"))) < 0) {
            Map<String, Object> anomaly = new HashMap<>();
            anomaly.put("type", "SALES_DROP");
            anomaly.put("description", "今日销量骤降（低于近7天日均的30%）");
            anomaly.put("metricValue", todaySales);
            anomaly.put("thresholdValue", dailyAvg.multiply(new BigDecimal("0.3")));
            anomaly.put("dailyAvg", dailyAvg);
            anomalies.add(anomaly);
        }

        return Result.success(anomalies);
    }

    // ========== 趋势预测（简单移动平均） ==========

    /** 销售趋势预测 - 基于移动平均 */
    @GetMapping("/stats/forecast")
    public Result<Map<String, Object>> forecast(@RequestParam(defaultValue = "7") int days) {
        Map<String, Object> data = new HashMap<>();

        // 取过去14天的数据
        List<String> labels = new ArrayList<>();
        List<BigDecimal> actualSales = new ArrayList<>();

        for (int i = 13; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            labels.add(date.format(DateTimeFormatter.ofPattern("MM-dd")));
            actualSales.add(getSalesBetween(
                    LocalDateTime.of(date, LocalTime.MIN),
                    LocalDateTime.of(date, LocalTime.MAX)));
        }

        // 7日移动平均预测未来days天
        List<String> forecastLabels = new ArrayList<>();
        List<BigDecimal> forecastSales = new ArrayList<>();

        BigDecimal sumLast7 = actualSales.subList(7, 14).stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgLast7 = sumLast7.divide(new BigDecimal("7"), 2, RoundingMode.HALF_UP);

        for (int i = 1; i <= days; i++) {
            LocalDate futureDate = LocalDate.now().plusDays(i);
            forecastLabels.add(futureDate.format(DateTimeFormatter.ofPattern("MM-dd")));
            forecastSales.add(avgLast7); // 简单预测：用近期均值
        }

        data.put("actualLabels", labels);
        data.put("actualSales", actualSales);
        data.put("forecastLabels", forecastLabels);
        data.put("forecastSales", forecastSales);
        data.put("method", "7日移动平均");

        return Result.success(data);
    }
}
