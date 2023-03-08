package io.frebigbird.example.charon.proxy.token;

import lombok.NoArgsConstructor;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

@NoArgsConstructor
public class ObtainingTokenException extends RuntimeException {
    private String message;

    private Throwable cause;

    public ObtainingTokenException(Throwable t) {
        this.cause = t;
    }

    public ObtainingTokenException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return (cause instanceof HttpStatusCodeException || cause instanceof ResourceAccessException)
            ? cause.getMessage()
            : message;
    }
}
