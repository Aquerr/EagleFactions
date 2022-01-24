//package io.github.aquerr.eaglefactions.storage.serializers;
//
//import com.google.common.collect.Lists;
//import com.google.common.reflect.TypeToken;
//import io.github.aquerr.eaglefactions.api.entities.FactionChest;
//import io.github.aquerr.eaglefactions.entities.FactionChestImpl;
//import ninja.leaping.configurate.ConfigurationNode;
//import ninja.leaping.configurate.objectmapping.ObjectMappingException;
//import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
//import org.checkerframework.checker.nullness.qual.NonNull;
//import org.checkerframework.checker.nullness.qual.Nullable;
//import org.spongepowered.api.Sponge;
//import org.spongepowered.api.data.DataContainer;
//import org.spongepowered.api.data.DataQuery;
//import org.spongepowered.api.data.DataView;
//import org.spongepowered.api.data.key.Keys;
//import org.spongepowered.api.data.persistence.DataContainer;
//import org.spongepowered.api.data.persistence.DataFormats;
//import org.spongepowered.api.data.persistence.DataQuery;
//import org.spongepowered.api.data.persistence.DataTranslator;
//import org.spongepowered.api.data.persistence.DataTranslators;
//import org.spongepowered.api.item.ItemType;
//import org.spongepowered.api.item.ItemTypes;
//import org.spongepowered.api.item.inventory.ItemStack;
//import org.spongepowered.api.util.Tuple;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.Set;
//import java.util.stream.Collectors;
//
///**
// * This class has been mostly copied from Nuclues.
// *
// * <p>See https://github.com/NucleusPowered/Nucleus/blob/sponge-api/7/src/main/java/io/github/nucleuspowered/nucleus/configurate/typeserialisers/NucleusItemStackSnapshotSerialiser.java</p>
// *
// * <p>This class, as such, is copyrighted (c) by NucleusPowered team and Nucleus contributors.</p>
// */
//public class SlotItemTypeSerializer implements TypeSerializer<FactionChest.SlotItem>
//{
//    @Override
//    public FactionChest.SlotItem deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException
//    {
//        final int column = value.getNode("column").getInt();
//        final int row = value.getNode("row").getInt();
//        final ConfigurationNode itemNode = value.getNode("item");
//
//        boolean emptyEnchant = false;
//        ConfigurationNode ench = itemNode.getNode("UnsafeData", "ench");
//        if (!ench.isVirtual())
//        {
//            List<? extends ConfigurationNode> enchantments = ench.getChildrenList();
//            if (enchantments.isEmpty())
//            {
//                // Remove empty enchantment list.
//                itemNode.getNode("UnsafeData").removeChild("ench");
//            }
//            else
//            {
//                enchantments.forEach(x -> {
//                    try
//                    {
//                        short id = Short.parseShort(x.getNode("id").getString());
//                        short lvl = Short.parseShort(x.getNode("lvl").getString());
//
//                        x.getNode("id").setValue(id);
//                        x.getNode("lvl").setValue(lvl);
//                    }
//                    catch (NumberFormatException e)
//                    {
//                        x.setValue(null);
//                    }
//                });
//            }
//        }
//
//        ConfigurationNode data = itemNode.getNode("Data");
//        if (!data.isVirtual() && data.hasListChildren())
//        {
//            List<? extends ConfigurationNode> n = data.getChildrenList().stream()
//                    .filter(x ->
//                            !x.getNode("DataClass").getString("").endsWith("SpongeEnchantmentData")
//                                    || (!x.getNode("ManipulatorData", "ItemEnchantments").isVirtual() && x.getNode("ManipulatorData", "ItemEnchantments").hasListChildren()))
//                    .collect(Collectors.toList());
//            emptyEnchant = n.size() != data.getChildrenList().size();
//
//            if (emptyEnchant)
//            {
//                if (n.isEmpty())
//                {
//                    itemNode.removeChild("Data");
//                }
//                else
//                {
//                    itemNode.getNode("Data").setValue(n);
//                }
//            }
//        }
//
//        DataContainer dataContainer = DataFormats.CONFIGURATION_NODE.translate(itemNode);
//        Set<DataQuery> ldq = dataContainer.getKeys(true);
//
//        for (DataQuery dataQuery : ldq)
//        {
//            String el = dataQuery.asString(".");
//            if (el.contains("$Array$"))
//            {
//                try
//                {
//                    Tuple<DataQuery, Object> r = TypeHelper.getArray(dataQuery, dataContainer);
//                    dataContainer.set(r.getFirst(), r.getSecond());
//                }
//                catch (Exception e)
//                {
//                    e.printStackTrace();
//                }
//
//                dataContainer.remove(dataQuery);
//            }
//        }
//
//        final Optional<ItemType> itemType = Sponge.getRegistry().getType(ItemType.class, String.valueOf(dataContainer.get(DataQuery.of("ItemType")).get()));
//        if (!itemType.isPresent())
//        {
//            throw new ObjectMappingException("ItemType could not be recognized. Probably comes from a mod that has been removed from the server.");
//        }
//
//        ItemStack itemStack;
//        try
//        {
//            itemStack = ItemStack.builder().fromContainer(dataContainer).build();
//        }
//        catch (Exception e)
//        {
//            throw new ObjectMappingException("Could not create Item Stack from data container.");
//        }
//
//        // Validate the item.
//        if (itemStack.isEmpty() || itemStack.getType() == ItemTypes.NONE)
//        {
//            // don't bother
//            throw new ObjectMappingException("Could not deserialize item. Item is empty.");
//        }
//
//        if (emptyEnchant)
//        {
//            itemStack.offer(Keys.ITEM_ENCHANTMENTS, Lists.newArrayList());
//            return new FactionChestImpl.SlotItemImpl(column, row, itemStack);
//        }
//
//        if (itemStack.get(Keys.ITEM_ENCHANTMENTS).isPresent())
//        {
//            // Reset the data.
//            itemStack.offer(Keys.ITEM_ENCHANTMENTS, itemStack.get(Keys.ITEM_ENCHANTMENTS).get());
//            return new FactionChestImpl.SlotItemImpl(column, row, itemStack);
//        }
//
//        return new FactionChestImpl.SlotItemImpl(column, row, itemStack);
//    }
//
//    @Override
//    public void serialize(@NonNull TypeToken<?> type, FactionChest.SlotItem obj, @NonNull ConfigurationNode value) throws ObjectMappingException
//    {
//        if (obj == null)
//            return;
//
//        final ItemStack itemStack = obj.getItem().copy();
//        DataView view;
//        try
//        {
//            view = itemStack.toContainer();
//        }
//        catch (NullPointerException e)
//        {
//            System.out.println("BOOM!");
//            return;
//        }
//
//        final Map<DataQuery, Object> dataQueryObjectMap = view.getValues(true);
//        for (final Map.Entry<DataQuery, Object> entry : dataQueryObjectMap.entrySet())
//        {
//            if (entry.getValue().getClass().isArray())
//            {
//                if (entry.getValue().getClass().getComponentType().isPrimitive())
//                    {
//                    DataQuery old = entry.getKey();
//                    Tuple<DataQuery, List<?>> dqo = TypeHelper.getList(old, entry.getValue());
//                    view.remove(old);
//                    view.set(dqo.getFirst(), dqo.getSecond());
//                }
//                else
//                {
//                    view.set(entry.getKey(), Lists.newArrayList((Object[]) entry.getValue()));
//                }
//            }
//        }
//
//        value.getNode("column").setValue(obj.getColumn());
//        value.getNode("row").setValue(obj.getRow());
//        value.getNode("item").setValue(DataTranslators.CONFIGURATION_NODE.translate(view));
//    }
//}
