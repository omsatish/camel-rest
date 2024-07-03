package com.example.camel.repository;

import com.example.camel.entiry.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    // Additional query methods if needed
}
