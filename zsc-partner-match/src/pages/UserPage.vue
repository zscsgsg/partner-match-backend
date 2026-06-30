<template>
  <div v-if="user">
    <van-cell title="当前用户" :value="user?.username" />

    <!-- 用户画像卡片 -->
    <div class="profile-card" v-if="profile">
      <div class="profile-title"><van-icon name="bar-chart-o" /> 我的画像</div>
      <div class="profile-grid">
        <div class="profile-item">
          <div class="profile-value">{{ profile.totalTeamsCreated }}</div>
          <div class="profile-label">创建队伍</div>
        </div>
        <div class="profile-item">
          <div class="profile-value">{{ profile.totalTeamsJoined }}</div>
          <div class="profile-label">加入队伍</div>
        </div>
        <div class="profile-item">
          <div class="profile-value">
            {{ profile.cooperationScore?.toFixed(1) }}
          </div>
          <div class="profile-label">协作评分</div>
        </div>
        <div class="profile-item">
          <div class="profile-value">
            {{ profile.avgTeamDuration?.toFixed(0) }}天
          </div>
          <div class="profile-label">平均组队时长</div>
        </div>
      </div>
      <div class="profile-tags" v-if="preferredTagsList.length > 0">
        <span class="tag-label">活跃标签：</span>
        <van-tag
          v-for="tag in preferredTagsList"
          :key="tag"
          type="primary"
          plain
          size="medium"
          style="margin: 2px 4px"
        >
          {{ tag }}
        </van-tag>
      </div>
    </div>

    <van-cell title="修改信息" is-link to="/user/update" />
    <van-cell title="我的画像" is-link to="/user/profile" />
    <van-cell title="组队历史" is-link :to="`/team/${user.id}/history`" />
    <van-cell title="我创建的队伍" is-link to="/user/team/create" />
    <van-cell title="我加入的队伍" is-link to="/user/team/join" />
    <van-button
      round
      block
      type="danger"
      style="margin: 16px auto; width: 90%; display: block"
      @click="doLogout"
    >
      退出登录
    </van-button>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { showToast } from "vant";
import {
  getCurrentUser,
  userLogout,
  getUserProfile,
  calculateUserProfile,
} from "../api/user";
import type { UserType } from "../models/user";
import { setCurrentUserState } from "../states/user";

const router = useRouter();
const user = ref<UserType | null>(null);

interface UserProfileType {
  userId: number;
  totalTeamsCreated: number;
  totalTeamsJoined: number;
  avgTeamDuration: number;
  cooperationScore: number;
  preferredTags: string;
}

const profile = ref<UserProfileType | null>(null);

const preferredTagsList = computed(() => {
  if (!profile.value?.preferredTags) return [];
  try {
    return JSON.parse(profile.value.preferredTags) as string[];
  } catch {
    return [];
  }
});

onMounted(async () => {
  const res = await getCurrentUser();
  if (res?.data?.code === 0) {
    user.value = res.data.data;
    setCurrentUserState(user.value as UserType);

    // 先触发画像计算（更新统计数据），再加载画像
    await calculateUserProfile().catch(() => {});
    const profileRes = await getUserProfile();
    if (profileRes?.data?.code === 0) {
      profile.value = profileRes.data.data;
    }
  } else {
    showToast("获取用户信息失败，请重新登录");
    router.push("/user/login");
  }
});

const doLogout = async () => {
  try {
    await userLogout({});
    showToast("已退出登录");
  } catch (e) {
    // 即使接口失败也清空状态
  }
  setCurrentUserState(null as any);
  router.replace("/user/login");
};
</script>

<style scoped>
.profile-card {
  margin: 12px 16px;
  background: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.profile-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  margin-bottom: 10px;
}

.profile-item {
  text-align: center;
}

.profile-value {
  font-size: 20px;
  font-weight: 700;
  color: #1989fa;
}

.profile-label {
  font-size: 11px;
  color: #999;
  margin-top: 2px;
}

.profile-tags {
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}

.tag-label {
  font-size: 12px;
  color: #999;
}
</style>
