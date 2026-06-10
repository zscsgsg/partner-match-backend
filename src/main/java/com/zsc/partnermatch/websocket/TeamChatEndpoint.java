package com.zsc.partnermatch.websocket;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zsc.partnermatch.entity.ChatMessage;
import com.zsc.partnermatch.service.IChatMessageService;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 队伍聊天室 WebSocket 端点
 *
 * 路径格式: /ws/team/{teamId}/{userId}
 * - teamId: 队伍 ID（聊天房间）
 * - userId: 用户 ID
 *
 * 核心设计：
 * - 房间管理: Map<teamId, Set<Session>> 维护每个队伍在线成员
 * - 消息持久化: 每条消息保存到 MySQL，支持历史消息查询
 * - 广播机制: 收到消息后广播给同队伍所有在线成员
 */
@ServerEndpoint("/ws/team/{teamId}/{userId}/{userName}")
@Component
@Slf4j
public class TeamChatEndpoint {

    /**
     * 房间管理: teamId -> 该队伍内所有在线用户的 Session
     */
    private static final Map<Long, Set<Session>> ROOM_SESSIONS = new ConcurrentHashMap<>();

    /**
     * Session -> 对应的 teamId（用于 onClose 时清理）
     */
    private static final Map<Session, Long> SESSION_ROOM = new ConcurrentHashMap<>();

    /**
     * 在线连接数统计
     */
    private static volatile int onlineCount = 0;

    /**
     * 由于 @ServerEndpoint 每次连接都 new 一个实例，
     * 需要通过 static 方式注入 Spring Bean
     */
    private static IChatMessageService chatMessageService;

    @Autowired
    public void setChatMessageService(IChatMessageService chatMessageService) {
        TeamChatEndpoint.chatMessageService = chatMessageService;
    }

    /**
     * 连接建立成功
     */
    @OnOpen
    public void onOpen(Session session,
                       @PathParam("teamId") Long teamId,
                       @PathParam("userId") Long userId,
                       @PathParam("userName") String userName) {
        // 加入房间
        ROOM_SESSIONS.computeIfAbsent(teamId, k -> new CopyOnWriteArraySet<>()).add(session);
        SESSION_ROOM.put(session, teamId);
        onlineCount++;

        log.info("[WebSocket] 用户连接 teamId={} userId={} userName={} 当前在线={}",
                teamId, userId, userName, onlineCount);

        // 发送加入成功消息
        JSONObject msg = new JSONObject();
        msg.set("type", "system");
        msg.set("content", userName + " 加入了聊天室");
        msg.set("createTime", LocalDateTime.now().toString());
        sendMessage(session, msg.toString());
    }

    /**
     * 收到消息
     */
    @OnMessage
    public void onMessage(String message, Session session,
                          @PathParam("teamId") Long teamId,
                          @PathParam("userId") Long userId,
                          @PathParam("userName") String userName) {
        log.info("[WebSocket] 收到消息 teamId={} userId={} content={}", teamId, userId, message);

        // 1. 持久化到数据库
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setTeamId(teamId);
        chatMessage.setSenderId(userId);
        chatMessage.setSenderName(userName);
        chatMessage.setContent(message);
        chatMessage.setCreateTime(LocalDateTime.now());
        try {
            if (chatMessageService != null) {
                chatMessageService.save(chatMessage);
            }
        } catch (Exception e) {
            log.error("[WebSocket] 消息持久化失败", e);
        }

        // 2. 构造广播消息
        JSONObject broadcastMsg = new JSONObject();
        broadcastMsg.set("type", "chat");
        broadcastMsg.set("senderId", userId);
        broadcastMsg.set("senderName", userName);
        broadcastMsg.set("content", message);
        broadcastMsg.set("createTime", chatMessage.getCreateTime().toString());

        // 3. 广播给房间内所有成员
        String msgStr = broadcastMsg.toString();
        Set<Session> roomSessions = ROOM_SESSIONS.get(teamId);
        if (roomSessions != null) {
            for (Session s : roomSessions) {
                sendMessage(s, msgStr);
            }
        }
    }

    /**
     * 连接关闭
     */
    @OnClose
    public void onClose(Session session,
                        @PathParam("teamId") Long teamId,
                        @PathParam("userId") Long userId,
                        @PathParam("userName") String userName) {
        // 从房间中移除
        Set<Session> roomSessions = ROOM_SESSIONS.get(teamId);
        if (roomSessions != null) {
            roomSessions.remove(session);
            if (roomSessions.isEmpty()) {
                ROOM_SESSIONS.remove(teamId);
            }
        }
        SESSION_ROOM.remove(session);
        onlineCount--;

        log.info("[WebSocket] 用户断开 teamId={} userId={} userName={} 当前在线={}",
                teamId, userId, userName, onlineCount);

        // 广播退出消息
        JSONObject msg = new JSONObject();
        msg.set("type", "system");
        msg.set("content", userName + " 离开了聊天室");
        msg.set("createTime", LocalDateTime.now().toString());

        if (roomSessions != null) {
            String msgStr = msg.toString();
            for (Session s : roomSessions) {
                sendMessage(s, msgStr);
            }
        }
    }

    /**
     * 发生错误
     */
    @OnError
    public void onError(Session session, Throwable error,
                        @PathParam("teamId") Long teamId,
                        @PathParam("userId") Long userId) {
        log.error("[WebSocket] 连接异常 teamId={} userId={}", teamId, userId, error);
    }

    /**
     * 发送消息（异步，避免阻塞）
     */
    private void sendMessage(Session session, String message) {
        try {
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendText(message);
            }
        } catch (IOException e) {
            log.error("[WebSocket] 发送消息失败", e);
        }
    }

    /**
     * 获取当前在线连接数
     */
    public static int getOnlineCount() {
        return onlineCount;
    }
}
