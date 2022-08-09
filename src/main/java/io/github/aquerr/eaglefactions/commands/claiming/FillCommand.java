package io.github.aquerr.eaglefactions.commands.claiming;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class FillCommand extends AbstractCommand
{
    private final ProtectionConfig protectionConfig;
    private final MessageService messageService;

    public FillCommand(EagleFactions plugin)
    {
        super(plugin);
        this.protectionConfig = plugin.getConfiguration().getProtectionConfig();
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {

        ServerPlayer player = requirePlayerSource(context);
        Faction faction = requirePlayerFaction(player);

        final boolean isAdmin = super.getPlugin().getPlayerManager().hasAdminMode(player.user());
        if (!isAdmin && !super.getPlugin().getPermsManager().canClaim(player.uniqueId(), faction))
            throw messageService.resolveExceptionWithMessage("error.command.claim.players-with-your-rank-cant-claim-lands");

        final ServerWorld world = player.world();

        if (!canClaimInWorld(world, isAdmin))
            throw messageService.resolveExceptionWithMessage("error.command.claim.not-claimable-world");

        if (isFactionUnderAttack(faction))
            throw messageService.resolveExceptionWithMessage("error.command.claim.faction.under-attack", EagleFactionsPlugin.ATTACKED_FACTIONS.get(faction.getName()));

        fill(player, faction);
        return CommandResult.success();
    }

    private boolean canClaimInWorld(ServerWorld world, boolean isAdmin)
    {
        if (this.protectionConfig.getClaimableWorldNames().contains(world.properties().name()))
            return true;
        else return this.protectionConfig.getNotClaimableWorldNames().contains(world.properties().name()) && isAdmin;
    }

    private boolean hasReachedClaimLimit(Faction faction)
    {
        return super.getPlugin().getPowerManager().getFactionMaxClaims(faction) <= faction.getClaims().size();
    }

    private boolean isFactionUnderAttack(Faction faction)
    {
        return EagleFactionsPlugin.ATTACKED_FACTIONS.containsKey(faction.getName());
    }

    // Starts where player is standing
    private void fill(final ServerPlayer player, Faction faction) throws CommandException
    {
        final UUID worldUUID = player.world().uniqueId();
        final Queue<Vector3i> chunks = new LinkedList<>();
        chunks.add(player.location().chunkPosition());
        while (!chunks.isEmpty())
        {
            if (hasReachedClaimLimit(faction))
                throw messageService.resolveExceptionWithMessage("error.command.claim.faction.not-enough-power");

            final Vector3i chunkPosition = chunks.poll();
            if (!super.getPlugin().getFactionLogic().isClaimed(worldUUID, chunkPosition))
            {
                faction = super.getPlugin().getFactionLogic().getFactionByName(faction.getName());
                super.getPlugin().getFactionLogic().startClaiming(player, faction, worldUUID, chunkPosition);
                chunks.add(chunkPosition.add(1, 0, 0));
                chunks.add(chunkPosition.add(-1, 0, 0));
                chunks.add(chunkPosition.add(0, 0, 1));
                chunks.add(chunkPosition.add(0, 0, -1));
            }
        }
    }
}
