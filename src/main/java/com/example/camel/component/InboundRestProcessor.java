package com.example.camel.component;

import com.example.camel.entiry.Person;
import org.apache.camel.Exchange;

public class InboundRestProcessor {

    public void process(Exchange exchange) throws Exception {
        Person person = exchange.getIn().getBody(Person.class);
        reProcess(exchange, person);
    }

    public void reProcess(Exchange exchange, Person person) throws Exception {
        exchange.getIn().setHeader("city", person.getCity());
    }
}
