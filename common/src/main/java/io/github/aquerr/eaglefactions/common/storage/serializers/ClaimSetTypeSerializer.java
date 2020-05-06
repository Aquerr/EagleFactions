package io.github.aquerr.eaglefactions.common.storage.serializers;

import com.google.common.reflect.TypeToken;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClaimSetTypeSerializer implements TypeSerializer<Set<Claim>>
{
    @Override
    public Set<Claim> deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException
    {
        final Set<Claim> claims = new HashSet<>();
        final List<? extends ConfigurationNode> nodes = value.getChildrenList();
        for (final ConfigurationNode configurationNode : nodes)
        {
            final Claim claim = configurationNode.getValue(EFTypeSerializers.CLAIM_TYPE_TOKEN);
            if (claim != null)
                claims.add(claim);
        }
        return claims;
    }

    @Override
    public void serialize(TypeToken<?> type, Set<Claim> obj, ConfigurationNode value) throws ObjectMappingException
    {
        if (obj == null)
            return;

        for (final Claim claim : obj)
        {
            final ConfigurationNode configurationNode = value.getAppendedNode();
            configurationNode.setValue(EFTypeSerializers.CLAIM_TYPE_TOKEN, claim);
        }
    }
}
