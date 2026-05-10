<template>
  <div>
    <!-- 顶部统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>总用户数</template>
          <div class="stat-num">{{ overview.totalUsers }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>总销售额</template>
          <div class="stat-num">¥ {{ overview.totalSales }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>总订单量</template>
          <div class="stat-num">{{ overview.totalOrders }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>有画像用户</template>
          <div class="stat-num">{{ overview.profiledUsers }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Tab 切换不同分析维度 -->
    <el-card style="margin-top: 20px">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 销售趋势 -->
        <el-tab-pane label="销售趋势" name="trend">
          <div class="tab-toolbar">
            <el-radio-group v-model="trendPeriod" @change="fetchTrend">
              <el-radio-button value="day">按天</el-radio-button>
              <el-radio-button value="week">按周</el-radio-button>
              <el-radio-button value="month">按月</el-radio-button>
            </el-radio-group>
          </div>
          <div ref="trendChartRef" style="width: 100%; height: 400px;"></div>
        </el-tab-pane>

        <!-- 用户画像 -->
        <el-tab-pane label="用户画像" name="profile">
          <el-table :data="profileList" style="width: 100%" border stripe>
            <el-table-column prop="userId" label="用户ID" width="80" align="center" />
            <el-table-column prop="username" label="用户名" width="130" />
            <el-table-column prop="province" label="省份" width="100" />
            <el-table-column prop="city" label="城市" width="100" />
            <el-table-column prop="purchaseLevel" label="消费等级" width="120" align="center">
              <template #default="scope">
                <el-tag :type="getLevelColor(scope.row.purchaseLevel)" size="small">
                  {{ scope.row.purchaseLevel || '未知' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="totalSpent" label="总消费" width="120">
              <template #default="scope">
                <span style="color: #f56c6c">¥{{ scope.row.totalSpent }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="totalOrders" label="订单数" width="100" align="center" />
            <el-table-column prop="preferredCategoryName" label="偏好品类" width="130" />
            <el-table-column prop="lastPurchaseTime" label="最近购买" min-width="180">
              <template #default="scope">
                {{ formatTime(scope.row.lastPurchaseTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 销售排行 -->
        <el-tab-pane label="销售排行" name="ranking">
          <el-table :data="rankingList" style="width: 100%" border stripe>
            <el-table-column label="排名" width="80" align="center">
              <template #default="scope">
                <span :class="{ 'top-three': scope.$index < 3 }" class="rank-num">{{ scope.$index + 1 }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="productName" label="商品名称" min-width="250" />
            <el-table-column prop="totalSales" label="销售数量" width="120" align="center" />
            <el-table-column prop="totalRevenue" label="销售总额" width="150">
              <template #default="scope">
                <span style="color: #f56c6c">¥{{ scope.row.totalRevenue }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 异常检测 -->
        <el-tab-pane label="异常检测" name="anomaly">
          <el-table :data="anomalyList" style="width: 100%" border stripe>
            <el-table-column prop="date" label="日期" width="150" />
            <el-table-column prop="amount" label="销售额" width="150">
              <template #default="scope">
                <span style="color: #f56c6c">¥{{ scope.row.amount }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="avgAmount" label="平均销售额" width="150">
              <template #default="scope">
                ¥{{ scope.row.avgAmount }}
              </template>
            </el-table-column>
            <el-table-column prop="deviation" label="偏离程度" width="120" align="center">
              <template #default="scope">
                <el-tag type="danger" size="small">{{ scope.row.deviation }}%</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="方向" width="100" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.direction === 'UP' ? 'danger' : 'warning'" size="small">
                  {{ scope.row.direction === 'UP' ? '异常偏高' : '异常偏低' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="anomalyList.length === 0" class="no-anomaly">
            <el-empty description="暂无异常数据，一切正常！" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import request from '../../utils/request'
import * as echarts from 'echarts'

const activeTab = ref('trend')
const trendPeriod = ref('day')
const trendChartRef = ref(null)

const overview = ref({
  totalUsers: 0,
  totalSales: 0,
  totalOrders: 0,
  profiledUsers: 0
})

const profileList = ref([])
const rankingList = ref([])
const anomalyList = ref([])

const fetchOverview = async () => {
  const res: any = await request.get('/analysis/overview')
  if (res) {
    overview.value = res
  }
}

const fetchTrend = async () => {
  const res: any = await request.get('/analysis/trend', {
    params: { period: trendPeriod.value }
  })
  await nextTick()
  initTrendChart(res)
}

const fetchProfile = async () => {
  const res: any = await request.get('/analysis/user-profiles')
  profileList.value = res
}

const fetchRanking = async () => {
  const res: any = await request.get('/analysis/sales-ranking')
  rankingList.value = res
}

const fetchAnomaly = async () => {
  const res: any = await request.get('/analysis/anomaly')
  anomalyList.value = res
}

const handleTabChange = (tab: string) => {
  if (tab === 'trend') fetchTrend()
  else if (tab === 'profile') fetchProfile()
  else if (tab === 'ranking') fetchRanking()
  else if (tab === 'anomaly') fetchAnomaly()
}

const initTrendChart = (data: any) => {
  if (!trendChartRef.value || !data) return
  const myChart = echarts.init(trendChartRef.value)
  const option = {
    title: { text: '' },
    tooltip: { trigger: 'axis' },
    legend: { data: ['销售额', '订单量'], top: '5%' },
    grid: { top: '20%', left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.dates || []
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '销售额',
        type: 'line',
        smooth: true,
        data: data.sales || [],
        itemStyle: { color: '#409EFF' },
        areaStyle: { opacity: 0.2 }
      },
      {
        name: '订单量',
        type: 'bar',
        barWidth: '20%',
        data: data.orders || [],
        itemStyle: { color: '#67C23A' }
      }
    ]
  }
  myChart.setOption(option)
  window.addEventListener('resize', () => myChart.resize())
}

const getLevelColor = (level: string) => {
  const map: Record<string, string> = {
    '高': 'danger',
    '中': 'warning',
    '低': 'info'
  }
  return map[level] || 'info'
}

const formatTime = (val: string) => {
  return val ? val.replace('T', ' ') : ''
}

onMounted(() => {
  fetchOverview()
  fetchTrend()
})
</script>

<style scoped>
.stat-num {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.tab-toolbar {
  margin-bottom: 15px;
}

.rank-num {
  display: inline-block;
  width: 24px;
  height: 24px;
  line-height: 24px;
  border-radius: 50%;
  text-align: center;
  font-size: 12px;
  background: #e0e0e0;
  color: #666;
}

.top-three {
  background: #ff6700;
  color: #fff;
}

.no-anomaly {
  padding: 40px 0;
}
</style>
