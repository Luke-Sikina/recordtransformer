package com.sikina.recordtransformer;

public record ExampleRecord(int a, String b, float c) {
    public ExampleRecord(int a, String b, boolean ignored, boolean ignored2) {
        this(a, b, 0f);
    }

    public void someMethod() {
        System.out.println(":/");
    }

    public boolean d() {
        return false;
    }
}
