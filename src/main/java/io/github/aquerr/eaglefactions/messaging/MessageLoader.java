package io.github.aquerr.eaglefactions.messaging;

import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.plugin.PluginContainer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static net.kyori.adventure.text.Component.text;

@Singleton
public class MessageLoader
{
    private final EagleFactions plugin;
    private final FactionsConfig factionsConfig;

    private static MessageLoader instance = null;

    public static void init(EagleFactions plugin, PluginContainer pluginContainer)
    {
        if (instance == null)
            instance = new MessageLoader(plugin, pluginContainer);
    }

    private MessageLoader(final EagleFactions plugin, final PluginContainer pluginContainer)
    {
        instance = this;
        this.plugin = plugin;
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        Path configDir = plugin.getConfigDir();
        String messagesFileName = this.factionsConfig.getLanguageFileName();
        Path messagesFilePath = configDir.resolve("messages").resolve(messagesFileName);

        if (Files.notExists(configDir.resolve("messages")))
        {
            try
            {
                Files.createDirectories(configDir.resolve("messages"));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        Optional<Asset> optionalMessagesFile = Sponge.assetManager().asset(pluginContainer, "messages/" + messagesFileName);
        if (optionalMessagesFile.isPresent())
        {
            try
            {
                optionalMessagesFile.get().copyToFile(messagesFilePath, false, true);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            optionalMessagesFile = Sponge.assetManager().asset(pluginContainer, "messages/english.conf");
            optionalMessagesFile.ifPresent(x->
            {
                try
                {
                    x.copyToFile(messagesFilePath, false, true);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            });
        }

        final ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().path(messagesFilePath).build();
        ConfigurationNode configNode;

        try
        {
            configNode = configLoader.load(ConfigurationOptions.defaults().shouldCopyDefaults(true));
            loadPluginMessages(configNode, configLoader);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void loadPluginMessages(ConfigurationNode configNode, ConfigurationLoader<CommentedConfigurationNode> configLoader)
    {
        final Field[] messageFields = Messages.class.getFields();
        boolean missingNodes = false;

        for (final Field messageField : messageFields)
        {
            String message = configNode.node(messageField.getName()).getString();

            if (message == null || message.equals(""))
            {
                missingNodes = true;
                try
                {
                    configNode.node(messageField.getName()).set(String.class, messageField.get(null));
                }
                catch (IllegalAccessException | SerializationException e)
                {
                    e.printStackTrace();
                }
                continue;
            }

            try
            {
                messageField.set(Messages.class.getClass(), message);
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }

        if (missingNodes)
        {
            try
            {
                configLoader.save(configNode);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static TextComponent parseMessage(final String message, final TextColor messageBaseColor, final Map<Placeholder, TextComponent> placeholdersMap)
    {
        TextComponent resultText = Component.empty();
        final String[] splitMessage = message.split(" ");
        for (final String word : splitMessage)
        {
            boolean didFill = false;
            for (final Map.Entry<Placeholder, TextComponent> mapEntry : placeholdersMap.entrySet())
            {
                if (word.contains(mapEntry.getKey().getPlaceholder()))
                {
                    final String placeholderReplacement = LegacyComponentSerializer.legacyAmpersand().serialize(mapEntry.getValue().color(messageBaseColor));
                    final String filledPlaceholder = word.replace(mapEntry.getKey().getPlaceholder(), placeholderReplacement);
                    final TextComponent formattedText = LegacyComponentSerializer.legacyAmpersand().deserialize(filledPlaceholder + " ");
                    resultText = resultText.append(formattedText);
                    didFill = true;
                    break;
                }
            }

            if (didFill)
                continue;

            resultText = resultText.append(text(word + " ", messageBaseColor));
        }
        return resultText;
    }
}
