<template>
  <van-skeleton
    title
    avatar
    :thumb="ikun"
    :row="3"
    :loading="props.loading"
    v-for="team in props.teamList"
  >
    <van-card :desc="team.description" :title="`${team.name}`">
      <template #tags>
        <van-tag plain type="danger" style="margin-right: 8px; margin-top: 8px">
          {{ teamStatusEnum[team.status] }}
        </van-tag>
      </template>

      <template #bottom>
        <div>队伍人数 {{ team.hasJoinNum }}/{{ team.maxNum }}</div>
        <div>创建时间 {{ team.createTime }}</div>
        <div v-if="team.expireTime">过期时间 {{ team.expireTime }}</div>
      </template>
      <template #footer>
        <van-button
          type="primary"
          v-if="currentUser?.id !== team.userId && !team.hasJoin"
          size="mini"
          @click="doJoinTeam(team)"
        >
          加入队伍
        </van-button>
        <van-button
          plain
          type="primary"
          v-if="currentUser?.id === team.userId"
          size="mini"
          @click="updateTeam1(team.id)"
          >更新队伍</van-button
        >
        <van-button
          plain
          type="primary"
          v-if="currentUser?.id !== team.userId && team.hasJoin"
          size="mini"
          @click="doExitTeam(team.id)"
          >退出队伍</van-button
        >
        <van-button
          type="warning"
          v-if="currentUser?.id === team.userId"
          size="mini"
          @click="doDeleteTeam(team.id)"
          >解散队伍</van-button
        >
        <van-button
          type="success"
          v-if="team.hasJoin"
          size="mini"
          icon="chat-o"
          @click="goToChat(team.id)"
          >聊天</van-button
        >
      </template>
    </van-card>
  </van-skeleton>
  <van-dialog
    v-model:show="showPassworldDialog"
    title="请输入队伍密码"
    show-cancel-button
    @confirm="confirmJoin"
  >
    <van-field v-model="password" placeholder="请输入密码" />
  </van-dialog>
</template>

<script setup lang="ts">
import type { TeamType } from "../models/team";
import { teamStatusEnum } from "../constants/team";
import ikun from "../assets/vite.svg";
import { showToast } from "vant";
import { joinTeam, quitTeam, deleteTeam } from "../api/team";
import { getCurrentUser } from "../api/user";
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
const router = useRouter();
interface TeamCardListProps {
  loading: boolean;
  teamList: TeamType[];
}
const password = ref("");
const showPassworldDialog = ref(false);
const currentUser = ref();
const props = withDefaults(defineProps<TeamCardListProps>(), {
  loading: true,
  // @ts-ignore
  teamList: [] as TeamType[],
});
const currentTeamId = ref<number | null>(null);
const emit = defineEmits(["refresh"]);
const joinTeam1 = async (id: number, password?: string) => {
  const res = await joinTeam(id, password);
  if (res.data.code === 0) {
    showToast("加入队伍");
    emit("refresh");
  } else {
    showToast(res.data.description || "加入队伍失败");
  }
};

const doJoinTeam = (team: TeamType) => {
  if (team.status === 2) {
    // 加密队伍：需要输入密码
    showPassworldDialog.value = true;
    currentTeamId.value = team.id;
    password.value = "";
  } else {
    // 公开(0)或私有(1)队伍：直接加入
    joinTeam1(team.id);
  }
};
const doExitTeam = async (id: number) => {
  const res = await quitTeam(id);
  if (res.data.code === 0) {
    showToast("退出队伍");
    emit("refresh");
  } else {
    showToast(res.data.description || "退出队伍失败");
  }
};
const doDeleteTeam = async (id: number) => {
  const res = await deleteTeam(id);
  if (res.data.code === 0) {
    showToast("解散队伍");
    emit("refresh");
  } else {
    showToast(res.data.description || "解散队伍失败");
  }
};

const updateTeam1 = async (id: number) => {
  router.push({
    path: "/team/update",
    query: { id },
  });
};
const confirmJoin = async () => {
  if (currentTeamId.value !== null) {
    await joinTeam1(currentTeamId.value, password.value);
    password.value = ""; // 清空密码
    currentTeamId.value = null; // 清除记录
  }
  showPassworldDialog.value = false;
};
const goToChat = (teamId: number) => {
  router.push({ path: "/team/chat", query: { teamId } });
};
onMounted(async () => {
  const res = await getCurrentUser();

  currentUser.value = res.data.data;
});
</script>

<style scoped></style>
