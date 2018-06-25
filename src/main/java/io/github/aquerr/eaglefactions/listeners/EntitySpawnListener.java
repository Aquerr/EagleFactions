package io.github.aquerr.eaglefactions.listeners;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionHome;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Hostile;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

@Singleton
public class EntitySpawnListener extends GenericListener
{

    @Inject
    EntitySpawnListener(FactionsCache cache, Settings settings, EagleFactions eagleFactions, EventManager eventManager)
    {
        super(cache, settings, eagleFactions, eventManager);
    }

    @Listener
    public void onEntitySpawn(SpawnEntityEvent event)
    {
        for (Entity entity : event.getEntities())
        {
            //EntityType for CustomNPC (it isn't count as a monster) => EntityCustomNpc
            if (entity.toString().contains("EntityCustomNpc")) return;

            if (entity instanceof Hostile)
            {
                if (!settings.getMobSpawning())
                {
                    if (settings.getSafeZoneWorldNames().contains(entity.getWorld().getName()))
                    {
                        event.setCancelled(true);
                        return;
                    }

                    if (cache.getClaimOwner(entity.getWorld().getUniqueId(), entity.getLocation().getChunkPosition()).isPresent())
                    {
                        event.setCancelled(true);
                        return;
                    }
                }
            } else if (entity instanceof Player)
            {
                if (settings.shouldSpawnAtHomeAfterDeath())
                {
                    Player player = (Player) entity;

                    Optional<Faction> optionalPlayerFaction = cache.getFactionByPlayer(player.getUniqueId());

                    if (optionalPlayerFaction.isPresent())
                    {
                        FactionHome factionHome = optionalPlayerFaction.get().Home;
                        if (factionHome != null)
                        {
                            event.setCancelled(true);
                            World world = Sponge.getServer().getWorld(factionHome.WorldUUID).get();
                            player.setLocation(new Location(world, factionHome.BlockPosition));
                            return;
                        } else
                        {
                            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.COULD_NOT_SPAWN_AT_FACTIONS_HOME_HOME_MAY_NOT_BE_SET));
                        }
                    }
                }
            }
        }
    }
}
