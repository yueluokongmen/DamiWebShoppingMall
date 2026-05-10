<template>
  <div class="detail-page">
    <div class="site-header">
      <div class="container header-content">
        <h2 class="logo" @click="$router.push('/')">大米商城</h2>
        <div class="header-info">
          <span class="product-name">{{ spu.productName || '商品详情' }}</span>
          <span class="sep">|</span>
          <span class="link" @click="$router.push('/')">返回首页</span>
        </div>
      </div>
    </div>

    <div class="container main-content" v-if="spu.productId">
      <div class="img-box">
        <img :src="displayImage" alt="">
      </div>

      <div class="info-box">
        <h1 class="title">{{ spu.productName }}</h1>
        <p class="desc">
          <span class="highlight">「购机赠大米手环」</span>
          {{ spu.subtitle }}
        </p>

        <div class="price-box">
          <span class="price">{{ displayPrice }} 元</span>
        </div>

        <div class="line"></div>

        <div class="option-box" v-for="(spec, index) in spu.specOptions" :key="index">
          <div class="option-title">选择{{ spec.label }}</div>
          <div class="option-list">
            <div class="option-item" :class="{ active: selectedSpecs[spec.label] === val }" v-for="val in spec.value"
              :key="val" @click="selectSpec(spec.label, val)">
              {{ val }}
            </div>
          </div>
        </div>

        <div class="total-box">
          <div class="item">
            <div>
              {{ spu.productName }}
              <span v-for="(val, key) in selectedSpecs" :key="key" style="margin-left:5px">{{ val }}</span>
            </div>
            <span>{{ displayPrice }} 元</span>
          </div>
          <div class="total-price">
            总计：{{ displayPrice }} 元
          </div>
        </div>

        <div class="btn-box">
          <el-button color="#ff6700" size="large" class="buy-btn" @click="handleAddToCart" :disabled="!currentSku">
            {{ currentSku ? '加入购物车' : '该组合缺货' }}
          </el-button>
          <el-button type="info" size="large" class="like-btn">喜欢</el-button>
        </div>
      </div>
    </div>

    <div class="container loading-box" v-else>
      <el-skeleton :rows="10" animated />
    </div>
    <div class="container detail-content" v-if="spu.detail">
      <h3 class="section-title">商品详情</h3>
      <div class="content">{{ spu.detail }}</div>
    </div>

    <!-- 推荐模块 -->
    <div class="container recommend-section" v-if="recommendList.length > 0">
      <h3 class="section-title">看了又看</h3>
      <div class="recommend-list">
        <div v-for="item in recommendList" :key="item.productId" class="recommend-card" @click="goDetail(item.productId)">
          <div class="rec-img-wrap">
            <img :src="getFirstImage(item.images)" alt="" />
          </div>
          <p class="rec-name">{{ item.productName }}</p>
          <p class="rec-price">¥ {{ item.spuPrice }}</p>
        </div>
      </div>
    </div>
    <div class="site-footer">
      <div class="container">大米商城 © 2025 Mi Mall Demo</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../utils/request'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const spu = ref<any>({})
const skuList = ref<any[]>([]) //存储所有 SKU
const selectedSpecs = reactive<any>({}) //存储用户选中的规格，例: { "颜色": "黑色", "版本": "12+256" }
const recommendList = ref<any[]>([]) //推荐列表

//计算当前选中的 SKU
const currentSku = computed(() => {
  return skuList.value.find(sku => {
    const specs = sku.specs || {}
    //检查每一项选中的规格是否都匹配
    return Object.keys(selectedSpecs).every(key => specs[key] === selectedSpecs[key])
  })
})

//动态显示价格
const displayPrice = computed(() => {
  if (currentSku.value) {
    return currentSku.value.skuPrice
  }
  return spu.value.spuPrice //没选中或匹配不到时，显示默认起售价
})

//动态显示图片
const displayImage = computed(() => {
  if (currentSku.value && currentSku.value.skuImage) {
    return currentSku.value.skuImage
  }
  // 默认取SPU的第一张图
  if (spu.value.images && spu.value.images.length > 0) {
    return spu.value.images[0]
  }
  return ''
})

