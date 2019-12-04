package io.github.aquerr.eaglefactions.common.message;

import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Singleton
public class MessageLoader
{
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
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        Path configDir = plugin.getConfigDir();
        String messagesFileName = this.factionsConfig.getLanguageFileName();
        Path messagesFilePath = configDir.resolve("messages").resolve(messagesFileName);

        if (!Files.exists(configDir.resolve("messages")))
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

        Optional<Asset> optionalMessagesFile = Sponge.getAssetManager().getAsset(plugin, "messages" + File.separator + messagesFileName);
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
            optionalMessagesFile = Sponge.getAssetManager().getAsset(plugin, "messages" + File.separator + "english.conf");
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

        ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(messagesFilePath).build();
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

    private void loadPluginMessages(ConfigurationNode configNode, ConfigurationLoader configLoader)
    {
        Field[] messageFields = PluginMessages.class.getFields();
        boolean missingNodes = false;

        for (Field messageField : messageFields)
        {
            Object object = configNode.getNode(messageField.getName()).getString("MISSING_MESSAGE");

            if (object.equals("MISSING_MESSAGE"))
            {
                missingNodes = true;
            }

            String message = object.toString();

            try
            {
                messageField.set(PluginMessages.class.getClass(), message);
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
}
