package io.github.aquerr.eaglefactions.commands.management;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.api.entities.FactionPermType;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

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
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final ServerPlayer player = requirePlayerSource(context);
        final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());

        if(!optionalPlayerFaction.isPresent())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND, NamedTextColor.RED)));

        final Faction faction = optionalPlayerFaction.get();
        if(!faction.getLeader().equals(player.uniqueId()) && !super.getPlugin().getPlayerManager().hasAdminMode(player))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_TO_DO_THIS, NamedTextColor.RED)));

        showPerms(player, faction);
        return CommandResult.success();
    }

    private void showPerms(final Player player, final Faction faction)
    {
        final TextComponent textBuilder = Component.empty();
        for (final Map.Entry<FactionMemberType, Map<FactionPermType, Boolean>> memberEntry : faction.getPerms().entrySet())
        {
            final Map<FactionPermType, Boolean> memberPerms = memberEntry.getValue();
            textBuilder.append(Component.text(memberEntry.getKey().toString() + ": ", NamedTextColor.AQUA));

            for (final Map.Entry<FactionPermType, Boolean> permEntry : memberPerms.entrySet())
            {
                final TextComponent permTextBuilder = Component.empty();

                if(permEntry.getValue())
                {
                    permTextBuilder.append(Component.text(permEntry.getKey().toString(), NamedTextColor.GREEN));
                }
                else
                {
                    permTextBuilder.append(Component.text(permEntry.getKey().toString(), NamedTextColor.RED));
                }

                permTextBuilder.clickEvent(SpongeComponents.executeCallback(togglePerm(faction, memberEntry.getKey(), permEntry.getKey(), !permEntry.getValue())));
                permTextBuilder.hoverEvent(Component.text(Messages.SET_TO + " " + String.valueOf(!permEntry.getValue()).toUpperCase()));

                textBuilder.append(permTextBuilder);
                textBuilder.append(Component.text(" | "));
            }

            textBuilder.append(Component.newline());
        }

        player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.CLICK_ON_THE_PERMISSION_YOU_WANT_TO_CHANGE)));
        player.sendMessage(Component.text("RED", NamedTextColor.RED).append(Component.text(" = " + Messages.HAS_NOT_PERMISSIONS_FOR)));
        player.sendMessage(Component.text("GREEN", NamedTextColor.GREEN).append(Component.text(" = " + Messages.HAS_PERMISSIONS_FOR)));
        player.sendMessage(Component.text("=============================="));
        player.sendMessage(textBuilder);
    }

    private Consumer<CommandCause> togglePerm(final Faction faction, final FactionMemberType factionMemberType, final FactionPermType factionPermType, final Boolean flagValue)
    {
        return commandSource ->
        {
            getPlugin().getFactionLogic().togglePerm(faction, factionMemberType, factionPermType, flagValue);
            showPerms((Player)commandSource, faction);
        };
    }
}
