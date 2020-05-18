package io.github.aquerr.eaglefactions.common.entities;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionChest;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FactionChestImpl implements FactionChest
{
    private String factionName; //Reference to faction holding this chest
    private Inventory inventory;

    public FactionChestImpl(final String factionName)
    {
        this.factionName = factionName;
    }

    public FactionChestImpl(final String factionName, final List<SlotItem> items)
    {
        this.factionName = factionName;
        this.inventory = buildInventory(items);
    }

    public FactionChestImpl(final String factionName, final Inventory inventory)
    {
        this.factionName = factionName;
        this.inventory = buildInventory(toSlotItems(inventory));
    }

    @Override
    public String getFactionName()
    {
        return this.factionName;
    }

    @Override
    public List<SlotItem> getItems()
    {
        if(this.inventory == null)
            return new ArrayList<>();
        return toSlotItems(this.inventory);
    }

    @Override
    public Inventory getInventory()
    {
        if(this.inventory == null)
            this.inventory = buildInventory(new ArrayList<>());
        return this.inventory;
    }

    private Inventory buildInventory(final List<SlotItem> slotItems)
    {
        //Create inventory
        final Inventory inventory = Inventory.builder()
                .of(InventoryArchetypes.CHEST)
                .property(InventoryTitle.of(Text.of(TextColors.BLUE, Text.of("Faction's chest"))))
                .listener(InteractInventoryEvent.Close.class, (x) ->
                {
                    final Faction faction = EagleFactionsPlugin.getPlugin().getFactionLogic().getFactionByName(factionName);
                    if(faction != null)
                    {
                        EagleFactionsPlugin.getPlugin().getFactionLogic().setChest(faction, this);
                    }
                })
                .build(EagleFactionsPlugin.getPlugin());

        //Fill it with items
        int column = 1;
        int row = 1;

        for(final Inventory slot : inventory.slots())
        {
            ItemStack itemStack = getAtPosition(slotItems, row, column);
            if(itemStack != null)
                slot.offer(itemStack);

            column++;
            if(column > 9)
            {
                column = 1;
                row++;
            }
        }

        return inventory;
    }

    private List<SlotItem> toSlotItems(final Inventory inventory)
    {
        final List<FactionChest.SlotItem> slotItemList = new ArrayList<>();
        final Iterable<Inventory> slots = inventory.slots();
        int column = 1;
        int row = 1;
        for(Inventory slot : slots)
        {
            Optional<ItemStack> optionalItemStack = slot.peek();
            if(optionalItemStack.isPresent())
            {
                slotItemList.add(new FactionChestImpl.SlotItemImpl(column, row, optionalItemStack.get()));
            }

            column++;
            if(column > 9)
            {
                row++;
                column = 1;
            }

            if(row > 3)
                break;
        }
        return slotItemList;
    }

    private ItemStack getAtPosition(final List<SlotItem> items, int row, int column)
    {
        ItemStack itemStack = null;
        for(SlotItem slotItem : items)
        {
            if(slotItem.getRow() == row && slotItem.getColumn() == column)
                itemStack = slotItem.getItem().copy();
        }

        return itemStack;
    }

    @ConfigSerializable
    public static final class SlotItemImpl implements FactionChest.SlotItem
    {
        @Setting
        private int column;
        @Setting
        private int row;
        @Setting
        private ItemStack item;

        public SlotItemImpl()
        {

        }

        public SlotItemImpl(int column, int row, ItemStack item)
        {
            this.column = column;
            this.row = row;
            this.item = item;
        }

        public int getColumn()
        {
            return this.column;
        }

        public int getRow()
        {
            return this.row;
        }

        public ItemStack getItem()
        {
            return this.item;
        }
    }
}
