package io.github.sknpz.tana.category;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.sknpz.tana.shared.exception.ConflictException;
import io.github.sknpz.tana.shared.exception.ResourceNotFoundException;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> list() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Já existe uma categoria chamada '" + name + "'.");
        }
        return CategoryResponse.from(categoryRepository.save(new Category(name)));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findById(id);
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("Já existe uma categoria chamada '" + name + "'.");
        }
        category.rename(name);
        return CategoryResponse.from(category);
    }

    /** Os produtos da categoria ficam sem categoria (ON DELETE SET NULL no banco). */
    @Transactional
    public void delete(Long id) {
        categoryRepository.delete(findById(id));
    }

    /** Usado por outros módulos para resolver a categoria informada em um produto. */
    @Transactional(readOnly = true)
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", id));
    }
}
