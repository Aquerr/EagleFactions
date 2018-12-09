package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.entities.Faction;
import org.checkerframework.checker.nullness.Opt;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class InventoryListener extends AbstractListener
{
    public InventoryListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener
    public void onInventoryClose(org.spongepowered.api.event.item.inventory.InteractInventoryEvent.Close event)
    {
        Container container = event.getTargetInventory();
        User user = null;
        if(event.getCause().containsType(Player.class))
        {
            user = event.getCause().first(Player.class).get();
        }
        else if(event.getCause().containsType(User.class))
        {
            user = event.getCause().first(User.class).get();
        }

        if(user == null)
            return;

        Inventory inventory = container.root();
        Optional<InventoryTitle> text = inventory.getInventoryProperty(InventoryTitle.class);
        if(text.isPresent() && text.get().getValue().equals(Text.of(TextColors.BLUE, "Faction's chest")))
        {
            //TOOD: Do Something with the chest...
            Optional<Faction> optionalFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(user.getUniqueId());
            if(optionalFaction.isPresent())
            {
                super.getPlugin().getFactionLogic().setChest(optionalFaction.get(), inventory);
            }
        }
    }
}