//加载数据
const loadDetail = async () => {
  const id = route.params.id
  if (!id) return

  try {
    //调用接口获取spu+skuList
    const res = await request.get(`/spu/detail/${id}`) as any
    const spuData = res.spu
    const skuData = res.skuList

    // console.log('原始 spuData.specOptions:', spuData.specOptions)

    if (typeof spuData.images === 'string') spuData.images = JSON.parse(spuData.images)
    if (typeof spuData.specOptions === 'string') spuData.specOptions = JSON.parse(spuData.specOptions)

    if (Array.isArray(spuData.specOptions)) {
      spuData.specOptions.forEach((item: any) => {
        if (typeof item.list === 'string') {
          try {
            item.list = JSON.parse(item.list)
          } catch (e) { }
        }
      })
    }

    // console.log('处理后的 specOptions:', spuData.specOptions)


    skuData.forEach((item: any) => {
      if (typeof item.specs === 'string') {
        try {
          item.specs = JSON.parse(item.specs)
        } catch (e) { }
      }
    })

    spu.value = spuData
    skuList.value = skuData

    initSpecs()
    if (userStore.userInfo.userId) {
    recordBrowseLog(spuData)
}
    fetchRecommend(id)
  } catch (error) {
    console.error(error)
    ElMessage.error('获取商品详情失败')
  }
}

//初始化规格选中状态
const initSpecs = () => {
  if (spu.value.specOptions && spu.value.specOptions.length > 0) {
    spu.value.specOptions.forEach((option: any) => {

      if (option.value && option.value.length > 0) {
        selectedSpecs[option.label] = option.value[0]
      }
    })
  }
}

//用户点击规格
const selectSpec = (key: string, value: string) => {
  selectedSpecs[key] = value
}
//记录浏览日志
const recordBrowseLog = async (product: any) => {
  try {
    await request.post('/user/log/add', {
      userId: userStore.userInfo.userId,
      username: userStore.userInfo.username,
      productId: product.productId,
      productName: product.productName
    })
  } catch (e) {
    //日志记录失败
    console.error('Log error', e)
  }
}
//获取推荐商品
const fetchRecommend = async (productId: any) => {
  try {
    const res: any = await request.get('/recommend/similar', {
      params: { productId }
    })
    recommendList.value = res || []
  } catch (e) {
    console.error('Recommend error', e)
  }
}

//获取商品第一张图
const getFirstImage = (images: any) => {
  if (!images) return ''
  if (typeof images === 'string') {
    try { const arr = JSON.parse(images); return arr[0] || '' } catch { return images }
  }
  if (Array.isArray(images)) return images[0] || ''
  return ''
}

//跳转到推荐商品详情
const goDetail = (productId: number) => {
  router.push(`/detail/${productId}`)
}

