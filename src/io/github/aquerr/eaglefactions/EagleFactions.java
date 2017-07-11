package io.github.aquerr.eaglefactions;

import io.github.aquerr.eaglefactions.commands.HelpCommand;

import com.google.inject.Inject;
import org.slf4j.Logger;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

import org.spongepowered.api.text.Text;

@Plugin(id = PluginInfo.Id, name = PluginInfo.Name, version = PluginInfo.Version, description = PluginInfo.Description)
public class EagleFactions {

    @Inject
    private Logger logger;

    public Logger getLogger() {
        return logger;
    }

    @Listener
    public void onServerInitialization(GameInitializationEvent event) {
       // getLogger ().info(String.valueOf (Text.of (TextColors.AQUA, "EagleFactions is loading...")));
       // getLogger ().info (String.valueOf (Text.of (TextColors.AQUA,"Preparing wings...")));

        getLogger ().info("EagleFactions is loading...");
        getLogger ().info("Preparing wings...");

        //getLogger ().info ();

        InitializeCommands ();

    }

    private void InitializeCommands()
    {
        getLogger ().info ("Initializing commands...");

        CommandSpec commandhelp = CommandSpec.builder ()
                .description (Text.of ("Help"))
                .permission ("eaglefactions.command.help")
                .executor (new HelpCommand ())
                .build ();

        CommandSpec commandEagleFactions = CommandSpec.builder ()
                .description (Text.of ("Help"))
                .permission ("eaglefactions.command.help")
                .executor (new HelpCommand())
                .child (commandhelp, "help")
                .build ();

        Sponge.getCommandManager ().register (this, commandEagleFactions, "factions", "f");
    }
}
