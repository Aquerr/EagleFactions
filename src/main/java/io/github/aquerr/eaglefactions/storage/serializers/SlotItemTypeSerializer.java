package io.github.aquerr.eaglefactions.storage.serializers;

import com.google.common.collect.Lists;
import io.github.aquerr.eaglefactions.api.entities.FactionChest;
import io.github.aquerr.eaglefactions.entities.FactionChestImpl;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SlotItemTypeSerializer implements TypeSerializer<FactionChest.SlotItem>
{
    @Override
    public FactionChest.SlotItem deserialize(Type type, ConfigurationNode node) throws SerializationException
    {
        final int column = node.node("column").getInt();
        final int row = node.node("row").getInt();
        final ConfigurationNode itemNode = node.node("item");

        DataContainer dataContainer = null;
        try
        {
            String itemNodeAsString = HoconConfigurationLoader.builder().buildAndSaveString(itemNode);
            dataContainer = DataFormats.HOCON.get().read(itemNodeAsString);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        final Optional<ItemType> itemType = Sponge.game().registry(RegistryTypes.ITEM_TYPE)
                .findEntry(ResourceKey.resolve(String.valueOf(dataContainer.get(DataQuery.of("ItemType")).get())))
                .map(RegistryEntry::value);
        if (!itemType.isPresent())
        {
            throw new SerializationException("ItemType could not be recognized. Probably comes from a mod that has been removed from the server.");
        }

        ItemStack itemStack;
        try
        {
            itemStack = ItemStack.builder().fromContainer(dataContainer).build();
        }
        catch (Exception e)
        {
            throw new SerializationException("Could not create Item Stack from data container.");
        }

        // Validate the item.
        if (itemStack.isEmpty() || itemStack.type() == null)
        {
            // don't bother
            throw new SerializationException("Could not deserialize item. Item is empty.");
        }

        return new FactionChestImpl.SlotItemImpl(column, row, itemStack);
    }

    @Override
    public void serialize(Type type, FactionChest.@Nullable SlotItem obj, ConfigurationNode node) throws SerializationException
    {
        if (obj == null)
            return;

        final ItemStack itemStack = obj.getItem().copy();
        DataView view;
        try
        {
            view = itemStack.toContainer();
        }
        catch (NullPointerException e)
        {
            throw new SerializationException(e);
        }

        final Map<DataQuery, Object> dataQueryObjectMap = view.values(true);
        for (final Map.Entry<DataQuery, Object> entry : dataQueryObjectMap.entrySet())
        {
            if (entry.getValue().getClass().isArray())
            {
                if (entry.getValue().getClass().getComponentType().isPrimitive())
                {
                    DataQuery old = entry.getKey();
                    Tuple<DataQuery, List<?>> dqo = TypeHelper.getList(old, entry.getValue());
                    view.remove(old);
                    view.set(dqo.first(), dqo.second());
                }
                else
                {
                    view.set(entry.getKey(), Lists.newArrayList((Object[]) entry.getValue()));
                }
            }
        }

        node.node("column").set(obj.getColumn());
        node.node("row").set(obj.getRow());
        try
        {
            String itemStackAsString = DataFormats.HOCON.get().write(view);
            ConfigurationNode itemNode = HoconConfigurationLoader.builder().buildAndLoadString(itemStackAsString);
            node.node("item").set(itemNode);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
