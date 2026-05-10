<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>订单管理列表</span>
        <el-button type="primary" @click="fetchOrders">刷新列表</el-button>
      </div>
    </template>

    <el-table :data="tableData" style="width: 100%" border stripe>
      <el-table-column prop="orderNo" label="订单编号" width="220" />

      <el-table-column label="购买商品" min-width="200">
        <template #default="scope">
          <div v-for="item in scope.row.orderItems" :key="item.itemId" class="order-item">
            <el-image :src="item.skuImage" style="width: 30px; height: 30px; margin-right: 5px" />
            <span>{{ item.skuName }} x {{ item.itemQuantity }}</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="totalAmount" label="总金额" width="100">
        <template #default="scope">
          <span style="color: #f56c6c">￥{{ scope.row.totalAmount }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="orderStatus" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.orderStatus)">
            {{ getStatusText(scope.row.orderStatus) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="下单时间" width="180">
        <template #default="scope">
          {{ formatTime(scope.row.createTime) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button v-if="scope.row.orderStatus === 1" type="success" size="small"
            @click="handleDelivery(scope.row.orderNo)">
            发货
          </el-button>
          <el-button v-if="scope.row.orderStatus === 2" type="primary" size="small"
            @click="handleComplete(scope.row.orderNo)">
            完成订单
          </el-button>
          <span v-if="scope.row.orderStatus !== 1 && scope.row.orderStatus !== 2" style="color: #999; font-size: 12px">无需操作</span>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

//列表数据
const tableData = ref([])

//页面加载时自动获取数据
onMounted(() => {
  fetchOrders()
})

//获取订单列表
const fetchOrders = async () => {
  const res: any = await request.get('/admin/order/list')
  tableData.value = res
}

//发货
const handleDelivery = (orderNo: string) => {
  ElMessageBox.confirm(
    '确定要为这个订单发货吗？',
    '发货确认',
    {
      confirmButtonText: '确定发货',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    await request.post(`/admin/order/delivery/${orderNo}`)
    ElMessage.success('发货成功！')
    fetchOrders() //刷新列表看状态变化
  })
}

//状态文字转换
const getStatusText = (status: number) => {
  switch (status) {
    case 0: return '待付款'
    case 1: return '待发货' //已支付
    case 2: return '已发货'
    case 3: return '已完成'
    case 4: return '已取消'
    default: return '未知'
  }
}

//状态颜色转换
const getStatusType = (status: number) => {
  switch (status) {
    case 0: return 'danger'  //红色
    case 1: return 'warning' //橙色
    case 2: return 'primary' //蓝色
    case 3: return 'success' //绿色
    default: return 'info'
  }
}

//时间格式化
const formatTime = (timeStr: string) => {
  return timeStr ? timeStr.replace('T', ' ') : ''
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-item {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
  font-size: 12px;
  color: #666;
}
</style>