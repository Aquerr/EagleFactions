package io.github.aquerr.eaglefactions;

import com.google.inject.Inject;
import io.github.aquerr.eaglefactions.commands.*;
import io.github.aquerr.eaglefactions.config.Configuration;
import io.github.aquerr.eaglefactions.config.IConfiguration;
import io.github.aquerr.eaglefactions.entities.*;
import io.github.aquerr.eaglefactions.listeners.*;
import io.github.aquerr.eaglefactions.logic.AttackLogic;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.MessageLoader;
import io.github.aquerr.eaglefactions.logic.PVPLogger;
import io.github.aquerr.eaglefactions.managers.*;
import io.github.aquerr.eaglefactions.parsers.FactionNameArgument;
import io.github.aquerr.eaglefactions.parsers.FactionPlayerArgument;
import io.github.aquerr.eaglefactions.version.VersionChecker;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Plugin(id = PluginInfo.ID, name = PluginInfo.NAME, version = PluginInfo.VERSION, description = PluginInfo.DESCRIPTION, authors = PluginInfo.AUTHOR,
        dependencies = {@Dependency(id = "placeholderapi", optional = true)})
public class EagleFactions
{
    public static Map<List<String>, CommandSpec> Subcommands;
    public static List<Invite> InviteList;
    public static List<AllyRequest> AllayInviteList;
    public static List<StopWarRequest> stopWarRequestList;
    public static List<UUID> AutoClaimList;
    public static List<UUID> AutoMapList;
    public static List<UUID> AdminList;
    public static Map<String, Integer> AttackedFactions;
    public static Map<UUID, Integer> BlockedHome;
    public static Map<UUID, ChatEnum> ChatList;
    public static Map<UUID, Integer> HomeCooldownPlayers;

    private IConfiguration _configuration;
    private PVPLogger _pvpLogger;
    private PlayerManager _playerManager;
    private FlagManager _flagManager;
    private IProtectionManager _protectionManager;
    private PowerManager _powerManager;
    private AttackLogic _attackLogic;
    private FactionLogic _factionLogic;

    private static EagleFactions eagleFactions;

    public static EagleFactions getPlugin()
    {
        return eagleFactions;
    }

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path _configDir;

    public Path getConfigDir()
    {
        return _configDir;
    }

