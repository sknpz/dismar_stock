package io.github.sknpz.tana.shared.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

/** Valida um CNPJ (com ou sem pontuação). Valores nulos ou vazios são aceitos: o campo é opcional. */
@Documented
@Constraint(validatedBy = ValidCnpj.Validator.class)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCnpj {

    String message() default "CNPJ inválido.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<ValidCnpj, String> {

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return Cnpj.normalize(value) == null || Cnpj.isValid(value);
        }
    }
}
