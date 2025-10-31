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
import com.aidj.aihub.rest.StreamingOutputUtil;

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
        return StreamingOutputUtil.streamingOutputFromTokenStream(responseStream);
    }
}
