package org.example.model;


import java.util.UUID;

public class Category {
    public enum Type {
        INCOME, EXPENSE
    }

    private UUID id;
    private Type type;
    private String name;

    public Category(UUID id, Type type, String name) {
        this.id = id;
        this.type = type;
        this.name = name;
    }

    // Геттеры и сеттеры
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", type=" + type +
                ", name='" + name + '\'' +
                '}';
    }
}