package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.entities.Faction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.property.GuiIdProperty;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.type.GridInventory;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Iterator;
import java.util.Optional;

public class InventoryListener extends AbstractListener
{
    public InventoryListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener
    public void onInventoryClose(InteractInventoryEvent.Close event)
    {
//
//        User user = null;
//        if(event.getCause().containsType(Player.class))
//        {
//            user = event.getCause().first(Player.class).get();
//        }
//        else if(event.getCause().containsType(User.class))
//        {
//            user = event.getCause().first(User.class).get();
//        }
//
//        if(user == null)
//            return;
//
//        Optional<Faction> optionalFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(user.getUniqueId());
//        if(optionalFaction.isPresent())
//        {
////            super.getPlugin().getFactionLogic().setChest(optionalFaction.get(), inventory);
//        }
    }
}
