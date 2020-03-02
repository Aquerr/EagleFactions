package io.github.aquerr.eaglefactions.common.commands;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.events.EventRunner;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class UnclaimCommand extends AbstractCommand
{
    private final FactionsConfig factionsConfig;

    public UnclaimCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        if (!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        final Player player = (Player)source;
        final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());

        if(super.getPlugin().getPlayerManager().hasAdminMode(player))
        {
            final World world = player.getWorld();
            final Vector3i chunk = player.getLocation().getChunkPosition();
            final Optional<Faction> optionalChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), chunk);

            if (optionalChunkFaction.isPresent())
            {
                final boolean isCancelled = EventRunner.runFactionUnclaimEvent(player, optionalChunkFaction.get(), world, chunk);
                if (isCancelled)
                    return CommandResult.success();

                if (!this.factionsConfig.canPlaceHomeOutsideFactionClaim() && optionalChunkFaction.get().getHome() != null)
                {
                    if (world.getUniqueId().equals(optionalChunkFaction.get().getHome().getWorldUUID()))
                    {
                            final Location<World> homeLocation = world.getLocation(optionalChunkFaction.get().getHome().getBlockPosition());
                            if(homeLocation.getChunkPosition().toString().equals(player.getLocation().getChunkPosition().toString()))
                                super.getPlugin().getFactionLogic().setHome(optionalChunkFaction.get(), world.getUniqueId(), null);
                    }
                }

                super.getPlugin().getFactionLogic().removeClaim(optionalChunkFaction.get(), new Claim(world.getUniqueId(), chunk));

                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, Messages.LAND_HAS_BEEN_SUCCESSFULLY_UNCLAIMED));
                return CommandResult.success();
            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.THIS_PLACE_DOES_NOT_BELOG_TO_ANYONE));
                return CommandResult.success();
            }
        }

        //Check if player is in the faction.
        if (!optionalPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        final Faction playerFaction = optionalPlayerFaction.get();
        if (!this.getPlugin().getFlagManager().canClaim(player.getUniqueId(), playerFaction))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.PLAYERS_WITH_YOUR_RANK_CANT_UNCLAIM_LANDS));

        final World world = player.getWorld();
        final Vector3i chunk = player.getLocation().getChunkPosition();
        final Optional<Faction> optionalChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), chunk);
        if (!optionalChunkFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.THIS_PLACE_IS_ALREADY_CLAIMED));

        final Faction chunkFaction = optionalChunkFaction.get();
        if (!chunkFaction.getName().equals(playerFaction.getName()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, Messages.THIS_LAND_BELONGS_TO_SOMEONE_ELSE));

        final boolean isCancelled = EventRunner.runFactionUnclaimEvent(player, chunkFaction, world, chunk);
        if (isCancelled)
            return CommandResult.success();

        if (!this.factionsConfig.canPlaceHomeOutsideFactionClaim() && optionalChunkFaction.get().getHome() != null)
        {
            if (world.getUniqueId().equals(optionalChunkFaction.get().getHome().getWorldUUID()))
            {
                final Location<World> homeLocation = world.getLocation(optionalChunkFaction.get().getHome().getBlockPosition());
                if(homeLocation.getChunkPosition().equals(chunk))
                    super.getPlugin().getFactionLogic().setHome(optionalChunkFaction.get(), null, null);
            }
        }

        super.getPlugin().getFactionLogic().removeClaim(optionalChunkFaction.get(), new Claim(world.getUniqueId(), chunk));
        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, Messages.LAND_HAS_BEEN_SUCCESSFULLY_UNCLAIMED));
        return CommandResult.success();
    }
}
