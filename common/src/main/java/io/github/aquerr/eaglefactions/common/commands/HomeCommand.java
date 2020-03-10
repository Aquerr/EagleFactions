package io.github.aquerr.eaglefactions.common.commands;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionHome;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
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
	private final FactionsConfig factionsConfig;

    public HomeCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        final Optional<Faction> optionalFaction = context.getOne(Text.of("faction"));

        if (!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        final Player player = (Player)source;

        if (optionalFaction.isPresent())
        {
            if (super.getPlugin().getPlayerManager().hasAdminMode(player))
            {
                final Faction faction = optionalFaction.get();
                if (faction.getHome() == null)
                    throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "This faction does not have its home set!"));

                teleportHome(player, player.getLocation().getBlockPosition(), faction.getHome());
            }
            else
            {
                final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
                if (!optionalPlayerFaction.isPresent())
                    throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

                final Faction faction = optionalFaction.get();
                if (!optionalPlayerFaction.get().getName().equals(faction.getName()) && !optionalPlayerFaction.get().getAlliances().contains(faction.getName()))
                    throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "You can't teleport to this faction's home! You must be its ally first!"));

                if(faction.getHome() == null)
                    throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "This faction does not have its home set!"));

                if (EagleFactionsPlugin.HOME_COOLDOWN_PLAYERS.containsKey(player.getUniqueId()))
                {
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, Messages.HOME_COMMAND_IS_CURRENTLY_ON_COOLDOWN + " " + Messages.YOU_NEED_TO_WAIT + " ", TextColors.YELLOW, EagleFactionsPlugin.HOME_COOLDOWN_PLAYERS.get(player.getUniqueId()) + " " + Messages.SECONDS + " ", TextColors.RED, Messages.TO_BE_ABLE_TO_USE_IT_AGAIN));
                    return CommandResult.success();
                }
                else if (this.factionsConfig.shouldBlockHomeAfterDeathInOwnFaction() && EagleFactionsPlugin.BLOCKED_HOME.containsKey(player.getUniqueId()))
                {
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, Messages.YOU_CANT_TELEPORT_TO_FACTIONS_HOME_BECAUSE_YOU_DIED_RECENTLY_IN_YOUR_FACTIONS_LAND));
                    return CommandResult.success();
                }
                else
                {
                    if(this.factionsConfig.canHomeBetweenWorlds())
                    {
                        teleportHome(player, player.getLocation().getBlockPosition(), faction.getHome());
                    }
                    else
                    {
                        if(player.getWorld().getUniqueId().equals(faction.getHome().getWorldUUID()))
                        {
                            player.sendMessage(ChatTypes.ACTION_BAR, Text.of(Messages.STAND_STILL_FOR + " ", TextColors.GOLD, this.factionsConfig.getHomeDelayTime() + " " + Messages.SECONDS, TextColors.RESET, "!"));
                            teleportHome(player, player.getLocation().getBlockPosition(), faction.getHome());
                        }
                        else
                        {
                            source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, Messages.FACTIONS_HOME_IS_NOT_SET_IN_THIS_WORLD));
                        }
                    }
                }
            }
        }
        else
        {
            final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());

            if (!optionalPlayerFaction.isPresent())
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

            final Faction playerFaction = optionalPlayerFaction.get();

            if (playerFaction.getHome() == null)
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.FACTIONS_HOME_IS_NOT_SET));

            if (super.getPlugin().getPlayerManager().hasAdminMode(player))
            {
                teleportHome(player, player.getLocation().getBlockPosition(), playerFaction.getHome());
                return CommandResult.success();
            }

            if (EagleFactionsPlugin.HOME_COOLDOWN_PLAYERS.containsKey(player.getUniqueId()))
            {
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, Messages.HOME_COMMAND_IS_CURRENTLY_ON_COOLDOWN + " " + Messages.YOU_NEED_TO_WAIT + " ", TextColors.YELLOW, EagleFactionsPlugin.HOME_COOLDOWN_PLAYERS.get(player.getUniqueId()) + " " + Messages.SECONDS + " ", TextColors.RED, Messages.TO_BE_ABLE_TO_USE_IT_AGAIN));
                return CommandResult.success();
            }
            else if (this.factionsConfig.shouldBlockHomeAfterDeathInOwnFaction() && EagleFactionsPlugin.BLOCKED_HOME.containsKey(player.getUniqueId()))
            {
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, Messages.YOU_CANT_TELEPORT_TO_FACTIONS_HOME_BECAUSE_YOU_DIED_RECENTLY_IN_YOUR_FACTIONS_LAND));
                return CommandResult.success();
            }
            else
            {
                if(this.factionsConfig.canHomeBetweenWorlds())
                {
                    teleportHome(player, player.getLocation().getBlockPosition(), playerFaction.getHome());
                }
                else
                {
                    if(player.getWorld().getUniqueId().equals(playerFaction.getHome().getWorldUUID()))
                    {
                        teleportHome(player, player.getLocation().getBlockPosition(), playerFaction.getHome());
                    }
                    else
                    {
                        source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, Messages.FACTIONS_HOME_IS_NOT_SET_IN_THIS_WORLD));
                    }
                }
            }
        }
        return CommandResult.success();
    }

    private void teleportHome(Player player, Vector3i lastBlockPosition, FactionHome factionHome)
    {
		if(this.factionsConfig.getHomeDelayTime() == 0)
		{
            teleport(player, factionHome);
			return;
		}

		player.sendMessage(ChatTypes.ACTION_BAR, Text.of(Messages.STAND_STILL_FOR + " ", TextColors.GOLD, this.factionsConfig.getHomeDelayTime() + " " + Messages.SECONDS, TextColors.RESET, "!"));

		final Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
        taskBuilder.interval(1, TimeUnit.SECONDS).delay(2, TimeUnit.SECONDS).execute(new Consumer<Task>()
        {
            int seconds = factionsConfig.getHomeDelayTime();

            @Override
            public void accept(Task task)
            {
                if(!player.getLocation().getBlockPosition().equals(lastBlockPosition))
                {
                    player.sendMessage(ChatTypes.ACTION_BAR, Text.of(TextColors.RED, Messages.YOU_MOVED + " " + Messages.TELEPORTING_HAS_BEEN_CANCELLED));
                    task.cancel();
                    return;
                }

                if (seconds <= 0)
                {
                    teleport(player, factionHome);
                    task.cancel();
                }
                else
                {
                    player.sendMessage(ChatTypes.ACTION_BAR, Text.of(TextColors.AQUA, "Teleporting to faction's home in [", TextColors.GOLD, seconds, TextColors.AQUA, "] seconds."));
                    seconds--;
                }
            }
        }).submit(super.getPlugin());
    }

    private void teleport(final Player player, final FactionHome factionHome)
    {
        final Optional<World> optionalWorld = Sponge.getServer().getWorld(factionHome.getWorldUUID());
        if(!optionalWorld.isPresent())
        {
            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "The home you are trying teleport to is in a world that is missing or is corrupted. "));
            return;
        }
        player.setLocationSafely(new Location<>(optionalWorld.get(), factionHome.getBlockPosition()));
        player.sendMessage(ChatTypes.ACTION_BAR, Text.of(TextColors.GREEN, Messages.YOU_WERE_TELEPORTED_TO_FACTIONS_HOME));
        startHomeCooldown(player.getUniqueId());
    }

    private void startHomeCooldown(UUID playerUUID)
    {
        EagleFactionsPlugin.HOME_COOLDOWN_PLAYERS.put(playerUUID, this.factionsConfig.getHomeCooldown());

        final Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

        taskBuilder.interval(1, TimeUnit.SECONDS).execute(task ->
        {
            if (EagleFactionsPlugin.HOME_COOLDOWN_PLAYERS.containsKey(playerUUID))
            {
                int seconds = EagleFactionsPlugin.HOME_COOLDOWN_PLAYERS.get(playerUUID);

                if(seconds < 1)
                {
                    EagleFactionsPlugin.HOME_COOLDOWN_PLAYERS.remove(playerUUID);
                    task.cancel();
                }
                EagleFactionsPlugin.HOME_COOLDOWN_PLAYERS.replace(playerUUID, seconds, seconds - 1);
            }
        }).submit(super.getPlugin());
    }
}
