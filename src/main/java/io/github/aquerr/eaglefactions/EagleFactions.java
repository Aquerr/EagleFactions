package io.github.aquerr.eaglefactions;

import io.github.aquerr.eaglefactions.commands.*;

import com.google.inject.Inject;
import io.github.aquerr.eaglefactions.config.FactionsConfig;
import io.github.aquerr.eaglefactions.config.MainConfig;
import io.github.aquerr.eaglefactions.entities.Invite;
import org.slf4j.Logger;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Plugin(id = PluginInfo.Id, name = PluginInfo.Name, version = PluginInfo.Version, description = PluginInfo.Description)
public class EagleFactions
{

    public static Map<List<String>, CommandSpec> Subcommands;
    public static List<Invite> InviteList = new ArrayList<>();

    @Inject
    private Logger _logger;
    public Logger getLogger(){return _logger;}

    private static EagleFactions eagleFactions;
    public static EagleFactions getEagleFactions() {return eagleFactions;}

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;
    public Path getConfigDir(){return configDir;}



    @Listener
    public void onServerInitialization(GameInitializationEvent event)
    {

        eagleFactions = this;

        //TODO:Change color of loggs.
       getLogger ().info("EagleFactions is loading...");
       getLogger ().info ("Preparing wings...");

       SetupConfigs();

       InitializeCommands();

        //Display some info text in the console.
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.GREEN,"=========================================="));
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.AQUA, "EagleFactions", TextColors.WHITE, " is ready to use!"));
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.WHITE,"Thank you for choosing this plugin!"));
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.WHITE,"Current version " + PluginInfo.Version));
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.WHITE,"Have a great time with EagleFactions! :D"));
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.GREEN,"=========================================="));
    }

    private void SetupConfigs()
    {
        getLogger().info("Setting up configs...");

        //Create config directory for EagleFactions.
        try
        {
            Files.createDirectories(configDir);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        // Create settings & data directory for EagleFactions
        if (!Files.exists(configDir.resolve("settings")))
        {
            try
            {
                Files.createDirectories(configDir.resolve("settings"));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        if (!Files.exists(configDir.resolve("data")))
        {
            try
            {
                Files.createDirectories(configDir.resolve("data"));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        // Create config.conf
        MainConfig.getConfig().setup();
        // Create messages.conf
        //MessageConfig.getMainConfig().setup();
        // Create teams.conf
        FactionsConfig.getConfig().setup();
        // Create claims.conf
        //ClaimsConfig.getMainConfig().setup();
        // Create claims.conf
        //ClaimsConfig.getMainConfig().setup();
        // Start Tax Service
        //Utils.startTaxService();
    }

    private void InitializeCommands()
    {
        getLogger ().info ("Initializing commands...");

        Subcommands = new HashMap<List<String>, CommandSpec>();

        //Help command should display all possible commands in plugin.
        Subcommands.put (Arrays.asList ("help"), CommandSpec.builder ()
                .description (Text.of ("Help"))
                .permission ("eaglefactions.command.help")
                .executor (new HelpCommand ())
                .build());

        //TODO: Player should assign a faction tag while creating a faction.
        //Create faction command.
        Subcommands.put (Arrays.asList ("create"), CommandSpec.builder ()
        .description (Text.of ("Create Faction Command"))
        .permission ("eaglefactions.command.create")
        .arguments (GenericArguments.onlyOne (GenericArguments.string (Text.of ("faction name"))))
        .executor (new CreateCommand ())
        .build ());

        //Disband faction command.
        Subcommands.put(Arrays.asList("disband"), CommandSpec.builder()
        .description(Text.of("Disband Faction Command"))
        .permission("eaglefactions.command.disband")
        .executor(new DisbandCommand())
        .build());

        //List all factions.
        Subcommands.put(Arrays.asList("list"), CommandSpec.builder()
        .description(Text.of("List all factions"))
        .permission("eaglefactions.command.list")
        .executor(new ListCommand())
        .build());

        //Invite a player to the faction.
        Subcommands.put(Arrays.asList("invite"), CommandSpec.builder()
        .description(Text.of("Invites a player to the faction"))
        .permission("eaglefactions.command.invite")
        .arguments(GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))))
        .executor(new InviteCommand())
        .build());

        //Join faction command
        Subcommands.put(Arrays.asList("join"), CommandSpec.builder()
        .description(Text.of("Join a specific faction"))
        .permission("eaglefactions.command.join")
        .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("faction name"))))
        .executor(new JoinCommand())
        .build());

        //Leave faction command
        Subcommands.put(Arrays.asList("leave"), CommandSpec.builder()
        .description(Text.of("Leave a faction"))
        .permission("eaglefactions.command.leave")
        .executor(new LeaveCommand())
        .build());

        //Version command
        Subcommands.put(Arrays.asList("version"), CommandSpec.builder()
        .description(Text.of("Shows plugin version"))
        .permission("eaglefactions.command.version")
        .executor(new VersionCommand())
        .build());

        //Info command. Shows info about a faction.
        Subcommands.put(Arrays.asList("info"), CommandSpec.builder()
        .description(Text.of("Show info about a faction"))
        .permission("eaglefaction.command.info")
        .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("faction name"))))
        .executor(new InfoCommand())
        .build());

        //Player command. Shows info about a player. (its factions etc.)
        Subcommands.put(Arrays.asList("p", "player"), CommandSpec.builder()
        .description(Text.of("Show info about a player"))
        .permission("eaglefactions.command.player")
        .arguments(GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))))
        .executor(new PlayerCommand())
        .build());

        //Build all commands
        CommandSpec commandEagleFactions = CommandSpec.builder ()
                .description (Text.of ("Help Command"))
                .permission ("eaglefactions.command.*")
                .executor (new HelpCommand())
                .children (Subcommands)
                .build ();

        //Register commands
        Sponge.getCommandManager ().register (this, commandEagleFactions, "factions", "faction", "f");
    }

}
