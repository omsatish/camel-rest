package com.example.camel.component;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class PersonQueueReader extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("activemq:queue:person")
                .routeId("person-queue-reader")
                .log(LoggingLevel.INFO, ">>>>>Message received from activemq queue : ${body}");
    }
}
