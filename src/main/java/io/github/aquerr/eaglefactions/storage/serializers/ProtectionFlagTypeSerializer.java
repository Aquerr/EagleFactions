package io.github.aquerr.eaglefactions.storage.serializers;

import io.github.aquerr.eaglefactions.api.entities.ProtectionFlag;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlagType;
import io.github.aquerr.eaglefactions.entities.ProtectionFlagImpl;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class ProtectionFlagTypeSerializer implements TypeSerializer<ProtectionFlag>
{
    @Override
    public ProtectionFlag deserialize(Type type, ConfigurationNode node) throws SerializationException
    {
        ProtectionFlagType protectionFlagType = ProtectionFlagType.valueOf(node.node("type").getString());
        boolean value = node.node("value").getBoolean();
        return new ProtectionFlagImpl(protectionFlagType, value);
    }

    @Override
    public void serialize(Type type, @Nullable ProtectionFlag obj, ConfigurationNode node) throws SerializationException
    {
        if (obj == null)
            return;

        node.node("type").set(obj.getType().getName());
        node.node("value").set(obj.getValue());
    }
}
