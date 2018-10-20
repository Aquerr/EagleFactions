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
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class ProtectionManager implements IProtectionManager
{
    private EagleFactions plugin;

    public ProtectionManager(EagleFactions plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean canInteract(Location location, World world, Player player)
    {
        if(hasAdminMode(player)
                || isBlockWhitelistedForInteraction(location.getBlockType())
                || (player.getItemInHand(HandTypes.MAIN_HAND).isPresent()
                || isItemWhitelisted(player.getItemInHand(HandTypes.MAIN_HAND).get().getType())))
        {
            return true;
        }

        if (this.plugin.getConfiguration().getConfigFields().getSafeZoneWorldNames().contains(world.getName()) && !player.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT))
        {
            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_INTERACT_HERE));
            return false;
        }
        else if (this.plugin.getConfiguration().getConfigFields().getWarZoneWorldNames().contains(world.getName()) && !player.hasPermission(PluginPermissions.WAR_ZONE_INTERACT))
        {
            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_INTERACT_HERE));
            return false;
        }

        Optional<Faction> optionalChunkFaction = this.plugin.getFactionLogic().getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
        Optional<Faction> optionalPlayerFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        if(optionalChunkFaction.isPresent())
        {
            if(optionalChunkFaction.get().getName().equals("WarZone") || optionalChunkFaction.get().getName().equals("SafeZone"))
            {
                if(optionalChunkFaction.get().getName().equals("SafeZone") && player.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT))
                {
                    return true;
                }
                else if(optionalChunkFaction.get().getName().equals("WarZone") && player.hasPermission(PluginPermissions.WAR_ZONE_INTERACT))
                {
                    return true;
                }
                else
                {
                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_INTERACT_HERE));
                    return false;
                }
            }

            //Check if player has Eagle's Feather
            if(location.getTileEntity().isPresent()
                    && player.getItemInHand(HandTypes.MAIN_HAND).isPresent()
                    && player.getItemInHand(HandTypes.MAIN_HAND).get().getType() == ItemTypes.FEATHER
                    && player.getItemInHand(HandTypes.MAIN_HAND).get().get(Keys.DISPLAY_NAME).isPresent()
                    && player.getItemInHand(HandTypes.MAIN_HAND).get().get(Keys.DISPLAY_NAME).get().equals(EagleFeather.getDisplayName()))
            {
                ItemStack feather = player.getItemInHand(HandTypes.MAIN_HAND).get();
                feather.setQuantity(feather.getQuantity() - 1);
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.DARK_PURPLE, "You have used eagle's feather!"));
                return true;
            }

            if(optionalPlayerFaction.isPresent())
            {
                if (!this.plugin.getFlagManager().canInteract(player, optionalPlayerFaction.get(), optionalChunkFaction.get()))
                {
                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_INTERACT_HERE));
                    return false;
                }
            }
            else
            {
                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_INTERACT_HERE));
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canBreak(Location location, World world, Player player)
    {
        if(hasAdminMode(player) || isBlockWhitelistedForPlaceDestroy(location.getBlockType()))
            return true;

        if(this.plugin.getConfiguration().getConfigFields().getSafeZoneWorldNames().contains(world.getName()) && !player.hasPermission(PluginPermissions.SAFE_ZONE_BUILD))
        {
            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_DESTROY_BLOCKS_HERE));
            return false;
        }
        else if(this.plugin.getConfiguration().getConfigFields().getWarZoneWorldNames().contains(world.getName()) && this.plugin.getConfiguration().getConfigFields().isBlockDestroyAtWarzoneDisabled() && !player.hasPermission(PluginPermissions.WAR_ZONE_BUILD))
        {
            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_DESTROY_BLOCKS_HERE));
            return false;
        }

        Optional<Faction> optionalChunkFaction = this.plugin.getFactionLogic().getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
        Optional<Faction> optionalPlayerFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        if(optionalChunkFaction.isPresent())
        {
            if(optionalChunkFaction.get().getName().equals("WarZone") || optionalChunkFaction.get().getName().equals("SafeZone"))
            {
                if(optionalChunkFaction.get().getName().equals("SafeZone") && player.hasPermission(PluginPermissions.SAFE_ZONE_BUILD))
                {
                    return true;
                }
                else if(optionalChunkFaction.get().getName().equals("WarZone") && player.hasPermission(PluginPermissions.WAR_ZONE_BUILD))
                {
                    return true;
                }
                else
                {
                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_DESTROY_BLOCKS_HERE));
                    return false;
                }
            }

            if(optionalPlayerFaction.isPresent())
            {
                if (!this.plugin.getFlagManager().canBreakBlock(player, optionalPlayerFaction.get(), optionalChunkFaction.get()))
                {
                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_DESTROY_BLOCKS_HERE));
                    return false;
                }
            }
            else
            {
                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_DESTROY_BLOCKS_HERE));
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canBreak(Location location, World world)
    {
        if(isBlockWhitelistedForPlaceDestroy(location.getBlockType()))
            return true;

        if(this.plugin.getConfiguration().getConfigFields().getSafeZoneWorldNames().contains(world.getName()))
        {
            return false;
        }
        else if(this.plugin.getConfiguration().getConfigFields().getWarZoneWorldNames().contains(world.getName()) && this.plugin.getConfiguration().getConfigFields().isBlockDestroyAtWarzoneDisabled())
        {
            return false;
        }

        Optional<Faction> optionalChunkFaction = this.plugin.getFactionLogic().getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
        if (optionalChunkFaction.isPresent())
        {
            if(!optionalChunkFaction.get().getName().equals("SafeZone") && !optionalChunkFaction.get().getName().equals("WarZone") && this.plugin.getConfiguration().getConfigFields().isBlockDestroyAtClaimsDisabled())
            {
                return false;
            }
            else if(optionalChunkFaction.get().getName().equals("SafeZone"))
            {
                return false;
            }
            else if (optionalChunkFaction.get().getName().equals("WarZone") && this.plugin.getConfiguration().getConfigFields().isBlockDestroyAtWarzoneDisabled())
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canPlace(Location location, World world, Player player)
    {
        if(hasAdminMode(player) || (player.getItemInHand(HandTypes.MAIN_HAND).isPresent() && isBlockWhitelistedForPlaceDestroy(player.getItemInHand(HandTypes.MAIN_HAND).get().getType())))
            return true;

        if (this.plugin.getConfiguration().getConfigFields().getSafeZoneWorldNames().contains(world.getName()) && !player.hasPermission(PluginPermissions.SAFE_ZONE_BUILD))
        {
            return false;
        }
        else if (this.plugin.getConfiguration().getConfigFields().getWarZoneWorldNames().contains(world.getName()) && !player.hasPermission(PluginPermissions.WAR_ZONE_BUILD))
        {
            return false;
        }

        Optional<Faction> optionalPlayerFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        Optional<Faction> optionalChunkFaction = this.plugin.getFactionLogic().getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
        if(optionalChunkFaction.isPresent())
        {
            if(optionalChunkFaction.get().getName().equals("WarZone") || optionalChunkFaction.get().getName().equals("SafeZone"))
            {
                if(optionalChunkFaction.get().getName().equals("SafeZone") && player.hasPermission(PluginPermissions.SAFE_ZONE_BUILD))
                {
                    return true;
                }
                else if(optionalChunkFaction.get().getName().equals("WarZone") && player.hasPermission(PluginPermissions.WAR_ZONE_BUILD))
                {
                    return true;
                }
                else
                {
                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_ACCESS_TO_DO_THIS));
                    return false;
                }
            }

            if(optionalPlayerFaction.isPresent())
            {
                if (!this.plugin.getFlagManager().canPlaceBlock(player, optionalPlayerFaction.get(), optionalChunkFaction.get()))
                {
                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_ACCESS_TO_DO_THIS));
                    return false;
                }
            }
            else
            {
                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_ACCESS_TO_DO_THIS));
                return false;
            }
        }
        return true;
    }

    private boolean hasAdminMode(Player player)
    {
        return EagleFactions.AdminList.contains(player.getUniqueId());
    }

    //TODO: Looks like it is unnecessary
    private boolean isItemWhitelisted(CatalogType itemType)
    {
        return this.plugin.getConfiguration().getConfigFields().getWhiteListedInteractBlocks().contains(itemType.getId());
    }

    private boolean isBlockWhitelistedForInteraction(CatalogType blockType)
    {
        return this.plugin.getConfiguration().getConfigFields().getWhiteListedInteractBlocks().contains(blockType.getId());
    }

    private boolean isBlockWhitelistedForPlaceDestroy(CatalogType blockOrItemType)
    {
        return this.plugin.getConfiguration().getConfigFields().getWhiteListedPlaceDestroyBlocks().contains(blockOrItemType.getId());
    }
}
