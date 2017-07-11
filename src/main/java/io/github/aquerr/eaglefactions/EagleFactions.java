package io.github.aquerr.eaglefactions;


import io.github.aquerr.eaglefactions.commands.HelpCommand;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import javax.inject.Inject;
import java.util.logging.Logger;

@Plugin (id = PluginInfo.Id, name = PluginInfo.Name, version = PluginInfo.Version, description = PluginInfo.Description)
public class EagleFactions
{
    private static EagleFactions eagleFactions;


    protected EagleFactions()
    {

    }

    @Inject
    private Logger logger;
    public Logger getLogger()
    {
        return logger;
    }

    @Listener
    public void onServerInitialization(GameInitializationEvent event){
        getLogger ().info("EagleFactions is loading...");
        getLogger ().info ("Preparing wings...");

        eagleFactions = this;

        InitializeCommands();


    }

    private void InitializeCommands()
    {
        getLogger ().info ("Inicjowanie komend...");

        CommandSpec commandhelp = CommandSpec.builder ()
                .description (Text.of ("Pomoc"))
                .permission ("eaglefactions.command.help")
                .executor (new HelpCommand ())
                .build ();

        CommandSpec commandEagleFactions = CommandSpec.builder ()
                .description (Text.of ("Pomoc"))
                .permission ("eaglefactions.command.help")
                .executor (new HelpCommand())
                .child (commandhelp, "help")
                .build ();

        Sponge.getCommandManager ().register (this, commandEagleFactions, "factions", "f");
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event)
    {

    }

    @Listener
    public void onServerStopping(GameStoppingServerEvent event){
        getLogger ().info ("Landing...");
    }
}