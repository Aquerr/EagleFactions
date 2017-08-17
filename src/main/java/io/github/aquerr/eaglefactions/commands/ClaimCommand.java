package io.github.aquerr.eaglefactions.commands;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.services.PowerService;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;

public class ClaimCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if(source instanceof Player)
        {
            Player player = (Player)source;

            String playerFactionName = FactionLogic.getFactionName(player.getUniqueId());

            if(playerFactionName != null)
            {
                if(FactionLogic.getLeader(playerFactionName).equals(player.getUniqueId().toString()) || FactionLogic.getOfficers(playerFactionName).contains(player.getUniqueId().toString()))
                {
                    Vector3i chunk = player.getLocation().getChunkPosition();

                    if(!FactionLogic.isClaimed(chunk))
                    {

                        if(PowerService.getFactionPower(FactionLogic.getFaction(playerFactionName)).doubleValue() >= FactionLogic.getClaims(playerFactionName).size())
                        {
                            if(!FactionLogic.getClaims(playerFactionName).isEmpty())
                            {
                                if(FactionLogic.isClaimConnected(playerFactionName, chunk))
                                {
                                    FactionLogic.addClaim(playerFactionName, chunk);

                                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Land ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " has been successfully ", TextColors.GOLD, "claimed", TextColors.WHITE, "!"));
                                    return CommandResult.success();
                                }
                                else
                                {
                                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Claims needs to be connected!"));
                                }
                            }
                            else
                            {
                                FactionLogic.addClaim(playerFactionName, chunk);

                                player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Land ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " has been successfully ", TextColors.GOLD, "claimed", TextColors.WHITE, "!"));
                                return CommandResult.success();
                            }

                        }
                        else
                        {
                            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Your faction does not have power to claim more land!"));
                        }
                    }
                    else
                    {
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "This place is already claimed!"));
                    }

                }
                else if(EagleFactions.AdminList.contains(player.getUniqueId().toString()))
                {
                    Vector3i chunk = player.getLocation().getChunkPosition();

                    if(!FactionLogic.isClaimed(chunk))
                    {
                        FactionLogic.addClaim(playerFactionName, chunk);

                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Land ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " has been successfully ", TextColors.GOLD, "claimed", TextColors.WHITE, "!"));
                        return CommandResult.success();
                    }
                    else
                    {
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "This place is already claimed!"));
                    }
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be the faction leader or officer to do this!"));
                }
            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be in a faction in order to claim lands!"));
            }
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Only in-game players can use this command!"));
        }

        return CommandResult.success();
    }
}
