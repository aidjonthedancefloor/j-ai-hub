package com.aidj.aihub.mcp;

import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.json.McpJsonMapper;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletRegistration.Dynamic;

public class McpServerInitializer implements ServletContextListener {

    private MagicCalculatorAsyncTools magicCalculatorAsyncTools = new MagicCalculatorAsyncTools();

    private McpAsyncServer server;
    private HttpServletStreamableServerTransportProvider transport;

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        ServletContext ctx = sce.getServletContext();

        transport = HttpServletStreamableServerTransportProvider.builder()
            .jsonMapper(McpJsonMapper.getDefault())
            .mcpEndpoint("/mcp")
            .build();

        Dynamic registration = ctx.addServlet("mcp-streamable", transport);
        registration.addMapping("/mcp");
        registration.setAsyncSupported(true);
        registration.setLoadOnStartup(1);

        server = McpServer.async(transport)
            .serverInfo("mcp-magic-calculator-server", "1.0.0")
            .capabilities(ServerCapabilities.builder()
                .tools(true)
                .logging()
                .build())
            .tools(
                magicCalculatorAsyncTools.addition(),
                magicCalculatorAsyncTools.subtraction()
            )
            .build();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            if (server != null) {
                try {
                    server.closeGracefully();
                } catch (Throwable t) {
                    server.close();
                }
            }
            if (transport != null) {
                try {
                    transport.closeGracefully().block();
                } catch (Throwable ignore) {
                }
            }
        } catch (Throwable ignore) {
        }
    }

}
