import { createRouter, createWebHistory } from 'vue-router'
import type { RouteLocationNormalized, NavigationGuardNext } from 'vue-router'
import { useUserStore } from '../stores/user'
const routes = [
    {
        path: '/',
        component: () => import('../views/Home.vue')
    },
    {
        path: '/login',
        component: () => import('../views/Login.vue')
    },
    {
        path: '/register',
        component: () => import('../views/Register.vue')
    },
    {
        path: '/detail/:id',
        component: () => import('../views/Detail.vue')
    },
    {
        path: '/cart',
        component: () => import('../views/Cart.vue')
    },
    {
        path: '/checkout',
        component: () => import('../views/Checkout.vue')
    },
    {
        path: '/pay',
        component: () => import('../views/Pay.vue')
    },
    {
        path: '/order/list',
        component: () => import('../views/OrderList.vue')
    },
    {
        path: '/user',
        component: () => import('../views/Profile.vue')
    },
    {
        path: '/user/address',
        component: () => import('../views/UserAddress.vue')
    },
    {
        path: '/search',
        component: () => import('../views/GoodsList.vue')
    },
    {
        path: '/order/detail',
        component: () => import('../views/OrderDetail.vue')
    },
    {
        path: '/user/security',
        name: 'UserSecurity',
        component: () => import('../views/UserSecurity.vue')
    },
    {
        path: '/admin/login',
        component: () => import('../views/admin/AdminLogin.vue')
    },
    {
        path: '/admin',
        component: () => import('../views/admin/AdminLayout.vue'),
        beforeEnter: (to: RouteLocationNormalized, from: RouteLocationNormalized, next: NavigationGuardNext) => {
            const userStore = useUserStore()
            const role = userStore.userInfo.role
            if (role !== 1 && role !== 2) {
                next('/admin/login')
            } else {
                next()
            }
        },
        children: [
            {
                path: 'dashboard',
                component: () => import('../views/admin/Dashboard.vue'),
                meta: { title: '控制台' }
            },
            {
                path: 'products',
                component: () => import('../views/admin/ProductList.vue'),
                meta: { title: '商品列表' }
            },
            {
                path: 'product/add',
                component: () => import('../views/admin/ProductAdd.vue'),
                meta: { title: '发布商品' }
            },
            {
                path: 'orders',
                component: () => import('../views/admin/OrderList.vue'),
                meta: { title: '订单列表' }
            },
            {
                path: 'users',
                component: () => import('../views/admin/UserList.vue'),
                meta: { title: '客户列表' }
            },
            {
                path: 'category',
                component: () => import('../views/admin/CategoryManage.vue'),
                meta: { title: '分类管理' }
            },
            {
                path: 'stats',
                component: () => import('../views/admin/SalesStats.vue'),
                meta: { title: '销售统计' }
            },
            {
                path: 'logs',
                component: () => import('../views/admin/BrowsingLogs.vue'),
                meta: { title: '浏览日志' }
            },
            {
                path: 'sales-manage',
                component: () => import('../views/admin/SalesManage.vue'),
                meta: { title: '销售人员管理' }
            },
            {
                path: 'login-logs',
                component: () => import('../views/admin/LoginLogs.vue'),
                meta: { title: '登录日志' }
            },
            {
                path: 'operation-logs',
                component: () => import('../views/admin/OperationLogs.vue'),
                meta: { title: '操作日志' }
            },
            {
                path: 'analysis',
                component: () => import('../views/admin/DataAnalysis.vue'),
                meta: { title: '数据分析' }
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router
