package com.aidj.aihub.mcp;

import java.util.logging.Logger;


import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Calculator {

    private static final Logger LOGGER = Logger.getLogger(Calculator.class.getName());

    public int add(int a, int b) {
        LOGGER.info("Adding " + a + " and " + b);
        return a + b;
    }

    public int subtract(int a, int b) {
        LOGGER.info("Subtracting " + a + " and " + b);
        return a - b;
    }

}
