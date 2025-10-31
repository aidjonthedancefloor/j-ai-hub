package com.aidj.aihub.rest.applications;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public abstract class AiHubApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        classes.addAll(getApplicationClasses());
        // classes.add(tools.jackson.jakarta.rs.json.JacksonJsonProvider.class); // 415 Unsupported Media Type without this

        return classes;
    }

    public abstract Set<Class<?>> getApplicationClasses();

}
