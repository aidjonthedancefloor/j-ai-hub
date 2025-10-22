package com.aidj.aihub.completion;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aidj.aihub.LlmFactory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;


public class AdHoc {

    interface Assistant {

        TokenStream chat(List<ChatMessage> messages);
    }

    public static TokenStream adHocComplete(List<String> systemPrompts, List<String> userPrompts) {
        return adHocChat(
            Stream.concat(
                systemPrompts.stream().map(p -> new SystemMessage(p)),
                userPrompts.stream().map(p -> new UserMessage(p))
            )
                .collect(Collectors.toList())
        );
    }

    public static TokenStream adHocChat(List<ChatMessage> messages) {
        StreamingChatModel model = LlmFactory.createStreamingChatModel();
        Assistant assistant = AiServices.create(Assistant.class, model);
        return assistant.chat(messages);
    }
}
