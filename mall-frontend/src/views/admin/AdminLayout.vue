<template>
    <div class="admin-layout">
        <el-container class="layout-container">
            <el-aside width="220px" class="aside-menu">
                <div class="logo-box">
                    <h2>{{ isAdmin ? '大米后台' : '销售后台' }}</h2>
                </div>
                <el-menu :default-active="activeMenu" class="el-menu-vertical" background-color="#304156"
                    text-color="#bfcbd9" active-text-color="#ff6700" router>
                    <el-menu-item index="/admin/dashboard">
                        <el-icon>
                            <DataBoard />
                        </el-icon>
                        <span>控制台</span>
                    </el-menu-item>

                    <el-sub-menu index="1">
                        <template #title>
                            <el-icon>
                                <Goods />
                            </el-icon>
                            <span>商品管理</span>
                        </template>
                        <el-menu-item index="/admin/products">商品列表</el-menu-item>
                        <el-menu-item index="/admin/product/add">发布商品</el-menu-item>
                        <el-menu-item index="/admin/category">分类/系列管理</el-menu-item>
                    </el-sub-menu>

                    <el-sub-menu index="2">
                        <template #title>
                            <el-icon>
                                <List />
                            </el-icon>
                            <span>订单管理</span>
                        </template>
                        <el-menu-item index="/admin/orders">订单列表</el-menu-item>
                        <el-menu-item index="/admin/stats">销售统计</el-menu-item>
                    </el-sub-menu>

                    <el-sub-menu index="3">
                        <template #title>
                            <el-icon>
                                <User />
                            </el-icon>
                            <span>客户管理</span>
                        </template>
                        <el-menu-item index="/admin/users">客户列表</el-menu-item>
                        <el-menu-item index="/admin/logs">浏览日志</el-menu-item>
                    </el-sub-menu>

                    <!-- 仅管理员可见的菜单 -->
                    <el-sub-menu index="4" v-if="isAdmin">
                        <template #title>
                            <el-icon>
                                <Avatar />
                            </el-icon>
                            <span>人员管理</span>
                        </template>
                        <el-menu-item index="/admin/sales-manage">销售人员</el-menu-item>
                    </el-sub-menu>

                    <el-sub-menu index="5" v-if="isAdmin">
                        <template #title>
                            <el-icon>
                                <Document />
                            </el-icon>
                            <span>日志管理</span>
                        </template>
                        <el-menu-item index="/admin/login-logs">登录日志</el-menu-item>
                        <el-menu-item index="/admin/operation-logs">操作日志</el-menu-item>
                    </el-sub-menu>

                    <el-sub-menu index="6" v-if="isAdmin">
                        <template #title>
                            <el-icon>
                                <TrendCharts />
                            </el-icon>
                            <span>数据分析</span>
                        </template>
                        <el-menu-item index="/admin/analysis">数据总览</el-menu-item>
                    </el-sub-menu>
                </el-menu>
            </el-aside>

            <el-container>
                <el-header class="admin-header">
                    <div class="breadcrumb">
                        管理系统 / {{ currentRouteName }}
                    </div>
                    <div class="header-right">
                        <el-tag :type="isAdmin ? 'danger' : 'warning'" size="small" style="margin-right: 10px;">
                            {{ isAdmin ? '管理员' : '销售人员' }}
                        </el-tag>
                        <span class="username">{{ userStore.userInfo.username }}</span>
                        <el-button type="danger" size="small" link @click="handleLogout">退出</el-button>
                    </div>
                </el-header>

                <el-main class="admin-main">
                    <router-view />
                </el-main>
            </el-container>
        </el-container>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { DataBoard, Goods, List, User, Avatar, Document, TrendCharts } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isAdmin = computed(() => userStore.userInfo.role === 1)
const activeMenu = computed(() => route.path)
const currentRouteName = computed(() => route.meta.title || '首页')

const handleLogout = () => {
    userStore.logout()
    router.push('/admin/login')
}
</script>

<style scoped lang="scss">
.layout-container {
    height: 100vh;
}

.aside-menu {
    background-color: #304156;
    color: #fff;
    display: flex;
    flex-direction: column;

    .logo-box {
        height: 60px;
        line-height: 60px;
        text-align: center;
        background-color: #2b3649;

        h2 {
            margin: 0;
            color: #fff;
            font-size: 20px;
        }
    }

    .el-menu-vertical {
        border-right: none;
        flex: 1;
        overflow-y: auto;
    }
}

.admin-header {
    background: #fff;
    border-bottom: 1px solid #e6e6e6;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 20px;
    height: 60px;

    .breadcrumb {
        color: #97a8be;
        font-size: 14px;
    }

    .header-right {
        font-size: 14px;
        display: flex;
        align-items: center;

        .username {
            margin-right: 15px;
            color: #333;
        }
    }
}

.admin-main {
    background-color: #f0f2f5;
    padding: 20px;
}
</style>
