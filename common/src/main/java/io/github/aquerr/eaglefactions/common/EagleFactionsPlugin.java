package io.github.aquerr.eaglefactions.common;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.Configuration;
import io.github.aquerr.eaglefactions.api.entities.*;
import io.github.aquerr.eaglefactions.api.logic.AttackLogic;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.logic.PVPLogger;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.managers.PowerManager;
import io.github.aquerr.eaglefactions.api.managers.ProtectionManager;
import io.github.aquerr.eaglefactions.api.storage.StorageManager;
import io.github.aquerr.eaglefactions.common.commands.*;
import io.github.aquerr.eaglefactions.common.commands.management.*;
import io.github.aquerr.eaglefactions.common.commands.access.AccessCommand;
import io.github.aquerr.eaglefactions.common.commands.access.AccessFactionCommand;
import io.github.aquerr.eaglefactions.common.commands.access.AccessPlayerCommand;
import io.github.aquerr.eaglefactions.common.commands.args.BackupNameArgument;
import io.github.aquerr.eaglefactions.common.commands.args.FactionArgument;
import io.github.aquerr.eaglefactions.common.commands.args.FactionPlayerArgument;
import io.github.aquerr.eaglefactions.common.commands.args.OwnFactionPlayerArgument;
import io.github.aquerr.eaglefactions.common.commands.backup.BackupCommand;
import io.github.aquerr.eaglefactions.common.commands.backup.RestoreBackupCommand;
import io.github.aquerr.eaglefactions.common.commands.claiming.*;
import io.github.aquerr.eaglefactions.common.commands.general.*;
import io.github.aquerr.eaglefactions.common.commands.rank.DemoteCommand;
import io.github.aquerr.eaglefactions.common.commands.rank.PromoteCommand;
import io.github.aquerr.eaglefactions.common.commands.rank.SetLeaderCommand;
import io.github.aquerr.eaglefactions.common.commands.relation.AllyCommand;
import io.github.aquerr.eaglefactions.common.commands.relation.EnemyCommand;
import io.github.aquerr.eaglefactions.common.commands.relation.TruceCommand;
import io.github.aquerr.eaglefactions.common.commands.admin.*;
import io.github.aquerr.eaglefactions.common.config.ConfigurationImpl;
import io.github.aquerr.eaglefactions.common.entities.FactionImpl;
import io.github.aquerr.eaglefactions.common.integrations.bstats.Metrics;
import io.github.aquerr.eaglefactions.common.integrations.dynmap.DynmapService;
import io.github.aquerr.eaglefactions.common.integrations.placeholderapi.EFPlaceholderService;
import io.github.aquerr.eaglefactions.common.listeners.*;
import io.github.aquerr.eaglefactions.common.listeners.faction.FactionJoinListener;
import io.github.aquerr.eaglefactions.common.listeners.faction.FactionKickListener;
import io.github.aquerr.eaglefactions.common.listeners.faction.FactionLeaveListener;
import io.github.aquerr.eaglefactions.common.logic.AttackLogicImpl;
import io.github.aquerr.eaglefactions.common.logic.FactionLogicImpl;
import io.github.aquerr.eaglefactions.common.logic.PVPLoggerImpl;
import io.github.aquerr.eaglefactions.common.managers.PermsManagerImpl;
import io.github.aquerr.eaglefactions.common.managers.PlayerManagerImpl;
import io.github.aquerr.eaglefactions.common.managers.PowerManagerImpl;
import io.github.aquerr.eaglefactions.common.managers.ProtectionManagerImpl;
import io.github.aquerr.eaglefactions.common.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import io.github.aquerr.eaglefactions.common.scheduling.EagleFactionsScheduler;
import io.github.aquerr.eaglefactions.common.scheduling.FactionRemoverTask;
import io.github.aquerr.eaglefactions.common.storage.StorageManagerImpl;
import io.github.aquerr.eaglefactions.common.storage.serializers.ClaimSetTypeSerializer;
import io.github.aquerr.eaglefactions.common.storage.serializers.ClaimTypeSerializer;
import io.github.aquerr.eaglefactions.common.version.VersionChecker;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.asset.AssetId;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.DataManager;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Plugin(id = PluginInfo.ID, name = PluginInfo.NAME, version = PluginInfo.VERSION, description = PluginInfo.DESCRIPTION, authors = PluginInfo.AUTHOR,
        dependencies = {@Dependency(id = "placeholderapi", optional = true)})
