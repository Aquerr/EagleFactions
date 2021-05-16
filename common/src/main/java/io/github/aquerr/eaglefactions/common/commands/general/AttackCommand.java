package io.github.aquerr.eaglefactions.common.commands.general;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.PowerConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.common.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import io.github.aquerr.eaglefactions.common.messaging.Placeholders;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.Collections;
import java.util.Optional;

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
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        final Player player = requirePlayerSource(source);

        if(this.factionsConfig.canAttackOnlyAtNight() && isNight(player.getWorld()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CAN_ATTACK_SOMEONES_TERRITORY_ONLY_AT_NIGHT));

        return attackChunk(player);
    }

    private CommandResult attackChunk(Player player) throws CommandException
    {
        final Faction playerFaction = requirePlayerFaction(player);
        final Optional<Faction> optionalChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(player.getWorld().getUniqueId(), player.getLocation().getChunkPosition());
        if(!optionalChunkFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.THIS_PLACE_DOES_NOT_BELONG_TO_ANYONE));

        if(optionalChunkFaction.get().isSafeZone() || optionalChunkFaction.get().isWarZone())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_ATTACK_THIS_FACTION));

        if(!super.getPlugin().getPermsManager().canAttack(player.getUniqueId(), playerFaction))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.PLAYERS_WITH_YOUR_RANK_CANT_ATTACK_LANDS));

        final Faction attackedFaction = optionalChunkFaction.get();

        if(playerFaction.getName().equals(attackedFaction.getName()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_ATTACK_YOURSELF));

        if(playerFaction.getAlliances().contains(attackedFaction.getName()) || playerFaction.getTruces().contains(attackedFaction.getName()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_ATTACK_THIS_FACTION + " " + Messages.YOU_ARE_IN_THE_SAME_ALLIANCE));

        if(!canAttackFactionPowerCheck(playerFaction, attackedFaction))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_ATTACK_THIS_FACTION + " " + Messages.THEIR_POWER_IS_TO_HIGH));

        int attackTime = this.factionsConfig.getAttackTime();
        Vector3i attackedClaim = player.getLocation().getChunkPosition();

        super.getPlugin().getAttackLogic().informAboutAttack(attackedFaction, player.getLocation());
        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, Messages.ATTACK_ON_THE_CHUNK_HAS_BEEN_STARTED + " ", MessageLoader.parseMessage(Messages.STAY_IN_THE_CHUNK_FOR_NUMBER_SECONDS_TO_DESTROY_IT, TextColors.GREEN, Collections.singletonMap(Placeholders.NUMBER, Text.of(TextColors.GOLD, attackTime)))));

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

    private boolean isNight(World world)
    {
        return world.getProperties().getWorldTime() % 24000L < 12000;
    }
}
