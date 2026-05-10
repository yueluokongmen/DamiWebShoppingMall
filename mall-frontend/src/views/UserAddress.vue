<template>
    <div class="user-address-page">
        <div class="site-header">
            <div class="container header-content">
                <h2 class="logo" @click="$router.push('/')">大米商城</h2>
                <div class="header-title">个人中心</div>
                <div class="user-area">
                    <span class="link" @click="$router.push('/')">返回首页</span>
                </div>
            </div>
        </div>

        <div class="container">
            <div class="profile-layout">
                <div class="side-menu">
                    <div class="avatar-box">
                        <img src="https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&h=100&fit=crop"
                            alt="avatar">
                        <h3>{{ userStore.userInfo.username || '大米用户' }}</h3>
                    </div>
                    <ul class="menu-list">
                        <li @click="$router.push('/user')">我的个人中心</li>
                        <li @click="$router.push('/order/list')">我的订单</li>
                        <li class="active">收货地址</li>
                        <li>账户安全</li>
                    </ul>
                </div>

                <div class="main-content">
                    <div class="content-header">
                        <h3>收货地址</h3>
                    </div>

                    <div class="address-box">
                        <div class="address-list">
                            <div class="address-item" v-for="item in addressList" :key="item.addressId">
                                <div class="name">
                                    {{ item.receiverName }}
                                    <span class="tag" v-if="item.isDefault === 1">默认</span>
                                </div>
                                <div class="tel">{{ item.receiverPhone }}</div>
                                <div class="addr">
                                    {{ item.province }} {{ item.city }} {{ item.district }}<br>
                                    {{ item.detailedAddress }}
                                </div>

                                <div class="actions">
                                    <span class="btn-text" @click="openEdit(item)">修改</span>
                                    <span class="btn-text" @click="handleDelete(item.addressId)">删除</span>
                                    <span class="btn-text set-default" v-if="item.isDefault === 0"
                                        @click="setAsDefault(item)">设为默认</span>
                                </div>
                            </div>

                            <div class="address-item add-new" @click="openAdd">
                                <div class="icon">+</div>
                                <div>添加新地址</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <el-dialog v-model="dialogVisible" :title="isEdit ? '修改收货地址' : '添加收货地址'" width="500px">
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
import { ref, onMounted, reactive } from 'vue'
import request from '../utils/request'
import { useUserStore } from '../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const addressList = ref<any[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false) // 区分是新增还是修改

const addressForm = reactive<any>({
    addressId: null,
    receiverName: '',
    receiverPhone: '',
    province: '',
    city: '',
    district: '',
    detailedAddress: '',
    isDefault: 0
})

// 加载地址列表
const loadAddressList = async () => {
    if (!userStore.userInfo.userId) return
    try {
        const res = await request.get(`/address/list?userId=${userStore.userInfo.userId}`) as any
        addressList.value = res
    } catch (e) { console.error(e) }
}

// 打开新增
const openAdd = () => {
    isEdit.value = false
    // 清空表单
    Object.assign(addressForm, {
        addressId: null, receiverName: '', receiverPhone: '', province: '', city: '', district: '', detailedAddress: ''
    })
    dialogVisible.value = true
}

// 打开编辑
const openEdit = (item: any) => {
    isEdit.value = true
    // 填充表单
    Object.assign(addressForm, item)
    dialogVisible.value = true
}

// 保存
const saveAddress = async () => {
    if (!addressForm.receiverName || !addressForm.receiverPhone || !addressForm.detailedAddress) {
        ElMessage.warning('请填写完整信息')
        return
    }

    const params = { userId: userStore.userInfo.userId, ...addressForm }
    try {
        if (isEdit.value) {
            await request.post('/address/update', params)
            ElMessage.success('修改成功')
        } else {
            await request.post('/address/add', params)
            ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        loadAddressList()
    } catch (e) { console.error(e) }
}

// 设为默认
const setAsDefault = async (item: any) => {
    try {
        // 调用update 接口
        await request.post('/address/update', {
            userId: userStore.userInfo.userId,
            addressId: item.addressId,
            isDefault: 1
        })
        ElMessage.success('设置成功')
        loadAddressList()
    } catch (e) { console.error(e) }
}

// 删除地址
const handleDelete = (id: number) => {
    ElMessageBox.confirm('确定要删除这个地址吗?', '提示', {
        type: 'warning'
    }).then(async () => {
        try {
            await request.delete(`/address/delete/${id}`)
            ElMessage.success('删除成功')
            loadAddressList()
        } catch (e) { }
    }).catch(() => { })
}

onMounted(() => {
    loadAddressList()
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

    .header-title {
        font-size: 22px;
        color: #333;
        margin-left: 20px;
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
    }
}

.profile-layout {
    display: flex;
    margin-top: 30px;
    padding-bottom: 50px;

    /* 左侧菜单样式 */
    .side-menu {
        width: 234px;
        background: #fff;
        padding: 30px 0;
        margin-right: 14px;

        .avatar-box {
            text-align: center;
            margin-bottom: 30px;

            img {
                width: 80px;
                height: 80px;
                border-radius: 50%;
                border: 2px solid #f5f5f5;
            }

            h3 {
                font-size: 16px;
                color: #333;
                font-weight: normal;
                margin-top: 10px;
            }
        }

        .menu-list {
            list-style: none;
            padding: 0;
            margin: 0;

            li {
                height: 50px;
                line-height: 50px;
                text-align: center;
                color: #333;
                cursor: pointer;
                transition: all .2s;

                &:hover {
                    color: #ff6700;
                }

                &.active {
                    color: #ff6700;
                    font-weight: bold;
                }
            }
        }
    }

    .main-content {
        flex: 1;
        background: #fff;
        padding: 30px;

        .content-header {
            margin-bottom: 30px;

            h3 {
                font-size: 28px;
                font-weight: normal;
                color: #757575;
                margin: 0;
            }
        }

        .address-list {
            display: flex;
            flex-wrap: wrap;
            gap: 20px;

            /* 地址卡片 */
            .address-item {
                width: 268px;
                height: 178px;
                border: 1px solid #e0e0e0;
                padding: 20px;
                box-sizing: border-box;
                position: relative;
                transition: all .2s;
                cursor: pointer;

                &:hover {
                    border-color: #b0b0b0;

                    .actions {
                        display: block;
                    }
                }

                .name {
                    font-size: 18px;
                    color: #333;
                    margin-bottom: 10px;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                }

                .tag {
                    font-size: 12px;
                    background: #ff6700;
                    color: #fff;
                    padding: 2px 5px;
                }

                .tel {
                    color: #757575;
                    margin-bottom: 5px;
                }

                .addr {
                    color: #757575;
                    font-size: 14px;
                    line-height: 22px;
                    height: 44px;
                    overflow: hidden;
                }

                .actions {
                    position: absolute;
                    bottom: 15px;
                    right: 20px;
                    display: none;

                    .btn-text {
                        color: #ff6700;
                        margin-left: 10px;
                        font-size: 12px;

                        &:hover {
                            text-decoration: underline;
                        }
                    }

                    .set-default {
                        color: #757575;

                        &:hover {
                            color: #ff6700;
                        }
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
                }

                &:hover {
                    border-color: #b0b0b0;
                    background: #fff;

                    .icon {
                        background: #b0b0b0;
                    }
                }
            }
        }
    }
}
</style>