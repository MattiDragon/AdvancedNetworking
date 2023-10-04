package io.github.mattidragon.advancednetworking.misc;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.mattidragon.advancednetworking.AdvancedNetworking;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ResourceFilter<R, V extends TransferVariant<R>> {
    private final Registry<R> registry;

    private String idFilter = "";
    private String nbtFilter = "";
    private Mode mode = Mode.RESOURCE;
    private boolean useRegex = false;
    private boolean isWhitelist = true;

    public ResourceFilter(Registry<R> registry) {
        this.registry = registry;
    }

    public List<Text> validate() {
        var list = new ArrayList<Text>();
        if (!idFilter.isBlank()) {
            if (shouldUseRegex()) {
                try {
                    Pattern.compile(idFilter);
                } catch (PatternSyntaxException e) {
                    list.add(Text.translatable("node.advanced_networking.filter.invalid_id_regex", e.getDescription(), e.getIndex()));
                }
            } else {
                var id = Identifier.tryParse(idFilter.trim());
                if (id == null) {
                    list.add(Text.translatable("node.advanced_networking.filter.invalid_id", idFilter));
                } else {
                    if (mode == Mode.RESOURCE && !registry.containsId(id))
                        list.add(Text.translatable("node.advanced_networking.filter.unknown_resource", id));
                    if (mode == Mode.TAG && registry.streamTags().map(TagKey::id).noneMatch(id::equals))
                        list.add(Text.translatable("node.advanced_networking.filter.unknown_tag", id));
                }
            }
        }

        try {
            if (!nbtFilter.isBlank())
                NbtPathArgumentType.nbtPath().parse(new StringReader(nbtFilter.trim()));
        } catch (CommandSyntaxException | StringIndexOutOfBoundsException e) {
            list.add(Text.translatable("node.advanced_networking.filter.invalid_nbt_path", e.getMessage()));
        }
        return list;
    }

    public boolean isAllowed(V resource) {
        NbtPathArgumentType.NbtPath nbtPath;
        try {
            nbtPath = nbtFilter.isBlank() ? null : NbtPathArgumentType.nbtPath().parse(new StringReader(nbtFilter.trim()));
        } catch (CommandSyntaxException e) {
            throw new RuntimeException("Error while building nbt path not caught in validation", e);
        }

        var idMatches = idFilter.isBlank() || switch (mode) {
            case RESOURCE -> checkId(registry.getId(resource.getObject()));
            case TAG -> registry.getEntry(resource.getObject())
                    .streamTags()
                    .map(TagKey::id)
                    .anyMatch(this::checkId);
        };
        var nbtMatches = nbtPath == null || nbtPath.count(resource.getNbt()) > 0;
        var matches = idMatches && nbtMatches;

        if (isWhitelist) {
            return matches;
        } else {
            return !matches;
        }
    }

    private boolean checkId(Identifier id) {
        if (shouldUseRegex()) {
            return Pattern.matches(idFilter, id.toString());
        } else {
            return new Identifier(idFilter).equals(id);
        }
    }

    public void readNbt(NbtCompound data) {
        // Can't go breaking old saves
        if (data.contains("itemId", NbtElement.STRING_TYPE)) {
            idFilter = data.getString("itemId");
        } else if (data.contains("fluidId", NbtElement.STRING_TYPE)) {
            idFilter = data.getString("fluidId");
        } else {
            idFilter = data.getString("idFilter");
        }

        // Can't go breaking old saves
        if (data.contains("nbt", NbtElement.STRING_TYPE)) {
            nbtFilter = data.getString("nbt");
        } else {
            nbtFilter = data.getString("nbtFilter");
        }

        mode = Mode.byOrdinal(data.getInt("mode"));
        isWhitelist = data.getBoolean("whitelist");
        useRegex = data.getBoolean("regex");
    }

    public void writeNbt(NbtCompound data) {
        data.putString("idFilter", idFilter);
        data.putString("nbtFilter", nbtFilter);
        data.putInt("mode", mode.ordinal());
        data.putBoolean("whitelist", isWhitelist);
        data.putBoolean("regex", useRegex);
    }

    public String getIdFilter() {
        return idFilter;
    }

    public void setIdFilter(String idFilter) {
        this.idFilter = idFilter;
    }

    public String getNbtFilter() {
        return nbtFilter;
    }

    public void setNbtFilter(String nbtFilter) {
        this.nbtFilter = nbtFilter;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public void setUseRegex(boolean useRegex) {
        this.useRegex = useRegex;
    }

    public boolean isWhitelist() {
        return isWhitelist;
    }

    public void setWhitelist(boolean whitelist) {
        isWhitelist = whitelist;
    }

    public boolean shouldUseRegex() {
        return useRegex && !AdvancedNetworking.CONFIG.get().disableRegexFilter();
    }

    public enum Mode {
        RESOURCE, TAG;

        private static Mode byOrdinal(int ordinal) {
            return ordinal > 0 && ordinal < values().length ? values()[ordinal] : RESOURCE;
        }
    }

}
