package com.sikina.recordtransformer;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TransformableRecordWrapper<T extends Record> {
    private T instance;
    private final Map<String, Function<T, Object>> getters;
    private final Map<String, Object> updates = new HashMap<>();

    public TransformableRecordWrapper(T instance) throws NoSuchMethodException {
        // I made a ton of assumptions that only hold true for records
        assert instance.getClass().isRecord();

        this.instance = instance;
        Map<String, Type> constructorParams =
            Arrays.stream(canonicalConstructorOfRecord(instance.getClass()).getParameters())
            .collect(Collectors.toMap(
                Parameter::getName,
                Parameter::getParameterizedType
            ));
        getters = Arrays.stream(instance.getClass().getMethods())
            .filter(m -> constructorParams.containsKey(m.getName()))
            .filter(m -> constructorParams.get(m.getName()).equals(m.getReturnType()))
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
                throw new RuntimeException(e);
            }
        };
    }

    // TODO: how can I make this type safe?
    public TransformableRecordWrapper<T> with(String key, Object value) {
        updates.put(key, value);
        return this;
    }

    public TransformableRecordWrapper<T> transform() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        Constructor<?> constructor = canonicalConstructorOfRecord(instance.getClass());

        Object[] args = Arrays.stream(constructor.getParameters())
            .map(Parameter::getName)
            .map(p -> updates.getOrDefault(p, getValueFromCurrent(p)))
            .toArray();

        instance = (T)constructor.newInstance(args);
        return this;
    }

    private Constructor<?> canonicalConstructorOfRecord(Class<?> recordClass) throws NoSuchMethodException, SecurityException {
        Class<?>[] componentTypes = Arrays.stream(recordClass.getRecordComponents())
            .map(RecordComponent::getType)
            .toArray(Class<?>[]::new);
        return recordClass.getDeclaredConstructor(componentTypes);
    }

    private Object getValueFromCurrent(String key) {
        return getters
            .get(key)
            .apply(instance);
    }

    public T instance() {
        return instance;
    }

}
