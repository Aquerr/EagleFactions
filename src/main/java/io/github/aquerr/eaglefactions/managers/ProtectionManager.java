package io.github.aquerr.eaglefactions.managers;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import org.spongepowered.api.entity.living.player.Player;
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
        if (this.plugin.getConfiguration().getConfigFileds().getSafeZoneWorldNames().contains(world.getName()) && !player.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT))
        {
            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_INTERACT_HERE));
            return false;
        }
        else if (this.plugin.getConfiguration().getConfigFileds().getWarZoneWorldNames().contains(world.getName()) && !player.hasPermission(PluginPermissions.WAR_ZONE_INTERACT))
        {
            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_INTERACT_HERE));
            return false;
        }

        Optional<Faction> optionalChunkFaction = this.plugin.getFactionLogic().getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
        Optional<Faction> optionalPlayerFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        if(optionalChunkFaction.isPresent())
        {
            if(optionalChunkFaction.get().getName().equals("SafeZone") && player.hasPermission(PluginPermissions.SAFE_ZONE_INTERACT))
            {
                return true;
            }
            else if(optionalChunkFaction.get().getName().equals("WarZone") && player.hasPermission(PluginPermissions.WAR_ZONE_INTERACT))
            {
                return true;
            }
            else if (optionalPlayerFaction.isPresent())
            {
                if (!this.plugin.getFlagManager().canInteract(player, optionalPlayerFaction.get(), optionalChunkFaction.get()))
                {
                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_INTERACT_HERE));
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

    @Override
    public boolean canBreak(Location location, World world, Player player)
    {
        if(this.plugin.getConfiguration().getConfigFileds().getSafeZoneWorldNames().contains(world.getName()) && !player.hasPermission(PluginPermissions.SAFE_ZONE_BUILD))
        {
            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_DESTROY_BLOCKS_HERE));
            return false;
        }
        else if(this.plugin.getConfiguration().getConfigFileds().getWarZoneWorldNames().contains(world.getName()) && this.plugin.getConfiguration().getConfigFileds().isBlockDestroyAtWarzoneDisabled() && !player.hasPermission(PluginPermissions.WAR_ZONE_BUILD))
        {
            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_DESTROY_BLOCKS_HERE));
            return false;
        }

        Optional<Faction> optionalChunkFaction = this.plugin.getFactionLogic().getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
        Optional<Faction> optionalPlayerFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        if(optionalChunkFaction.isPresent())
        {
            if(optionalChunkFaction.get().getName().equals("SafeZone") && player.hasPermission(PluginPermissions.SAFE_ZONE_BUILD))
            {
                return true;
            }
            else if(optionalChunkFaction.get().getName().equals("WarZone") && player.hasPermission(PluginPermissions.WAR_ZONE_BUILD))
            {
                return true;
            }
            else if(optionalPlayerFaction.isPresent())
            {
                if (!this.plugin.getFlagManager().canBreakBlock(player, optionalPlayerFaction.get(), optionalChunkFaction.get()))
                {
                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_DESTROY_BLOCKS_HERE));
                    return false;
                }
            }
            else
            {
                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.THIS_LAND_BELONGS_TO_SOMEONE_ELSE));
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canBreak(Location location, World world)
    {
        if(this.plugin.getConfiguration().getConfigFileds().getSafeZoneWorldNames().contains(world.getName()))
        {
            return false;
        }
        else if(this.plugin.getConfiguration().getConfigFileds().getWarZoneWorldNames().contains(world.getName()) && this.plugin.getConfiguration().getConfigFileds().isBlockDestroyAtWarzoneDisabled())
        {
            return false;
        }

        Optional<Faction> optionalChunkFaction = this.plugin.getFactionLogic().getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
        if (optionalChunkFaction.isPresent())
        {
            if(!optionalChunkFaction.get().getName().equals("SafeZone") && !optionalChunkFaction.get().getName().equals("WarZone") && this.plugin.getConfiguration().getConfigFileds().isBlockDestroyAtClaimsDisabled())
            {
                return false;
            }
            else if(optionalChunkFaction.get().getName().equals("SafeZone"))
            {
                return false;
            }
            else if (optionalChunkFaction.get().getName().equals("WarZone") && this.plugin.getConfiguration().getConfigFileds().isBlockDestroyAtWarzoneDisabled())
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canPlace(Location location, World world, Player player)
    {
        if (this.plugin.getConfiguration().getConfigFileds().getSafeZoneWorldNames().contains(world.getName()) && !player.hasPermission(PluginPermissions.SAFE_ZONE_BUILD))
        {
            return false;
        }
        else if (this.plugin.getConfiguration().getConfigFileds().getWarZoneWorldNames().contains(world.getName()) && !player.hasPermission(PluginPermissions.WAR_ZONE_BUILD))
        {
            return false;
        }

        Optional<Faction> optionalPlayerFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        Optional<Faction> optionalChunkFaction = this.plugin.getFactionLogic().getFactionByChunk(world.getUniqueId(), location.getChunkPosition());
        if(optionalChunkFaction.isPresent())
        {
            if(optionalChunkFaction.get().getName().equals("SafeZone") && player.hasPermission(PluginPermissions.SAFE_ZONE_BUILD))
            {
                return true;
            }
            else if(optionalChunkFaction.get().getName().equals("WarZone") && player.hasPermission(PluginPermissions.WAR_ZONE_BUILD))
            {
                return true;
            }
            else if(optionalPlayerFaction.isPresent())
            {
                if (!this.plugin.getFlagManager().canPlaceBlock(player, optionalPlayerFaction.get(), optionalChunkFaction.get()))
                {
                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_PRIVILEGES_TO_DESTROY_BLOCKS_HERE));
                    return false;
                }
            }
            else
            {
                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.THIS_LAND_BELONGS_TO_SOMEONE_ELSE));
                return false;
            }
        }
        return true;
    }
}
