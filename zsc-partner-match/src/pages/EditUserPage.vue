<template>
  <van-form @submit="onSubmit">
    <van-cell-group inset>
      <van-field
        v-model="editObject.currentValue as string"
        :name="editObject.editKey as string"
        :label="editObject.editName as string"
        :placeholder="`请输入${editObject.editName as string}`"
        :rules="[
          {
            required: true,
            message: ('请填写' + editObject.editName) as string,
          },
        ]"
      />
    </van-cell-group>
    <div style="margin: 16px">
      <van-button round block type="primary" native-type="submit">
        确认修改
      </van-button>
    </div>
  </van-form>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from "vue-router";
import { ref, onMounted } from "vue";
import { updateUser, getCurrentUser } from "../api/user";
import { showToast } from "vant";

const route = useRoute();
const router = useRouter();
const currentUserId = ref<number | null>(null);
const editObject = ref({
  editKey: route.query.editKey,
  editName: route.query.editName,
  currentValue: route.query.currentValue,
});

// 页面加载时获取当前登录用户
onMounted(async () => {
  const res = await getCurrentUser();
  if (res?.data?.code === 0) {
    currentUserId.value = res.data.data.id; // ✅ 保存真实ID
  } else {
    showToast("请先登录");
    router.replace("/user/login");
  }
});

const onSubmit = async () => {
  if (!currentUserId.value) {
    showToast("请先登录");
    router.replace("/user/login");
    return;
  }
  const res = await updateUser(currentUserId.value, {
    [editObject.value.editKey as string]: editObject.value
      .currentValue as string,
  });
  if (res.data.code === 0) {
    showToast("修改成功");
    // 返回上一页
    router.back();
  } else {
    showToast(res?.data?.message || "修改失败");
  }
};
</script>

<style scoped></style>
