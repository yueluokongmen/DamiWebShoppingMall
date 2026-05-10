<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>销售人员管理</span>
        <el-button type="primary" @click="showAddDialog">添加销售人员</el-button>
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

      <el-table-column label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '正常' : '已禁用' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="lastLoginTime" label="最后登录" width="180">
        <template #default="scope">
          {{ formatTime(scope.row.lastLoginTime) }}
        </template>
      </el-table-column>

      <el-table-column prop="lastLoginIp" label="最后登录IP" width="150">
        <template #default="scope">
          {{ scope.row.lastLoginIp || '-' }}
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="创建时间" min-width="180">
        <template #default="scope">
          {{ formatTime(scope.row.createTime) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="200" fixed="right" align="center">
        <template #default="scope">
          <el-button v-if="scope.row.status === 1" type="danger" size="small" link
            @click="handleDisable(scope.row.userId)">
            禁用
          </el-button>
          <el-button v-else type="success" size="small" link
            @click="handleEnable(scope.row.userId)">
            启用
          </el-button>
          <el-button type="warning" size="small" link @click="handleReset(scope.row.userId)">
            重置密码
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加对话框 -->
    <el-dialog v-model="addDialogVisible" title="添加销售人员" width="450px">
      <el-form :model="addForm" :rules="addRules" ref="addFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="addForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="addForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="addForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdd" :loading="addLoading">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import request from '../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'

const tableData = ref([])
const addDialogVisible = ref(false)
const addLoading = ref(false)
const addFormRef = ref<FormInstance>()

const addForm = reactive({
  username: '',
  password: '',
  nickname: ''
})

const addRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

const fetchList = async () => {
  const res: any = await request.get('/admin/sales/list')
  tableData.value = res
}

const showAddDialog = () => {
  addForm.username = ''
  addForm.password = ''
  addForm.nickname = ''
  addDialogVisible.value = true
}

const handleAdd = async () => {
  await addFormRef.value?.validate()
  addLoading.value = true
  try {
    await request.post('/admin/sales/add', addForm)
    ElMessage.success('添加成功')
    addDialogVisible.value = false
    fetchList()
  } finally {
    addLoading.value = false
  }
}

const handleDisable = (id: number) => {
  ElMessageBox.confirm('确定要禁用该销售人员吗？', '警告', { type: 'warning' }).then(async () => {
    await request.post(`/admin/sales/disable/${id}`)
    ElMessage.success('已禁用')
    fetchList()
  })
}

const handleEnable = (id: number) => {
  ElMessageBox.confirm('确定要启用该销售人员吗？', '提示', { type: 'info' }).then(async () => {
    await request.post(`/admin/sales/enable/${id}`)
    ElMessage.success('已启用')
    fetchList()
  })
}

const handleReset = (id: number) => {
  ElMessageBox.confirm('确定要将该销售人员的密码重置为 123456 吗？', '警告', { type: 'warning' }).then(async () => {
    await request.post(`/admin/sales/reset-pwd/${id}`)
    ElMessage.success('重置成功')
  })
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
</style>
