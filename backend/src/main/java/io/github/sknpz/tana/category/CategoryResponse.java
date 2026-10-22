package io.github.sknpz.tana.category;

public record CategoryResponse(Long id, String name) {

    static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}
