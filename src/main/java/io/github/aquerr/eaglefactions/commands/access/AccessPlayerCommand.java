package io.github.aquerr.eaglefactions.commands.access;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPermission;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import io.github.aquerr.eaglefactions.util.ParticlesUtil;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Optional;

public class AccessPlayerCommand extends AbstractCommand
{
    private final PermsManager permsManager;

    public AccessPlayerCommand(EagleFactionsPlugin plugin)
    {
        super(plugin);
        this.permsManager = plugin.getPermsManager();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final FactionPlayer factionPlayer = context.requireOne(EagleFactionsCommandParameters.factionPlayer());

        final ServerPlayer player = requirePlayerSource(context);
        final Faction playerFaction = requirePlayerFaction(player);

        // Access can be run only by leader and officers
        final Faction chunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(player.world().uniqueId(), player.serverLocation().chunkPosition())
                .orElseThrow(() -> this.getPlugin().getMessageService().resolveExceptionWithMessage("error.claim.place-does-not-belong-to-anyone"));

        if (!playerFaction.equals(chunkFaction))
            throw this.getPlugin().getMessageService().resolveExceptionWithMessage("error.claim.place-does-not-belong-to-your-faction");

        if (!permsManager.hasPermission(player.uniqueId(), playerFaction, FactionPermission.MANAGE_INTERNAL_CLAIMS))
            throw this.getPlugin().getMessageService().resolveExceptionWithMessage(EFMessageService.ERROR_YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS);

        // Get claim at player's location
        final Optional<Claim> optionalClaim = chunkFaction.getClaimAt(player.world().uniqueId(), player.serverLocation().chunkPosition());
        final Claim claim = optionalClaim.get();

        if (claim.getOwners().contains(factionPlayer.getUniqueId()))
        {
            super.getPlugin().getFactionLogic().removeClaimOwner(chunkFaction, claim, factionPlayer.getUniqueId());
            ParticlesUtil.spawnRemoveAccessParticles(claim);
            player.sendMessage(super.getPlugin().getMessageService().resolveMessageWithPrefix("command.access.player.remove", factionPlayer.getName(), claim.getChunkPosition().toString()));
        }
        else
        {
            super.getPlugin().getFactionLogic().addClaimOwner(chunkFaction, claim, factionPlayer.getUniqueId());
            ParticlesUtil.spawnAddAccessParticles(claim);
            player.sendMessage(super.getPlugin().getMessageService().resolveMessageWithPrefix("command.access.player.add", factionPlayer.getName(), claim.getChunkPosition().toString()));
        }

        return CommandResult.success();
    }
}
