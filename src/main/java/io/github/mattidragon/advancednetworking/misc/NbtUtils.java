package io.github.mattidragon.advancednetworking.misc;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class NbtUtils {
    public static List<String> readStrings(NbtCompound nbt, String id) {
        return nbt.getList(id).stream().map(NbtElement::asString).flatMap(Optional::stream).toList();
    }

    public static void writeStrings(NbtCompound nbt, String id, List<String> strings) {
        nbt.put(id, strings.stream().map(NbtString::of).collect(Collectors.toCollection(NbtList::new)));
    }
}
