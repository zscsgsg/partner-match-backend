<template>
  <div
    class="home-page"
    v-infinite-scroll="loadMore"
    :infinite-scroll-disabled="loading || (checked && matchLoaded)"
    :infinite-scroll-distance="10"
    :infinite-scroll-immediate="false"
  >
    <van-cell center title="AI 智能匹配">
      <template #right-icon>
        <van-switch v-model="checked" @change="handleSwitchChange" />
      </template>
    </van-cell>

    <UserCardList :user-list="userList" :loading="loading" />
    <div v-if="finished && userList.length > 0" class="no-more">
      没有更多了～
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from "vue";
import { recommendUsers, smartMatchUsers } from "../api/user";
import UserCardList from "../components/UserCardList.vue";

const userList = ref<any[]>([]);
const loading = ref(false);
const finished = ref(false);
const checked = ref(false); // false: 推荐模式, true: AI智能匹配模式

// 推荐模式的分页状态
let recCurrentPage = 1;
const recPageSize = 10;

// 智能匹配模式是否已加载完成（匹配模式一次性加载，无需分页）
const matchLoaded = ref(false);

// 切换开关时的处理
const handleSwitchChange = async (newVal: boolean) => {
  // 重置所有状态
  userList.value = [];
  finished.value = false;
  matchLoaded.value = false;
  recCurrentPage = 1;

  if (newVal) {
    // AI 智能匹配模式：一次性加载
    await loadSmartMatchUsers();
  } else {
    // 推荐模式：重新加载推荐用户第一页
    await loadRecommendUsers(true);
  }
};

// 加载智能匹配用户（一次性，含 AI 推荐理由）
const loadSmartMatchUsers = async () => {
  if (loading.value) return;
  loading.value = true;
  try {
    const res = await smartMatchUsers(10);

    let users = res.data.data || [];
    if (!Array.isArray(users)) users = [];
    userList.value = users;
    finished.value = true;
    matchLoaded.value = true;
  } catch (error) {
    console.error("加载智能匹配用户失败", error);
  } finally {
    loading.value = false;
  }
};

// 加载推荐用户（分页）
const loadRecommendUsers = async (refresh = false) => {
  if (loading.value) return;
  loading.value = true;
  try {
    const res = await recommendUsers(refresh ? 1 : recCurrentPage, recPageSize);
    const pageData = res?.data?.data;
    const records = pageData?.records;
    if (!records || records.length === 0) {
      finished.value = true;
      return;
    }
    const processedRecords = records.map((user: any) => {
      if (user.tags && typeof user.tags === "string") {
        try {
          user.tags = JSON.parse(user.tags);
        } catch (e) {
          user.tags = [];
        }
      } else if (!user.tags) {
        user.tags = [];
      }
      return user;
    });
    if (refresh) {
      userList.value = processedRecords;
      recCurrentPage = 2;
    } else {
      userList.value.push(...processedRecords);
      recCurrentPage++;
    }
    if (records.length < recPageSize) {
      finished.value = true;
    }
  } catch (error) {
    console.error("加载推荐用户失败", error);
  } finally {
    loading.value = false;
  }
};

// 无限滚动加载更多
const loadMore = async () => {
  if (loading.value || finished.value) return;
  if (checked.value) {
    // 匹配模式下已一次性加载完成，无需加载更多
    return;
  } else {
    await loadRecommendUsers(false);
  }
};

// 初始加载：默认推荐模式（开关关闭）
onMounted(() => {
  loadRecommendUsers(true);
});
</script>

<style scoped>
.home-page {
  padding: 12px;
  height: 100vh;
  overflow-y: auto;
}
.no-more {
  text-align: center;
  font-size: 12px;
  color: #969799;
  padding: 16px 0;
}
</style>
