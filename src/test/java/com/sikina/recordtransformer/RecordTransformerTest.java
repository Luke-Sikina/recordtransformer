package com.sikina.recordtransformer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import otherpackage.ForeignRecord;

import java.util.Objects;

class RecordTransformerTest {

    public record ExampleRec(int a, String b){}

    public record RecordWithNonCanonicalConstructor(int a) {
        RecordWithNonCanonicalConstructor(boolean ignored) {
            this(1);
        }
    }

    public record RecordWithExtraMethod(int a) {
        public int b() {
            return 0;
        }
    }

    public record RecordWithNonSerializableField(int a, NonSerializable b){}

    public static final class NonSerializable{
        private final String s;

        public NonSerializable(String s) {
            this.s = s;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NonSerializable that)) return false;
            return s.equals(that.s);
        }

        @Override
        public int hashCode() {
            return Objects.hash(s);
        }
    }

    public record RecordWithConstructorThatSometimesExplodes(boolean explode){
        public RecordWithConstructorThatSometimesExplodes(boolean explode) {
            this.explode = explode;
            if (explode) {
                throw new RuntimeException();
            }
        }
    }

    public record ExplodingRecord(int a, String b) {
        public int a() {
            throw new RuntimeException();
        }
    }

    @Test
    void shouldConstructObject() {
        var t = new RecordTransformer<>(new ExampleRec(1, "foo"));
        Assertions.assertNotNull(t);
    }

    @Test
    void shouldConstructForeignRecord() {
        // I thought records in another package could cause IllegalAccessExceptions.
        // This test is just verifying that they don't.
        var t = new RecordTransformer<>(new ForeignRecord(1, "foo"));
        Assertions.assertNotNull(t);
    }

    @Test
    void shouldConstructRecordThatHasNonCanonicalConstructor() {
        var t = new RecordTransformer<>(new RecordWithNonCanonicalConstructor(true));
        Assertions.assertNotNull(t);
    }

    @Test
    void shouldConstructRecordThatHasExtraMethod() {
        var t = new RecordTransformer<>(new RecordWithExtraMethod(1));
        Assertions.assertNotNull(t);
    }

    @Test
    void shouldTransformRecord() {
        var t = new RecordTransformer<>(new ExampleRec(1, "foo"));
        ExampleRec actual = t.with(t.rec()::a).as(2)
            .transform()
            .rec();

        ExampleRec expected = new ExampleRec(2, "foo");
        Assertions.assertEquals(expected, actual);

        actual = t.withTypeUnsafe("b", "bar")
            .transform()
            .rec();
        expected = new ExampleRec(2, "bar");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void shouldTransformRecordFromAnotherPackage() {
        var t = new RecordTransformer<>(new ForeignRecord(1, "foo"));
        ForeignRecord actual = t.with(t.rec()::a).as(2)
            .transform()
            .rec();

        ForeignRecord expected = new ForeignRecord(2, "foo");
        Assertions.assertEquals(expected, actual);

        actual = t.withTypeUnsafe("b", "bar")
            .transform()
            .rec();
        expected = new ForeignRecord(2, "bar");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void shouldTransformRecordWithNonCanonicalConstructor() {
        var t = new RecordTransformer<>(new RecordWithNonCanonicalConstructor(1));
        RecordWithNonCanonicalConstructor actual = t.with(t.rec()::a).as(2)
            .transform()
            .rec();

        RecordWithNonCanonicalConstructor expected = new RecordWithNonCanonicalConstructor(2);
        Assertions.assertEquals(expected, actual);

        actual = t.withTypeUnsafe("a", 3)
            .transform()
            .rec();
        expected = new RecordWithNonCanonicalConstructor(3);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void shouldTransformRecordWithExtraMethod() {
        var t = new RecordTransformer<>(new RecordWithExtraMethod(1));
        RecordWithExtraMethod actual = t.with(t.rec()::a).as(2)
            .transform()
            .rec();

        RecordWithExtraMethod expected = new RecordWithExtraMethod(2);
        Assertions.assertEquals(expected, actual);

        actual = t.withTypeUnsafe("a", 3)
            .transform()
            .rec();
        expected = new RecordWithExtraMethod(3);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void shouldTransformRecordWithNonSerializableField() {
        // Non-serializable fields should not impact the wrapper because nothing actually gets serialized
        // We just use serializable lambda to get method names
        var t = new RecordTransformer<>(new RecordWithNonSerializableField(1, new NonSerializable("foo")));
        RecordWithNonSerializableField actual = t.with(t.rec()::a).as(2)
            .transform()
            .rec();

        RecordWithNonSerializableField expected = new RecordWithNonSerializableField(2, new NonSerializable("foo"));
        Assertions.assertEquals(expected, actual);

        actual = t.withTypeUnsafe("b", new NonSerializable("bar"))
            .transform()
            .rec();
        expected = new RecordWithNonSerializableField(2, new NonSerializable("bar"));
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void shouldExplode() {
        var t = new RecordTransformer<>(new ExplodingRecord(1, "foo"));
        Assertions.assertThrows(GetterException.class, t::transform);
    }

    @Test
    void shouldExplodeOnTransform() {
        // This is a really janky test. RecordWithConstructorThatSometimesExplodes will throw a RTE if
        // set to true. We make the first record with it set to false, then transform to true
        // to get the exception and verify that it is captured correctly.
        var t = new RecordTransformer<>(new RecordWithConstructorThatSometimesExplodes(false));
        t.with(t.rec()::explode).as(true);
        Assertions.assertThrows(ConstructorException.class, t::transform);
    }
}