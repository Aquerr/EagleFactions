package io.github.aquerr.eaglefactions.common.commands;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.PowerConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.message.PluginMessages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

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
        if(!(source instanceof Player))
            throw new CommandException(Text.of (PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        final Player player = (Player)source;

        if(this.factionsConfig.canAttackOnlyAtNight() && player.getWorld().getProperties().getWorldTime() % 24000L < 12000)
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_CAN_ATTACK_SOMEONES_TERRITORY_ONLY_AT_NIGHT));

        return attackChunk(player);
    }

    private CommandResult attackChunk(Player player) throws CommandException
    {
        final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        if(!optionalPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        final Faction playerFaction = optionalPlayerFaction.get();
        final Optional<Faction> optionalChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(player.getWorld().getUniqueId(), player.getLocation().getChunkPosition());
        if(!optionalChunkFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.THIS_PLACE_DOES_NOT_BELOG_TO_ANYONE));

        if(optionalChunkFaction.get().getName().equals("SafeZone") || optionalChunkFaction.get().getName().equals("WarZone"))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_CANT_ATTACK_THIS_FACTION));

        if(!super.getPlugin().getFlagManager().canAttack(player.getUniqueId(), playerFaction))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.PLAYERS_WITH_YOUR_RANK_CANT_ATTACK_LANDS));

        final Faction attackedFaction = optionalChunkFaction.get();

        if(playerFaction.getName().equals(attackedFaction.getName()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_CANT_ATTACK_YOURSELF));

        if(playerFaction.getAlliances().contains(attackedFaction.getName()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_CANT_ATTACK_THIS_FACTION + " " + PluginMessages.YOU_ARE_IN_THE_SAME_ALLIANCE));

        final float neededPowerPercentageToAttack = this.powerConfig.getNeededPowerPercentageToAttack();
        final float attackedFactionMaxPower = super.getPlugin().getPowerManager().getFactionMaxPower(attackedFaction);
        final float attackedFactionPower = super.getPlugin().getPowerManager().getFactionPower(attackedFaction);
        final float playerFactionPower = super.getPlugin().getPowerManager().getFactionPower(playerFaction);

        if(attackedFactionMaxPower * neededPowerPercentageToAttack < attackedFactionPower || playerFactionPower < attackedFactionPower)
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_CANT_ATTACK_THIS_FACTION + " " + PluginMessages.THEIR_POWER_IS_TO_HIGH));

        int attackTime = this.factionsConfig.getAttackTime();
        Vector3i attackedClaim = player.getLocation().getChunkPosition();

        super.getPlugin().getAttackLogic().informAboutAttack(attackedFaction);
        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.ATTACK_ON_THE_CHUNK_HAS_BEEN_STARTED + " " + PluginMessages.STAY_IN_THE_CHUNK_FOR + " ", TextColors.GOLD, attackTime + " " + PluginMessages.SECONDS, TextColors.GREEN, " " + PluginMessages.TO_DESTROY_IT));

        super.getPlugin().getAttackLogic().blockClaiming(attackedFaction.getName());
        super.getPlugin().getAttackLogic().attack(player, attackedClaim);

        return CommandResult.success();
    }
}
