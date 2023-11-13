package io.github.aquerr.eaglefactions;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.Configuration;
import io.github.aquerr.eaglefactions.api.entities.AcceptableInvite;
import io.github.aquerr.eaglefactions.api.entities.ChatEnum;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionChest;
import io.github.aquerr.eaglefactions.api.entities.FactionInvite;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.logic.AttackLogic;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.logic.PVPLogger;
import io.github.aquerr.eaglefactions.api.managers.InvitationManager;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.managers.PowerManager;
import io.github.aquerr.eaglefactions.api.managers.ProtectionManager;
import io.github.aquerr.eaglefactions.api.managers.RankManager;
import io.github.aquerr.eaglefactions.api.storage.StorageManager;
import io.github.aquerr.eaglefactions.commands.VersionCommand;
import io.github.aquerr.eaglefactions.commands.access.AccessCommand;
import io.github.aquerr.eaglefactions.commands.access.AccessFactionCommand;
import io.github.aquerr.eaglefactions.commands.access.AccessPlayerCommand;
import io.github.aquerr.eaglefactions.commands.access.NotAccessibleByFactionCommand;
import io.github.aquerr.eaglefactions.commands.access.OwnedByCommand;
import io.github.aquerr.eaglefactions.commands.admin.AdminCommand;
import io.github.aquerr.eaglefactions.commands.admin.DebugCommand;
import io.github.aquerr.eaglefactions.commands.admin.RegenCommand;
import io.github.aquerr.eaglefactions.commands.admin.ReloadCommand;
import io.github.aquerr.eaglefactions.commands.admin.SetFactionCommand;
import io.github.aquerr.eaglefactions.commands.admin.SetMaxPowerCommand;
import io.github.aquerr.eaglefactions.commands.admin.SetMaxPowerForAllCommand;
import io.github.aquerr.eaglefactions.commands.admin.SetPowerCommand;
import io.github.aquerr.eaglefactions.commands.args.BackupNameArgument;
import io.github.aquerr.eaglefactions.commands.args.FactionArgument;
import io.github.aquerr.eaglefactions.commands.args.FactionPlayerArgument;
import io.github.aquerr.eaglefactions.commands.args.OwnFactionPlayerArgument;
import io.github.aquerr.eaglefactions.commands.backup.BackupCommand;
import io.github.aquerr.eaglefactions.commands.backup.RestoreBackupCommand;
import io.github.aquerr.eaglefactions.commands.claiming.AutoClaimCommand;
import io.github.aquerr.eaglefactions.commands.claiming.ClaimCommand;
import io.github.aquerr.eaglefactions.commands.claiming.ClaimsListCommand;
import io.github.aquerr.eaglefactions.commands.claiming.FillCommand;
import io.github.aquerr.eaglefactions.commands.claiming.SquareClaimCommand;
import io.github.aquerr.eaglefactions.commands.claiming.UnclaimAllCommand;
import io.github.aquerr.eaglefactions.commands.claiming.UnclaimCommand;
import io.github.aquerr.eaglefactions.commands.general.AttackCommand;
import io.github.aquerr.eaglefactions.commands.general.AutoMapCommand;
import io.github.aquerr.eaglefactions.commands.general.ChatCommand;
import io.github.aquerr.eaglefactions.commands.general.ChestCommand;
import io.github.aquerr.eaglefactions.commands.general.CoordsCommand;
import io.github.aquerr.eaglefactions.commands.general.EagleFeatherCommand;
import io.github.aquerr.eaglefactions.commands.general.HelpCommand;
import io.github.aquerr.eaglefactions.commands.general.HomeCommand;
import io.github.aquerr.eaglefactions.commands.general.InfoCommand;
import io.github.aquerr.eaglefactions.commands.general.InviteCommand;
import io.github.aquerr.eaglefactions.commands.general.JoinCommand;
import io.github.aquerr.eaglefactions.commands.general.KickCommand;
import io.github.aquerr.eaglefactions.commands.general.LeaveCommand;
import io.github.aquerr.eaglefactions.commands.general.ListCommand;
import io.github.aquerr.eaglefactions.commands.general.MapCommand;
import io.github.aquerr.eaglefactions.commands.general.PlayerCommand;
import io.github.aquerr.eaglefactions.commands.general.TopCommand;
import io.github.aquerr.eaglefactions.commands.management.CreateCommand;
import io.github.aquerr.eaglefactions.commands.management.DescriptionCommand;
import io.github.aquerr.eaglefactions.commands.management.DisbandCommand;
import io.github.aquerr.eaglefactions.commands.management.MotdCommand;
import io.github.aquerr.eaglefactions.commands.management.PermsCommand;
import io.github.aquerr.eaglefactions.commands.management.PublicCommand;
import io.github.aquerr.eaglefactions.commands.management.RenameCommand;
import io.github.aquerr.eaglefactions.commands.management.SetHomeCommand;
import io.github.aquerr.eaglefactions.commands.management.TagColorCommand;
import io.github.aquerr.eaglefactions.commands.management.TagCommand;
import io.github.aquerr.eaglefactions.commands.rank.DemoteCommand;
import io.github.aquerr.eaglefactions.commands.rank.PromoteCommand;
import io.github.aquerr.eaglefactions.commands.rank.SetLeaderCommand;
import io.github.aquerr.eaglefactions.commands.relation.AllyCommand;
import io.github.aquerr.eaglefactions.commands.relation.EnemyCommand;
import io.github.aquerr.eaglefactions.commands.relation.TruceCommand;
import io.github.aquerr.eaglefactions.config.ConfigurationImpl;
import io.github.aquerr.eaglefactions.entities.FactionImpl;
import io.github.aquerr.eaglefactions.entities.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.integrations.bstats.Metrics;
import io.github.aquerr.eaglefactions.integrations.dynmap.DynmapService;
import io.github.aquerr.eaglefactions.integrations.placeholderapi.EFPlaceholderService;
import io.github.aquerr.eaglefactions.integrations.ultimatechat.UltimateChatService;
import io.github.aquerr.eaglefactions.listeners.BlockBreakListener;
import io.github.aquerr.eaglefactions.listeners.BlockPlaceListener;
import io.github.aquerr.eaglefactions.listeners.ChatMessageListener;
import io.github.aquerr.eaglefactions.listeners.EntityDamageListener;
import io.github.aquerr.eaglefactions.listeners.EntitySpawnListener;
import io.github.aquerr.eaglefactions.listeners.ExplosionListener;
import io.github.aquerr.eaglefactions.listeners.ModifyBlockListener;
import io.github.aquerr.eaglefactions.listeners.NotifyNeighborBlockListener;
import io.github.aquerr.eaglefactions.listeners.PlayerDeathListener;
import io.github.aquerr.eaglefactions.listeners.PlayerDisconnectListener;
import io.github.aquerr.eaglefactions.listeners.PlayerInteractListener;
import io.github.aquerr.eaglefactions.listeners.PlayerJoinListener;
import io.github.aquerr.eaglefactions.listeners.PlayerMoveListener;
import io.github.aquerr.eaglefactions.listeners.SendCommandListener;
import io.github.aquerr.eaglefactions.integrations.ultimatechat.listener.UltimateChatMessageListener;
import io.github.aquerr.eaglefactions.listeners.faction.FactionJoinListener;
import io.github.aquerr.eaglefactions.listeners.faction.FactionKickListener;
import io.github.aquerr.eaglefactions.listeners.faction.FactionLeaveListener;
import io.github.aquerr.eaglefactions.logic.AttackLogicImpl;
import io.github.aquerr.eaglefactions.logic.FactionLogicImpl;
import io.github.aquerr.eaglefactions.logic.PVPLoggerImpl;
import io.github.aquerr.eaglefactions.managers.InvitationManagerImpl;
import io.github.aquerr.eaglefactions.managers.PermsManagerImpl;
import io.github.aquerr.eaglefactions.managers.PlayerManagerImpl;
import io.github.aquerr.eaglefactions.managers.PowerManagerImpl;
import io.github.aquerr.eaglefactions.managers.ProtectionManagerImpl;
import io.github.aquerr.eaglefactions.managers.RankManagerImpl;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.scheduling.EagleFactionsScheduler;
import io.github.aquerr.eaglefactions.scheduling.FactionRemoverTask;
import io.github.aquerr.eaglefactions.storage.StorageManagerImpl;
import io.github.aquerr.eaglefactions.storage.serializers.ClaimSetTypeSerializer;
import io.github.aquerr.eaglefactions.storage.serializers.ClaimTypeSerializer;
import io.github.aquerr.eaglefactions.storage.serializers.SlotItemListTypeSerializer;
import io.github.aquerr.eaglefactions.storage.serializers.SlotItemTypeSerializer;
import io.github.aquerr.eaglefactions.version.VersionChecker;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.asset.AssetId;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Plugin(id = PluginInfo.ID, name = PluginInfo.NAME, version = PluginInfo.VERSION, description = PluginInfo.DESCRIPTION, authors = PluginInfo.AUTHOR,
        dependencies = {@Dependency(id = "placeholderapi", optional = true)}, url = PluginInfo.URL)
