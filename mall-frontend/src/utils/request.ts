import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
    // baseURL: '/api', 
    // timeout: 5000
    baseURL: import.meta.env.VITE_API_BASE_URL, 
    timeout: 5000
})

//请求拦截器
request.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) {
        config.headers['token'] = token 
    }
    return config
}, error => {
    return Promise.reject(error)
})

//响应拦截器
request.interceptors.response.use(
    response => {
        const res = response.data

        if (res.code === 200) {
            return res.data 
        } else {
            //code不是200说明后端报错
            console.error('Error:', res.msg)
            
            ElMessage.error(res.msg || '操作失败')
            
            return Promise.reject(new Error(res.msg || 'Error'))
        }
    },
    error => {
        console.error('Request Error:', error)
        ElMessage.error(error.message || '网络请求失败')
        return Promise.reject(error)
    }
)

export default request