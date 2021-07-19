package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.managers.ProtectionResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.item.inventory.container.InteractContainerEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;

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

        ServerLocation location = ServerLocation.of(player.world().key(), player.position());
//        ServerLocation location = ServerLocation.of(event.)
//                .map(interactionPoint -> new Location<>(player.getWorld(), interactionPoint))
//                .orElse(player.getLocation());

        //TODO: To test... don't know how mods will behave with it.
        if (location.blockType() == BlockTypes.AIR)
            return;

        //Handle hitting entities
        boolean hasHitEntity = event.context().containsKey(EventContextKeys.ENTITY_HIT);
        if(hasHitEntity)
        {
            final Entity hitEntity = event.context().get(EventContextKeys.ENTITY_HIT).get();
            if (hitEntity instanceof Living && !(hitEntity instanceof ArmorStand))
                return;

            location = hitEntity.serverLocation();
        }

        final ProtectionResult protectionResult = super.getPlugin().getProtectionManager().canUseItem(location, player.user(), event.itemStack(), true);
        if (!protectionResult.hasAccess())
        {
            event.setCancelled(true);
            return;
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onEntityInteract(final InteractEntityEvent.Secondary event, @Root final ServerPlayer player)
    {
        final Entity targetEntity = event.entity();

        if((targetEntity instanceof Living) && !(targetEntity instanceof ArmorStand))
            return;

        final Vector3d blockPosition = targetEntity.serverLocation().position();
        final ServerLocation location = ServerLocation.of(targetEntity.serverLocation().world(), blockPosition);
        //TODO: canInteractWithBlock should be probably changed to canHitEntity
        boolean canInteractWithEntity = super.getPlugin().getProtectionManager().canInteractWithBlock(location, player.user(), true).hasAccess();
        if(!canInteractWithEntity)
        {
            event.setCancelled(true);
            return;
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockInteract(final InteractBlockEvent.Secondary event, @Root final ServerPlayer player)
    {
        //If AIR or NONE then return
        if (event.block() == BlockSnapshot.NONE || event.block().state().type() == BlockTypes.AIR)
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

    private void checkInteraction(InteractBlockEvent.Secondary interactBlockEvent, ServerLocation location, ServerPlayer player, boolean shouldNotify)
    {
        ItemStackSnapshot usedItem = interactBlockEvent.context().get(EventContextKeys.USED_ITEM)
                .filter(this::isItemStackNotAirAndNotEmpty)
                .orElse(null);
        ProtectionResult protectionResult;
        if (usedItem != null)
        {
            protectionResult = super.getPlugin().getProtectionManager().canUseItem(location, player.user(), interactBlockEvent.context().get(EventContextKeys.USED_ITEM).get(), shouldNotify);
        }
        else
        {
            protectionResult = super.getPlugin().getProtectionManager().canInteractWithBlock(location, player.user(), shouldNotify);
        }

        if (!protectionResult.hasAccess())
        {
            interactBlockEvent.setCancelled(true);
        }
    }

    private void removeEagleFeather(final Player player)
    {
        final ItemStack feather = player.itemInHand(HandTypes.MAIN_HAND);
        feather.setQuantity(feather.quantity() - 1);
        player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text("You used Eagle's Feather!", NamedTextColor.DARK_PURPLE)));
    }

    private boolean isItemStackNotAirAndNotEmpty(ItemStackSnapshot itemStackSnapshot)
    {
        return ItemTypes.AIR != itemStackSnapshot.type() && !itemStackSnapshot.isEmpty();
    }
}
