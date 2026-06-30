<template>
  <div class="teamAddPage">
    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field
          v-model="addTeamData.name"
          name="name"
          label="名称"
          placeholder="请输入队伍名称"
          :rules="[{ required: true, message: '请填写队伍名称' }]"
        />
        <van-field
          v-model="addTeamData.description"
          name="description"
          rows="4"
          label="描述"
          placeholder="请输入队伍描述"
          type="textarea"
          :rules="[{ required: true, message: '请填写队伍描述' }]"
        />

        <!-- 过期时间选择 -->
        <van-field
          v-model="addTeamData.expireTime"
          is-link
          readonly
          name="expireTime"
          label="过期时间"
          placeholder="点击选择过期时间"
          @click="showPicker = true"
        />
        <van-popup v-model:show="showPicker" destroy-on-close position="bottom">
          <van-date-picker
            :model-value="pickerValue"
            @confirm="onConfirm"
            @cancel="showPicker = false"
          />
        </van-popup>
        <van-field name="status" label="状态">
          <template #input>
            <van-radio-group
              v-model="addTeamData.status"
              direction="horizontal"
            >
              <van-radio name="0">公开</van-radio>
              <van-radio name="1">私有</van-radio>
              <van-radio name="2">加密</van-radio>
            </van-radio-group>
          </template>
        </van-field>

        <van-field
          v-if="Number(addTeamData.status) === 2"
          v-model="addTeamData.password"
          type="password"
          name="password"
          label="密码"
          placeholder="请输入密码"
          :rules="[{ required: true, message: '请填写密码' }]"
        />
      </van-cell-group>

      <div style="margin: 16px">
        <van-button round block type="primary" native-type="submit">
          更新队伍
        </van-button>
      </div>
    </van-form>
  </div>
</template>
<script lang="ts" setup>
import { ref, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { getTeamById, updateTeam } from "../api/team";
import { showToast } from "vant";
import type { TeamType } from "../models/team";

const router = useRouter();
const route = useRoute();
const teamId = Number(route.query.id);

const addTeamData = ref({
  name: "",
  description: "",
  expireTime: "",
  status: 0,
  password: "",
});

const showPicker = ref(false);
const pickerValue = ref<string[]>([]);

const getCurrentDateArray = () => {
  const now = new Date();
  const year = now.getFullYear().toString();
  // 月份从0开始，所以+1，且需要补零
  const month = (now.getMonth() + 1).toString().padStart(2, "0");
  const day = now.getDate().toString().padStart(2, "0");
  return [year, month, day];
};

// 4. 在组件挂载时设置默认值
onMounted(async () => {
  pickerValue.value = getCurrentDateArray();
  if (!teamId) {
    showToast("队伍不存在");
    router.push({
      path: "/team",
      replace: true,
    });
  }
  const res = await getTeamById(teamId);

  if (res.data.code === 0) {
    addTeamData.value = res.data.data;

    if (addTeamData.value.expireTime) {
      const d = new Date(addTeamData.value.expireTime);
      if (!isNaN(d.getTime())) {
        pickerValue.value = [
          d.getFullYear().toString(),
          (d.getMonth() + 1).toString().padStart(2, "0"),
          d.getDate().toString().padStart(2, "0"),
        ];
      }
    }
  }
});

const onConfirm = ({ selectedValues }: { selectedValues: string[] }) => {
  /// 显示用，例如 "2025-12-31"
  const dateStr = selectedValues.join("-");
  const dateTimeStr = `${dateStr}T23:59:59`;
  addTeamData.value.expireTime = dateTimeStr;
  //记录选择的数组
  pickerValue.value = selectedValues;
  // 关闭弹窗
  showPicker.value = false;
};

const onSubmit = async () => {
  // 校验表单
  // const postData = {
  //   ...addTeamData.value,
  //   status: Number(addTeamData.value.status),
  // };

  const postData: Partial<TeamType> = {
    id: teamId, // 必须包含 id
    name: addTeamData.value.name,
    description: addTeamData.value.description,

    status: Number(addTeamData.value.status),
    // 将字符串转为 Date 对象
    expireTime: addTeamData.value.expireTime
      ? new Date(addTeamData.value.expireTime)
      : undefined,
    password: addTeamData.value.password,
  };

  // 提交表单数据
  const res = await updateTeam(postData);
  if (res.data.code === 0) {
    showToast("更新成功");
    //回到队伍列表页
    router.push({
      path: "/team",
      replace: true,
    });
  }
};
</script>
<style scoped></style>
