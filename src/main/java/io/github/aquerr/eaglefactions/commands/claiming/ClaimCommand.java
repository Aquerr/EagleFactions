package io.github.aquerr.eaglefactions.commands.claiming;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.managers.claim.ClaimContextImpl;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;

import static io.github.aquerr.eaglefactions.util.WorldUtil.getPlainWorldName;

public class ClaimCommand extends AbstractCommand
{
    private static final String ERROR_NOT_CLAIMABLE_WORLD = "error.command.claim.not-claimable-world";

    private final FactionLogic factionLogic;
    private final ProtectionConfig protectionConfig;
    private final FactionsConfig factionsConfig;
    private final MessageService messageService;

    public ClaimCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionLogic = plugin.getFactionLogic();
        this.protectionConfig = plugin.getConfiguration().getProtectionConfig();
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final Optional<Faction> optionalFaction = context.one(EagleFactionsCommandParameters.optionalFaction());
        final ServerPlayer player = requirePlayerSource(context);
        final ServerWorld world = player.world();
        final Vector3i chunk = player.serverLocation().chunkPosition();
        final Optional<Faction> optionalPlayerFaction = this.factionLogic.getFactionByPlayerUUID(player.uniqueId());
        final Optional<Faction> optionalChunkFaction = this.factionLogic.getFactionByChunk(world.uniqueId(), chunk);
        final boolean hasAdminMode = super.getPlugin().getPlayerManager().hasAdminMode(player.user());

        if (optionalChunkFaction.isPresent())
            throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_THIS_PLACE_IS_ALREADY_CLAIMED);

        if(optionalFaction.isPresent())
        {
            final Faction faction = optionalFaction.get();
            if(hasAdminMode)
            {
                return preformAdminClaim(player, faction, chunk);
            }
            return preformClaimByFaction(player, faction, chunk);
        }
        else
        {
            if (!optionalPlayerFaction.isPresent())
                throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND_MESSAGE_KEY);
            final Faction faction = optionalPlayerFaction.get();
            if(hasAdminMode)
                return preformAdminClaim(player, faction, chunk);
            else return preformNormalClaim(player, faction, chunk);
        }
    }

    private CommandResult preformClaimByFaction(final ServerPlayer player, final Faction faction, final Vector3i chunk) throws CommandException
    {
        final ServerWorld world = player.world();
        final Optional<Faction> optionalPlayerFaction = this.factionLogic.getFactionByPlayerUUID(player.uniqueId());
        final boolean isClaimableWorld = this.protectionConfig.getClaimableWorldNames().contains(getPlainWorldName(world));

        if(!optionalPlayerFaction.isPresent() || !optionalPlayerFaction.get().getName().equals(faction.getName()))
            throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS);

        if(!isClaimableWorld)
            throw messageService.resolveExceptionWithMessage(ERROR_NOT_CLAIMABLE_WORLD);

        return preformNormalClaim(player, faction, chunk);
    }

    private CommandResult preformAdminClaim(final ServerPlayer player, final Faction faction, final Vector3i chunk) throws CommandException
    {
        final ServerWorld world = player.world();
        final boolean safeZoneWorld = this.protectionConfig.getSafeZoneWorldNames().contains(getPlainWorldName(world));
        final boolean warZoneWorld = this.protectionConfig.getWarZoneWorldNames().contains(getPlainWorldName(world));

        //Even admin cannot claim territories in safezone nor warzone world.
        if (safeZoneWorld || warZoneWorld)
            throw messageService.resolveExceptionWithMessage(ERROR_NOT_CLAIMABLE_WORLD);

        boolean isCancelled = EventRunner.runFactionClaimEventPre(player, faction, player.world(), chunk);
        if (isCancelled)
            return CommandResult.success();

        this.factionLogic.addClaim(faction, new Claim(world.uniqueId(), chunk));
        player.sendMessage(messageService.resolveMessageWithPrefix("command.claim.land-has-been-successfully-claimed", chunk.toString()));
        EventRunner.runFactionClaimEventPost(player, faction, player.world(), chunk);
        return CommandResult.success();
    }

    private CommandResult preformNormalClaim(final ServerPlayer player, final Faction faction, final Vector3i chunk) throws CommandException
    {
        final ServerWorld world = player.world();
        final boolean isClaimableWorld = this.protectionConfig.getClaimableWorldNames().contains(getPlainWorldName(world));

        if(!isClaimableWorld)
            throw messageService.resolveExceptionWithMessage(ERROR_NOT_CLAIMABLE_WORLD);

        //If not admin then check faction perms for player
        if (!this.getPlugin().getPermsManager().canClaim(player.uniqueId(), faction))
            throw messageService.resolveExceptionWithMessage("error.command.claim.players-with-your-rank-cant-claim-lands");

        //Check if faction has enough power to claim territory
        if (this.factionLogic.getFactionMaxClaims(faction) <= faction.getClaims().size())
            throw messageService.resolveExceptionWithMessage("error.command.claim.faction.not-enough-power");

        //If attacked then It should not be able to claim territories
        if (EagleFactionsPlugin.ATTACKED_FACTIONS.containsKey(faction.getName()))
            throw messageService.resolveExceptionWithMessage("error.command.claim.faction.under-attack", EagleFactionsPlugin.ATTACKED_FACTIONS.get(faction.getName()));

        if (this.factionsConfig.requireConnectedClaims() && !this.factionLogic.isClaimConnected(faction, new Claim(world.uniqueId(), chunk)))
            throw messageService.resolveExceptionWithMessage("error.command.claim.claim.claims-need-to-be-connected");

        boolean isCancelled = EventRunner.runFactionClaimEventPre(player, faction, world, chunk);
        if (isCancelled)
            return CommandResult.success();

        this.factionLogic.startClaiming(new ClaimContextImpl(ServerLocation.of(world, chunk),
                player,
                faction,
                this.messageService));
        this.factionLogic.startClaiming(new ClaimContextImpl(ServerLocation.of(world, chunk), player, faction, messageService));
        return CommandResult.success();
    }
}
