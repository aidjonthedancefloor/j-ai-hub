package com.aidj.aihub.mcp;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult.Builder;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import javax.enterprise.context.ApplicationScoped;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@ApplicationScoped
public class MagicCalculatorAsyncTools {

    private Calculator calculator = new Calculator();

    public AsyncToolSpecification addition() {
        return mkBinumericTool(
            "calculator-addition",
            "Calculator Addition",
            "Uses the magic calculator to add two numbers.",
            calculator::add
        );
    }

    public AsyncToolSpecification subtraction() {
        return mkBinumericTool(
            "calculator-subtraction",
            "Calculator Subtraction",
            "Uses the magic calculator to subtract two numbers.",
            calculator::subtract
        );
    }

    // tool that takes in "a: number" and "b: number"
    private AsyncToolSpecification mkBinumericTool(String name, String title, String description, BiFunction<Integer, Integer, Integer> f) {
            JsonSchema inputJsonSchema =
            new JsonSchema(
                "object",
                Map.of(
                    "a", Map.of("type", "number"),
                    "b", Map.of("type", "number")
                ),
                List.of("a", "b"),
                null,
                null,
                null
            );

        Tool tool = Tool.builder()
            .name(name)
            .title(title)
            .description(description)
            .inputSchema(inputJsonSchema)
            .build();

        return AsyncToolSpecification.builder()
            .tool(tool)
            .callHandler((McpAsyncServerExchange exchange, CallToolRequest request) ->
                Mono.fromCallable(() -> {
                    Integer a = ((Integer) request.arguments().get("a")).intValue();
                    Integer b = ((Integer) request.arguments().get("b")).intValue();
                    int res = f.apply(a, b);

                    Builder resultBuilder = CallToolResult.builder().isError(false);
                    resultBuilder.structuredContent(Map.of("result =", res));
                    // resultBuilder.addTextContent("Found " + output.size() + " results.");
                    return resultBuilder.build();
                }).onErrorResume(error ->
                    Mono.just(
                        CallToolResult.builder()
                            .addTextContent("Error encountered w/ tool " + name + " :" + error.getMessage())
                            .isError(true)
                            .build()
                    )
                )
            )
            .build();
    }
}
