package io.github.aquerr.eaglefactions.storage.serializers;

import io.github.aquerr.eaglefactions.api.entities.FactionMember;
import io.github.aquerr.eaglefactions.entities.FactionMemberImpl;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FactionMemberTypeSerializer implements TypeSerializer<FactionMember>
{

    @Override
    public FactionMember deserialize(Type type, ConfigurationNode node) throws SerializationException
    {
        UUID uuid = node.node("uuid").get(EFTypeTokens.UUID_TOKEN, (UUID) null);
        Set<String> rankNames = new HashSet<>(node.node("ranks").getList(String.class, Collections.emptyList()));
        return new FactionMemberImpl(uuid, rankNames);
    }

    @Override
    public void serialize(Type type, @Nullable FactionMember obj, ConfigurationNode node) throws SerializationException
    {
        if (obj == null)
            return;

        node.node("uuid").set(obj.getUniqueId().toString());
        node.node("ranks").setList(String.class, new ArrayList<>(obj.getRankNames()));
    }
}
