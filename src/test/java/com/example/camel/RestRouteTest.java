package com.example.camel;

import com.example.camel.entiry.Person;
import com.example.camel.repository.PersonRepository;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;

import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@CamelSpringBootTest
@SpringBootTest
@MockEndpoints
public class RestRouteTest {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private PersonRepository personRepository;

    @BeforeEach
    public void setUp() {
        // Clear the database before each test
        personRepository.deleteAll();
    }

    @Test
    public void testCreatePersonRoute() {
        Person person = new Person();
        person.setName("John Doe");
        person.setCity("New York");

        Person createdPerson = (Person) producerTemplate.requestBody("direct:createPerson", person);

        assertNotNull(createdPerson);
        assertEquals("John Doe", createdPerson.getName());
        assertEquals("New York", createdPerson.getCity());
    }

    @Test
    public void testGetPersonByIdRoute() {
        Person person = new Person();
        person.setName("Jane Doe");
        person.setCity("Los Angeles");
        person = personRepository.save(person);

        Person fetchedPerson = (Person) producerTemplate.requestBodyAndHeader("direct:getPersonById", null, "id", person.getId());

        assertNotNull(fetchedPerson);
        assertEquals("Jane Doe", fetchedPerson.getName());
        assertEquals("Los Angeles", fetchedPerson.getCity());
    }

    @Test
    public void testUpdatePersonRoute() {
        Person person = new Person();
        person.setName("John Doe");
        person.setCity("New York");
        person = personRepository.save(person);

        Person updatedPersonData = new Person();
        updatedPersonData.setName("Johnny Doe");
        updatedPersonData.setCity("Boston");

        Person updatedPerson = (Person) producerTemplate.requestBodyAndHeader("direct:updatePerson", updatedPersonData, "id", person.getId());

        assertNotNull(updatedPerson);
        assertEquals("Johnny Doe", updatedPerson.getName());
        assertEquals("Boston", updatedPerson.getCity());
    }

    @Test
    public void testDeletePersonRoute() {
        Person person = new Person();
        person.setName("Jane Doe");
        person.setCity("Los Angeles");
        person = personRepository.save(person);

        String response = (String) producerTemplate.requestBodyAndHeader("direct:deletePerson", null, "id", person.getId());

        assertEquals("Person with ID " + person.getId() + " deleted successfully.", response);
        assertFalse(personRepository.findById(person.getId()).isPresent());
    }
}

