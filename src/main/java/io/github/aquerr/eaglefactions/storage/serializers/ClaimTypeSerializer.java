package io.github.aquerr.eaglefactions.storage.serializers;

import io.github.aquerr.eaglefactions.api.entities.Claim;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import org.spongepowered.math.vector.Vector3i;

import java.lang.reflect.Type;
import java.util.*;

public class ClaimTypeSerializer implements TypeSerializer<Claim>
{
    @Override
    public Claim deserialize(Type type, ConfigurationNode node) throws SerializationException
    {
        Set<UUID> owners;
        boolean isAccessibleByFaction;
        UUID worldUniqueId = node.node("worldUUID").get(EFTypeTokens.UUID_TOKEN, new UUID(0, 0));
        Vector3i chunkPosition = node.node("chunkPosition").get(EFTypeTokens.VECTOR_3I_TOKEN, Vector3i.ZERO);
        isAccessibleByFaction = node.node("accessibleByFaction").getBoolean(true);
        owners = new HashSet<>(node.node("owners").getList(EFTypeTokens.UUID_TOKEN, Collections.emptyList()));

        return new Claim(worldUniqueId, chunkPosition, owners, isAccessibleByFaction);
    }

    @Override
    public void serialize(Type type, @Nullable Claim obj, ConfigurationNode node) throws SerializationException
    {
        if (obj == null)
            return;

        node.node("worldUUID").set(EFTypeTokens.UUID_TOKEN, obj.getWorldUUID());
        node.node("chunkPosition").set(EFTypeTokens.VECTOR_3I_TOKEN, obj.getChunkPosition());
        node.node("accessibleByFaction").set(obj.isAccessibleByFaction());
        node.node("owners").set(EFTypeTokens.UUID_LIST_TYPE_TOKEN, new ArrayList<>(obj.getOwners()));
    }

    public static Vector3i deserializeVector3i(String vectorAsString)
    {
        Objects.requireNonNull(vectorAsString);

        String[] vectors = vectorAsString.replace("(", "").replace(")", "").replace(" ", "").split(",");
        int x = Integer.parseInt(vectors[0]);
        int y = Integer.parseInt(vectors[1]);
        int z = Integer.parseInt(vectors[2]);
        return Vector3i.from(x, y, z);
    }
}
