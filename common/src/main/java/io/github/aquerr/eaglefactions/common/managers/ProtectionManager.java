package io.github.aquerr.eaglefactions.common.managers;

import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.EagleFeather;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.managers.IProtectionManager;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.PluginPermissions;
import io.github.aquerr.eaglefactions.common.message.PluginMessages;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Singleton
public class ProtectionManager implements IProtectionManager
{
    private static ProtectionManager INSTANCE = null;
    private final EagleFactions plugin;

    public static ProtectionManager getInstance(final EagleFactions eagleFactions)
    {
        if (INSTANCE == null)
            return new ProtectionManager(eagleFactions);
        else return INSTANCE;
    }

    private ProtectionManager(final EagleFactions plugin)
    {
        INSTANCE = this;
        this.plugin = plugin;
    }

    @Override
    public boolean canInteractWithBlock(final Location<World> location, final User user)
    {
        final boolean isTileEntityAtLocation = location.getTileEntity().isPresent();

        if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(user.getUniqueId()))
        {
            if(user instanceof Player)
            {
                Player player = (Player)user;
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Interact With Block:")));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Location: " + location.toString())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("User: " + user.getName())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Block at location: " + location.getBlockType().getName())));
            }
        }

        final World world = location.getExtent();

        if (hasAdminMode(user.getUniqueId()))
            return true;

        if (isBlockWhitelistedForInteraction(location.getBlockType()))
        {
            return true;
        }

        final Set<String> safeZoneWorlds = this.plugin.getConfiguration().getConfigFields().getSafeZoneWorldNames();
        final Set<String> warZoneWorlds = this.plugin.getConfiguration().getConfigFields().getWarZoneWorldNames();

        if(safeZoneWorlds.contains(world.getName()) || warZoneWorlds.contains(world.getName()))
        {
            if (safeZoneWorlds.contains(world.getName()) && user.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT))
                return true;

            if (warZoneWorlds.contains(world.getName()) && user.hasPermission(PluginPermissions.WAR_ZONE_INTERACT))
                return true;

            //Warzone, chest and eaglefeather
            if(warZoneWorlds.contains(world.getName()) && isTileEntityAtLocation && isHoldingEagleFeather(user))
            {
                removeEagleFeather(user);
                return true;
            }

            user.getPlayer().ifPresent(x->x.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_ACCESS_TO_DO_THIS)));
            return false;
        }

        final Optional<Faction> optionalChunkFaction = this.plugin.getFactionLogic().getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
        final Optional<Faction> optionalPlayerFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(user.getUniqueId());
        if (!optionalChunkFaction.isPresent())
            return true;

        final Faction chunkFaction = optionalChunkFaction.get();
        if (chunkFaction.getName().equalsIgnoreCase("SafeZone") && user.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT))
            return true;
        if (chunkFaction.getName().equalsIgnoreCase("WarZone") && user.hasPermission(PluginPermissions.WAR_ZONE_INTERACT))
            return true;

        if(chunkFaction.getName().equalsIgnoreCase("WarZone") && isTileEntityAtLocation && isHoldingEagleFeather(user))
        {
            removeEagleFeather(user);
            return true;
        }

        //If player is not in a faction but there is a faction at chunk
        if(!optionalPlayerFaction.isPresent())
        {
            //Holding Eagle Feather?
            if(isTileEntityAtLocation && isHoldingEagleFeather(user))
            {
                removeEagleFeather(user);
                return true;
            }

            user.getPlayer().ifPresent(x->x.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_INTERACT_HERE)));
            return false;
        }

        final Faction playerFaction = optionalPlayerFaction.get();
        if (plugin.getFlagManager().canInteract(user.getUniqueId(), playerFaction, chunkFaction))
            return true;
        else
        {
            //Holding Eagle Feather?
            if(isTileEntityAtLocation && isHoldingEagleFeather(user))
            {
                removeEagleFeather(user);
                return true;
            }

            user.getPlayer().ifPresent(x->x.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_INTERACT_HERE)));
            return false;
        }
    }

    @Override
    public boolean canUseItem(final Location<World> location, final User user, final ItemStackSnapshot usedItem)
    {
        if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(user.getUniqueId()))
        {
            if(user instanceof Player)
            {
                Player player = (Player)user;
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Usage of item:")));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Location: " + location.toString())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("User: " + user.getName())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Block at location: " + location.getBlockType().getName())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Used item: " + usedItem.getType().getName())));
            }
        }

        World world = location.getExtent();

        if (hasAdminMode(user.getUniqueId()))
            return true;

        if (isItemWhitelisted(usedItem.getType().getId()))
            return true;

        final Set<String> safeZoneWorlds = this.plugin.getConfiguration().getConfigFields().getSafeZoneWorldNames();
        final Set<String> warZoneWorlds = this.plugin.getConfiguration().getConfigFields().getWarZoneWorldNames();

        if(safeZoneWorlds.contains(world.getName()) || warZoneWorlds.contains(world.getName()))
        {
            if (safeZoneWorlds.contains(world.getName()) && user.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT))
                return true;

            if (warZoneWorlds.contains(world.getName()) && user.hasPermission(PluginPermissions.WAR_ZONE_INTERACT))
                return true;

            user.getPlayer().ifPresent(x->x.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_ACCESS_TO_DO_THIS)));
            return false;
        }

        final Optional<Faction> optionalChunkFaction = this.plugin.getFactionLogic().getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
        final Optional<Faction> optionalPlayerFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(user.getUniqueId());
        if (!optionalChunkFaction.isPresent())
            return true;

        final Faction chunkFaction = optionalChunkFaction.get();
        if (chunkFaction.getName().equalsIgnoreCase("SafeZone") && user.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT))
            return true;
        if (chunkFaction.getName().equalsIgnoreCase("WarZone") && user.hasPermission(PluginPermissions.WAR_ZONE_INTERACT))
            return true;

        //If player is not in a faction but there is a faction at chunk
        if(!optionalPlayerFaction.isPresent())
        {
            user.getPlayer().ifPresent(x->x.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_INTERACT_HERE)));
            return false;
        }

        Faction playerFaction = optionalPlayerFaction.get();
        if (plugin.getFlagManager().canInteract(user.getUniqueId(), playerFaction, chunkFaction))
            return true;
        else
        {
            user.getPlayer().ifPresent(x->x.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_INTERACT_HERE)));
            return false;
        }
    }

    @Override
    public boolean canBreak(Location<World> location, User user)
    {
        if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(user.getUniqueId()))
        {
            if(user instanceof Player)
            {
                Player player = (Player)user;
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Block break:")));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Location: " + location.toString())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("User: " + user.getName())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Block at location: " + location.getBlockType().getName())));
            }
        }

        World world = location.getExtent();
        if(hasAdminMode(user.getUniqueId()) || isBlockWhitelistedForPlaceDestroy(location.getBlockType()))
            return true;

        final Set<String> safeZoneWorlds = this.plugin.getConfiguration().getConfigFields().getSafeZoneWorldNames();
        final Set<String> warZoneWorlds = this.plugin.getConfiguration().getConfigFields().getWarZoneWorldNames();

        if(safeZoneWorlds.contains(world.getName()) || warZoneWorlds.contains(world.getName()))
        {
            if (safeZoneWorlds.contains(world.getName()) && user.hasPermission(PluginPermissions.SAFE_ZONE_BUILD))
                return true;

            if (warZoneWorlds.contains(world.getName()) && user.hasPermission(PluginPermissions.WAR_ZONE_BUILD))
                return true;

            user.getPlayer().ifPresent(x->x.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_DESTROY_BLOCKS_HERE)));
            return false;
        }

        Optional<Faction> optionalChunkFaction = this.plugin.getFactionLogic().getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
        Optional<Faction> optionalPlayerFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(user.getUniqueId());
        if(optionalChunkFaction.isPresent())
        {
            if(optionalChunkFaction.get().getName().equals("WarZone") || optionalChunkFaction.get().getName().equals("SafeZone"))
            {
                if(optionalChunkFaction.get().getName().equals("SafeZone") && user.hasPermission(PluginPermissions.SAFE_ZONE_BUILD))
                {
                    return true;
                }
                else if(optionalChunkFaction.get().getName().equals("WarZone") && user.hasPermission(PluginPermissions.WAR_ZONE_BUILD))
                {
                    return true;
                }
                else
                {
                    user.getPlayer().ifPresent(x->x.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_DESTROY_BLOCKS_HERE)));
                    return false;
                }
            }

            if(optionalPlayerFaction.isPresent())
            {
                if (!this.plugin.getFlagManager().canBreakBlock(user.getUniqueId(), optionalPlayerFaction.get(), optionalChunkFaction.get()))
                {
                    user.getPlayer().ifPresent(x->x.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_DESTROY_BLOCKS_HERE)));
                    return false;
                }
            }
            else
            {
                user.getPlayer().ifPresent(x->x.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_DESTROY_BLOCKS_HERE)));
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canBreak(Location<World> location)
    {
        final World world = location.getExtent();

        if(isBlockWhitelistedForPlaceDestroy(location.getBlockType()))
            return true;

        if(this.plugin.getConfiguration().getConfigFields().getSafeZoneWorldNames().contains(world.getName()))
            return false;

        if(this.plugin.getConfiguration().getConfigFields().getWarZoneWorldNames().contains(world.getName()) && this.plugin.getConfiguration().getConfigFields().shouldProtectWarZoneFromMobGrief())
            return false;

        final Optional<Faction> optionalChunkFaction = this.plugin.getFactionLogic().getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
        if(!optionalChunkFaction.isPresent())
            return true;

        if(optionalChunkFaction.get().getName().equalsIgnoreCase("SafeZone"))
            return false;

        if(optionalChunkFaction.get().getName().equalsIgnoreCase("WarZone") && this.plugin.getConfiguration().getConfigFields().shouldProtectWarZoneFromMobGrief())
            return false;

        if(this.plugin.getConfiguration().getConfigFields().shouldProtectClaimFromMobGrief())
            return false;

        return true;
    }

    @Override
    public boolean canPlace(Location<World> location, User user)
    {
        if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(user.getUniqueId()))
        {
            if(user instanceof Player)
            {
                Player player = (Player)user;
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Block place:")));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Location: " + location.toString())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("User: " + user.getName())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Block at location: " + location.getBlockType().getName())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Item in hand: " + (user.getItemInHand(HandTypes.MAIN_HAND).isPresent() ? user.getItemInHand(HandTypes.MAIN_HAND).get().getType().getName() : ""))));
            }
        }

        World world = location.getExtent();
        if(hasAdminMode(user.getUniqueId()) || (user.getItemInHand(HandTypes.MAIN_HAND).isPresent() && isBlockWhitelistedForPlaceDestroy(user.getItemInHand(HandTypes.MAIN_HAND).get().getType())))
            return true;

        final Set<String> safeZoneWorlds = this.plugin.getConfiguration().getConfigFields().getSafeZoneWorldNames();
        final Set<String> warZoneWorlds = this.plugin.getConfiguration().getConfigFields().getWarZoneWorldNames();

        if(safeZoneWorlds.contains(world.getName()) || warZoneWorlds.contains(world.getName()))
        {
            if (safeZoneWorlds.contains(world.getName()) && user.hasPermission(PluginPermissions.SAFE_ZONE_BUILD))
                return true;

            if (warZoneWorlds.contains(world.getName()) && user.hasPermission(PluginPermissions.WAR_ZONE_BUILD))
                return true;

            user.getPlayer().ifPresent(x->x.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_ACCESS_TO_DO_THIS)));
            return false;
        }

        Optional<Faction> optionalPlayerFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(user.getUniqueId());
        Optional<Faction> optionalChunkFaction = this.plugin.getFactionLogic().getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
        if(optionalChunkFaction.isPresent())
        {
            if(optionalChunkFaction.get().getName().equals("WarZone") || optionalChunkFaction.get().getName().equals("SafeZone"))
            {
                if(optionalChunkFaction.get().getName().equals("SafeZone") && user.hasPermission(PluginPermissions.SAFE_ZONE_BUILD))
                {
                    return true;
                }
                else if(optionalChunkFaction.get().getName().equals("WarZone") && user.hasPermission(PluginPermissions.WAR_ZONE_BUILD))
                {
                    return true;
                }
                else
                {
                    user.getPlayer().ifPresent(x->x.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_ACCESS_TO_DO_THIS)));
                    return false;
                }
            }

            if(optionalPlayerFaction.isPresent())
            {
                if (!this.plugin.getFlagManager().canPlaceBlock(user.getUniqueId(), optionalPlayerFaction.get(), optionalChunkFaction.get()))
                {
                    user.getPlayer().ifPresent(x->x.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_ACCESS_TO_DO_THIS)));
                    return false;
                }
            }
            else
            {
                user.getPlayer().ifPresent(x->x.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_ACCESS_TO_DO_THIS)));
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canExplode(Location<World> location, User user)
    {
        if(EagleFactionsPlugin.DEBUG_MODE_PLAYERS.contains(user.getUniqueId()))
        {
            if(user instanceof Player)
            {
                Player player = (Player)user;
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Explosion:")));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Location: " + location.toString())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("User: " + user.getName())));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of("Block at location: " + location.getBlockType().getName())));
            }
        }

        boolean shouldProtectWarZoneFromPlayers = this.plugin.getConfiguration().getConfigFields().shouldProtectWarzoneFromPlayers();
        boolean allowExplosionsByOtherPlayersInClaims = this.plugin.getConfiguration().getConfigFields().shouldAllowExplosionsByOtherPlayersInClaims();

        //Check if admin
        if(EagleFactionsPlugin.ADMIN_MODE_PLAYERS.contains(user.getUniqueId()))
            return true;

        //Check world
        if (this.plugin.getConfiguration().getConfigFields().getSafeZoneWorldNames().contains(location.getExtent().getName()))
            return false;

        if (this.plugin.getConfiguration().getConfigFields().getWarZoneWorldNames().contains(location.getExtent().getName()))
            return !shouldProtectWarZoneFromPlayers;

        //If no faction
        Optional<Faction> optionalChunkFaction = this.plugin.getFactionLogic().getFactionByChunk(location.getExtent().getUniqueId(), location.getChunkPosition());
        if (!optionalChunkFaction.isPresent())
            return true;

        //If SafeZone or WarZone
        Faction chunkFaction = optionalChunkFaction.get();
        if (chunkFaction.getName().equalsIgnoreCase("SafeZone"))
            return false;
        if (chunkFaction.getName().equalsIgnoreCase("WarZone") && shouldProtectWarZoneFromPlayers)
            return false;

        //If player is in faction
        Optional<Faction> optionalPlayerFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(user.getUniqueId());
        if (optionalPlayerFaction.isPresent())
        {
            Faction playerFaction = optionalPlayerFaction.get();
            //If same faction
            if (chunkFaction.getName().equalsIgnoreCase(playerFaction.getName()))
            {
                //Check faction's flags
                return this.plugin.getFlagManager().canBreakBlock(user.getUniqueId(), playerFaction, chunkFaction);
            }
            return allowExplosionsByOtherPlayersInClaims;
        }
        else //If player is not in faction but faction exists at chunk
        {
            return allowExplosionsByOtherPlayersInClaims;
        }
    }

    @Override
    public boolean canExplode(final Location<World> location)
    {
        boolean shouldProtectWarZoneFromMobGrief = this.plugin.getConfiguration().getConfigFields().shouldProtectWarZoneFromMobGrief();
        boolean shouldProtectClaimsFromMobGrief = this.plugin.getConfiguration().getConfigFields().shouldProtectClaimFromMobGrief();

        //Check world
        if (this.plugin.getConfiguration().getConfigFields().getSafeZoneWorldNames().contains(location.getExtent().getName()))
            return false;

        if (this.plugin.getConfiguration().getConfigFields().getWarZoneWorldNames().contains(location.getExtent().getName()))
            return !shouldProtectWarZoneFromMobGrief;

        Optional<Faction> optionalChunkFaction = this.plugin.getFactionLogic().getFactionByChunk(location.getExtent().getUniqueId(), location.getChunkPosition());
        if (!optionalChunkFaction.isPresent())
            return true;

        Faction chunkFaction = optionalChunkFaction.get();
        if (chunkFaction.getName().equalsIgnoreCase("SafeZone"))
            return false;
        else if (chunkFaction.getName().equalsIgnoreCase("WarZone") && shouldProtectWarZoneFromMobGrief)
            return false;
        else return !shouldProtectClaimsFromMobGrief;
    }

    @Override
    public boolean isItemWhitelisted(final String itemId)
    {
        return this.plugin.getConfiguration().getConfigFields().getWhiteListedItems().contains(itemId);
    }

    @Override
    public boolean isBlockWhitelistedForInteraction(CatalogType blockType)
    {
        return this.plugin.getConfiguration().getConfigFields().getWhiteListedInteractBlocks().contains(blockType.getId());
    }

    @Override
    public boolean isBlockWhitelistedForPlaceDestroy(CatalogType blockOrItemType)
    {
        return this.plugin.getConfiguration().getConfigFields().getWhiteListedPlaceDestroyBlocks().contains(blockOrItemType.getId());
    }

