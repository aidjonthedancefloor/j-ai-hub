package com.aidj.aihub.rest.resources.v1;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;

import com.aidj.aihub.completion.AdHoc;

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
    @Produces("application/json")
    @Consumes("application/json")
    public CompletionOutput complete(CompletionInput input) {
        return CompletionOutput.builder()
            .response(AdHoc.adHocComplete(input.getSystemPrompts(), input.getUserPrompts()))
            .build();
    }
}
