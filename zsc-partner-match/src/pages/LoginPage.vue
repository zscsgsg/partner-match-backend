<template>
  <div class="login-container">
    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field
          v-model="loginForm.userAccount"
          name="userAccount"
          label="账号"
          placeholder="请输入账号"
          :rules="[{ required: true, message: '请填写账号' }]"
        />
        <van-field
          v-model="loginForm.userPassword"
          type="password"
          name="userPassword"
          label="密码"
          placeholder="请输入密码"
          :rules="[{ required: true, message: '请填写密码' }]"
        />
      </van-cell-group>
      <div style="margin: 16px">
        <van-button round block type="primary" native-type="submit">
          登录
        </van-button>
        <van-button
          round
          block
          plain
          type="primary"
          style="margin-top: 12px"
          @click="toRegister"
        >
          注册账号
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup lang="ts">
import { reactive } from "vue";
import { useRouter, useRoute } from "vue-router";
import { showToast } from "vant";
import { userLogin } from "../api/user";

const router = useRouter();
const route = useRoute();

const loginForm = reactive({
  userAccount: "",
  userPassword: "",
});

const onSubmit = async () => {
  const res = await userLogin(loginForm);

  if (res?.data?.code === 0) {
    showToast("登录成功");
    // 获取 redirect 参数，跳回原页面，否则跳转首页
    const redirect = route.query.redirect as string;
    if (redirect) {
      window.location.href = redirect;
    } else {
      router.replace("/");
    }
  }
};

const toRegister = () => {
  router.push("/user/register");
};
</script>

<style scoped>
.login-container {
  padding: 20px;
}
</style>
