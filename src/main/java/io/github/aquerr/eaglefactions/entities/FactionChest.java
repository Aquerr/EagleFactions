package io.github.aquerr.eaglefactions.entities;

import io.github.aquerr.eaglefactions.EagleFactions;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.*;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class FactionChest implements Serializable
{
    private List<SlotItem> items;

    public FactionChest()
    {
        items = new ArrayList<>();
    }

    public FactionChest(List<SlotItem> items)
    {
        this.items = items;
    }

    public static FactionChest fromInventory(Inventory inventory)
    {
        List<FactionChest.SlotItem> slotItemList = new ArrayList<>();

        Iterable<Inventory> slots = inventory.slots();
        int column = 1;
        int row = 1;
        for(Inventory slot : slots)
        {
            Optional<ItemStack> optionalItemStack = slot.peek();
            if(optionalItemStack.isPresent())
            {
                slotItemList.add(new FactionChest.SlotItem(column, row, optionalItemStack.get()));
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

        return new FactionChest(slotItemList);
    }

    public List<SlotItem> getItems()
    {
        return this.items;
    }

    public ItemStack getAtPosition(int row, int column)
    {
        ItemStack itemStack = null;
        for(SlotItem slotItem : this.items)
        {
            if(slotItem.getRow() == row && slotItem.getColumn() == column)
                itemStack = ItemStack.of(slotItem.getItem().getType(), slotItem.getItem().getQuantity());
        }

        return itemStack;
    }

    public Inventory toInventory()
    {
        //Create inventory
        Inventory inventory = Inventory.builder()
                .of(InventoryArchetypes.CHEST)
                .property(InventoryTitle.of(Text.of(TextColors.BLUE, Text.of("Faction's chest"))))
                .listener(InteractInventoryEvent.Close.class, (x) ->
                {
                    // x is actually an Inventory that contains both player and chest inventory
                    FactionChest factionChest = null;
                    Iterator<Inventory> inventoryIterator = x.getTargetInventory().iterator();
                    while(inventoryIterator.hasNext())
                    {
                        Inventory inv = inventoryIterator.next();
                        //Ensure that it is a chest.
                        if(inv.capacity() == 27)
                        {
                            factionChest = FactionChest.fromInventory(inv);
                        }
                    }

                    if(factionChest == null)
                        factionChest = new FactionChest();
                    Optional<Faction> optionalFaction = EagleFactions.getPlugin().getFactionLogic().getFactionByPlayerUUID(x.getCause().first(User.class).get().getUniqueId());
                    if(optionalFaction.isPresent())
                    {
                        EagleFactions.getPlugin().getFactionLogic().setChest(optionalFaction.get(), factionChest);
                    }
                })
                .build(EagleFactions.getPlugin());

        //Fill it with items
        int column = 1;
        int row = 1;

        for(Inventory slot : inventory.slots())
        {
            ItemStack itemStack = getAtPosition(row, column);
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

    @ConfigSerializable
    public static class SlotItem implements Serializable
    {
        @Setting
        private int column;
        @Setting
        private int row;
        @Setting
        private ItemStack item;

        public SlotItem()
        {

        }

        public SlotItem(int column, int row, ItemStack item)
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
