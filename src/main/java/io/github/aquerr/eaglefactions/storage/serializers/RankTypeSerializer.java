package io.github.aquerr.eaglefactions.storage.serializers;

import io.github.aquerr.eaglefactions.api.entities.FactionPermission;
import io.github.aquerr.eaglefactions.api.entities.Rank;
import io.github.aquerr.eaglefactions.entities.RankImpl;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class RankTypeSerializer implements TypeSerializer<Rank>
{
    @Override
    public Rank deserialize(Type type, ConfigurationNode node) throws SerializationException
    {
        String name = node.node("name").getString();
        String displayName = node.node("display_name").getString();
        int ladderPosition = node.node("ladder_position").getInt();
        Set<FactionPermission> permissions = node.node("permissions").getList(String.class, Collections.emptyList())
                .stream().map(FactionPermission::valueOf)
                .collect(Collectors.toSet());
        boolean displayInChat = node.node("display_in_chat").getBoolean(true);

        return RankImpl.builder()
                .name(name)
                .displayName(displayName)
                .ladderPosition(ladderPosition)
                .permissions(permissions)
                .displayInChat(displayInChat)
                .build();
    }

    @Override
    public void serialize(Type type, @Nullable Rank obj, ConfigurationNode node) throws SerializationException
    {
        if (obj == null)
            return;

        node.node("name").set(obj.getName());
        node.node("display_name").set(obj.getDisplayName());
        node.node("ladder_position").set(obj.getLadderPosition());
        node.node("permissions").setList(String.class, obj.getPermissions().stream().map(FactionPermission::name).collect(Collectors.toList()));
        node.node("display_in_chat").set(obj.canDisplayInChat());
    }
}
