package org.example.bakeryapi.category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    // "system" is a reserved keyword in MySQL; use a safe column name.
    @Column(name = "is_system", nullable = false, columnDefinition = "boolean default false")
    private boolean system = false;

    protected Category() {
        // Constructor for JPA
    }

    public Category(String name) {
        this.name = name;
    }

    public Category(String name, boolean system) {
        this.name = name;
        this.system = system;
    }

    public void update(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isSystem() {
        return system;
    }
}


