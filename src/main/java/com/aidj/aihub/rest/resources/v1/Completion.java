package com.aidj.aihub.rest.resources.v1;

import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.aidj.aihub.completion.AdHoc;

import dev.langchain4j.service.TokenStream;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
    POST /complete

    input:
    {
        systemPrompts: [<string>, ...],
        userPrompts: [<string>, ...],
    }

    output:
    (newline delimited json:)
    | { "chunk": <string> }
    | { "error": <string> }
*/

public class Completion {
    @Data
    @NoArgsConstructor
    public static class CompletionInput {
        private List<String> systemPrompts;
        private List<String> userPrompts;
    }

    @POST
    @Consumes("application/json")
    @Produces("application/x-ndjson") // newline delimited json
    public StreamingOutput complete(CompletionInput input) {
        TokenStream responseStream = AdHoc.adHocComplete(input.getSystemPrompts(), input.getUserPrompts());

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
