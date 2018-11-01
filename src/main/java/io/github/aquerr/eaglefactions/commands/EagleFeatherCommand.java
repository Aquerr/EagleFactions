package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.EagleFeather;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class EagleFeatherCommand extends AbstractCommand
{
    public EagleFeatherCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException
    {
        if(!(source instanceof Player))
        {
            source.sendMessage (Text.of (PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
            return CommandResult.success();
        }

        Player player = (Player)source;
        player.getInventory().offer(EagleFeather.getEagleFeatherItem());

        return CommandResult.success();
    }
}
