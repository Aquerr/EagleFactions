package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionFlagType;
import io.github.aquerr.eaglefactions.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class FlagsCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if (source instanceof Player)
        {
            Player player = (Player)source;

            String playerFactionName = FactionLogic.getFactionName(player.getUniqueId());

            if (playerFactionName != null)
            {
                Faction faction = FactionLogic.getFaction(playerFactionName);

                if (faction.Leader.equals(player.getUniqueId().toString()) || EagleFactions.AdminList.contains(player.getUniqueId()))
                {
                    showFlags(player, faction);
                }
                else
                {
                    player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be a faction's leader to use this command!"));
                }
            }
            else
            {
                player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be in a faction in order to do this!"));
            }
        }
        else
        {
            source.sendMessage (Text.of (PluginInfo.ErrorPrefix, TextColors.RED, "Only in-game players can use this command!"));
        }

        return CommandResult.success();
    }

    private void showFlags(Player player, Faction faction)
    {
        Text.Builder textBuilder = Text.builder();
        Text.Builder flagTextBuilder;

        textBuilder.append(Text.of(TextColors.AQUA, "------------------------------" + "\n"));
        //textBuilder.append(Text.of(TextColors.AQUA, "______________________________" + "\n"));
        textBuilder.append(Text.of(TextColors.AQUA, "|   WHO    |  USE  | PLACE | DESTROY |"));
        textBuilder.append(Text.of(TextColors.AQUA, "------------------------------"));


        for (Map.Entry<FactionMemberType, Map<FactionFlagType, Boolean>> memberEntry : faction.Flags.entrySet())
        {
            textBuilder.append(Text.of("\n"));

            Map<FactionFlagType, Boolean> memberFlags = memberEntry.getValue();

            textBuilder.append(Text.of(TextColors.AQUA,"| " + memberEntry.getKey().toString()));

            for (Map.Entry<FactionFlagType, Boolean> flagEntry : memberFlags.entrySet())
            {
                textBuilder.append(Text.of(TextColors.AQUA, " | "));

                Boolean flagValue = flagEntry.getValue();
                flagTextBuilder = Text.builder();
                flagTextBuilder.append(Text.of(flagValue.toString()));
                flagTextBuilder.onClick(TextActions.executeCallback(toggleFlag(faction, memberEntry.getKey(), flagEntry.getKey(), flagValue)));
                flagTextBuilder.onHover(TextActions.showText(Text.of("Set to " + String.valueOf(!flagValue).toUpperCase())));

                if (flagValue.booleanValue())
                {
                    flagTextBuilder.color(TextColors.GREEN);
                }
                else
                {
                    flagTextBuilder.color(TextColors.RED);
                }

                textBuilder.append(flagTextBuilder.build());
            }
        }

        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Permissions flags for " + faction.Name + ":"));
        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Click on the permission you want to change."));
        player.sendMessage(textBuilder.build());
    }

    private Consumer<CommandSource> toggleFlag(Faction faction, FactionMemberType factionMemberType, FactionFlagType factionFlagType, Boolean toggled)
    {
        return commandSource ->
        {
            FactionLogic.toggleFlag(faction, factionMemberType, factionFlagType, toggled);
            showFlags((Player)commandSource, faction);
        };
    }
}
