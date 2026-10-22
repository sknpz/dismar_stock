package br.com.dismar.stock.shared.exception;

/** Operação conflita com o estado atual dos dados (ex.: SKU duplicado). Vira HTTP 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
