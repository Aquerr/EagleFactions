package io.github.aquerr.eaglefactions.commands.access;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.util.ParticlesUtil;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Optional;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class AccessPlayerCommand extends AbstractCommand
{
    public AccessPlayerCommand(EagleFactionsPlugin plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final FactionPlayer factionPlayer = context.requireOne(EagleFactionsCommandParameters.factionPlayer());

        final ServerPlayer player = requirePlayerSource(context);
        final Faction playerFaction = requirePlayerFaction(player);

        // Access can be run only by leader and officers
        final Faction chunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(player.world().uniqueId(), player.serverLocation().chunkPosition())
                .orElseThrow(() -> new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.THIS_PLACE_DOES_NOT_BELONG_TO_ANYONE))));

        if (!playerFaction.equals(chunkFaction))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.THIS_PLACE_DOES_NOT_BELONG_TO_YOUR_FACTION)));

        if (!playerFaction.getLeader().equals(player.uniqueId()) && !playerFaction.getOfficers().contains(player.uniqueId()) && !super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS, RED)));

        // Get claim at player's location
        final Optional<Claim> optionalClaim = chunkFaction.getClaimAt(player.world().uniqueId(), player.serverLocation().chunkPosition());
        final Claim claim = optionalClaim.get();

        if (claim.getOwners().contains(factionPlayer.getUniqueId()))
        {
            super.getPlugin().getFactionLogic().removeClaimOwner(chunkFaction, claim, factionPlayer.getUniqueId());
            ParticlesUtil.spawnRemoveAccessParticles(claim);
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text(factionPlayer.getName(), GOLD)).append(text(" has been removed from the claim ", GREEN)).append(text(claim.getChunkPosition().toString(), GOLD)));
        }
        else
        {
            super.getPlugin().getFactionLogic().addClaimOwner(chunkFaction, claim, factionPlayer.getUniqueId());
            ParticlesUtil.spawnAddAccessParticles(claim);
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text(factionPlayer.getName(), GOLD)).append(text(" has been added to the claim ", GREEN)).append(text(claim.getChunkPosition().toString(), GOLD)));

        }

        return CommandResult.success();
    }
}
