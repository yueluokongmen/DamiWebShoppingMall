<template>
    <div class="pay-page">
        <div class="site-header">
            <div class="container header-content">
                <h2 class="logo" @click="$router.push('/')">大米商城</h2>
                <div class="divider">|</div>
                <div class="header-title">支付订单</div>
            </div>
        </div>

        <div class="container">
            <div class="pay-box" v-if="orderInfo.orderNo">
                <div class="section-alert">
                    <el-icon class="icon-success">
                        <CircleCheckFilled />
                    </el-icon>
                    <div class="text-info">
                        <h3>订单提交成功！去付款咯～</h3>
                        <p class="sub-text">请在 30 分钟内完成支付，超时后将取消订单</p>
                        <p class="order-no">订单号：{{ orderInfo.orderNo }}</p>
                    </div>
                    <div class="price-info">
                        应付金额：<span class="total">{{ orderInfo.totalAmount }}</span>元
                    </div>
                </div>

                <div class="section-payment">
                    <h3>选择以下支付方式付款</h3>
                    <div class="payment-options">
                        <div class="pay-item" @click="handlePay('微信')">
                            <div class="qr-placeholder wechat-bg">
                                <div v-if="loading">支付中...</div>
                                <div v-else>
                                    微信扫码<br>
                                    <span class="tip">(点击模拟支付)</span>
                                </div>
                            </div>
                            <p>微信支付</p>
                        </div>
                        <div class="pay-item" @click="handlePay('支付宝')">
                            <div class="qr-placeholder alipay-bg">
                                <div v-if="loading">支付中...</div>
                                <div v-else>
                                    支付宝扫码<br>
                                    <span class="tip">(点击模拟支付)</span>
                                </div>
                            </div>
                            <p>支付宝</p>
                        </div>
                    </div>
                </div>
            </div>

            <div class="container loading-box" v-else>
                <el-skeleton :rows="5" animated />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { CircleCheckFilled } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const orderInfo = ref<any>({})
const loading = ref(false)

const loadOrder = async () => {
    const orderNo = route.query.orderNo
    if (!orderNo) {
        ElMessage.error('订单号不存在')
        router.push('/')
        return
    }
    try {
        const res = await request.get(`/order/detail?orderNo=${orderNo}`)
        orderInfo.value = res
    } catch (e) {
        console.error(e)
    }
}

// 点击二维码直接支付
const handlePay = async (type: string) => {
    if (loading.value) return
    loading.value = true
    try {
        await request.post(`/order/pay?orderNo=${orderInfo.value.orderNo}`)
        ElMessage.success(`${type}支付成功！`)
        // 跳转到订单列表
        router.push('/order/list')
    } catch (e) {
        console.error(e)
    } finally {
        loading.value = false
    }
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

    .header-content {
        display: flex;
        align-items: center;
        height: 100%;
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
}

.pay-box {
    background: #fff;
    margin-top: 30px;
    padding: 48px;

    .section-alert {
        display: flex;
        align-items: flex-start;
        padding-bottom: 40px;
        border-bottom: 1px solid #e0e0e0;

        .icon-success {
            font-size: 80px;
            color: #83c44e;
            margin-right: 25px;
        }

        .text-info {
            flex: 1;
            margin-top: 10px;

            h3 {
                font-size: 24px;
                margin-bottom: 10px;
                font-weight: normal;
                color: #333;
                margin-top: 0;
            }

            .sub-text {
                color: #757575;
                font-size: 14px;
                margin-bottom: 5px;
            }

            .order-no {
                color: #757575;
                font-size: 14px;
            }
        }

        .price-info {
            font-size: 14px;
            margin-top: 10px;
            color: #757575;

            .total {
                color: #ff6700;
                font-size: 24px;
                margin: 0 5px;
                font-weight: bold;
            }
        }
    }

    .section-payment {
        padding: 30px 0;

        h3 {
            font-size: 18px;
            margin-bottom: 20px;
            color: #333;
            margin-top: 0;
        }

        .payment-options {
            display: flex;
            gap: 40px;
            justify-content: center;
            margin-top: 40px;

            .pay-item {
                text-align: center;
                cursor: pointer;
                transition: all .2s;

                &:hover {
                    transform: translateY(-5px);
                }

                /* 悬浮动效 */

                .qr-placeholder {
                    width: 180px;
                    height: 180px;
                    margin: 0 auto 15px;
                    border: 1px solid #e0e0e0;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    color: #999;
                    font-size: 14px;
                    background: #fff;
                    line-height: 1.5;
                    flex-direction: column;

                    .tip {
                        font-size: 12px;
                        color: #ff6700;
                        margin-top: 5px;
                    }

                    &.wechat-bg {
                        border-color: #09bb07;
                        color: #09bb07;
                    }

                    &.alipay-bg {
                        border-color: #00a0e9;
                        color: #00a0e9;
                    }
                }

                p {
                    color: #666;
                    margin: 0;
                    font-size: 16px;
                }
            }
        }
    }
}

.loading-box {
    margin-top: 30px;
    padding: 48px;
    background: #fff;
}
</style>