//    private boolean canUseItem(User user)
//    {
//        Optional<ItemStack> optionalItemStack = user.getItemInHand(HandTypes.MAIN_HAND);
//        return optionalItemStack.isPresent() && isItemWhitelisted(optionalItemStack.get().getType());
//
//    }

//    private boolean canUseBlock(Location<World> location)
//    {
//        BlockType blockType = location.getBlockType();
//        return blockType != BlockTypes.AIR && isBlockWhitelistedForInteraction(blockType);
//    }

    private boolean hasAdminMode(UUID playerUUID)
    {
        return EagleFactionsPlugin.ADMIN_MODE_PLAYERS.contains(playerUUID);
    }

    private boolean isHoldingEagleFeather(final User user)
    {
        return user.getItemInHand(HandTypes.MAIN_HAND).isPresent()
                && user.getItemInHand(HandTypes.MAIN_HAND).get().getType() == ItemTypes.FEATHER
                && user.getItemInHand(HandTypes.MAIN_HAND).get().get(Keys.DISPLAY_NAME).isPresent()
                && user.getItemInHand(HandTypes.MAIN_HAND).get().get(Keys.DISPLAY_NAME).get().equals(EagleFeather.getDisplayName());
    }

    private void removeEagleFeather(final User user)
    {
        final ItemStack feather = user.getItemInHand(HandTypes.MAIN_HAND).get();
        feather.setQuantity(feather.getQuantity() - 1);
        user.getPlayer().ifPresent(x->x.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.DARK_PURPLE, "You have used eagle's feather!")));
    }
}
