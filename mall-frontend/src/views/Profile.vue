<template>
    <div class="profile-page">
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
                        <img :src="userStore.userInfo.avatar || defaultAvatar" alt="avatar">
                        <h3>{{ userStore.userInfo.nickname || userStore.userInfo.username }}</h3>
                    </div>
                    <ul class="menu-list">
                        <li class="active">我的个人中心</li>
                        <li @click="$router.push('/order/list')">我的订单</li>
                        <li @click="$router.push('/user/address')">收货地址</li>
                        <li @click="$router.push('/user/security')">账户安全</li>
                    </ul>
                </div>

                <div class="main-content">
                    <div class="user-card">
                        <div class="avatar-wrap">
                            <img :src="userStore.userInfo.avatar || defaultAvatar" alt="avatar">
                        </div>
                        <div class="info">
                            <div class="welcome">Hello, {{ userStore.userInfo.nickname || userStore.userInfo.username }}
                            </div>
                            <div class="tips">今天也是充满活力的一天！</div>
                            <div class="link" @click="handleEditProfile">修改个人资料 ></div>
                        </div>
                    </div>

                    <div class="portal-box">
                        <div class="box-hd">
                            <h3>我的订单</h3>
                            <span class="more" @click="$router.push('/order/list')">查看全部订单 ></span>
                        </div>
                        <div class="box-bd">
                            <div class="status-item"
                                @click="$router.push({ path: '/order/list', query: { status: '0' } })">
                                <el-icon class="icon" style="background:#ff9f43">
                                    <Wallet />
                                </el-icon>
                                <span>待付款</span>
                                <span class="badge" v-if="stats.unpaid > 0">{{ stats.unpaid }}</span>
                            </div>

                            <div class="status-item"
                                @click="$router.push({ path: '/order/list', query: { status: '1' } })">
                                <el-icon class="icon" style="background:#ff6b6b">
                                    <Van />
                                </el-icon>
                                <span>待发货</span>
                                <span class="badge" v-if="stats.unshipped > 0">{{ stats.unshipped }}</span>
                            </div>

                            <div class="status-item"
                                @click="$router.push({ path: '/order/list', query: { status: '2' } })">
                                <el-icon class="icon" style="background:#1dd1a1">
                                    <Box />
                                </el-icon>
                                <span>已发货</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <el-dialog v-model="editDialogVisible" title="修改个人资料" width="500px">
            <el-form label-width="80px" class="edit-form">
                <el-form-item label="头像">
                    <el-upload class="avatar-uploader" :action="uploadUrl" :show-file-list="false"
                        :on-success="handleAvatarSuccess" :before-upload="beforeAvatarUpload">
                        <img v-if="editForm.avatar" :src="editForm.avatar" class="avatar" />
                        <el-icon v-else class="avatar-uploader-icon">
                            <Plus />
                        </el-icon>
                    </el-upload>
                    <div class="tips-text">支持 JPG/PNG，大小不超过 2MB</div>
                </el-form-item>

                <el-form-item label="昵称">
                    <el-input v-model="editForm.nickname" placeholder="请输入新昵称"></el-input>
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="editDialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="saveProfile" :loading="saving">保存</el-button>
                </span>
            </template>
        </el-dialog>

    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import request from '../utils/request'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { Wallet, Van, Box, Plus } from '@element-plus/icons-vue'

const userStore = useUserStore()
const stats = ref({ unpaid: 0, unshipped: 0 })
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const baseURL = import.meta.env.VITE_API_BASE_URL
const uploadUrl = `${baseURL}/file/upload`

// 弹窗控制
const editDialogVisible = ref(false)
const saving = ref(false)
const editForm = reactive({
    nickname: '',
    avatar: ''
})

// 打开弹窗：回显数据
const handleEditProfile = () => {
    editForm.nickname = userStore.userInfo.nickname || userStore.userInfo.username
    editForm.avatar = userStore.userInfo.avatar || ''
    editDialogVisible.value = true
}

