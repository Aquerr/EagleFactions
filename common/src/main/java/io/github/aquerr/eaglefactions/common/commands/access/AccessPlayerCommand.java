package io.github.aquerr.eaglefactions.common.commands.access;

import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import io.github.aquerr.eaglefactions.common.util.ParticlesUtil;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class AccessPlayerCommand extends AbstractCommand
{
    public AccessPlayerCommand(EagleFactionsPlugin plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        final FactionPlayer factionPlayer = context.requireOne(Text.of("player"));

        if(!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        final Player player = (Player)source;
        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        if (!optionalPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        final Faction playerFaction = optionalPlayerFaction.get();

        // Access can be run only by leader and officers
        final Optional<Faction> optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(player.getWorld().getUniqueId(), player.getLocation().getChunkPosition());
        if (!optionalChunkFaction.isPresent())
            throw new CommandException(PluginInfo.ERROR_PREFIX.concat(Text.of(Messages.THIS_PLACE_DOES_NOT_BELONG_TO_ANYONE)));

        final Faction chunkFaction = optionalChunkFaction.get();
        if (!playerFaction.getName().equals(chunkFaction.getName()))
            throw new CommandException(PluginInfo.ERROR_PREFIX.concat(Text.of(Messages.THIS_PLACE_DOES_NOT_BELONG_TO_YOUR_FACTION)));

        if (!playerFaction.getLeader().equals(player.getUniqueId()) && !playerFaction.getOfficers().contains(player.getUniqueId()) && !super.getPlugin().getPlayerManager().hasAdminMode(player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS));

        // Get claim at player's location
        final Optional<Claim> optionalClaim = chunkFaction.getClaimAt(player.getWorld().getUniqueId(), player.getLocation().getChunkPosition());
        final Claim claim = optionalClaim.get();

        if (claim.getOwners().contains(factionPlayer.getUniqueId()))
        {
            super.getPlugin().getFactionLogic().removeClaimOwner(chunkFaction, claim, factionPlayer.getUniqueId());
            ParticlesUtil.spawnRemoveAccessParticles(claim);
            source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GOLD, factionPlayer.getName(), TextColors.GREEN, " has been removed from the claim ", TextColors.GOLD, claim.getChunkPosition()));
        }
        else
        {
            super.getPlugin().getFactionLogic().addClaimOwner(chunkFaction, claim, factionPlayer.getUniqueId());
            ParticlesUtil.spawnAddAccessParticles(claim);
            source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GOLD, factionPlayer.getName(), TextColors.GREEN, " has been added to the claim ", TextColors.GOLD, claim.getChunkPosition()));
        }

        return CommandResult.success();
    }
}
