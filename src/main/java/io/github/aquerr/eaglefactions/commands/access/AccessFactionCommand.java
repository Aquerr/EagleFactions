package io.github.aquerr.eaglefactions.commands.access;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPermission;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import io.github.aquerr.eaglefactions.util.ParticlesUtil;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Optional;

public class AccessFactionCommand extends AbstractCommand
{
    private final PermsManager permsManager;

    public AccessFactionCommand(EagleFactionsPlugin plugin)
    {
        super(plugin);
        this.permsManager = plugin.getPermsManager();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final ServerPlayer player = requirePlayerSource(context);
        final Faction playerFaction = requirePlayerFaction(player);

        // Access can be run only by leader and officers
        final Faction chunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(player.world().uniqueId(), player.serverLocation().chunkPosition())
                .orElseThrow(() -> this.getPlugin().getMessageService().resolveExceptionWithMessage("error.claim.place-does-not-belong-to-anyone"));

        if (!playerFaction.equals(chunkFaction))
            throw this.getPlugin().getMessageService().resolveExceptionWithMessage("error.claim.place-does-not-belong-to-your-faction");

        if (!permsManager.hasPermission(player.uniqueId(), chunkFaction, FactionPermission.MANAGE_INTERNAL_CLAIMS))
            throw this.getPlugin().getMessageService().resolveExceptionWithMessage(EFMessageService.ERROR_YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS);

        // Get claim at player's location
        final Optional<Claim> optionalClaim = chunkFaction.getClaimAt(player.world().uniqueId(), player.serverLocation().chunkPosition());
        final Claim claim = optionalClaim.get();
        final boolean currentAccess = claim.isAccessibleByFaction();

        super.getPlugin().getFactionLogic().setClaimAccessibleByFaction(chunkFaction, claim, !currentAccess);

        if (!currentAccess)
        {
            ParticlesUtil.spawnAddAccessParticles(claim);
            player.sendMessage(super.getPlugin().getMessageService().resolveMessageWithPrefix("command.access.faction.add", claim.getChunkPosition().toString()));
        }
        else
        {
            ParticlesUtil.spawnRemoveAccessParticles(claim);
            player.sendMessage(super.getPlugin().getMessageService().resolveMessageWithPrefix("command.access.faction.remove", claim.getChunkPosition().toString()));
        }
        return CommandResult.success();
    }
}
