<template>
  <div class="edit-tags-page">
    <van-nav-bar title="编辑标签" left-arrow @click-left="router.back()" />

    <van-divider content-position="left">已选标签</van-divider>
    <div v-if="activeIds.length === 0" style="padding: 0 16px; color: #999">
      请选择标签
    </div>
    <van-row gutter="16" style="padding: 0 16px">
      <van-col v-for="tag in activeIds" :key="tag" style="margin-bottom: 8px">
        <van-tag
          round
          closeable
          size="large"
          type="primary"
          @close="doClose(tag)"
        >
          {{ tag }}
        </van-tag>
      </van-col>
    </van-row>

    <van-divider content-position="left">选择标签</van-divider>
    <van-loading v-if="tagLoading" style="margin: 20px auto; display: block" />
    <van-tree-select
      v-else
      v-model:active-id="activeIds"
      v-model:main-active-index="activeIndex"
      :items="tagList"
    />

    <div style="padding: 12px">
      <van-button block type="primary" :loading="saving" @click="doSaveTags">
        保存标签
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { showToast } from "vant";
import { getTagTree } from "../api/tag";
import { getCurrentUser, updateUser } from "../api/user";

const router = useRouter();

const tagList = ref<any[]>([]);
const tagLoading = ref(true);
const activeIds = ref<string[]>([]);
const activeIndex = ref(0);
const saving = ref(false);
const currentUserId = ref<number | null>(null);

const doClose = (tag: string) => {
  activeIds.value = activeIds.value.filter((item) => item !== tag);
};

const doSaveTags = async () => {
  if (!currentUserId.value) {
    showToast("请先登录");
    return;
  }
  saving.value = true;
  try {
    const tagsValue = JSON.stringify(activeIds.value);
    const res = await updateUser(currentUserId.value, { tags: tagsValue });
    if (res?.data?.code === 0) {
      showToast("保存成功");
      router.back();
    } else {
      showToast(res?.data?.message || "保存失败");
    }
  } catch (e) {
    showToast("保存失败");
  } finally {
    saving.value = false;
  }
};

onMounted(async () => {
  // 获取当前用户
  const userRes = await getCurrentUser();
  if (userRes?.data?.code === 0 && userRes.data.data) {
    currentUserId.value = userRes.data.data.id;
    // 加载用户已有的标签
    const userTags = userRes.data.data.tags;
    if (userTags) {
      try {
        const parsed =
          typeof userTags === "string" ? JSON.parse(userTags) : userTags;
        if (Array.isArray(parsed)) {
          activeIds.value = parsed;
        }
      } catch {}
    }
  } else {
    showToast("请先登录");
    router.replace("/user/login");
    return;
  }

  // 加载标签树
  try {
    const res = await getTagTree();
    tagList.value = res.data?.data ?? [];
  } catch (error) {
    console.error("加载标签树失败", error);
    showToast("标签加载失败");
  } finally {
    tagLoading.value = false;
  }
});
</script>

<style scoped>
.edit-tags-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 20px;
}
</style>
