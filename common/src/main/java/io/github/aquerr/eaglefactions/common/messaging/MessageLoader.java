package io.github.aquerr.eaglefactions.common.messaging;

import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

@Singleton
public class MessageLoader
{
    private final EagleFactions plugin;
    private final FactionsConfig factionsConfig;

    private static MessageLoader instance = null;

    public static MessageLoader getInstance(EagleFactions plugin)
    {
        if (instance == null)
            return new MessageLoader(plugin);
        return instance;
    }

    private MessageLoader(final EagleFactions plugin)
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
                Files.createDirectory(configDir.resolve("messages"));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        Optional<Asset> optionalMessagesFile = Sponge.getAssetManager().getAsset(plugin, "messages/" + messagesFileName);
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
            optionalMessagesFile = Sponge.getAssetManager().getAsset(plugin, "messages/english.conf");
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

        final ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(messagesFilePath).build();
        ConfigurationNode configNode;

        try
        {
            configNode = configLoader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
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
            Object object = configNode.getNode(messageField.getName()).getString("MISSING_MESSAGE");

            if (object.equals("MISSING_MESSAGE"))
            {
                missingNodes = true;
            }

            String message = object.toString();

//            if (messageField.getName().equals("YOU_OPENED_FACTION_CHEST"))
//            {
//                try
//                {
//                    messageField.set(Messages.class.getClass(), toTextTemplate(message));
//                }
//                catch (IllegalAccessException e)
//                {
//                    e.printStackTrace();
//                }
//                continue;
//            }

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

    public static Text parseMessage(final String message, final TextColor messageBaseColor, final Map<Placeholder, Text> placeholdersMap)
    {
        final Text.Builder resultText = Text.builder();
        final String[] splitMessage = message.split(" ");
        for (final String word : splitMessage)
        {
            boolean didFill = false;
            for (final Map.Entry<Placeholder, Text> mapEntry : placeholdersMap.entrySet())
            {
                if (word.contains(mapEntry.getKey().getPlaceholder()))
                {
                    final String placeholderReplacement = TextSerializers.FORMATTING_CODE.serialize(mapEntry.getValue().toBuilder().append(Text.of(messageBaseColor)).build());
                    final String filledPlaceholder = word.replace(mapEntry.getKey().getPlaceholder(), placeholderReplacement);
                    final Text formattedText = TextSerializers.FORMATTING_CODE.deserialize(filledPlaceholder + " ");
                    resultText.append(formattedText);
                    didFill = true;

//                    final String filledPlaceholder = word.replace(mapEntry.getKey().getPlaceholder(), mapEntry.getValue().toPlainSingle());
//                    resultText.append(Text.builder().append(Text.of(filledPlaceholder)).build());
//                    didFill = true;
                    break;
                }
            }

            if (didFill)
                continue;

            resultText.append(Text.of(messageBaseColor, word + " "));
        }
        return resultText.build();
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