public class EagleFactionsPlugin implements EagleFactions
{
    //TODO: Convert these fields to instance fields.
    public static final Map<List<String>, CommandSpec> SUBCOMMANDS = new HashMap<>();
    public static final List<Invite> INVITE_LIST = new ArrayList<>();
    public static final List<AllyRequest> TRUCE_INVITE_LIST = new ArrayList<>();
    public static final List<AllyRequest> ALLY_INVITE_LIST = new ArrayList<>();
    public static final List<ArmisticeRequest> ARMISTICE_REQUEST_LIST = new ArrayList<>();
    public static final List<UUID> AUTO_CLAIM_LIST = new ArrayList<>();
    public static final List<UUID> AUTO_MAP_LIST = new ArrayList<>();
    public static final Map<UUID, String> REGEN_CONFIRMATION_MAP = new HashMap<>();
    public static final Map<String, Integer> ATTACKED_FACTIONS = new HashMap<>();
    public static final Map<UUID, Integer> BLOCKED_HOME = new HashMap<>();
    public static final Map<UUID, ChatEnum> CHAT_LIST = new HashMap<>();
    public static final Map<UUID, Integer> HOME_COOLDOWN_PLAYERS = new HashMap<>();
    public static final List<UUID> DEBUG_MODE_PLAYERS = new ArrayList<>();

    private static EagleFactionsPlugin eagleFactions;

    private Configuration configuration;
    private PVPLogger pvpLogger;
    private PlayerManagerImpl playerManager;
    private PermsManagerImpl permsManager;
    private ProtectionManager protectionManager;
    private PowerManagerImpl powerManager;
    private AttackLogic attackLogic;
    private FactionLogic factionLogic;
    private StorageManager storageManager;

    @Inject
    @AssetId("Settings.conf")
    private Asset configAsset;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    //Integrations
    @Inject
    private Metrics metrics;

    private EFPlaceholderService efPlaceholderService;
    private DynmapService dynmapService;

    public static EagleFactionsPlugin getPlugin()
    {
        return eagleFactions;
    }

    public Path getConfigDir()
    {
        return configDir;
    }

