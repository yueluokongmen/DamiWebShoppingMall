<template>
    <div>
        <el-row :gutter="20">
            <el-col :span="6">
                <el-card shadow="hover">
                    <template #header>总销售额</template>
                    <div class="stat-num">¥ {{ statsData.totalSales }}</div>
                </el-card>
            </el-col>
            <el-col :span="6">
                <el-card shadow="hover">
                    <template #header>总订单量</template>
                    <div class="stat-num">{{ statsData.totalOrders }}</div>
                </el-card>
            </el-col>
            <el-col :span="6">
                <el-card shadow="hover">
                    <template #header>今日订单</template>
                    <div class="stat-num">{{ statsData.todayOrders }}</div>
                </el-card>
            </el-col>
            <el-col :span="6">
                <el-card shadow="hover">
                    <template #header>总用户数</template>
                    <div class="stat-num">{{ statsData.totalUsers }}</div>
                </el-card>
            </el-col>
        </el-row>

        <el-card style="margin-top: 20px">
            <template #header>销售趋势分析 (近7天)</template>
            <div ref="chartRef" style="width: 100%; height: 400px;"></div>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import * as echarts from 'echarts'
import request from '../../utils/request'

const chartRef = ref(null)

const statsData = reactive({
    totalSales: 0,
    totalOrders: 0,
    todayOrders: 0,
    totalUsers: 0
})

onMounted(async () => {
    const res: any = await request.get('/admin/stats/data')

    //填充顶部卡片数据
    statsData.totalSales = res.totalSales
    statsData.totalOrders = res.totalOrders
    statsData.todayOrders = res.todayOrders
    statsData.totalUsers = res.totalUsers

    //初始化图表
    initChart(res)
})

const initChart = (data: any) => {
    if (!chartRef.value) return 

    const myChart = echarts.init(chartRef.value)

    const option = {
        title: { text: '' },
        tooltip: { trigger: 'axis' },
        legend: { data: ['销售额', '订单量'],
            top: '5%',
         },
        grid: {
            top: '20%',
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        xAxis: {
            type: 'category',
            boundaryGap: false,
            data: data.dates //日期数组
        },
        yAxis: { type: 'value' },
        series: [
            {
                name: '销售额',
                type: 'line',
                smooth: true,
                data: data.sales, //销售额
                itemStyle: { color: '#409EFF' },
                areaStyle: { opacity: 0.2 }
            },
            {
                name: '订单量',
                type: 'bar',
                barWidth: '20%',
                data: data.orders, //订单量
                itemStyle: { color: '#67C23A' }
            }
        ]
    }

    myChart.setOption(option)
    window.addEventListener('resize', () => myChart.resize())
}
</script>

<style scoped>
.stat-num {
    font-size: 24px;
    font-weight: bold;
    color: #303133;
}
</style>