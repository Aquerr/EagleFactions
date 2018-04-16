package io.github.aquerr.eaglefactions.logic;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;

public class MessageLoader
{
    private Path messagesFilePath;

//    private String ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND;
//    private String YOU_NEED_TO_BE_LEADER;

    public MessageLoader(Path configDir)
    {
        //TODO: Consider having language option in main config file.
        //String pluginLanguage = MainLogic.getPluginLanguage();

        messagesFilePath = configDir.resolve("messages").resolve("config.conf");

        ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(messagesFilePath).build();
        ConfigurationNode configNode;

        try
        {
            configNode = configLoader.load();

            String languageFileName = configNode.getNode("language-file").getString();
            messagesFilePath = configDir.resolve("messages").resolve(languageFileName);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        configLoader = HoconConfigurationLoader.builder().setPath(messagesFilePath).build();

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
        PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND = configNode.getNode("only-in-game-players-can-use-this-command").getString();
        PluginMessages.YOU_MUST_BE_THE_FACTIONS_LEADER_TO_DO_THIS = configNode.getNode("you-must-be-the-factions-leader-to-do-this").getString();
        PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND = configNode.getNode("you-must-be-in-faction-in-order-to-do-this").getString();
        PluginMessages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS = configNode.getNode("you-must-be-the-factions-leader-or-officer-to-do-this").getString();
        PluginMessages.YOU_ARE_IN_WAR_WITH_THIS_FACTION = configNode.getNode("you-are-in-war-with-this-faction").getString();
        PluginMessages.SEND_THIS_FACTION_A_PEACE_REQUEST_FIRST_BEFORE_INVITING_THEM_TO_ALLIES = configNode.getNode("send-this-faction-a-peace-request-first-before-inviting-them-to-allies").getString();
        PluginMessages.YOU_ARE_IN_ALLIANCE_WITH_THIS_FACTION = configNode.getNode("you-are-in-alliance-with-this-faction").getString();
    }
}
