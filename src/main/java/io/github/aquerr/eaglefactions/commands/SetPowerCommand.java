package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.services.PowerService;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.util.Optional;

public class SetPowerCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Optional<Player> selectedPlayer = context.<Player>getOne("player");
        Optional<String> power = context.<String>getOne("power");

        if(source instanceof Player)
        {
            Player player = (Player)source;

            if(EagleFactions.AdminList.contains(player.getUniqueId().toString()))
            {
                if(selectedPlayer != null)
                {
                    if(power.isPresent())
                    {
                        BigDecimal newPower = new BigDecimal(power.get());

                        PowerService.setPower(selectedPlayer.get().getUniqueId(), newPower);

                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Player's power has been changed!"));
                        return CommandResult.success();
                    }
                    else
                    {

                    }
                }
                else
                {
                    player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "There is no such player."));
                }
            }
            else
            {
                player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You need to toggle faction admin mode to do this!"));
            }
        }
        else
        {
            source.sendMessage (Text.of (PluginInfo.ErrorPrefix, TextColors.RED, "Only in-game players can use this command!"));
        }

        return CommandResult.success();
    }
}
