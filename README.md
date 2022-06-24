# Transformable Record Wrapper

## Intro

I wanted to use a record for a stateful entity, similar to React JS. I found that it was difficult to go
from one instance of the state to the next with records. This is my attempt to fix that problem.

This rough demo allows you to:
- **Create** a wrapper around a record object:  
    `var t = new RecordTransformer(new MyRecord(1, "Foo"...));`
- **Queue** a series of changes to that record object:  
    `t.with(t.rec()::name).as("Bar")`
- **Transform** that record object into a new instance of the record, merging the current state with the queued changes:  
    `t.transform();`

## Requirements

- Java 17
- Maven 3 (for building)

## Usage

The end goal is to make this a library and put it up on Maven Central. For now, the easiest way to use this
is probably just to copy it into your codebase. Alternatively, you could build this (`mvn clean install`) and
include the `funky-1.0-SNAPSHOT.jar` in your project.

Here's an example of how the library works:

```java
import com.sikina.recordtransformer.RecordTransformer;

public class Example {
    // Create an interesting record
    public enum Color {Blue, Brown, Green}
    public record MyRecord(int id, String name, Color favoriteColor) {}

    // This is slower and a bit syntactically clunky, but it does a better job of
    // verifying types at compile time
    public void typeSafeExample() {
        var transformer = new RecordTransformer<>(new MyRecord(50, "Philburt", Color.Brown));
        System.out.println(transformer.rec());
        transformer
            .with(transformer.rec()::name).as("Tomi")
            .with(transformer.rec()::favoriteColor).as(Color.Green)
            .transform();
        System.out.println(transformer.rec());
    }

    // This is 50% faster, but is more prone to runtime breakages from type mismatches
    public void typelessExample() {
        var transformer = new RecordTransformer<>(new MyRecord(50, "Philburt", Color.Brown));
        System.out.println(transformer.rec());
        transformer
            .withTypeUnsafe("name", "Tomi")
            .withTypeUnsafe("favoriteColor", Color.Blue)
            .transform();
        System.out.println(transformer.rec());
    }
}
```

## References
I borrowed some cool serialization logic from here: https://github.com/Hervian/safety-mirror  
I was inspired by this post:
https://github.com/openjdk/amber-docs/blob/master/eg-drafts/reconstruction-records-and-classes.md
