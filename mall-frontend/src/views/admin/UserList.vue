<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>客户管理列表</span>
        <el-button type="primary" @click="fetchList">刷新</el-button>
      </div>
    </template>

    <el-table :data="tableData" style="width: 100%" border stripe>
      <el-table-column prop="userId" label="ID" width="80" align="center" />

      <el-table-column label="头像" width="80" align="center">
        <template #default="scope">
          <el-avatar :size="40"
            :src="scope.row.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
        </template>
      </el-table-column>

      <el-table-column prop="username" label="用户名" width="150" />
      <el-table-column prop="nickname" label="昵称" width="150" />
      <el-table-column prop="mobile" label="手机号" width="150" />

      <el-table-column label="角色" width="100" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.role === 1" type="danger">管理员</el-tag>
          <el-tag v-else-if="scope.row.role === 2" type="warning">销售</el-tag>
          <el-tag v-else type="info">普通用户</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '正常' : '已禁用' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="注册时间" min-width="180">
        <template #default="scope">
          {{ formatTime(scope.row.createTime) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="220" fixed="right" align="center">
        <template #default="scope">
          <el-button v-if="scope.row.status === 1 && scope.row.role !== 1" type="danger" size="small" link
            @click="handleDisable(scope.row.userId)">
            禁用
          </el-button>
          <el-button v-if="scope.row.status !== 1 && scope.row.role !== 1" type="success" size="small" link
            @click="handleEnable(scope.row.userId)">
            启用
          </el-button>
          <el-button type="warning" size="small" link @click="handleReset(scope.row.userId)">
            重置密码
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const tableData = ref([])

const fetchList = async () => {
  const res: any = await request.get('/admin/user/list')
  tableData.value = res
}

const handleDisable = (id: number) => {
  ElMessageBox.confirm('确定要禁用该用户吗？', '警告', { type: 'warning' }).then(async () => {
    await request.post(`/admin/user/disable/${id}`)
    ElMessage.success('已禁用')
    fetchList()
  })
}

const handleEnable = (id: number) => {
  ElMessageBox.confirm('确定要启用该用户吗？', '提示', { type: 'info' }).then(async () => {
    await request.post(`/admin/user/enable/${id}`)
    ElMessage.success('已启用')
    fetchList()
  })
}

const handleReset = (id: number) => {
  ElMessageBox.confirm('确定要将该用户的密码重置为 123456 吗？', '警告', {
    type: 'warning'
  }).then(async () => {
    await request.post(`/admin/user/reset-pwd/${id}`)
    ElMessage.success('重置成功')
  })
}

//简单的时间格式化
const formatTime = (val: string) => {
  return val ? val.replace('T', ' ') : ''
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>