//加入购物车
const handleAddToCart = async () => {
  if (!userStore.userInfo.userId) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  if (!currentSku.value) {
    ElMessage.warning('该规格组合暂时缺货')
    return
  }

  const params = {
    userId: userStore.userInfo.userId,
    skuId: currentSku.value.skuId,
    cartQuantity: 1
  }

  try {
    await request.post('/cart/add', params)
    ElMessage.success('成功加入购物车！')
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped lang="scss">
.container {
  width: 1226px;
  margin: 0 auto;
}

.site-header {
  height: 64px;
  background: #fff;
  border-bottom: 1px solid #e0e0e0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;

  .header-content {
    display: flex;
    align-items: center;
    height: 100%;

    .logo {
      color: #ff6700;
      font-size: 24px;
      margin: 0;
      cursor: pointer;
      margin-right: 20px;
    }

    .header-info {
      flex: 1;
      display: flex;
      align-items: center;
      font-size: 12px;
      color: #616161;

      .product-name {
        font-size: 18px;
        color: #333;
        margin-right: 10px;
      }

      .sep {
        margin: 0 10px;
        color: #e0e0e0;
      }

      .link {
        cursor: pointer;

        &:hover {
          color: #ff6700;
        }
      }
    }
  }
}

.main-content {
  display: flex;
  padding-top: 32px;
  padding-bottom: 50px;

  .img-box {
    width: 560px;
    height: 560px;
    border: 1px solid #e0e0e0;
    display: flex;
    align-items: center;
    justify-content: center;

    img {
      max-width: 100%;
      max-height: 100%;
      object-fit: contain;
    }

  }

  .info-box {
    flex: 1;
    margin-left: 30px;

    .title {
      font-size: 24px;
      font-weight: 400;
      color: #212121;
      margin: 0;
    }

    .desc {
      color: #b0b0b0;
      margin-top: 8px;
      line-height: 1.5;
      font-size: 14px;

      .highlight {
        color: #ff6700;
      }
    }

    .price-box {
      margin-top: 15px;
      font-size: 18px;
      color: #ff6700;
      border-bottom: 1px solid #e0e0e0;
      padding-bottom: 15px;


    }

    .line {
      margin: 20px 0;
    }

    /* 动态选项样式 */
    .option-box {
      margin-bottom: 20px;

      .option-title {
        font-size: 14px;
        color: #333;
        margin-bottom: 10px;
      }

      .option-list {
        display: flex;
        gap: 10px;
        flex-wrap: wrap; // 允许换行

        .option-item {
          border: 1px solid #e0e0e0;
          padding: 10px 30px;
          cursor: pointer;
          font-size: 14px;
          color: #757575;
          transition: all 0.2s;

          &:hover {
            border-color: #ff6700;
            color: #ff6700;
          }

          &.active {
            border-color: #ff6700;
            color: #ff6700;
          }
        }
      }
    }

    .total-box {
      background: #f9f9fa;
      padding: 30px;
      margin-bottom: 30px;

      .item {
        color: #616161;
        font-size: 14px;
        display: flex;
        justify-content: space-between;
        margin-bottom: 10px;
      }

      .total-price {
        color: #ff6700;
        font-size: 24px;
        padding-top: 20px;
      }
    }

    .btn-box {
      display: flex;
      gap: 15px;

      .buy-btn {
        width: 200px;
        height: 50px;
        font-size: 16px;
        font-weight: bold;
        color: #fff;
      }

      .like-btn {
        width: 140px;
        height: 50px;
      }
    }
  }
}

.loading-box {
  margin-top: 50px;
}

.site-footer {
  text-align: center;
  padding: 30px 0;
  color: #999;
  background: #fff;
  border-top: 1px solid #eee;
  margin-top: 50px;
}

.detail-content {
  margin-top: 40px;
  background: #fff;
  padding: 40px;
  min-height: 200px;
  border: 1px solid #e0e0e0;
}

.detail-content .section-title {
  font-size: 22px;
  color: #333;
  margin-bottom: 30px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.detail-content .content {
  font-size: 16px;
  color: #666;
  line-height: 1.8;
  white-space: pre-wrap; 
}

.detail-content .content :deep(img) {
  max-width: 100%;
  display: block;
  margin: 10px 0;
}

.recommend-section {
  margin-top: 40px;
  background: #fff;
  padding: 40px;
  border: 1px solid #e0e0e0;

  .section-title {
    font-size: 22px;
    color: #333;
    margin-bottom: 30px;
    padding-bottom: 10px;
    border-bottom: 1px solid #eee;
  }

  .recommend-list {
    display: flex;
    gap: 14px;
    flex-wrap: wrap;

    .recommend-card {
      width: 234px;
      height: 280px;
      background: #fafafa;
      padding: 20px 0;
      text-align: center;
      cursor: pointer;
      transition: all .2s linear;
      border: 1px solid #f0f0f0;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
        border-color: #ff6700;
      }

      .rec-img-wrap {
        width: 140px;
        height: 140px;
        margin: 0 auto 18px;

        img {
          width: 100%;
          height: 100%;
          object-fit: contain;
        }
      }

      .rec-name {
        font-size: 14px;
        color: #333;
        margin: 0 10px 6px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .rec-price {
        color: #ff6700;
        font-size: 14px;
        margin: 0;
      }
    }
  }
}
</style>