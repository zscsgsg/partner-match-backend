<template>
  <div class="user-card-list">
    <van-card
      v-for="user in userList"
      :key="user.id || user.userId"
      :desc="user.profile || '这个人很懒，什么都没写'"
      :title="
        (user.username || user.userAccount) +
        (user.totalScore !== undefined
          ? ' — 匹配度 ' + Math.round(user.totalScore * 100) + '%'
          : '')
      "
      :thumb="user.avatarUrl || defaultAvatar"
      @click="toUserDetail(user.id || user.userId)"
    >
      <!-- AI 推荐理由 -->
      <template v-if="user.explanation" #desc>
        <div class="match-explanation">
          <van-icon
            name="bullhorn-o"
            size="14"
            style="margin-right: 4px; color: #1989fa"
          />
          <span>{{ user.explanation }}</span>
        </div>
        <div
          class="match-detail"
          v-if="user.commonTags && user.commonTags.length"
        >
          <span class="detail-label">共同标签：</span>
          <van-tag
            v-for="tag in user.commonTags"
            :key="tag"
            plain
            size="medium"
            type="success"
            style="margin-right: 4px"
          >
            {{ tag }}
          </van-tag>
        </div>
      </template>

      <template #tags>
        <van-tag
          v-for="tag in Array.isArray(user.tags) ? user.tags : []"
          :key="tag"
          plain
          type="primary"
          style="margin-right: 8px; margin-bottom: 4px"
        >
          {{ tag }}
        </van-tag>
      </template>
      <template #footer>
        <van-button size="mini" type="primary" @click.stop="contact(user)">
          联系我
        </van-button>
      </template>
    </van-card>

    <van-empty
      v-if="userList.length === 0 && !loading"
      description="暂无用户"
    />

    <van-loading v-if="loading" class="loading" size="24px" />
  </div>
</template>

<script setup lang="ts">
import type { PropType } from "vue";
import { showToast } from "vant";
import type { UserType } from "../models/user";

const props = defineProps({
  userList: {
    type: Array as PropType<UserType[]>,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
});

const defaultAvatar = "https://picsum.photos/200/200?random=default";

const toUserDetail = (userId?: number) => {
  if (!userId) return;
  // 暂无用户详情页，后续可扩展
  showToast(`查看用户 ID: ${userId}`);
};

const contact = (user: UserType) => {
  showToast(`联系 ${user.username || user.userAccount}`);
  // 实际可跳转到聊天页面或复制联系方式
};
</script>

<style scoped>
.user-card-list {
  padding: 12px;
}
.loading {
  display: flex;
  justify-content: center;
  margin: 20px 0;
}
.match-explanation {
  display: flex;
  align-items: flex-start;
  background: #f0f8ff;
  padding: 8px 10px;
  border-radius: 6px;
  margin-bottom: 6px;
  font-size: 13px;
  color: #333;
  line-height: 1.5;
}
.match-detail {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 4px;
}
.detail-label {
  font-size: 12px;
  color: #999;
}
</style>
