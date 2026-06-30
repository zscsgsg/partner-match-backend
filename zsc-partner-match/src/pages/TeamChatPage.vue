<template>
  <div class="chat-container">
    <!-- 顶部标题栏 -->
    <van-nav-bar title="队伍聊天室" left-arrow @click-left="router.back()">
      <template #right>
        <span :class="['status-dot', connected ? 'online' : 'offline']"></span>
        <span style="font-size: 12px; margin-left: 4px; color: #999">{{
          connected ? "已连接" : "连接中..."
        }}</span>
      </template>
    </van-nav-bar>

    <!-- 消息列表 -->
    <div class="message-list" ref="messageListRef">
      <div v-if="loading" class="loading-tip">
        <van-loading size="24px">加载中...</van-loading>
      </div>
      <div v-if="!loading && messages.length === 0" class="empty-tip">
        暂无消息，发一条试试吧
      </div>
      <div
        v-for="(msg, index) in messages"
        :key="index"
        :class="[
          'message-item',
          msg.type === 'system'
            ? 'system'
            : msg.senderId === currentUserId
              ? 'mine'
              : 'other',
        ]"
      >
        <!-- 系统消息 -->
        <div v-if="msg.type === 'system'" class="system-msg">
          {{ msg.content }}
        </div>
        <!-- 聊天消息 -->
        <div v-else class="chat-msg">
          <div class="sender-name">{{ msg.senderName }}</div>
          <div class="bubble">{{ msg.content }}</div>
          <div class="msg-time">{{ formatTime(msg.createTime) }}</div>
        </div>
      </div>
    </div>

    <!-- 底部输入栏 -->
    <div class="input-bar">
      <van-field
        v-model="inputText"
        placeholder="输入消息..."
        @keyup.enter="sendMessage"
      />
      <van-button
        type="primary"
        size="small"
        @click="sendMessage"
        :disabled="!inputText.trim()"
      >
        发送
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from "vue";
import { useRoute, useRouter } from "vue-router";
import { showToast } from "vant";
import { getChatMessages } from "../api/team";
import { getCurrentUser } from "../api/user";

const route = useRoute();
const router = useRouter();

// 从路由参数获取队伍信息
const teamId = Number(route.query.teamId);
const currentUserId = ref<number>(0);
const currentUserName = ref<string>("");

// 消息数据
interface ChatMessage {
  type: "chat" | "system";
  senderId?: number;
  senderName?: string;
  content: string;
  createTime: string;
}

const messages = ref<ChatMessage[]>([]);
const inputText = ref("");
const connected = ref(false);
const loading = ref(true);
const messageListRef = ref<HTMLElement | null>(null);

let ws: WebSocket | null = null;
let reconnectTimer: ReturnType<typeof setTimeout> | null = null;
const reconnectCount = ref(0);

// 加载历史消息
const loadHistory = async () => {
  try {
    const res = await getChatMessages(teamId, 1, 50);
    if (res?.data?.code === 0) {
      const records = res.data.data?.records || [];
      // API 返回按时间降序，需要反转为升序显示
      messages.value = records.reverse().map((r: any) => ({
        type: "chat" as const,
        senderId: r.senderId,
        senderName: r.senderName,
        content: r.content,
        createTime: r.createTime,
      }));
      scrollToBottom();
    }
  } catch (e) {
    console.error("加载历史消息失败", e);
  } finally {
    loading.value = false;
  }
};

