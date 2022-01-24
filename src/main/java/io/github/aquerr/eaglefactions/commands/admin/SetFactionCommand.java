package io.github.aquerr.eaglefactions.commands.admin;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.CommonParameters;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class SetFactionCommand extends AbstractCommand
{
    public SetFactionCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        Player player = context.requireOne(CommonParameters.PLAYER);
        Faction faction = context.requireOne(EagleFactionsCommandParameters.faction());
        FactionMemberType factionMemberType = context.requireOne(Parameter.enumValue(FactionMemberType.class).key("rank").build());

        if (factionMemberType == FactionMemberType.ALLY || factionMemberType == FactionMemberType.NONE || factionMemberType == FactionMemberType.TRUCE)
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text("The given rank is not valid!", RED)));

        super.getPlugin().getFactionLogic().setFaction(player.uniqueId(), faction.getName(), factionMemberType);
        context.cause().audience().sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Player's faction has been changed!", GREEN)));
        return CommandResult.success();
    }
}
