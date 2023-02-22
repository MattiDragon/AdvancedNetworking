package io.github.mattidragon.advancednetworking.datagen;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionException;

public class ReadmeProvider implements DataProvider {
    private static final String README = """
            # Generated data
            The data here is automatically generated and published only for reference for modpack and resource pack development.
            Any changes made to it will be reverted next time data generation is run.
            """;
    @SuppressWarnings({"deprecation"})
    private static final HashCode HASH = Hashing.sha1().hashUnencodedChars(README);

    private final FabricDataGenerator generator;

    public ReadmeProvider(FabricDataGenerator generator) {
        this.generator = generator;
    }

    @Override
    public void run(DataWriter writer) {
        var path = generator.getOutput().resolve("README.md");
        try {
            writer.write(path, README.getBytes(StandardCharsets.UTF_8), HASH);
        } catch (IOException e) {
            throw new CompletionException(e);
        }
    }

    @Override
    public String getName() {
        return "Readme";
    }
}
