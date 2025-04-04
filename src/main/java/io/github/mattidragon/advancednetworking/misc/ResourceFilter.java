package io.github.mattidragon.advancednetworking.misc;

import com.mojang.brigadier.StringReader;
import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ResourceFilter<R, V extends TransferVariant<R>> {
    private final Registry<R> registry;
    private final FilterPredicateParsing.PredicateParser<R> predicateParser;

    private String filter = "*";
    private boolean isRegex = false;
    private boolean isWhitelist = true;
    
    @Nullable
    private Predicate<V> cachedPredicate = null;

    public ResourceFilter(Registry<R> registry, FilterPredicateParsing.PredicateParser<R> predicateParser) {
        this.registry = registry;
        this.predicateParser = predicateParser;
    }

    public List<Text> validate() {
        var list = new ArrayList<Text>();
        
        if (shouldUseRegex()) {
            try {
                var pattern = Pattern.compile(filter);
                cachedPredicate = v -> pattern.matcher(Objects.requireNonNull(registry.getId(v.getObject()), "resource not registered").toString()).matches();
            } catch (PatternSyntaxException e) {
                list.add(Text.translatable("node.advanced_networking.filter.invalid_id_regex", e.getDescription(), e.getIndex()));
            }
        } else {
            var parse = predicateParser.parse(new StringReader(filter.trim()));
            parse.ifLeft(predicate -> this.cachedPredicate = predicate::test);
            parse.ifRight(e -> list.add(Text.translatable("node.advanced_networking.filter.invalid_filter", e.getMessage())));
        }
        
        return list;
    }

    public boolean isAllowed(V resource) {
        if (cachedPredicate == null) {
            if (shouldUseRegex()) {
                var pattern = Pattern.compile(filter);
                cachedPredicate = v -> pattern.matcher(Objects.requireNonNull(registry.getId(v.getObject()), "resource not registered").toString()).matches();
            } else {
                cachedPredicate = predicateParser.parse(new StringReader(filter.trim()))
                        .left()
                        .orElseThrow(() -> new IllegalStateException("Error in predicate not caught in validation"))
                        ::test;
            }
        }
        
        var matches = cachedPredicate.test(resource);

        if (isWhitelist) {
            return matches;
        } else {
            return !matches;
        }
    }

    public void readNbt(NbtCompound data) {
        filter = data.getString("idFilter")
                .or(() -> data.getString("filter"))
                .orElse("");

        isWhitelist = data.getBoolean("whitelist", true);
        isRegex = data.getBoolean("regex", false);
        
        cachedPredicate = null;
    }

    public void writeNbt(NbtCompound data) {
        data.putString("filter", filter);
        data.putBoolean("whitelist", isWhitelist);
        data.putBoolean("regex", isRegex);
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
        cachedPredicate = null;
    }

    public void setUseRegex(boolean useRegex) {
        this.isRegex = useRegex;
        cachedPredicate = null;
    }

    public boolean isWhitelist() {
        return isWhitelist;
    }

    public void setWhitelist(boolean whitelist) {
        isWhitelist = whitelist;
    }

    public boolean shouldUseRegex() {
        return isRegex && !AdvancedNetworking.CONFIG.get().disableRegexFilter();
    }
}
