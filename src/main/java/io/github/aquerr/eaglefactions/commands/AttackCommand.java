package io.github.aquerr.eaglefactions.commands;

import com.flowpowered.math.vector.Vector3i;
import com.google.inject.Inject;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.AttackLogic;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import io.github.aquerr.eaglefactions.managers.FlagManager;
import io.github.aquerr.eaglefactions.managers.PowerManager;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class AttackCommand implements CommandExecutor
{

    @Inject
    private Settings settings;

    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if (source instanceof Player)
        {
            Player player = (Player) source;
            if (settings.shouldAttackOnlyAtNight())
            {
                if ((player.getWorld().getProperties().getWorldTime() % 24000L) >= 12000)
                {
                    attackChunk(player);
                } else
                {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_CAN_ATTACK_SOMEONES_TERRITORY_ONLY_AT_NIGHT));
                }
            } else
            {
                attackChunk(player);
            }
        } else
        {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
        }

        return CommandResult.success();
    }

    private void attackChunk(Player player)
    {
        Optional<Faction> optionalPlayerFaction = FactionLogic.getFactionByPlayerUUID(player.getUniqueId());

        if (optionalPlayerFaction.isPresent())
        {
            Faction playerFaction = optionalPlayerFaction.get();

            Optional<Faction> optionalChunkFaction = FactionLogic.getFactionByChunk(player.getWorld().getUniqueId(), player.getLocation().getChunkPosition());
            if (optionalChunkFaction.isPresent())
            {
                if (optionalChunkFaction.get().Name.equals("SafeZone") || optionalChunkFaction.get().Name.equals("WarZone"))
                {
                    player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_CANT_ATTACK_THIS_FACTION));
                    return;
                } else
                {
                    if (FlagManager.canAttack(player, playerFaction))
                    {
                        Faction attackedFaction = optionalChunkFaction.get();

                        if (!playerFaction.Name.equals(attackedFaction.Name))
                        {
                            if (!playerFaction.Alliances.contains(attackedFaction.Name))
                            {
                                if (PowerManager.getFactionMaxPower(attackedFaction).doubleValue() * settings.getAttackMinPowerPercentage() >= PowerManager.getFactionPower(attackedFaction).doubleValue() && PowerManager.getFactionPower(playerFaction).doubleValue() > PowerManager.getFactionPower(attackedFaction).doubleValue())
                                {
                                    int attackTime = settings.getAttackTime();
                                    Vector3i attackedClaim = player.getLocation().getChunkPosition();

                                    AttackLogic.informAboutAttack(attackedFaction);
                                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.ATTACK_ON_THE_CHUNK_HAS_BEEN_STARTED + " " + PluginMessages.STAY_IN_THE_CHUNK_FOR + " ", TextColors.GOLD, attackTime + " " + PluginMessages.SECONDS, TextColors.GREEN, " " + PluginMessages.TO_DESTROY_IT));

                                    AttackLogic.blockClaiming(attackedFaction.Name);
                                    AttackLogic.attack(player, attackedClaim);
                                    return;
                                } else
                                {
                                    player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_CANT_ATTACK_THIS_FACTION + " " + PluginMessages.THEIR_POWER_IS_TO_HIGH));
                                }
                            } else
                            {
                                player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_CANT_ATTACK_THIS_FACTION + " " + PluginMessages.YOU_ARE_IN_THE_SAME_ALLIANCE));
                            }
                        } else
                        {
                            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_CANT_ATTACK_YOURSELF));
                        }
                    } else
                    {
                        player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.PLAYERS_WITH_YOUR_RANK_CANT_ATTACK_LANDS));
                    }
                }
            } else
            {
                player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THIS_PLACE_DOES_NOT_BELOG_TO_ANYONE));
            }
        } else
        {
            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
        }
    }
}
