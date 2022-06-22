# Transformable Record Wrapper

## Intro

I wanted to use a record for a stateful entity, similar to React JS. I found that it was difficult to go
from one instance of the state to the next with records. This is my attempt to fix that problem.

This rough demo allows you to:
- **Create** a wrapper around a record object:  
    `var lens = new RecordLens(new MyRecord(1, "Foo"...));`
- **Queue** a series of changes to that record object:  
    `.with(lens.rec()::name).as("Bar")`
- **Transform** that record object into a new instance of the record, merging the current state with the queued changes:  
    `lens.transform();`

## Requirements

- Java 17
- Maven 3.8 (ish)

## Usage

The end goal is to make this a library and put it up on Maven Central. For now, the easiest way to use this
is probably just to copy it into your codebase. Alternatively, you could build this (`mvn clean install`) and
include the `funky-1.0-SNAPSHOT.jar` in your project.

Here's an example of how the library works:

```java
import com.sikina.recordtransformer.RecordLens;

public class Example {
    public enum Color {Blue, Brown, Green}
    public record MyRecord(int id, String name, Color favoriteColor){};

    // This is slower and a bit syntactically clunky, but it does a better job of
    // verifying types at compile time
    public void typeSafeExample() {
        var lens = new RecordLens<>(new MyRecord(50, "Philburt", Color.Brown));
        System.out.println(lens.rec());
        lens
            .with(lens.rec()::name).as("Tomi")
            .with(lens.rec()::favoriteColor).as(Color.Green)
            .transform();
        System.out.println(lens.rec());
    }

    // This is 50% faster, but is more prone to runtime breakages from type mismatches
    public void typelessExample() {
        var lens = new RecordLens<>(new MyRecord(50, "Philburt", Color.Brown));
        System.out.println(lens.rec());
        lens
            .withTypeUnsafe("name", "Tomi")
            .withTypeUnsafe("favoriteColor", Color.Blue)
            .transform();
        System.out.println(lens.rec());
    }
}
```

## References
I borrowed some cool serialization logic from here: https://github.com/Hervian/safety-mirror
