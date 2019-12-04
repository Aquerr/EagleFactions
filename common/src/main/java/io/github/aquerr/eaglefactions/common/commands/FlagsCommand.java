package io.github.aquerr.eaglefactions.common.commands;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionFlagTypes;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.message.PluginMessages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class FlagsCommand extends AbstractCommand
{
    public FlagsCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        if (source instanceof Player)
        {
            final Player player = (Player)source;
            final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());

            if (optionalPlayerFaction.isPresent())
            {
                Faction faction = optionalPlayerFaction.get();

                if (faction.getLeader().equals(player.getUniqueId()) || EagleFactionsPlugin.ADMIN_MODE_PLAYERS.contains(player.getUniqueId()))
                {
                    showFlags(player, faction);
                }
                else
                {
                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_THE_FACTIONS_LEADER_TO_DO_THIS));
                }
            }
            else
            {
                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
            }
        }
        else
        {
            source.sendMessage (Text.of (PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
        }

        return CommandResult.success();
    }

    private void showFlags(final Player player, final Faction faction)
    {
        final Text.Builder textBuilder = Text.builder();
        for (final Map.Entry<FactionMemberType, Map<FactionFlagTypes, Boolean>> memberEntry : faction.getFlags().entrySet())
        {
            final Map<FactionFlagTypes, Boolean> memberFlags = memberEntry.getValue();
            textBuilder.append(Text.of(TextColors.AQUA, memberEntry.getKey().toString() + ": "));

            for (final Map.Entry<FactionFlagTypes, Boolean> flagEntry : memberFlags.entrySet())
            {
                final Text.Builder flagTextBuilder = Text.builder();

                if(flagEntry.getValue())
                {
                    flagTextBuilder.append(Text.of(TextColors.GREEN, flagEntry.getKey().toString()));
                }
                else
                {
                    flagTextBuilder.append(Text.of(TextColors.RED, flagEntry.getKey().toString()));
                }

                flagTextBuilder.onClick(TextActions.executeCallback(toggleFlag(faction, memberEntry.getKey(), flagEntry.getKey(), !flagEntry.getValue())));
                flagTextBuilder.onHover(TextActions.showText(Text.of(PluginMessages.SET_TO + " " + String.valueOf(!flagEntry.getValue()).toUpperCase())));

                textBuilder.append(flagTextBuilder.build());
                textBuilder.append(Text.of(" | "));
            }

            textBuilder.append(Text.of("\n"));
        }

        //player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.PERMISSIONS_FLAGS_FOR + " " + faction.NAME + ":"));
        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.CLICK_ON_THE_PERMISSION_YOU_WANT_TO_CHANGE));
        player.sendMessage(Text.of(TextColors.RED, "RED", TextColors.RESET, " = " + PluginMessages.HAS_NOT_PERMISSIONS_FOR));
        player.sendMessage(Text.of(TextColors.GREEN, "GREEN", TextColors.RESET, " = " + PluginMessages.HAS_PERMISSIONS_FOR));
        player.sendMessage(Text.of("=============================="));
        player.sendMessage(textBuilder.build());
    }

    private Consumer<CommandSource> toggleFlag(final Faction faction, final FactionMemberType factionMemberType, final FactionFlagTypes factionFlagTypes, final Boolean flagValue)
    {
        return commandSource ->
        {
            getPlugin().getFactionLogic().toggleFlag(faction, factionMemberType, factionFlagTypes, flagValue);
            showFlags((Player)commandSource, faction);
        };
    }
}
