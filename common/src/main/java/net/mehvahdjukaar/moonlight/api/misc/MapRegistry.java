package net.mehvahdjukaar.moonlight.api.misc;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class MapRegistry<T> implements Codec<T> {
    private final BiMap<ResourceLocation, T> map = HashBiMap.create();
    private final String name;

    public MapRegistry(String name) {
        this.name = name;
    }

    @Deprecated(forRemoval = true)
    public MapRegistry() {
        this.name = "unnamed";
    }

    public static <B> CodecMapRegistry<B> ofCodec(String name) {
        return new CodecMapRegistry<>(name);
    }

    public static <B> CodecMapRegistry<B> ofCodec() {
        return new CodecMapRegistry<>("unnamed");
    }


    public <E> Codec<E> dispatch(Function<? super E, ? extends T> type) {
        return Codec.super.dispatch(type, c -> (Codec<E>) c);
    }

    public <B extends T> T register(ResourceLocation name, B value) {
        this.map.put(name, value);
        return value;
    }

    public <B extends T> T register(String name, B value) {
        this.register(new ResourceLocation(name), value);
        return value;
    }

    @Nullable
    public T getValue(ResourceLocation name) {
        return this.map.get(name);
    }

    @Nullable
    public T getValue(String name) {
        return this.getValue(new ResourceLocation(name));
    }

    @Nullable
    public ResourceLocation getKey(T value) {
        return this.map.inverse().get(value);
    }

    public Set<ResourceLocation> keySet() {
        return this.map.keySet();
    }

    public Set<T> getValues() {
        return this.map.values();
    }

    public Set<Map.Entry<ResourceLocation, T>> getEntries() {
        return this.map.entrySet();
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    public int size() {
        return this.map.size();
    }

    public boolean containsKey(ResourceLocation name) {
        return this.map.containsKey(name);
    }

    public <U> DataResult<Pair<T, U>> decode(DynamicOps<U> ops, U json) {
        return ResourceLocation.CODEC.decode(ops, json).flatMap(pair -> {
            ResourceLocation id = pair.getFirst();
            T value = this.getValue(id);
            return value == null ? DataResult.error(() -> "Could not find any entry with key '" + id + "' in registry [" + name + "] \n Known keys: " + this.keySet()) :
                    DataResult.success(Pair.of(value, pair.getSecond()));
        });
    }

    public <U> DataResult<U> encode(T object, DynamicOps<U> ops, U prefix) {
        ResourceLocation id = this.getKey(object);
        return id == null ? DataResult.error(() -> "Could not find element " + object + " in registry" + name) :
                ops.mergeToPrimitive(prefix, ops.createString(id.toString()));
    }

    public void clear() {
        this.map.clear();
    }
}
