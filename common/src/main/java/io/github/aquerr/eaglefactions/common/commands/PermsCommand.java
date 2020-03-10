package io.github.aquerr.eaglefactions.common.commands;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPermType;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
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

public class PermsCommand extends AbstractCommand
{
    public PermsCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        if(!(source instanceof Player))
            throw new CommandException(Text.of (PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        final Player player = (Player)source;
        final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());

        if(!optionalPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        final Faction faction = optionalPlayerFaction.get();
        if(!faction.getLeader().equals(player.getUniqueId()) && !super.getPlugin().getPlayerManager().hasAdminMode(player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_TO_DO_THIS));

        showPerms(player, faction);
        return CommandResult.success();
    }

    private void showPerms(final Player player, final Faction faction)
    {
        final Text.Builder textBuilder = Text.builder();
        for (final Map.Entry<FactionMemberType, Map<FactionPermType, Boolean>> memberEntry : faction.getPerms().entrySet())
        {
            final Map<FactionPermType, Boolean> memberPerms = memberEntry.getValue();
            textBuilder.append(Text.of(TextColors.AQUA, memberEntry.getKey().toString() + ": "));

            for (final Map.Entry<FactionPermType, Boolean> permEntry : memberPerms.entrySet())
            {
                final Text.Builder permTextBuilder = Text.builder();

                if(permEntry.getValue())
                {
                    permTextBuilder.append(Text.of(TextColors.GREEN, permEntry.getKey().toString()));
                }
                else
                {
                    permTextBuilder.append(Text.of(TextColors.RED, permEntry.getKey().toString()));
                }

                permTextBuilder.onClick(TextActions.executeCallback(togglePerm(faction, memberEntry.getKey(), permEntry.getKey(), !permEntry.getValue())));
                permTextBuilder.onHover(TextActions.showText(Text.of(Messages.SET_TO + " " + String.valueOf(!permEntry.getValue()).toUpperCase())));

                textBuilder.append(permTextBuilder.build());
                textBuilder.append(Text.of(" | "));
            }

            textBuilder.append(Text.of("\n"));
        }

        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, Messages.CLICK_ON_THE_PERMISSION_YOU_WANT_TO_CHANGE));
        player.sendMessage(Text.of(TextColors.RED, "RED", TextColors.RESET, " = " + Messages.HAS_NOT_PERMISSIONS_FOR));
        player.sendMessage(Text.of(TextColors.GREEN, "GREEN", TextColors.RESET, " = " + Messages.HAS_PERMISSIONS_FOR));
        player.sendMessage(Text.of("=============================="));
        player.sendMessage(textBuilder.build());
    }

    private Consumer<CommandSource> togglePerm(final Faction faction, final FactionMemberType factionMemberType, final FactionPermType factionPermType, final Boolean flagValue)
    {
        return commandSource ->
        {
            getPlugin().getFactionLogic().toggleFlag(faction, factionMemberType, factionPermType, flagValue);
            showPerms((Player)commandSource, faction);
        };
    }
}
