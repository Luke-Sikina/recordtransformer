package com.sikina.recordtransformer;

import java.lang.reflect.InvocationTargetException;

public class Example {
    /**
     * Goal: create a wrapper around an immutable state that lets you:
     * - queue a series of mutations
     * - create a new state object that merges that list of mutations with the current state
     */
    public static void main(String[] args) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        ExampleRecord first = new ExampleRecord(1, "foo", 5f);
        TransformableRecordWrapper<ExampleRecord> transformer = new TransformableRecordWrapper<>(first);

        ExampleRecord second = transformer
            .withTypeUnsafe("a", 2)
            .with(transformer.instance()::b, "bar")
            .with(transformer.instance()::c, 1f)
            .transform()
            .instance();
        assert second.a() == 2;
        assert second.b().equals("bar");
        assert second.c() == 1f;

        ExampleRecord third = transformer
            .with(transformer.instance()::a, 3)
            .transform()
            .instance();
        assert third.a() == 3;
        assert third.b().equals(second.b());
        assert third.c() == second.c();

        System.out.println(":)");
    }
}
