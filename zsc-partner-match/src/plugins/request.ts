import axios from "axios";
import { showToast } from "vant";

const baseURL = import.meta.env.VITE_API_BASE_URL || "/api";
const myAxios = axios.create({
  //区分开发环境和生产环境的baseURL
  baseURL: baseURL,
  timeout: 10000,
  // 跨域请求时是否需要使用凭证 cookie
  withCredentials: true,
});

// Add a request interceptor
myAxios.interceptors.request.use(
  function (config) {
    // Do something before request is sent
    return config;
  },
  function (error) {
    // Do something with request error
    return Promise.reject(error);
  },
);

// Add a response interceptor
myAxios.interceptors.response.use(
  function (response) {
    // Any status code that lie within the range of 2xx cause this function to trigger
    // Do something with response data
    console.log(response);

    const { data } = response;

    console.log(data);
    // 未登录
    if (data?.code === 40100) {
      // 不是获取用户信息接口，或者不是登录页面，则跳转到登录页面
      if (
        !response.request.responseURL.includes("user/current") &&
        !window.location.pathname.includes("/user/login")
      ) {
        window.location.href = `/user/login?redirect=${window.location.href}`;
      }
      return response;
    }
    // 其他业务错误：统一弹 Toast 提示用户
    if (data?.code !== undefined && data?.code !== 0) {
      const msg = data?.description
        ? `${data.message}：${data.description}`
        : data?.message || "请求失败";
      showToast(msg);
    }
    return response;
  },
  function (error) {
    // Any status codes that falls outside the range of 2xx cause this function to trigger
    // Do something with response error
    // 网络错误或服务器 500 等也提示用户
    const msg =
      error?.response?.data?.message ||
      error?.message ||
      "网络请求失败，请检查网络连接";
    showToast(msg);
    return Promise.reject(error);
  },
);

export default myAxios;
