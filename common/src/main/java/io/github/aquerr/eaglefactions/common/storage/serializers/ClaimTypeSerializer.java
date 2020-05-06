package io.github.aquerr.eaglefactions.common.storage.serializers;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.reflect.TypeToken;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClaimTypeSerializer implements TypeSerializer<Claim>
{
    @Override
    public Claim deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException
    {
        Vector3i chunkPosition = Vector3i.ZERO;
        UUID worldUniqueId = new UUID(0, 0);
        List<UUID> owners;
        boolean isAccessibleByFaction;

        try
        {
            final Object object = value.getKey();
            if (object instanceof String)
            {
                String[] worldUUIDAndChunkPosition = ((String) object).split("\\|");
                worldUniqueId = UUID.fromString(worldUUIDAndChunkPosition[0]);
                String[] vectors = worldUUIDAndChunkPosition[1].replace("(", "").replace(")", "").replace(" ", "").split(",");
                int x = Integer.parseInt(vectors[0]);
                int y = Integer.parseInt(vectors[1]);
                int z = Integer.parseInt(vectors[2]);
                chunkPosition = Vector3i.from(x, y, z);
            }
            isAccessibleByFaction = value.getNode("accessibleByFaction").getBoolean(true);
            owners = new ArrayList<>(value.getNode("owners").getList(TypeToken.of(UUID.class)));
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

        value.getNode(obj.getWorldUUID() + "|" + obj.getChunkPosition(), "accessibleByFaction").setValue(obj.isAccessibleByFaction());
        value.getNode(obj.getWorldUUID() + "|" + obj.getChunkPosition(), "owners").setValue(obj.getOwners());
//        value.getNode("worldUUID").setValue(obj.getWorldUUID());
//        value.getNode("chunkPosition").setValue(obj.getChunkPosition());
//        value.getNode("accessibleByFaction").setValue(obj.isAccessibleByFaction());
//        value.getNode("owners").setValue(obj.getOwners());
    }
}
