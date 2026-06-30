<template>
  <div class="history-page">
    <van-nav-bar title="组队历史" left-arrow @click-left="router.back()" />

    <div v-if="loading" class="loading-container">
      <van-loading size="36px" vertical>加载中...</van-loading>
    </div>

    <div v-else-if="historyList.length === 0" class="empty-container">
      <van-empty description="暂无组队记录" />
    </div>

    <div v-else class="timeline">
      <div
        v-for="(item, index) in historyList"
        :key="index"
        class="timeline-item"
      >
        <!-- 时间线圆点和连线 -->
        <div
          class="timeline-dot"
          :class="{ dissolved: item.isDissolved }"
        ></div>
        <div v-if="index < historyList.length - 1" class="timeline-line"></div>

        <!-- 卡片内容 -->
        <div class="history-card">
          <div class="card-header">
            <span class="team-name">{{ item.teamName }}</span>
            <van-tag v-if="item.isCaptain" type="primary"> 队长 </van-tag>
            <van-tag v-if="item.isDissolved" type="danger" plain>
              已解散
            </van-tag>
          </div>

          <div class="card-info">
            <div class="info-row">
              <van-icon name="clock-o" size="14" />
              <span>加入时间：{{ formatDate(item.joinTime) }}</span>
            </div>
            <div class="info-row">
              <van-icon name="underway-o" size="14" />
              <span>组队时长：{{ item.durationDays }} 天</span>
            </div>
          </div>

          <!-- 队友列表 -->
          <div
            class="teammates"
            v-if="item.teammates && item.teammates.length > 0"
          >
            <div class="teammates-title">队友：</div>
            <div class="teammate-list">
              <div
                v-for="mate in item.teammates"
                :key="mate.userId"
                class="teammate-item"
              >
                <van-image
                  v-if="mate.avatarUrl"
                  round
                  width="28"
                  height="28"
                  :src="mate.avatarUrl"
                />
                <div v-else class="avatar-placeholder">
                  {{ mate.username?.charAt(0) || "?" }}
                </div>
                <span class="mate-name">{{ mate.username }}</span>
              </div>
            </div>
          </div>

          <!-- 聊天入口（未解散的队伍） -->
          <div class="card-action" v-if="!item.isDissolved">
            <van-button
              size="mini"
              type="primary"
              plain
              icon="chat-o"
              @click="goToChat(item.teamId)"
            >
              进入聊天
            </van-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { showToast } from "vant";
import { getUserTeamHistory } from "../api/team";

const route = useRoute();
const router = useRouter();
const userId = Number(route.params.userId);

interface Teammate {
  userId: number;
  username: string;
  avatarUrl?: string;
}

interface HistoryItem {
  teamId: number;
  teamName: string;
  teamDescription?: string;
  isDissolved: boolean;
  isCaptain: boolean;
  joinTime: string;
  durationDays: number;
  teammates?: Teammate[];
}

const historyList = ref<HistoryItem[]>([]);
const loading = ref(true);

const formatDate = (dateStr: string) => {
  if (!dateStr) return "未知";
  const date = new Date(dateStr);
  return date.toLocaleDateString("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  });
};

const goToChat = (teamId: number) => {
  router.push({ path: "/team/chat", query: { teamId } });
};

onMounted(async () => {
  try {
    const res = await getUserTeamHistory(userId);
    if (res?.data?.code === 0) {
      historyList.value = res.data.data || [];
    } else {
      showToast("获取组队历史失败");
    }
  } catch (e) {
    showToast("网络错误");
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.history-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 20px;
}

.loading-container,
.empty-container {
  padding: 60px 0;
}

.timeline {
  padding: 16px 16px 16px 32px;
  position: relative;
}

.timeline-item {
  position: relative;
  padding-left: 24px;
  padding-bottom: 20px;
}

.timeline-dot {
  position: absolute;
  left: -6px;
  top: 6px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #1989fa;
  border: 2px solid white;
  box-shadow: 0 0 0 2px #1989fa;
  z-index: 1;
}

.timeline-dot.dissolved {
  background: #ee0a24;
  box-shadow: 0 0 0 2px #ee0a24;
}

.timeline-line {
  position: absolute;
  left: 0;
  top: 20px;
  bottom: 0;
  width: 2px;
  background: #dcdee0;
}

.history-card {
  background: white;
  border-radius: 10px;
  padding: 14px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.team-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.card-info {
  margin-bottom: 8px;
}

.info-row {
  font-size: 13px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 4px;
}

.teammates {
  margin-bottom: 8px;
}

.teammates-title {
  font-size: 12px;
  color: #999;
  margin-bottom: 6px;
}

.teammate-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.teammate-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.avatar-placeholder {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #999;
}

.mate-name {
  font-size: 12px;
  color: #666;
}

.card-action {
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}
</style>
