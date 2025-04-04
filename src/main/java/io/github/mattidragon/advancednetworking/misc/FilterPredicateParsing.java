package io.github.mattidragon.advancednetworking.misc;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Dynamic;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.command.argument.ItemPredicateParsing;
import net.minecraft.component.ComponentType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.predicate.component.ComponentPredicate;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.packrat.PackratParser;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FilterPredicateParsing {
    private static final RegistryWrapper.WrapperLookup STATIC_LOOKUP = DynamicRegistryManager.of(Registries.REGISTRIES);
    @SuppressWarnings("deprecation")
    private static final PackratParser<List<Predicate<TransferVariant<Item>>>> ITEM_PARSER
            = ItemPredicateParsing.createParser(new Context<>(STATIC_LOOKUP, RegistryKeys.ITEM, Item::getRegistryEntry));
    @SuppressWarnings("deprecation")
    private static final PackratParser<List<Predicate<TransferVariant<Fluid>>>> FLUID_PARSER
            = ItemPredicateParsing.createParser(new Context<>(STATIC_LOOKUP, RegistryKeys.FLUID, Fluid::getRegistryEntry));

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

    private static class Context<V> implements ItemPredicateParsing.Callbacks<Predicate<TransferVariant<V>>, ComponentCheck, SubPredicateCheck> {
        private final Function<V, RegistryEntry<V>> entryFunction;
        private final RegistryWrapper.Impl<V> valueRegistry;
        private final RegistryWrapper.Impl<ComponentType<?>> components;
        private final RegistryWrapper.Impl<ComponentPredicate.Type<?>> subPredicateTypes;
        private final RegistryKey<Registry<V>> key;
        private final RegistryWrapper.WrapperLookup lookup;

        Context(RegistryWrapper.WrapperLookup lookup, RegistryKey<Registry<V>> registryKey, Function<V, RegistryEntry<V>> entryFunction) {
            this.valueRegistry = lookup.getOrThrow(registryKey);
            this.entryFunction = entryFunction;
            this.components = lookup.getOrThrow(RegistryKeys.DATA_COMPONENT_TYPE);
            this.subPredicateTypes = lookup.getOrThrow(RegistryKeys.DATA_COMPONENT_PREDICATE_TYPE);
            this.key = registryKey;
            this.lookup = lookup;
        }

        @Override
        public Predicate<TransferVariant<V>> itemMatchPredicate(ImmutableStringReader reader, Identifier id) throws CommandSyntaxException {
            var entry = valueRegistry.getOptional(RegistryKey.of(key, id))
                    .orElseThrow(() -> INVALID_ITEM_ID_EXCEPTION.createWithContext(reader, id));
            return variant -> entryFunction.apply(variant.getObject()) == entry;
        }

        @Override
        public Stream<Identifier> streamItemIds() {
            return valueRegistry.streamKeys().map(RegistryKey::getValue);
        }

        @Override
        public Predicate<TransferVariant<V>> tagMatchPredicate(ImmutableStringReader reader, Identifier id) throws CommandSyntaxException {
            var tag = valueRegistry.getOptional(TagKey.of(key, id))
                    .orElseThrow(() -> UNKNOWN_ITEM_TAG_EXCEPTION.createWithContext(reader, id));
            return variant -> entryFunction.apply(variant.getObject()).isIn(tag.getTag());
        }

        @Override
        public Stream<Identifier> streamTags() {
            return valueRegistry.streamTagKeys().map(TagKey::id);
        }

        @Override
        public ComponentCheck componentCheck(ImmutableStringReader reader, Identifier id) throws CommandSyntaxException {
            var componentType = this.components
                    .getOptional(RegistryKey.of(RegistryKeys.DATA_COMPONENT_TYPE, id))
                    .map(RegistryEntry::value)
                    .orElseThrow(() -> UNKNOWN_ITEM_COMPONENT_EXCEPTION.createWithContext(reader, id));
            return ComponentCheck.read(reader, id, componentType);
        }

        @Override
        public Stream<Identifier> streamComponentIds() {
            return components.streamKeys().map(RegistryKey::getValue);
        }

        @Override
        public Predicate<TransferVariant<V>> componentMatchPredicate(ImmutableStringReader reader, ComponentCheck check, Dynamic<?> dynamic) throws CommandSyntaxException {
            return check.createPredicate(reader, wrapDynamic(dynamic))::test;
        }

        @Override
        public Predicate<TransferVariant<V>> componentPresencePredicate(ImmutableStringReader reader, ComponentCheck check) {
            return check.presenceChecker::test;
        }

        @Override
        public SubPredicateCheck subPredicateCheck(ImmutableStringReader reader, Identifier id) throws CommandSyntaxException {
            return this.subPredicateTypes
                    .getOptional(RegistryKey.of(RegistryKeys.DATA_COMPONENT_PREDICATE_TYPE, id))
                    .map(SubPredicateCheck::new)
                    .orElseThrow(() -> UNKNOWN_ITEM_PREDICATE_EXCEPTION.createWithContext(reader, id));

        }

        @Override
        public Stream<Identifier> streamSubPredicateIds() {
            return subPredicateTypes.streamKeys().map(RegistryKey::getValue);
        }

        @Override
        public Predicate<TransferVariant<V>> subPredicatePredicate(ImmutableStringReader reader, SubPredicateCheck check, Dynamic<?> dynamic) throws CommandSyntaxException {
            return check.createPredicate(reader, wrapDynamic(dynamic))::test;
        }

        @Override
        public Predicate<TransferVariant<V>> negate(Predicate<TransferVariant<V>> predicate) {
            return predicate.negate();
        }

        @Override
        public Predicate<TransferVariant<V>> anyOf(List<Predicate<TransferVariant<V>>> predicates) {
            return Util.anyOf(predicates);
        }

        private <T> Dynamic<T> wrapDynamic(Dynamic<T> dynamic) {
            return new Dynamic<>(lookup.getOps(dynamic.getOps()), dynamic.getValue());
        }
    }

    private record ComponentCheck(
            Identifier id,
            Predicate<? super TransferVariant<?>> presenceChecker,
            Decoder<? extends Predicate<? super TransferVariant<?>>> valueChecker
    ) {
        public static <T> ComponentCheck read(ImmutableStringReader reader,
                                              Identifier id,
                                              ComponentType<T> type) throws CommandSyntaxException {
            var codec = type.getCodec();
            if (codec == null) {
                throw UNKNOWN_ITEM_COMPONENT_EXCEPTION.createWithContext(reader, id);
            } else {
                return new ComponentCheck(id,
                        stack -> stack.getComponentMap().contains(type),
                        codec.map(expected -> stack -> Objects.equals(expected, stack.getComponentMap().get(type))));
            }
        }

        public Predicate<? super TransferVariant<?>> createPredicate(ImmutableStringReader reader, Dynamic<?> dynamic) throws CommandSyntaxException {
            return this.valueChecker.parse(dynamic).getOrThrow(
                    error -> MALFORMED_ITEM_COMPONENT_EXCEPTION.createWithContext(reader, this.id.toString(), error)
            );
        }
    }

    private record SubPredicateCheck(Identifier id, Decoder<? extends Predicate<? super TransferVariant<?>>> type) {
        public SubPredicateCheck(RegistryEntry.Reference<ComponentPredicate.Type<?>> type) {
            this(type.registryKey().getValue(), type.value().getPredicateCodec()
                    .map((predicate) -> v -> predicate.test(v.getComponentMap())));
        }

        public Predicate<? super TransferVariant<?>> createPredicate(ImmutableStringReader reader, Dynamic<?> value) throws CommandSyntaxException {
            return type.parse(value).getOrThrow((error) -> MALFORMED_ITEM_PREDICATE_EXCEPTION.createWithContext(reader, this.id.toString(), error));
        }
    }
}
