# Transformable Record Wrapper

## Intro

I wanted to use a record for a stateful entity, similar to React JS. I found that it was difficult to go
from one instance of the state to the next with records. This is my attempt to fix that problem.

This rough demo allows you to:
- **Create** a wrapper around a record object
- **Queue** a series of changes to that record object
- **Transform** that record object into a new instance of the record, merging the current state with the queued changes

## Requirements

- Java 17
- Maven 3.8 (ish)

## Usage

**Build**: `mvn clean install`  
**Run**: `java -classpath target/funky-1.0-SNAPSHOT-jar-with-dependencies.jar com.sikina.recordtransformer.Example`  
**Use**: see `Example.java`

## Problems
I don't understand how the magic library gets the method name from an anonymous method.
```