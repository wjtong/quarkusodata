package com.banfftech.service;

import jakarta.enterprise.context.ApplicationScoped;
import com.banfftech.model.Person;

// the class that implements Person entity read and write operations
@ApplicationScoped
public class PersonService {
    // create a new Person entity
    public Person create(Person person) {
        person.persist();
        return person;
    }
    // update an existing Person entity
    public Person update(Person person) {
        person.persist();
        return person;
    }
    // delete an existing Person entity
    public void delete(Person person) {
        person.delete();
    }
    // find a Person entity by its primary key
    public Person findById(String id) {
        return Person.findById(id);
    }
    // find all Person entities
    public Iterable<Person> findAll() {
        return Person.listAll();
    }
}
