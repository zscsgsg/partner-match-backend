<template>
  <van-card
    v-for="item in userList"
    :desc="item.profile"
    :title="item.username"
    :thumb="item.avatarUrl"
  >
    <template #tags>
      <van-tag
        plain
        type="danger"
        v-for="tag in item.tags"
        :key="tag"
        style="margin-right: 8px"
      >
        {{ tag }}</van-tag
      >
    </template>
    <template #footer>
      <van-button size="mini">联系我</van-button>
    </template>
  </van-card>
  <van-empty v-if="!userList || userList.length < 1" description="暂无用户" />
</template>
<script lang="ts" setup>
import { useRoute } from "vue-router";
import { ref, onMounted } from "vue";
import { searchUsersByTag } from "../api/user";
// const router = useRouter();
const route = useRoute();
//获取搜索标签（可能是字符串或数组）
const { tags } = route.query;
const tagStr = Array.isArray(tags) ? tags.join(",") : (tags as string) || "";
//用一个钩子获取标签对应的用户
onMounted(async () => {
  try {
    const res = await searchUsersByTag(tagStr);
    const userListData = res.data.data;
    if (userListData) {
      userListData.forEach((user: any) => {
        user.tags = JSON.parse(user.tags);
      });
      userList.value = userListData;
    }
  } catch (error) {
    console.error("搜索用户失败", error);
    userList.value = [];
  }
});
//console.log(tags);

const userList = ref<any[]>([]);
</script>
