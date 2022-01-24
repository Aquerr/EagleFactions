package io.github.aquerr.eaglefactions.commands.access;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.util.ParticlesUtil;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.w3c.dom.Text;

import java.util.Optional;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class AccessFactionCommand extends AbstractCommand
{
    public AccessFactionCommand(EagleFactionsPlugin plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
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
        final boolean currentAccess = claim.isAccessibleByFaction();

        super.getPlugin().getFactionLogic().setClaimAccessibleByFaction(chunkFaction, claim, !currentAccess);

        if (!currentAccess)
        {
            ParticlesUtil.spawnAddAccessParticles(claim);
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Faction access added for claim: ", GREEN)).append(text(claim.getChunkPosition().toString(), GOLD)));
        }
        else
        {
            ParticlesUtil.spawnRemoveAccessParticles(claim);
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Faction access removed for claim: ", GREEN)).append(text(claim.getChunkPosition().toString(), GOLD)));
        }
        return CommandResult.success();
    }
}
