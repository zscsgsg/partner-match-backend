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
        <van-field name="stepper" label="最大人数"">
          <template #input>
            <van-stepper v-model="addTeamData.maxNum" :min="1" :max="10" />
          </template>
        </van-field>

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
          创建队伍
        </van-button>
        <van-button
          round
          block
          plain
          type="primary"
          style="margin-top: 12px"
          @click="goBack"
        >
          返回
        </van-button>
      </div>
    </van-form>
  </div>
</template>
<script lang="ts" setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { addTeam } from "../api/team";
import { showToast } from "vant";

const router = useRouter();

const initForemData = {
  name: "",
  description: "",
  maxNum: 3,
  expireTime: "",
  status: 0,
  password: "",
};
const addTeamData = ref({ ...initForemData });

const showPicker = ref(false);
const pickerValue = ref<string[]>([]);

const getCurrentDateArray = () => {
  const now = new Date();
  const year = now.getFullYear().toString();
  // 月份从0开始，所以+1，且需要补零
  const month = (now.getMonth() + 1).toString().padStart(2, '0');
  const day = now.getDate().toString().padStart(2, '0');
  return [year, month, day];
};

// 4. 在组件挂载时设置默认值
onMounted(() => {
  pickerValue.value = getCurrentDateArray();
  
  // 可选：如果希望表单里也默认显示今天的过期时间（例如今天+7天，或者就是今天），可以同步更新 addTeamData
  // 这里仅设置 picker 的默认选中项，不强制填充表单，除非你需要
});









const onConfirm = ({ selectedValues }: { selectedValues: string[] }) => {
  /// 显示用，例如 "2025-12-31"
  const dateStr =selectedValues.join("-");
  const dateTimeStr = `${dateStr}T23:59:59`;
  addTeamData.value.expireTime = dateTimeStr;
  //记录选择的数组
  pickerValue.value = selectedValues;
  // 关闭弹窗
  showPicker.value = false;
};
const goBack = () => {
  router.back();
  addTeamData.value = { ...initForemData };
};
const onSubmit = async () => {
  // 校验表单
  const postData = {
      ...addTeamData.value,
      status: Number(addTeamData.value.status),

  }
  // 提交表单数据
  const res = await addTeam(postData);
  if (res.data.code === 0) {
    showToast("创建成功");
    //回到队伍列表页
    router.push({ 
      path: "/team",
      replace: true
     });
  }
}
</script>
<style scoped></style>
