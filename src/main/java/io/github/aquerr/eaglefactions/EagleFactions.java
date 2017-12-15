package io.github.aquerr.eaglefactions;

import io.github.aquerr.eaglefactions.commands.*;

import com.google.inject.Inject;
import io.github.aquerr.eaglefactions.config.FactionsConfig;
import io.github.aquerr.eaglefactions.config.MainConfig;
import io.github.aquerr.eaglefactions.entities.AllyInvite;
import io.github.aquerr.eaglefactions.entities.Invite;
import io.github.aquerr.eaglefactions.entities.RemoveEnemy;
import io.github.aquerr.eaglefactions.listeners.*;
import org.slf4j.Logger;

import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.dispatcher.SimpleDispatcher;
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

@Plugin(id = PluginInfo.Id, name = PluginInfo.Name, version = PluginInfo.Version, description = PluginInfo.Description, authors = PluginInfo.Author)
public class EagleFactions
{

    public static Map<List<String>, CommandSpec> Subcommands;
    public static List<Invite> InviteList = new ArrayList<>();
    public static List<AllyInvite> AllayInviteList = new ArrayList<>();
    public static List<RemoveEnemy> RemoveEnemyList = new ArrayList<>();
    public static List<String> AutoClaimList = new ArrayList<>();
    public static List<String> AutoMapList = new ArrayList<>();
    public static List<String> AdminList = new ArrayList<>();
    public static List<String> AttackedFactions = new ArrayList<>();

    @Inject
    private Logger _logger;
    public Logger getLogger(){return _logger;}

    private static EagleFactions eagleFactions;
    public static EagleFactions getEagleFactions() {return eagleFactions;}

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;
    public Path getConfigDir(){return configDir;}

    @Inject
    private Game game;
    public Game getGame(){return game;}


