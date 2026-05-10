<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>操作日志</span>
        <div>
          <el-select v-model="searchType" placeholder="操作类型" style="width: 140px; margin-right: 10px;" clearable
            @change="fetchList">
            <el-option label="新增" value="CREATE" />
            <el-option label="修改" value="UPDATE" />
            <el-option label="删除" value="DELETE" />
            <el-option label="查询" value="QUERY" />
            <el-option label="登录" value="LOGIN" />
          </el-select>
          <el-input v-model="searchUsername" placeholder="搜索用户名" style="width: 200px; margin-right: 10px;"
            clearable @clear="fetchList" @keyup.enter="fetchList" />
          <el-button type="primary" @click="fetchList">查询</el-button>
        </div>
      </div>
    </template>

    <el-table :data="tableData" style="width: 100%" border stripe>
      <el-table-column prop="logId" label="ID" width="80" align="center" />

      <el-table-column prop="username" label="操作用户" width="130">
        <template #default="scope">
          <el-tag size="small">{{ scope.row.username }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="角色" width="100" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.role === 1" type="danger" size="small">管理员</el-tag>
          <el-tag v-else-if="scope.row.role === 2" type="warning" size="small">销售</el-tag>
          <el-tag v-else type="info" size="small">用户</el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="operation" label="操作内容" min-width="250" show-overflow-tooltip />

      <el-table-column label="类型" width="100" align="center">
        <template #default="scope">
          <el-tag :type="getTypeColor(scope.row.operationType)" size="small">
            {{ scope.row.operationType }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="targetType" label="目标类型" width="120" />

      <el-table-column prop="targetId" label="目标ID" width="100" align="center" />

      <el-table-column prop="opIp" label="操作IP" width="140" />

      <el-table-column prop="opTime" label="操作时间" width="180">
        <template #default="scope">
          {{ formatTime(scope.row.opTime) }}
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-box">
      <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :page-sizes="[10, 20, 50]"
        :total="total" layout="total, sizes, prev, pager, next, jumper" @size-change="fetchList"
        @current-change="fetchList" />
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '../../utils/request'

const tableData = ref([])
const searchUsername = ref('')
const searchType = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const fetchList = async () => {
  const res: any = await request.get('/admin/log/operation-list', {
    params: {
      page: page.value,
      pageSize: pageSize.value,
      username: searchUsername.value || undefined,
      operationType: searchType.value || undefined
    }
  })
  tableData.value = res.records || res.list || res
  total.value = res.total || 0
}

const getTypeColor = (type: string) => {
  const map: Record<string, string> = {
    CREATE: 'success',
    UPDATE: 'warning',
    DELETE: 'danger',
    QUERY: 'info',
    LOGIN: 'primary'
  }
  return map[type] || 'info'
}

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

.pagination-box {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
