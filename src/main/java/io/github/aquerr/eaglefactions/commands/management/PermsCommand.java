package io.github.aquerr.eaglefactions.commands.management;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.api.entities.FactionPermType;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.Messages;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Map;
import java.util.function.Consumer;

import static net.kyori.adventure.text.format.NamedTextColor.*;

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
        final Faction faction = requirePlayerFaction(player);
        if(!faction.getLeader().equals(player.uniqueId()) && !super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_TO_DO_THIS, RED)));

        showPerms(player, faction);
        return CommandResult.success();
    }

    private void showPerms(final Audience audience, final Faction faction)
    {
        final TextComponent.Builder textBuilder = Component.text();
        for (final Map.Entry<FactionMemberType, Map<FactionPermType, Boolean>> memberEntry : faction.getPerms().entrySet())
        {
            final Map<FactionPermType, Boolean> memberPerms = memberEntry.getValue();
            textBuilder.append(Component.text(memberEntry.getKey().toString() + ": ", AQUA));

            for (final Map.Entry<FactionPermType, Boolean> permEntry : memberPerms.entrySet())
            {
                final TextComponent.Builder permTextBuilder = Component.text();

                if(permEntry.getValue())
                {
                    permTextBuilder.append(Component.text(permEntry.getKey().toString(), GREEN));
                }
                else
                {
                    permTextBuilder.append(Component.text(permEntry.getKey().toString(), RED));
                }

                permTextBuilder.clickEvent(SpongeComponents.executeCallback((commandCause) -> togglePerm(faction, memberEntry.getKey(), permEntry.getKey(), !permEntry.getValue())));
                permTextBuilder.hoverEvent(HoverEvent.showText(Component.text(Messages.SET_TO + " " + String.valueOf(!permEntry.getValue()).toUpperCase())));

                textBuilder.append(permTextBuilder.build());
                textBuilder.append(Component.text(" | "));
            }

            textBuilder.append(Component.newline());
        }

        audience.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.CLICK_ON_THE_PERMISSION_YOU_WANT_TO_CHANGE)));
        audience.sendMessage(Component.text("RED", RED).append(Component.text(" = " + Messages.HAS_NOT_PERMISSIONS_FOR, WHITE)));
        audience.sendMessage(Component.text("GREEN", GREEN).append(Component.text(" = " + Messages.HAS_PERMISSIONS_FOR, WHITE)));
        audience.sendMessage(Component.text("=============================="));
        audience.sendMessage(textBuilder.build());
    }

    private Consumer<CommandCause> togglePerm(final Faction faction, final FactionMemberType factionMemberType, final FactionPermType factionPermType, final Boolean flagValue)
    {
        return commandSource ->
        {
            getPlugin().getFactionLogic().togglePerm(faction, factionMemberType, factionPermType, flagValue);
            showPerms(commandSource.audience(), faction);
        };
    }
}