public class EagleFactionsPlugin implements EagleFactions
{
    //TODO: Convert these fields to instance fields.
    public static final Map<List<String>, CommandSpec> SUBCOMMANDS = new HashMap<>();
    public static final List<FactionInvite> INVITE_LIST = new LinkedList<>();
    public static final List<AcceptableInvite> RELATION_INVITES = new LinkedList<>();
    public static final List<UUID> AUTO_CLAIM_LIST = new LinkedList<>();
    public static final List<UUID> AUTO_MAP_LIST = new LinkedList<>();
    public static final Map<UUID, String> REGEN_CONFIRMATION_MAP = new HashMap<>();
    public static final Map<String, Integer> ATTACKED_FACTIONS = new HashMap<>();
    public static final Map<UUID, Integer> BLOCKED_HOME = new HashMap<>();
    public static final Map<UUID, ChatEnum> CHAT_LIST = new HashMap<>();
    public static final Map<UUID, Integer> HOME_COOLDOWN_PLAYERS = new HashMap<>();
    public static final List<UUID> DEBUG_MODE_PLAYERS = new LinkedList<>();

    private static EagleFactionsPlugin eagleFactions;

    private Configuration configuration;
    private PVPLogger pvpLogger;
    private PlayerManager playerManager;
    private PermsManager permsManager;
    private ProtectionManager protectionManager;
    private PowerManager powerManager;
    private AttackLogic attackLogic;
    private FactionLogic factionLogic;
    private InvitationManager invitationManager;
    private RankManager rankManager;
    private StorageManager storageManager;

