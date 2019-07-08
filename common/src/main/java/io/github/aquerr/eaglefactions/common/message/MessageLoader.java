package io.github.aquerr.eaglefactions.common.message;

import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

@Singleton
public class MessageLoader
{
    private static MessageLoader instance = null;

    public static MessageLoader getInstance(EagleFactions eagleFactions)
    {
        if (instance == null)
            return new MessageLoader(eagleFactions);
        return instance;
    }

    private MessageLoader(EagleFactions eagleFactions)
    {
        instance = this;
        Path configDir = eagleFactions.getConfigDir();
        String messagesFileName = eagleFactions.getConfiguration().getConfigFields().getLanguageFileName();
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

        if (!Files.exists(messagesFilePath))
        {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("messages/" + messagesFileName);

            //If there is not such language...
            if(inputStream == null)
                inputStream = getClass().getClassLoader().getResourceAsStream("messages/english.conf");
            
            try
            {
                Files.copy(inputStream, messagesFilePath);
                inputStream.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
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
