package com.sikina.recordtransformer;

import com.sikina.recordtransformer.exceptions.ConstructorException;
import com.sikina.recordtransformer.exceptions.GetterMappingException;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A wrapper that provides transformation functions to a record
 * via with / transform functions.
 * @param <T>
 */
public class TransformableRecordWrapper<T extends Record> {
    private T rec;
    private final Map<String, Function<T, Object>> getters;
    private final Map<String, Object> updates = new HashMap<>();

    /**
     * Call this constructor once, at the beginning of
     * @param rec the record to transform
     * @throws GetterMappingException thrown if the wrapper can't get record components from rec
     */
    public TransformableRecordWrapper(T rec) throws GetterMappingException {
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
                throw new GetterMappingException(e);
            }
        };
    }

    /**
     * @return Returns the current record.
     */
    public T instance() {
        return rec;
    }

    /**
     * Lazily changes the field referenced by the getter to the new value.
     * This change will not be reflected in rec() until you call transform().
     *
     * @param getter A getter on record T - used to enforce type checking and reference the field being changed
     * @param value The updated value
     * @return this - for chaining
     * @throws GetterMappingException if the getter cannot be transformed into a SerializableLambda
     */
    public <V> TransformableRecordWrapper<T> with(Accessor<V> getter, V value) throws GetterMappingException {
        try {
            Method m = getter.getClass().getDeclaredMethod("writeReplace");
            m.setAccessible(true);
            SerializedLambda replacement = (SerializedLambda) m.invoke(getter);
            updates.put(replacement.getImplMethodName(), value);
            return this;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new GetterMappingException(e);
        }
    }

    /**
     * Lazily changes the field referenced by the key to the new value.
     * This change will not be reflected in rec() until you call transform().
     *
     * This is not type safe. If you try to add a value with a type that cannot be matched to
     * the type of the corresponding field, things will explode when you call transform.
     * The upside of this method compared to with is speed - this doesn't have to do any reflection
     * or serialization work to get the name of the field from the Accessor, so it is significantly
     * faster.
     *
     * @param key the name of the field to change
     * @param value the value to change that field to
     * @return
     */
    public TransformableRecordWrapper<T> withTypeUnsafe(String key, Object value) {
        updates.put(key, value);
        return this;
    }

    public TransformableRecordWrapper<T> transform() throws ConstructorException {
        // throws ConstructorException
        Constructor<?> constructor = canonicalConstructorOfRecord(rec.getClass());
        Object[] args = Arrays.stream(constructor.getParameters())
            .map(Parameter::getName)
            // get update if exists, else get old record value
            .map(p -> updates.getOrDefault(p, getValueFromCurrent(p)))
            .toArray();

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
