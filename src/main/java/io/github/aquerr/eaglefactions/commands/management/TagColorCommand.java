package io.github.aquerr.eaglefactions.commands.management;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPermission;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Optional;

public class TagColorCommand extends AbstractCommand
{
    private final MessageService messageService;
    private final PermsManager permsManager;

    public TagColorCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.messageService = plugin.getMessageService();
        this.permsManager = plugin.getPermsManager();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        if (!getPlugin().getConfiguration().getChatConfig().canColorTags())
            throw messageService.resolveExceptionWithMessage("error.command.tag-color.disabled");

        final ServerPlayer player = requirePlayerSource(context);
        final NamedTextColor color = NamedTextColor.namedColor(context.requireOne(Parameter.color().key("color").build()).rgb());
        final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
        if (!optionalPlayerFaction.isPresent())
            throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND_MESSAGE_KEY);

        Faction playerFaction = optionalPlayerFaction.get();
        if (!permsManager.hasPermission(player.uniqueId(), playerFaction, FactionPermission.MANAGE_TAG_COLOR)
                && !super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
            throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS);

        final boolean isCancelled = EventRunner.runFactionTagColorUpdateEventPre(player, playerFaction, Optional.ofNullable(playerFaction.getTag().color()).map(NamedTextColor::nearestTo).orElse(NamedTextColor.WHITE), color);
        if (!isCancelled)
        {
            super.getPlugin().getFactionLogic().changeTagColor(playerFaction, color);
            player.sendMessage(messageService.resolveMessageWithPrefix("command.tag-color.success"));
            EventRunner.runFactionTagColorUpdateEventPost(player, playerFaction, Optional.ofNullable(playerFaction.getTag().color()).map(NamedTextColor::nearestTo).orElse(NamedTextColor.WHITE), color);
        }
        return CommandResult.success();
    }
}
