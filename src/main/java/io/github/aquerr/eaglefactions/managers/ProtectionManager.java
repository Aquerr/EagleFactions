package io.github.aquerr.eaglefactions.managers;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.entities.EagleFeather;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.UUID;

public class ProtectionManager implements IProtectionManager
{
    private EagleFactions plugin;

    public ProtectionManager(EagleFactions plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean canInteract(Location location, World world, User user)
    {
        if(hasAdminMode(user.getUniqueId())
                || isBlockWhitelistedForInteraction(location.getBlockType())
                || (user.getItemInHand(HandTypes.MAIN_HAND).isPresent()
                && isItemWhitelisted(user.getItemInHand(HandTypes.MAIN_HAND).get().getType())))
        {
            return true;
        }

        if (this.plugin.getConfiguration().getConfigFields().getSafeZoneWorldNames().contains(world.getName()) && !user.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT))
        {
            user.getPlayer().ifPresent(x->x.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_INTERACT_HERE)));
            return false;
        }
        else if (this.plugin.getConfiguration().getConfigFields().getWarZoneWorldNames().contains(world.getName()) && !user.hasPermission(PluginPermissions.WAR_ZONE_INTERACT))
        {
            user.getPlayer().ifPresent(x->x.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_INTERACT_HERE)));
            return false;
        }

        Optional<Faction> optionalChunkFaction = this.plugin.getFactionLogic().getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
        Optional<Faction> optionalPlayerFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(user.getUniqueId());
        if(optionalChunkFaction.isPresent())
        {
            if(optionalChunkFaction.get().getName().equals("WarZone") || optionalChunkFaction.get().getName().equals("SafeZone"))
            {
                if(optionalChunkFaction.get().getName().equals("SafeZone") && user.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT))
                {
                    return true;
                }
                else if(optionalChunkFaction.get().getName().equals("WarZone") && user.hasPermission(PluginPermissions.WAR_ZONE_INTERACT))
                {
                    return true;
                }
                else
                {
                    user.getPlayer().ifPresent(x->x.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_INTERACT_HERE)));
                    return false;
                }
            }

            //Check if player has Eagle's Feather
            if(location.getTileEntity().isPresent()
                    && user.getItemInHand(HandTypes.MAIN_HAND).isPresent()
                    && user.getItemInHand(HandTypes.MAIN_HAND).get().getType() == ItemTypes.FEATHER
                    && user.getItemInHand(HandTypes.MAIN_HAND).get().get(Keys.DISPLAY_NAME).isPresent()
                    && user.getItemInHand(HandTypes.MAIN_HAND).get().get(Keys.DISPLAY_NAME).get().equals(EagleFeather.getDisplayName()))
            {
                ItemStack feather = user.getItemInHand(HandTypes.MAIN_HAND).get();
                feather.setQuantity(feather.getQuantity() - 1);
                user.getPlayer().ifPresent(x->x.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.DARK_PURPLE, "You have used eagle's feather!")));
                return true;
            }

            if(optionalPlayerFaction.isPresent())
            {
                if (!this.plugin.getFlagManager().canInteract(user.getUniqueId(), optionalPlayerFaction.get(), optionalChunkFaction.get()))
                {
                    user.getPlayer().ifPresent(x->x.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_INTERACT_HERE)));
                    return false;
                }
            }
            else
            {
                user.getPlayer().ifPresent(x->x.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_INTERACT_HERE)));
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canBreak(Location location, World world, User user)
    {
        if(hasAdminMode(user.getUniqueId()) || isBlockWhitelistedForPlaceDestroy(location.getBlockType()))
            return true;

        if(this.plugin.getConfiguration().getConfigFields().getSafeZoneWorldNames().contains(world.getName()) && !user.hasPermission(PluginPermissions.SAFE_ZONE_BUILD))
        {
            user.getPlayer().ifPresent(x->x.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_DESTROY_BLOCKS_HERE)));
            return false;
        }
        else if(this.plugin.getConfiguration().getConfigFields().getWarZoneWorldNames().contains(world.getName()) && this.plugin.getConfiguration().getConfigFields().isBlockDestroyAtWarzoneDisabled() && !user.hasPermission(PluginPermissions.WAR_ZONE_BUILD))
        {
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
    public boolean canBreak(Location location, World world)
    {
        //Air can be always destroyed.
        if(location.getBlockType() == BlockTypes.AIR)
            return true;

        if(isBlockWhitelistedForPlaceDestroy(location.getBlockType()))
            return true;

        if(this.plugin.getConfiguration().getConfigFields().getSafeZoneWorldNames().contains(world.getName()))
            return false;

        if(this.plugin.getConfiguration().getConfigFields().getWarZoneWorldNames().contains(world.getName()) && this.plugin.getConfiguration().getConfigFields().isBlockDestroyAtWarzoneDisabled())
            return false;

        Optional<Faction> optionalChunkFaction = this.plugin.getFactionLogic().getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
        if(!optionalChunkFaction.isPresent())
            return true;

        if(optionalChunkFaction.get().getName().equalsIgnoreCase("SafeZone"))
            return false;

        if(optionalChunkFaction.get().getName().equalsIgnoreCase("WarZone") && this.plugin.getConfiguration().getConfigFields().isBlockDestroyAtWarzoneDisabled())
            return false;

        if(this.plugin.getConfiguration().getConfigFields().isBlockDestroyAtClaimsDisabled())
            return false;

        return true;
    }

    @Override
    public boolean canPlace(Location location, World world, User user)
    {
        if(hasAdminMode(user.getUniqueId()) || (user.getItemInHand(HandTypes.MAIN_HAND).isPresent() && isBlockWhitelistedForPlaceDestroy(user.getItemInHand(HandTypes.MAIN_HAND).get().getType())))
            return true;

        if (this.plugin.getConfiguration().getConfigFields().getSafeZoneWorldNames().contains(world.getName()) && !user.hasPermission(PluginPermissions.SAFE_ZONE_BUILD))
        {
            return false;
        }
        else if (this.plugin.getConfiguration().getConfigFields().getWarZoneWorldNames().contains(world.getName()) && !user.hasPermission(PluginPermissions.WAR_ZONE_BUILD))
        {
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

    private boolean hasAdminMode(UUID playerUUID)
    {
        return EagleFactions.AdminList.contains(playerUUID);
    }

    @Override
    public boolean isItemWhitelisted(CatalogType itemType)
    {
        return this.plugin.getConfiguration().getConfigFields().getWhiteListedInteractBlocks().contains(itemType.getId());
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
}
