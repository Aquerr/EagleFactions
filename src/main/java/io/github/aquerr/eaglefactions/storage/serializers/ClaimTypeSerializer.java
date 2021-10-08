package io.github.aquerr.eaglefactions.storage.serializers;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.reflect.TypeToken;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.util.TypeTokens;

import java.util.*;

public class ClaimTypeSerializer implements TypeSerializer<Claim>
{
    @Override
    public Claim deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException
    {
        Vector3i chunkPosition = Vector3i.ZERO;
        UUID worldUniqueId = new UUID(0, 0);
        Set<UUID> owners;
        boolean isAccessibleByFaction;

        try
        {
            worldUniqueId = value.getNode("worldUUID").getValue(TypeTokens.UUID_TOKEN, new UUID(0, 0));
            chunkPosition = value.getNode("chunkPosition").getValue(TypeTokens.VECTOR_3I_TOKEN, Vector3i.ZERO);
            isAccessibleByFaction = value.getNode("accessibleByFaction").getBoolean(true);
            owners = new HashSet<>(value.getNode("owners").getList(TypeTokens.UUID_TOKEN, Collections.EMPTY_LIST));
        }
        catch (Exception e)
        {
            throw new ObjectMappingException("Could not deserialize the claim: " + worldUniqueId.toString() + "|" + chunkPosition, e);
        }

        return new Claim(worldUniqueId, chunkPosition, owners, isAccessibleByFaction);
    }

    @Override
    public void serialize(TypeToken<?> type, Claim obj, ConfigurationNode value) throws ObjectMappingException
    {
        if (obj == null)
            return;

        value.getNode("worldUUID").setValue(TypeTokens.UUID_TOKEN, obj.getWorldUUID());
        value.getNode("chunkPosition").setValue(TypeTokens.VECTOR_3I_TOKEN, obj.getChunkPosition());
        value.getNode("accessibleByFaction").setValue(obj.isAccessibleByFaction());
        value.getNode("owners").setValue(EFTypeSerializers.UUID_LIST_TYPE_TOKEN, new ArrayList<>(obj.getOwners()));
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
