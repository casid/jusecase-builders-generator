package app;

import java.lang.reflect.ParameterizedType;
import java.util.function.Consumer;

/**
 * Base class of the sample builders: holds the entity, so builders only declare the entity type.
 */
public class Builder<T, B extends Builder> implements org.jusecase.builders.Builder<T> {
    protected B builder;
    protected T entity;

    @SuppressWarnings("unchecked")
    public Builder() {
        builder = (B) this;
        entity = newEntity();
    }

    @Override
    public T build() {
        return entity;
    }

    /** Used by the generated builder methods. */
    public T getEntity() {
        return entity;
    }

    public B reset() {
        entity = newEntity();
        return builder;
    }

    public B with(Consumer<T> consumer) {
        consumer.accept(entity);
        return builder;
    }

    @SuppressWarnings("unchecked")
    protected T newEntity() {
        try {
            ParameterizedType superclass = (ParameterizedType) getClass().getGenericSuperclass();
            return ((Class<T>) superclass.getActualTypeArguments()[0]).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create new entity, consider overriding newEntity() for this builder.", e);
        }
    }
}
