package io.github.aquerr.eaglefactions.messaging;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.messaging.locale.Localization;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.command.exception.CommandException;

import java.nio.file.Paths;

/**
 * Eagle Factions Message Service that create messages loaded from language files.
 */
public class EFMessageService implements MessageService
{
    private final EagleFactions plugin;
    private final Localization localization;

    public EFMessageService(FactionsConfig factionsConfig)
    {
        this.localization = Localization.forTag(factionsConfig.getLanguageFileName());
    }

    @Override
    public Component resolveMessageWithPrefix(String messageKey)
    {
        return LinearComponents.linear(PluginInfo.PLUGIN_PREFIX, resolveComponentWithMessage(messageKey));
    }

    @Override
    public CommandException resolveExceptionWithMessage(String messageKey)
    {
        return new CommandException(LinearComponents.linear(PluginInfo.ERROR_PREFIX, resolveComponentWithMessage(messageKey)));
    }

    @Override
    public Component resolveComponentWithMessage(String messageKey)
    {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(resolveMessage(messageKey));
    }

    @Override
    public String resolveMessage(String messageKey)
    {
        return null;
    }
}
