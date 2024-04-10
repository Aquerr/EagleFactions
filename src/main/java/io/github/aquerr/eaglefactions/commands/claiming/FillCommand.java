package io.github.aquerr.eaglefactions.commands.claiming;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.exception.CouldNotClaimException;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.managers.claim.ClaimManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.util.WorldUtil;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

import java.util.LinkedList;
import java.util.Queue;

public class FillCommand extends AbstractCommand
{
    private final FactionLogic factionLogic;
    private final PermsManager permsManager;
    private final PlayerManager playerManager;
    private final ProtectionConfig protectionConfig;
    private final MessageService messageService;
    private final ClaimManager claimManager;

    public FillCommand(EagleFactions plugin)
    {
        super(plugin);
        this.factionLogic = plugin.getFactionLogic();
        this.permsManager = plugin.getPermsManager();
        this.playerManager = plugin.getPlayerManager();
        this.protectionConfig = plugin.getConfiguration().getProtectionConfig();
        this.messageService = plugin.getMessageService();
        this.claimManager = plugin.getClaimManager();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {

        ServerPlayer player = requirePlayerSource(context);
        Faction faction = requirePlayerFaction(player);

        final boolean isAdmin = this.playerManager.hasAdminMode(player.user());
        if (!isAdmin && !this.permsManager.canClaim(player.uniqueId(), faction))
            throw messageService.resolveExceptionWithMessage("error.command.claim.players-with-your-rank-cant-claim-lands");

        final ServerWorld world = player.world();

        if (!canClaimInWorld(world, isAdmin))
            throw messageService.resolveExceptionWithMessage("error.command.claim.not-claimable-world");

        if (isFactionUnderAttack(faction))
            throw messageService.resolveExceptionWithMessage("error.command.claim.faction.under-attack", EagleFactionsPlugin.ATTACKED_FACTIONS.get(faction.getName()));

        if (hasReachedClaimLimit(faction))
            throw messageService.resolveExceptionWithMessage("error.command.claim.faction.not-enough-power");

        fill(player, faction);
        return CommandResult.success();
    }

    private boolean canClaimInWorld(ServerWorld world, boolean isAdmin)
    {
        if (this.protectionConfig.getClaimableWorldNames().contains(WorldUtil.getPlainWorldName(world)))
            return true;
        else return this.protectionConfig.getNotClaimableWorldNames().contains(WorldUtil.getPlainWorldName(world)) && isAdmin;
    }

    private boolean hasReachedClaimLimit(Faction faction)
    {
        return this.factionLogic.getFactionMaxClaims(faction) <= faction.getClaims().size();
    }

    private boolean isFactionUnderAttack(Faction faction)
    {
        return EagleFactionsPlugin.ATTACKED_FACTIONS.containsKey(faction.getName());
    }

    // Starts where player is standing
    private void fill(final ServerPlayer player, Faction faction) throws CommandException
    {
        int currentClaims = faction.getClaims().size();
        int maxClaims = this.factionLogic.getFactionMaxClaims(faction);

        final ServerWorld world = player.world();
        final Queue<Vector3i> chunks = new LinkedList<>();
        chunks.add(player.location().chunkPosition());
        while (!chunks.isEmpty())
        {
            final Vector3i chunkPosition = chunks.poll();
            if (!this.factionLogic.isClaimed(world.uniqueId(), chunkPosition))
            {
                faction = this.factionLogic.getFactionByName(faction.getName());

                if (currentClaims >= maxClaims)
                {
                    throw messageService.resolveExceptionWithMessage("error.command.claim.faction.not-enough-power");
                }

                tryClaim(player, faction, ServerLocation.of(world, WorldUtil.getChunkTopCenter(world, chunkPosition)));
                currentClaims++;

                chunks.add(chunkPosition.add(1, 0, 0));
                chunks.add(chunkPosition.add(-1, 0, 0));
                chunks.add(chunkPosition.add(0, 0, 1));
                chunks.add(chunkPosition.add(0, 0, -1));
            }
        }
    }

    private void tryClaim(ServerPlayer player, Faction faction, ServerLocation serverLocation) throws CommandException
    {
        try
        {
            this.claimManager.claim(player, faction, serverLocation, false);
            player.sendMessage(messageService.resolveComponentWithMessage("command.claim.land-has-been-successfully-claimed", ""));
        }
        catch (CouldNotClaimException e)
        {
            throw messageService.resolveExceptionWithMessage("error.claim.could-not-claim-territory-with-reason", e.getLocalizedMessage());
        }
    }
}
