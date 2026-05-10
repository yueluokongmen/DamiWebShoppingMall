<template>
    <div class="cart-page">
        <div class="site-header">
            <div class="container header-content">
                <h2 class="logo" @click="$router.push('/')">大米商城</h2>
                <div class="header-title">
                    <h2>我的购物车</h2>
                    <p>温馨提示：产品是否购买成功，以最终下单为准哦，请尽快结算</p>
                </div>
            </div>
        </div>

        <div class="container cart-container" v-if="cartList.length > 0">
            <div class="cart-header">
                <div class="col col-check">
                    <el-checkbox v-model="allChecked" @change="handleCheckAll">全选</el-checkbox>
                </div>
                <div class="col col-img">&nbsp;</div>
                <div class="col col-name">商品名称</div>
                <div class="col col-price">单价</div>
                <div class="col col-num">数量</div>
                <div class="col col-total">小计</div>
                <div class="col col-action">操作</div>
            </div>

            <div class="cart-item-list">
                <div class="cart-item" v-for="(item, index) in cartList" :key="item.cartId">
                    <div class="col col-check">
                        <el-checkbox v-model="item.selectedBoolean" @change="calcTotal" />
                    </div>
                    <div class="col col-img">
                        <img :src="parseImg(item)" alt="" />
                    </div>
                    <div class="col col-name">{{ item.productName }}</div>
                    <div class="col col-price">{{ item.price }}元</div>
                    <div class="col col-num">
                        <el-input-number v-model="item.cartQuantity" :min="1" size="small"
                            @change="(val: number) => handleUpdate(item, val)" />
                    </div>
                    <div class="col col-total" style="color:#ff6700">
                        {{ (item.price * item.cartQuantity).toFixed(2) }}元
                    </div>
                    <div class="col col-action">
                        <el-icon class="del-icon" @click="handleDelete(item.cartId)">
                            <Delete />
                        </el-icon>
                    </div>
                </div>
            </div>

            <div class="cart-bar">
                <div class="section-left">
                    <el-button type="text" @click="$router.push('/')">继续购物</el-button>
                    <span class="total-count">共 <i>{{ cartList.length }}</i> 件商品，已选择 <i>{{ checkedCount }}</i> 件</span>
                </div>
                <span class="total-price">
                    合计：<em>{{ totalPrice }}</em>元
                </span>
                <el-button class="btn-primary" size="large" :disabled="checkedCount === 0"
                    @click="$router.push('/checkout')">
                    去结算
                </el-button>
            </div>
        </div>

        <div class="container empty-cart" v-else>
            <h2>购物车还是空的</h2>
            <el-button type="primary" @click="$router.push('/')">马上去购物</el-button>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import request from '../utils/request'
import { useUserStore } from '../stores/user'
import { Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus' 
const userStore = useUserStore()
const cartList = ref<any[]>([])
const allChecked = ref(true)

//获取购物车列表
const fetchCart = async () => {
    if (!userStore.userInfo.userId) return

    const res: any = await request.get(`/cart/list?userId=${userStore.userInfo.userId}`)

    cartList.value = res.map((item: any) => ({
        ...item,
        selectedBoolean: item.selected === 1
    }))
    calcTotal()
}

//处理图片显示
const parseImg = (item: any) => {
    if (item.productImage) {
        return item.productImage
    }
    //显示默认图
    return '/imgs/default.webp'
}
//计算总价和选中数量
const totalPrice = ref(0)
const checkedCount = ref(0)

const calcTotal = () => {
    let total = 0
    let count = 0
    let isAll = true

    cartList.value.forEach(item => {
        if (item.selectedBoolean) {
            total += item.price * item.cartQuantity
            count += item.cartQuantity
        } else {
            isAll = false
        }
    })

    totalPrice.value = total
    checkedCount.value = count
    allChecked.value = cartList.value.length > 0 && isAll
}

const handleUpdate = async (item: any, val: number) => {
    //先计算现在的总价
    calcTotal()

    //发送请求给后端同步数据库
    try {
        await request.post('/cart/update', {
            cartId: item.cartId,
            cartQuantity: val
        })
    } catch (e) {
        console.error(e)
    }
}

const handleCheckAll = (val: boolean) => {
    cartList.value.forEach(item => {
        item.selectedBoolean = val
    })
    calcTotal()
}

const handleDelete = (cartId: number) => {
    //防止误删
    ElMessageBox.confirm('确定要删除这个商品吗?', '提示', {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
    }).then(async () => {
        try {
            //调用后端接口删除
            await request.delete(`/cart/delete/${cartId}`)
            
            ElMessage.success('删除成功')
            
            //重新加载列表
            fetchCart()
        } catch (e) {
            console.error(e)
        }
    }).catch(() => {})
}
onMounted(() => {
    fetchCart()
})
</script>

<style scoped lang="scss">
.container {
    width: 1226px;
    margin: 0 auto;
}

.site-header {
    height: 100px;
    background: #fff;
    border-bottom: 2px solid #ff6700;
    display: flex;
    align-items: center;

    .header-content {
        display: flex;
        align-items: center;
    }

    .logo {
        color: #ff6700;
        font-size: 28px;
        cursor: pointer;
        margin-right: 40px;
    }

    .header-title {
        h2 {
            display: inline-block;
            font-size: 28px;
            font-weight: normal;
            color: #424242;
            margin: 0;
        }

        p {
            display: inline-block;
            font-size: 12px;
            color: #757575;
            margin-left: 15px;
            margin-top: 10px;
        }
    }
}

.cart-container {
    padding: 30px 0;
}

.cart-header {
    height: 70px;
    line-height: 70px;
    background: #fff;
    display: flex;
    color: #424242;

    .col {
        padding: 0 10px;
    }
}

.cart-item {
    height: 100px;
    display: flex;
    align-items: center;
    background: #fff;
    border-top: 1px solid #e0e0e0;
    padding: 15px 0;
    box-sizing: content-box;

    .col-img img {
        width: 80px;
        height: 80px;
    }

    .del-icon {
        cursor: pointer;
        font-size: 18px;

        &:hover {
            color: #ff6700;
        }
    }
}

.col-check {
    width: 110px;
    padding-left: 20px !important;
}

.col-img {
    width: 120px;
}

.col-name {
    width: 380px;
}

.col-price {
    width: 140px;
}

.col-num {
    width: 150px;
}

.col-total {
    width: 120px;
}

.col-action {
    width: 80px;
}

.cart-bar {
    height: 50px;
    line-height: 50px;
    background: #fff;
    margin-top: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    position: sticky;
    bottom: 0;
    box-shadow: 0 -3px 10px rgba(0, 0, 0, 0.05);

    .section-left {
        padding-left: 20px;
        color: #757575;

        i {
            color: #ff6700;
            font-style: normal;
            margin: 0 3px;
        }
    }

    .total-price {
        color: #ff6700;
        font-size: 14px;

        em {
            font-size: 30px;
            font-style: normal;
        }
    }

    .btn-primary {
        width: 200px;
        height: 50px;
        background: #ff6700;
        color: #fff;
        border: none;
        font-size: 18px;

        &:hover {
            background: #f25807;
        }

        &.is-disabled {
            background: #e0e0e0;
            color: #b0b0b0;
        }
    }
}

.empty-cart {
    padding: 80px 0;
    text-align: center;
    color: #b0b0b0;
}
</style>