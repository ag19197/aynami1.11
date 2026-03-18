package com.gs.ayanami.controller;

import com.gs.ayanami.client.DifyClient;
import com.gs.ayanami.model.ChatRequestWrapper;
import com.gs.ayanami.model.ChatResponse;
import com.gs.ayanami.service.ChatService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;


@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequestWrapper request) {
        return chatService.chat(request.getQuery(), request.getUser(), request.getConversationId());
    }
}
