package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.managers.ProtectionResult;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.item.inventory.container.InteractContainerEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.BlockCarrier;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.server.ServerLocation;

import static net.kyori.adventure.text.format.NamedTextColor.DARK_PURPLE;

public class PlayerInteractListener extends AbstractListener
{
    public PlayerInteractListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onItemUse(final InteractItemEvent.Secondary event, @Root final ServerPlayer player)
    {
        if (event.itemStack() == ItemStackSnapshot.empty())
            return;

        ServerLocation location = player.serverLocation();
        final ProtectionResult protectionResult = super.getPlugin().getProtectionManager().canUseItem(location, player.user(), event.itemStack(), true);
        if (!protectionResult.hasAccess())
        {
            event.setCancelled(true);
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onEntityInteract(final InteractEntityEvent event, @Root final ServerPlayer player)
    {
        final Entity targetEntity = event.entity();

        if((targetEntity instanceof Living) && !(targetEntity instanceof ArmorStand))
            return;

        boolean canInteractWithEntity = super.getPlugin().getProtectionManager().canHitEntity(targetEntity, player, true).hasAccess();
        if(!canInteractWithEntity)
        {
            event.setCancelled(true);
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockInteract(final InteractBlockEvent.Secondary event, @Root final ServerPlayer player)
    {
        //If AIR or NONE then return
        if (event.block() == BlockSnapshot.empty() || event.block().state().type() == BlockTypes.AIR.get())
            return;

        event.block().location()
                .ifPresent(location -> checkInteraction(event, location, player, true));
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockInteract(final InteractBlockEvent.Primary event, @Root final ServerPlayer player)
    {
        //If AIR or NONE then return
        if (event.block() == BlockSnapshot.empty() || event.block().state().type() == BlockTypes.AIR.get())
            return;

        event.block().location()
                .ifPresent(location -> checkInteraction(event, location, player, true));
    }

    @Listener
    public void onInventoryOpenEvent(final InteractContainerEvent.Open event, final @Root ServerPlayer player)
    {
        BlockSnapshot blockSnapshot = event.context().get(EventContextKeys.BLOCK_HIT).orElse(null);
        if (blockSnapshot == null)
            return;

        ServerLocation location = blockSnapshot.location().orElse(null);
        if (location == null)
            return;

        ProtectionResult protectionResult = super.getPlugin().getProtectionManager().canInteractWithBlock(location, player.user(), true);
        if (!protectionResult.hasAccess())
        {
            event.setCancelled(true);
        }
        else
        {
            if (protectionResult.isEagleFeather())
                removeEagleFeather(player);
        }
    }

    private void checkInteraction(InteractBlockEvent interactBlockEvent, ServerLocation location, ServerPlayer player, boolean shouldNotify)
    {
        ItemStackSnapshot usedItem = interactBlockEvent.context().get(EventContextKeys.USED_ITEM)
                .filter(this::isItemStackNotAirAndNotEmpty)
                .orElse(null);
        ProtectionResult protectionResult;
        if (usedItem != null)
        {
            protectionResult = super.getPlugin().getProtectionManager().canUseItem(location, player.user(), usedItem, shouldNotify);
        }
        else
        {
            protectionResult = super.getPlugin().getProtectionManager().canInteractWithBlock(location, player.user(), shouldNotify);
        }

        if (!protectionResult.hasAccess())
        {
            ((Cancellable)interactBlockEvent).setCancelled(true);
        }
    }

    private void removeEagleFeather(final Player player)
    {
        final ItemStack feather = player.itemInHand(HandTypes.MAIN_HAND);
        feather.setQuantity(feather.quantity() - 1);
        player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text("You used Eagle's Feather!", DARK_PURPLE)));
    }

    private boolean isItemStackNotAirAndNotEmpty(ItemStackSnapshot itemStackSnapshot)
    {
        return ItemTypes.AIR.get() != itemStackSnapshot.type() && !itemStackSnapshot.isEmpty();
    }
}
