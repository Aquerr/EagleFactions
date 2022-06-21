package io.github.aquerr.eaglefactions.storage.serializers;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import org.spongepowered.math.vector.Vector3i;

import java.lang.reflect.Type;

public class Vector3iTypeSerializer implements TypeSerializer<Vector3i>
{
    @Override
    public Vector3i deserialize(Type type, ConfigurationNode node) throws SerializationException
    {
        int x = node.node("x").getInt();
        int y = node.node("y").getInt();
        int z = node.node("z").getInt();
        return new Vector3i(x, y, z);
    }

    @Override
    public void serialize(Type type, @Nullable Vector3i obj, ConfigurationNode node) throws SerializationException
    {
        if (obj == null)
            return;

        node.node("x").set(obj.x());
        node.node("y").set(obj.y());
        node.node("z").set(obj.z());
    }
}
