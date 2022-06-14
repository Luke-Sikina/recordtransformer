package com.sikina.recordtransformer;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TransformableRecordWrapper<T> {
    private T instance;
    private final Map<String, Function<T, Object>> getters;
    private final Map<String, Object> updates = new HashMap<>();

    public TransformableRecordWrapper(T instance) {
        // I made a ton of assumptions that only hold true for records
        assert instance.getClass().isRecord();

        this.instance = instance;
        Map<String, Type> constructorParams =
            Arrays.stream(instance.getClass().getConstructors()[0].getParameters())
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

    public TransformableRecordWrapper<T> transform() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        // TODO: how do you differentiate the default record constructor from any others?
        Constructor<?> constructor = instance.getClass().getConstructors()[0];

        Object[] args = Arrays.stream(constructor.getParameters())
            .map(Parameter::getName)
            .map(p -> updates.getOrDefault(p, getValueFromCurrent(p)))
            .toArray();

        instance = (T)constructor.newInstance(args);
        return this;
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
