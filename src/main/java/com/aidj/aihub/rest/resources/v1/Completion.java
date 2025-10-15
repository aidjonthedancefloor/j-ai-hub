package com.aidj.aihub.rest.resources.v1;

import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

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
    {
        response: <string>
    }
*/

public class Completion {
    @Data
    @NoArgsConstructor
    public static class CompletionInput {
        private List<String> systemPrompts;
        private List<String> userPrompts;
    }

    @Data
    @Builder
    public static class CompletionOutput {
        private String response;
    }

    @POST
    @Consumes("application/json")
    @Produces("application/x-ndjson") // newline delimited json
    public Response complete(CompletionInput input) {
        TokenStream responseStream = AdHoc.adHocComplete(input.getSystemPrompts(), input.getUserPrompts());

        StreamingOutput streamingOutput = output -> {
            PrintWriter writer = new PrintWriter(output, true);
            Semaphore sem = new Semaphore(0);

            Runnable cleanup = () -> {
                writer.close();
                sem.release();
            };

            responseStream
                .onPartialResponse(token -> {
                    writer.println("{\"response\":\"" + token.replace("\n", "\\n").replace("\"", "\\\"") + "\"}");
                })
                .onCompleteResponse(x -> {
                    cleanup.run();
                })
                .onError(err -> {
                    writer.println("{\"error\":\"" + (err == null ? "unknown" : err.getMessage()) + "\"}");
                    cleanup.run();
                })
                .start();

            try {
                sem.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };


        return Response.ok(streamingOutput).header("Content-Type", "application/x-ndjson").build();
        // return streamingOutput;
    }
}
