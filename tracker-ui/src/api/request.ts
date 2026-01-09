import axios from 'axios';
import { ElMessage } from 'element-plus';

const service = axios.create({
  baseURL: '/api', // 统一后端地址
  timeout: 10000
});

// 响应拦截器：自动解包后端定义的 Result 规范
service.interceptors.response.use(
  (response) => {
    const res = response.data;
    // 假设后端 code 为 200 代表成功
    if (res.code !== 200) {
      ElMessage.error(res.message || '系统错误');
      return Promise.reject(new Error(res.message || 'Error'));
    }
    return res.data; // 直接返回真正的业务数据
  },
  (error) => {
    ElMessage.error('网络异常，请检查后端服务');
    return Promise.reject(error);
  }
);

export default service;