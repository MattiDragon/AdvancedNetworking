package io.github.mattidragon.advancednetworking.misc;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.*;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Decoder;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.command.argument.packrat.ArgumentParser;
import net.minecraft.command.argument.packrat.PackratParsing;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentMapImpl;
import net.minecraft.component.ComponentType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.predicate.item.ItemSubPredicate;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FilterPredicateParsing {
    private static final RegistryWrapper.WrapperLookup STATIC_LOOKUP = DynamicRegistryManager.of(Registries.REGISTRIES);
    @SuppressWarnings("deprecation")
    private static final ArgumentParser<List<Predicate<TransferVariant<Item>>>> ITEM_PARSER 
            = PackratParsing.createParser(new Context<>(STATIC_LOOKUP, RegistryKeys.ITEM, Item::getRegistryEntry, Item::getComponents));
    @SuppressWarnings("deprecation")
    private static final ArgumentParser<List<Predicate<TransferVariant<Fluid>>>> FLUID_PARSER 
            = PackratParsing.createParser(new Context<>(STATIC_LOOKUP, RegistryKeys.FLUID, Fluid::getRegistryEntry, fluid -> ComponentMap.EMPTY));

    private static final SimpleCommandExceptionType ITEM_PREDICATES_NOT_SUPPORTED = new SimpleCommandExceptionType(
            Text.translatable("advanced_networking.resource_filter.predicates_not_supported")
    );
    private static final DynamicCommandExceptionType INVALID_ITEM_ID_EXCEPTION = new DynamicCommandExceptionType(
            id -> Text.stringifiedTranslatable("argument.item.id.invalid", id)
    );
    private static final DynamicCommandExceptionType UNKNOWN_ITEM_TAG_EXCEPTION = new DynamicCommandExceptionType(
            tag -> Text.stringifiedTranslatable("arguments.item.tag.unknown", tag)
    );
    private static final DynamicCommandExceptionType UNKNOWN_ITEM_COMPONENT_EXCEPTION = new DynamicCommandExceptionType(
            component -> Text.stringifiedTranslatable("arguments.item.component.unknown", component)
    );
    private static final Dynamic2CommandExceptionType MALFORMED_ITEM_COMPONENT_EXCEPTION = new Dynamic2CommandExceptionType(
            (object, object2) -> Text.stringifiedTranslatable("arguments.item.component.malformed", object, object2)
    );
    private static final DynamicCommandExceptionType UNKNOWN_ITEM_PREDICATE_EXCEPTION = new DynamicCommandExceptionType(
            predicate -> Text.stringifiedTranslatable("arguments.item.predicate.unknown", predicate)
    );
    private static final Dynamic2CommandExceptionType MALFORMED_ITEM_PREDICATE_EXCEPTION = new Dynamic2CommandExceptionType(
            (object, object2) -> Text.stringifiedTranslatable("arguments.item.predicate.malformed", object, object2)
    );
    
    public static final PredicateParser<Item> ITEM_PREDICATE_PARSER = reader -> {
        try {
            return Either.left(Util.allOf(ITEM_PARSER.parse(reader)));
        } catch (CommandSyntaxException e) {
            return Either.right(e);
        }
    };
    public static final PredicateParser<Fluid> FLUID_PREDICATE_PARSER = reader -> {
        try {
            return Either.left(Util.allOf(FLUID_PARSER.parse(reader)));
        } catch (CommandSyntaxException e) {
            return Either.right(e);
        }
    };

    @FunctionalInterface
    public interface PredicateParser<R> {
        Either<Predicate<TransferVariant<R>>, CommandSyntaxException> parse(StringReader reader);
    }

    private static class Context<V> implements PackratParsing.Callbacks<Predicate<TransferVariant<V>>, ComponentCheck<V>, ItemSubPredicateCheck> {
        private final Function<V, RegistryEntry<V>> entryFunction;
        private final Function<V, ComponentMap> defaultComponentGetter;
        
        private final RegistryWrapper.Impl<V> items;
        private final RegistryWrapper.Impl<ComponentType<?>> components;
        private final RegistryWrapper.Impl<ItemSubPredicate.Type<?>> subPredicateTypes;
        private final RegistryKey<Registry<V>> key;
        private final RegistryOps<NbtElement> ops;

        Context(RegistryWrapper.WrapperLookup lookup, RegistryKey<Registry<V>> registryKey, Function<V, RegistryEntry<V>> entryFunction, Function<V, ComponentMap> defaultComponentGetter) {
            this.entryFunction = entryFunction;
            this.defaultComponentGetter = defaultComponentGetter;
            this.items = lookup.getWrapperOrThrow(registryKey);
            this.components = lookup.getWrapperOrThrow(RegistryKeys.DATA_COMPONENT_TYPE);
            this.subPredicateTypes = lookup.getWrapperOrThrow(RegistryKeys.ITEM_SUB_PREDICATE_TYPE);
            this.ops = lookup.getOps(NbtOps.INSTANCE);
            this.key = registryKey;
        }


        @Override
        public Predicate<TransferVariant<V>> itemMatchPredicate(ImmutableStringReader reader, Identifier id) throws CommandSyntaxException {
            var entry = items.getOptional(RegistryKey.of(key, id))
                    .orElseThrow(() -> INVALID_ITEM_ID_EXCEPTION.createWithContext(reader, id));
            return variant -> entryFunction.apply(variant.getObject()) == entry;
        }

        @Override
        public Stream<Identifier> streamItemIds() {
            return items.streamKeys().map(RegistryKey::getValue);
        }

        @Override
        public Predicate<TransferVariant<V>> tagMatchPredicate(ImmutableStringReader reader, Identifier id) throws CommandSyntaxException {
            var tag = items.getOptional(TagKey.of(key, id))
                    .orElseThrow(() -> UNKNOWN_ITEM_TAG_EXCEPTION.createWithContext(reader, id));
            return variant -> entryFunction.apply(variant.getObject()).isIn(tag.getTag());
        }

        @Override
        public Stream<Identifier> streamTags() {
            return items.streamTagKeys().map(TagKey::id);
        }

        @Override
        public ComponentCheck<V> componentCheck(ImmutableStringReader reader, Identifier id) throws CommandSyntaxException {
            var componentType = this.components
                    .getOptional(RegistryKey.of(RegistryKeys.DATA_COMPONENT_TYPE, id))
                    .map(RegistryEntry::value)
                    .orElseThrow(() -> UNKNOWN_ITEM_COMPONENT_EXCEPTION.createWithContext(reader, id));
            return ComponentCheck.read(reader, id, componentType, defaultComponentGetter);
        }

        @Override
        public Stream<Identifier> streamComponentIds() {
            return components.streamKeys().map(RegistryKey::getValue);
        }

        @Override
        public Predicate<TransferVariant<V>> componentMatchPredicate(ImmutableStringReader reader, ComponentCheck<V> check, NbtElement nbt) throws CommandSyntaxException {
            return check.createPredicate(reader, ops, nbt);
        }

        @Override
        public Predicate<TransferVariant<V>> componentPresencePredicate(ImmutableStringReader reader, ComponentCheck<V> check) {
            return check.presenceChecker;
        }

        @Override
        public ItemSubPredicateCheck subPredicateCheck(ImmutableStringReader reader, Identifier id) throws CommandSyntaxException {
            return this.subPredicateTypes
                    .getOptional(RegistryKey.of(RegistryKeys.ITEM_SUB_PREDICATE_TYPE, id))
                    .map(ItemSubPredicateCheck::new)
                    .orElseThrow(() -> UNKNOWN_ITEM_PREDICATE_EXCEPTION.createWithContext(reader, id));

        }

        @Override
        public Stream<Identifier> streamSubPredicateIds() {
            return subPredicateTypes.streamKeys().map(RegistryKey::getValue);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Predicate<TransferVariant<V>> subPredicatePredicate(ImmutableStringReader reader, ItemSubPredicateCheck check, NbtElement nbt) throws CommandSyntaxException {
            if (key.equals(RegistryKeys.ITEM)) {
                // Unchecked cast because we know V is Item from the registry key
                return (Predicate<TransferVariant<V>>) (Predicate<?>) check.createPredicate(reader, ops, nbt);
            }
            throw ITEM_PREDICATES_NOT_SUPPORTED.createWithContext(reader);
        }

        @Override
        public Predicate<TransferVariant<V>> negate(Predicate<TransferVariant<V>> predicate) {
            return predicate.negate();
        }

        @Override
        public Predicate<TransferVariant<V>> anyOf(List<Predicate<TransferVariant<V>>> predicates) {
            return Util.anyOf(predicates);
        }
    }

    private record ComponentCheck<V>(
            Identifier id,
            Predicate<TransferVariant<V>> presenceChecker,
            Decoder<? extends Predicate<TransferVariant<V>>> valueChecker
    ) {
        public static <V, T> ComponentCheck<V> read(ImmutableStringReader reader,
                                                    Identifier id,
                                                    ComponentType<T> type,
                                                    Function<V, ComponentMap> defaultComponentGetter) throws CommandSyntaxException {
            var codec = type.getCodec();
            if (codec == null) {
                throw UNKNOWN_ITEM_COMPONENT_EXCEPTION.createWithContext(reader, id);
            } else {
                return new ComponentCheck<>(id, 
                        stack -> getComponents(stack, defaultComponentGetter).contains(type), 
                        codec.map(expected -> stack -> Objects.equals(expected, getComponents(stack, defaultComponentGetter).get(type))));
            }
        }

        public Predicate<TransferVariant<V>> createPredicate(ImmutableStringReader reader, RegistryOps<NbtElement> ops, NbtElement nbt) throws CommandSyntaxException {
            return this.valueChecker.parse(ops, nbt).getOrThrow(
                    error -> MALFORMED_ITEM_COMPONENT_EXCEPTION.createWithContext(reader, this.id.toString(), error)
            );
        }
    }

    private record ItemSubPredicateCheck(Identifier id, Decoder<? extends Predicate<TransferVariant<Item>>> type) {
        public ItemSubPredicateCheck(RegistryEntry.Reference<ItemSubPredicate.Type<?>> type) {
            this(type.registryKey().getValue(),
                    type.value().codec().map(predicate -> variant -> {
                        @SuppressWarnings("deprecation")
                        var stack = new ItemStack(variant.getObject().getRegistryEntry(), 1, variant.getComponents());
                        return predicate.test(stack);
                    }));
        }

        public Predicate<TransferVariant<Item>> createPredicate(ImmutableStringReader reader, RegistryOps<NbtElement> ops, NbtElement nbt) throws CommandSyntaxException {
            var dataResult = this.type.parse(ops, nbt);
            return dataResult.getOrThrow(
                    error -> MALFORMED_ITEM_PREDICATE_EXCEPTION.createWithContext(reader, this.id.toString(), error)
            );
        }
    }
    
    private static <V> ComponentMap getComponents(TransferVariant<V> variant, Function<V, ComponentMap> baseGetter) {
        var map = new ComponentMapImpl(baseGetter.apply(variant.getObject()));
        map.applyChanges(variant.getComponents());
        return map;
    }
}
