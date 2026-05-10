<template>
    <el-card>
        <template #header>
            <div class="card-header">
                <span>用户浏览日志 (最近100条)</span>
                <el-button @click="fetchList">刷新</el-button>
            </div>
        </template>

        <el-table :data="tableData" style="width: 100%" border stripe>
            <el-table-column prop="logId" label="ID" width="80" align="center" />

            <el-table-column prop="username" label="访问用户" width="150">
                <template #default="scope">
                    <el-tag size="small">{{ scope.row.username }}</el-tag>
                </template>
            </el-table-column>

            <el-table-column prop="productName" label="浏览商品" min-width="200" />

            <el-table-column prop="productId" label="商品ID" width="100" align="center" />

            <el-table-column prop="browseTime" label="浏览时间" width="180">
                <template #default="scope">
                    {{ formatTime(scope.row.browseTime) }}
                </template>
            </el-table-column>
        </el-table>
    </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '../../utils/request'

const tableData = ref([])

const fetchList = async () => {
    const res: any = await request.get('/admin/log/list')
    tableData.value = res
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