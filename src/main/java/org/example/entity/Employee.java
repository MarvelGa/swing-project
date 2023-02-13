package org.example.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "Worker")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private int id;
    @Column(unique = true)
    private String name;
    @OneToMany(mappedBy = "employee", fetch = FetchType.EAGER)
    private List<Contact> contacts;

    public Employee() {
    }

    public Employee(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

}
