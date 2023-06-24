package io.github.aquerr.eaglefactions.model;

import io.github.aquerr.eaglefactions.api.entities.FactionChest;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class FactionChestImpl implements FactionChest
{
    private String factionName; //Reference to faction holding this chest

    private NonNullList<ItemStack> items = NonNullList.create();
//    private Supplier<List<ItemStack>> inventorySupplier;
//    private List<ItemStack> rawInventory;

    public FactionChestImpl(final String factionName)
    {
        this.factionName = factionName;
//        this.inventorySupplier = () -> {
//            if (this.rawInventory != null)
//                return this.rawInventory;
//            this.rawInventory = buildInventory(new ArrayList<>());
//            return this.rawInventory;
//        };
    }

    public FactionChestImpl(final String factionName, final List<SlotItem> items)
    {
        this.factionName = factionName;
//        this.inventorySupplier = () -> {
//            if (this.rawInventory != null)
//                return this.rawInventory;
//            this.rawInventory = buildInventory(items);
//            return this.rawInventory;
//        };
    }

    public FactionChestImpl(final String factionName, final Inventory inventory)
    {
        this.factionName = factionName;
//        this.inventorySupplier = () -> {
//            if (this.rawInventory != null)
//                return this.rawInventory;
//            this.rawInventory = buildInventory(toSlotItems(inventory));
//            return this.rawInventory;
//        };
    }

    @Override
    public String getFactionName()
    {
        return this.factionName;
    }

    @Override
    public List<SlotItem> getItems()
    {
        return List.copyOf(asSlotItems());
//        return ofNullable(this.inventorySupplier.get())
//                .map(this::toSlotItems)
//                .orElse(Collections.emptyList());
    }

    private List<SlotItem> asSlotItems()
    {
        int column = 0;
        int row = 1;
        Iterator<ItemStack> itemStackIterator = this.items.iterator();
        List<SlotItem> slotItems = new ArrayList<>();
        while (itemStackIterator.hasNext())
        {
            column++;
            slotItems.add(new SlotItemImpl(column, row, itemStackIterator.next().copy()));

            if (column > 9)
            {
                column = 1;
                row++;
            }
        }
        return slotItems;
    }

//    @Override
//    public Inventory getInventory()
//    {
//        return ;
//    }

//    private Inventory buildInventory(final List<SlotItem> slotItems)
//    {
//        FactionChest factionChest = this;

        //Create inventory
//        InventoryMenu inventoryMenu = ChestMenu.threeRows(this.hashCode(), );

//        Inventory inventoryMenu = new InventoryMenu(ViewableInventory.builder()
//                .type(ContainerTypes.GENERIC_9X3)
//                .completeStructure()
//                .identity(UUID.randomUUID())
//                .plugin(EagleFactionsPlugin.getPlugin().getPluginContainer())
//                .build());
//        inventoryMenu.setTitle(Component.text("Faction's chest", NamedTextColor.BLUE));
//        inventoryMenu.registerClose((cause, container) ->
//        {
//            final Faction faction = EagleFactionsPlugin.getPlugin().getFactionLogic().getFactionByName(factionName);
//            if(faction != null)
//            {
//                EagleFactionsPlugin.getPlugin().getFactionLogic().setChest(faction, factionChest);
//            }
//        });
//
//        //Fill it with items
//        int column = 1;
//        int row = 1;
//
//        for(final Inventory slot : inventoryMenu.inventory().slots())
//        {
//            ItemStack itemStack = getAtPosition(slotItems, row, column);
//            if (itemStack != null)
//                slot.offer(itemStack);
//
//            column++;
//            if (column > 9)
//            {
//                column = 1;
//                row++;
//            }
//        }
//        return inventoryMenu;
//    }

//    private List<SlotItem> toSlotItems(final Inventory inventory)
//    {
//        if (inventory == null)
//            return Collections.emptyList();
//
//        final List<FactionChest.SlotItem> slotItemList = new ArrayList<>();
//        final List<Slot> slots = inventory.slots();
//        int column = 1;
//        int row = 1;
//        for(Inventory slot : slots)
//        {
//            ItemStack itemStack = slot.peek();
//            if(itemStack != ItemStack.empty())
//            {
//                slotItemList.add(new FactionChestImpl.SlotItemImpl(column, row, itemStack));
//            }
//
//            column++;
//            if(column > 9)
//            {
//                row++;
//                column = 1;
//            }
//
//            if(row > 3)
//                break;
//        }
//        return slotItemList;
//    }

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

    public static final class SlotItemImpl implements FactionChest.SlotItem
    {
        private int column;
        private int row;
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
