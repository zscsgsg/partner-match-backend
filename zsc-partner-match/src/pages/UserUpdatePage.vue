<template>
  <div v-if="user">
    <van-cell
      title="头像"
      is-link
      @click="toEdit('avatarUrl', '头像', user.avatarUrl)"
    >
      <img style="height: 40px; width: 40px" :src="user.avatarUrl" />
    </van-cell>
    <van-cell
      title="昵称"
      is-link
      :value="user.username"
      @click="toEdit('username', '昵称', user.username)"
    />
    <van-cell title="账号" :value="user.userAccount" />
    <van-cell
      title="简介"
      is-link
      :value="user.profile || '未填写'"
      @click="toEdit('profile', '简介', user.profile)"
    />
    <van-cell
      title="性别"
      is-link
      :value="user.gender === 1 ? '男' : user.gender === 0 ? '女' : '未知'"
      @click="toEdit('gender', '性别', user.gender)"
    />
    <van-cell
      title="电话"
      is-link
      :value="user.phone"
      @click="toEdit('phone', '电话', user.phone)"
    />
    <van-cell
      title="邮箱"
      is-link
      :value="user.email"
      @click="toEdit('email', '邮箱', user.email)"
    />
    <van-cell
      title="标签"
      is-link
      :value="displayTags"
      @click="router.push('/user/edit/tags')"
    />
    <van-cell
      title="注册时间"
      :value="new Date(user.createTime).toLocaleString()"
      style="min-width: 70%"
    />
  </div>
  <van-empty v-else description="请先登录" />
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { showToast } from "vant";
import { getCurrentUser } from "../api/user";
import type { UserType } from "../models/user";
import { setCurrentUserState } from "../states/user";

const router = useRouter();
const user = ref<UserType | null>(null);

const displayTags = computed(() => {
  if (!user.value?.tags) return "未设置";
  try {
    const tags =
      typeof user.value.tags === "string"
        ? JSON.parse(user.value.tags)
        : user.value.tags;
    return Array.isArray(tags) && tags.length > 0 ? tags.join("、") : "未设置";
  } catch {
    return "未设置";
  }
});

onMounted(async () => {
  const res = await getCurrentUser();
  if (res?.data?.code === 0) {
    user.value = res.data.data;
    setCurrentUserState(user.value as UserType);
  } else {
    showToast("获取用户信息失败，请重新登录");
    // 可选：跳转到登录页
    router.push("/user/login");
  }
});

const toEdit = (editKey: string, editName: string, currentValue: any) => {
  router.push({
    path: "/user/edit",
    query: {
      editKey,
      editName,
      currentValue: String(currentValue),
    },
  });
};
</script>
