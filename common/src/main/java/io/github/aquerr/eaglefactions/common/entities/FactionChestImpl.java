package io.github.aquerr.eaglefactions.common.entities;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionChest;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import ninja.leaping.configurate.objectmapping.Setting;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.*;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FactionChestImpl implements FactionChest
{
    private String factionName; //Reference to faction holding this chest
    private List<SlotItem> items;
    private Inventory inventory;

    public FactionChestImpl(final String factionName)
    {
        this.factionName = factionName;
        items = new ArrayList<>();
    }

    public FactionChestImpl(final String factionName, final List<SlotItem> items)
    {
        this.factionName = factionName;
        this.items = items;
    }

    public FactionChestImpl(final String factionName, final Inventory inventory)
    {
        this.factionName = factionName;
        this.inventory = inventory;
    }

    public static FactionChest fromInventory(final String factionName, final Inventory inventory)
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

        return new FactionChestImpl(factionName, slotItemList);
    }

    public String getFactionName()
    {
        return this.factionName;
    }

    public List<FactionChest.SlotItem> getItems()
    {
        return this.items;
    }

    public ItemStack getAtPosition(int row, int column)
    {
        ItemStack itemStack = null;
        for(SlotItem slotItem : this.items)
        {
            if(slotItem.getRow() == row && slotItem.getColumn() == column)
                itemStack = ItemStack.builder().fromContainer(slotItem.getItem().toContainer()).build();
        }

        return itemStack;
    }

    public Inventory toInventory()
    {
        //Create inventory
        if(this.inventory != null)
            return this.inventory;

        this.inventory = Inventory.builder()
                .of(InventoryArchetypes.CHEST)
                .property(InventoryTitle.of(Text.of(TextColors.BLUE, Text.of("FactionImpl's chest"))))
//                .listener(InteractInventoryEvent.Close.class, new FactionChestCloseListener())
                .listener(InteractInventoryEvent.Close.class, (x) ->
                {
                    // x is actually an Inventory that contains both player and chest inventory
                    FactionChest factionChest = null;
                    for (final Inventory inv : x.getTargetInventory()) {
                        //Ensure that it is a chest.
                        if (inv.capacity() == 27) {
                            factionChest = FactionChestImpl.fromInventory(this.factionName, inv);
                            break;
                        }
                    }

                    if(factionChest == null)
                        factionChest = new FactionChestImpl(factionName);
                    final Faction faction = EagleFactionsPlugin.getPlugin().getFactionLogic().getFactionByName(factionName);
                    if(faction != null)
                    {
                        EagleFactionsPlugin.getPlugin().getFactionLogic().setChest(faction, factionChest);
                    }
                })
                .build(EagleFactionsPlugin.getPlugin());

        //Fill it with items
        int column = 1;
        int row = 1;

        for(final Inventory slot : inventory.slots())
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
