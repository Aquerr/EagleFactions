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

public class MaxPowerCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Optional<Player> optionalSelectedPlayer = context.<Player>getOne(Text.of("player"));
        Optional<String> optionalPower = context.<String>getOne(Text.of("power"));

        if (optionalSelectedPlayer.isPresent() && optionalPower.isPresent())
        {
            if (source instanceof Player)
            {
                Player player = (Player) source;

                if (EagleFactions.AdminList.contains(player.getUniqueId()))
                {
                    setMaxPower(optionalSelectedPlayer.get(), optionalPower.get());
                }
                else
                {
                    player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You need to toggle faction admin mode to do this!"));
                }
            }
            else
            {
                setMaxPower(optionalSelectedPlayer.get(), optionalPower.get());
            }
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Wrong command arguments!"));
            source.sendMessage(Text.of(TextColors.RED, "Usage: /f maxpower <player> <power>"));
        }

        return CommandResult.success();
    }

    private void setMaxPower(Player player, String power)
    {
        BigDecimal newPower = new BigDecimal(power);

        PowerService.setMaxPower(player.getUniqueId(), newPower);

        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Player's maxpower has been changed!"));
    }
}
