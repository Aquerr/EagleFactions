package io.github.aquerr.eaglefactions.messaging;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.messaging.locale.Localization;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.command.exception.CommandException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.PropertyResourceBundle;

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

    private static final String LANG_FILE_BASE_PATH = "assets/eaglefactions/lang/messages";
    private static final String DEFAULT_LANG_FILE_PATH = "assets/eaglefactions/lang/messages.properties";

    private final Localization localization;

    private static class InstanceHolder {

        public static EFMessageService INSTANCE = null;
    }

    public static void init(String langTag) {
        Path langDir = EagleFactionsPlugin.getPlugin().getConfigDir().resolve("lang");
        String jarLangFilePath = DEFAULT_LANG_FILE_PATH;
        if (!langTag.equals("en_US"))
        {
            jarLangFilePath = String.format(LANG_FILE_BASE_PATH + "_%s.properties", langTag);
        }

        Path fileLangFilePath = langDir.resolve(String.format("messages_%s.properties", langTag));

        try
        {
            Files.createDirectories(langDir);
            generateLangFile(jarLangFilePath, fileLangFilePath);
        }
        catch (IOException e)
        {
            try
            {
                jarLangFilePath = DEFAULT_LANG_FILE_PATH;
                fileLangFilePath = fileLangFilePath.resolveSibling("messages.properties");
                generateLangFile(jarLangFilePath, fileLangFilePath);
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
                throw new IllegalStateException("Could not generate language file!");
            }
            e.printStackTrace();
        }

        PropertyResourceBundle propertyResourceBundle;

        try
        {
            propertyResourceBundle = new PropertyResourceBundle(Files.newInputStream(fileLangFilePath));
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }

        InstanceHolder.INSTANCE = new EFMessageService(propertyResourceBundle);
    }

    private static void generateLangFile(String jarLangFilePath, Path fileLangFilePath) throws IOException
    {
        InputStream langFilePathStream = EagleFactionsPlugin.getPlugin().getResource(jarLangFilePath).toURL().openStream();
        Files.copy(langFilePathStream, fileLangFilePath, StandardCopyOption.REPLACE_EXISTING);
    }

    public static EFMessageService getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private EFMessageService(PropertyResourceBundle resourceBundle) {
        this.localization = Localization.forResourceBundle(resourceBundle);
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
        args = Arrays.stream(args)
                .map(arg -> {
                    if (arg instanceof Component)
                    {
                        return LegacyComponentSerializer.legacyAmpersand().serialize((Component) arg);
                    }
                    return arg;
                }).toArray();
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