    private boolean isDisabled = false;

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
    private UltimateChatService ultimateChatService;

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
        try
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

            EventRunner.init(Sponge.getEventManager());

            //Display some info text in the console.
            Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.GREEN, "=========================================="));
            Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.AQUA, "Eagle Factions", TextColors.WHITE, " is ready to use!"));
            Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.WHITE, "Thank you for choosing this plugin!"));
            Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.WHITE, "Current version: " + PluginInfo.VERSION));
            Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.WHITE, "Have a great time with Eagle Factions! :D"));
            Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.GREEN, "=========================================="));

            CompletableFuture.runAsync(this::checkVersionAndInform);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            disablePlugin();
        }
    }

    @Listener
    public void onGameStarting(final GameStartingServerEvent event)
    {
        if (isDisabled)
            return;

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

        setDefaultPermissions();

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

        if (isUltimateChatLoaded())
        {
            this.ultimateChatService = new UltimateChatService(this.configuration.getChatConfig());
            this.ultimateChatService.registerTags();
        }
    }

    public void setDefaultPermissions()
    {
        final Optional<PermissionService> optionalPermissionService = Sponge.getServiceManager().provide(PermissionService.class);
        if(optionalPermissionService.isPresent())
        {
            final PermissionService permissionService = optionalPermissionService.get();
            final Map<String, Boolean> permissionContext = permissionService.getDefaults().getSubjectData().getPermissions(SubjectData.GLOBAL_CONTEXT);
            boolean hasEagleFactionsPermission = false;
            for (final String permission : permissionContext.keySet())
            {
                if (permission.contains("eaglefactions"))
                {
                    hasEagleFactionsPermission = true;
                    break;
                }
            }

            //If eaglefactions already exists then don't add default permissions.
            if (!hasEagleFactionsPermission)
            {
                permissionService.getDefaults().getSubjectData().setPermission(SubjectData.GLOBAL_CONTEXT, "eaglefactions.player", Tristate.TRUE);
            }
        }
    }

    // Start removing inactive factions when server is fully started.
    @Listener
    public void onGameLoad(final GameStartedServerEvent event)
    {
        if (isDisabled)
            return;

        startFactionsRemover();
    }

    @Listener
    public void onReload(final GameReloadEvent event)
    {
        if (isDisabled)
            return;

        this.configuration.reloadConfiguration();
        this.storageManager.reloadStorage();

        if (this.configuration.getDynmapConfig().isDynmapIntegrationEnabled())
        {
            this.dynmapService.reload();
        }

        if(event.getSource() instanceof Player)
        {
            Player player = (Player)event.getSource();
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, Messages.CONFIG_HAS_BEEN_RELOADED));
        }
    }

    @Override
    public Configuration getConfiguration()
    {
        return this.configuration;
    }

    @Override
    public PVPLogger getPVPLogger()
    {
        return this.pvpLogger;
    }

    @Override
    public PermsManager getPermsManager()
    {
        return this.permsManager;
    }

    @Override
    public PlayerManager getPlayerManager()
    {
        return playerManager;
    }

    @Override
    public PowerManager getPowerManager()
    {
        return powerManager;
    }

    @Override
    public ProtectionManager getProtectionManager()
    {
        return protectionManager;
    }

    @Override
    public AttackLogic getAttackLogic()
    {
        return attackLogic;
    }

    @Override
    public FactionLogic getFactionLogic()
    {
        return factionLogic;
    }

    @Override
    public StorageManager getStorageManager()
    {
        return this.storageManager;
    }

    @Override
    public InvitationManager getInvitationManager()
    {
        return this.invitationManager;
    }

    @Override
    public RankManager getRankManager()
    {
        return this.rankManager;
    }

    public EFPlaceholderService getEfPlaceholderService()
    {
        return this.efPlaceholderService;
    }

    public DynmapService getDynmapService()
    {
        return this.dynmapService;
    }

    @Override
    public Faction.Builder getBuilderForFaction(String name, Text tag, UUID leader)
    {
        return new FactionImpl.BuilderImpl(name, tag, leader);
    }

    @Override
    public FactionPlayer createNewFactionPlayer(final String playerName, final UUID uniqueId, final String factionName, final float power, final float maxpower, final boolean diedInWarZone)
    {
        return new FactionPlayerImpl(playerName, uniqueId, factionName, power, maxpower, diedInWarZone);
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
        this.storageManager = new StorageManagerImpl(this, this.configuration.getStorageConfig(), this.configDir);
        this.playerManager = new PlayerManagerImpl(this.storageManager, this.factionLogic, this.getConfiguration().getFactionsConfig(), this.configuration.getPowerConfig());
        this.powerManager = new PowerManagerImpl(this.playerManager, this.configuration.getPowerConfig());
        this.permsManager = new PermsManagerImpl();
        this.factionLogic = new FactionLogicImpl(this.playerManager, this.storageManager, this.getConfiguration().getFactionsConfig());
        this.attackLogic = new AttackLogicImpl(this.factionLogic, this.getConfiguration().getFactionsConfig());
        this.protectionManager = new ProtectionManagerImpl(this.factionLogic, this.permsManager, this.playerManager, this.configuration.getProtectionConfig(), this.configuration.getChatConfig(), this.configuration.getFactionsConfig());
        this.invitationManager = new InvitationManagerImpl(this.storageManager, this.factionLogic, this.playerManager);
        this.rankManager = new RankManagerImpl(this.factionLogic, this.storageManager);
    }

    private void startFactionsRemover()
    {
        //Do not turn on faction's remover if max inactive time == 0
        if(this.getConfiguration().getFactionsConfig().getMaxInactiveTime() == 0)
            return;

        EagleFactionsScheduler.getInstance().scheduleWithDelayedIntervalAsync(new FactionRemoverTask(eagleFactions), 0, TimeUnit.SECONDS, 1, TimeUnit.HOURS);
    }

    private void registerTypeSerializers()
    {
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(Claim.class), new ClaimTypeSerializer());
        TypeSerializers.getDefaultSerializers().registerType(new TypeToken<Set<Claim>>(){}, new ClaimSetTypeSerializer());
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(FactionChest.SlotItem.class), new SlotItemTypeSerializer());
        TypeSerializers.getDefaultSerializers().registerType(new TypeToken<List<FactionChest.SlotItem>>(){}, new SlotItemListTypeSerializer());
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
        Sponge.getServiceManager().setProvider(this, InvitationManager.class, this.invitationManager);
        Sponge.getServiceManager().setProvider(this, RankManager.class, this.rankManager);
    }

    private void initializeCommands()
    {
        //Help command should display all possible commands in plugin.
        SUBCOMMANDS.put(Collections.singletonList("help"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_HELP_DESC))
                .permission(PluginPermissions.HELP_COMMAND)
                .arguments(GenericArguments.optional(GenericArguments.integer(Text.of("page"))))
                .executor(new HelpCommand(this))
                .build());

        //Create faction command.
        SUBCOMMANDS.put(Arrays.asList("c", "create"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_CREATE_DESC))
                .permission(PluginPermissions.CREATE_COMMAND)
                .arguments(GenericArguments.string(Text.of("tag")),
                        GenericArguments.string(Text.of("name")))
                .executor(new CreateCommand(this))
                .build());

        //Disband faction command.
        SUBCOMMANDS.put(Collections.singletonList("disband"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_DISBAND_DESC))
                .permission(PluginPermissions.DISBAND_COMMAND)
                .arguments(GenericArguments.optional(new FactionArgument(this, Text.of("faction"))))
                .executor(new DisbandCommand(this))
                .build());

        //List all factions.
        SUBCOMMANDS.put(Collections.singletonList("list"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_LIST_DESC))
                .permission(PluginPermissions.LIST_COMMAND)
                .executor(new ListCommand(this))
                .build());

        //Invite a player to the faction.
        SUBCOMMANDS.put(Collections.singletonList("invite"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_INVITE_DESC))
                .permission(PluginPermissions.INVITE_COMMAND)
                .arguments(GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))))
                .executor(new InviteCommand(this))
                .build());

        //Kick a player from the faction.
        SUBCOMMANDS.put(Collections.singletonList("kick"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_KICK_DESC))
                .permission(PluginPermissions.KICK_COMMAND)
                .arguments(GenericArguments.onlyOne(new FactionPlayerArgument(this, Text.of("player"))))
                .executor(new KickCommand(this))
                .build());

        //Join faction command
        SUBCOMMANDS.put(Arrays.asList("j", "join"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_JOIN_DESC))
                .permission(PluginPermissions.JOIN_COMMAND)
                .arguments(new FactionArgument(this, Text.of("faction")))
                .executor(new JoinCommand(this))
                .build());

        //Leave faction command
        SUBCOMMANDS.put(Collections.singletonList("leave"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_LEAVE_DESC))
                .permission(PluginPermissions.LEAVE_COMMAND)
                .executor(new LeaveCommand(this))
                .build());

        //Version command
        SUBCOMMANDS.put(Arrays.asList("v", "version"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_VERSION_DESC))
                .permission(PluginPermissions.VERSION_COMMAND)
                .executor(new VersionCommand(this))
                .build());

        //Info command. Shows info about a faction.
        SUBCOMMANDS.put(Arrays.asList("i", "info"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_INFO_DESC))
                .arguments(GenericArguments.optional(new FactionArgument(this, Text.of("faction"))))
                .executor(new InfoCommand(this))
                .build());

        //Player command. Shows info about a player. (its factions etc.)
        SUBCOMMANDS.put(Arrays.asList("p", "player"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_PLAYER_DESC))
                .permission(PluginPermissions.PLAYER_COMMAND)
                .arguments(GenericArguments.optional(new FactionPlayerArgument(this, Text.of("player"))))
                .executor(new PlayerCommand(this))
                .build());

        //Truce command
        SUBCOMMANDS.put(Collections.singletonList("truce"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_TRUCE_DESC))
                .permission(PluginPermissions.TRUCE_COMMAND)
                .arguments(GenericArguments.onlyOne(new FactionArgument(this, Text.of("faction"))))
                .executor(new TruceCommand(this))
                .build());

        //Ally command
        SUBCOMMANDS.put(Collections.singletonList("ally"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_ALLY_DESC))
                .permission(PluginPermissions.ALLY_COMMAND)
                .arguments(GenericArguments.onlyOne(new FactionArgument(this, Text.of("faction"))))
                .executor(new AllyCommand(this))
                .build());

        //Enemy command
        SUBCOMMANDS.put(Collections.singletonList("enemy"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_ENEMY_DESC))
                .permission(PluginPermissions.ENEMY_COMMAND)
                .arguments(GenericArguments.onlyOne(new FactionArgument(this, Text.of("faction"))))
                .executor(new EnemyCommand(this))
                .build());

        //Promote command
        SUBCOMMANDS.put(Collections.singletonList("promote"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_PROMOTE_DESC))
                .arguments(GenericArguments.onlyOne(new OwnFactionPlayerArgument(this, Text.of("player"))))
                .permission(PluginPermissions.PROMOTE_COMMAND)
                .executor(new PromoteCommand(this))
                .build());

        //Demote command
        SUBCOMMANDS.put(Collections.singletonList("demote"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_DEMOTE_DESC))
                .arguments(GenericArguments.onlyOne(new OwnFactionPlayerArgument(this, Text.of("player"))))
                .permission(PluginPermissions.DEMOTE_COMMAND)
                .executor(new DemoteCommand(this))
                .build());

        //Claims command
        SUBCOMMANDS.put(Arrays.asList("claims", "listclaims"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_LIST_CLAIMS_DESC))
                .permission(PluginPermissions.CLAIMS_LIST_COMMAND)
                .arguments(GenericArguments.optional(new FactionArgument(this, Text.of("faction"))))
                .executor(new ClaimsListCommand(this))
                .build());

        //Claim command
        SUBCOMMANDS.put(Collections.singletonList("claim"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_CLAIM_DESC))
                .permission(PluginPermissions.CLAIM_COMMAND)
                .arguments(GenericArguments.optional(new FactionArgument(this, Text.of("faction"))))
                .executor(new ClaimCommand(this))
                .build());

        //Square Claim command
        SUBCOMMANDS.put(Collections.singletonList("squareclaim"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_SQUARE_CLAIM_DESC))
                .permission(PluginPermissions.RADIUS_CLAIM_COMMAND)
                .arguments(GenericArguments.onlyOne(GenericArguments.integer(Text.of("radius"))))
                .executor(new SquareClaimCommand(this))
                .build());

        // Fill Command
        SUBCOMMANDS.put(Collections.singletonList("fillclaim"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_FILL_CLAIM_DESC))
                .permission(PluginPermissions.COMMAND_FILL_CLAIM_COMMAND)
                .executor(new FillCommand(this))
                .build());

        //Unclaim command
        SUBCOMMANDS.put(Collections.singletonList("unclaim"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_UNCLAIM_DESC))
                .permission(PluginPermissions.UNCLAIM_COMMAND)
                .executor(new UnclaimCommand(this))
                .build());

        //Unclaimall Command
        SUBCOMMANDS.put(Collections.singletonList("unclaimall"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_UNCLAIM_ALL_DESC))
                .permission(PluginPermissions.UNCLAIM_ALL_COMMAND)
                .executor(new UnclaimAllCommand(this))
                .build());

        //Map command
        SUBCOMMANDS.put(Collections.singletonList("map"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_MAP_DESC))
                .permission(PluginPermissions.MAP_COMMAND)
                .executor(new MapCommand(this))
                .build());

        //Sethome command
        SUBCOMMANDS.put(Collections.singletonList("sethome"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_SET_HOME_DESC))
                .permission(PluginPermissions.SET_HOME_COMMAND)
                .executor(new SetHomeCommand(this))
                .build());

        //Home command
        SUBCOMMANDS.put(Collections.singletonList("home"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_HOME_DESC))
                .permission(PluginPermissions.HOME_COMMAND)
                .arguments(GenericArguments.optional(new FactionArgument(this, Text.of("faction"))))
                .executor(new HomeCommand(this))
                .build());

        //Autoclaim command.
        SUBCOMMANDS.put(Collections.singletonList("autoclaim"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_AUTO_CLAIM_DESC))
                .permission(PluginPermissions.AUTO_CLAIM_COMMAND)
                .executor(new AutoClaimCommand(this))
                .build());

        //Automap command
        SUBCOMMANDS.put(Collections.singletonList("automap"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_AUTO_MAP_DESC))
                .permission(PluginPermissions.AUTO_MAP_COMMAND)
                .executor(new AutoMapCommand(this))
                .build());

        //Coords Command
        SUBCOMMANDS.put(Collections.singletonList("coords"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_COORDS_DESC))
                .permission(PluginPermissions.COORDS_COMMAND)
                .arguments(GenericArguments.optional(new FactionArgument(this, Text.of("faction"))))
                .executor(new CoordsCommand(this))
                .build());

        //Admin command
        SUBCOMMANDS.put(Collections.singletonList("admin"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_ADMIN_DESC))
                .permission(PluginPermissions.ADMIN_MODE_COMMAND)
                .executor(new AdminCommand(this))
                .build());

        SUBCOMMANDS.put(Collections.singletonList("setfaction"), CommandSpec.builder()
                .description(Text.of(Messages.SET_FACTION_COMMAND))
                .permission(PluginPermissions.SET_FACTION_COMMAND)
                .arguments(GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))),
                        GenericArguments.onlyOne(new FactionArgument(this, Text.of("faction"))),
                        GenericArguments.onlyOne(GenericArguments.enumValue(Text.of("rank"), FactionMemberType.class)))
                .executor(new SetFactionCommand(this))
                .build());

        //SetPower Command
        SUBCOMMANDS.put(Collections.singletonList("setpower"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_SET_POWER_DESC))
                .permission(PluginPermissions.SET_POWER_COMMAND)
                .arguments(GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))),
                        GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of("power"))))
                .executor(new SetPowerCommand(this))
                .build());

        //MaxPower Command
        SUBCOMMANDS.put(Collections.singletonList("setmaxpower"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_SET_MAX_POWER_DESC))
                .permission(PluginPermissions.MAX_POWER_COMMAND)
                .arguments(GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))),
                        GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of("power"))))
                .executor(new SetMaxPowerCommand(this))
                .build());

        // MaxPowerAll Command
        SUBCOMMANDS.put(Collections.singletonList("setmaxpower_forall"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_SET_MAX_POWER_FOR_ALL_DESC))
                .permission(PluginPermissions.MAX_POWER_FOR_ALL_COMMAND)
                .arguments(GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of("power"))))
                .executor(new SetMaxPowerForAllCommand(this))
                .build());

        //Attack Command
        SUBCOMMANDS.put(Collections.singletonList("attack"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_ATTACK_DESC))
                .permission(PluginPermissions.ATTACK_COMMAND)
                .executor(new AttackCommand(this))
                .build());

        //Reload Command
        SUBCOMMANDS.put(Collections.singletonList("reload"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_RELOAD_DESC))
                .permission(PluginPermissions.RELOAD_COMMAND)
                .executor(new ReloadCommand(this))
                .build());

        //Chat Command
        SUBCOMMANDS.put(Collections.singletonList("chat"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_CHAT_DESC))
                .permission(PluginPermissions.CHAT_COMMAND)
                .arguments(GenericArguments.optional(GenericArguments.enumValue(Text.of("chat"), ChatEnum.class)))
                .executor(new ChatCommand(this))
                .build());

        //Top Command
        SUBCOMMANDS.put(Collections.singletonList("top"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_TOP_DESC))
                .permission(PluginPermissions.TOP_COMMAND)
                .executor(new TopCommand(this))
                .build());

        //Setleader Command
        SUBCOMMANDS.put(Collections.singletonList("setleader"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_SET_LEADER_DESC))
                .permission(PluginPermissions.SET_LEADER_COMMAND)
                .arguments(GenericArguments.onlyOne(new FactionPlayerArgument(this, Text.of("player"))))
                .executor(new SetLeaderCommand(this))
                .build());

        //Perms Command
        SUBCOMMANDS.put(Collections.singletonList("perms"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_PERMS_DESC))
                .permission(PluginPermissions.PERMS_COMMAND)
                .executor(new PermsCommand(this))
                .build());

        //TagColor Command
        SUBCOMMANDS.put(Collections.singletonList("tagcolor"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_TAG_COLOR_DESC))
                .permission(PluginPermissions.TAG_COLOR_COMMAND)
                .arguments(GenericArguments.catalogedElement(Text.of("color"), TextColor.class))
                .executor(new TagColorCommand(this))
                .build());

        //Rename Command
        SUBCOMMANDS.put(Collections.singletonList("rename"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_RENAME_DESC))
                .permission(PluginPermissions.RENAME_COMMAND)
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("name"))))
                .executor(new RenameCommand(this))
                .build());

        //Tag Command
        SUBCOMMANDS.put(Collections.singletonList("tag"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_TAG_DESC))
                .permission(PluginPermissions.TAG_COMMAND)
                .arguments(GenericArguments.string(Text.of("tag")))
                .executor(new TagCommand(this))
                .build());

        //Description Command
        SUBCOMMANDS.put(Arrays.asList("desc", "description"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_DESC_DESC))
                .permission(PluginPermissions.DESCRIPTION_COMMAND)
                .arguments(GenericArguments.remainingJoinedStrings(Text.of("description")))
                .executor(new DescriptionCommand(this))
                .build());
        //Motd Command
        SUBCOMMANDS.put(Collections.singletonList("motd"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_MOTD_DESC))
                .permission(PluginPermissions.MOTD_COMMAND)
                .arguments(GenericArguments.remainingJoinedStrings(Text.of("motd")))
                .executor(new MotdCommand(this))
                .build());

        //EagleFeather Command
        SUBCOMMANDS.put(Collections.singletonList("feather"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_FEATHER_DESC))
                .permission(PluginPermissions.FEATHER_COMMAND)
                .executor(new EagleFeatherCommand(this))
                .build());

        //Chest Command
        SUBCOMMANDS.put(Collections.singletonList("chest"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_CHEST_DESC))
                .permission(PluginPermissions.CHEST_COMMAND)
                .arguments(GenericArguments.optional(new FactionArgument(this, Text.of("faction"))))
                .executor(new ChestCommand(this))
                .build());

        //Public Command
        SUBCOMMANDS.put(Collections.singletonList("public"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_PUBLIC_DESC))
                .permission(PluginPermissions.PUBLIC_COMMAND)
                .arguments(GenericArguments.optional(new FactionArgument(this, Text.of("faction"))))
                .executor(new PublicCommand(this))
                .build());

        //Debug Command
        SUBCOMMANDS.put(Collections.singletonList("debug"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_DEBUG_DESC))
                .permission(PluginPermissions.DEBUG_COMMAND)
                .executor(new DebugCommand(this))
                .build());

        //Backup Command
        SUBCOMMANDS.put(Collections.singletonList("createbackup"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_CREATE_BACKUP_DESC))
                .permission(PluginPermissions.BACKUP_COMMAND)
                .executor(new BackupCommand(this))
                .build());

        //Restore Backup Command
        SUBCOMMANDS.put(Collections.singletonList("restorebackup"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_RESTORE_BACKUP_DESC))
                .permission(PluginPermissions.RESTORE_BACKUP_COMMAND)
                .arguments(GenericArguments.onlyOne(new BackupNameArgument(this, Text.of("filename"))))
                .executor(new RestoreBackupCommand(this))
                .build());

        //Regen Command
        SUBCOMMANDS.put(Collections.singletonList("regen"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_REGEN_DESC))
                .permission(PluginPermissions.REGEN_COMMAND)
                .arguments(GenericArguments.onlyOne(new FactionArgument(this, Text.of("faction"))))
                .executor(new RegenCommand(this))
                .build());

        //Access Player Command
        final CommandSpec accessPlayerCommand = CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_ACCESS_PLAYER_DESC))
                .permission(PluginPermissions.ACCESS_PLAYER_COMMAND)
                .arguments(GenericArguments.onlyOne(new OwnFactionPlayerArgument(this, Text.of("player"))))
                .executor(new AccessPlayerCommand(this))
                .build();

        //Access Faction Command
        final CommandSpec accessFactionCommand = CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_ACCESS_FACTION_DESC))
                .permission(PluginPermissions.ACCESS_FACTION_COMMAND)
                .executor(new AccessFactionCommand(this))
                .build();

        //Access OwnedBy Command
        final CommandSpec accessOwnedByCommand = CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_ACCESS_OWNED_BY_DESC))
                .permission(PluginPermissions.ACCESS_OWNED_BY_COMMAND)
                .arguments(GenericArguments.onlyOne(new OwnFactionPlayerArgument(this, Text.of("player"))))
                .executor(new OwnedByCommand(this))
                .build();

        //Access AccessibleByFaction Command
        final CommandSpec accessibleByFactionCommand = CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_ACCESS_ACCESSIBLE_BY_FACTION_DESC))
                .permission(PluginPermissions.ACCESS_NOT_ACCESSIBLE_BY_FACTION_COMMAND)
                .executor(new NotAccessibleByFactionCommand(this))
                .build();

        //Access Command
        SUBCOMMANDS.put(Collections.singletonList("access"), CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_ACCESS_DESC))
                .permission(PluginPermissions.ACCESS_COMMAND)
                .executor(new AccessCommand(this))
                .child(accessPlayerCommand, "player", "p")
                .child(accessFactionCommand, "faction", "f")
                .child(accessOwnedByCommand, "ownedBy")
                .child(accessibleByFactionCommand, "notAccessibleByFaction")
                .build());

        //Build all commands
        CommandSpec commandEagleFactions = CommandSpec.builder()
                .description(Text.of(Messages.COMMAND_HELP_DESC))
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
        Sponge.getEventManager().registerListeners(this, new EntitySpawnListener(this));
        Sponge.getEventManager().registerListeners(this, new PlayerDisconnectListener(this));
        Sponge.getEventManager().registerListeners(this, new SendCommandListener(this));
        Sponge.getEventManager().registerListeners(this, new ExplosionListener(this));
        Sponge.getEventManager().registerListeners(this, new ModifyBlockListener(this));
        Sponge.getEventManager().registerListeners(this, new NotifyNeighborBlockListener(this));

        // Chat
        if(isUltimateChatLoaded())
        {
            Sponge.getEventManager().registerListeners(this, new UltimateChatMessageListener(this));
        }
        else // Sponge/Vanilla
        {
            Sponge.getEventManager().registerListeners(this, new ChatMessageListener(this));
        }

        //EF events
        Sponge.getEventManager().registerListeners(this, new FactionKickListener(this));
        Sponge.getEventManager().registerListeners(this, new FactionLeaveListener(this));
        Sponge.getEventManager().registerListeners(this, new FactionJoinListener(this));
    }

    private void disablePlugin()
    {
        this.isDisabled = true;
        Sponge.getEventManager().unregisterPluginListeners(this);
        Sponge.getCommandManager().getOwnedBy(this).forEach(Sponge.getCommandManager()::removeMapping);
        Sponge.getServer().getConsole().sendMessage(PluginInfo.ERROR_PREFIX.concat(Text.of(TextColors.RED, "EagleFactions has been disabled!")));
    }

    private boolean isUltimateChatLoaded() {
        return Sponge.getPluginManager().isLoaded("ultimatechat");
    }

    private void checkVersionAndInform()
    {
        if (!this.configuration.getVersionConfig().shouldPerformVersionCheck())
        {
            Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.GOLD, "Version check: Disabled."));
            return;
        }

        if(!VersionChecker.getInstance().isLatest(PluginInfo.VERSION))
        {
            Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.GOLD, "Hey! A new version of ", TextColors.AQUA, PluginInfo.NAME, TextColors.GOLD, " is available online!"));
            Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.GREEN, "=========================================="));
        }
    }
}
