package io.github.aquerr.eaglefactions.commands.general;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.PowerConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.messaging.Placeholders;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collections;
import java.util.Optional;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class AttackCommand extends AbstractCommand
{
    private final FactionsConfig factionsConfig;
    private final PowerConfig powerConfig;

    public AttackCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.powerConfig = plugin.getConfiguration().getPowerConfig();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final ServerPlayer player = requirePlayerSource(context);

        if(this.factionsConfig.canAttackOnlyAtNight() && isNight(player.world()))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_CAN_ATTACK_SOMEONES_TERRITORY_ONLY_AT_NIGHT, RED)));

        return attackChunk(player);
    }

    private CommandResult attackChunk(ServerPlayer player) throws CommandException
    {
        final Faction playerFaction = requirePlayerFaction(player);
        final Optional<Faction> optionalChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(player.world().uniqueId(), player.serverLocation().chunkPosition());
        if(!optionalChunkFaction.isPresent())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.THIS_PLACE_DOES_NOT_BELONG_TO_ANYONE, RED)));

        if(optionalChunkFaction.get().isSafeZone() || optionalChunkFaction.get().isWarZone())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_CANT_ATTACK_THIS_FACTION, RED)));

        if(!super.getPlugin().getPermsManager().canAttack(player.uniqueId(), playerFaction))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.PLAYERS_WITH_YOUR_RANK_CANT_ATTACK_LANDS, RED)));

        final Faction attackedFaction = optionalChunkFaction.get();

        if(playerFaction.getName().equals(attackedFaction.getName()))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_CANT_ATTACK_YOURSELF, RED)));

        if(playerFaction.getAlliances().contains(attackedFaction.getName()) || playerFaction.getTruces().contains(attackedFaction.getName()))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_CANT_ATTACK_THIS_FACTION + " " + Messages.YOU_ARE_IN_THE_SAME_ALLIANCE, RED)));

        if(!canAttackFactionPowerCheck(playerFaction, attackedFaction))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_CANT_ATTACK_THIS_FACTION + " " + Messages.THEIR_POWER_IS_TO_HIGH, RED)));

        int attackTime = this.factionsConfig.getAttackTime();
        Vector3i attackedClaim = player.serverLocation().chunkPosition();

        super.getPlugin().getAttackLogic().informAboutAttack(attackedFaction, player.serverLocation());
        player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text(Messages.ATTACK_ON_THE_CHUNK_HAS_BEEN_STARTED + " ", GREEN)).append(MessageLoader.parseMessage(Messages.STAY_IN_THE_CHUNK_FOR_NUMBER_SECONDS_TO_DESTROY_IT, GREEN, Collections.singletonMap(Placeholders.NUMBER, text(attackTime, GOLD)))));

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
