package io.github.mattidragon.advancednetworking.datagen;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ReadmeProvider implements DataProvider {
    private static final String README = """
            # Generated data
            The data here is automatically generated and published only for reference for modpack and resource pack development.
            Any changes made to it will be reverted next time data generation is run.
            """;
    @SuppressWarnings({"deprecation", "UnstableApiUsage"})
    private static final HashCode HASH = Hashing.sha1().hashUnencodedChars(README);
    
    private final DataGenerator root;
    
    public ReadmeProvider(DataGenerator root) {
        this.root = root;
    }
    
    @Override
    public void run(DataWriter writer) throws IOException {
        var path = root.getOutput().resolve("README.md");
    
        writer.write(path, README.getBytes(StandardCharsets.UTF_8), HASH);
    }
    
    @Override
    public String getName() {
        return "Readme";
    }
}
