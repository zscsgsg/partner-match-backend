<template>
  <div class="teamPage">
    <van-search
      v-model="searchText"
      placeholder="搜索队伍"
      @search="onSearch"
    />

    <van-empty v-if="!teamList.length" class="no-more"
      >没有更多队伍了～</van-empty
    >
    <TeamCardList
      :team-list="teamList"
      :loading="loading"
      @refresh="fetchTeams"
    />
    <van-button
      type="primary"
      class="fab-button"
      icon="plus"
      @click="doJoinTeam"
    />
  </div>
</template>
<script lang="ts" setup>
import { useRouter } from "vue-router";
import { ref, onMounted } from "vue";
import { listMyCreateTeams } from "../api/team";
import TeamCardList from "../components/TeamCardList.vue";

const router = useRouter();
const doJoinTeam = () => {
  router.push("/team/add");
};

const teamList = ref<any[]>([]);
const loading = ref(false);
const searchText = ref("");
const fetchTeams = async (name?: string) => {
  loading.value = true;
  try {
    const res = await listMyCreateTeams({ name });
    teamList.value = res?.data?.data ?? [];
  } catch (error) {
    console.error("获取队伍列表失败", error);
    teamList.value = [];
  } finally {
    loading.value = false;
  }
};
//搜索队伍
const onSearch = async (val: string) => {
  fetchTeams(val);
};
// const handleRefresh = () => {
//   fetchTeams(searchText.value);
// };

onMounted(() => {
  fetchTeams();
});
</script>
<style scoped>
.team-list-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}
.fab-button {
  position: fixed;
  bottom: 60px; /* 避免被底部 TabBar 遮挡 */
  right: 16px;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-color: #1989fa;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  z-index: 100;
  border: none;
  cursor: pointer;
}

.fab-button .van-icon {
  font-size: 24px;
}
</style>
