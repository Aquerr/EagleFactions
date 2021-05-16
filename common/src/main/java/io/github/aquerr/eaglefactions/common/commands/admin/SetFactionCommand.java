package io.github.aquerr.eaglefactions.common.commands.admin;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.commands.AbstractCommand;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class SetFactionCommand extends AbstractCommand
{
    public SetFactionCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Player player = context.requireOne(Text.of("player"));
        Faction faction = context.requireOne(Text.of("faction"));
        FactionMemberType factionMemberType = context.requireOne(Text.of("rank"));

        if (factionMemberType == FactionMemberType.ALLY || factionMemberType == FactionMemberType.NONE || factionMemberType == FactionMemberType.TRUCE)
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "The given rank is not valid!"));

        super.getPlugin().getFactionLogic().setFaction(player.getUniqueId(), faction.getName(), factionMemberType);
        source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, "Player's faction has been changed!"));
        return CommandResult.success();
    }
}
