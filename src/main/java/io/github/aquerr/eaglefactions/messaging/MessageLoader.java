package io.github.aquerr.eaglefactions.messaging;

import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.plugin.PluginContainer;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static net.kyori.adventure.text.Component.text;

@Singleton
public class MessageLoader
{
    private static final String LANG_DIR_NAME = "lang";

    private final PluginContainer pluginContainer;
    private final EagleFactions plugin;
    private final FactionsConfig factionsConfig;

    private static MessageLoader instance = null;

    public static MessageLoader getInstance()
    {
        return Optional.ofNullable(instance)
                .orElseThrow(() -> new RuntimeException("MessageLoader not yet initialized!"));
    }

    public static void init(EagleFactions eagleFactions, PluginContainer pluginContainer)
    {
        if (instance == null)
            instance = new MessageLoader(eagleFactions, pluginContainer);
    }

    private MessageLoader(final EagleFactions plugin, final PluginContainer pluginContainer)
    {
        this.plugin = plugin;
        this.pluginContainer = pluginContainer;
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        Path configDir = plugin.getConfigDir();
        String langFileName = this.factionsConfig.getLanguageFileName();
        Path langFilePath = configDir.resolve(LANG_DIR_NAME).resolve(langFileName);

        if (Files.notExists(configDir.resolve(LANG_DIR_NAME)))
        {
            try
            {
                Files.createDirectory(configDir.resolve(LANG_DIR_NAME));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        try
        {
            Optional<InputStream> optionalLangFile = pluginContainer.openResource(URI.create(LANG_DIR_NAME + "/" + langFileName));
            if (optionalLangFile.isPresent())
            {
                Files.copy(optionalLangFile.get(), langFilePath);
            }
            else
            {
                optionalLangFile = pluginContainer.openResource(URI.create(LANG_DIR_NAME + "/english.conf"));
                optionalLangFile.ifPresent(x->
                {
                    try
                    {
                        Files.copy(x, langFilePath);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                });
            }
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }

        final ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().path(langFilePath).build();
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
                    configNode.node(messageField.getName()).set(messageField.get(null));
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

//    public static String parseMessage(String message, Object supplier)
//    {
//        String result = message;
//        if (supplier instanceof Faction)
//        {
//            Faction faction = (Faction) supplier;
//            result = result.replace(Placeholders.FACTION_NAME.getPlaceholder(), faction.getName());
//        }
//        else if (supplier instanceof User)
//        {
//            User user = (User) supplier;
//            result = result.replace(Placeholders.PLAYER_NAME.getPlaceholder(), user.getName());
//            result = result.replace(Placeholders.POWER.getPlaceholder(), String.valueOf(EagleFactionsPlugin.getPlugin().getPowerManager().getPlayerPower(user.getUniqueId())));
//        }
//        else if (supplier instanceof String)
//        {
//            for (final Placeholder placeholder : Placeholders.PLACEHOLDERS)
//            {
//                result = result.replace(placeholder.getPlaceholder(), (String) supplier);
//            }
//        }
//        else if (supplier instanceof Integer)
//        {
//            result = result.replace(Placeholders.NUMBER.getPlaceholder(), String.valueOf(supplier));
//        }
//        return result;
//    }

    public static TextComponent parseMessage(final String message, final NamedTextColor messageBaseColor, final Map<Placeholder, TextComponent> placeholdersMap)
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

//    public static TextTemplate toTextTemplate(final String message)
//    {
//        final String[] splitMessage = message.split(" ");
//        final List<Object> newWords = new ArrayList<>();
//        for (final String word : splitMessage)
//        {
//            boolean didReplace = false;
//            for (final Placeholder placeholder : Placeholders.PLACEHOLDERS)
//            {
//                if (word.contains(placeholder.getPlaceholder()))
//                {
//                    newWords.add(TextTemplate.arg(word.replace("%", "")).color(TextColors.GOLD).build());
//                    didReplace = true;
//                    break;
//                }
//            }
//
//            if (didReplace)
//                continue;
//
//            newWords.add(word);
//            newWords.add(" ");
//        }
//        return TextTemplate.of(newWords);
//    }
}
