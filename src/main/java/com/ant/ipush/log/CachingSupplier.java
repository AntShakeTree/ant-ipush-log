package com.ant.ipush.log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class CachingSupplier<T> implements Supplier<T>, Serializable {

    private final AtomicReference<T> value = new AtomicReference<>();
    private transient final Supplier<T> delegate;

    /**
     * Factory method for a {@link CachingSupplier} that will supply the given {@code value}.
     * <p>
     * This factory method should be used when the value is already available. Used this way this supplier can be
     * serialized.
     *
     * @param value the value to supply
     * @param <T>   the type of results supplied by this supplier
     * @return a {@link CachingSupplier} that supplies the given value
     */
    public static <T> CachingSupplier<T> of(T value) {
        return new CachingSupplier<>(value);
    }

    /**
     * Factory method for a {@link CachingSupplier} that delegates to the given {@code supplier} when asked to supply a
     * value. If the given {@code supplier} is a {@link CachingSupplier} the instance is returned as is, if not a new
     * {@link CachingSupplier} instance is created.
     *
     * @param supplier supplier for which to cache the result
     * @param <T>      the type of results supplied by this supplier
     * @return a {@link CachingSupplier} based on given {@code supplier}
     */
    public static <T> CachingSupplier<T> of(Supplier<T> supplier) {
        if (supplier instanceof CachingSupplier) {
            return (CachingSupplier<T>) supplier;
        }
        return new CachingSupplier<>(supplier);
    }

    private CachingSupplier(Supplier<T> delegate) {
        this.delegate = delegate;
    }

    private CachingSupplier(T value) {
        this.value.set(value);
        delegate = () -> value;
    }

    @Override
    public T get() {
        T result = value.get();
        if (result == null) {
            result = value.updateAndGet(v -> v == null ? delegate.get() : v);
        }
        return result;
    }

    /**
     * Java Serialization API Method that ensures that an instance of this class can be serialized by first invoking
     * {@link #get()}.
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        get();
        stream.defaultWriteObject();
    }

}