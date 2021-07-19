package io.github.aquerr.eaglefactions.storage.serializers;

import io.github.aquerr.eaglefactions.api.entities.Claim;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClaimSetTypeSerializer implements TypeSerializer<Set<Claim>>
{
    @Override
    public Set<Claim> deserialize(Type type, ConfigurationNode value) throws SerializationException
    {
        final Set<Claim> claims = new HashSet<>();
        final List<? extends ConfigurationNode> nodes = value.childrenList();
        for (final ConfigurationNode configurationNode : nodes)
        {
            final Claim claim = configurationNode.get(EFTypeSerializers.CLAIM_TYPE_TOKEN);
            if (claim != null)
                claims.add(claim);
        }
        return claims;
    }

    @Override
    public void serialize(Type type, Set<Claim> obj, ConfigurationNode value) throws SerializationException
    {
        if (obj == null)
            return;

        for (final Claim claim : obj)
        {
            final ConfigurationNode configurationNode = value.node();
            configurationNode.set(EFTypeSerializers.CLAIM_TYPE_TOKEN, claim);
        }
    }
}
