package dev.Fjc.ultraBans.builders.data;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class MutedDataType implements PersistentDataType<byte[], String> {

    @Override
    public @NotNull Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public @NotNull Class<String> getComplexType() {
        return String.class;
    }

    @Override
    public byte @NotNull [] toPrimitive(@NotNull String complex, @NotNull PersistentDataAdapterContext context) {
        return complex.getBytes();
    }

    @Override
    public @NotNull String fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
        return Arrays.toString(primitive);
    }
}
