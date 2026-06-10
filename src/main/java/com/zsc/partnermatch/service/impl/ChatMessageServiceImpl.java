package com.zsc.partnermatch.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsc.partnermatch.entity.ChatMessage;
import com.zsc.partnermatch.mapper.ChatMessageMapper;
import com.zsc.partnermatch.service.IChatMessageService;
import org.springframework.stereotype.Service;

/**
 * 聊天消息服务实现
 */
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IChatMessageService {
}
