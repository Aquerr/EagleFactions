package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.PowerConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;

public class AttackCommand extends AbstractCommand
{
    private final FactionsConfig factionsConfig;
    private final PowerConfig powerConfig;
    private final MessageService messageService;

    public AttackCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.powerConfig = plugin.getConfiguration().getPowerConfig();
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final ServerPlayer player = requirePlayerSource(context);

        if(this.factionsConfig.canAttackOnlyAtNight() && isNight(player.world()))
            throw messageService.resolveExceptionWithMessage("error.command.attack.you-can-attack-only-at-night");

        return attackChunk(player);
    }

    private CommandResult attackChunk(ServerPlayer player) throws CommandException
    {
        final Faction playerFaction = requirePlayerFaction(player);
        final Optional<Faction> optionalChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(player.world().uniqueId(), player.serverLocation().chunkPosition());
        if(!optionalChunkFaction.isPresent())
            throw messageService.resolveExceptionWithMessage("error.claim.place-does-not-belong-to-anyone");

        if(optionalChunkFaction.get().isSafeZone() || optionalChunkFaction.get().isWarZone())
            throw messageService.resolveExceptionWithMessage("error.command.attack.you-cant-attack-this-faction");

        if(!super.getPlugin().getPermsManager().canAttack(player.uniqueId(), playerFaction))
            throw messageService.resolveExceptionWithMessage("error.command.attack.players-with-your-rank-cant-attack-territories");

        final Faction attackedFaction = optionalChunkFaction.get();

        if(playerFaction.getName().equals(attackedFaction.getName()))
            throw messageService.resolveExceptionWithMessage("error.command.attack.you-cant-attack-yourself");

        if(playerFaction.getAlliances().contains(attackedFaction.getName()) || playerFaction.getTruces().contains(attackedFaction.getName()))
            throw messageService.resolveExceptionWithMessage("error.command.attack.cant-attack-this-faction-because-of-alliance");

        if(!canAttackFactionPowerCheck(playerFaction, attackedFaction))
            throw messageService.resolveExceptionWithMessage("error.command.attack.cant-attack-this-faction-because-of-to-high-enemy-power");

        int attackTime = this.factionsConfig.getAttackTime();
        Vector3i attackedClaim = player.serverLocation().chunkPosition();

        super.getPlugin().getAttackLogic().informAboutAttack(attackedFaction, player.serverLocation());
        player.sendMessage(messageService.resolveMessageWithPrefix("command.attack.start", attackTime));

        super.getPlugin().getAttackLogic().blockClaiming(attackedFaction.getName());
        super.getPlugin().getAttackLogic().attack(player, attackedClaim);

        return CommandResult.success();
    }

    private boolean canAttackFactionPowerCheck(Faction attacker, Faction target)
    {
        final float neededPowerPercentageToAttack = this.powerConfig.getNeededPowerPercentageToAttack();
        final float targetFactionMaxPower = super.getPlugin().getPowerManager().getFactionMaxPower(target);
        final float vulnerabilityBoundary = targetFactionMaxPower * neededPowerPercentageToAttack;
        final float targetFactionPower = super.getPlugin().getPowerManager().getFactionPower(target);
        final float attackerFactionPower = super.getPlugin().getPowerManager().getFactionPower(attacker);

        return targetFactionPower <= vulnerabilityBoundary && attackerFactionPower >= targetFactionPower;
    }

    private boolean isNight(ServerWorld world)
    {
        return world.properties().dayTime().asTicks().ticks() % 24000L < 12000;
    }
}
