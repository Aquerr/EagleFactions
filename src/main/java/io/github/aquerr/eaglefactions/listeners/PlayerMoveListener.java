package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.scheduling.EagleFactionsScheduler;
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

public class PlayerMoveListener extends AbstractListener
{
    private final MessageService messageService;
    private final FactionsConfig factionsConfig;
    private final FactionLogic factionLogic;
    private final PlayerManager playerManager;
    private final ChatConfig chatConfig;

    public PlayerMoveListener(final EagleFactions plugin)
    {
        super(plugin);
        this.messageService = plugin.getMessageService();
        this.factionLogic = plugin.getFactionLogic();
        this.playerManager = plugin.getPlayerManager();
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.chatConfig = plugin.getConfiguration().getChatConfig();
    }

    @Listener(order = Order.EARLY)
    public void onPlayerMove(final MoveEntityEvent event, final @Root ServerPlayer player)
    {
        final Vector3d lastLocation = event.originalPosition();
        final Vector3d newLocation = event.destinationPosition();

        final ServerWorld world = player.world();
        final Vector3i oldChunk = world.chunkAtBlock(lastLocation.toInt()).chunkPosition();
        final Vector3i newChunk = world.chunkAtBlock(newLocation.toInt()).chunkPosition();

        if(oldChunk.equals(newChunk))
            return;

        //TODO: Add checks for safezone, warzone and unclaimable worlds.

        final Optional<Faction> optionalOldChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(world.uniqueId(), oldChunk);
        final Optional<Faction> optionalNewChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(world.uniqueId(), newChunk);
        String oldChunkFactionName = EagleFactionsPlugin.WILDERNESS_NAME;
        String newChunkFactionName = EagleFactionsPlugin.WILDERNESS_NAME;

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

            if (!newChunkFactionName.equalsIgnoreCase(EagleFactionsPlugin.SAFE_ZONE_NAME)
                    && !newChunkFactionName.equalsIgnoreCase(EagleFactionsPlugin.WAR_ZONE_NAME)
                    && !newChunkFactionName.equalsIgnoreCase(EagleFactionsPlugin.WILDERNESS_NAME))
            {
                if (!this.playerManager.hasAdminMode(player.user()))
                {
                    if (!this.factionLogic.hasOnlinePlayers(optionalNewChunkFaction.get()) && this.factionsConfig.getBlockEnteringFactions())
                    {
                        //Teleport player back if all entering faction's players are offline.
                        player.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage("error.move.you-cant-enter-this-faction-none-its-members-are-online")));
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            else if (oldChunkFactionName.equalsIgnoreCase(EagleFactionsPlugin.WAR_ZONE_NAME)
                    && newChunkFactionName.equalsIgnoreCase(EagleFactionsPlugin.SAFE_ZONE_NAME))
            {
                if (!this.playerManager.hasAdminMode(player.user()) && this.factionsConfig.shouldBlockEnteringSafezoneFromWarzone())
                {
                    if (this.playerManager.getFactionPlayer(player.uniqueId()).get().diedInWarZone())
                    {
                        this.playerManager.setDeathInWarZone(player.uniqueId(), false);
                    }
                    else
                    {
                        //Block player before going to SafeZone from WarZone
                        player.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage("error.move.you-cant-enter-safezone-when-you-are-in-warzone")));
                        event.setCancelled(true);
                        return;
                    }
                }
            }


            //TODO: Show respective colors for enemy faction, alliance & neutral.
            //Show entering phrase
            if (this.chatConfig.shouldShowFactionEnterPhrase())
            {
                player.sendActionBar(messageService.resolveComponentWithMessage("move.you-entered-faction", newChunkFactionName));
            }

            EventRunner.runFactionAreaEnterEventPost(event, player, optionalNewChunkFaction, optionalOldChunkFaction);
        }

        if ("Wilderness".equals(newChunkFactionName))
        {
            EagleFactionsScheduler.getInstance().scheduleWithDelayAsync(() ->
            {
                try
                {
                    //Check if player has tuned on AutoClaim
                    if (EagleFactionsPlugin.AUTO_CLAIM_LIST.contains(player.uniqueId()))
                    {
                        Sponge.server().commandManager().process(player, "f claim");
                    }
                }
                catch (CommandException exception)
                {
                    exception.printStackTrace();
                }
            }, 50, TimeUnit.MILLISECONDS);
        }

        EagleFactionsScheduler.getInstance().scheduleWithDelayAsync(() ->
        {
            try
            {
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