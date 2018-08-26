package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionHome;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.config.ConfigFields;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Hostile;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class EntitySpawnListener extends AbstractListener
{
    public EntitySpawnListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.EARLY)
    public void onEntitySpawn(SpawnEntityEvent event)
    {
        for (Entity entity: event.getEntities())
        {
            //EntityType for CustomNPC (it isn't count as a monster) => EntityCustomNpc
            if(entity.toString().contains("EntityCustomNpc")) return;

            if(entity instanceof Hostile)
            {
                if (!getPlugin().getConfiguration().getConfigFileds().getMobSpawning())
                {
                    if (getPlugin().getConfiguration().getConfigFileds().getSafeZoneWorldNames().contains(entity.getWorld().getName()))
                    {
                        event.setCancelled(true);
                        return;
                    }

                    if(getPlugin().getFactionLogic().isClaimed(entity.getWorld().getUniqueId(), entity.getLocation().getChunkPosition()))
                    {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            else if(entity instanceof Player)
            {
                if(getPlugin().getConfiguration().getConfigFileds().shouldSpawnAtHomeAfterDeath())
                {
                    Player player = (Player)entity;

                    Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());

                    if(optionalPlayerFaction.isPresent())
                    {
                        FactionHome factionHome = optionalPlayerFaction.get().getHome();
                        if(factionHome != null)
                        {
                            event.setCancelled(true);
                            World world = Sponge.getServer().getWorld(factionHome.getWorldUUID()).get();
                            player.setLocation(new Location<World>(world, factionHome.getBlockPosition()));
                            return;
                        }
                        else
                        {
                            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.COULD_NOT_SPAWN_AT_FACTIONS_HOME_HOME_MAY_NOT_BE_SET));
                        }
                    }
                }
            }
        }
    }
}