    @Listener
    public void onServerInitialization(final GameInitializationEvent event)
    {
        eagleFactions = this;

        registerTypeSerializers();

        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.AQUA, "Preparing wings..."));

        setupConfigs();

        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.AQUA, "Configs loaded."));

        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.AQUA, "Loading managers and cache..."));
        setupManagers();
        registerAPI();

        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.AQUA, "Managers loaded."));

        initializeCommands();

        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.AQUA, "Commands loaded."));

        registerListeners();

        //Display some info text in the console.
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.GREEN, "=========================================="));
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.AQUA, "Eagle Factions", TextColors.WHITE, " is ready to use!"));
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.WHITE, "Thank you for choosing this plugin!"));
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.WHITE, "Current version: " + PluginInfo.VERSION));
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.WHITE, "Have a great time with Eagle Factions! :D"));
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.GREEN, "=========================================="));

        CompletableFuture.runAsync(() ->
        {
            if(!VersionChecker.isLatest(PluginInfo.VERSION))
            {
                Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.GOLD, "Hey! A new version of ", TextColors.AQUA, PluginInfo.NAME, TextColors.GOLD, " is available online!"));
                Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.GREEN, "=========================================="));
            }
        });
    }

    private void registerTypeSerializers()
    {
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(Claim.class), new ClaimTypeSerializer());
        TypeSerializers.getDefaultSerializers().registerType(new TypeToken<Set<Claim>>(){},  new ClaimSetTypeSerializer());
    }

    private void registerAPI()
    {
        //This is not really needed as api consumers can access managers classes through EagleFactions interface instance.
        //But we are still registering these managers just in case someone will try to access not through EagleFactions interface.
        Sponge.getServiceManager().setProvider(this, FactionLogic.class, this.factionLogic);
        Sponge.getServiceManager().setProvider(this, PowerManager.class, this.powerManager);
        Sponge.getServiceManager().setProvider(this, PlayerManager.class, this.playerManager);
        Sponge.getServiceManager().setProvider(this, ProtectionManager.class, this.protectionManager);
        Sponge.getServiceManager().setProvider(this, PermsManager.class, this.permsManager);
        Sponge.getServiceManager().setProvider(this, PVPLogger.class, this.pvpLogger);
        Sponge.getServiceManager().setProvider(this, AttackLogic.class, this.attackLogic);
    }

    @Listener
    public void onGameStarting(final GameStartingServerEvent event)
    {
        try
        {
            Class placeholderInterface = Class.forName("me.rojo8399.placeholderapi.PlaceholderService");
            Optional placeholderService1 = Sponge.getServiceManager().provide(placeholderInterface);
            placeholderService1.ifPresent(placeholderService -> {
                printInfo("Found PlaceholderAPI! Registering placeholders...");
                efPlaceholderService = EFPlaceholderService.getInstance(this, placeholderService);
                printInfo("Registered Eagle Factions' placeholders.");
            });
        }
        catch(final NoClassDefFoundError | ClassNotFoundException error)
        {
            printInfo("PlaceholderAPI could not be found. Skipping addition of placeholders.");
        }

        final Optional<PermissionService> permissionService = Sponge.getServiceManager().provide(PermissionService.class);
        if(permissionService.isPresent())
        {
            permissionService.get().getDefaults().getSubjectData().setPermission(SubjectData.GLOBAL_CONTEXT, "eaglefactions.player", Tristate.TRUE);
        }

        if (configuration.getDynmapConfig().isDynmapIntegrationEnabled())
        {
            try
            {
                Class.forName("org.dynmap.DynmapCommonAPI");
                this.dynmapService = new DynmapService(this);
                this.dynmapService.activate();

                printInfo("Dynmap Integration is active!");
            }
            catch (final ClassNotFoundException error)
            {
                printInfo("Dynmap could not be found. Dynmap integration will not be available.");
            }
        }
    }

    @Listener
    public void onServerPostInitialization(final GamePostInitializationEvent event)
    {
        startFactionsRemover();
    }

    @Listener
    public void onReload(final GameReloadEvent event)
    {
        this.configuration.reloadConfiguration();
        this.storageManager.reloadStorage();

        if(event.getSource() instanceof Player)
        {
            Player player = (Player)event.getSource();
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, Messages.CONFIG_HAS_BEEN_RELOADED));
        }
    }

    private void initializeCommands()
    {
        //Help command should display all possible commands in plugin.
        SUBCOMMANDS.put(Collections.singletonList("help"), CommandSpec.builder()
                .description(Text.of("Help"))
                .permission(PluginPermissions.HELP_COMMAND)
                .arguments(GenericArguments.optional(GenericArguments.integer(Text.of("page"))))
                .executor(new HelpCommand(this))
                .build());

        //Create faction command.
        SUBCOMMANDS.put(Arrays.asList("c", "create"), CommandSpec.builder()
                .description(Text.of("Create Faction Command"))
                .permission(PluginPermissions.CREATE_COMMAND)
                .arguments(GenericArguments.string(Text.of("tag")),
                        GenericArguments.string(Text.of("name")))
                .executor(new CreateCommand(this))
                .build());

        //Disband faction command.
        SUBCOMMANDS.put(Collections.singletonList("disband"), CommandSpec.builder()
                .description(Text.of("Disband Faction Command"))
                .permission(PluginPermissions.DISBAND_COMMAND)
                .arguments(GenericArguments.optional(new FactionArgument(this, Text.of("faction"))))
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
                .arguments(GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))))
                .executor(new InviteCommand(this))
                .build());

        //Kick a player from the faction.
        SUBCOMMANDS.put(Collections.singletonList("kick"), CommandSpec.builder()
                .description(Text.of("Kicks a player from the faction"))
                .permission(PluginPermissions.KICK_COMMAND)
                .arguments(GenericArguments.onlyOne(new FactionPlayerArgument(this, Text.of("player"))))
                .executor(new KickCommand(this))
                .build());

        //Join faction command
        SUBCOMMANDS.put(Arrays.asList("j", "join"), CommandSpec.builder()
                .description(Text.of("Join a specific faction"))
                .permission(PluginPermissions.JOIN_COMMAND)
                .arguments(new FactionArgument(this, Text.of("faction")))
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
                .arguments(GenericArguments.optional(new FactionArgument(this, Text.of("faction"))))
                .executor(new InfoCommand(this))
                .build());

        //Player command. Shows info about a player. (its factions etc.)
        SUBCOMMANDS.put(Arrays.asList("p", "player"), CommandSpec.builder()
                .description(Text.of("Show info about a player"))
                .permission(PluginPermissions.PLAYER_COMMAND)
                .arguments(GenericArguments.optional(new FactionPlayerArgument(this, Text.of("player"))))
                .executor(new PlayerCommand(this))
                .build());

        //Truce command
        SUBCOMMANDS.put(Collections.singletonList("truce"), CommandSpec.builder()
                .description(Text.of("Invite or remove faction from truce"))
                .permission(PluginPermissions.TRUCE_COMMAND)
                .arguments(GenericArguments.onlyOne(new FactionArgument(this, Text.of("faction"))))
                .executor(new TruceCommand(this))
                .build());

        //Ally command
        SUBCOMMANDS.put(Collections.singletonList("ally"), CommandSpec.builder()
                .description(Text.of("Invite or remove faction from alliance"))
                .permission(PluginPermissions.ALLY_COMMAND)
                .arguments(GenericArguments.onlyOne(new FactionArgument(this, Text.of("faction"))))
                .executor(new AllyCommand(this))
                .build());

        //Enemy command
        SUBCOMMANDS.put(Collections.singletonList("enemy"), CommandSpec.builder()
                .description(Text.of("Declare or remove faction from war"))
                .permission(PluginPermissions.ENEMY_COMMAND)
                .arguments(GenericArguments.onlyOne(new FactionArgument(this, Text.of("faction"))))
                .executor(new EnemyCommand(this))
                .build());

        //Promote command
        SUBCOMMANDS.put(Collections.singletonList("promote"), CommandSpec.builder()
                .description(Text.of("Promotes the player to a higher rank"))
                .arguments(GenericArguments.onlyOne(new OwnFactionPlayerArgument(this, Text.of("player"))))
                .permission(PluginPermissions.PROMOTE_COMMAND)
                .executor(new PromoteCommand(this))
                .build());

        //Demote command
        SUBCOMMANDS.put(Collections.singletonList("demote"), CommandSpec.builder()
                .description(Text.of("Demotes the player to a lower rank"))
                .arguments(GenericArguments.onlyOne(new OwnFactionPlayerArgument(this, Text.of("player"))))
                .permission(PluginPermissions.DEMOTE_COMMAND)
                .executor(new DemoteCommand(this))
                .build());

        //Claims command.
        SUBCOMMANDS.put(Arrays.asList("claims", "listclaims"), CommandSpec.builder()
                .description(Text.of("Shows a list of faction's claims."))
                .permission(PluginPermissions.CLAIMS_LIST_COMMAND)
                .arguments(GenericArguments.optional(new FactionArgument(this, Text.of("faction"))))
                .executor(new ClaimsListCommand(this))
                .build());

        //Claim command.
        SUBCOMMANDS.put(Collections.singletonList("claim"), CommandSpec.builder()
                .description(Text.of("Claim a land for your faction"))
                .permission(PluginPermissions.CLAIM_COMMAND)
                .arguments(GenericArguments.optional(new FactionArgument(this, Text.of("faction"))))
                .executor(new ClaimCommand(this))
                .build());

        SUBCOMMANDS.put(Collections.singletonList("squareclaim"), CommandSpec.builder()
                .description(Text.of("Claim land in form of square with a given radius"))
                .permission(PluginPermissions.RADIUS_CLAIM_COMMAND)
                .arguments(GenericArguments.onlyOne(GenericArguments.integer(Text.of("radius"))))
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
                .executor(new UnclaimAllCommand(this))
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
                .arguments(GenericArguments.optional(new FactionArgument(this, Text.of("faction"))))
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
                .arguments(GenericArguments.optional(new FactionArgument(this, Text.of("faction"))))
                .executor(new CoordsCommand(this))
                .build());

        //Add SetPower Command
        SUBCOMMANDS.put(Collections.singletonList("setpower"), CommandSpec.builder()
                .description(Text.of("Set player's power"))
                .permission(PluginPermissions.SET_POWER_COMMAND)
                .arguments(GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))),
                        GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of("power"))))
                .executor(new SetPowerCommand(this))
                .build());

        //Add MaxPower Command
        SUBCOMMANDS.put(Collections.singletonList("maxpower"), CommandSpec.builder()
                .description(Text.of("Set player's maxpower"))
                .permission(PluginPermissions.MAX_POWER_COMMAND)
                .arguments(GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))),
                        GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of("power"))))
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
                .arguments(GenericArguments.onlyOne(new FactionPlayerArgument(this, Text.of("player"))))
                .executor(new SetLeaderCommand(this))
                .build());

        //Perms Command
        SUBCOMMANDS.put(Collections.singletonList("perms"), CommandSpec.builder()
                .description(Text.of("Set perms for members in faction."))
                .permission(PluginPermissions.PERMS_COMMAND)
                .executor(new PermsCommand(this))
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
                .permission(PluginPermissions.RENAME_COMMAND)
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("name"))))
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
                .arguments(GenericArguments.optional(new FactionArgument(this, Text.of("faction"))))
                .executor(new ChestCommand(this))
                .build());

        //Public Command
        SUBCOMMANDS.put(Collections.singletonList("public"), CommandSpec.builder()
                .description(Text.of("Sets faction as public or not"))
                .permission(PluginPermissions.PUBLIC_COMMAND)
                .arguments(GenericArguments.optional(new FactionArgument(this, Text.of("faction"))))
                .executor(new PublicCommand(this))
                .build());

        //Debug Command
        SUBCOMMANDS.put(Collections.singletonList("debug"), CommandSpec.builder()
                .description(Text.of("Toggles debug mode"))
                .permission(PluginPermissions.DEBUG_COMMAND)
                .executor(new DebugCommand(this))
                .build());

        //Backup Command
        SUBCOMMANDS.put(Collections.singletonList("createbackup"), CommandSpec.builder()
                .description(Text.of("Creates a backup of Eagle Factions data"))
                .permission(PluginPermissions.BACKUP_COMMAND)
                .executor(new BackupCommand(this))
                .build());

        //Restore Backup Command
        SUBCOMMANDS.put(Collections.singletonList("restorebackup"), CommandSpec.builder()
                .description(Text.of("Restores Eagle Factions data from the given backup file"))
                .permission(PluginPermissions.RESTORE_BACKUP_COMMAND)
                .arguments(GenericArguments.onlyOne(new BackupNameArgument(this, Text.of("filename"))))
                .executor(new RestoreBackupCommand(this))
                .build());

        //Regen Command
        SUBCOMMANDS.put(Collections.singletonList("regen"), CommandSpec.builder()
                .description(Text.of("Disband a faction and then regenerate the faction chunks"))
                .permission(PluginPermissions.REGEN_COMMAND)
                .arguments(GenericArguments.onlyOne(new FactionArgument(this, Text.of("faction"))))
                .executor(new RegenCommand(this))
                .build());

        //Access Player Command
        final CommandSpec accessPlayerCommand = CommandSpec.builder()
                .description(Text.of("Manages player access for current claim."))
                .permission(PluginPermissions.ACCESS_PLAYER_COMMAND)
                .arguments(GenericArguments.onlyOne(new OwnFactionPlayerArgument(this, Text.of("player"))))
                .executor(new AccessPlayerCommand(this))
                .build();

        //Access Faction Command
        final CommandSpec accessFactionCommand = CommandSpec.builder()
                .description(Text.of("Manages faction access for current claim."))
                .permission(PluginPermissions.ACCESS_FACTION_COMMAND)
                .executor(new AccessFactionCommand(this))
                .build();

        //Access Command
        SUBCOMMANDS.put(Collections.singletonList("access"), CommandSpec.builder()
                .description(Text.of("Manages internal faction access for current claim."))
                .permission(PluginPermissions.ACCESS_COMMAND)
                .executor(new AccessCommand(this))
                .child(accessPlayerCommand, "player", "p")
                .child(accessFactionCommand, "faction", "f")
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

    private void registerListeners()
    {
        //Sponge events
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
        Sponge.getEventManager().registerListeners(this, new NotifyNeighborBlockEventListener(this));

        //EF events
        Sponge.getEventManager().registerListeners(this, new FactionKickListener(this));
        Sponge.getEventManager().registerListeners(this, new FactionLeaveListener(this));
        Sponge.getEventManager().registerListeners(this, new FactionJoinListener(this));
    }

    public Configuration getConfiguration()
    {
        return this.configuration;
    }

    public PVPLogger getPVPLogger()
    {
        return this.pvpLogger;
    }

    public PermsManagerImpl getPermsManager()
    {
        return this.permsManager;
    }

    public PlayerManagerImpl getPlayerManager()
    {
        return playerManager;
    }

    public PowerManagerImpl getPowerManager()
    {
        return powerManager;
    }

    public ProtectionManager getProtectionManager()
    {
        return protectionManager;
    }

    public AttackLogic getAttackLogic()
    {
        return attackLogic;
    }

    public FactionLogic getFactionLogic()
    {
        return factionLogic;
    }

    public StorageManager getStorageManager()
    {
        return this.storageManager;
    }

    public InputStream getResourceAsStream(String fileName)
    {
        return this.getClass().getClassLoader().getResourceAsStream(fileName);
    }

    public URL getResource(final String fileName)
    {
        return this.getClass().getResource(fileName);
    }

    public void printInfo(final String message)
    {
        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.YELLOW, message));
    }

    private void setupConfigs()
    {
        configuration = new ConfigurationImpl(configDir, configAsset);
        MessageLoader messageLoader = MessageLoader.getInstance(this);
        pvpLogger = PVPLoggerImpl.getInstance(this);
    }

    private void setupManagers()
    {
        storageManager = new StorageManagerImpl(this, this.configuration.getStorageConfig(), this.configDir);
        playerManager = new PlayerManagerImpl(this.storageManager, this.factionLogic, this.getConfiguration().getFactionsConfig(), this.configuration.getPowerConfig());
        powerManager = new PowerManagerImpl(this.playerManager, this.configuration.getPowerConfig(), this.configDir);
        permsManager = new PermsManagerImpl();
        factionLogic = new FactionLogicImpl(this.playerManager, this.storageManager, this.getConfiguration().getFactionsConfig());
        attackLogic = new AttackLogicImpl(this.factionLogic, this.getConfiguration().getFactionsConfig());
        protectionManager = new ProtectionManagerImpl(this.factionLogic, this.permsManager, this.playerManager, this.configuration.getProtectionConfig(), this.configuration.getChatConfig(), this.configuration.getFactionsConfig());
    }

    private void startFactionsRemover()
    {
        //Do not turn on faction's remover if max inactive time == 0
        if(this.getConfiguration().getFactionsConfig().getMaxInactiveTime() == 0)
            return;

        EagleFactionsScheduler.getInstance().scheduleWithDelayedInterval(new FactionRemoverTask(eagleFactions), 0, TimeUnit.SECONDS, 1, TimeUnit.HOURS);
    }

    public EFPlaceholderService getEfPlaceholderService()
    {
        return efPlaceholderService;
    }
}
