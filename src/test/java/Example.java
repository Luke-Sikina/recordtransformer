import com.sikina.recordtransformer.RecordTransformer;

public class Example {
    public enum Color {Blue, Brown, Green}
    public record MyRecord(int id, String name, Color favoriteColor){};

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
