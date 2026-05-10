<template>
    <div class="order-detail-page">
        <div class="site-header">
            <div class="container header-content">
                <h2 class="logo" @click="$router.push('/')">大米商城</h2>
                <div class="divider">|</div>
                <div class="header-title">订单详情</div>
                <div class="user-area">
                    <span class="link" @click="$router.push('/user')">个人中心</span>
                    <span class="sep">|</span>
                    <span class="link" @click="$router.push('/')">返回首页</span>
                </div>
            </div>
        </div>

        <div class="container">
            <div class="detail-header">
                <div class="status-text">
                    <h1>{{ getStatusText(order.orderStatus) }}</h1>
                    <div class="actions" v-if="order.orderStatus === 0">
                        <el-button type="primary"
                            @click="$router.push(`/pay?orderNo=${order.orderNo}`)">立即付款</el-button>
                    </div>
                </div>
                <div class="steps-box">
                    <el-steps :active="activeStep" finish-status="success" align-center>
                        <el-step title="下单" :description="formatTime(order.createTime)"></el-step>
                        <el-step title="付款" :description="formatTime(order.payTime)"></el-step>
                        <el-step title="发货" description="等待发货"></el-step>
                        <el-step title="交易完成"></el-step>
                    </el-steps>
                </div>
            </div>

            <div class="detail-body">
                <div class="info-section">
                    <h3>收货信息</h3>
                    <div class="info-content" v-if="order.receiverInfoSnapshot">
                        <div class="row">
                            <span class="label">收货人：</span>
                            <span>{{ order.receiverInfoSnapshot.receiverName }}</span>
                        </div>
                        <div class="row">
                            <span class="label">联系电话：</span>
                            <span>{{ order.receiverInfoSnapshot.receiverPhone }}</span>
                        </div>
                        <div class="row">
                            <span class="label">收货地址：</span>
                            <span>
                                {{ order.receiverInfoSnapshot.province }}
                                {{ order.receiverInfoSnapshot.city }}
                                {{ order.receiverInfoSnapshot.district }} <br>
                                {{ order.receiverInfoSnapshot.detailedAddress }}
                            </span>
                        </div>
                    </div>
                </div>

                <div class="info-section">
                    <h3>订单信息</h3>
                    <div class="info-content">
                        <div class="row"><span class="label">订单编号：</span>{{ order.orderNo }}</div>
                        <div class="row"><span class="label">下单时间：</span>{{ formatTime(order.createTime) }}</div>
                        <div class="row" v-if="order.payTime"><span class="label">支付时间：</span>{{
                            formatTime(order.payTime) }}</div>
                    </div>
                </div>

                <div class="goods-section">
                    <h3>商品清单</h3>
                    <div class="goods-table">
                        <div class="goods-header">
                            <div class="col-main">商品名称</div>
                            <div class="col-price">单价</div>
                            <div class="col-num">数量</div>
                            <div class="col-total">小计</div>
                        </div>
                        <div class="goods-item" v-for="item in order.orderItems" :key="item.itemId">
                            <div class="col-main">
                                <img :src="item.skuImage || defaultImg" @error="useDefaultImg">
                                <span class="name">{{ item.skuName }}</span>
                            </div>
                            <div class="col-price">{{ item.currentPrice }}元</div>
                            <div class="col-num">{{ item.itemQuantity }}</div>
                            <div class="col-total">{{ item.itemTotalPrice }}元</div>
                        </div>
                    </div>
                </div>

                <div class="total-section">
                    <div class="row">
                        <label>商品总价：</label>
                        <span class="val">{{ order.totalAmount }}元</span>
                    </div>
                    <div class="row">
                        <label>运费：</label>
                        <span class="val">0元</span>
                    </div>
                    <div class="row total">
                        <label>应付金额：</label>
                        <span class="val price">{{ order.totalAmount }}</span>
                        <span class="unit">元</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import request from '../utils/request'

const route = useRoute()
const order = ref<any>({})
const defaultImg = '/imgs/default.webp'

const activeStep = computed(() => {
    const status = order.value.orderStatus
    if (status === 0) return 1 // 待付款 
    if (status === 1) return 2 // 待发货 
    if (status === 2) return 3 // 已发货
    if (status === 3) return 4 // 完成
    return 1
})

const loadOrder = async () => {
    const orderNo = route.query.orderNo
    if (!orderNo) return

    try {
        const res = await request.get(`/order/detail?orderNo=${orderNo}`)
        order.value = res

    } catch (e) {
        console.error(e)
    }
}

const getStatusText = (status: number) => {
    const map: any = { 0: '等待付款', 1: '等待发货', 2: '已发货', 3: '交易完成' }
    return map[status] || '未知状态'
}

const formatTime = (timeStr: string) => {
    if (!timeStr) return ''
    return timeStr.replace('T', ' ').substring(0, 19)
}

const useDefaultImg = (e: Event) => {
    (e.target as HTMLImageElement).src = defaultImg
}

onMounted(() => {
    loadOrder()
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
    margin-bottom: 30px;

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

.detail-header {
    background: #fff;
    padding: 40px 50px;
    margin-bottom: 20px;
    border: 1px solid #e0e0e0;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .status-text {
        h1 {
            margin: 0 0 10px;
            font-weight: normal;
            font-size: 28px;
            color: #333;
        }
    }

    .steps-box {
        width: 600px;
    }
}

.detail-body {
    background: #fff;
    padding: 30px 50px;
    border: 1px solid #e0e0e0;
    margin-bottom: 50px;

    .info-section {
        border-bottom: 1px solid #e0e0e0;
        padding-bottom: 20px;
        margin-bottom: 20px;

        h3 {
            font-size: 18px;
            color: #333;
            margin-bottom: 20px;
            font-weight: normal;
        }

        .info-content {
            .row {
                margin-bottom: 10px;
                font-size: 14px;
                color: #333;
                display: flex;

                .label {
                    width: 100px;
                    color: #757575;
                }
            }
        }
    }

    .goods-section {
        border-bottom: 1px solid #e0e0e0;
        padding-bottom: 20px;
        margin-bottom: 20px;

        h3 {
            font-size: 18px;
            color: #333;
            margin-bottom: 20px;
            font-weight: normal;
        }

        .goods-table {
            .goods-header {
                display: flex;
                padding: 10px 0;
                border-bottom: 1px solid #eee;
                color: #757575;
                font-size: 14px;
            }

            .goods-item {
                display: flex;
                align-items: center;
                padding: 15px 0;
                border-bottom: 1px solid #eee;
                color: #333;
            }

            .col-main {
                flex: 1;
                display: flex;
                align-items: center;

                img {
                    width: 60px;
                    height: 60px;
                    margin-right: 15px;
                    border: 1px solid #eee;
                }
            }

            .col-price {
                width: 150px;
                text-align: center;
            }

            .col-num {
                width: 100px;
                text-align: center;
            }

            .col-total {
                width: 150px;
                text-align: right;
                color: #ff6700;
            }
        }
    }

    .total-section {
        text-align: right;

        .row {
            margin-bottom: 10px;
            font-size: 14px;
            color: #757575;

            label {
                display: inline-block;
                width: 100px;
                text-align: right;
            }

            .val {
                display: inline-block;
                width: 150px;
                color: #ff6700;
                text-align: right;
            }

            &.total {
                margin-top: 20px;

                .price {
                    font-size: 30px;
                }
            }
        }
    }
}
</style>