    @Listener
    public void onServerInitialization(GameInitializationEvent event)
    {
        eagleFactions = this;

       //Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.AQUA, "EagleFactions is loading..."));
       Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.AQUA, "Preparing wings..."));

       SetupConfigs();

       Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.AQUA, "Configs loaded..."));

       InitializeCommands();

       Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.AQUA, "Commands loaded..."));

       RegisterListeners();

        //Display some info text in the console.
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.GREEN,"=========================================="));
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.AQUA, "EagleFactions", TextColors.WHITE, " is ready to use!"));
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.WHITE,"Thank you for choosing this plugin!"));
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.WHITE,"Current version: " + PluginInfo.Version));
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

        // Create data directory for EagleFactions
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

        if (!Files.exists(configDir.resolve("players")))
        {
            try
            {
                Files.createDirectories(configDir.resolve("players"));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        // Create configs
        MainConfig.getConfig().setup();
        FactionsConfig.getConfig().setup();
    }

    private void InitializeCommands()
    {
        getLogger ().info ("Initializing commands...");

        Subcommands = new HashMap<List<String>, CommandSpec>();

        //Help command should display all possible commands in plugin.
        Subcommands.put (Arrays.asList ("help"), CommandSpec.builder ()
                .description (Text.of ("Help"))
                .permission (PluginPermissions.HelpCommand)
                .executor (new HelpCommand ())
                .build());

        //Create faction command.
        Subcommands.put (Arrays.asList ("c","create"), CommandSpec.builder ()
        .description (Text.of ("Create Faction Command"))
        .permission (PluginPermissions.CreateCommand)
        .arguments (GenericArguments.optional(GenericArguments.string(Text.of("tag"))),
                GenericArguments.optional(GenericArguments.string(Text.of("faction name"))))
        .executor (new CreateCommand ())
        .build ());

        //Disband faction command.
        Subcommands.put(Arrays.asList("disband"), CommandSpec.builder()
        .description(Text.of("Disband Faction Command"))
        .permission(PluginPermissions.DisbandCommand)
        .executor(new DisbandCommand())
        .build());

        //List all factions.
        Subcommands.put(Arrays.asList("list"), CommandSpec.builder()
        .description(Text.of("List all factions"))
        .permission(PluginPermissions.ListCommand)
        .executor(new ListCommand())
        .build());

        //Invite a player to the faction.
        Subcommands.put(Arrays.asList("invite"), CommandSpec.builder()
        .description(Text.of("Invites a player to the faction"))
        .permission(PluginPermissions.InviteCommand)
        .arguments(GenericArguments.optional(GenericArguments.player(Text.of("player"))))
        .executor(new InviteCommand())
        .build());

        //Kick a player from the faction.
        Subcommands.put(Arrays.asList("kick"), CommandSpec.builder()
        .description(Text.of("Kicks a player from the faction"))
        .permission(PluginPermissions.KickCommand)
        .arguments(GenericArguments.optional(GenericArguments.player(Text.of("player"))))
        .executor(new KickCommand())
        .build());

        //Join faction command
        Subcommands.put(Arrays.asList("j","join"), CommandSpec.builder()
        .description(Text.of("Join a specific faction"))
        .permission(PluginPermissions.JoinCommand)
        .arguments(GenericArguments.optional(GenericArguments.string(Text.of("faction name"))))
        .executor(new JoinCommand())
        .build());

        //Leave faction command
        Subcommands.put(Arrays.asList("leave"), CommandSpec.builder()
        .description(Text.of("Leave a faction"))
        .permission(PluginPermissions.LeaveCommand)
        .executor(new LeaveCommand())
        .build());

        //Version command
        Subcommands.put(Arrays.asList("v","version"), CommandSpec.builder()
        .description(Text.of("Shows plugin version"))
        .permission(PluginPermissions.VersionCommand)
        .executor(new VersionCommand())
        .build());

        //Info command. Shows info about a faction.
        Subcommands.put(Arrays.asList("i","info"), CommandSpec.builder()
        .description(Text.of("Show info about a faction"))
        .permission(PluginPermissions.InfoCommand)
        .arguments(GenericArguments.optional(GenericArguments.string(Text.of("faction name"))))
        .executor(new InfoCommand())
        .build());

        //Player command. Shows info about a player. (its factions etc.)
        Subcommands.put(Arrays.asList("p", "player"), CommandSpec.builder()
        .description(Text.of("Show info about a player"))
        .permission(PluginPermissions.PlayerCommand)
        .arguments(GenericArguments.optional(GenericArguments.player(Text.of("player"))))
        .executor(new PlayerCommand())
        .build());

        //Build add ally command.
        CommandSpec addAllyCommand = CommandSpec.builder()
                .description(Text.of("Invite faction to the alliance"))
                .permission(PluginPermissions.AddAllyCommand)
                .arguments(GenericArguments.optional(GenericArguments.string(Text.of("faction name"))))
                .executor(new AddAllyCommand())
                .build();

        //Build remove ally command.
        CommandSpec removeAllyCommand = CommandSpec.builder()
                .description(Text.of("Remove faction from the alliance"))
                .permission(PluginPermissions.RemoveAllyCommand)
                .arguments(GenericArguments.optional(GenericArguments.string(Text.of("faction name"))))
                .executor(new RemoveAllyCommand())
                .build();

        //Build alliance commands.
        Subcommands.put(Arrays.asList("ally"), CommandSpec.builder()
        .description(Text.of("Invite faction to the alliance"))
        .permission(PluginPermissions.AllyCommands)
        .child(addAllyCommand, "a", "add")
        .child(removeAllyCommand, "r", "remove")
        .build());

        //Build add enemy command.
        CommandSpec addEnemyCommand = CommandSpec.builder()
                .description(Text.of("Set faction as enemy"))
                .permission(PluginPermissions.AddEnemyCommand)
                .arguments(GenericArguments.optional(GenericArguments.string(Text.of("faction name"))))
                .executor(new AddEnemyCommand())
                .build();

        //Build remove enemy command.
        CommandSpec removeEnemyCommand = CommandSpec.builder()
                .description(Text.of("Remove faction from the enemies"))
                .permission(PluginPermissions.RemoveEnemyCommand)
                .arguments(GenericArguments.optional(GenericArguments.string(Text.of("faction name"))))
                .executor(new RemoveEnemyCommand())
                .build();

        //Build enemy commands.
        Subcommands.put(Arrays.asList("enemy"), CommandSpec.builder()
                .description(Text.of("Declare someone a war"))
                .permission(PluginPermissions.EnemyCommands)
                .child(addEnemyCommand, "a", "add")
                .child(removeEnemyCommand, "r", "remove")
                .build());

        //Officer command. Add or remove officers.
        Subcommands.put(Arrays.asList("officer"), CommandSpec.builder()
                .description(Text.of("Add or Remove officer"))
                .arguments(GenericArguments.optional(GenericArguments.player(Text.of("player"))))
                .permission(PluginPermissions.OfficerCommand)
                .executor(new OfficerCommand())
                .build());

        //Friendly Fire command.
        Subcommands.put(Arrays.asList("friendlyfire"), CommandSpec.builder()
                .description(Text.of("Allow/Deny friendly fire in the faction"))
                .permission(PluginPermissions.FriendlyFireCommand)
                .executor(new FriendlyFireCommand())
                .build());

        //Claim command.
        Subcommands.put(Arrays.asList("claim"), CommandSpec.builder()
                .description(Text.of("Claim a land for your faction"))
                .permission(PluginPermissions.ClaimCommand)
                .executor(new ClaimCommand())
                .build());

        //Unclaim command.
        Subcommands.put(Arrays.asList("unclaim"), CommandSpec.builder()
                .description(Text.of("Unclaim a land caputred by your faction."))
                .permission(PluginPermissions.UnclaimCommand)
                .executor(new UnclaimCommand())
                .build());

        //Add Unclaimall Command
        Subcommands.put(Arrays.asList("unclaimall"), CommandSpec.builder()
                .description(Text.of("Remove all claims"))
                .permission(PluginPermissions.UnclaimAllCommand)
                .executor(new UnclaimallCommand())
                .build());

        //Map command
        Subcommands.put(Arrays.asList("map"), CommandSpec.builder()
                .description(Text.of("Turn on/off factions map"))
                .permission(PluginPermissions.MapCommand)
                .executor(new MapCommand())
                .build());

        //Sethome command
        Subcommands.put(Arrays.asList("sethome"), CommandSpec.builder()
                .description(Text.of("Set faction's home"))
                .permission(PluginPermissions.SetHomeCommand)
                .executor(new SetHomeCommand())
                .build());

        //Home command
        Subcommands.put(Arrays.asList("home"), CommandSpec.builder()
                .description(Text.of("Teleport to faciton's home"))
                .permission(PluginPermissions.HomeCommand)
                .executor(new HomeCommand())
                .build());

        //Add autoclaim command.
        Subcommands.put(Arrays.asList("autoclaim"), CommandSpec.builder()
                .description(Text.of("Autoclaim Command"))
                .permission(PluginPermissions.AutoClaimCommand)
                .executor(new AutoClaimCommand())
                .build());

        //Add automap command
        Subcommands.put(Arrays.asList("automap"), CommandSpec.builder()
                .description(Text.of("Automap command"))
                .permission(PluginPermissions.AutoMapCommand)
                .executor(new AutoMapCommand())
                .build());

        //Add admin command
        Subcommands.put(Arrays.asList("admin"), CommandSpec.builder()
                .description(Text.of("Toggle admin mode"))
                .permission(PluginPermissions.AdminCommand)
                .executor(new AdminCommand())
                .build());

        //Add Coords Command
        Subcommands.put(Arrays.asList("coords"), CommandSpec.builder()
                .description(Text.of("Show your teammates coords"))
                .permission(PluginPermissions.CoordsCommand)
                .executor(new CoordsCommand())
                .build());

        //Add SetPower Command
        Subcommands.put(Arrays.asList("setpower"), CommandSpec.builder()
                .description(Text.of("Set player's power"))
                .permission(PluginPermissions.SetPowerCommand)
                .arguments(GenericArguments.optional(GenericArguments.player(Text.of("player"))), GenericArguments.remainingJoinedStrings(Text.of("power")))
                .executor(new SetPowerCommand())
                .build());

        //Add MaxPower Command
        Subcommands.put(Arrays.asList("maxpower"), CommandSpec.builder()
                .description(Text.of("Set player's maxpower"))
                .permission(PluginPermissions.MaxPowerCommand)
                .arguments(GenericArguments.optional(GenericArguments.player(Text.of("player"))), GenericArguments.remainingJoinedStrings(Text.of("power")))
                .executor(new MaxPowerCommand())
                .build());

        //Add Attack Command
        Subcommands.put(Arrays.asList("attack"), CommandSpec.builder()
                .description(Text.of("Destroy a claim"))
                .permission(PluginPermissions.AttackCommand)
                .executor(new AttackCommand())
                .build());

        //Reload Command
        Subcommands.put(Arrays.asList("reload"), CommandSpec.builder()
                .description(Text.of("Reload config file"))
                .permission(PluginPermissions.ReloadCommand)
                .executor(new ReloadCommand())
                .build());

//        CommandCallable chatCommand = new ChatCommand();
////
////        SimpleDispatcher rootCommand = new SimpleDispatcher();

//        rootCommand.register(chatCommand, "chatcommand", "chat");

        Subcommands.put(Arrays.asList("chat"), CommandSpec.builder()
                .description(Text.of("Chat command"))
                .permission(PluginPermissions.ChatCommand)
                .executor(new ChatCommand())
                .build());

        //Build all commands
        CommandSpec commandEagleFactions = CommandSpec.builder ()
                .description (Text.of ("Help Command"))
                .executor (new HelpCommand())
                .children (Subcommands)
                .build ();

        //Register commands
        Sponge.getCommandManager ().register (this, commandEagleFactions, "factions", "faction", "f");
    }

    private void RegisterListeners()
    {
        getLogger ().info ("Registering listeners...");

        Sponge.getEventManager().registerListeners(this, new EntityDamageListener());
        Sponge.getEventManager().registerListeners(this, new PlayerJoinListener());
        Sponge.getEventManager().registerListeners(this, new PlayerDeathListener());
        Sponge.getEventManager().registerListeners(this, new PlayerBlockPlaceListener());
        Sponge.getEventManager().registerListeners(this, new EntityBlockBreakListener());
        Sponge.getEventManager().registerListeners(this, new PlayerInteractListener());
        Sponge.getEventManager().registerListeners(this, new PlayerMoveListener());
        Sponge.getEventManager().registerListeners(this, new ChatMessageListener());
        Sponge.getEventManager().registerListeners(this, new EntitySpawnListener());
    }
}
