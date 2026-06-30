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
  </div>
</template>
<script lang="ts" setup>
import { ref, onMounted } from "vue";
import { listMyJoinTeams } from "../api/team";
import TeamCardList from "../components/TeamCardList.vue";

const teamList = ref<any[]>([]);
const loading = ref(false);
const searchText = ref("");
const fetchTeams = async (name?: string) => {
  loading.value = true;
  try {
    const res = await listMyJoinTeams({ name });
    teamList.value = res?.data?.data ?? [];
  } catch (error) {
    console.error("获取我加入的队伍列表失败", error);
    teamList.value = [];
  } finally {
    loading.value = false;
  }
};

// const handleRefresh = () => {
//   fetchTeams(searchText.value);
// };
//搜索队伍
const onSearch = async (val: string) => {
  fetchTeams(val);
};

onMounted(() => {
  fetchTeams();
});
</script>
<style scoped>
.team-list-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}
</style>
