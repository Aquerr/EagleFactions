package io.github.aquerr.eaglefactions.commands.general;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionHome;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.messaging.Placeholders;
import io.github.aquerr.eaglefactions.scheduling.EagleFactionsConsumerTask;
import io.github.aquerr.eaglefactions.scheduling.EagleFactionsScheduler;
import io.github.aquerr.eaglefactions.util.ParticlesUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class HomeCommand extends AbstractCommand
{
    private final FactionsConfig factionsConfig;

    public HomeCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final Optional<Faction> optionalFaction = context.one(EagleFactionsCommandParameters.faction());

        final ServerPlayer player = requirePlayerSource(context);

        if (optionalFaction.isPresent())
        {
            if (super.getPlugin().getPlayerManager().hasAdminMode(player))
            {
                final Faction faction = optionalFaction.get();
                if (faction.getHome() == null)
                    throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.THIS_FACTION_DOES_NOT_HAVE_ITS_HOME_SET, NamedTextColor.RED)));

                teleportHome(player, player.serverLocation().blockPosition(), faction.getHome());
            }
            else
            {
                final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
                if (!optionalPlayerFaction.isPresent())
                    throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND, NamedTextColor.RED)));

                final Faction faction = optionalFaction.get();
                if (!optionalPlayerFaction.get().getName().equals(faction.getName()) && !optionalPlayerFaction.get().getAlliances().contains(faction.getName()))
                    throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_CANT_TELEPORT_TO_THIS_FACTION_HOME_ALLIANCE_NEEDED, NamedTextColor.RED)));

                if (faction.getHome() == null)
                    throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.THIS_FACTION_DOES_NOT_HAVE_ITS_HOME_SET, NamedTextColor.RED)));

                if (EagleFactionsPlugin.HOME_COOLDOWN_PLAYERS.containsKey(player.uniqueId()))
                {
                    player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.HOME_COMMAND_IS_CURRENTLY_ON_COOLDOWN + " " + Messages.YOU_NEED_TO_WAIT + " ", NamedTextColor.RED)).append(Component.text(EagleFactionsPlugin.HOME_COOLDOWN_PLAYERS.get(player.uniqueId()) + " " + Messages.SECONDS + " ", NamedTextColor.YELLOW).append(Component.text(Messages.TO_BE_ABLE_TO_USE_IT_AGAIN, NamedTextColor.RED))));
                    return CommandResult.success();
                }
                else if (this.factionsConfig.shouldBlockHomeAfterDeathInOwnFaction() && EagleFactionsPlugin.BLOCKED_HOME.containsKey(player.uniqueId()))
                {
                    player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.YOU_CANT_TELEPORT_TO_FACTIONS_HOME_BECAUSE_YOU_DIED_RECENTLY_IN_YOUR_FACTIONS_LAND, NamedTextColor.RED)));
                    return CommandResult.success();
                }
                else
                {
                    if (this.factionsConfig.canHomeBetweenWorlds())
                    {
                        teleportHome(player, player.serverLocation().blockPosition(), faction.getHome());
                    }
                    else
                    {
                        if (player.world().uniqueId().equals(faction.getHome().getWorldUUID()))
                        {
                            player.sendActionBar(Component.text(Messages.STAND_STILL_FOR + " ").append(Component.text(this.factionsConfig.getHomeDelayTime() + " " + Messages.SECONDS, NamedTextColor.GOLD).append(Component.text("!"))));
                            teleportHome(player, player.serverLocation().blockPosition(), faction.getHome());
                        }
                        else
                        {
                            player.sendMessage(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.FACTIONS_HOME_IS_NOT_SET_IN_THIS_WORLD)));
                        }
                    }
                }
            }
        }
        else
        {
            final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());

            if (!optionalPlayerFaction.isPresent())
                throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND, NamedTextColor.RED)));

            final Faction playerFaction = optionalPlayerFaction.get();

            if (playerFaction.getHome() == null)
                throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.FACTIONS_HOME_IS_NOT_SET, NamedTextColor.RED)));

            if (super.getPlugin().getPlayerManager().hasAdminMode(player))
            {
                teleportHome(player, player.serverLocation().blockPosition(), playerFaction.getHome());
                return CommandResult.success();
            }

            if (EagleFactionsPlugin.HOME_COOLDOWN_PLAYERS.containsKey(player.uniqueId()))
            {
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.HOME_COMMAND_IS_CURRENTLY_ON_COOLDOWN + " " + Messages.YOU_NEED_TO_WAIT + " ", NamedTextColor.RED).append(Component.text(EagleFactionsPlugin.HOME_COOLDOWN_PLAYERS.get(player.uniqueId()) + " " + Messages.SECONDS + " ", NamedTextColor.YELLOW).append(Component.text(Messages.TO_BE_ABLE_TO_USE_IT_AGAIN, NamedTextColor.RED)))));
                return CommandResult.success();
            }
            else if (this.factionsConfig.shouldBlockHomeAfterDeathInOwnFaction() && EagleFactionsPlugin.BLOCKED_HOME.containsKey(player.uniqueId()))
            {
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.YOU_CANT_TELEPORT_TO_FACTIONS_HOME_BECAUSE_YOU_DIED_RECENTLY_IN_YOUR_FACTIONS_LAND, NamedTextColor.RED)));
                return CommandResult.success();
            }
            else
            {
                if (this.factionsConfig.canHomeBetweenWorlds())
                {
                    teleportHome(player, player.serverLocation().blockPosition(), playerFaction.getHome());
                }
                else
                {
                    if (player.world().uniqueId().equals(playerFaction.getHome().getWorldUUID()))
                    {
                        teleportHome(player, player.serverLocation().blockPosition(), playerFaction.getHome());
                    }
                    else
                    {
                        player.sendMessage(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.FACTIONS_HOME_IS_NOT_SET_IN_THIS_WORLD)));
                    }
                }
            }
        }
        return CommandResult.success();
    }

    private void teleportHome(ServerPlayer player, Vector3i lastBlockPosition, FactionHome factionHome)
    {
        if (this.factionsConfig.getHomeDelayTime() == 0 || player.hasPermission(PluginPermissions.HOME_COMMAND_NO_DELAY))
        {
            teleport(player, factionHome);
            return;
        }

        player.sendActionBar(Component.text(Messages.STAND_STILL_FOR + " ").append(Component.text(this.factionsConfig.getHomeDelayTime() + " " + Messages.SECONDS, NamedTextColor.GOLD)).append(Component.text("!")));

        final EagleFactionsScheduler eagleFactionsScheduler = EagleFactionsScheduler.getInstance();
        eagleFactionsScheduler.scheduleWithDelayedInterval(new ParticlesUtil.HomeParticles(player), 0, TimeUnit.SECONDS, 50, TimeUnit.MILLISECONDS);
        eagleFactionsScheduler.scheduleWithDelayedInterval(new EagleFactionsConsumerTask<ScheduledTask>()
        {
            int seconds = factionsConfig.getHomeDelayTime();

            @Override
            public void accept(ScheduledTask task)
            {
                if (!player.serverLocation().blockPosition().equals(lastBlockPosition))
                {
                    player.sendActionBar(Component.text(Messages.YOU_MOVED + " " + Messages.TELEPORTING_HAS_BEEN_CANCELLED, NamedTextColor.RED));
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
                    player.sendActionBar(MessageLoader.parseMessage(Messages.TELEPORTING_TO_FACTION_HOME, NamedTextColor.AQUA, ImmutableMap.of(Placeholders.NUMBER, Component.text(seconds, NamedTextColor.GOLD))).color(NamedTextColor.RED));
                    seconds--;
                }
            }
        }, 2, TimeUnit.SECONDS, 1, TimeUnit.SECONDS);
    }

    private void teleport(final Player player, final FactionHome factionHome)
    {
        final Optional<ServerWorld> optionalWorld = Sponge.server().worldManager().worlds().stream()
                .filter(world -> world.uniqueId().equals(factionHome.getWorldUUID()))
                .findFirst();
        if (!optionalWorld.isPresent())
        {
            player.sendMessage(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.MISSING_OR_CORRUPTED_HOME, NamedTextColor.RED)));
            return;
        }

        Optional<ServerLocation> safeLocation = Sponge.server().teleportHelper().findSafeLocation(ServerLocation.of(optionalWorld.get(), factionHome.getBlockPosition()));
        if (!safeLocation.isPresent())
        {
            player.sendMessage(Component.text("Home location is not safe. Teleport is cancelled!", NamedTextColor.RED));
        }
        else
        {
            player.setLocation(safeLocation.get());
            player.sendActionBar(Component.text(Messages.YOU_WERE_TELEPORTED_TO_FACTIONS_HOME, NamedTextColor.GREEN));
            startHomeCooldown(player.uniqueId());
        }
    }

    private void startHomeCooldown(UUID playerUUID)
    {
        EagleFactionsPlugin.HOME_COOLDOWN_PLAYERS.put(playerUUID, this.factionsConfig.getHomeCooldown());

        final Task scheduledTask = Task.builder().interval(1, TimeUnit.SECONDS).execute(task ->
        {
            if (EagleFactionsPlugin.HOME_COOLDOWN_PLAYERS.containsKey(playerUUID))
            {
                int seconds = EagleFactionsPlugin.HOME_COOLDOWN_PLAYERS.get(playerUUID);

                if (seconds < 1)
                {
                    EagleFactionsPlugin.HOME_COOLDOWN_PLAYERS.remove(playerUUID);
                    task.cancel();
                }
                EagleFactionsPlugin.HOME_COOLDOWN_PLAYERS.replace(playerUUID, seconds, seconds - 1);
            }
        }).build();
        Sponge.asyncScheduler().submit(scheduledTask);
    }
}
