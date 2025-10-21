package com.aidj.aihub.completion;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aidj.aihub.LlmFactory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.XStreamableHttpMcpTransport;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;

import java.time.Duration;
import java.util.List;

public class Mcp {
    interface Assistant {

        TokenStream chat(List<ChatMessage> messages);
    }

    public McpClient mkWeatherClient() {

        McpTransport transport = new XStreamableHttpMcpTransport.Builder()
                .url("http://127.0.0.1:8000/mcp")
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();

        McpClient mcpClient = new DefaultMcpClient.Builder()
                .transport(transport)
                .build();

        return mcpClient;
    }

    public static TokenStream mcpComplete(List<String> systemPrompts, List<String> userPrompts, List<McpClient> clients) {
        StreamingChatModel model = LlmFactory.createStreamingChatModel();

        ToolProvider toolProvider = McpToolProvider.builder()
                .mcpClients(clients)
                .build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .streamingChatModel(model)
                .toolProvider(toolProvider)
                .build();

        List<ChatMessage> chatMessages = Stream.concat(
            systemPrompts.stream().map(p -> new SystemMessage(p)),
            userPrompts.stream().map(p -> new UserMessage(p))
        ).collect(Collectors.toList());

        return assistant.chat(chatMessages);
    }
}
