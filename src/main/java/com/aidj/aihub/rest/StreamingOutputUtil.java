package com.aidj.aihub.rest;

import java.io.PrintWriter;
import java.util.concurrent.Semaphore;

import javax.json.Json;
import javax.ws.rs.core.StreamingOutput;

import dev.langchain4j.service.TokenStream;

/*
 * Converts a `TokenStream` into a JAX-RS `StreamingOutput` that streams
 * newline-delimited JSON objects representing either chunks of text or errors.
 */
public class StreamingOutputUtil {
    public static StreamingOutput streamingOutputFromTokenStream(TokenStream tokenStream) {
        return streamingOutputFromTokenStreamWithCallback(tokenStream, () -> {});
    }

    public static StreamingOutput streamingOutputFromTokenStreamWithCallback(TokenStream tokenStream, Runnable done) {
        StreamingOutput streamingOutput = outputStream -> {
            PrintWriter writer = new PrintWriter(outputStream, true);
            Semaphore sem = new Semaphore(0);

            Runnable cleanup = () -> {
                writer.close();
                sem.release();
                done.run();
            };

            tokenStream
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
