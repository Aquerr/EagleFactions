package io.github.aquerr.eaglefactions.common.commands.admin;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class SetMaxPowerForAllCommand extends AbstractCommand
{
    public SetMaxPowerForAllCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        final double power = context.requireOne(Text.of("power"));

        if (isPlayer(source))
        {
            final Player player = (Player) source;
            if (!super.getPlugin().getPlayerManager().hasAdminMode(player))
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_NEED_TO_TOGGLE_FACTION_ADMIN_MODE_TO_DO_THIS));
        }
        setMaxPower(source, (float) power);
        return CommandResult.success();
    }

    private void setMaxPower(CommandSource source, float power)
    {
        for (final FactionPlayer factionPlayer : super.getPlugin().getPlayerManager().getServerPlayers())
        {
            super.getPlugin().getPowerManager().setPlayerMaxPower(factionPlayer.getUniqueId(), power);
        }
        source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, Messages.PLAYERS_MAXPOWER_HAS_BEEN_CHANGED));
    }
}
