package com.example.camel.component;

import com.example.camel.entiry.Person;
import com.example.camel.repository.PersonRepository;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import org.springframework.stereotype.Component;

import java.net.ConnectException;

@Component
public class PersonRouts extends RouteBuilder {

    @Autowired
    private PersonRepository personRepository;

    @Override
    public void configure() throws Exception {

        onException(JmsException.class, ConnectException.class)
                .routeId("jmsExceptionRoutId")
                .handled(true)
                .log(LoggingLevel.INFO, "JMS Exception occurred");

        restConfiguration()
                .component("jetty")
                .host("0.0.0.0")
                .port(8081)
                .bindingMode(RestBindingMode.json)
                .enableCORS(true);

        rest("/api")
                .post("/persons").type(Person.class).to("direct:createPerson")
                .get("/persons/{id}").outType(Person.class).to("direct:getPersonById")
                .put("/persons/{id}").type(Person.class).to("direct:updatePerson")
                .delete("/persons/{id}").to("direct:deletePerson");

        from("seda:toDB")
                .log("Received in toDB request: ${body}")
                .to("jpa:"+Person.class.getName());

        from("seda:activeMQ")
                .log("Received in activeMQ request: ${body}")
                .to("activemq:queue:person?exchangePattern=InOnly");

        from("direct:createPerson")
                .log("Creating person: ${body}")
                .log("Sending inDB person: ${body}")
                .bean(InboundRestProcessor.class, "process")
                .choice()
                    .when(simple("${header.city} == 'Noida'"))
                        .log(LoggingLevel.INFO, "Only DB")
                        .to("seda:toDB")
                    .otherwise()
                        .log(LoggingLevel.INFO, "Both DB and MQ")
                        .to("seda:activeMQ")
                        .to("seda:toDB")
                .end()
               // .wireTap("seda:toDB")
              //  .log("Sending activeMQ person: ${body}")
              //  .wireTap("seda:activeMQ")
              //  .log("Person created successfully")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .transform().simple("Person created successfully: ${body}")
                .end();

        from("direct:getPersonById")
                .log("Fetching person with ID: ${header.id}")
                .toD("jpa:com.example.camel.entities.Person?query=select p from Person p where p.id = ${header.id}")
                .log("Person fetched successfully: ${body}");

        from("direct:updatePerson")
                .log("Updating person with ID: ${header.id}")
                .process(exchange -> {
                    Long id = exchange.getIn().getHeader("id", Long.class);
                    Person updatedPerson = exchange.getIn().getBody(Person.class);
                    updatedPerson.setId(id); // Ensure ID is set for update
                    exchange.getIn().setBody(updatedPerson);
                })
                .to("jpa:com.example.camel.entities.Person");

        from("direct:deletePerson")
                .log("Deleting person with ID: ${header.id}")
                .process(exchange -> {
                    Long id = exchange.getIn().getHeader("id", Long.class);
                    exchange.getIn().setBody(id);
                })
                .toD("jpa:com.example.camel.entities.Person?nativeQuery=DELETE FROM Person WHERE id = ${header.id}")
                .log("Person deleted successfully");
    }
}
