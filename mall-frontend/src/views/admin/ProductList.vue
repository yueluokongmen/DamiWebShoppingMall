<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span style="font-weight: bold">商品管理列表</span>
        <el-button type="primary" @click="$router.push('/admin/product/add')">
          <el-icon>
            <Plus />
          </el-icon> 发布新商品
        </el-button>
      </div>
    </template>

    <el-table :data="tableData" style="width: 100%" border stripe>
      <el-table-column prop="productId" label="ID" width="60" align="center" />
      <el-table-column label="主图" width="80" align="center">
        <template #default="scope">
          <el-image v-if="scope.row.images && scope.row.images.length > 0"
            style="width: 40px; height: 40px; border-radius: 4px" :src="scope.row.images[0]" preview-teleported />
        </template>
      </el-table-column>
      <el-table-column prop="productName" label="商品名称" min-width="200" show-overflow-tooltip />
      <el-table-column prop="spuPrice" label="起售价" width="100" align="center">
        <template #default="scope">￥{{ scope.row.spuPrice }}</template>
      </el-table-column>

      <el-table-column label="上架状态" width="100" align="center">
        <template #default="scope">
          <el-switch v-model="scope.row.productStatus" :active-value="1" :inactive-value="0"
            @change="(val: any) => handleStatusChange(scope.row.productId, val)" />
        </template>
      </el-table-column>

      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="scope">
          <el-button type="warning" size="small" plain @click="openSkuDialog(scope.row)">
            SKU配置
          </el-button>
          <el-button type="primary" size="small" link @click="handleEdit(scope.row.productId)">
            编辑
          </el-button>
          <el-button type="danger" size="small" link @click="handleDelete(scope.row.productId)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="skuDialogVisible" title="SKU 规格管理" width="800px">
      <div style="margin-bottom: 15px">
        <el-button type="primary" @click="addSkuRow">
          <el-icon>
            <Plus />
          </el-icon> 添加新规格
        </el-button>
        <span style="font-size: 12px; color: #999; margin-left: 10px">
          当前正在管理: <b style="color:#333">{{ currentSpu.productName }}</b>
        </span>
      </div>

      <el-table :data="skuList" border style="width: 100%">
        <el-table-column label="规格图" width="80" align="center">
          <template #default="scope">
            <el-upload class="sku-uploader" :action="uploadUrl" :headers="uploadHeaders" :show-file-list="false"
              :on-success="(res: any) => handleSkuImgSuccess(res, scope.row)">
              <img v-if="scope.row.skuImage" :src="scope.row.skuImage" class="sku-img" />
              <el-icon v-else class="sku-uploader-icon">
                <Plus />
              </el-icon>
            </el-upload>
          </template>
        </el-table-column>

        <el-table-column label="规格名称 (如: 红色 64G)" min-width="180">
          <template #default="scope">
            <el-input v-model="scope.row.skuName" placeholder="请输入规格名" size="small" />
          </template>
        </el-table-column>

        <el-table-column label="价格" width="120">
          <template #default="scope">
            <el-input-number v-model="scope.row.skuPrice" :min="0" :precision="2" size="small" style="width:100%" />
          </template>
        </el-table-column>

        <el-table-column label="库存" width="120">
          <template #default="scope">
            <el-input-number v-model="scope.row.stock" :min="0" size="small" style="width:100%" />
          </template>
        </el-table-column>

        <el-table-column label="操作" width="140" align="center">
          <template #default="scope">
            <el-button type="success" size="small" link @click="saveSku(scope.row)">保存</el-button>
            <el-button type="danger" size="small" link @click="deleteSku(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="skuDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import request from '../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const tableData = ref([])

// SKU 相关数据
const skuDialogVisible = ref(false)
const currentSpu = ref<any>({})
const skuList = ref<any[]>([])

// 上传配置
const uploadUrl = `${import.meta.env.VITE_API_BASE_URL}/file/upload`
const uploadHeaders = computed(() => ({ token: localStorage.getItem('token') || '' }))

onMounted(() => { fetchList() })

const fetchList = async () => {
  const res: any = await request.get('/admin/product/list')
  tableData.value = res
}

const handleStatusChange = async (id: number, status: number) => {
  try {
    await request.post(`/admin/product/status/${id}/${status}`)
    ElMessage.success(status === 1 ? '已上架' : '已下架')
  } catch (e) { fetchList() }
}

