package org.example.repository;

import org.example.model.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(UUID id);
    List<Category> findAll();
    List<Category> findByType(Category.Type type);
    void delete(UUID id);
    boolean exists(UUID id);
    void update(Category category);
}