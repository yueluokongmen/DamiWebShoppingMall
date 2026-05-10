<template>
    <div class="checkout-page">
        <div class="site-header">
            <div class="container header-content">
                <h2 class="logo" @click="$router.push('/')">大米商城</h2>
                <div class="divider">|</div>
                <div class="header-title">确认订单</div>
            </div>
        </div>

        <div class="container">
            <div class="checkout-box">
                <div class="section-address">
                    <h3>收货地址</h3>
                    <div class="address-list">
                        <div class="address-item"
                            :class="{ active: selectedAddress && selectedAddress.addressId === item.addressId }"
                            v-for="item in addressList" :key="item.addressId" @click="selectAddress(item)">
                            <div class="name">{{ item.receiverName }}</div>
                            <div class="tel">{{ item.receiverPhone }}</div>
                            <div class="addr">
                                {{ item.province }} {{ item.city }} {{ item.district }}<br>
                                {{ item.detailedAddress }}
                            </div>
                            <span class="action"
                                v-if="selectedAddress && selectedAddress.addressId === item.addressId">已选</span>
                            <span class="action" v-else>选择</span>
                        </div>

                        <div class="address-item add-new" @click="dialogVisible = true">
                            <div class="icon">+</div>
                            <div>添加新地址</div>
                        </div>
                    </div>
                </div>

                <div class="section-goods">
                    <h3>商品清单</h3>
                    <div class="goods-list">
                        <div class="item" v-for="item in goodsList" :key="item.skuId">
                            <div class="img-box">
                                <img :src="item.productImage || defaultImg" @error="useDefaultImg" alt="">
                            </div>
                            <div class="name">
                                <span class="pro-name">{{ item.productName }}</span>
                            </div>
                            <div class="price">{{ item.price }}元 x {{ item.cartQuantity }}</div>
                            <div class="total">{{ (item.price * item.cartQuantity).toFixed(2) }}元</div>
                        </div>
                    </div>
                </div>

                <div class="section-count">
                    <div class="money-box">
                        <div class="row">
                            <label>商品件数：</label>
                            <span class="val">{{ totalCount }}件</span>
                        </div>
                        <div class="row">
                            <label>商品总价：</label>
                            <span class="val">{{ totalPrice }}元</span>
                        </div>
                        <div class="row">
                            <label>运费：</label>
                            <span class="val">0元</span>
                        </div>
                        <div class="row total-row">
                            <label>应付总额：</label>
                            <span class="total-price">{{ totalPrice }}</span>
                            <span class="unit">元</span>
                        </div>
                    </div>

                    <div class="btn-box">
                        <el-button class="btn-back" @click="$router.push('/cart')">返回购物车</el-button>
                        <el-button type="primary" class="btn-submit" @click="handleSubmit">立即下单</el-button>
                    </div>
                </div>
            </div>
        </div>

        <el-dialog v-model="dialogVisible" title="添加收货地址" width="500px">
            <el-form :model="addressForm" label-width="100px">
                <el-form-item label="姓名">
                    <el-input v-model="addressForm.receiverName" placeholder="收货人姓名"></el-input>
                </el-form-item>
                <el-form-item label="电话">
                    <el-input v-model="addressForm.receiverPhone" placeholder="11位手机号"></el-input>
                </el-form-item>
                <el-form-item label="省份">
                    <el-input v-model="addressForm.province" placeholder="如：广东省"></el-input>
                </el-form-item>
                <el-form-item label="城市">
                    <el-input v-model="addressForm.city" placeholder="如：深圳市"></el-input>
                </el-form-item>
                <el-form-item label="区/县">
                    <el-input v-model="addressForm.district" placeholder="如：南山区"></el-input>
                </el-form-item>
                <el-form-item label="详细地址">
                    <el-input type="textarea" v-model="addressForm.detailedAddress" placeholder="街道、楼牌号等"></el-input>
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="dialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="saveAddress">保存</el-button>
                </span>
            </template>
        </el-dialog>

    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive } from 'vue'
import request from '../utils/request'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const router = useRouter()
const goodsList = ref<any[]>([])

// 地址相关状态
const addressList = ref<any[]>([])
const selectedAddress = ref<any>(null)
const dialogVisible = ref(false) // 控制弹窗显示

// 表单数据
const addressForm = reactive({
    receiverName: '',
    receiverPhone: '',
    province: '',
    city: '',
    district: '',
    detailedAddress: ''
})

const defaultImg = '/imgs/default.webp'

// 选中地址
const selectAddress = (item: any) => {
    selectedAddress.value = item
}

// 保存新地址
const saveAddress = async () => {
    // 简单校验
    if (!addressForm.receiverName || !addressForm.receiverPhone || !addressForm.detailedAddress) {
        ElMessage.warning('请填写完整的地址信息')
        return
    }

    try {
        await request.post('/address/add', {
            userId: userStore.userInfo.userId,
            ...addressForm
        })
        ElMessage.success('添加成功')
        dialogVisible.value = false // 关闭弹窗

        // 清空表单
        addressForm.receiverName = ''
        addressForm.receiverPhone = ''
        addressForm.province = ''
        addressForm.city = ''
        addressForm.district = ''
        addressForm.detailedAddress = ''

        // 重新加载地址列表
        loadAddressList()

    } catch (e) {
        console.error(e)
    }
}

