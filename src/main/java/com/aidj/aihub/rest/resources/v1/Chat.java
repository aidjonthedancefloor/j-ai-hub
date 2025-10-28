package com.aidj.aihub.rest.resources.v1;

import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;

import jakarta.json.Json;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.StreamingOutput;

import com.aidj.aihub.completion.AdHoc;
import com.aidj.aihub.rest.StreamingOutputUtil;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.service.TokenStream;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
    POST /chat

    input:
    {
        prompts: [{role: "user" | "system" | "assistant", content: <string>}]
    }

    output:
    (newline delimited json:)
    | { "chunk": <string> }
    | { "error": <string> }
*/
public class Chat {
    @Data
    @NoArgsConstructor
    public static class Prompt {
        private String role;
        private String content;

        public Optional<ChatMessage> toChatMessage() {
            return switch (role) {
                case "user" -> Optional.of(new UserMessage(content));
                case "system"-> Optional.of(new SystemMessage(content));
                case "assistant"-> Optional.of(new AiMessage(content));
                default -> Optional.empty();
            };
        }
    }

    @Data
    @NoArgsConstructor
    public static class ChatInput {
        private List<Prompt> prompts;

        public Optional<List<ChatMessage>> toChatMessages() {
            List<ChatMessage> chatMessages = prompts.stream()
                .map(Prompt::toChatMessage)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
            if (chatMessages.size() != prompts.size()) {
                return Optional.empty();
            }
            return Optional.of(chatMessages);
        }
    }

    @POST
    @Consumes("application/json")
    @Produces("application/x-ndjson") // newline delimited json
    public StreamingOutput chat(ChatInput input) {
        List<ChatMessage> chatMessages = input.toChatMessages().orElseThrow(() -> new IllegalArgumentException("Invalid prompt roles")); // TODO validation error instead

        TokenStream responseStream = AdHoc.adHocChat(chatMessages);
        return StreamingOutputUtil.streamingOutputFromTokenStream(responseStream);
    }
}
