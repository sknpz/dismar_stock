package br.com.dismar.stock.shared.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " com id " + id + " não existe.");
    }
}
