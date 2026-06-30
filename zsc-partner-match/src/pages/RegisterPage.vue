<template>
  <div class="register-container">
    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field
          v-model="registerForm.userAccount"
          name="userAccount"
          label="账号"
          placeholder="请输入账号（至少4位）"
          :rules="[{ required: true, message: '请填写账号' }]"
        />
        <van-field
          v-model="registerForm.userPassword"
          type="password"
          name="userPassword"
          label="密码"
          placeholder="请输入密码（至少6位）"
          :rules="[{ required: true, message: '请填写密码' }]"
        />
        <van-field
          v-model="registerForm.checkPassword"
          type="password"
          name="checkPassword"
          label="确认密码"
          placeholder="请再次输入密码"
          :rules="[{ required: true, message: '请确认密码' }]"
        />
      </van-cell-group>
      <div style="margin: 16px">
        <van-button round block type="primary" native-type="submit">
          注册
        </van-button>
        <van-button
          round
          block
          plain
          type="primary"
          style="margin-top: 12px"
          @click="toLogin"
        >
          已有账号？去登录
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup lang="ts">
import { reactive } from "vue";
import { useRouter } from "vue-router";
import { showToast } from "vant";
import { userRegister } from "../api/user";

const router = useRouter();

const registerForm = reactive({
  userAccount: "",
  userPassword: "",
  checkPassword: "",
});

const onSubmit = async () => {
  if (registerForm.userPassword !== registerForm.checkPassword) {
    showToast("两次输入的密码不一致");
    return;
  }
  const res = await userRegister(registerForm);
  if (res?.data?.code === 0) {
    showToast("注册成功，请登录");
    router.replace("/user/login");
  }
};

const toLogin = () => {
  router.push("/user/login");
};
</script>

<style scoped>
.register-container {
  padding: 20px;
}
</style>