//加载地址的方法
const loadAddressList = async () => {
    try {
        const addrRes = await request.get(`/address/list?userId=${userStore.userInfo.userId}`) as any
        addressList.value = addrRes


        if (!selectedAddress.value && addressList.value.length > 0) {
            selectedAddress.value = addressList.value[0]
        }
    } catch (e) {
        console.error(e)
    }
}

const useDefaultImg = (e: Event) => {
    (e.target as HTMLImageElement).src = defaultImg
}

const loadData = async () => {
    // 1. 查购物车
    const cartRes: any = await request.get(`/cart/list?userId=${userStore.userInfo.userId}`)
    goodsList.value = cartRes.filter((item: any) => item.selected === 1)

    if (goodsList.value.length === 0) {
        ElMessage.warning('没有待结算的商品')
        router.push('/cart')
        return
    }

    // 2. 查地址
    loadAddressList()
}

const totalPrice = computed(() => {
    return goodsList.value.reduce((sum, item) => sum + item.price * item.cartQuantity, 0).toFixed(2)
})

const totalCount = computed(() => {
    return goodsList.value.reduce((sum, item) => sum + item.cartQuantity, 0)
})

const handleSubmit = async () => {
    if (!selectedAddress.value) return ElMessage.error('请选择地址')

    const params = {
        userId: userStore.userInfo.userId,
        addressId: selectedAddress.value.addressId
    }

    try {
        const orderNo = await request.post('/order/submit', params)
        ElMessage.success('下单成功！')
        router.push(`/pay?orderNo=${orderNo}`)
    } catch (e) {
        console.error(e)
    }
}

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

.checkout-box {
    background: #fff;
    margin-top: 30px;
    padding: 48px 0 0;
}

/* 地址区域 */
.section-address {
    margin-bottom: 30px;
    padding: 0 24px;

    h3 {
        margin-bottom: 20px;
        font-size: 18px;
        color: #333;
    }

    .address-list {
        display: flex;
        gap: 20px;
        flex-wrap: wrap;

        .address-item {
            width: 268px;
            height: 178px;
            border: 1px solid #e0e0e0;
            padding: 15px 24px;
            cursor: pointer;
            position: relative;
            transition: all .2s ease;
            box-sizing: border-box;

            .name {
                font-size: 18px;
                color: #333;
                line-height: 30px;
                margin-bottom: 10px;
            }

            .tel {
                color: #757575;
                margin-bottom: 5px;
            }

            .addr {
                color: #757575;
                font-size: 14px;
                line-height: 22px;
            }

            .action {
                position: absolute;
                bottom: 15px;
                right: 24px;
                color: #ff6700;
                display: none;
            }

            &:hover {
                border-color: #b0b0b0;

                .action {
                    display: block;
                }
            }

            &.active {
                border-color: #ff6700;

                .action {
                    display: block;
                }
            }
        }

        .add-new {
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            background: #f9f9f9;
            color: #b0b0b0;

            .icon {
                width: 30px;
                height: 30px;
                background: #e0e0e0;
                border-radius: 50%;
                color: #fff;
                text-align: center;
                line-height: 30px;
                font-size: 20px;
                margin-bottom: 10px;
                transition: background .2s;
            }

            &:hover {
                background: #fff;
                border-color: #b0b0b0;
                color: #757575;

                .icon {
                    background: #b0b0b0;
                }
            }
        }
    }
}

/* 商品清单 */
.section-goods {
    padding: 0 24px;
    border-bottom: 1px solid #e0e0e0;

    h3 {
        margin-bottom: 20px;
        font-size: 18px;
    }

    .goods-list {
        .item {
            display: flex;
            align-items: center;
            margin-bottom: 20px;

            .img-box {
                width: 40px;
                height: 40px;
                margin-right: 15px;
                overflow: hidden;
            }

            img {
                width: 100%;
                height: 100%;
                object-fit: cover;
            }

            .name {
                width: 600px;
                color: #424242;
                font-size: 14px;
            }

            .price {
                flex: 1;
                text-align: center;
                color: #424242;
            }

            .total {
                width: 150px;
                text-align: right;
                color: #ff6700;
            }
        }
    }
}

/* 底部结算 */
.section-count {
    padding: 20px 48px;
    border-top: 2px solid #f5f5f5;

    .money-box {
        text-align: right;
        margin-bottom: 20px;

        .row {
            margin-bottom: 10px;
            font-size: 14px;
            color: #757575;

            .val {
                display: inline-block;
                width: 120px;
                color: #ff6700;
            }
        }

        .total-row {
            margin-top: 20px;

            .total-price {
                font-size: 30px;
                color: #ff6700;
            }

            .unit {
                color: #ff6700;
                margin-left: 5px;
            }
        }
    }

    .btn-box {
        text-align: right;
        border-top: 1px solid #e0e0e0;
        padding-top: 20px;

        .btn-back {
            width: 160px;
            height: 40px;
            border-color: #b0b0b0;
            color: #757575;
            margin-right: 20px;
        }

        .btn-submit {
            width: 160px;
            height: 40px;
            background: #ff6700;
            border-color: #ff6700;
            font-size: 14px;
        }
    }
}
</style>