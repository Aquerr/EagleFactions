package io.github.aquerr.eaglefactions.listeners;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.messaging.Placeholders;
import io.github.aquerr.eaglefactions.scheduling.EagleFactionsScheduler;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static net.kyori.adventure.text.format.NamedTextColor.*;

public class PlayerMoveListener extends AbstractListener
{
    private final FactionsConfig factionsConfig;
    private final ChatConfig chatConfig;

    public PlayerMoveListener(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.chatConfig = plugin.getConfiguration().getChatConfig();
    }

//    @Listener
//    public void onFlanEntityMove(final MoveEntityEvent event)
//    {
//        final Location<World> lastLocation = event.getFromTransform().getLocation();
//        final Location<World> newLocation = event.getToTransform().getLocation();
//
//        if(lastLocation.getChunkPosition().equals(newLocation.getChunkPosition()))
//            return;
//
//        if (ModSupport.isFlan(event.getTargetEntity()))
//        {
//            final Optional<User> optionalPlayer = event.getContext().get(EventContextKeys.OWNER);
//            if (optionalPlayer.isPresent() && optionalPlayer.get() instanceof Player)
//            {
//                onPlayerMove(event, (Player) optionalPlayer.get());
//            }
//        }
//    }

    @Listener(order = Order.EARLY)
    public void onPlayerMove(final MoveEntityEvent event, final @Root ServerPlayer player)
    {
        final Vector3d lastLocation = event.originalPosition();
        final Vector3d newLocation = event.destinationPosition();

        if(lastLocation.equals(newLocation))
            return;

        final ServerWorld world = player.world();
        final Vector3i oldChunk = world.chunkAtBlock(lastLocation.toInt()).chunkPosition();
        final Vector3i newChunk = world.chunkAtBlock(newLocation.toInt()).chunkPosition();

        //TODO: Add checks for safezone, warzone and unclaimable worlds.

        final Optional<Faction> optionalOldChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(world.uniqueId(), oldChunk);
        final Optional<Faction> optionalNewChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(world.uniqueId(), newChunk);
        String oldChunkFactionName = "Wilderness";
        String newChunkFactionName = "Wilderness";

        if (optionalOldChunkFaction.isPresent())
            oldChunkFactionName = optionalOldChunkFaction.get().getName();

        if (optionalNewChunkFaction.isPresent())
            newChunkFactionName = optionalNewChunkFaction.get().getName();

        //Inform a player about entering faction's land.
        if (!oldChunkFactionName.equals(newChunkFactionName))
        {
            //Fire FactionAreaEnterEvent
            final boolean isCancelled = EventRunner.runFactionAreaEnterEventPre(event, player, optionalNewChunkFaction, optionalOldChunkFaction);
            if(isCancelled)
            {
                event.setCancelled(true);
                return;
            }

            if (!newChunkFactionName.equalsIgnoreCase("SafeZone") && !newChunkFactionName.equalsIgnoreCase("WarZone") && !newChunkFactionName.equalsIgnoreCase("Wilderness"))
            {
                if (!super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
                {
                    if (!getPlugin().getFactionLogic().hasOnlinePlayers(optionalNewChunkFaction.get()) && this.factionsConfig.getBlockEnteringFactions())
                    {
                        //Teleport player back if all entering faction's players are offline.
                        player.sendMessage(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_CANT_ENTER_THIS_FACTION + " " + Messages.NONE_OF_THIS_FACTIONS_PLAYERS_ARE_ONLINE, RED)));
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            else if (oldChunkFactionName.equalsIgnoreCase("WarZone") && newChunkFactionName.equalsIgnoreCase("SafeZone"))
            {
                if (!super.getPlugin().getPlayerManager().hasAdminMode(player.user()) && this.factionsConfig.shouldBlockEnteringSafezoneFromWarzone())
                {
                    if (super.getPlugin().getPlayerManager().getFactionPlayer(player.uniqueId()).get().diedInWarZone())
                    {
                        super.getPlugin().getPlayerManager().setDeathInWarZone(player.uniqueId(), false);
                    }
                    else
                    {
                        //Block player before going to SafeZone from WarZone
                        player.sendMessage(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_CANT_ENTER_SAFEZONE_WHEN_YOU_ARE_IN_WARZONE, RED)));
                        event.setCancelled(true);
                        return;
                    }
                }
            }


            //TODO: Show respective colors for enemy faction, alliance & neutral.
            //Show entering phrase
            if (this.chatConfig.shouldShowFactionEnterPhrase())
            {
                Component information;
                if(optionalNewChunkFaction.isPresent())
                {
                    information = Component.text()
                            .append(MessageLoader.parseMessage(Messages.YOU_HAVE_ENTERED_FACTION, WHITE, ImmutableMap.of(Placeholders.FACTION_NAME, Component.text(newChunkFactionName, GOLD))))
                            .build();
                    //TODO: Further consideration needed for code below... Long description does not look so good
//                    if(optionalNewChunkFaction.get().getDescription().equals(""))
//                    {
//                        information = Text.builder()
//                                .append(Text.of(PluginMessages.YOU_HAVE_ENTERED_FACTION + " ", TextColors.GOLD, newChunkFactionName))
//                                .build();
//                    }
//                    else
//                    {
////                        information = Text.builder()
////                                .append(Text.of(PluginMessages.YOU_HAVE_ENTERED_FACTION + " ", TextColors.GOLD, newChunkFactionName, TextColors.RESET, " - ", TextColors.AQUA, optionalNewChunkFaction.get().getDescription()))
////                                .build();
//                        information = Text.builder()
//                                .append(Text.of(PluginMessages.YOU_HAVE_ENTERED_FACTION + " ", TextColors.GOLD, newChunkFactionName))
//                                .build();
//                        player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GOLD, optionalNewChunkFaction.get().getName() + ": ", TextColors.RESET, optionalNewChunkFaction.get().getDescription())));
//                    }
                }
                else
                {
                    information = Component.text()
                            .append(MessageLoader.parseMessage(Messages.YOU_HAVE_ENTERED_FACTION, WHITE, ImmutableMap.of(Placeholders.FACTION_NAME, Component.text(newChunkFactionName, GOLD))))
                            .build();
                }

                player.sendActionBar(information);
            }

            EventRunner.runFactionAreaEnterEventPost(event, player, optionalNewChunkFaction, optionalOldChunkFaction);
        }

        //Is there any better way for doing this? :O
        //I am feeling bad that I needed to write something like this...
        EagleFactionsScheduler.getInstance().scheduleWithDelayAsync(() ->
        {
            try
            {
                //Check if player has tuned on AutoClaim
                if (EagleFactionsPlugin.AUTO_CLAIM_LIST.contains(player.uniqueId()))
                {
                    Sponge.server().commandManager().process(player, "f claim");
                }

                //Check if player has turned on AutoMap
                if (EagleFactionsPlugin.AUTO_MAP_LIST.contains(player.uniqueId()))
                {
                    Sponge.server().commandManager().process(player, "f map");
                }
            }
            catch (CommandException exception)
            {
                exception.printStackTrace();
            }
        }, 50, TimeUnit.MILLISECONDS);
    }
}