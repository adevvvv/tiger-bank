package org.example.repository;


import com.google.inject.Singleton;
import org.example.model.Category;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
public class InMemoryCategoryRepository implements CategoryRepository {

    private final Map<UUID, Category> categories = new ConcurrentHashMap<>();

    @Override
    public Category save(Category category) {
        categories.put(category.getId(), category);
        return category;
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return Optional.ofNullable(categories.get(id));
    }

    @Override
    public List<Category> findAll() {
        return new ArrayList<>(categories.values());
    }

    @Override
    public List<Category> findByType(Category.Type type) {
        return categories.values().stream()
                .filter(category -> category.getType() == type)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        categories.remove(id);
    }

    @Override
    public boolean exists(UUID id) {
        return categories.containsKey(id);
    }

    @Override
    public void update(Category category) {
        if (exists(category.getId())) {
            categories.put(category.getId(), category);
        } else {
            throw new IllegalArgumentException("Category not found with id: " + category.getId());
        }
    }
}