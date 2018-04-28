package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionFlagType;
import io.github.aquerr.eaglefactions.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.security.PublicKey;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class FlagsCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if (source instanceof Player)
        {
            Player player = (Player)source;

            Optional<Faction> optionalPlayerFaction = FactionLogic.getFactionByPlayerUUID(player.getUniqueId());

            if (optionalPlayerFaction.isPresent())
            {
                Faction faction = optionalPlayerFaction.get();

                if (faction.Leader.equals(player.getUniqueId().toString()) || EagleFactions.AdminList.contains(player.getUniqueId()))
                {
                    showFlags(player, faction);
                }
                else
                {
                    player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_MUST_BE_THE_FACTIONS_LEADER_TO_DO_THIS));
                }
            }
            else
            {
                player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
            }
        }
        else
        {
            source.sendMessage (Text.of (PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
        }

        return CommandResult.success();
    }

    private void showFlags(Player player, Faction faction)
    {
        Text.Builder textBuilder = Text.builder();
        Text.Builder flagTextBuilder;

        //textBuilder.append(Text.of(TextColors.AQUA, "------------------------------" + "\n"));
        //textBuilder.append(Text.of(TextColors.AQUA, "|   WHO    |  USE  | PLACE | DESTROY |"));
        //textBuilder.append(Text.of(TextColors.AQUA, "------------------------------"));

        textBuilder.append(Text.of(TextColors.AQUA, "|   " + PluginMessages.WHO + "    |  " + PluginMessages.USE + "  | " + PluginMessages.PLACE + " | " + PluginMessages.DESTROY));


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
                flagTextBuilder.onHover(TextActions.showText(Text.of(PluginMessages.SET_TO + " " + String.valueOf(!flagValue).toUpperCase())));

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

        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.PERMISSIONS_FLAGS_FOR + " " + faction.Name + ":"));
        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.CLICK_ON_THE_PERMISSION_YOU_WANT_TO_CHANGE));
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
