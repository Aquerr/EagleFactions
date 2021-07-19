package io.github.aquerr.eaglefactions.commands.admin;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.Messages;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class SetMaxPowerForAllCommand extends AbstractCommand
{
    public SetMaxPowerForAllCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final double power = context.requireOne(Parameter.doubleNumber().key("power").build());

        if (isPlayer(context))
        {
            final ServerPlayer player = (ServerPlayer) context.cause().audience();
            if (!super.getPlugin().getPlayerManager().hasAdminMode(player))
                throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_NEED_TO_TOGGLE_FACTION_ADMIN_MODE_TO_DO_THIS, NamedTextColor.RED)));
        }
        setMaxPower(context, (float) power);
        return CommandResult.success();
    }

    private void setMaxPower(CommandContext context, float power)
    {
        for (final FactionPlayer factionPlayer : super.getPlugin().getPlayerManager().getServerPlayers())
        {
            super.getPlugin().getPowerManager().setPlayerMaxPower(factionPlayer.getUniqueId(), power);
        }
        context.sendMessage(Identity.nil(), PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.PLAYERS_MAXPOWER_HAS_BEEN_CHANGED, NamedTextColor.GREEN)));
    }
}
