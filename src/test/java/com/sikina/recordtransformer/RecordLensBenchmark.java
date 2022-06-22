package com.sikina.recordtransformer;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 3)
@Measurement(iterations = 8)
public class RecordLensBenchmark {
    public static final int I = 10;
    public record LargeRecord(int a, int b, int c, int d, int e, int f, int g, int h, int i, int j){}

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(RecordLensBenchmark.class.getSimpleName())
            .forks(1)
            .build();

        new Runner(opt).run();
    }
    @Benchmark
    public void unsafeTransform() {
        LargeRecord start = new LargeRecord(1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
        RecordLens<LargeRecord> lens = new RecordLens<>(start);
        for (int i = 0; i < I; i++) {
            lens = lens
                .withTypeUnsafe("a", i)
                .withTypeUnsafe("b", i)
                .withTypeUnsafe("c", i)
                .withTypeUnsafe("d", i)
                .withTypeUnsafe("e", i)
                .withTypeUnsafe("f", i)
                .withTypeUnsafe("g", i)
                .withTypeUnsafe("h", i)
                .withTypeUnsafe("i", i)
                .withTypeUnsafe("j", i)
                .transform();
        }

    }

    @Benchmark
    public void safeTransform() {
        LargeRecord start = new LargeRecord(1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
        RecordLens<LargeRecord> lens = new RecordLens<>(start);
        for (int i = 0; i < I; i++) {
            lens = lens
                .with(lens.rec()::a).as(i)
                .with(lens.rec()::b).as(i)
                .with(lens.rec()::c).as(i)
                .with(lens.rec()::d).as(i)
                .with(lens.rec()::e).as(i)
                .with(lens.rec()::f).as(i)
                .with(lens.rec()::g).as(i)
                .with(lens.rec()::h).as(i)
                .with(lens.rec()::i).as(i)
                .with(lens.rec()::j).as(i)
                .transform();
        }

    }
}