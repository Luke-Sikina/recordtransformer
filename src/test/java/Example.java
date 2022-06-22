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
