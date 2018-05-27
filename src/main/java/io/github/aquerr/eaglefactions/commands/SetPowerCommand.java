package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import io.github.aquerr.eaglefactions.managers.PowerManager;
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
        Optional<Player> optionalSelectedPlayer = context.<Player>getOne("player");
        Optional<String> optionalPower = context.<String>getOne("power");

        if (optionalSelectedPlayer.isPresent() && optionalPower.isPresent())
        {
            if (source instanceof Player)
            {
                Player player = (Player) source;

                if (EagleFactions.AdminList.contains(player.getUniqueId()))
                {
                    setPower(optionalSelectedPlayer.get(), optionalPower.get());
                }
                else
                {
                    player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_NEED_TO_TOGGLE_FACTION_ADMIN_MODE_TO_DO_THIS));
                }
            }
            else
            {
                setPower(optionalSelectedPlayer.get(), optionalPower.get());
            }
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.WRONG_COMMAND_ARGUMENTS));
            source.sendMessage(Text.of(TextColors.RED, PluginMessages.USAGE + " /f setpower <player> <power>"));
        }

        return CommandResult.success();
    }

    private void setPower(Player player, String power)
    {
        BigDecimal newPower = new BigDecimal(power);

        PowerManager.setPower(player.getUniqueId(), newPower);

        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.PLAYERS_POWER_HAS_BEEN_CHANGED));
    }
}
