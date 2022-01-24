package io.github.aquerr.eaglefactions.storage.serializers;

import com.google.common.collect.Lists;
import io.github.aquerr.eaglefactions.api.entities.FactionChest;
import io.github.aquerr.eaglefactions.entities.FactionChestImpl;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackComparators;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class has been mostly copied from Nuclues.
 *
 * <p>See https://github.com/NucleusPowered/Nucleus/blob/sponge-api/7/src/main/java/io/github/nucleuspowered/nucleus/configurate/typeserialisers/NucleusItemStackSnapshotSerialiser.java</p>
 *
 * <p>This class, as such, is copyrighted (c) by NucleusPowered team and Nucleus contributors.</p>
 */
public class SlotItemTypeSerializer implements TypeSerializer<FactionChest.SlotItem>
{
    @Override
    public FactionChest.SlotItem deserialize(Type type, ConfigurationNode node) throws SerializationException
    {
        final int column = node.node("column").getInt();
        final int row = node.node("row").getInt();
        final ConfigurationNode itemNode = node.node("item");

        boolean emptyEnchant = false;
        ConfigurationNode ench = itemNode.node("UnsafeData", "ench");
        if (!ench.virtual())
        {
            List<? extends ConfigurationNode> enchantments = ench.childrenList();
            if (enchantments.isEmpty())
            {
                // Remove empty enchantment list.
                itemNode.node("UnsafeData").removeChild("ench");
            }
            else
            {
                enchantments.forEach(x -> {
                    try
                    {
                        short id = Short.parseShort(x.node("id").getString());
                        short lvl = Short.parseShort(x.node("lvl").getString());

                        x.node("id").set(id);
                        x.node("lvl").set(lvl);
                    }
                    catch (NumberFormatException | SerializationException e)
                    {
                        try
                        {
                            x.set(null);
                        }
                        catch (SerializationException ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        }

        ConfigurationNode data = itemNode.node("Data");
        if (!data.virtual() && data.isList())
        {
            List<? extends ConfigurationNode> n = data.childrenList().stream()
                    .filter(x ->
                            !x.node("DataClass").getString("").endsWith("SpongeEnchantmentData")
                                    || (!x.node("ManipulatorData", "ItemEnchantments").virtual() && x.node("ManipulatorData", "ItemEnchantments").isList()))
                    .collect(Collectors.toList());
            emptyEnchant = n.size() != data.childrenList().size();

            if (emptyEnchant)
            {
                if (n.isEmpty())
                {
                    itemNode.removeChild("Data");
                }
                else
                {
                    itemNode.node("Data").set(n);
                }
            }
        }

        DataContainer dataContainer = null;
        try
        {
            dataContainer = DataFormats.HOCON.get().read(itemNode.getString());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        Set<DataQuery> ldq = dataContainer.keys(true);

        for (DataQuery dataQuery : ldq)
        {
            String el = dataQuery.asString(".");
            if (el.contains("$Array$"))
            {
                try
                {
                    Tuple<DataQuery, Object> r = TypeHelper.getArray(dataQuery, dataContainer);
                    dataContainer.set(r.first(), r.second());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                dataContainer.remove(dataQuery);
            }
        }

        final Optional<ItemType> itemType = Sponge.game().registry(RegistryTypes.ITEM_TYPE).findEntry(ResourceKey.resolve(String.valueOf(dataContainer.get(DataQuery.of("ItemType")).get())))
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

        if (emptyEnchant)
        {
            itemStack.offer(Keys.APPLIED_ENCHANTMENTS, new ArrayList<>());
            return new FactionChestImpl.SlotItemImpl(column, row, itemStack);
        }

        if (itemStack.get(Keys.APPLIED_ENCHANTMENTS).isPresent())
        {
            // Reset the data.
            itemStack.offer(Keys.APPLIED_ENCHANTMENTS, itemStack.get(Keys.APPLIED_ENCHANTMENTS).get());
            return new FactionChestImpl.SlotItemImpl(column, row, itemStack);
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
            System.out.println("BOOM!");
            return;
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
            node.node("item").set(DataFormats.HOCON.get().write(view));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
