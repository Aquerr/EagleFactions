package io.github.aquerr.eaglefactions.commands.claiming;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.HomeConfig;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.events.EventRunner;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;

public class UnclaimCommand extends AbstractCommand
{
    private final HomeConfig homeConfig;
    private final MessageService messageService;

    public UnclaimCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.homeConfig = plugin.getConfiguration().getHomeConfig();
        this.messageService = plugin.getMessageService();
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

                if (!this.homeConfig.canPlaceHomeOutsideFactionClaim() && optionalChunkFaction.get().getHome() != null)
                {
                    if (world.uniqueId().equals(optionalChunkFaction.get().getHome().getWorldUUID()))
                    {
                            final ServerLocation homeLocation = world.location(optionalChunkFaction.get().getHome().getBlockPosition());
                            if(homeLocation.chunkPosition().toString().equals(player.serverLocation().chunkPosition().toString()))
                                super.getPlugin().getFactionLogic().setHome(optionalChunkFaction.get(), null);
                    }
                }

                super.getPlugin().getFactionLogic().removeClaim(optionalChunkFaction.get(), new Claim(world.uniqueId(), chunk));

                player.sendMessage(messageService.resolveMessageWithPrefix("command.unclaim.land-has-been-successfully-unclaimed", chunk.toString()));
                EventRunner.runFactionUnclaimEventPost(player, optionalChunkFaction.get(), world, chunk);
                return CommandResult.success();
            }
            else
            {
                throw messageService.resolveExceptionWithMessage("error.claim.place-does-not-belong-to-anyone");
            }
        }

        final Faction playerFaction = requirePlayerFaction(player);
        if (!this.getPlugin().getPermsManager().canClaim(player.uniqueId(), playerFaction))
            throw messageService.resolveExceptionWithMessage("error.command.unclaim.players-with-your-rank-cant-unclaim-lands");

        final ServerWorld world = player.world();
        final Vector3i chunk = player.serverLocation().chunkPosition();
        final Optional<Faction> optionalChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(world.uniqueId(), chunk);
        if (!optionalChunkFaction.isPresent())
            throw messageService.resolveExceptionWithMessage("error.claim.place-does-not-belong-to-anyone");

        final Faction chunkFaction = optionalChunkFaction.get();
        if (!chunkFaction.getName().equals(playerFaction.getName()))
            throw messageService.resolveExceptionWithMessage("error.claim.place-belongs-to-someone-else");

        final boolean isCancelled = EventRunner.runFactionUnclaimEventPre(player, chunkFaction, world, chunk);
        if (isCancelled)
            return CommandResult.success();

        if (!this.homeConfig.canPlaceHomeOutsideFactionClaim() && optionalChunkFaction.get().getHome() != null)
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
        player.sendMessage(messageService.resolveMessageWithPrefix("command.unclaim.land-has-been-successfully-unclaimed", chunk.toString()));
        EventRunner.runFactionUnclaimEventPost(player, optionalChunkFaction.get(), world, chunk);
        return CommandResult.success();
    }
}
