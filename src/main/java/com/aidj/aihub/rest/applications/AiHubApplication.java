package com.aidj.aihub.rest.applications;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public abstract class AiHubApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        classes.addAll(getApplicationClasses());

        return classes;
    }

    public abstract Set<Class<?>> getApplicationClasses();

}
