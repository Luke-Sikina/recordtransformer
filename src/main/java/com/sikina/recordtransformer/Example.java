package com.sikina.recordtransformer;

import java.lang.reflect.InvocationTargetException;

public class Example {
    /**
     * Goal: create a wrapper around an immutable state that lets you:
     * - queue a series of mutations
     * - create a new state object that merges that list of mutations with the current state
     */
    public static void main(String[] args) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        ExampleRecord first = new ExampleRecord(1, "foo", 5f);
        TransformableRecordWrapper<ExampleRecord> transformer = new TransformableRecordWrapper<>(first);

        ExampleRecord second = transformer
            .with("a", 2)
            .with("b", "bar")
            .transform()
            .instance();
        assert second.a() == 2;
        assert second.b().equals("bar");
        assert second.c() == first.c();

        ExampleRecord third = transformer
            .with("a", 3)
            .transform()
            .instance();
        assert third.a() == 3;
        assert third.b().equals(second.b());
        assert third.c() == first.c();
        assert third.c() == second.c();

        System.out.println(":)");
    }
}
