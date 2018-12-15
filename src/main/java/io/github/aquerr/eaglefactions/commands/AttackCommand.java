package io.github.aquerr.eaglefactions.commands;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.message.PluginMessages;
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
    public AttackCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if(source instanceof Player)
        {
            Player player = (Player)source;
            if(getPlugin().getConfiguration().getConfigFields().canAttackOnlyAtNight())
            {
                if((player.getWorld().getProperties().getWorldTime() % 24000L) >= 12000)
                {
                    attackChunk(player);
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_CAN_ATTACK_SOMEONES_TERRITORY_ONLY_AT_NIGHT));
                }
            }
            else
            {
                attackChunk(player);
            }
        }
        else
        {
            source.sendMessage (Text.of (PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
        }

        return CommandResult.success();
    }

    private void attackChunk(Player player)
    {
        Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());

        if(optionalPlayerFaction.isPresent())
        {
            Faction playerFaction = optionalPlayerFaction.get();

            Optional<Faction> optionalChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(player.getWorld().getUniqueId(), player.getLocation().getChunkPosition());
            if(optionalChunkFaction.isPresent())
            {
                if(optionalChunkFaction.get().getName().equals("SafeZone") || optionalChunkFaction.get().getName().equals("WarZone"))
                {
                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_CANT_ATTACK_THIS_FACTION));
                    return;
                }
                else
                {
                    if (this.getPlugin().getFlagManager().canAttack(player.getUniqueId(), playerFaction))
                    {
                        Faction attackedFaction = optionalChunkFaction.get();

                        if (!playerFaction.getName().equals(attackedFaction.getName()))
                        {
                            if(!playerFaction.getAlliances().contains(attackedFaction.getName()))
                            {
                                if(getPlugin().getPowerManager().getFactionMaxPower(attackedFaction).doubleValue() * getPlugin().getConfiguration().getConfigFields().getNeededPowerPercentageToAttack() >= getPlugin().getPowerManager().getFactionPower(attackedFaction).doubleValue() && getPlugin().getPowerManager().getFactionPower(playerFaction).doubleValue() > getPlugin().getPowerManager().getFactionPower(attackedFaction).doubleValue())
                                {
                                    int attackTime = getPlugin().getConfiguration().getConfigFields().getAttackTime();
                                    Vector3i attackedClaim = player.getLocation().getChunkPosition();

                                    getPlugin().getAttackLogic().informAboutAttack(attackedFaction);
                                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.ATTACK_ON_THE_CHUNK_HAS_BEEN_STARTED + " " + PluginMessages.STAY_IN_THE_CHUNK_FOR + " ", TextColors.GOLD, attackTime + " " + PluginMessages.SECONDS, TextColors.GREEN, " " + PluginMessages.TO_DESTROY_IT));

                                    getPlugin().getAttackLogic().blockClaiming(attackedFaction.getName());
                                    getPlugin().getAttackLogic().attack(player, attackedClaim);
                                    return;
                                }
                                else
                                {
                                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_CANT_ATTACK_THIS_FACTION + " " + PluginMessages.THEIR_POWER_IS_TO_HIGH));
                                }
                            }
                            else
                            {
                                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_CANT_ATTACK_THIS_FACTION + " " + PluginMessages.YOU_ARE_IN_THE_SAME_ALLIANCE));
                            }
                        }
                        else
                        {
                            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_CANT_ATTACK_YOURSELF));
                        }
                    }
                    else
                    {
                        player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.PLAYERS_WITH_YOUR_RANK_CANT_ATTACK_LANDS));
                    }
                }
            }
            else
            {
                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.THIS_PLACE_DOES_NOT_BELOG_TO_ANYONE));
            }
        }
        else
        {
            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
        }
    }
}
