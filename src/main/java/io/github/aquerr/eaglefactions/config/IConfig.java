package io.github.aquerr.eaglefactions.config;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;

/**
 * Created by Aquerr on 2017-07-12.
 */
public interface IConfig
{
    void setup();

    void load();

    void save();

    void populate();

    CommentedConfigurationNode get();
}
