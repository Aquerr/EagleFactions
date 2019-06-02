package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.message.PluginMessages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class AutoMapCommand extends AbstractCommand
{
    public AutoMapCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if(source instanceof Player)
        {
            Player player = (Player)source;

            if(EagleFactions.AUTO_MAP_LIST.contains(player.getUniqueId()))
            {
                EagleFactions.AUTO_MAP_LIST.remove(player.getUniqueId());

                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GOLD, "AutoMap", TextColors.WHITE, " " + PluginMessages.HAS_BEEN_TURNED + " ", TextColors.GOLD, PluginMessages.OFF));
                return CommandResult.success();
            }
            else
            {
                EagleFactions.AUTO_MAP_LIST.add(player.getUniqueId());

                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GOLD, "AutoMap", TextColors.WHITE, " " + PluginMessages.HAS_BEEN_TURNED + " ", TextColors.GOLD, PluginMessages.ON));
                return CommandResult.success();
            }
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
        }

        return CommandResult.success();
    }
}
