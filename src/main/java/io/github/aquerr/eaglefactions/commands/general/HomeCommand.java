package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.HomeConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionHome;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import io.github.aquerr.eaglefactions.scheduling.EagleFactionsConsumerTask;
import io.github.aquerr.eaglefactions.scheduling.EagleFactionsScheduler;
import io.github.aquerr.eaglefactions.util.WorldUtil;
import net.kyori.adventure.identity.Identity;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class HomeCommand extends AbstractCommand
{
    private final HomeConfig homeConfig;
    private final MessageService messageService;

    public HomeCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.homeConfig = plugin.getConfiguration().getHomeConfig();
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final Optional<Faction> optionalFaction = context.one(EagleFactionsCommandParameters.optionalFaction());
        final ServerPlayer player = requirePlayerSource(context);

        if (optionalFaction.isPresent())
        {
            if (player.hasPermission(PluginPermissions.HOME_COMMAND_ADMIN_TELEPORT_TO_OTHERS) || super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
            {
                final Faction faction = optionalFaction.get();
                if (faction.getHome() == null)
                    throw messageService.resolveExceptionWithMessage("error.command.home.faction-does-not-have-set-up-its-home");

                teleportHome(player, faction.getHome());
            }
            else
            {
                final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
                if (!optionalPlayerFaction.isPresent())
                    throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND_MESSAGE_KEY);

                final Faction faction = optionalFaction.get();
                if (!optionalPlayerFaction.get().getName().equals(faction.getName()) && !optionalPlayerFaction.get().getAlliances().contains(faction.getName()))
                    throw messageService.resolveExceptionWithMessage("error.command.home.you-cant-teleport-to-this-faction-home-alliance-needed");

                if (faction.getHome() == null)
                    throw messageService.resolveExceptionWithMessage("error.command.home.faction-does-not-have-set-up-its-home");

                if (EagleFactionsPlugin.HOME_COOLDOWN_PLAYERS.containsKey(player.uniqueId()))
                {
                    player.sendMessage(messageService.resolveMessageWithPrefix("command.home.home-is-on-cooldown", EagleFactionsPlugin.HOME_COOLDOWN_PLAYERS.get(player.uniqueId())));
                    return CommandResult.success();
                }
                else if (this.homeConfig.shouldBlockHomeAfterDeathInOwnFaction() && EagleFactionsPlugin.BLOCKED_HOME.containsKey(player.uniqueId()))
                {
                    player.sendMessage(messageService.resolveMessageWithPrefix("command.home.cant-teleport-because-of-recent-death"));
                    return CommandResult.success();
                }
                else
                {
                    if (this.homeConfig.canHomeBetweenWorlds())
                    {
                        teleportHome(player, faction.getHome());
                    }
                    else
                    {
                        if (player.world().uniqueId().equals(faction.getHome().getWorldUUID()))
                        {
                            player.sendActionBar(messageService.resolveComponentWithMessage("command.home.stand-still", this.homeConfig.getHomeDelayTime()));
                            teleportHome(player, faction.getHome());
                        }
                        else
                        {
                            context.sendMessage(Identity.nil(), PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage("error.command.home.faction-home-not-in-this-world")));
                        }
                    }
                }
            }
        }
        else
        {
            final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());

            if (!optionalPlayerFaction.isPresent())
                throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND_MESSAGE_KEY);

            final Faction playerFaction = optionalPlayerFaction.get();

            if (playerFaction.getHome() == null)
                throw messageService.resolveExceptionWithMessage("error.command.home.faction-does-not-have-set-up-its-home");

            if (super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
            {
                teleportHome(player, playerFaction.getHome());
                return CommandResult.success();
            }

            if (EagleFactionsPlugin.HOME_COOLDOWN_PLAYERS.containsKey(player.uniqueId()))
            {
                player.sendMessage(messageService.resolveMessageWithPrefix("command.home.home-is-on-cooldown", EagleFactionsPlugin.HOME_COOLDOWN_PLAYERS.get(player.uniqueId())));
                return CommandResult.success();
            }
            else if (this.homeConfig.shouldBlockHomeAfterDeathInOwnFaction() && EagleFactionsPlugin.BLOCKED_HOME.containsKey(player.uniqueId()))
            {
                player.sendMessage(messageService.resolveMessageWithPrefix("command.home.cant-teleport-because-of-recent-death"));
                return CommandResult.success();
            }
            else
            {
                if (this.homeConfig.canHomeBetweenWorlds())
                {
                    teleportHome(player, playerFaction.getHome());
                }
                else
                {
                    if (player.world().uniqueId().equals(playerFaction.getHome().getWorldUUID()))
                    {
                        teleportHome(player, playerFaction.getHome());
                    }
                    else
                    {
                        context.sendMessage(Identity.nil(), PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage("error.command.home.faction-home-not-in-this-world")));
                    }
                }
            }
        }
        return CommandResult.success();
    }

    private void teleportHome(ServerPlayer player, FactionHome factionHome)
    {
        if (this.homeConfig.getHomeDelayTime() == 0
                || player.hasPermission(PluginPermissions.HOME_COMMAND_ADMIN_NO_DELAY)
        )
        {
            teleport(player, factionHome);
            return;
        }

        player.sendActionBar(messageService.resolveComponentWithMessage("command.home.stand-still", this.homeConfig.getHomeDelayTime()));

        HomeTeleportAnimationTasks homeTeleportAnimationTasks = new HomeTeleportAnimationTasks();
        EagleFactionsScheduler eagleFactionsScheduler = EagleFactionsScheduler.getInstance();
        if (this.homeConfig.isSourceTeleportAnimationEnabled())
        {
            ScheduledTask scheduledTask = spawnTeleportAnimationAtSource(eagleFactionsScheduler, player);
            homeTeleportAnimationTasks.setSourceAnimationTask(scheduledTask);
        }
        if (this.homeConfig.isDestinationTeleportAnimationEnabled())
        {
            ScheduledTask scheduledTask = spawnTeleportAnimationAtDestination(eagleFactionsScheduler, player, factionHome);
            homeTeleportAnimationTasks.setDestinationAnimationTask(scheduledTask);
        }

        scheduleTeleportHome(eagleFactionsScheduler, player, factionHome, homeTeleportAnimationTasks);
    }

    private void scheduleTeleportHome(EagleFactionsScheduler eagleFactionsScheduler, ServerPlayer player, FactionHome factionHome, HomeTeleportAnimationTasks homeTeleportAnimationTasks)
    {
        eagleFactionsScheduler.scheduleWithDelayedInterval(
                new HomeTeleportTask(
                        messageService,
                        homeConfig,
                        player,
                        factionHome,
                        homeTeleportAnimationTasks
                ),
                2,
                TimeUnit.SECONDS,
                1,
                TimeUnit.SECONDS
        );
    }

    private ScheduledTask spawnTeleportAnimationAtSource(EagleFactionsScheduler eagleFactionsScheduler, ServerPlayer player)
    {
        return eagleFactionsScheduler.scheduleWithDelayedInterval(
                new HomeParticlesTask(
                        player,
                        player.serverLocation()
                ),
                0,
                TimeUnit.SECONDS,
                50,
                TimeUnit.MILLISECONDS
        );
    }

    private ScheduledTask spawnTeleportAnimationAtDestination(EagleFactionsScheduler eagleFactionsScheduler, ServerPlayer player, FactionHome factionHome)
    {
        return eagleFactionsScheduler.scheduleWithDelayedInterval(
                new HomeParticlesTask(player,
                        WorldUtil.getBlockTopCenter(ServerLocation.of(WorldUtil.getWorldByUUID(factionHome.getWorldUUID()).get(), factionHome.getBlockPosition()))
                ),
                0,
                TimeUnit.SECONDS,
                50,
                TimeUnit.MILLISECONDS
        );
    }

    private void teleport(final ServerPlayer player, final FactionHome factionHome)
    {
        final Optional<ServerWorld> optionalWorld = WorldUtil.getWorldByUUID(factionHome.getWorldUUID());
        if (!optionalWorld.isPresent())
        {
            player.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage("command.home.missing-or-corrupted-hme")));
            return;
        }
        ServerLocation safeLocation = Sponge.server().teleportHelper().findSafeLocation(ServerLocation.of(optionalWorld.get(), factionHome.getBlockPosition()))
                .orElseGet(() -> ServerLocation.of(optionalWorld.get(), factionHome.getBlockPosition()));
        player.setLocation(safeLocation);
        player.sendActionBar(messageService.resolveComponentWithMessage("command.home.teleport-success"));
        startHomeCooldown(player.uniqueId());
    }

    private void startHomeCooldown(UUID playerUUID)
    {
        EagleFactionsPlugin.HOME_COOLDOWN_PLAYERS.put(playerUUID, this.homeConfig.getHomeCooldown());
        EagleFactionsScheduler.getInstance().scheduleWithDelayedIntervalAsync(new HomeCooldownTask(playerUUID),
                0, TimeUnit.SECONDS,
                1, TimeUnit.SECONDS
        );
    }

    private static class HomeTeleportTask implements EagleFactionsConsumerTask<ScheduledTask>
    {
        private final HomeConfig homeConfig;
        private final MessageService messageService;
        private final Vector3i playerLastBlockPosition;
        private final ServerPlayer player;
        private final FactionHome factionHome;
        private final HomeTeleportAnimationTasks homeTeleportAnimationTasks;
        private int seconds;

        public HomeTeleportTask(MessageService messageService, HomeConfig homeConfig, ServerPlayer player, FactionHome factionHome, HomeTeleportAnimationTasks homeTeleportAnimationTasks)
        {
            this.messageService = messageService;
            this.homeTeleportAnimationTasks = homeTeleportAnimationTasks;
            this.player = player;
            this.playerLastBlockPosition = player.blockPosition();
            this.factionHome = factionHome;
            this.homeConfig = homeConfig;
            this.seconds = homeConfig.getHomeDelayTime();
        }

        @Override
        public void accept(ScheduledTask task)
        {
            if (!player.serverLocation().blockPosition().equals(playerLastBlockPosition))
            {
                player.sendActionBar(messageService.resolveComponentWithMessage("command.home.you-moved"));
                cancelAnimations();
                task.cancel();
                return;
            }

            if (seconds <= 0)
            {
                teleport(player, factionHome);
                cancelAnimations();
                task.cancel();
            }
            else
            {
                player.sendActionBar(messageService.resolveComponentWithMessage("command.home.teleporting", seconds));
                seconds--;
            }
        }

        private void cancelAnimations()
        {
            if (homeTeleportAnimationTasks.getSourceAnimationTask() != null)
                homeTeleportAnimationTasks.getSourceAnimationTask().cancel();
            if (homeTeleportAnimationTasks.getDestinationAnimationTask() != null)
                homeTeleportAnimationTasks.getDestinationAnimationTask().cancel();
        }

        private void teleport(final ServerPlayer player, final FactionHome factionHome)
        {
            final Optional<ServerWorld> optionalWorld = WorldUtil.getWorldByUUID(factionHome.getWorldUUID());
            if (!optionalWorld.isPresent())
            {
                player.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage("command.home.missing-or-corrupted-hme")));
                return;
            }
            ServerLocation safeLocation = Sponge.server().teleportHelper().findSafeLocation(ServerLocation.of(optionalWorld.get(), factionHome.getBlockPosition()))
                    .orElseGet(() -> ServerLocation.of(optionalWorld.get(), factionHome.getBlockPosition()));
            player.setLocation(safeLocation);
            player.sendActionBar(messageService.resolveComponentWithMessage("command.home.teleport-success"));
            startHomeCooldown(player.uniqueId());
        }

        private void startHomeCooldown(UUID playerUUID)
        {
            EagleFactionsPlugin.HOME_COOLDOWN_PLAYERS.put(playerUUID, this.homeConfig.getHomeCooldown());
            EagleFactionsScheduler.getInstance().scheduleWithDelayedIntervalAsync(new HomeCooldownTask(playerUUID),
                    0, TimeUnit.SECONDS,
                    1, TimeUnit.SECONDS
            );
        }
    }

    private static class HomeCooldownTask implements EagleFactionsConsumerTask<ScheduledTask>
    {
        private final UUID playerUUID;

        public HomeCooldownTask(UUID playerUUID)
        {
            this.playerUUID = playerUUID;
        }

        @Override
        public void accept(ScheduledTask scheduledTask)
        {
            if (EagleFactionsPlugin.HOME_COOLDOWN_PLAYERS.containsKey(playerUUID))
            {
                int seconds = EagleFactionsPlugin.HOME_COOLDOWN_PLAYERS.get(playerUUID);

                if (seconds < 1)
                {
                    EagleFactionsPlugin.HOME_COOLDOWN_PLAYERS.remove(playerUUID);
                    scheduledTask.cancel();
                }
                EagleFactionsPlugin.HOME_COOLDOWN_PLAYERS.replace(playerUUID, seconds, seconds - 1);
            }
        }
    }

    private static class HomeTeleportAnimationTasks
    {
        private ScheduledTask sourceAnimationTask;
        private ScheduledTask destinationAnimationTask;

        public void setDestinationAnimationTask(ScheduledTask destinationAnimationTask)
        {
            this.destinationAnimationTask = destinationAnimationTask;
        }

        public void setSourceAnimationTask(ScheduledTask sourceAnimationTask)
        {
            this.sourceAnimationTask = sourceAnimationTask;
        }

        public ScheduledTask getDestinationAnimationTask()
        {
            return destinationAnimationTask;
        }

        public ScheduledTask getSourceAnimationTask()
        {
            return sourceAnimationTask;
        }
    }

    public static class HomeParticlesTask implements EagleFactionsConsumerTask<ScheduledTask>
    {
        private final ServerPlayer player;
        private final ServerWorld world;
        private final ServerLocation particleLocation;

        private final double r = 0.6;
        private final double numberOfParticles = 28;

        private final double angleIncrement = (2 / numberOfParticles) * Math.PI;
        private double angle = 0;

        private final Vector3i lastPlayerBlockPosition;

        public HomeParticlesTask(final ServerPlayer player, final ServerLocation particleLocation)
        {
            this.player = player;
            this.world = player.world();
            this.particleLocation = particleLocation;
            this.lastPlayerBlockPosition = player.serverLocation().blockPosition();
        }

        @Override
        public void accept(ScheduledTask task)
        {
            double x = this.particleLocation.x() + r * Math.cos(angle);
            double z = this.particleLocation.z() + r * Math.sin(angle);


            world.spawnParticles(ParticleEffect.builder()
                            .type(ParticleTypes.END_ROD)
                            .quantity(5)
                            .offset(Vector3d.from(0, 0.5, 0))
                            .build(),
                    Vector3d.from(x, particleLocation.y() + 0.5, z)
            );

            if (angle + angleIncrement > 360)
            {
                angle = (angle + angleIncrement) - 360;
            }
            else
            {
                angle += angleIncrement;
            }

            if (!this.lastPlayerBlockPosition.equals(this.player.serverLocation().blockPosition()) || !this.player.isOnline())
                task.cancel();
        }
    }
}
