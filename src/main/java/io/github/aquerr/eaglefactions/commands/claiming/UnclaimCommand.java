package io.github.aquerr.eaglefactions.commands.claiming;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.messaging.Messages;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class UnclaimCommand extends AbstractCommand
{
    private final FactionsConfig factionsConfig;

    public UnclaimCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final ServerPlayer player = requirePlayerSource(context);

        if(super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
        {
            final ServerWorld world = player.world();
            final Vector3i chunk = player.serverLocation().chunkPosition();
            final Optional<Faction> optionalChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(world.uniqueId(), chunk);

            if (optionalChunkFaction.isPresent())
            {
                final boolean isCancelled = EventRunner.runFactionUnclaimEventPre(player, optionalChunkFaction.get(), world, chunk);
                if (isCancelled)
                    return CommandResult.success();

                if (!this.factionsConfig.canPlaceHomeOutsideFactionClaim() && optionalChunkFaction.get().getHome() != null)
                {
                    if (world.uniqueId().equals(optionalChunkFaction.get().getHome().getWorldUUID()))
                    {
                            final ServerLocation homeLocation = world.location(optionalChunkFaction.get().getHome().getBlockPosition());
                            if(homeLocation.chunkPosition().toString().equals(player.serverLocation().chunkPosition().toString()))
                                super.getPlugin().getFactionLogic().setHome(optionalChunkFaction.get(), null);
                    }
                }

                super.getPlugin().getFactionLogic().removeClaim(optionalChunkFaction.get(), new Claim(world.uniqueId(), chunk));

                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text(Messages.LAND_HAS_BEEN_SUCCESSFULLY_UNCLAIMED, GREEN)));
                EventRunner.runFactionUnclaimEventPost(player, optionalChunkFaction.get(), world, chunk);
                return CommandResult.success();
            }
            else
            {
                player.sendMessage(PluginInfo.ERROR_PREFIX.append(text(Messages.THIS_PLACE_DOES_NOT_BELONG_TO_ANYONE, RED)));
                return CommandResult.success();
            }
        }

        final Faction playerFaction = requirePlayerFaction(player);
        if (!this.getPlugin().getPermsManager().canClaim(player.uniqueId(), playerFaction))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.PLAYERS_WITH_YOUR_RANK_CANT_UNCLAIM_LANDS, RED)));

        final ServerWorld world = player.world();
        final Vector3i chunk = player.serverLocation().chunkPosition();
        final Optional<Faction> optionalChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(world.uniqueId(), chunk);
        if (!optionalChunkFaction.isPresent())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.THIS_PLACE_DOES_NOT_BELONG_TO_ANYONE, RED)));

        final Faction chunkFaction = optionalChunkFaction.get();
        if (!chunkFaction.getName().equals(playerFaction.getName()))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.THIS_LAND_BELONGS_TO_SOMEONE_ELSE, RED)));

        final boolean isCancelled = EventRunner.runFactionUnclaimEventPre(player, chunkFaction, world, chunk);
        if (isCancelled)
            return CommandResult.success();

        if (!this.factionsConfig.canPlaceHomeOutsideFactionClaim() && optionalChunkFaction.get().getHome() != null)
        {
            if (world.uniqueId().equals(optionalChunkFaction.get().getHome().getWorldUUID()))
            {
                final ServerLocation homeLocation = world.location(optionalChunkFaction.get().getHome().getBlockPosition());
                if(homeLocation.chunkPosition().equals(chunk))
                    super.getPlugin().getFactionLogic().setHome(optionalChunkFaction.get(), null);
            }
        }

        //We need to get faction again to see changes made after removing home.
        final Faction faction = super.getPlugin().getFactionLogic().getFactionByChunk(world.uniqueId(), chunk).get();
        super.getPlugin().getFactionLogic().removeClaim(faction, new Claim(world.uniqueId(), chunk));
        player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text(Messages.LAND_HAS_BEEN_SUCCESSFULLY_UNCLAIMED, GREEN)));
        EventRunner.runFactionUnclaimEventPost(player, optionalChunkFaction.get(), world, chunk);
        return CommandResult.success();
    }
}
