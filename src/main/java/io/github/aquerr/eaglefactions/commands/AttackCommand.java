package io.github.aquerr.eaglefactions.commands;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.AttackLogic;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.services.PowerService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.concurrent.TimeUnit;

public class AttackCommand implements CommandExecutor
{
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if(source instanceof Player)
        {
            Player player = (Player)source;

            String playerFactionName = FactionLogic.getFactionName(player.getUniqueId());

            if(playerFactionName != null)
            {
                if(FactionLogic.isClaimed(player.getWorld().getUniqueId(), player.getLocation().getChunkPosition()))
                {
                    if(FactionLogic.getLeader(playerFactionName).equals(player.getUniqueId().toString()) || FactionLogic.getOfficers(playerFactionName).contains(player.getUniqueId().toString()))
                    {
                        Faction faction = FactionLogic.getFaction(FactionLogic.getFactionNameByChunk(player.getWorld().getUniqueId(), player.getLocation().getChunkPosition()));

                        if(!FactionLogic.getAlliances(playerFactionName).contains(faction.Name))
                        {
                            if(PowerService.getFactionMaxPower(faction).doubleValue() * 0.2 >= PowerService.getFactionPower(faction).doubleValue() && PowerService.getFactionPower(FactionLogic.getFaction(playerFactionName)).doubleValue() > PowerService.getFactionPower(faction).doubleValue())
                            {
                                Vector3i attackedClaim = player.getLocation().getChunkPosition();
                                int seconds = 0;

                                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Attack on the chunk has been started! Stay in the chunk for 10 seconds to destroy it!"));
                                AttackLogic.attack(player, attackedClaim, seconds);
                                return CommandResult.success();
                            }
                            else
                            {
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You can't attack this faction! Their power is too high!"));
                            }
                        }
                        else
                        {
                            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You can't attack this faction! You are in the alliance with it!"));
                        }
                    }
                    else
                    {
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be the faction leader or officer to do this!"));
                    }
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "This place does not belongs to anyone!"));
                }
            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be in a faction in order to do this!"));
            }
        }
        else
        {
            source.sendMessage (Text.of (PluginInfo.ErrorPrefix, TextColors.RED, "Only in-game players can use this command!"));
        }

        return CommandResult.success();
    }
}
