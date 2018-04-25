package io.github.aquerr.eaglefactions.commands;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.AttackLogic;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.MainLogic;
import io.github.aquerr.eaglefactions.managers.PowerManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class AttackCommand implements CommandExecutor
{
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if(source instanceof Player)
        {
            if(MainLogic.shouldAttackOnlyAtNight())
            {
                if(Sponge.getServer().getDefaultWorld().isPresent())
                {
                    if((Sponge.getServer().getDefaultWorld().get().getWorldTime() % 24000L) >= 12000)
                    {
                        Player player = (Player)source;
                        attackChunk(player);
                    }
                    else
                    {
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You can attack someone's territory only at night!"));
                    }
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Unexpected error! Default world is not a normal type world..."));
                }
            }
            else
            {
                Player player = (Player)source;
                attackChunk(player);
            }
        }
        else
        {
            source.sendMessage (Text.of (PluginInfo.ErrorPrefix, TextColors.RED, "Only in-game players can use this command!"));
        }

        return CommandResult.success();
    }

    private void attackChunk(Player player)
    {
        String playerFactionName = FactionLogic.getFactionName(player.getUniqueId());

        if(playerFactionName != null)
        {
            Faction playerFaction = FactionLogic.getFaction(playerFactionName);

            String chunkFactionName = FactionLogic.getFactionByChunk(player.getWorld().getUniqueId(), player.getLocation().getChunkPosition());
            if(chunkFactionName != null)
            {
                if(chunkFactionName.equals("SafeZone") || chunkFactionName.equals("WarZone"))
                {
                    player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You can't attack this faction!"));
                    return;
                }
                else
                {
                    if(playerFaction.Leader.equals(player.getUniqueId().toString()) || playerFaction.Officers.contains(player.getUniqueId().toString()))
                    {
                        Faction attackedFaction = FactionLogic.getFaction(FactionLogic.getFactionByChunk(player.getWorld().getUniqueId(), player.getLocation().getChunkPosition()));

                        if (!playerFaction.Name.equals(attackedFaction.Name))
                        {
                             if(!playerFaction.Alliances.contains(attackedFaction.Name))
                             {
                                 if(PowerManager.getFactionMaxPower(attackedFaction).doubleValue() * MainLogic.getAttackMinPowerPercentage() >= attackedFaction.Power.doubleValue() && playerFaction.Power.doubleValue() > attackedFaction.Power.doubleValue())
                                 {
                                     int attackTime = MainLogic.getAttackTime();
                                     Vector3i attackedClaim = player.getLocation().getChunkPosition();

                                     AttackLogic.informAboutAttack(attackedFaction);
                                     player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Attack on the chunk has been started! Stay in the chunk for ", TextColors.GOLD, attackTime + " seconds", TextColors.GREEN, " to destroy it!"));

                                     AttackLogic.blockClaiming(attackedFaction.Name);
                                     AttackLogic.attack(player, attackedClaim);
                                     return;
                                 }
                                 else
                                 {
                                     player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You can't attack this faction! Their power is too high!"));
                                 }
                             }
                             else
                             {
                                 player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You can't attack this faction! You are in the alliance with it!"));
                             }
                        }
                        else
                        {
                            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You can't attack yourself! :)"));
                        }
                    }
                    else
                    {
                        player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be the faction leader or officer to do this!"));
                    }
                }
            }
            else
            {
                player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "This place does not belongs to anyone!"));
            }
        }
        else
        {
            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be in a faction in order to do this!"));
        }
    }
}
