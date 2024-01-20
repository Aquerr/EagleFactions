//package io.github.aquerr.eaglefactions.commands.management;
//
//import io.github.aquerr.eaglefactions.api.EagleFactions;
//import io.github.aquerr.eaglefactions.api.entities.Faction;
//import io.github.aquerr.eaglefactions.api.entities.FactionPermission;
//import io.github.aquerr.eaglefactions.api.messaging.MessageService;
//import io.github.aquerr.eaglefactions.commands.AbstractCommand;
//import net.kyori.adventure.audience.Audience;
//import net.kyori.adventure.text.Component;
//import net.kyori.adventure.text.TextComponent;
//import net.kyori.adventure.text.event.HoverEvent;
//import org.spongepowered.api.adventure.SpongeComponents;
//import org.spongepowered.api.command.CommandCause;
//import org.spongepowered.api.command.CommandResult;
//import org.spongepowered.api.command.exception.CommandException;
//import org.spongepowered.api.command.parameter.CommandContext;
//import org.spongepowered.api.entity.living.player.server.ServerPlayer;
//
//import java.util.Map;
//import java.util.function.Consumer;
//
//import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
//import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
//import static net.kyori.adventure.text.format.NamedTextColor.RED;
//
//public class PermsCommand extends AbstractCommand
//{
//    private final MessageService messageService;
//
//    public PermsCommand(final EagleFactions plugin)
//    {
//        super(plugin);
//        this.messageService = plugin.getMessageService();
//    }
//
//    @Override
//    public CommandResult execute(final CommandContext context) throws CommandException
//    {
//        final ServerPlayer player = requirePlayerSource(context);
//        final Faction faction = requirePlayerFaction(player);
//        if(!faction.getLeader().equals(player.uniqueId()) && !super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
//            throw messageService.resolveExceptionWithMessage("error.command.perms.leader-required");
//
//        showPerms(player, faction);
//        return CommandResult.success();
//    }
//
//    private void showPerms(final Audience audience, final Faction faction)
//    {
//        final TextComponent.Builder textBuilder = Component.text();
//        for (final Map.Entry<FactionMemberType, Map<FactionPermission, Boolean>> memberEntry : faction.getPerms().entrySet())
//        {
//            final Map<FactionPermission, Boolean> memberPerms = memberEntry.getValue();
//            textBuilder.append(Component.text(memberEntry.getKey().toString() + ": ", AQUA));
//
//            for (final Map.Entry<FactionPermission, Boolean> permEntry : memberPerms.entrySet())
//            {
//                final TextComponent.Builder permTextBuilder = Component.text();
//
//                if(permEntry.getValue())
//                {
//                    permTextBuilder.append(Component.text(permEntry.getKey().toString(), GREEN));
//                }
//                else
//                {
//                    permTextBuilder.append(Component.text(permEntry.getKey().toString(), RED));
//                }
//
//                permTextBuilder.clickEvent(SpongeComponents.executeCallback((commandCause) -> togglePerm(faction, memberEntry.getKey(), permEntry.getKey(), !permEntry.getValue())));
//                permTextBuilder.hoverEvent(HoverEvent.showText(messageService.resolveComponentWithMessage("command.perms.set-to", String.valueOf(!permEntry.getValue()).toUpperCase())));
//
//                textBuilder.append(permTextBuilder.build());
//                textBuilder.append(Component.text(" | "));
//            }
//
//            textBuilder.append(Component.newline());
//        }
//
//        audience.sendMessage(messageService.resolveMessageWithPrefix("command.perms.click-permission-you-want-to-change"));
//        audience.sendMessage(messageService.resolveComponentWithMessage("command.perms.red-has-not-permission"));
//        audience.sendMessage(messageService.resolveComponentWithMessage("command.perms.green-has-permission"));
//        audience.sendMessage(Component.text("=============================="));
//        audience.sendMessage(textBuilder.build());
//    }
//
//    private Consumer<CommandCause> togglePerm(final Faction faction, final FactionMemberType factionMemberType, final FactionPermission factionPermission, final Boolean flagValue)
//    {
//        return commandSource ->
//        {
//            getPlugin().getFactionLogic().togglePerm(faction, factionMemberType, factionPermission, flagValue);
//            showPerms(commandSource.audience(), faction);
//        };
//    }
//}
