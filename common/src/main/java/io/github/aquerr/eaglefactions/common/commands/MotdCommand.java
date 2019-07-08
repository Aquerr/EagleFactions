package io.github.aquerr.eaglefactions.common.commands;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.common.message.PluginMessages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class MotdCommand extends AbstractCommand
{
    public MotdCommand(final EagleFactionsPlugin plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        final Optional<String> optionalMotd = context.<String>getOne("motd");

        if (!optionalMotd.isPresent())
        {
            source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.WRONG_COMMAND_ARGUMENTS));
            source.sendMessage(Text.of(TextColors.RED, PluginMessages.USAGE + " /f motd <motd>"));
            return CommandResult.success();
        }

        if (!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        final Player player = (Player) source;
        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        if (!optionalPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        //Check if player is leader
        if (!optionalPlayerFaction.get().getLeader().equals(player.getUniqueId()) && !optionalPlayerFaction.get().getOfficers().contains(player.getUniqueId()))
        {
            source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS));
            return CommandResult.success();
        }

        final String motd = optionalMotd.get();

        //Check motd length
        if(motd.length() > 255)
        {
            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "Motd is too long " + " (" + PluginMessages.MAX + " " + 255 + " " + PluginMessages.CHARS + ")"));
            return CommandResult.success();
        }

        super.getPlugin().getFactionLogic().setMessageOfTheDay(optionalPlayerFaction.get(), motd);
        player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GREEN, "Faction's message of the day has been updated!")));
        return CommandResult.success();
    }
}