const handleEdit = (id: number) => {
  router.push({ path: '/admin/product/add', query: { id } })
}

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' })
    .then(async () => {
      await request.delete(`/admin/product/delete/${id}`)
      ElMessage.success('删除成功')
      fetchList()
    })
}


//打开SKU弹窗
const openSkuDialog = async (spu: any) => {
  currentSpu.value = spu
  skuDialogVisible.value = true
  await fetchSkuList(spu.productId)
}

//获取SKU列表
const fetchSkuList = async (productId: number) => {
  const res: any = await request.get(`/admin/sku/list?productId=${productId}`)
  skuList.value = res
}

//前端添加一行空数据 (此时还没存数据库)
const addSkuRow = () => {
  skuList.value.push({
    productId: currentSpu.value.productId,
    skuName: '',
    skuImage: currentSpu.value.images && currentSpu.value.images[0] ? currentSpu.value.images[0] : '', 
    skuPrice: currentSpu.value.spuPrice,
    stock: 999,
    specs: {} //暂时留空,可以在这里扩展
  })
}

//SKU图片上传成功
const handleSkuImgSuccess = (res: any, row: any) => {
  if (res.code === 200) {
    row.skuImage = res.data
  } else {
    ElMessage.error(res.msg)
  }
}

//保存单行SKU
const saveSku = async (row: any) => {
    if (!row.skuName) return ElMessage.warning('请输入规格名称')

    // 输入示例："颜色:黑色 内存:256G"
    // 解析目标：row.specs = { "颜色": "黑色", "内存": "256G" }
    
    const specsObj: any = {}
    //按空格分割多种属性
    const parts = row.skuName.split(' ') 
    
    let isParseSuccess = false
    
    parts.forEach((part: string) => {
        //按冒号分割键和值 (支持中英文冒号)
        const [key, val] = part.split(/[:：]/) 
        if (key && val) {
            specsObj[key] = val
            isParseSuccess = true
        }
    })

    if (!isParseSuccess) {
        specsObj["规格"] = row.skuName
    }

    row.specs = specsObj

    //save SKU
    await request.post('/admin/sku/save', row)
    ElMessage.success('保存成功')
    
  
    await updateSpuSpecOptions(row.productId)
    
    fetchSkuList(currentSpu.value.productId) 
}

//自动计算并更新SPU的specOptions
const updateSpuSpecOptions = async (productId: number) => {
    //查出该商品所有 SKU
    const res: any = await request.get(`/admin/sku/list?productId=${productId}`)
    const allSkus = res
    
    //聚合规格
    // 目标结构: [{"label":"颜色", "value":["黑","白"]}, {"label":"内存", "value":["64G","128G"]}]
    const optionsMap = new Map<string, Set<string>>()
    
    allSkus.forEach((sku: any) => {
        if (sku.specs) {
            Object.entries(sku.specs).forEach(([key, val]) => {
                if (!optionsMap.has(key)) {
                    optionsMap.set(key, new Set())
                }
                optionsMap.get(key)?.add(String(val))
            })
        }
    })

    //转换成需要的 List<Map> 结构
    const specOptions = [] as any[]
    optionsMap.forEach((valueSet, key) => {
        specOptions.push({
            label: key,
            value: Array.from(valueSet)
        })
    })

    //调用后端更新 SPU 信息
  
    await request.post('/admin/product/save', {
        productId: productId,
        specOptions: specOptions
    })
}

//删除SKU
const deleteSku = async (row: any) => {
  //如果是还没保存的新行直接前端删
  if (!row.skuId) {
    const index = skuList.value.indexOf(row)
    skuList.value.splice(index, 1)
    return
  }

  ElMessageBox.confirm('确定删除该规格吗？', '提示', { type: 'warning' })
    .then(async () => {
      await request.delete(`/admin/sku/delete/${row.skuId}`)
      ElMessage.success('删除成功')
      fetchSkuList(currentSpu.value.productId)
    })
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sku-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 40px;
  height: 40px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.sku-uploader:hover {
  border-color: #409EFF;
}

.sku-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.sku-uploader-icon {
  font-size: 14px;
  color: #8c939d;
}
</style>