package io.github.aquerr.eaglefactions.api.entities;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;

import java.util.function.Consumer;

public class FactionChestCloseListener implements Consumer<InteractInventoryEvent.Close> {

    private final EagleFactions plugin;
    private final String factionName;

    public FactionChestCloseListener(final EagleFactions plugin, final String factionName)
    {
        this.plugin = plugin;
        this.factionName = factionName;
    }

    @Override
    public void accept(InteractInventoryEvent.Close event) {
        //event contains an Inventory that contains both player and chest inventory
        FactionChest factionChest = null;
        for (final Inventory inv : event.getTargetInventory()) {
            //Ensure that it is a chest.
            if (inv.capacity() == 27) {
                factionChest = FactionChest.fromInventory(this.factionName, inv);
                break;
            }
        }

        if(factionChest == null)
            factionChest = new FactionChest(this.factionName);
        final Faction faction = this.plugin.getFactionLogic().getFactionByName(this.factionName);
        if(faction != null)
        {
            this.plugin.getFactionLogic().setChest(faction, factionChest);
        }
    }
}
