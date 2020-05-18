package io.github.aquerr.eaglefactions.common.listeners;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.events.EventRunner;
import io.github.aquerr.eaglefactions.common.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import io.github.aquerr.eaglefactions.common.messaging.Placeholders;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

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

    @Listener(order = Order.EARLY)
    public void onPlayerMove(final MoveEntityEvent event, final @Root Player player)
    {
        final Location<World> lastLocation = event.getFromTransform().getLocation();
        final Location<World> newLocation = event.getToTransform().getLocation();

        if(lastLocation.getChunkPosition().equals(newLocation.getChunkPosition()))
            return;

        final World world = player.getWorld();
        final Vector3i oldChunk = lastLocation.getChunkPosition();
        final Vector3i newChunk = newLocation.getChunkPosition();

        //TODO: Add checks for safezone, warzone and unclaimable worlds.

        final Optional<Faction> optionalOldChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), oldChunk);
        final Optional<Faction> optionalNewChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), newChunk);
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
            final boolean isCancelled = EventRunner.runFactionAreaEnterEvent(event, player, optionalNewChunkFaction, optionalOldChunkFaction);
            if(isCancelled)
            {
                event.setCancelled(true);
                return;
            }

            if (!newChunkFactionName.equalsIgnoreCase("SafeZone") && !newChunkFactionName.equalsIgnoreCase("WarZone") && !newChunkFactionName.equalsIgnoreCase("Wilderness"))
            {
                if (!super.getPlugin().getPlayerManager().hasAdminMode(player))
                {
                    if (!getPlugin().getFactionLogic().hasOnlinePlayers(optionalNewChunkFaction.get()) && this.factionsConfig.getBlockEnteringFactions())
                    {
                        //Teleport player back if all entering faction's players are offline.
                        player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_ENTER_THIS_FACTION + " " + Messages.NONE_OF_THIS_FACTIONS_PLAYERS_ARE_ONLINE));
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            else if (oldChunkFactionName.equalsIgnoreCase("WarZone") && newChunkFactionName.equalsIgnoreCase("SafeZone"))
            {
                if (!super.getPlugin().getPlayerManager().hasAdminMode(player) && this.factionsConfig.shouldBlockEnteringSafezoneFromWarzone())
                {
                    if (super.getPlugin().getPlayerManager().getFactionPlayer(player.getUniqueId()).get().diedInWarZone())
                    {
                        super.getPlugin().getPlayerManager().setDeathInWarZone(player.getUniqueId(), false);
                    }
                    else
                    {
                        //Block player before going to SafeZone from WarZone
                        player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_ENTER_SAFEZONE_WHEN_YOU_ARE_IN_WARZONE));
                        event.setCancelled(true);
                        return;
                    }
                }
            }


            //TODO: Show respective colors for enemy faction, alliance & neutral.
            //Show entering phrase
            if (this.chatConfig.shouldShowFactionEnterPhrase())
            {
                Text information;
                if(optionalNewChunkFaction.isPresent())
                {
                    information = Text.builder()
                            .append(Text.of(MessageLoader.parseMessage(Messages.YOU_HAVE_ENTERED_FACTION, TextColors.RESET, ImmutableMap.of(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, newChunkFactionName)))))
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
                    information = Text.builder()
                            .append(Text.of(MessageLoader.parseMessage(Messages.YOU_HAVE_ENTERED_FACTION, TextColors.RESET, ImmutableMap.of(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, newChunkFactionName)))))
                            .build();
                }

                player.sendMessage(ChatTypes.ACTION_BAR, information);
            }
        }

        //Is there any better way for doing this? :O
        //I am feeling bad that I needed to write something like this...
        final Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
        taskBuilder.delay(50, TimeUnit.MILLISECONDS).execute(()->{
            //Check if player has tuned on AutoClaim
            if (EagleFactionsPlugin.AUTO_CLAIM_LIST.contains(player.getUniqueId()))
            {
                Sponge.getCommandManager().process(player, "f claim");
            }

            //Check if player has turned on AutoMap
            if (EagleFactionsPlugin.AUTO_MAP_LIST.contains(player.getUniqueId()))
            {
                Sponge.getCommandManager().process(player, "f map");
            }
        }).submit(super.getPlugin());
    }
}