<template>
    <div class="goods-list-page">
        <div class="site-header">
            <div class="container header-content">
                <h2 class="logo" @click="$router.push('/')">大米商城</h2>
                <div class="search-bar">
                    <el-input v-model="keyword" placeholder="搜一搜" class="search-input" @keyup.enter="handleSearch">
                        <template #append>
                            <el-button @click="handleSearch"><el-icon>
                                    <Search />
                                </el-icon></el-button>
                        </template>
                    </el-input>
                </div>
                <div class="user-area">
                    <span class="link" @click="$router.push('/')">返回首页</span>
                </div>
            </div>
        </div>

        <div class="breadcrumbs">
            <div class="container">
                <span @click="$router.push('/')">首页</span>
                <span class="sep">></span>
                <span class="act">全部商品</span>
                <span class="sep" v-if="keyword">></span>
                <span class="act" v-if="keyword">"{{ keyword }}"</span>
            </div>
        </div>

        <div class="container main-box">
            <div class="goods-list" v-if="goodsList.length > 0">
                <div class="goods-item" v-for="item in goodsList" :key="item.productId"
                    @click="toDetail(item.productId)">
                    <div class="img-box">
                        <img :src="item.images && item.images[0] ? item.images[0] : ''" alt="">
                    </div>
                    <div class="name">{{ item.productName }}</div>
                    <div class="price">{{ item.spuPrice }} 元</div>
                </div>
            </div>

            <div class="empty-box" v-else>
                <el-empty description="抱歉，没有找到相关商品" />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../utils/request'
import { Search } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const goodsList = ref<any[]>([])
const keyword = ref('')

//加载数据
const loadData = async () => {
    const query = route.query // 获取 URL 上的参数
    const params: any = {}

    if (query.keyword) {
        params.keyword = query.keyword
        keyword.value = query.keyword as string
    }

    if (query.familyId) {
        params.familyId = query.familyId
    }

    try {
        const res: any = await request.get('/spu/search', { params })
        goodsList.value = res
    } catch (e) {
        console.error(e)
    }
}

const handleSearch = () => {
    if (!keyword.value) return
    router.push({ path: '/search', query: { keyword: keyword.value } })
}

const toDetail = (id: number) => {
    router.push(`/detail/${id}`)
}

// 监听路由变化 (例如从搜"手机"变成了搜"电视"，页面不刷新但数据要变)
watch(() => route.query, () => {
    loadData()
})

onMounted(() => {
    loadData()
})
</script>

<style scoped lang="scss">
.container {
    width: 1226px;
    margin: 0 auto;
}

.site-header {
    border-bottom: 2px solid #ff6700;
    background: #fff;
    height: 100px;
    display: flex;
    align-items: center;

    .header-content {
        display: flex;
        align-items: center;
        height: 100%;
        width: 100%;
    }

    .logo {
        color: #ff6700;
        font-size: 28px;
        cursor: pointer;
        font-weight: bold;
        margin-right: 50px;
    }

    /* 顶部搜索框 */
    .search-bar {
        width: 500px;

        .search-input {
            :deep(.el-input__wrapper) {
                border-radius: 0;
            }

            :deep(.el-button) {
                border-radius: 0;
                background: #fff;

                &:hover {
                    background: #ff6700;
                    color: #fff;
                }
            }
        }
    }

    .user-area {
        margin-left: auto;
        font-size: 14px;

        .link {
            cursor: pointer;

            &:hover {
                color: #ff6700
            }
        }
    }
}

.breadcrumbs {
    height: 40px;
    line-height: 40px;
    background: #f5f5f5;
    font-size: 12px;
    color: #757575;

    .sep {
        margin: 0 5px;
    }

    .act {
        color: #333;
    }

    span {
        cursor: pointer;

        &:hover {
            color: #ff6700
        }
    }
}

.main-box {
    padding: 20px 0 50px;

    .goods-list {
        display: flex;
        flex-wrap: wrap;
        gap: 14px;

        .goods-item {
            width: 296px;
            height: 380px;
            background: #fff;
            padding: 20px;
            box-sizing: border-box;
            text-align: center;
            transition: all .2s;
            cursor: pointer;
            border: 1px solid #e0e0e0;

            &:hover {
                transform: translateY(-2px);
                box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
                border-color: #ff6700;
            }

            .img-box {
                width: 200px;
                height: 200px;
                margin: 0 auto 20px;

                img {
                    width: 100%;
                    height: 100%;
                    object-fit: contain;
                }
            }

            .name {
                font-size: 16px;
                color: #333;
                margin-bottom: 10px;
                text-overflow: ellipsis;
                white-space: nowrap;
                overflow: hidden;
            }

            .price {
                color: #ff6700;
                font-size: 18px;
            }
        }
    }
}
</style>