<template>
  <form action="/">
    <van-search
      v-model="searchText"
      show-action
      placeholder="请输入要搜索的标签"
      @search="onSearch"
      @cancel="onCancel"
    />
  </form>
  <van-divider content-position="left">已选标签</van-divider>
  <div v-if="activeIds.length === 0">请选择标签</div>
  <van-row gutter="16" style="padding: 0 16px">
    <van-col v-for="tag in activeIds" :key="tag">
      <van-tag
        round
        closeable
        size="medium"
        type="primary"
        @close="doClose(tag)"
      >
        {{ tag }}
      </van-tag>
    </van-col>
  </van-row>
  <van-divider content-position="left">选择标签</van-divider>
  <van-loading v-if="tagLoading" style="margin: 20px auto; display: block" />
  <div
    v-else-if="tagError"
    style="text-align: center; color: #ee0a24; padding: 20px"
  >
    {{ tagError }}
  </div>
  <van-tree-select
    v-else
    v-model:active-id="activeIds"
    v-model:main-active-index="activeIndex"
    :items="tagList"
  />
  <div style="padding: 12px">
    <van-button block type="primary" @click="doSearchResult">搜索</van-button>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { getTagTree } from "../api/tag";
// import { showToast } from "vant";

const router = useRouter();

const searchText = ref("");

// 从后端获取的原始标签树（缓存用于搜索过滤恢复）
const originTagList = ref<any[]>([]);

// 标签列表（用于展示，会被搜索过滤修改）
const tagList = ref<any[]>([]);

// 加载状态
const tagLoading = ref(true);
const tagError = ref("");

// 页面挂载时从后端加载标签树
onMounted(async () => {
  tagLoading.value = true;
  tagError.value = "";
  try {
    const res = await getTagTree();
    const tree = res.data?.data ?? [];
    originTagList.value = tree;
    tagList.value = JSON.parse(JSON.stringify(tree));
  } catch (error: any) {
    console.error("加载标签树失败", error);
    tagError.value = "标签加载失败，请确认后端已启动并重启前端";
  } finally {
    tagLoading.value = false;
  }
});

/**
 * 搜索过滤标签
 */
const onSearch = () => {
  const keyword = searchText.value.trim();
  if (!keyword) {
    tagList.value = JSON.parse(JSON.stringify(originTagList.value));
    return;
  }
  tagList.value = originTagList.value.map((parentTag: any) => {
    if (!parentTag) return parentTag;
    const tempChildren = [...(parentTag.children ?? [])];
    const filteredChildren = tempChildren.filter((item: any) =>
      item.text.includes(keyword),
    );
    return { ...parentTag, children: filteredChildren };
  });
};

const onCancel = () => {
  searchText.value = "";
  tagList.value = JSON.parse(JSON.stringify(originTagList.value));
};

// 已选中的标签
const activeIds = ref<string[]>([]);
const activeIndex = ref(0);

// 移除标签
const doClose = (tag: string) => {
  activeIds.value = activeIds.value.filter((item) => {
    return item !== tag;
  });
};

/**
 * 执行搜索
 */
const doSearchResult = () => {
  router.push({
    path: "/user/list",
    query: {
      tags: activeIds.value,
    },
  });
};
</script>

<style scoped></style>
