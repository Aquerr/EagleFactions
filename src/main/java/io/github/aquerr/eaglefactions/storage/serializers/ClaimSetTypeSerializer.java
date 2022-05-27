package io.github.aquerr.eaglefactions.storage.serializers;

import io.github.aquerr.eaglefactions.api.entities.Claim;
import org.checkerframework.checker.nullness.qual.Nullable;
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
    public Set<Claim> deserialize(Type type, ConfigurationNode node) throws SerializationException
    {
        final Set<Claim> claims = new HashSet<>();
        final List<? extends ConfigurationNode> nodes = node.childrenList();
        for (final ConfigurationNode configurationNode : nodes)
        {
            final Claim claim = configurationNode.get(EFTypeTokens.CLAIM_TYPE_TOKEN);
            if (claim != null)
                claims.add(claim);
        }
        return claims;
    }

    @Override
    public void serialize(Type type, @Nullable Set<Claim> obj, ConfigurationNode node) throws SerializationException
    {
        if (obj == null)
            return;

        for (final Claim claim : obj)
        {
            final ConfigurationNode configurationNode = node.appendListNode();
            configurationNode.set(EFTypeTokens.CLAIM_TYPE_TOKEN, claim);
        }
    }
}
