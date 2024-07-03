package com.example.camel;

import com.example.camel.entiry.Person;
import com.example.camel.repository.PersonRepository;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RestRoute extends RouteBuilder {

    @Autowired
    private PersonRepository personRepository;

    @Override
    public void configure() throws Exception {
        restConfiguration()
                .component("jetty")
                .host("0.0.0.0")
                .port(8081)
                .bindingMode(RestBindingMode.json)
                .enableCORS(true);

        rest("/api")
                .post("/persons").type(Person.class).to("direct:createPerson")
                .get("/persons/{id}").outType(Person.class).to("direct:getPersonById");
               // .put("/persons/{id}").type(Person.class).to("direct:updatePerson")
               // .delete("/persons/{id}").to("direct:deletePerson");

        from("direct:createPerson")
                .log("Creating person: ${body}")
                .to("jpa:com.example.camel.entities.Person")
                .log("Person created successfully");


        from("direct:createPersonJPA")
                .log("Creating person: ${body}")
                .process(exchange -> {
                    Person person = exchange.getIn().getBody(Person.class);
                    Person savedPerson = personRepository.save(person);
                    exchange.getIn().setBody(savedPerson);
                });

        from("direct:getPersonById")
                .log("Fetching person with ID: ${header.id}")
                .toD("jpa:com.example.camel.entities.Person?query=select p from Person p where p.id = ${header.id}")
                .log("Person fetched successfully: ${body}");

        from("direct:getPersonByIdJPA")
                .log("Fetching person with ID: ${header.id}")
                .process(exchange -> {
                    Long id = exchange.getIn().getHeader("id", Long.class);
                    Person person = personRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("Person not found"));
                    exchange.getIn().setBody(person);
                });

        from("direct:updatePerson")
                .log("Updating person with ID: ${header.id}")
                .process(exchange -> {
                    Long id = exchange.getIn().getHeader("id", Long.class);
                    Person updatedPerson = exchange.getIn().getBody(Person.class);
                    updatedPerson.setId(id); // Ensure ID is set for update
                    exchange.getIn().setBody(updatedPerson);
                })
                .to("jpa:com.example.camel.entities.Person");


        from("direct:updatePersonJPA")
                .log("Updating person with ID: ${header.id}")
                .process(exchange -> {
                    Long id = exchange.getIn().getHeader("id", Long.class);
                    Person updatedPerson = exchange.getIn().getBody(Person.class);
                    Person existingPerson = personRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("Person not found"));
                    existingPerson.setName(updatedPerson.getName());
                    existingPerson.setCity(updatedPerson.getCity());
                    Person savedPerson = personRepository.save(existingPerson);
                    exchange.getIn().setBody(savedPerson);
                });

        from("direct:deletePerson")
                .log("Deleting person with ID: ${header.id}")
                .process(exchange -> {
                    Long id = exchange.getIn().getHeader("id", Long.class);
                    exchange.getIn().setBody(id);
                })
                .toD("jpa:com.example.camel.entities.Person?nativeQuery=DELETE FROM Person WHERE id = ${header.id}")
                .log("Person deleted successfully");

        from("direct:deletePersonJPA")
                .log("Deleting person with ID: ${header.id}")
                .process(exchange -> {
                    Long id = exchange.getIn().getHeader("id", Long.class);
                    personRepository.deleteById(id);
                    exchange.getIn().setBody("Person with ID " + id + " deleted successfully.");
                });
    }
}
