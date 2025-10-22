package com.aidj.aihub.rest.resources.v1;

import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;

import javax.json.Json;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.StreamingOutput;

import com.aidj.aihub.completion.AdHoc;

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
        // TODO validation error instead
        List<ChatMessage> chatMessages = input.toChatMessages().orElseThrow(() -> new IllegalArgumentException("Invalid prompt roles"));
        TokenStream responseStream = AdHoc.adHocChat(chatMessages);

        StreamingOutput streamingOutput = outputStream -> {
            PrintWriter writer = new PrintWriter(outputStream, true);
            Semaphore sem = new Semaphore(0);

            Runnable cleanup = () -> {
                writer.close();
                sem.release();
            };

            responseStream
                .onPartialResponse(token -> {
                    writer.println(
                        Json.createObjectBuilder()
                            .add("chunk", token)
                            .build()
                            .toString()
                    );
                })
                .onCompleteResponse(x -> {
                    cleanup.run();
                })
                .onError(err -> {
                    // TODO log `err`
                    writer.println(
                        Json.createObjectBuilder()
                            .add("error", "unknown")
                            .build()
                            .toString()
                    );
                    cleanup.run();
                })
                .start();

            try {
                sem.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        return streamingOutput;
    }
}
