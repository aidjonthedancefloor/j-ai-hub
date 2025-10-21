package com.aidj.aihub.rest.resources;

import javax.ws.rs.Path;

import com.aidj.aihub.rest.resources.v1.Completion;
import com.aidj.aihub.rest.resources.v1.McpCompletion;

@Path(value = "/v1")
public class V1Resource {

    @Path(value = "/complete")
    public Completion completion() {
        return new Completion();
    }

    @Path(value = "/mcp-complete")
    public McpCompletion mcpCompletion() {
        return new McpCompletion();
    }

}