// 图片上传成功回调
const handleAvatarSuccess = (res: any) => {
    // res 是后端返回的 Result 对象
    if (res.code === '200') {
        editForm.avatar = res.data // 获取返回的图片URL
        ElMessage.success('图片上传成功')
    } else {
        ElMessage.error(res.msg || '上传失败')
    }
}

// 上传前校验格式和大小
const beforeAvatarUpload = (rawFile: any) => {
    const isImg = rawFile.type === 'image/jpeg' || rawFile.type === 'image/png'
    const isLt2M = rawFile.size / 1024 / 1024 < 2

    if (!isImg) {
        ElMessage.error('头像必须是 JPG 或 PNG 格式!')
        return false
    }
    if (!isLt2M) {
        ElMessage.error('头像大小不能超过 2MB!')
        return false
    }
    return true
}

// 保存修改到后端
const saveProfile = async () => {
    saving.value = true
    try {
        const res: any = await request.post('/user/update', {
            userId: userStore.userInfo.userId,
            nickname: editForm.nickname,
            avatar: editForm.avatar
        })

        // 更新本地 Store
        userStore.userInfo = res
        ElMessage.success('修改成功')
        editDialogVisible.value = false
    } catch (e) {
        console.error(e)
    } finally {
        saving.value = false
    }
}

// 加载订单统计
const loadStats = async () => {
    if (!userStore.userInfo.userId) return
    try {
        const res: any = await request.get(`/order/list?userId=${userStore.userInfo.userId}`)
        stats.value.unpaid = res.filter((o: any) => o.orderStatus === 0).length
        stats.value.unshipped = res.filter((o: any) => o.orderStatus === 1).length
    } catch (e) { }
}

onMounted(() => {
    loadStats()
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
                object-fit: cover;
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

        .user-card {
            background: #fff;
            padding: 40px;
            display: flex;
            align-items: center;
            margin-bottom: 14px;

            .avatar-wrap {
                width: 80px;
                height: 80px;
                border-radius: 50%;
                overflow: hidden;
                margin-right: 20px;
                border: 2px solid #f5f5f5;

                img {
                    width: 100%;
                    height: 100%;
                    object-fit: cover;
                }
            }

            .info {
                .welcome {
                    font-size: 24px;
                    color: #333;
                    margin-bottom: 10px;
                }

                .tips {
                    color: #b0b0b0;
                    margin-bottom: 10px;
                }

                .link {
                    color: #ff6700;
                    cursor: pointer;
                    font-size: 12px;
                }
            }
        }

        .portal-box {
            background: #fff;
            padding: 30px;
            margin-bottom: 14px;

            .box-hd {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 30px;

                h3 {
                    font-size: 18px;
                    font-weight: normal;
                    margin: 0;
                    color: #333;
                }

                .more {
                    color: #757575;
                    font-size: 14px;
                    cursor: pointer;

                    &:hover {
                        color: #ff6700;
                    }
                }
            }

            .box-bd {
                display: flex;
                gap: 80px;
                padding-left: 40px;

                .status-item {
                    text-align: center;
                    cursor: pointer;
                    position: relative;

                    .icon {
                        display: block;
                        width: 50px;
                        height: 50px;
                        border-radius: 50%;
                        color: #fff;
                        font-size: 24px;
                        margin: 0 auto 10px;
                        line-height: 50px;
                    }

                    span {
                        color: #757575;
                    }

                    .badge {
                        position: absolute;
                        top: -5px;
                        right: -5px;
                        background: #ff6700;
                        color: #fff;
                        font-size: 12px;
                        padding: 0 6px;
                        border-radius: 10px;
                        border: 2px solid #fff;
                    }
                }
            }
        }
    }
}

/* 上传组件样式 */
.avatar-uploader .avatar {
    width: 100px;
    height: 100px;
    display: block;
    object-fit: cover;
    border-radius: 6px;
}

.avatar-uploader :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
}

.avatar-uploader :deep(.el-upload:hover) {
    border-color: #ff6700;
}

.avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 100px;
    height: 100px;
    text-align: center;
    line-height: 100px;
}

.tips-text {
    font-size: 12px;
    color: #999;
    margin-top: 8px;
    line-height: 1.2;
}
</style>