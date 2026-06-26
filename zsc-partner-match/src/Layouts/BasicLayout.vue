<template>
  <van-nav-bar
    :title="title"
    right-text="按钮"
    :left-arrow="leftArrow"
    @click-left="onClickLeft"
    @click-right="onClickRight"
  >
    <template #right>
      <van-icon name="search" size="18" />
    </template>
  </van-nav-bar>

  <!-- //内容 -->
  <div class="content" style="padding-bottom: 60px">
    <!-- //先用简单模式，路由之后再加 -->
    <router-view />
  </div>

  <van-tabbar route>
    <van-tabbar-item icon="home-o" name="index" to="/">主页</van-tabbar-item>
    <van-tabbar-item icon="search" name="team" to="/team"
      >队伍列</van-tabbar-item
    >
    <van-tabbar-item icon="friends-o" name="user" to="/user"
      >个人</van-tabbar-item
    >
  </van-tabbar>
</template>

<script setup lang="ts">
// import { ref } from "vue";

import { ref } from "vue";
import { useRouter } from "vue-router";
const router = useRouter();
// import { showToast } from "vant";
const leftArrow = ref(true);
const onClickLeft = () => router.back();
const onClickRight = () => router.push("/search");
const title = ref<string>("伙伴匹配系统");

router.beforeEach((to, _from, next) => {
  const routeTitle = to.path;
  const route = router.options.routes.find((item) => item.path === routeTitle);
  if (!route) {
    title.value = "伙伴匹配系统";
  } else {
    title.value = (route.meta?.title as string) ?? "伙伴匹配系统";
  }
  next();
});

// watch(route, (value) => {
//   console.log(value.name);
//   if (
//     value.name === "主页" ||
//     value.name === "队伍" ||
//     value.name === "个人信息"
//   ) {
//     leftArrow.value = false;
//   } else {
//     leftArrow.value = true;
//   }
// });
</script>
