package io.github.aquerr.eaglefactions.commands.claiming;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.messaging.Placeholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SquareClaimCommand extends AbstractCommand
{
    private final FactionsConfig factionsConfig;
    private final ProtectionConfig protectionConfig;

    public SquareClaimCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.protectionConfig = plugin.getConfiguration().getProtectionConfig();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final int radius = context.requireOne(Parameter.integerNumber().key("radius").build());

        ServerPlayer player = requirePlayerSource(context);
        Faction faction = requirePlayerFaction(player);

        final ServerWorld world = player.world();
        final boolean isAdmin = super.getPlugin().getPlayerManager().hasAdminMode(player.user());

        //Check if it is a claimable world
        if (!this.protectionConfig.getClaimableWorldNames().contains(((TextComponent)world.properties().displayName().get()).content()))
        {
            if(this.protectionConfig.getNotClaimableWorldNames().contains(((TextComponent)world.properties().displayName().get()).content()) && isAdmin)
            {
                return preformSquareClaim(player, faction, radius);
            }
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_CANNOT_CLAIM_TERRITORIES_IN_THIS_WORLD, NamedTextColor.RED)));
        }

        return preformSquareClaim(player, faction, radius);
    }

    private CommandResult preformSquareClaim(final ServerPlayer player, final Faction playerFaction, final int radius)
    {
        final Vector3i playerChunk = player.location().chunkPosition();
        final ServerWorld world = player.world();

        CompletableFuture.runAsync(() -> {
            //Radius claim
            final int startX = playerChunk.x() - radius;
            final int startZ = playerChunk.z() - radius;
            final int endX = playerChunk.x() + radius;
            final int endZ = playerChunk.z() + radius;

            final List<Vector3i> chunksToClaim = new ArrayList<>();
            final List<Claim> newFactionClaims = new ArrayList<>();

            for(int x = startX; x <= endX; x++)
            {
                for(int z = startZ; z <= endZ; z++)
                {
                    final Vector3i chunk = new Vector3i(x, 0, z);
                    chunksToClaim.add(chunk);
                }
            }

            for(final Vector3i chunk : chunksToClaim)
            {
                final Optional<Faction> optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(world.uniqueId(), chunk);
                if (optionalChunkFaction.isPresent())
                    continue;

                //Check if admin mode
                if (super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
                {
                    boolean isCancelled = EventRunner.runFactionClaimEventPre(player, playerFaction, world, chunk);
                    if (isCancelled)
                        continue;

                    newFactionClaims.add(new Claim(world.uniqueId(), chunk));
                    player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.LAND + " "))
                            .append(Component.text(chunk.toString(), NamedTextColor.GOLD))
                            .append(Component.text(" " + Messages.HAS_BEEN_SUCCESSFULLY + " ", NamedTextColor.WHITE))
                            .append(Component.text(Messages.CLAIMED, NamedTextColor.GOLD))
                            .append(Component.text("!")));

                    EventRunner.runFactionClaimEventPost(player, playerFaction, world, chunk);
                    continue;
                }

                //If not admin then check faction perms for player
                if (!this.getPlugin().getPermsManager().canClaim(player.uniqueId(), playerFaction))
                {
                    player.sendMessage(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.PLAYERS_WITH_YOUR_RANK_CANT_CLAIM_LANDS, NamedTextColor.RED)));
                    return;
                }

                //Check if faction has enough power to claim territory
                if (super.getPlugin().getPowerManager().getFactionMaxClaims(playerFaction) <= playerFaction.getClaims().size() + newFactionClaims.size())
                {
                    player.sendMessage(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOUR_FACTION_DOES_NOT_HAVE_POWER_TO_CLAIM_MORE_LANDS, NamedTextColor.RED)));
                    break;
                }

                //If attacked then It should not be able to claim territories
                if (EagleFactionsPlugin.ATTACKED_FACTIONS.containsKey(playerFaction.getName()))
                {
                    player.sendMessage(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOUR_FACTION_IS_UNDER_ATTACK + " ", NamedTextColor.RED))
                            .append(MessageLoader.parseMessage(Messages.YOU_NEED_TO_WAIT_NUMBER_SECONDS_TO_BE_ABLE_TO_CLAIM_AGAIN, NamedTextColor.RED, Collections.singletonMap(Placeholders.NUMBER, Component.text(EagleFactionsPlugin.ATTACKED_FACTIONS.get(playerFaction.getName()), NamedTextColor.GOLD)))));
                    break;
                }

                if (playerFaction.isSafeZone() || playerFaction.isWarZone())
                {
                    boolean isCancelled = EventRunner.runFactionClaimEventPre(player, playerFaction, world, chunk);
                    if (isCancelled)
                        continue;

                    newFactionClaims.add(new Claim(world.uniqueId(), chunk));
                    player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.LAND + " "))
                            .append(Component.text(chunk.toString(), NamedTextColor.GOLD))
                            .append(Component.text(" " + Messages.HAS_BEEN_SUCCESSFULLY + " "))
                            .append(Component.text(Messages.CLAIMED, NamedTextColor.GOLD))
                            .append(Component.text("!")));
                    EventRunner.runFactionClaimEventPost(player, playerFaction, world, chunk);
                    continue;
                }

                if (this.factionsConfig.requireConnectedClaims() && !super.getPlugin().getFactionLogic().isClaimConnected(playerFaction, new Claim(world.uniqueId(), chunk)))
                    continue;

                boolean isCancelled = EventRunner.runFactionClaimEventPre(player, playerFaction, world, chunk);
                if (isCancelled)
                    continue;

                if(this.factionsConfig.shouldDelayClaim())
                {
                    player.sendMessage(Component.text(Messages.CANT_RECTANGLE_CLAIM_IF_DELAYED_CLAIMING_IS_ON));
                    break;
                }

                newFactionClaims.add(new Claim(world.uniqueId(), chunk));
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.LAND + " "))
                        .append(Component.text(chunk.toString(), NamedTextColor.GOLD))
                        .append(Component.text(" " + Messages.HAS_BEEN_SUCCESSFULLY + " "))
                        .append(Component.text(Messages.CLAIMED, NamedTextColor.GOLD))
                        .append(Component.text("!")));
                EventRunner.runFactionClaimEventPost(player, playerFaction, world, chunk);
            }

            super.getPlugin().getFactionLogic().addClaims(playerFaction, newFactionClaims);
        });
        return CommandResult.success();
    }
}
