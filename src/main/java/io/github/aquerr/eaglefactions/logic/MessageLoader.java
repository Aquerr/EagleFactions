package io.github.aquerr.eaglefactions.logic;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

public class MessageLoader
{
    private Path messagesFilePath;

//    private String ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND;
//    private String YOU_NEED_TO_BE_LEADER;

    public MessageLoader(Path configDir)
    {
        //TODO: Consider having language option in main config file.
        String messagesFileName = MainLogic.getLanguageFileName();
        messagesFilePath = configDir.resolve("messages").resolve(messagesFileName);

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
            try
            {
                Files.copy(inputStream, messagesFilePath);
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
            configNode = configLoader.load();
            loadPluginMessages(configNode);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void loadPluginMessages(ConfigurationNode configNode)
    {
        Field[] messageFields = PluginMessages.class.getFields();

        for (Field messageField : messageFields)
        {
            String message = configNode.getNode(messageField.getName()).getString();

            try
            {
                messageField.set(PluginMessages.class.getClass(), message);
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }
    }
}
