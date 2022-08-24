package io.github.aquerr.eaglefactions.messaging;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.messaging.locale.Localization;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.command.exception.CommandException;

import java.text.MessageFormat;

/**
 * Eagle Factions Message Service that create messages loaded from language files.
 */
public class EFMessageService implements MessageService
{
    public static final String ERROR_YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND_MESSAGE_KEY = "error.command.must-be-in-faction-to-use-this-command";
    public static final String ERROR_ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND = "error.command.in-game-player-required";
    public static final String ERROR_YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS = "error.access.you-must-be-faction-leader-or-officer-to-do-this";
    public static final String ERROR_ADMIN_MODE_REQUIRED = "error.command.admin-mode-required";
    public static final String ERROR_THIS_PLACE_IS_ALREADY_CLAIMED = "error.claim.place-is-already-claimed";
    public static final String ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS = "error.general.you-dont-have-access-to-do-this";

    private final Localization localization;

    private static class InstanceHolder {
        public static EFMessageService INSTANCE = null;
    }

    public static void init(String langTag) {
        InstanceHolder.INSTANCE = new EFMessageService(langTag);
    }

    public static EFMessageService getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private EFMessageService(String langTag) {
        this.localization = Localization.forTag(langTag);
    }

    @Override
    public Component resolveMessageWithPrefix(String messageKey)
    {
        return resolveMessageWithPrefix(messageKey, new Object[0]);
    }

    @Override
    public Component resolveMessageWithPrefix(String messageKey, Object... args)
    {
        return LinearComponents.linear(PluginInfo.PLUGIN_PREFIX, resolveComponentWithMessage(messageKey, args));
    }

    @Override
    public CommandException resolveExceptionWithMessage(String messageKey)
    {
        return resolveExceptionWithMessage(messageKey, new Object[0]);
    }

    @Override
    public CommandException resolveExceptionWithMessage(String messageKey, Object... args)
    {
        return new CommandException(LinearComponents.linear(PluginInfo.ERROR_PREFIX, resolveComponentWithMessage(messageKey, args)));
    }

    @Override
    public CommandException resolveExceptionWithMessageAndThrowable(String messageKey, Throwable throwable)
    {
        return new CommandException(LinearComponents.linear(PluginInfo.ERROR_PREFIX, resolveComponentWithMessage(messageKey)), throwable);
    }

    @Override
    public TextComponent resolveComponentWithMessage(String messageKey)
    {
        return resolveComponentWithMessage(messageKey, new Object[0]);
    }

    @Override
    public TextComponent resolveComponentWithMessage(String messageKey, Object... args)
    {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(resolveMessage(messageKey, args));
    }

    @Override
    public String resolveMessage(String messageKey)
    {
        return this.resolveMessage(messageKey, new Object[0]);
    }

    @Override
    public String resolveMessage(String messageKey, Object... args)
    {
        return MessageFormat.format(this.localization.getMessage(messageKey), args);
    }
}
