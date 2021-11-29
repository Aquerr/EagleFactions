package io.github.aquerr.eaglefactions.storage.serializers;

import com.google.common.reflect.TypeToken;
import io.github.aquerr.eaglefactions.api.entities.FactionChest;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SlotItemListTypeSerializer implements TypeSerializer<List<FactionChest.SlotItem>>
{
    @Nullable
    @Override
    public List<FactionChest.SlotItem> deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException
    {
        final List<FactionChest.SlotItem> refillableItems = new ArrayList<>();
        final List<? extends ConfigurationNode> nodes = value.getChildrenList();
        for (final ConfigurationNode configurationNode : nodes)
        {
            final FactionChest.SlotItem refillableItem = configurationNode.getValue(EFTypeSerializers.SLOT_ITEM_TYPE_TOKEN);
            refillableItems.add(refillableItem);
        }
        return refillableItems;
    }

    @Override
    public void serialize(@NonNull TypeToken<?> type, @Nullable List<FactionChest.SlotItem> obj, @NonNull ConfigurationNode value) throws ObjectMappingException
    {
        if (obj == null)
            return;

        for (final FactionChest.SlotItem refillableItem : obj)
        {
            final ConfigurationNode configurationNode = value.getAppendedNode();
            configurationNode.setValue(EFTypeSerializers.SLOT_ITEM_TYPE_TOKEN, refillableItem);
        }
    }
}
