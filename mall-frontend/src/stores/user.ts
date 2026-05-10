import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '../utils/request'

export const useUserStore = defineStore('user', () => {

    const token = ref(localStorage.getItem('token') || '')
    const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))


    //登录
    const login = async (loginForm: any) => {
        try {
            //发送请求
            const data: any = await request.post('/user/login', loginForm)
            
            //存到状态里
            token.value = data.token
            userInfo.value = data
            
            //存到浏览器缓存
            localStorage.setItem('token', data.token)
            localStorage.setItem('userInfo', JSON.stringify(data))
            
            return true //登录成功
        } catch (error) {
            return false //登录失败
        }
    }

    //退出
    const logout = () => {
        token.value = ''
        userInfo.value = {}
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
    }

    return { token, userInfo, login, logout }
})