package io.github.aquerr.eaglefactions;

import com.google.inject.Inject;
import io.github.aquerr.eaglefactions.commands.*;
import io.github.aquerr.eaglefactions.config.Configuration;
import io.github.aquerr.eaglefactions.config.IConfiguration;
import io.github.aquerr.eaglefactions.dynmap.DynmapMain;
import io.github.aquerr.eaglefactions.entities.AllyRequest;
import io.github.aquerr.eaglefactions.entities.ChatEnum;
import io.github.aquerr.eaglefactions.entities.Invite;
import io.github.aquerr.eaglefactions.entities.StopWarRequest;
import io.github.aquerr.eaglefactions.listeners.*;
import io.github.aquerr.eaglefactions.logic.AttackLogic;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.PVPLogger;
import io.github.aquerr.eaglefactions.managers.*;
import io.github.aquerr.eaglefactions.message.MessageLoader;
import io.github.aquerr.eaglefactions.message.PluginMessages;
import io.github.aquerr.eaglefactions.parsers.FactionNameArgument;
import io.github.aquerr.eaglefactions.parsers.FactionPlayerArgument;
import io.github.aquerr.eaglefactions.placeholders.EFPlaceholderService;
import io.github.aquerr.eaglefactions.scheduling.EagleFactionsScheduler;
import io.github.aquerr.eaglefactions.scheduling.FactionRemoverTask;
import io.github.aquerr.eaglefactions.storage.StorageManager;
import io.github.aquerr.eaglefactions.version.VersionChecker;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Tristate;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Plugin(id = PluginInfo.ID, name = PluginInfo.NAME, version = PluginInfo.VERSION, description = PluginInfo.DESCRIPTION, authors = PluginInfo.AUTHOR,
        dependencies = {@Dependency(id = "placeholderapi", optional = true)})
public class EagleFactions
{
    public static final Map<List<String>, CommandSpec> SUBCOMMANDS = new HashMap<>();
    public static final List<Invite> INVITE_LIST = new ArrayList<>();
    public static final List<AllyRequest> ALLY_INVITE_LIST = new ArrayList<>();
    public static final List<StopWarRequest> WAR_STOP_REQUEST_LIST = new ArrayList<>();
    public static final List<UUID> AUTO_CLAIM_LIST = new ArrayList<>();
    public static final List<UUID> AUTO_MAP_LIST = new ArrayList<>();
    public static final List<UUID> ADMIN_MODE_PLAYERS = new ArrayList<>();
    public static final Map<String, Integer> ATTACKED_FACTIONS = new HashMap<>();
    public static final Map<UUID, Integer> BLOCKED_HOME = new HashMap<>();
    public static final Map<UUID, ChatEnum> CHAT_LIST = new HashMap<>();
    public static final Map<UUID, Integer> HOME_COOLDOWN_PLAYERS = new HashMap<>();
    public static final List<UUID> DEBUG_MODE_PLAYERS = new ArrayList<>();

    private static EagleFactions eagleFactions;