// 初始化 WebSocket 连接
const connectWebSocket = () => {
  if (!teamId || !currentUserId.value) {
    console.error(
      "WebSocket 参数缺失: teamId=%s userId=%s",
      teamId,
      currentUserId.value,
    );
    return;
  }
  const userName = encodeURIComponent(currentUserName.value || "anonymous");
  const wsUrl = `ws://localhost:8082/ws/team/${teamId}/${currentUserId.value}/${userName}`;
  console.log("WebSocket 连接中...", wsUrl);
  ws = new WebSocket(wsUrl);

  ws.onopen = () => {
    connected.value = true;
    reconnectCount.value = 0;
    console.log("WebSocket 连接成功");
  };

  ws.onmessage = (event) => {
    try {
      const msg = JSON.parse(event.data) as ChatMessage;
      messages.value.push(msg);
      scrollToBottom();
    } catch (e) {
      console.error("解析消息失败", e);
    }
  };

  ws.onclose = () => {
    connected.value = false;
    console.log("WebSocket 连接断开");
    // 自动重连（最多 3 次）
    if (reconnectCount.value < 3) {
      const delay = (reconnectCount.value + 1) * 2000;
      console.log(`将在 ${delay}ms 后重连...`);
      reconnectTimer = setTimeout(() => {
        reconnectCount.value++;
        connectWebSocket();
      }, delay);
    }
  };

  ws.onerror = (error) => {
    console.error("WebSocket 错误", error);
  };
};

// 发送消息
const sendMessage = () => {
  const text = inputText.value.trim();
  if (!text) return;
  if (!ws || ws.readyState !== WebSocket.OPEN) {
    showToast("未连接到聊天室，请检查后端是否启动");
    return;
  }
  ws.send(text);
  inputText.value = "";
};

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight;
    }
  });
};

// 格式化时间
const formatTime = (timeStr: string) => {
  if (!timeStr) return "";
  const date = new Date(timeStr);
  const now = new Date();
  const isToday = date.toDateString() === now.toDateString();
  if (isToday) {
    return date.toLocaleTimeString("zh-CN", {
      hour: "2-digit",
      minute: "2-digit",
    });
  }
  return date.toLocaleString("zh-CN", {
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
};

onMounted(async () => {
  // 获取当前用户信息
  const userRes = await getCurrentUser();
  if (userRes?.data?.code === 0 && userRes.data.data) {
    currentUserId.value = userRes.data.data.id;
    currentUserName.value = userRes.data.data.username || "匿名用户";
  } else {
    showToast("请先登录");
    router.push("/user/login");
    return;
  }

  // 加载历史消息
  await loadHistory();

  // 建立 WebSocket 连接
  connectWebSocket();
});

onUnmounted(() => {
  if (reconnectTimer) clearTimeout(reconnectTimer);
  if (ws) {
    ws.close();
    ws = null;
  }
});
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  padding-bottom: 8px;
}

.loading-tip,
.empty-tip {
  text-align: center;
  padding: 40px 0;
  color: #999;
  font-size: 14px;
}

.message-item {
  margin-bottom: 12px;
}

.message-item.system {
  text-align: center;
}

.system-msg {
  display: inline-block;
  background: rgba(0, 0, 0, 0.05);
  color: #999;
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 10px;
}

.chat-msg {
  max-width: 75%;
}

.message-item.mine .chat-msg {
  margin-left: auto;
  text-align: right;
}

.message-item.other .chat-msg {
  margin-right: auto;
  text-align: left;
}

.sender-name {
  font-size: 12px;
  color: #999;
  margin-bottom: 2px;
}

.bubble {
  display: inline-block;
  padding: 8px 12px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.4;
  word-break: break-word;
}

.message-item.mine .bubble {
  background: #1989fa;
  color: white;
  border-bottom-right-radius: 4px;
}

.message-item.other .bubble {
  background: white;
  color: #333;
  border-bottom-left-radius: 4px;
}

.msg-time {
  font-size: 11px;
  color: #bbb;
  margin-top: 2px;
}

.input-bar {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background: white;
  border-top: 1px solid #eee;
  gap: 8px;
}

.input-bar .van-field {
  flex: 1;
  border: 1px solid #eee;
  border-radius: 20px;
  padding: 4px 12px;
}

.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.status-dot.online {
  background: #07c160;
}
.status-dot.offline {
  background: #ee0a24;
}
</style>
