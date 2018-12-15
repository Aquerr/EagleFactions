package io.github.aquerr.eaglefactions.entities;

import io.github.aquerr.eaglefactions.EagleFactions;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FactionChest
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
                .listener(InteractInventoryEvent.Close.class, (x) ->{
                    List<FactionChest.SlotItem> slotItemList = new ArrayList<>();

                    Iterable<Inventory> slots = x.getTargetInventory().slots();
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
                    }

                    Optional<Faction> optionalFaction = EagleFactions.getPlugin().getFactionLogic().getFactionByPlayerUUID(x.getCause().first(User.class).get().getUniqueId());
                    if(optionalFaction.isPresent())
                    {
                        EagleFactions.getPlugin().getFactionLogic().setChest(optionalFaction.get(), new FactionChest(slotItemList));
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
    public static class SlotItem
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
