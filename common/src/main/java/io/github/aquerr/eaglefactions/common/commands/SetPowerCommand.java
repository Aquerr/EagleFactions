package io.github.aquerr.eaglefactions.common.commands;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
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

public class SetPowerCommand extends AbstractCommand
{
    public SetPowerCommand(EagleFactions plugin)
    {
        super(plugin);
    }

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

                if (EagleFactionsPlugin.ADMIN_MODE_PLAYERS.contains(player.getUniqueId()))
                {
                    setPower(source, optionalSelectedPlayer.get(), optionalPower.get());
                }
                else
                {
                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_NEED_TO_TOGGLE_FACTION_ADMIN_MODE_TO_DO_THIS));
                }
            }
            else
            {
                setPower(source, optionalSelectedPlayer.get(), optionalPower.get());
            }
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.WRONG_COMMAND_ARGUMENTS));
            source.sendMessage(Text.of(TextColors.RED, PluginMessages.USAGE + " /f setpower <player> <power>"));
        }

        return CommandResult.success();
    }

    private void setPower(CommandSource source, Player player, String power)
    {
        float newPower = Float.valueOf(power);
        super.getPlugin().getPowerManager().setPower(player.getUniqueId(), newPower);
        source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.PLAYERS_POWER_HAS_BEEN_CHANGED));
    }
}
