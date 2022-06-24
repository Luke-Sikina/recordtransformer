package com.sikina.recordtransformer;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A wrapper that provides an API for making transformations to an immutable record object.
 *
 * Note: the record you are wrapping must be accessible (public).
 * @param <T> the type of the record being wrapped.
 */
public class RecordTransformer<T extends Record> {
    private T rec;
    private final Map<String, Function<T, Object>> getters;
    private final Map<String, Object> updates = new HashMap<>();

    /**
     * Call this constructor once, at the beginning of your record's lifecycle. This constructor is a bit
     * expensive, so avoid duplicate wrapping if you can.
     *
     * @param rec the record to transform
     * @throws GetterException thrown if the wrapper can't get record components from rec
     */
    public RecordTransformer(T rec) throws GetterException {
        this.rec = rec;
        // create a list of getters that accept a T and produce the relevant field value
        // these are used to get old values in transform
        getters = Arrays.stream(rec.getClass().getRecordComponents())
            .map(RecordComponent::getAccessor)
            .collect(Collectors.toMap(
                Method::getName,
                this::createInvokableGetter
            ));
    }

    private Function<T, Object> createInvokableGetter(Method m) {
        return(T referenceRecord) -> {
            try {
                return m.invoke(referenceRecord);
            } catch (ReflectiveOperationException e) {
                throw new GetterException(e);
            }
        };
    }

    /**
     * @return Returns the current record.
     */
    public T rec() {
        return rec;
    }

    /**
     * Lazily changes the field referenced by the getter to the new value.
     * This value change will not be reflected in rec() until you call transform().
     *
     * This process happens in two steps - @{code with(getter).as(newValue)} in order to make type
     * checking work. If you call @{code with} without calling @{code as} afterwards, nothing will happen.
     *
     * @param getter A getter on record T - used to enforce type checking and reference the field being changed
     * @param <V> The type of the field being changed
     * @return PartialTransformation curried with this lens and the key from this getter.
     * @throws GetterException if the getter cannot be transformed into a SerializableLambda
     */
    public <V> PartialTransformation<T, V> with(Accessor<V> getter) throws GetterException {
        try {
            // From https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/invoke/LambdaMetafactory.html
            // When FLAG_SERIALIZABLE is set in flags, the function objects will implement Serializable,
            // and will have a writeReplace method that returns an appropriate SerializedLambda.
            Method m = getter.getClass().getDeclaredMethod("writeReplace");
            m.setAccessible(true);
            SerializedLambda replacement = (SerializedLambda) m.invoke(getter);
            return new PartialTransformation<>(this, updates::put, replacement.getImplMethodName());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new GetterException(e);
        }
    }

    /**
     * Lazily changes the field referenced by the key to the new value.
     * This change will not be reflected in rec() until you call transform().
     *
     * This is not type safe. If you try to add a value with a type that cannot be matched to
     * the type of the corresponding field, things will explode when you call transform.
     * The upside of this method compared to with is speed - this doesn't have to do any reflection
     * or serialization work to get the name of the field from the Accessor, so it is ~ 50% faster.
     *
     * @param key the name of the field to change
     * @param value the value to change that field to
     * @return this, for chaining
     */
    public RecordTransformer<T> withTypeUnsafe(String key, Object value) {
        updates.put(key, value);
        return this;
    }

    /**
     * Replace the underlying record with a new record with updates made to the fields previously
     * specified with @{code with} and @{code withTypeUnsafe}.
     * @return this, for chaining
     * @throws ConstructorException thrown if a reflective error occurs while calling the record's
     * canonical constructor. The mostly likely cause is a type mismatch for a field update.
     */
    public RecordTransformer<T> transform() throws ConstructorException {
        // throws ConstructorException
        Constructor<?> constructor = canonicalConstructorOfRecord(rec.getClass());
        Object[] args = Arrays.stream(constructor.getParameters())
            .map(Parameter::getName)
            // get update if exists, else get old record value
            .map(p -> updates.getOrDefault(p, getValueFromCurrent(p)))
            .toArray();
        updates.clear();

        try {
            rec = (T)constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ConstructorException(e);
        }
        return this;
    }

    private Constructor<?> canonicalConstructorOfRecord(Class<? extends Record> recordClass) throws ConstructorException {
        // The canonical constructor will have all the record's components. getRecordComponents
        // returns all those record components in the order that they appear in the constructor
        Class<?>[] componentTypes = Arrays.stream(recordClass.getRecordComponents())
            .map(RecordComponent::getType)
            .toArray(Class<?>[]::new);
        try {
            return recordClass.getDeclaredConstructor(componentTypes);
        } catch (NoSuchMethodException e) {
            throw new ConstructorException(e);
        }
    }

    private Object getValueFromCurrent(String key) {
        return getters
            .get(key)
            .apply(rec);
    }

}
