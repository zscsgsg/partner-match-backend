package com.zsc.partnermatch.event;

import org.springframework.context.ApplicationEvent;

/**
 * 队伍变更事件 —— 加入/退出/创建/删除队伍时发布
 * <p>
 * 用于解耦"队伍核心操作"与"画像重算、缓存失效"等后续动作
 */
public class TeamEvent extends ApplicationEvent {

    private final Long userId;
    private final Long teamId;
    private final EventType eventType;

    public TeamEvent(Object source, Long userId, Long teamId, EventType eventType) {
        super(source);
        this.userId = userId;
        this.teamId = teamId;
        this.eventType = eventType;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public enum EventType {
        /** 用户创建队伍 */
        CREATE,
        /** 用户加入队伍 */
        JOIN,
        /** 用户退出队伍（含解散） */
        QUIT,
        /** 队长删除队伍 */
        DELETE
    }
}
