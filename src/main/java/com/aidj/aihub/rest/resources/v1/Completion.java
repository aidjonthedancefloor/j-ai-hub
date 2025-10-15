package com.aidj.aihub.rest.resources.v1;

import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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

            // queue for tokens produced by worker threads
            final BlockingQueue<String> queue = new LinkedBlockingQueue<>(1024);
            final CountDownLatch done = new CountDownLatch(1);

            // register callbacks: producers only enqueue and signal completion
            responseStream
                .onPartialResponse(token -> {
                    boolean offered = queue.offer(token);
                    if (!offered) {
                        // queue full; drop token or log
                        System.err.println("Dropping token because queue is full");
                    }
                })
                .onCompleteResponse(x -> {
                    done.countDown();
                })
                .onError(err -> {
                    try {
                        queue.offer("{\"error\":\"" + (err == null ? "unknown" : err.getMessage()) + "\"}");
                    } catch (Exception e) {
                        // ignore
                    } finally {
                        done.countDown();
                    }
                })
                .start();

            // consumer: request thread drains the queue and performs all IO against the servlet response
            try {
                while (true) {
                    String s = queue.poll(250, TimeUnit.MILLISECONDS);
                    if (s != null) {
                        writer.println(s);
                        writer.flush();
                    } else {
                        if (done.getCount() == 0 && queue.isEmpty()) break;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                try {
                    writer.close();
                } catch (Exception ignored) {}
            }
        };


        return Response.ok(streamingOutput).header("Content-Type", "application/x-ndjson").build();
        // return streamingOutput;
    }
}
