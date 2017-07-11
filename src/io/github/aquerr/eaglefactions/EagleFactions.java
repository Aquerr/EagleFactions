package io.github.aquerr.eaglefactions;

import io.github.aquerr.eaglefactions.commands.EagleFactionsCommand;
import io.github.aquerr.eaglefactions.commands.HelpCommand;

import com.google.inject.Inject;
import org.slf4j.Logger;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

import org.spongepowered.api.text.Text;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Plugin(id = PluginInfo.Id, name = PluginInfo.Name, version = PluginInfo.Version, description = PluginInfo.Description)
public class EagleFactions
{

    public static Map<List<String>, CommandSpec> _subcommands;

    @Inject
    private Logger _logger;

    public Logger getLogger() {
        return _logger;
    }

    @Listener
    public void onServerInitialization(GameInitializationEvent event)
    {

        //TODO:Change color of loggs.
       getLogger ().info("EagleFactions is loading...");
       getLogger ().debug ("Preparing wings...");

       InitializeCommands ();

    }

    private void InitializeCommands()
    {
        getLogger ().info ("Initializing commands...");

        _subcommands = new HashMap<List<String>, CommandSpec>();

        _subcommands.put (Arrays.asList ("help"), CommandSpec.builder ()
                .description (Text.of ("Help"))
                .permission ("eaglefactions.command.help")
                .executor (new HelpCommand ())
                .build());

        CommandSpec commandEagleFactions = CommandSpec.builder ()
                .description (Text.of ("Factions"))
                .permission ("eaglefactions.command.use")
                .executor (new EagleFactionsCommand ())
                .children (_subcommands)
                .build ();



        Sponge.getCommandManager ().register (this, commandEagleFactions, "factions", "f");

        getLogger ().info ("EagleFactions is ready to use!");
        getLogger ().info ("Thank you for choosing this plugin!");
        getLogger ().info ("Current version " + PluginInfo.Version);
        getLogger ().info ("Have a great time with EagleFactions! :D");
    }
}
