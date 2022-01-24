package io.github.aquerr.eaglefactions.storage.serializers;

import io.github.aquerr.eaglefactions.api.entities.FactionChest;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SlotItemListTypeSerializer implements TypeSerializer<List<FactionChest.SlotItem>>
{
    @Override
    public List<FactionChest.SlotItem> deserialize(Type type, ConfigurationNode node) throws SerializationException
    {
        final List<FactionChest.SlotItem> refillableItems = new ArrayList<>();
        final List<? extends ConfigurationNode> nodes = node.childrenList();
        for (final ConfigurationNode configurationNode : nodes)
        {
            final FactionChest.SlotItem refillableItem = configurationNode.get(EFTypeTokens.SLOT_ITEM_TYPE_TOKEN);
            refillableItems.add(refillableItem);
        }
        return refillableItems;    }

    @Override
    public void serialize(Type type, @Nullable List<FactionChest.SlotItem> obj, ConfigurationNode node) throws SerializationException
    {
        if (obj == null)
            return;

        for (final FactionChest.SlotItem refillableItem : obj)
        {
            final ConfigurationNode configurationNode = node.appendListNode();
            configurationNode.get(EFTypeTokens.SLOT_ITEM_TYPE_TOKEN, refillableItem);
        }
    }
}
