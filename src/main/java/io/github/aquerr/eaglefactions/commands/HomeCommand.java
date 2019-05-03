package io.github.aquerr.eaglefactions.commands;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionHome;
import io.github.aquerr.eaglefactions.message.PluginMessages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class HomeCommand extends AbstractCommand
{
    public HomeCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if(source instanceof Player)
        {
            Player player = (Player)source;
            Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());

            if(optionalPlayerFaction.isPresent())
            {
                Faction playerFaction = optionalPlayerFaction.get();

                if(playerFaction.getHome() != null)
                {
                    if (EagleFactions.HOME_COOLDOWN_PLAYERS.containsKey(player.getUniqueId()))
                    {
                        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, PluginMessages.HOME_COMMAND_IS_CURRENTLY_ON_COOLDOWN + " " + PluginMessages.YOU_NEED_TO_WAIT + " ", TextColors.YELLOW, EagleFactions.HOME_COOLDOWN_PLAYERS.get(player.getUniqueId()) + " " + PluginMessages.SECONDS + " ", TextColors.RED, PluginMessages.TO_BE_ABLE_TO_USE_IT_AGAIN));
                        return CommandResult.success();
                    }
                    else if (getPlugin().getConfiguration().getConfigFields().shouldBlockHomeAfterDeathInOwnFaction() && EagleFactions.BLOCKED_HOME.containsKey(player.getUniqueId()))
                    {
                        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, PluginMessages.YOU_CANT_TELEPORT_TO_FACTIONS_HOME_BECAUSE_YOU_DIED_RECENTLY_IN_YOUR_FACTIONS_LAND));
                        return CommandResult.success();
                    }
                    else
                    {
                        if(getPlugin().getConfiguration().getConfigFields().canHomeBetweenWorlds())
                        {
                            player.sendMessage(ChatTypes.ACTION_BAR, Text.of(PluginMessages.STAND_STILL_FOR + " ", TextColors.GOLD, getPlugin().getConfiguration().getConfigFields().getHomeDelayTime() + " " + PluginMessages.SECONDS, TextColors.RESET, "!"));
                            teleportHome(player, player.getLocation().getBlockPosition(), playerFaction.getHome());
                        }
                        else
                        {
                            if(player.getWorld().getUniqueId().equals(playerFaction.getHome().getWorldUUID()))
                            {
                                player.sendMessage(ChatTypes.ACTION_BAR, Text.of(PluginMessages.STAND_STILL_FOR + " ", TextColors.GOLD, getPlugin().getConfiguration().getConfigFields().getHomeDelayTime() + " " + PluginMessages.SECONDS, TextColors.RESET, "!"));
                                teleportHome(player, player.getLocation().getBlockPosition(), playerFaction.getHome());
                            }
                            else
                            {
                                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, PluginMessages.FACTIONS_HOME_IS_NOT_SET_IN_THIS_WORLD));
                            }
                        }
                    }
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.FACTIONS_HOME_IS_NOT_SET));
                }

            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
            }

        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
        }

        return CommandResult.success();
    }

    private void teleportHome(Player player, Vector3i lastBlockPosition, FactionHome factionHome)
    {
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

        taskBuilder.interval(1, TimeUnit.SECONDS).delay(2, TimeUnit.SECONDS).execute(new Consumer<Task>()
        {
            int seconds = getPlugin().getConfiguration().getConfigFields().getHomeDelayTime();

            @Override
            public void accept(Task task)
            {
                if (player.getLocation().getBlockPosition().equals(lastBlockPosition))
                {
                    if (seconds <= 0)
                    {
                        player.setLocation(new Location<World>(Sponge.getServer().getWorld(factionHome.getWorldUUID()).get(), factionHome.getBlockPosition()));
                        player.sendMessage(ChatTypes.ACTION_BAR, Text.of(TextColors.GREEN, PluginMessages.YOU_WERE_TELEPORTED_TO_FACTIONS_HOME));
                        startHomeCooldown(player.getUniqueId());
                        task.cancel();
                    }
                    else
                    {
                        player.sendMessage(ChatTypes.ACTION_BAR, Text.of(TextColors.AQUA, "Teleporting to faction's home in [", TextColors.GOLD, seconds, TextColors.AQUA, "] seconds."));
                        seconds--;
                    }
                }
                else
                {
                    player.sendMessage(ChatTypes.ACTION_BAR, Text.of(TextColors.RED, PluginMessages.YOU_MOVED + " " + PluginMessages.TELEPORTING_HAS_BEEN_CANCELLED));
                    task.cancel();
                }
            }
        }).submit(EagleFactions.getPlugin());
    }

    private void startHomeCooldown(UUID playerUUID)
    {
        EagleFactions.HOME_COOLDOWN_PLAYERS.put(playerUUID, getPlugin().getConfiguration().getConfigFields().getHomeCooldown());

        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

        taskBuilder.interval(1, TimeUnit.SECONDS).execute(new Consumer<Task>()
        {
            @Override
            public void accept(Task task)
            {
                if (EagleFactions.HOME_COOLDOWN_PLAYERS.containsKey(playerUUID))
                {
                    int seconds = EagleFactions.HOME_COOLDOWN_PLAYERS.get(playerUUID);

                    if(seconds < 1)
                    {
                        EagleFactions.HOME_COOLDOWN_PLAYERS.remove(playerUUID);
                        task.cancel();
                    }
                    EagleFactions.HOME_COOLDOWN_PLAYERS.replace(playerUUID, seconds, seconds - 1);
                }
            }
        }).submit(EagleFactions.getPlugin());
    }
}
