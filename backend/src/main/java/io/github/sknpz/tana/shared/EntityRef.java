package io.github.sknpz.tana.shared;

/** Referência resumida a outra entidade dentro de uma resposta (ex.: o fornecedor de um produto). */
public record EntityRef(Long id, String name) {
}
