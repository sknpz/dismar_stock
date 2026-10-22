package br.com.dismar.stock.shared.exception;

/** Requisição válida no formato, mas que viola uma regra de negócio (ex.: saída maior que o estoque). Vira HTTP 422. */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
