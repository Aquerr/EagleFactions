package io.github.aquerr.eaglefactions.commands;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

public class SetHomeCommand implements CommandExecutor
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
                World world = player.getWorld();

                if(EagleFactions.AdminList.contains(player.getUniqueId()))
                {
                    Vector3i home = new Vector3i(player.getLocation().getBlockPosition());
                    FactionLogic.setHome(world.getUniqueId(), playerFactionName, home);
                    source.sendMessage(Text.of(PluginInfo.PluginPrefix, "Faction home has been set!"));

                    return CommandResult.success();
                }

                if(FactionLogic.getLeader(playerFactionName).equals(player.getUniqueId().toString()) || FactionLogic.getOfficers(playerFactionName).contains(player.getUniqueId().toString()))
                {
                    if(FactionLogic.isClaimed(world.getUniqueId(), player.getLocation().getChunkPosition()))
                    {
                        Vector3i home = new Vector3i(player.getLocation().getBlockPosition());

                        FactionLogic.setHome(world.getUniqueId(), playerFactionName, home);
                        source.sendMessage(Text.of(PluginInfo.PluginPrefix, "Faction home has been set!"));
                    }
                    else
                    {
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "This place is not claimed! You can set home only in claimed land!"));
                    }
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be the faction leader or officer to do this!"));
                }

            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be in a faction in order to use this command!"));
            }

        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Only in-game players can use this command!"));
        }

        return CommandResult.success();
    }
}