    private IConfiguration _configuration;
    private PVPLogger _pvpLogger;
    private PlayerManager _playerManager;
    private FlagManager _flagManager;
    private IProtectionManager _protectionManager;
    private PowerManager _powerManager;
    private AttackLogic _attackLogic;
    private FactionLogic _factionLogic;
    private StorageManager _storageManager;
    private EFPlaceholderService _efPlaceholderService;

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
        eagleFactions = this;

        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.AQUA, "Preparing wings..."));

        SetupConfigs();

        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.AQUA, "Configs loaded..."));

        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.AQUA, "Loading managers and cache..."));
        SetupManagers();

        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.AQUA, "Managers loaded..."));

        InitializeCommands();

        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.AQUA, "Commands loaded..."));

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
    public void onGameStarting(GameStartingServerEvent event)
    {
        try
        {
            Class placeholderInterface = Class.forName("me.rojo8399.placeholderapi.PlaceholderService");
            Optional placeholderService1 = Sponge.getServiceManager().provide(placeholderInterface);
            placeholderService1.ifPresent(placeholderService -> {
                printInfo("Found PlaceholderAPI! Registering placeholders...");
                _efPlaceholderService = EFPlaceholderService.getInstance(this, placeholderService);
                printInfo("Registered Eagle Factions' placeholders.");
            });
        } catch(NoClassDefFoundError error) {
            printInfo("No PlaceholderAPI found. Skipping adding placeholders.");
        }
        catch(ClassNotFoundException e)
        {
            printInfo("No PlaceholderAPI found. Skipping adding placeholders.");
        }

        final Optional<PermissionService> permissionService = Sponge.getServiceManager().provide(PermissionService.class);
        if(permissionService.isPresent())
        {
            permissionService.get().getDefaults().getSubjectData().setPermission(SubjectData.GLOBAL_CONTEXT, "eaglefactions.player", Tristate.TRUE);
        }

        if (_configuration.getConfigFields().isDynmapIntegrationEnabled()) {
            try {
                Class.forName("org.dynmap.DynmapCommonAPI");
                DynmapMain.activate();

                printInfo("Dynmap Integration is active!");
            } catch (ClassNotFoundException error) {
                printInfo("Failed to enable Dynmap Integration; Dynmap is not available");
            }
        }
    }

    @Listener
    public void onServerPostInitialization(GamePostInitializationEvent event)
    {
        startFactionsRemover();
    }

    @Listener
    public void onReload(GameReloadEvent event)
    {
        this._configuration.reloadConfiguration();
        this._storageManager.reloadStorage();

        if(event.getSource() instanceof Player)
        {
            Player player = (Player)event.getSource();
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.CONFIGS_HAS_BEEN_RELOADED));
        }
    }

    private void InitializeCommands()
    {
        //Help command should display all possible commands in plugin.
        SUBCOMMANDS.put(Collections.singletonList("help"), CommandSpec.builder()
                .description(Text.of("Help"))
                .permission(PluginPermissions.HELP_COMMAND)
                .executor(new HelpCommand(this))
                .build());

        //Create faction command.
        SUBCOMMANDS.put(Arrays.asList("c", "create"), CommandSpec.builder()
                .description(Text.of("Create Faction Command"))
                .permission(PluginPermissions.CREATE_COMMAND)
                .arguments(GenericArguments.string(Text.of("tag")),
                        GenericArguments.string(Text.of("faction name")))
                .executor(new CreateCommand(this))
                .build());

        //Disband faction command.
        SUBCOMMANDS.put(Collections.singletonList("disband"), CommandSpec.builder()
                .description(Text.of("Disband Faction Command"))
                .permission(PluginPermissions.DISBAND_COMMAND)
                .arguments(GenericArguments.optional(new FactionNameArgument(Text.of("faction name"))))
                .executor(new DisbandCommand(this))
                .build());

        //List all factions.
        SUBCOMMANDS.put(Collections.singletonList("list"), CommandSpec.builder()
                .description(Text.of("List all factions"))
                .permission(PluginPermissions.LIST_COMMAND)
                .executor(new ListCommand(this))
                .build());

        //Invite a player to the faction.
        SUBCOMMANDS.put(Collections.singletonList("invite"), CommandSpec.builder()
                .description(Text.of("Invites a player to the faction"))
                .permission(PluginPermissions.INVITE_COMMAND)
                .arguments(GenericArguments.player(Text.of("player")))
                .executor(new InviteCommand(this))
                .build());

        //Kick a player from the faction.
        SUBCOMMANDS.put(Collections.singletonList("kick"), CommandSpec.builder()
                .description(Text.of("Kicks a player from the faction"))
                .permission(PluginPermissions.KICK_COMMAND)
                .arguments(new FactionPlayerArgument(Text.of("player")))
                .executor(new KickCommand(this))
                .build());

        //Join faction command
        SUBCOMMANDS.put(Arrays.asList("j", "join"), CommandSpec.builder()
                .description(Text.of("Join a specific faction"))
                .permission(PluginPermissions.JOIN_COMMAND)
                .arguments(new FactionNameArgument(Text.of("faction name")))
                .executor(new JoinCommand(this))
                .build());

        //Leave faction command
        SUBCOMMANDS.put(Collections.singletonList("leave"), CommandSpec.builder()
                .description(Text.of("Leave a faction"))
                .permission(PluginPermissions.LEAVE_COMMAND)
                .executor(new LeaveCommand(this))
                .build());

        //VERSION command
        SUBCOMMANDS.put(Arrays.asList("v", "version"), CommandSpec.builder()
                .description(Text.of("Shows plugin version"))
                .permission(PluginPermissions.VERSION_COMMAND)
                .executor(new VersionCommand(this))
                .build());

        //Info command. Shows info about a faction.
        SUBCOMMANDS.put(Arrays.asList("i", "info"), CommandSpec.builder()
                .description(Text.of("Show info about a faction"))
                .arguments(GenericArguments.optional(new FactionNameArgument(Text.of("faction name"))))
                .executor(new InfoCommand(this))
                .build());

        //Player command. Shows info about a player. (its factions etc.)
        SUBCOMMANDS.put(Arrays.asList("p", "player"), CommandSpec.builder()
                .description(Text.of("Show info about a player"))
                .permission(PluginPermissions.PLAYER_COMMAND)
                .arguments(GenericArguments.optional(GenericArguments.player(Text.of("player"))))
                .executor(new PlayerCommand(this))
                .build());

        //Build alliance commands.
        SUBCOMMANDS.put(Collections.singletonList("ally"), CommandSpec.builder()
                .description(Text.of("Invite faction to the alliance"))
                .permission(PluginPermissions.ALLY_COMMAND)
                .arguments(new FactionNameArgument(Text.of("faction name")))
                .executor(new AllyCommand(this))
                .build());

        //Build enemy commands.
        SUBCOMMANDS.put(Collections.singletonList("enemy"), CommandSpec.builder()
                .description(Text.of("Declare someone a war"))
                .permission(PluginPermissions.ENEMY_COMMAND)
                .arguments(new FactionNameArgument(Text.of("faction name")))
                .executor(new EnemyCommand(this))
                .build());

        //Promote command
        SUBCOMMANDS.put(Collections.singletonList("promote"), CommandSpec.builder()
                .description(Text.of("Promotes the player to a higher rank"))
                .arguments(GenericArguments.player(Text.of("player")))
                .permission(PluginPermissions.PROMOTE_COMMAND)
                .executor(new PromoteCommand(this))
                .build());

        //Demote command
        SUBCOMMANDS.put(Collections.singletonList("demote"), CommandSpec.builder()
                .description(Text.of("Demotes the player to a lower rank"))
                .arguments(GenericArguments.player(Text.of("player")))
                .permission(PluginPermissions.DEMOTE_COMMAND)
                .executor(new DemoteCommand(this))
                .build());

        //Claim command.
        SUBCOMMANDS.put(Collections.singletonList("claim"), CommandSpec.builder()
                .description(Text.of("Claim a land for your faction"))
                .permission(PluginPermissions.CLAIM_COMMAND)
                .executor(new ClaimCommand(this))
                .build());

        SUBCOMMANDS.put(Collections.singletonList("squareclaim"), CommandSpec.builder()
                .description(Text.of("Claim land in form of square with a given radius"))
                .permission(PluginPermissions.RADIUS_CLAIM_COMMAND)
                .arguments(GenericArguments.integer(Text.of("radius")))
                .executor(new SquareClaimCommand(this))
                .build());

        //Unclaim command.
        SUBCOMMANDS.put(Collections.singletonList("unclaim"), CommandSpec.builder()
                .description(Text.of("Unclaim a land captured by your faction."))
                .permission(PluginPermissions.UNCLAIM_COMMAND)
                .executor(new UnclaimCommand(this))
                .build());

        //Add Unclaimall Command
        SUBCOMMANDS.put(Collections.singletonList("unclaimall"), CommandSpec.builder()
                .description(Text.of("Remove all claims"))
                .permission(PluginPermissions.UNCLAIM_ALL_COMMAND)
                .executor(new UnclaimallCommand(this))
                .build());

        //Map command
        SUBCOMMANDS.put(Collections.singletonList("map"), CommandSpec.builder()
                .description(Text.of("Turn on/off factions map"))
                .permission(PluginPermissions.MAP_COMMAND)
                .executor(new MapCommand(this))
                .build());

        //Sethome command
        SUBCOMMANDS.put(Collections.singletonList("sethome"), CommandSpec.builder()
                .description(Text.of("Set faction's home"))
                .permission(PluginPermissions.SET_HOME_COMMAND)
                .executor(new SetHomeCommand(this))
                .build());

        //Home command
        SUBCOMMANDS.put(Collections.singletonList("home"), CommandSpec.builder()
                .description(Text.of("Teleport to faction's home"))
                .permission(PluginPermissions.HOME_COMMAND)
                .executor(new HomeCommand(this))
                .build());

        //Add autoclaim command.
        SUBCOMMANDS.put(Collections.singletonList("autoclaim"), CommandSpec.builder()
                .description(Text.of("Autoclaim Command"))
                .permission(PluginPermissions.AUTO_CLAIM_COMMAND)
                .executor(new AutoClaimCommand(this))
                .build());

        //Add automap command
        SUBCOMMANDS.put(Collections.singletonList("automap"), CommandSpec.builder()
                .description(Text.of("Automap command"))
                .permission(PluginPermissions.AUTO_MAP_COMMAND)
                .executor(new AutoMapCommand(this))
                .build());

        //Add admin command
        SUBCOMMANDS.put(Collections.singletonList("admin"), CommandSpec.builder()
                .description(Text.of("Toggle admin mode"))
                .permission(PluginPermissions.ADMIN_COMMAND)
                .executor(new AdminCommand(this))
                .build());

        //Add Coords Command
        SUBCOMMANDS.put(Collections.singletonList("coords"), CommandSpec.builder()
                .description(Text.of("Show your teammates coords"))
                .permission(PluginPermissions.COORDS_COMMAND)
                .executor(new CoordsCommand(this))
                .build());

        //Add SetPower Command
        SUBCOMMANDS.put(Collections.singletonList("setpower"), CommandSpec.builder()
                .description(Text.of("Set player's power"))
                .permission(PluginPermissions.SET_POWER_COMMAND)
                .arguments(GenericArguments.player(Text.of("player")),
                        GenericArguments.string(Text.of("power")))
                .executor(new SetPowerCommand(this))
                .build());

        //Add MaxPower Command
        SUBCOMMANDS.put(Collections.singletonList("maxpower"), CommandSpec.builder()
                .description(Text.of("Set player's maxpower"))
                .permission(PluginPermissions.MAX_POWER_COMMAND)
                .arguments(GenericArguments.player(Text.of("player")),
                        GenericArguments.string(Text.of("power")))
                .executor(new MaxPowerCommand(this))
                .build());

        //Add Attack Command
        SUBCOMMANDS.put(Collections.singletonList("attack"), CommandSpec.builder()
                .description(Text.of("Destroy a claim"))
                .permission(PluginPermissions.ATTACK_COMMAND)
                .executor(new AttackCommand(this))
                .build());

        //Reload Command
        SUBCOMMANDS.put(Collections.singletonList("reload"), CommandSpec.builder()
                .description(Text.of("Reload config file"))
                .permission(PluginPermissions.RELOAD_COMMAND)
                .executor(new ReloadCommand(this))
                .build());

        //Chat Command
        SUBCOMMANDS.put(Collections.singletonList("chat"), CommandSpec.builder()
                .description(Text.of("Chat command"))
                .permission(PluginPermissions.CHAT_COMMAND)
                .arguments(GenericArguments.optional(GenericArguments.enumValue(Text.of("chat"), ChatEnum.class)))
                .executor(new ChatCommand(this))
                .build());

        //Top Command
        SUBCOMMANDS.put(Collections.singletonList("top"), CommandSpec.builder()
                .description(Text.of("Top Command"))
                .permission(PluginPermissions.TOP_COMMAND)
                .executor(new TopCommand(this))
                .build());

        //Setleader Command
        SUBCOMMANDS.put(Collections.singletonList("setleader"), CommandSpec.builder()
                .description(Text.of("Set someone as leader (removes you as a leader if you are one)"))
                .permission(PluginPermissions.SET_LEADER_COMMAND)
                .arguments(GenericArguments.player(Text.of("player")))
                .executor(new SetLeaderCommand(this))
                .build());

        //Flags Command
        SUBCOMMANDS.put(Collections.singletonList("flags"), CommandSpec.builder()
                .description(Text.of("Set flags/privileges for members in faction."))
                .permission(PluginPermissions.FLAGS_COMMAND)
                .executor(new FlagsCommand(this))
                .build());

        //TagColor Command
        SUBCOMMANDS.put(Collections.singletonList("tagcolor"), CommandSpec.builder()
                .description(Text.of("Change faction's tag color"))
                .permission(PluginPermissions.TAG_COLOR_COMMAND)
                .arguments(GenericArguments.catalogedElement(Text.of("color"), TextColor.class))
                .executor(new TagColorCommand(this))
                .build());

        //Rename Command
        SUBCOMMANDS.put(Collections.singletonList("rename"), CommandSpec.builder()
                .description(Text.of("Rename faction"))
                .permission(PluginPermissions.RENAMECOMMAND)
                .arguments(GenericArguments.string(Text.of("faction name")))
                .executor(new RenameCommand(this))
                .build());

        //Tag Command
        SUBCOMMANDS.put(Collections.singletonList("tag"), CommandSpec.builder()
                .description(Text.of("Change faction's tag"))
                .permission(PluginPermissions.TAG_COMMAND)
                .arguments(GenericArguments.string(Text.of("tag")))
                .executor(new TagCommand(this))
                .build());

        //Description Command
        SUBCOMMANDS.put(Arrays.asList("desc", "description"), CommandSpec.builder()
                .description(Text.of("Set faction's description."))
                .permission(PluginPermissions.DESCRIPTION_COMMAND)
                .arguments(GenericArguments.remainingJoinedStrings(Text.of("description")))
                .executor(new DescriptionCommand(this))
                .build());
        //Motd Command
        SUBCOMMANDS.put(Collections.singletonList("motd"), CommandSpec.builder()
                .description(Text.of("Set faction's message of the day."))
                .permission(PluginPermissions.MOTD_COMMAND)
                .arguments(GenericArguments.remainingJoinedStrings(Text.of("motd")))
                .executor(new MotdCommand(this))
                .build());

        //EagleFeather Command
        SUBCOMMANDS.put(Collections.singletonList("feather"), CommandSpec.builder()
                .description(Text.of("Spawns mystical eagle's feather"))
                .permission(PluginPermissions.FEATHER_COMMAND)
                .executor(new EagleFeatherCommand(this))
                .build());

        //Chest Command
        SUBCOMMANDS.put(Collections.singletonList("chest"), CommandSpec.builder()
                .description(Text.of("Opens faction's chest"))
                .permission(PluginPermissions.CHEST_COMMAND)
                .arguments(GenericArguments.optional(new FactionNameArgument(Text.of("faction name"))))
                .executor(new ChestCommand(this))
                .build());

        //Debug Command
        SUBCOMMANDS.put(Collections.singletonList("debug"), CommandSpec.builder()
                .description(Text.of("Toggles debug mode"))
                .permission(PluginPermissions.DEBUG_COMMAND)
                .executor(new DebugCommand(this))
                .build());

        //Build all commands
        CommandSpec commandEagleFactions = CommandSpec.builder()
                .description(Text.of("Help Command"))
                .executor(new HelpCommand(this))
                .children(SUBCOMMANDS)
                .build();

        //Register commands
        Sponge.getCommandManager().register(this, commandEagleFactions, "factions", "faction", "f");
    }

    private void RegisterListeners()
    {
        Sponge.getEventManager().registerListeners(this, new EntityDamageListener(this));
        Sponge.getEventManager().registerListeners(this, new PlayerJoinListener(this));
        Sponge.getEventManager().registerListeners(this, new PlayerDeathListener(this));
        Sponge.getEventManager().registerListeners(this, new BlockPlaceListener(this));
        Sponge.getEventManager().registerListeners(this, new BlockBreakListener(this));
        Sponge.getEventManager().registerListeners(this, new PlayerInteractListener(this));
        Sponge.getEventManager().registerListeners(this, new PlayerMoveListener(this));
        Sponge.getEventManager().registerListeners(this, new ChatMessageListener(this));
        Sponge.getEventManager().registerListeners(this, new EntitySpawnListener(this));
        Sponge.getEventManager().registerListeners(this, new PlayerDisconnectListener(this));
        Sponge.getEventManager().registerListeners(this, new SendCommandListener(this));
        Sponge.getEventManager().registerListeners(this, new ExplosionListener(this));
        Sponge.getEventManager().registerListeners(this, new ModifyBlockListener(this));
        Sponge.getEventManager().registerListeners(this, new FactionKickListener(this));
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

    public StorageManager getStorageManager()
    {
        return this._storageManager;
    }

    public InputStream getResourceAsStream(String fileName)
    {
        return this.getClass().getClassLoader().getResourceAsStream(fileName);
    }

    public URL getResource(String fileName)
    {
        return this.getClass().getClassLoader().getResource(fileName);
    }

    public void printInfo(String message)
    {
        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.YELLOW, message));
    }

    private void SetupConfigs()
    {
        _configuration = new Configuration(_configDir);
        MessageLoader messageLoader = MessageLoader.getInstance(this);

        _pvpLogger = new PVPLogger(getConfiguration());
    }

    private void SetupManagers()
    {
        _storageManager = StorageManager.getInstance(this);

        _playerManager = PlayerManager.getInstance(this);
        _powerManager = PowerManager.getInstance(this);
        _flagManager = FlagManager.getInstance(this);
        _factionLogic = FactionLogic.getInstance(this);
        _attackLogic = AttackLogic.getInstance(this);
        _protectionManager = ProtectionManager.getInstance(this);
    }

    private void startFactionsRemover()
    {
        //Do not turn on faction's remover if max inactive time == 0
        if(this.getConfiguration().getConfigFields().getMaxInactiveTime() == 0)
            return;

        EagleFactionsScheduler.getInstance().scheduleWithDelayedInterval(new FactionRemoverTask(eagleFactions), 0, TimeUnit.SECONDS, 1, TimeUnit.HOURS);
    }

    public EFPlaceholderService getEfPlaceholderService()
    {
        return _efPlaceholderService;
    }
}
