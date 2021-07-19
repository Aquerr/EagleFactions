package io.github.aquerr.eaglefactions.commands.admin;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.CommonParameters;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class SetFactionCommand extends AbstractCommand
{
    public SetFactionCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        ServerPlayer player = context.requireOne(CommonParameters.PLAYER);
        Faction faction = context.requireOne(EagleFactionsCommandParameters.faction());
        FactionMemberType factionMemberType = context.requireOne(Parameter.enumValue(FactionMemberType.class).key("rank").build());

        if (factionMemberType == FactionMemberType.ALLY || factionMemberType == FactionMemberType.NONE || factionMemberType == FactionMemberType.TRUCE)
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text("The given rank is not valid!", NamedTextColor.RED)));

        super.getPlugin().getFactionLogic().setFaction(player.uniqueId(), faction.getName(), factionMemberType);
        context.sendMessage(Identity.nil(), PluginInfo.PLUGIN_PREFIX.append(Component.text("Player's faction has been changed!", NamedTextColor.GREEN)));
        return CommandResult.success();
    }
}
