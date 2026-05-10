<template>
    <el-card>
        <template #header>
            <div class="card-header">
                <span>发布新商品 (SPU)</span>
                <el-button @click="$router.back()">返回列表</el-button>
            </div>
        </template>

        <el-form :model="form" ref="formRef" :rules="rules" label-width="120px" style="max-width: 800px">

            <el-form-item label="所属分类" prop="categoryId">
                <el-select v-model="form.categoryId" placeholder="请选择一级分类" @change="handleCategoryChange"
                    style="width: 100%">
                    <el-option v-for="item in categoryList" :key="item.categoryId" :label="item.categoryName"
                        :value="item.categoryId" />
                </el-select>
            </el-form-item>

            <el-form-item label="所属系列" prop="productFamilyId">
                <el-select v-model="form.productFamilyId" placeholder="请先选择分类，再选择系列" :disabled="!form.categoryId"
                    style="width: 100%">
                    <el-option v-for="item in familyList" :key="item.productFamilyId" :label="item.productFamilyName"
                        :value="item.productFamilyId" />
                </el-select>
            </el-form-item>

            <el-form-item label="商品名称" prop="productName">
                <el-input v-model="form.productName" placeholder="例如：大米14 Pro" />
            </el-form-item>

            <el-form-item label="副标题" prop="subtitle">
                <el-input v-model="form.subtitle" placeholder="例如：徕卡光学镜头 | 骁龙8Gen3" />
            </el-form-item>

            <el-form-item label="起售价" prop="spuPrice">
                <el-input-number v-model="form.spuPrice" :precision="2" :step="100" :min="0" style="width: 200px" />
                <span class="tip">元 (仅作为展示起售价)</span>
            </el-form-item>

            <el-form-item label="商品轮播图" prop="images">
                <el-upload v-model:file-list="fileList" :action="uploadUrl" :headers="uploadHeaders"
                    list-type="picture-card" :on-success="handleUploadSuccess" :on-remove="handleRemove" :limit="5">
                    <el-icon>
                        <Plus />
                    </el-icon>
                </el-upload>
                <div class="tip">建议尺寸 800x800，第一张将作为商品主图</div>
            </el-form-item>

            <el-form-item label="商品详情" prop="detail">
                <el-input v-model="form.detail" type="textarea" rows="4" placeholder="请输入纯文本描述" />
            </el-form-item>

            <el-form-item>
                <el-button type="primary" @click="submitForm" :loading="loading">立即发布</el-button>
                <el-button @click="resetForm">重置</el-button>
            </el-form-item>
        </el-form>
    </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router' 

const router = useRouter()
const route = useRoute() // 
const formRef = ref()
const loading = ref(false)
const categoryList = ref<any[]>([])
const familyList = ref<any[]>([])
const fileList = ref<any[]>([])

const uploadUrl = `${import.meta.env.VITE_API_BASE_URL}/file/upload`
const uploadHeaders = computed(() => ({ token: localStorage.getItem('token') || '' }))

//判断是否是编辑模式
const isEdit = computed(() => !!route.query.id)

const form = reactive({
    productId: null, 
    categoryId: '',
    productFamilyId: '',
    productName: '',
    subtitle: '',
    spuPrice: 0,
    detail: '',
    images: [] as string[],
    productStatus: 1
})

const rules = {
    categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
    productFamilyId: [{ required: true, message: '请选择系列', trigger: 'change' }],
    productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
    spuPrice: [{ required: true, message: '请输入价格', trigger: 'blur' }],
    images: [{ required: true, message: '请至少上传一张图片', trigger: 'change' }]
}

onMounted(async () => {
    await fetchCategoryList()

    if (route.query.id) {
        loadDetail(Number(route.query.id))
    }
})

const loadDetail = async (id: number) => {
    const res: any = await request.get(`/admin/product/info/${id}`)

    //填充表单
    Object.assign(form, res)
    if (form.categoryId) {
        await fetchFamilyList(form.categoryId)
    }

    if (form.images && form.images.length > 0) {
        fileList.value = form.images.map(url => ({ name: 'img', url: url }))
    }
}

const fetchCategoryList = async () => {
    const res: any = await request.get('/admin/category/list')
    categoryList.value = res
}

const fetchFamilyList = async (cid: any) => {
    const res: any = await request.get(`/admin/product-family/list?categoryId=${cid}`)
    familyList.value = res
}

const handleCategoryChange = async (newCategoryId: number) => {
    form.productFamilyId = ''
    familyList.value = []
    if (!newCategoryId) return
    await fetchFamilyList(newCategoryId)
}

const handleUploadSuccess = (response: any, uploadFile: any) => {
    if (response.code === 200) {
        form.images.push(response.data)
        ElMessage.success('上传成功')
    } else {
        ElMessage.error(response.msg || '上传失败')
        const index = fileList.value.indexOf(uploadFile)
        if (index !== -1) fileList.value.splice(index, 1)
    }
}

const handleRemove = (uploadFile: any) => {
    const url = uploadFile.response ? uploadFile.response.data : uploadFile.url
    form.images = form.images.filter(item => item !== url)
}

const submitForm = async () => {
    if (!formRef.value) return
    if (form.images.length === 0) return ElMessage.warning('请至少上传一张图片')

    await formRef.value.validate(async (valid: boolean) => {
        if (valid) {
            loading.value = true
            try {
                await request.post('/admin/product/save', form)
                ElMessage.success(isEdit.value ? '修改成功' : '发布成功')
                router.push('/admin/products')
            } catch (e) {
                console.error(e)
            } finally {
                loading.value = false
            }
        }
    })
}

const resetForm = () => {
    formRef.value.resetFields()
    fileList.value = []
    form.images = []
}
</script>


<style scoped>
.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.tip {
    font-size: 12px;
    color: #999;
    margin-left: 10px;
}
</style>