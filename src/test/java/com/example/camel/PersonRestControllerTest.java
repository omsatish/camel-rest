package com.example.camel;

import com.example.camel.entiry.Person;
import com.example.camel.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PersonRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PersonRepository personRepository;

    @BeforeEach
    public void setUp() {
        // Clear the database before each test
        personRepository.deleteAll();
    }

    @Test
    public void testCreatePerson() throws Exception {
        String personJson = "{\"name\":\"John Doe\",\"city\":\"New York\"}";

        ResultActions perform = mockMvc.perform(post("/api/persons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(personJson));
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.city").value("New York"));
    }

    @Test
    public void testGetPersonById() throws Exception {
        Person person = new Person();
        person.setName("Jane Doe");
        person.setCity("Los Angeles");
        person = personRepository.save(person);

        mockMvc.perform(get("/api/persons/{id}", person.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.city").value("Los Angeles"));
    }

    @Test
    public void testUpdatePerson() throws Exception {
        Person person = new Person();
        person.setName("John Doe");
        person.setCity("New York");
        person = personRepository.save(person);

        String updatedPersonJson = "{\"name\":\"Johnny Doe\",\"city\":\"Boston\"}";

        mockMvc.perform(put("/api/persons/{id}", person.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedPersonJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Johnny Doe"))
                .andExpect(jsonPath("$.city").value("Boston"));
    }

    @Test
    public void testDeletePerson() throws Exception {
        Person person = new Person();
        person.setName("Jane Doe");
        person.setCity("Los Angeles");
        person = personRepository.save(person);

        mockMvc.perform(delete("/api/persons/{id}", person.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("Person with ID " + person.getId() + " deleted successfully."));
    }
}
