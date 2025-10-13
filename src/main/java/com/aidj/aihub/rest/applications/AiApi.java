package com.aidj.aihub.rest.applications;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/ai-api")
public class AiApi extends AiHubApplication {

    @Override
    public Set<Class<?>> getApplicationClasses() {
        Set<Class<?>> classes = new HashSet<>();

        classes.add(com.aidj.aihub.rest.resources.HelloResource.class);
        classes.add(com.aidj.aihub.rest.resources.V1Resource.class);

        return classes;
    }

}
