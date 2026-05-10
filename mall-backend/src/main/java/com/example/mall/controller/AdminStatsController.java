package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.mall.common.Result;
import com.example.mall.entity.Orders;
import com.example.mall.mapper.OrdersMapper;
import com.example.mall.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/admin/stats")
public class AdminStatsController {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private UserMapper userMapper;

    /** 首页仪表盘数据 */
    @GetMapping("/data")
    public Result<Map<String, Object>> getData() {
        Map<String, Object> map = new HashMap<>();

        // 总销售额（只统计已付款订单）
        QueryWrapper<Orders> salesWrapper = new QueryWrapper<>();
        salesWrapper.select("IFNULL(sum(total_amount), 0) as total")
                .in("order_status", 1, 2, 3);
        Map<String, Object> salesRes = ordersMapper.selectMaps(salesWrapper).get(0);
        BigDecimal totalSales = (BigDecimal) salesRes.get("total");

        // 总订单量
        Long totalOrders = ordersMapper.selectCount(null);

        // 今日订单数
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        QueryWrapper<Orders> todayOrderWrapper = new QueryWrapper<>();
        todayOrderWrapper.ge("order_create_time", todayStart);
        Long todayOrders = ordersMapper.selectCount(todayOrderWrapper);

        // 总用户数
        Long totalUsers = userMapper.selectCount(null);

        map.put("totalSales", totalSales != null ? totalSales : 0);
        map.put("totalOrders", totalOrders);
        map.put("todayOrders", todayOrders);
        map.put("totalUsers", totalUsers);

        // 图表数据 (最近7天)
        List<String> dates = new ArrayList<>();
        List<BigDecimal> salesList = new ArrayList<>();
        List<Long> orderList = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dates.add(date.format(DateTimeFormatter.ofPattern("MM-dd")));

            LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);

            QueryWrapper<Orders> daySalesWrapper = new QueryWrapper<>();
            daySalesWrapper.select("IFNULL(sum(total_amount), 0) as total")
                    .ge("order_create_time", start)
                    .le("order_create_time", end)
                    .in("order_status", 1, 2, 3);
            Map<String, Object> daySalesRes = ordersMapper.selectMaps(daySalesWrapper).get(0);
            salesList.add((BigDecimal) daySalesRes.get("total"));

            QueryWrapper<Orders> dayOrderWrapper = new QueryWrapper<>();
            dayOrderWrapper.ge("order_create_time", start)
                    .le("order_create_time", end);
            orderList.add(ordersMapper.selectCount(dayOrderWrapper));
        }

        map.put("dates", dates);
        map.put("sales", salesList);
        map.put("orders", orderList);

        return Result.success(map);
    }

    /** 销售趋势 - 日/周/月切换 */
    @GetMapping("/trend")
    public Result<Map<String, Object>> trend(@RequestParam(defaultValue = "day") String dimension,
                                              @RequestParam(defaultValue = "7") int count) {
        Map<String, Object> map = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<BigDecimal> salesList = new ArrayList<>();
        List<Long> orderList = new ArrayList<>();

        LocalDate today = LocalDate.now();

        switch (dimension) {
            case "week" -> {
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

        map.put("labels", labels);
        map.put("sales", salesList);
        map.put("orders", orderList);
        return Result.success(map);
    }

    private BigDecimal getSalesBetween(LocalDateTime start, LocalDateTime end) {
        QueryWrapper<Orders> wrapper = new QueryWrapper<>();
        wrapper.select("IFNULL(sum(total_amount), 0) as total")
                .ge("order_create_time", start)
                .le("order_create_time", end)
                .in("order_status", 1, 2, 3);
        Map<String, Object> res = ordersMapper.selectMaps(wrapper).get(0);
        Object total = res.get("total");
        return total instanceof BigDecimal ? (BigDecimal) total : BigDecimal.ZERO;
    }

    private Long getOrderCountBetween(LocalDateTime start, LocalDateTime end) {
        QueryWrapper<Orders> wrapper = new QueryWrapper<>();
        wrapper.ge("order_create_time", start)
                .le("order_create_time", end);
        return ordersMapper.selectCount(wrapper);
    }
}
