package com.sikina.recordtransformer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CachedRecordTransformerTest {
    @Test
    void shouldTransformRecord() {
        var t = new CachedRecordTransformer<>(new RecordTransformerTest.ExampleRec(1, "foo"));
        RecordTransformerTest.ExampleRec actual = t.with(t.rec()::a).as(2)
            .transform()
            .rec();

        RecordTransformerTest.ExampleRec expected = new RecordTransformerTest.ExampleRec(2, "foo");
        Assertions.assertEquals(expected, actual);

        // and now while cached
        t = new CachedRecordTransformer<>(new RecordTransformerTest.ExampleRec(1, "foo"));
        actual = t.with(t.rec()::a).as(2)
            .transform()
            .rec();

        expected = new RecordTransformerTest.ExampleRec(2, "foo");
        Assertions.assertEquals(expected, actual);
    }

}