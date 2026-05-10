<template>
    <el-card>
        <template #header>
            <div class="card-header">
                <span>分类与系列管理</span>
            </div>
        </template>

        <el-tabs v-model="activeTab" type="card" @tab-change="handleTabChange">
            <el-tab-pane label="一级分类 (Category)" name="category">
                <div class="action-bar">
                    <el-button type="primary" @click="openCategoryDialog">
                        <el-icon>
                            <Plus />
                        </el-icon> 新增分类
                    </el-button>
                </div>
                <el-table :data="categoryList" border stripe style="width: 100%; margin-top: 20px">
                    <el-table-column prop="categoryId" label="ID" width="80" />
                    <el-table-column prop="categoryName" label="分类名称" />
                    <el-table-column prop="categoryIcon" label="图标代码" />
                    <el-table-column label="操作" width="120">
                        <template #default="scope">
                            <el-button type="danger" link @click="deleteCategory(scope.row.categoryId)">删除</el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </el-tab-pane>

            <el-tab-pane label="二级系列 (Product Family)" name="family">
                <div class="action-bar">
                    <el-select v-model="filterCategoryId" placeholder="按分类筛选" clearable @change="fetchFamilyList"
                        style="margin-right: 10px; width: 200px">
                        <el-option v-for="item in categoryList" :key="item.categoryId" :label="item.categoryName"
                            :value="item.categoryId" />
                    </el-select>
                    <el-button type="primary" @click="openFamilyDialog">
                        <el-icon>
                            <Plus />
                        </el-icon> 新增系列
                    </el-button>
                </div>
                <el-table :data="familyList" border stripe style="width: 100%; margin-top: 20px">
                    <el-table-column prop="productFamilyId" label="ID" width="80" />
                    <el-table-column label="所属分类" width="150">
                        <template #default="scope">
                            <el-tag>{{ getCategoryName(scope.row.categoryId) }}</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column prop="productFamilyName" label="系列名称" />
                    <el-table-column label="操作" width="120">
                        <template #default="scope">
                            <el-button type="danger" link
                                @click="deleteFamily(scope.row.productFamilyId)">删除</el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </el-tab-pane>
        </el-tabs>

        <el-dialog v-model="categoryDialogVisible" title="新增一级分类" width="400px">
            <el-form :model="categoryForm">
                <el-form-item label="分类名称" label-width="80px">
                    <el-input v-model="categoryForm.categoryName" placeholder="如: 手机" />
                </el-form-item>
                <el-form-item label="图标" label-width="80px">
                    <el-input v-model="categoryForm.categoryIcon" placeholder="这里是elementUI的icon" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="categoryDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="submitCategory">确定</el-button>
            </template>
        </el-dialog>

        <el-dialog v-model="familyDialogVisible" title="新增二级系列" width="400px">
            <el-form :model="familyForm">
                <el-form-item label="所属分类" label-width="80px">
                    <el-select v-model="familyForm.categoryId" placeholder="请选择" style="width: 100%">
                        <el-option v-for="item in categoryList" :key="item.categoryId" :label="item.categoryName"
                            :value="item.categoryId" />
                    </el-select>
                </el-form-item>
                <el-form-item label="系列名称" label-width="80px">
                    <el-input v-model="familyForm.productFamilyName" placeholder="如: 大米14系列" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="familyDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="submitFamily">确定</el-button>
            </template>
        </el-dialog>
    </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '../../utils/request'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('category')
const categoryList = ref<any[]>([])
const familyList = ref<any[]>([])
const filterCategoryId = ref('') 

//一级分类
const categoryDialogVisible = ref(false)
const categoryForm = reactive({ categoryName: '', categoryIcon: '' })

const fetchCategoryList = async () => {
    const res: any = await request.get('/admin/category/list')
    categoryList.value = res
}

const openCategoryDialog = () => {
    categoryForm.categoryName = ''
    categoryForm.categoryIcon = ''
    categoryDialogVisible.value = true
}

const submitCategory = async () => {
    if (!categoryForm.categoryName) return ElMessage.warning('请输入名称')
    await request.post('/admin/category/add', categoryForm)
    ElMessage.success('添加成功')
    categoryDialogVisible.value = false
    fetchCategoryList() //刷新列表
}

const deleteCategory = (id: number) => {
    ElMessageBox.confirm('删除分类可能会影响该分类下的商品，确定删除吗？', '警告', { type: 'warning' })
        .then(async () => {
            await request.delete(`/admin/category/delete/${id}`)
            ElMessage.success('删除成功')
            fetchCategoryList()
        })
}

// 二级系列
const familyDialogVisible = ref(false)
const familyForm = reactive({ categoryId: '', productFamilyName: '' })

const fetchFamilyList = async () => {
    let url = '/admin/product-family/list'
    if (filterCategoryId.value) {
        url += `?categoryId=${filterCategoryId.value}`
    }
    const res: any = await request.get(url)
    familyList.value = res
}

const openFamilyDialog = () => {
    familyForm.categoryId = ''
    familyForm.productFamilyName = ''
    familyDialogVisible.value = true
}

const submitFamily = async () => {
    if (!familyForm.categoryId || !familyForm.productFamilyName) return ElMessage.warning('请补全信息')
    await request.post('/admin/product-family/add', familyForm)
    ElMessage.success('添加成功')
    familyDialogVisible.value = false
    fetchFamilyList()
}

const deleteFamily = (id: number) => {
    ElMessageBox.confirm('确定删除该系列吗？', '提示', { type: 'warning' })
        .then(async () => {
            await request.delete(`/admin/product-family/delete/${id}`)
            ElMessage.success('删除成功')
            fetchFamilyList()
        })
}

//根据ID显示分类名称
const getCategoryName = (id: number) => {
    const item = categoryList.value.find(c => c.categoryId === id)
    return item ? item.categoryName : id
}

const handleTabChange = () => {
    if (activeTab.value === 'category') {
        fetchCategoryList()
    } else {
        
        if (categoryList.value.length === 0) fetchCategoryList()
        fetchFamilyList()
    }
}

onMounted(() => {
    fetchCategoryList()
})
</script>

<style scoped>
.action-bar {
    margin-bottom: 20px;
}
</style>