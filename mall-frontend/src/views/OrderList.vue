<template>
    <div class="order-list-page">
        <div class="site-header">
            <div class="container header-content">
                <h2 class="logo" @click="$router.push('/')">大米商城</h2>
                <div class="divider">|</div>
                <div class="header-title">我的订单</div>
                <div class="user-area">
                    <span class="link" @click="$router.push('/user')">个人中心</span>
                    <span class="sep">|</span>
                    <span class="link" @click="$router.push('/')">返回首页</span>
                </div>
            </div>
        </div>

        <div class="container main-box">
            <div class="filter-box">
                <el-tabs v-model="activeStatus" @tab-change="handleTabChange">
                    <el-tab-pane label="全部有效订单" name="all"></el-tab-pane>
                    <el-tab-pane label="待付款" name="0"></el-tab-pane>
                    <el-tab-pane label="待发货" name="1"></el-tab-pane>
                    <el-tab-pane label="已发货" name="2"></el-tab-pane>
                    <el-tab-pane label="已完成" name="3"></el-tab-pane>
                </el-tabs>
            </div>

            <div class="order-list" v-if="orderList.length > 0">
                <div class="order-item" v-for="order in orderList" :key="order.orderId">
                    <div class="order-header">
                        <div class="info">
                            <span class="status" :class="getStatusColor(order.orderStatus)">
                                {{ getStatusText(order.orderStatus) }}
                            </span>
                            <span class="sep">|</span>
                            <span class="date">{{ formatTime(order.createTime) }}</span>
                            <span class="sep">|</span>
                            <span class="no">订单号：{{ order.orderNo }}</span>
                        </div>
                        <div class="amount">
                            应付金额：<span class="val">{{ order.totalAmount }}</span>元
                        </div>
                    </div>

                    <div class="order-body">
                        <div class="goods-col">
                            <div class="goods-img-item" v-for="item in order.orderItems" :key="item.itemId">
                                <img :src="item.skuImage || defaultImg" @error="useDefaultImg" :title="item.skuName">
                                <div class="goods-info">
                                    <div class="name">{{ item.skuName }}</div>
                                    <div class="price">{{ item.currentPrice }}元 × {{ item.itemQuantity }}</div>
                                </div>
                            </div>
                        </div>

                        <div class="action-col">
                            <el-button v-if="order.orderStatus === 0" type="primary" size="small" class="btn-pay"
                                @click="$router.push(`/pay?orderNo=${order.orderNo}`)">
                                立即付款
                            </el-button>
                            <el-button v-if="order.orderStatus === 2" type="success" size="small" class="btn-pay"
                                @click="handleConfirm(order.orderNo)">
                                确认收货
                            </el-button>
                            <el-button v-if="order.orderStatus !== 0" size="small" plain
                                @click="$router.push(`/order/detail?orderNo=${order.orderNo}`)">
                                订单详情
                            </el-button>
                        </div>
                    </div>
                </div>
            </div>

            <div class="empty-box" v-else>
                <el-empty :description="emptyText" />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import request from '../utils/request'
import { useUserStore } from '../stores/user'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const orderList = ref<any[]>([])
const activeStatus = ref('all') // 默认选全部
const defaultImg = '/imgs/default.webp'

const emptyText = computed(() => {
    return activeStatus.value === 'all' ? '您还没有订单' : '暂无该状态的订单'
})

// 加载订单 (支持传状态)
const loadOrders = async (status?: string) => {
    if (!userStore.userInfo.userId) return

    let url = `/order/list?userId=${userStore.userInfo.userId}`
    // 如果 status 不是 'all'，就拼接到 URL 上
    if (status && status !== 'all') {
        url += `&status=${status}`
    }

    try {
        const res: any = await request.get(url)
        orderList.value = res
    } catch (e) {
        console.error(e)
    }
}

// 切换Tab时触发
const handleTabChange = (name: string) => {
    loadOrders(name)
    router.replace({ query: { ...route.query, status: name === 'all' ? undefined : name } })
}

const getStatusText = (status: number) => {
    const map: any = { 0: '待付款', 1: '待发货', 2: '已发货', 3: '已完成' }
    return map[status] || '未知'
}

const getStatusColor = (status: number) => {
    if (status === 0) return 'text-danger'
    if (status === 1) return 'text-success'
    return 'text-info'
}

const formatTime = (timeStr: string) => {
    if (!timeStr) return ''
    return timeStr.replace('T', ' ').substring(0, 16)
}

const useDefaultImg = (e: Event) => {
    (e.target as HTMLImageElement).src = defaultImg
}

//确认收货
const handleConfirm = (orderNo: string) => {
    ElMessageBox.confirm('确认已收到商品吗？', '确认收货', {
        confirmButtonText: '确认收货',
        cancelButtonText: '取消',
        type: 'info'
    }).then(async () => {
        await request.post(`/order/confirm/${orderNo}`)
        ElMessage.success('已确认收货')
        loadOrders(activeStatus.value === 'all' ? undefined : activeStatus.value)
    })
}

onMounted(() => {
    //页面加载时，检查URL里有没有status参数
    const queryStatus = route.query.status as string
    if (queryStatus) {
        activeStatus.value = queryStatus
        loadOrders(queryStatus)
    } else {
        loadOrders() // 查全部
    }
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
        margin: 0;
    }

    .divider {
        font-size: 24px;
        color: #ccc;
        margin: 0 15px;
        font-weight: 300;
        margin-top: 2px;
    }

    .header-title {
        font-size: 22px;
        color: #333;
        font-weight: normal;
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

        .sep {
            margin: 0 10px;
            color: #e0e0e0;
        }
    }
}

.main-box {
    padding: 30px 0;

    .filter-box {
        margin-bottom: 20px;
        background: #fff;
        padding: 0 20px;
        border-radius: 4px;
    }

    .order-item {
        border: 1px solid #e0e0e0;
        margin-bottom: 20px;
        background: #fff;

        .order-header {
            height: 50px;
            background: #fafaFA;
            padding: 0 20px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            border-bottom: 1px solid #e0e0e0;
            font-size: 14px;
            color: #757575;

            .info {
                .sep {
                    color: #e0e0e0;
                    margin: 0 10px;
                }

                .status {
                    font-size: 16px;
                    font-weight: bold;
                }

                .text-danger {
                    color: #ff6700;
                }

                .text-success {
                    color: #83c44e;
                }
            }

            .amount .val {
                font-size: 20px;
                color: #333;
                font-weight: bold;
                margin: 0 5px;
            }
        }

        .order-body {
            padding: 20px;
            display: flex;

            .goods-col {
                flex: 1;

                .goods-img-item {
                    display: flex;
                    align-items: center;
                    margin-bottom: 15px;

                    &:last-child {
                        margin-bottom: 0;
                    }

                    img {
                        width: 60px;
                        height: 60px;
                        border: 1px solid #eee;
                        margin-right: 15px;
                        object-fit: cover;
                    }

                    .goods-info {
                        .name {
                            font-size: 14px;
                            color: #333;
                            width: 400px;
                        }

                        .price {
                            font-size: 12px;
                            color: #999;
                            margin-top: 5px;
                        }
                    }
                }
            }

            .action-col {
                width: 150px;
                display: flex;
                flex-direction: column;
                justify-content: center;
                align-items: flex-end;

                .btn-pay {
                    width: 100px;
                    background: #ff6700;
                    border-color: #ff6700;
                }
            }
        }
    }
}

.empty-box {
    text-align: center;
    padding: 50px 0;
}
</style>