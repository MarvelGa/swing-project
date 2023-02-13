package org.example.entity;

import javax.persistence.*;

@Entity
@Table(name = "CONTACT")
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contact_id")
    private long id;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    public Contact() {
    }

    public Contact(String phoneNumber, Employee employee) {
        this.phoneNumber = phoneNumber;
        this.employee = employee;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

}
