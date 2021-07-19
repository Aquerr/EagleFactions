package io.github.aquerr.eaglefactions.storage.serializers;

import com.google.common.base.Preconditions;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.leangen.geantyref.TypeToken;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import org.spongepowered.math.vector.Vector3i;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ClaimTypeSerializer implements TypeSerializer<Claim>
{
    @Override
    public Claim deserialize(Type type, ConfigurationNode value) throws SerializationException
    {
        Vector3i chunkPosition = Vector3i.ZERO;
        UUID worldUniqueId = new UUID(0, 0);
        Set<UUID> owners;
        boolean isAccessibleByFaction;

        try
        {
            worldUniqueId = value.node("worldUUID").get(TypeToken.get(UUID.class), new UUID(0, 0));
            chunkPosition = value.node("chunkPosition").get(TypeToken.get(Vector3i.class), Vector3i.ZERO);
            isAccessibleByFaction = value.node("accessibleByFaction").getBoolean(true);
            owners = new HashSet<>(value.node("owners").getList(TypeToken.get(UUID.class), Collections.EMPTY_LIST));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new SerializationException("Could not deserialize the claim: " + worldUniqueId.toString() + "|" + chunkPosition);
        }

        return new Claim(worldUniqueId, chunkPosition, owners, isAccessibleByFaction);
    }

    @Override
    public void serialize(Type type, Claim obj, ConfigurationNode value) throws SerializationException
    {
        if (obj == null)
            return;

        value.node("worldUUID").set(TypeToken.get(UUID.class), obj.getWorldUUID());
        value.node("chunkPosition").set(TypeToken.get(Vector3i.class), obj.getChunkPosition());
        value.node("accessibleByFaction").set(obj.isAccessibleByFaction());
        value.node("owners").set(EFTypeSerializers.UUID_LIST_TYPE_TOKEN, new ArrayList<>(obj.getOwners()));
    }

    public static Vector3i deserializeVector3i(String vectorAsString)
    {
        Preconditions.checkNotNull(vectorAsString);

        String[] vectors = vectorAsString.replace("(", "").replace(")", "").replace(" ", "").split(",");
        int x = Integer.parseInt(vectors[0]);
        int y = Integer.parseInt(vectors[1]);
        int z = Integer.parseInt(vectors[2]);
        return Vector3i.from(x, y, z);
    }
}