    @Listener
    public void onServerInitialization(GameInitializationEvent event)
    {
        Subcommands = new HashMap<>();
        InviteList = new ArrayList<>();
        AllayInviteList = new ArrayList<>();
        stopWarRequestList = new ArrayList<>();
        AutoClaimList = new ArrayList<>();
        AutoMapList = new ArrayList<>();
        AdminList = new ArrayList<>();
        AttackedFactions = new HashMap<>();
        BlockedHome = new HashMap<>();
        ChatList = new HashMap<>();
        HomeCooldownPlayers = new HashMap<>();

        eagleFactions = this;

        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.AQUA, "Preparing wings..."));

        SetupConfigs();

        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.AQUA, "Configs loaded..."));

        SetupManagers();

        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.AQUA, "Managers loaded..."));

        InitializeCommands();

        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.AQUA, "Commands loaded..."));

        RegisterListeners();

        //Display some info text in the console.
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.GREEN, "=========================================="));
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.AQUA, "Eagle Factions", TextColors.WHITE, " is ready to use!"));
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.WHITE, "Thank you for choosing this plugin!"));
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.WHITE, "Current version: " + PluginInfo.VERSION));
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.WHITE, "Have a great time with Eagle Factions! :D"));
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.GREEN, "=========================================="));

        if(!VersionChecker.isLatest(PluginInfo.VERSION))
        {
            Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.GOLD, "Hey! A new version of ", TextColors.AQUA, PluginInfo.NAME, TextColors.GOLD, " is available online!"));
            Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.GREEN, "=========================================="));
        }
    }

    @Listener
    public void onServerGameLoaded(GameStartedServerEvent event)
    {
        startFactionsRemover();
    }

    private void SetupConfigs()
    {
        _configuration = new Configuration(_configDir);
        MessageLoader messageLoader = new MessageLoader(getConfiguration(), _configDir);

        _pvpLogger = new PVPLogger(getConfiguration());
    }

    private void SetupManagers()
    {
        _playerManager = new PlayerManager(this);
        _powerManager = new PowerManager(this);
        _flagManager = new FlagManager(this);
        _factionLogic = new FactionLogic(this);
        _attackLogic = new AttackLogic(_factionLogic, _configuration.getConfigFileds());
        _protectionManager = new ProtectionManager(this);
    }

    private void startFactionsRemover()
    {
        Task.Builder factionsRemoveTask = Sponge.getScheduler().createTaskBuilder();
        factionsRemoveTask.async().execute(new Runnable()
        {
            @Override
            public void run()
            {
                long maxInactive = getConfiguration().getConfigFileds().getMaxInactiveTime();
                if(maxInactive != 0)
                {
                    Map<String, Faction> factionsList = new HashMap<>(_factionLogic.getFactions());
                    for(Map.Entry<String, Faction> factionEntry : factionsList.entrySet())
                    {
                        if(factionEntry.getValue().getName().equalsIgnoreCase("safezone") || factionEntry.getValue().getName().equalsIgnoreCase("warzone"))
                            continue;

                        if(_factionLogic.hasOnlinePlayers(factionEntry.getValue()))
                            continue;

                        Duration inactiveTime = Duration.between(factionEntry.getValue().getLastOnline(), Instant.now());

                        if(maxInactive < inactiveTime.getSeconds())
                        {
                            _factionLogic.disbandFaction(factionEntry.getKey());
                        }
//                        else if(maxInactive - 172800 < inactiveTime.getSeconds())
//                        {
//                            long timeToRemove = maxInactive - inactiveTime.getSeconds();
//                            Sponge.getServer().getBroadcastChannel().send(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.RED, "Faction ", TextColors.GOLD, factionEntry.getKey(), TextColors.RED, " will be removed after ", timeToRemove + "days due to its long inactive time.")));
//                        }
                        else if(maxInactive * 0.75 < inactiveTime.getSeconds())
                        {
                            long timeToRemove = maxInactive - inactiveTime.getSeconds();
                            Sponge.getServer().getBroadcastChannel().send(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.RED, "Faction ", TextColors.GOLD, factionEntry.getKey(), TextColors.RED, " will be removed after ", timeToRemove + "s due to its long inactive time.")));
                        }
                    }
                }
            }
        }).interval(1, TimeUnit.HOURS).submit(this);
    }

    private void InitializeCommands()
    {
        //Help command should display all possible commands in plugin.
        Subcommands.put(Collections.singletonList("help"), CommandSpec.builder()
                .description(Text.of("Help"))
                .permission(PluginPermissions.HELP_COMMAND)
                .executor(new HelpCommand(this))
                .build());

        //Create faction command.
        Subcommands.put(Arrays.asList("c", "create"), CommandSpec.builder()
                .description(Text.of("Create Faction Command"))
                .permission(PluginPermissions.CREATE_COMMAND)
                .arguments(GenericArguments.optional(GenericArguments.string(Text.of("tag"))),
                        GenericArguments.optional(GenericArguments.string(Text.of("faction name"))))
                .executor(new CreateCommand(this))
                .build());

        //Disband faction command.
        Subcommands.put(Collections.singletonList("disband"), CommandSpec.builder()
                .description(Text.of("Disband Faction Command"))
                .permission(PluginPermissions.DISBAND_COMMAND)
                .executor(new DisbandCommand(this))
                .build());

        //List all factions.
        Subcommands.put(Collections.singletonList("list"), CommandSpec.builder()
                .description(Text.of("List all factions"))
                .permission(PluginPermissions.LIST_COMMAND)
                .executor(new ListCommand(this))
                .build());

        //Invite a player to the faction.
        Subcommands.put(Collections.singletonList("invite"), CommandSpec.builder()
                .description(Text.of("Invites a player to the faction"))
                .permission(PluginPermissions.INVITE_COMMAND)
                .arguments(GenericArguments.optional(GenericArguments.player(Text.of("player"))))
                .executor(new InviteCommand(this))
                .build());

        //Kick a player from the faction.
        Subcommands.put(Collections.singletonList("kick"), CommandSpec.builder()
                .description(Text.of("Kicks a player from the faction"))
                .permission(PluginPermissions.KICK_COMMAND)
                .arguments(new FactionPlayerArgument(Text.of("player")))
                .executor(new KickCommand(this))
                .build());

        //Join faction command
        Subcommands.put(Arrays.asList("j", "join"), CommandSpec.builder()
                .description(Text.of("Join a specific faction"))
                .permission(PluginPermissions.JOIN_COMMAND)
                .arguments(new FactionNameArgument(Text.of("faction name")))
                .executor(new JoinCommand(this))
                .build());

        //Leave faction command
        Subcommands.put(Collections.singletonList("leave"), CommandSpec.builder()
                .description(Text.of("Leave a faction"))
                .permission(PluginPermissions.LEAVE_COMMAND)
                .executor(new LeaveCommand(this))
                .build());

        //VERSION command
        Subcommands.put(Arrays.asList("v", "version"), CommandSpec.builder()
                .description(Text.of("Shows plugin version"))
                .permission(PluginPermissions.VERSION_COMMAND)
                .executor(new VersionCommand(this))
                .build());

        //Info command. Shows info about a faction.
        Subcommands.put(Arrays.asList("i", "info"), CommandSpec.builder()
                .description(Text.of("Show info about a faction"))
                .arguments(new FactionNameArgument(Text.of("faction name")))
                .executor(new InfoCommand(this))
                .build());

        //Player command. Shows info about a player. (its factions etc.)
        Subcommands.put(Arrays.asList("p", "player"), CommandSpec.builder()
                .description(Text.of("Show info about a player"))
                .permission(PluginPermissions.PLAYER_COMMAND)
                .arguments(GenericArguments.optional(GenericArguments.player(Text.of("player"))))
                .executor(new PlayerCommand(this))
                .build());

        //Build alliance commands.
        Subcommands.put(Collections.singletonList("ally"), CommandSpec.builder()
                .description(Text.of("Invite faction to the alliance"))
                .permission(PluginPermissions.ALLY_COMMAND)
                .arguments(new FactionNameArgument(Text.of("faction name")))
                .executor(new AllyCommand(this))
                .build());

        //Build enemy commands.
        Subcommands.put(Collections.singletonList("enemy"), CommandSpec.builder()
                .description(Text.of("Declare someone a war"))
                .permission(PluginPermissions.ENEMY_COMMAND)
                .arguments(new FactionNameArgument(Text.of("faction name")))
                .executor(new EnemyCommand(this))
                .build());

        //Promote command
        Subcommands.put(Collections.singletonList("promote"), CommandSpec.builder()
                .description(Text.of("Promotes the player to a higher rank"))
                .arguments(GenericArguments.optional(GenericArguments.player(Text.of("player"))))
                .permission(PluginPermissions.PROMOTE_COMMAND)
                .executor(new PromoteCommand(this))
                .build());

        //Demote command
        Subcommands.put(Collections.singletonList("demote"), CommandSpec.builder()
                .description(Text.of("Demotes the player to a lower rank"))
                .arguments(GenericArguments.optional(GenericArguments.player(Text.of("player"))))
                .permission(PluginPermissions.DEMOTE_COMMAND)
                .executor(new DemoteCommand(this))
                .build());

        //Claim command.
        Subcommands.put(Collections.singletonList("claim"), CommandSpec.builder()
                .description(Text.of("Claim a land for your faction"))
                .permission(PluginPermissions.CLAIM_COMMAND)
                .executor(new ClaimCommand(this))
                .build());

        //Unclaim command.
        Subcommands.put(Collections.singletonList("unclaim"), CommandSpec.builder()
                .description(Text.of("Unclaim a land captured by your faction."))
                .permission(PluginPermissions.UNCLAIM_COMMAND)
                .executor(new UnclaimCommand(this))
                .build());

        //Add Unclaimall Command
        Subcommands.put(Collections.singletonList("unclaimall"), CommandSpec.builder()
                .description(Text.of("Remove all claims"))
                .permission(PluginPermissions.UNCLAIM_ALL_COMMAND)
                .executor(new UnclaimallCommand(this))
                .build());

        //Map command
        Subcommands.put(Collections.singletonList("map"), CommandSpec.builder()
                .description(Text.of("Turn on/off factions map"))
                .permission(PluginPermissions.MAP_COMMAND)
                .executor(new MapCommand(this))
                .build());

        //Sethome command
        Subcommands.put(Collections.singletonList("sethome"), CommandSpec.builder()
                .description(Text.of("Set faction's home"))
                .permission(PluginPermissions.SET_HOME_COMMAND)
                .executor(new SetHomeCommand(this))
                .build());

        //Home command
        Subcommands.put(Collections.singletonList("home"), CommandSpec.builder()
                .description(Text.of("Teleport to faction's home"))
                .permission(PluginPermissions.HOME_COMMAND)
                .executor(new HomeCommand(this))
                .build());

        //Add autoclaim command.
        Subcommands.put(Collections.singletonList("autoclaim"), CommandSpec.builder()
                .description(Text.of("Autoclaim Command"))
                .permission(PluginPermissions.AUTO_CLAIM_COMMAND)
                .executor(new AutoClaimCommand(this))
                .build());

        //Add automap command
        Subcommands.put(Collections.singletonList("automap"), CommandSpec.builder()
                .description(Text.of("Automap command"))
                .permission(PluginPermissions.AUTO_MAP_COMMAND)
                .executor(new AutoMapCommand(this))
                .build());

        //Add admin command
        Subcommands.put(Collections.singletonList("admin"), CommandSpec.builder()
                .description(Text.of("Toggle admin mode"))
                .permission(PluginPermissions.ADMIN_COMMAND)
                .executor(new AdminCommand(this))
                .build());

        //Add Coords Command
        Subcommands.put(Collections.singletonList("coords"), CommandSpec.builder()
                .description(Text.of("Show your teammates coords"))
                .permission(PluginPermissions.COORDS_COMMAND)
                .executor(new CoordsCommand(this))
                .build());

        //Add SetPower Command
        Subcommands.put(Collections.singletonList("setpower"), CommandSpec.builder()
                .description(Text.of("Set player's power"))
                .permission(PluginPermissions.SET_POWER_COMMAND)
                .arguments(GenericArguments.optional(GenericArguments.player(Text.of("player"))),
                        GenericArguments.optional(GenericArguments.string(Text.of("power"))))
                .executor(new SetPowerCommand(this))
                .build());

        //Add MaxPower Command
        Subcommands.put(Collections.singletonList("maxpower"), CommandSpec.builder()
                .description(Text.of("Set player's maxpower"))
                .permission(PluginPermissions.MAX_POWER_COMMAND)
                .arguments(GenericArguments.optional(GenericArguments.player(Text.of("player"))),
                        GenericArguments.optional(GenericArguments.string(Text.of("power"))))
                .executor(new MaxPowerCommand(this))
                .build());

        //Add Attack Command
        Subcommands.put(Collections.singletonList("attack"), CommandSpec.builder()
                .description(Text.of("Destroy a claim"))
                .permission(PluginPermissions.ATTACK_COMMAND)
                .executor(new AttackCommand(this))
                .build());

        //Reload Command
        Subcommands.put(Collections.singletonList("reload"), CommandSpec.builder()
                .description(Text.of("Reload config file"))
                .permission(PluginPermissions.RELOAD_COMMAND)
                .executor(new ReloadCommand(this))
                .build());

        //Chat Command
        Subcommands.put(Collections.singletonList("chat"), CommandSpec.builder()
                .description(Text.of("Chat command"))
                .permission(PluginPermissions.CHAT_COMMAND)
                .arguments(GenericArguments.optional(GenericArguments.enumValue(Text.of("chat"), ChatEnum.class)))
                .executor(new ChatCommand(this))
                .build());

        //Top Command
        Subcommands.put(Collections.singletonList("top"), CommandSpec.builder()
                .description(Text.of("Top Command"))
                .permission(PluginPermissions.TOP_COMMAND)
                .executor(new TopCommand(this))
                .build());

        //Setleader Command
        Subcommands.put(Collections.singletonList("setleader"), CommandSpec.builder()
                .description(Text.of("Set someone as leader (removes you as a leader if you are one)"))
                .permission(PluginPermissions.SET_LEADER_COMMAND)
                .arguments(GenericArguments.optional(GenericArguments.player(Text.of("player"))))
                .executor(new SetLeaderCommand(this))
                .build());

        //Flags Command
        Subcommands.put(Collections.singletonList("flags"), CommandSpec.builder()
                .description(Text.of("Set flags/privileges for members in faction."))
                .permission(PluginPermissions.FLAGS_COMMAND)
                .executor(new FlagsCommand(this))
                .build());

        //TagColor Command
        Subcommands.put(Collections.singletonList("tagcolor"), CommandSpec.builder()
                .description(Text.of("Change faction's tag color"))
                .permission(PluginPermissions.TAG_COLOR_COMMAND)
                .arguments(GenericArguments.optional(GenericArguments.catalogedElement(Text.of("color"), TextColor.class)))
                .executor(new TagColorCommand(this))
                .build());

        //Rename Command
        Subcommands.put(Collections.singletonList("rename"), CommandSpec.builder()
                .description(Text.of("Rename faction"))
                .permission(PluginPermissions.RENAMECOMMAND)
                .arguments(GenericArguments.optional(GenericArguments.string(Text.of("faction name"))))
                .executor(new RenameCommand(this))
                .build());

        //Build all commands
        CommandSpec commandEagleFactions = CommandSpec.builder()
                .description(Text.of("Help Command"))
                .executor(new HelpCommand(this))
                .children(Subcommands)
                .build();

        //Register commands
        Sponge.getCommandManager().register(this, commandEagleFactions, "factions", "faction", "f");
    }

    private void RegisterListeners()
    {
        Sponge.getEventManager().registerListeners(this, new EntityDamageListener(this));
        Sponge.getEventManager().registerListeners(this, new PlayerJoinListener(this));
        Sponge.getEventManager().registerListeners(this, new PlayerDeathListener(this));
        Sponge.getEventManager().registerListeners(this, new PlayerBlockPlaceListener(this));
        Sponge.getEventManager().registerListeners(this, new BlockBreakListener(this));
        Sponge.getEventManager().registerListeners(this, new PlayerInteractListener(this));
        Sponge.getEventManager().registerListeners(this, new PlayerMoveListener(this));
        Sponge.getEventManager().registerListeners(this, new ChatMessageListener(this));
        Sponge.getEventManager().registerListeners(this, new EntitySpawnListener(this));
        Sponge.getEventManager().registerListeners(this, new FireBlockPlaceListener(this));
        Sponge.getEventManager().registerListeners(this, new PlayerDisconnectListener(this));
        Sponge.getEventManager().registerListeners(this, new SendCommandListener(this));
        Sponge.getEventManager().registerListeners(this, new ExplosionListener(this));
    }

    public IConfiguration getConfiguration()
    {
        return this._configuration;
    }

    public PVPLogger getPVPLogger()
    {
        return this._pvpLogger;
    }

    public FlagManager getFlagManager()
    {
        return _flagManager;
    }

    public PlayerManager getPlayerManager()
    {
        return _playerManager;
    }

    public PowerManager getPowerManager()
    {
        return _powerManager;
    }

    public IProtectionManager getProtectionManager()
    {
        return _protectionManager;
    }

    public AttackLogic getAttackLogic()
    {
        return _attackLogic;
    }

    public FactionLogic getFactionLogic()
    {
        return _factionLogic;
    }

    public InputStream getResourceAsStream(String fileName)
    {
        return this.getClass().getClassLoader().getResourceAsStream(fileName);
    }

    public URL getResource(String fileName)
    {
        return this.getClass().getClassLoader().getResource(fileName);
    }
}
