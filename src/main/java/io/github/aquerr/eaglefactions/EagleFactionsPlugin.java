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
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.commands.args.FactionArgument;
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
import io.github.aquerr.eaglefactions.integrations.dynmap.DynmapService;
import io.github.aquerr.eaglefactions.integrations.placeholderapi.EFPlaceholderService;
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
import io.github.aquerr.eaglefactions.version.VersionChecker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.parameter.CommonParameters;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.RegisterFactoryEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.resource.Resource;
import org.spongepowered.api.resource.ResourcePath;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.IOException;
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

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;


@Plugin(PluginInfo.ID)
//@Plugin(id = PluginInfo.ID, name = PluginInfo.NAME, version = PluginInfo.VERSION, description = PluginInfo.DESCRIPTION, authors = PluginInfo.AUTHOR,
//        dependencies = {@Dependency(id = "placeholderapi", optional = true)}, url = PluginInfo.URL)
public class EagleFactionsPlugin implements EagleFactions
{
    //TODO: Convert these fields to instance fields.
    public static final Map<List<String>, Command.Parameterized> SUBCOMMANDS = new HashMap<>();
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

    private final PluginContainer pluginContainer;
    private final Path configDir;

//    @Inject
//    @AssetId("Settings.conf")
//    private Asset configAsset;

    //Integrations
//    @Inject
//    private Metrics metrics;
    private EFPlaceholderService efPlaceholderService;
    private DynmapService dynmapService;
//    private UltimateChatService ultimateChatService;

    public static EagleFactionsPlugin getPlugin()
    {
        return eagleFactions;
    }

    @Inject
    public EagleFactionsPlugin(final PluginContainer pluginContainer, @ConfigDir(sharedRoot = false) final Path configDir)
    {
        eagleFactions = this;
        this.pluginContainer = pluginContainer;
        this.configDir = configDir;
    }

    public Path getConfigDir()
    {
        return configDir;
    }

    @Listener
    public void onServerInitialization(final ConstructPluginEvent event)
    {
        try
        {
            Sponge.server().sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Preparing wings...", AQUA)));

            setupConfigs();

            Sponge.server().sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Configs loaded.", AQUA)));

            Sponge.server().sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Loading managers and cache...", AQUA)));
            setupManagers();

            Sponge.server().sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Managers loaded.", AQUA)));


            registerListeners();

            EventRunner.init(Sponge.eventManager());

            //Display some info text in the console.
            Sponge.server().sendMessage(text("==========================================", GREEN));
            Sponge.server().sendMessage(text("Eagle Factions", AQUA).append(text(" is ready to use!", WHITE)));
            Sponge.server().sendMessage(text("Thank you for choosing this plugin!", WHITE));
            Sponge.server().sendMessage(text("Current version: " + PluginInfo.VERSION, WHITE));
            Sponge.server().sendMessage(text("Have a great time with Eagle Factions! :D", WHITE));
            Sponge.server().sendMessage(text("==========================================", GREEN));

            CompletableFuture.runAsync(() ->
            {
                if(!VersionChecker.isLatest(PluginInfo.VERSION))
                {
                    Sponge.server().sendMessage(Component.join(JoinConfiguration.noSeparators(),
                            text("Hey! A new version of ", GOLD),
                            text(PluginInfo.NAME, AQUA),
                            text(" is available online!", GOLD)));
                    Sponge.server().sendMessage(text("==========================================", GREEN));
                }
            });        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            disablePlugin();
        }

        initializeIntegrations();
    }

    @Listener
    public void onFactoryRegister(RegisterFactoryEvent event)
    {
        registerAPI(event);
    }

    private void initializeIntegrations()
    {
        if (isDisabled)
            return;

//        try
//        {
//            Class placeholderInterface = Class.forName("me.rojo8399.placeholderapi.PlaceholderService");
//            Optional<PlaceholderAPIPlugin> placeholderService1 = Sponge.game().factoryProvider().provide(placeholderInterface);
//            placeholderService1.ifPresent(placeholderService -> {
//                printInfo("Found PlaceholderAPI! Registering placeholders...");
//                efPlaceholderService = EFPlaceholderService.getInstance(this, placeholderService);
//                printInfo("Registered Eagle Factions' placeholders.");
//            });
//        }
//        catch(final NoClassDefFoundError | ClassNotFoundException error)
//        {
//            printInfo("PlaceholderAPI could not be found. Skipping addition of placeholders.");
//        }

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
//            this.ultimateChatService = new UltimateChatService(this.configuration.getChatConfig());
//            this.ultimateChatService.registerTags();
        }
    }

    @Listener
    public void onRegisterCommand(final RegisterCommandEvent<Command.Parameterized> event)
    {
        //Register commands...
        initializeCommands(event);
        Sponge.server().sendMessage(PluginInfo.PLUGIN_PREFIX.append(text("Commands loaded.", AQUA)));
    }

    private void setDefaultPermissions()
    {
        final Optional<PermissionService> optionalPermissionService = Sponge.serviceProvider().provide(PermissionService.class);
        if(optionalPermissionService.isPresent())
        {
            final PermissionService permissionService = optionalPermissionService.get();
            final Map<String, Boolean> permissionContext = permissionService.defaults().subjectData().permissions(SubjectData.GLOBAL_CONTEXT);
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
                permissionService.defaults().subjectData().setPermission(SubjectData.GLOBAL_CONTEXT, "eaglefactions.player", Tristate.TRUE);
            }
        }
    }

    // Start removing inactive factions when server is fully started.
    @Listener
    public void onGameLoad(final StartedEngineEvent<Server> event)
    {
        if (isDisabled)
            return;

        startFactionsRemover();
    }

    @Listener
    public void onReload(final RefreshGameEvent event)
    {
        if (isDisabled)
            return;

        this.configuration.reloadConfiguration();
        this.storageManager.reloadStorage();

        if (this.configuration.getDynmapConfig().isDynmapIntegrationEnabled())
        {
            this.dynmapService.reload();
        }

        if(event.source() instanceof Player)
        {
            Player player = (Player)event.source();
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text(Messages.CONFIG_HAS_BEEN_RELOADED)));
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
    public Faction.Builder getBuilderForFaction(String name, TextComponent tag, UUID leader)
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
        Sponge.server().sendMessage(PluginInfo.PLUGIN_PREFIX.append(text(message, YELLOW)));
    }

    private void setupConfigs()
    {
        Resource resource = null;
        try
        {
            resource = Sponge.server().resourceManager().load(ResourcePath.of(this.pluginContainer, "Settings.conf"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        configuration = new ConfigurationImpl(configDir, resource);
        MessageLoader.init(eagleFactions, pluginContainer);
        MessageLoader messageLoader = MessageLoader.getInstance();
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

    private void registerAPI(RegisterFactoryEvent event)
    {
        //This is not really needed as api consumers can access managers classes through EagleFactions interface instance.
        //But we are still registering these managers just in case someone will try to access not through EagleFactions interface.
        event.register(FactionLogic.class, this.factionLogic);
        event.register(PowerManager.class, this.powerManager);
        event.register(PlayerManager.class, this.playerManager);
        event.register(ProtectionManager.class, this.protectionManager);
        event.register(PermsManager.class, this.permsManager);
        event.register(PVPLogger.class, this.pvpLogger);
        event.register(AttackLogic.class, this.attackLogic);
        event.register(InvitationManager.class, this.invitationManager);
        event.register(RankManager.class, this.rankManager);
    }

    private void initializeCommands(RegisterCommandEvent<Command.Parameterized> event)
    {
        EagleFactionsCommandParameters.init(this.factionLogic);

        //Help command should display all possible commands in plugin.
        SUBCOMMANDS.put(Collections.singletonList("help"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_HELP_DESC))
                .permission(PluginPermissions.HELP_COMMAND)
                .addParameter(Parameter.integerNumber().optional().key("page").build())
                .executor(new HelpCommand(this))
                .build());

        //Create faction command.
        SUBCOMMANDS.put(Arrays.asList("c", "create"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_CREATE_DESC))
                .permission(PluginPermissions.CREATE_COMMAND)
                .addParameters(Parameter.string().key("tag").build(),
                        Parameter.string().key("name").build())
                .executor(new CreateCommand(this))
                .build());

        //Disband faction command.
        SUBCOMMANDS.put(Collections.singletonList("disband"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_DISBAND_DESC))
                .permission(PluginPermissions.DISBAND_COMMAND)
                .addParameter(EagleFactionsCommandParameters.faction())
                .executor(new DisbandCommand(this))
                .build());

        //List all factions.
        SUBCOMMANDS.put(Collections.singletonList("list"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_LIST_DESC))
                .permission(PluginPermissions.LIST_COMMAND)
                .executor(new ListCommand(this))
                .build());

        //Invite a player to the faction.
        SUBCOMMANDS.put(Collections.singletonList("invite"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_INVITE_DESC))
                .permission(PluginPermissions.INVITE_COMMAND)
                .addParameter(Parameter.player().key("player").build())
                .executor(new InviteCommand(this))
                .build());

        //Kick a player from the faction.
        SUBCOMMANDS.put(Collections.singletonList("kick"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_KICK_DESC))
                .permission(PluginPermissions.KICK_COMMAND)
                .addParameter(Parameter.player().key("player").build())
                .executor(new KickCommand(this))
                .build());

        //Join faction command
        SUBCOMMANDS.put(Arrays.asList("j", "join"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_JOIN_DESC))
                .permission(PluginPermissions.JOIN_COMMAND)
                .addParameter(EagleFactionsCommandParameters.faction())
                .executor(new JoinCommand(this))
                .build());

        //Leave faction command
        SUBCOMMANDS.put(Collections.singletonList("leave"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_LEAVE_DESC))
                .permission(PluginPermissions.LEAVE_COMMAND)
                .executor(new LeaveCommand(this))
                .build());

        //Version command
        SUBCOMMANDS.put(Arrays.asList("v", "version"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_VERSION_DESC))
                .permission(PluginPermissions.VERSION_COMMAND)
                .executor(new VersionCommand(this))
                .build());

        //Info command. Shows info about a faction.
        SUBCOMMANDS.put(Arrays.asList("i", "info"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_INFO_DESC))
                .addParameter(EagleFactionsCommandParameters.faction())
                .executor(new InfoCommand(this))
                .build());

        //Player command. Shows info about a player. (its factions etc.)
        SUBCOMMANDS.put(Arrays.asList("p", "player"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_PLAYER_DESC))
                .permission(PluginPermissions.PLAYER_COMMAND)
                .addParameter(EagleFactionsCommandParameters.factionPlayer())
                .executor(new PlayerCommand(this))
                .build());

        //Truce command
        SUBCOMMANDS.put(Collections.singletonList("truce"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_TRUCE_DESC))
                .permission(PluginPermissions.TRUCE_COMMAND)
                .addParameter(EagleFactionsCommandParameters.faction())
                .executor(new TruceCommand(this))
                .build());

        //Ally command
        SUBCOMMANDS.put(Collections.singletonList("ally"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_ALLY_DESC))
                .permission(PluginPermissions.ALLY_COMMAND)
                .addParameter(EagleFactionsCommandParameters.faction())
                .executor(new AllyCommand(this))
                .build());

        //Enemy command
        SUBCOMMANDS.put(Collections.singletonList("enemy"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_ENEMY_DESC))
                .permission(PluginPermissions.ENEMY_COMMAND)
                .addParameter(EagleFactionsCommandParameters.faction())
                .executor(new EnemyCommand(this))
                .build());

        //Promote command
        SUBCOMMANDS.put(Collections.singletonList("promote"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_PROMOTE_DESC))
                .addParameter(EagleFactionsCommandParameters.factionPlayer())
                .permission(PluginPermissions.PROMOTE_COMMAND)
                .executor(new PromoteCommand(this))
                .build());

        //Demote command
        SUBCOMMANDS.put(Collections.singletonList("demote"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_DEMOTE_DESC))
                .addParameter(EagleFactionsCommandParameters.factionPlayer())
                .permission(PluginPermissions.DEMOTE_COMMAND)
                .executor(new DemoteCommand(this))
                .build());

        //Claims command
        SUBCOMMANDS.put(Arrays.asList("claims", "listclaims"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_LIST_CLAIMS_DESC))
                .permission(PluginPermissions.CLAIMS_LIST_COMMAND)
                .addParameter(EagleFactionsCommandParameters.faction())
                .executor(new ClaimsListCommand(this))
                .build());

        //Claim command
        SUBCOMMANDS.put(Collections.singletonList("claim"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_CLAIM_DESC))
                .permission(PluginPermissions.CLAIM_COMMAND)
                .addParameter(EagleFactionsCommandParameters.faction())
                .executor(new ClaimCommand(this))
                .build());

        //Square Claim command
        SUBCOMMANDS.put(Collections.singletonList("squareclaim"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_SQUARE_CLAIM_DESC))
                .permission(PluginPermissions.RADIUS_CLAIM_COMMAND)
                .addParameter(Parameter.integerNumber().key("radius").build())
                .executor(new SquareClaimCommand(this))
                .build());

        // Fill Command
        SUBCOMMANDS.put(Collections.singletonList("fillclaim"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_FILL_CLAIM_DESC))
                .permission(PluginPermissions.COMMAND_FILL_CLAIM_COMMAND)
                .executor(new FillCommand(this))
                .build());

        //Unclaim command
        SUBCOMMANDS.put(Collections.singletonList("unclaim"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_UNCLAIM_DESC))
                .permission(PluginPermissions.UNCLAIM_COMMAND)
                .executor(new UnclaimCommand(this))
                .build());

        //Unclaimall Command
        SUBCOMMANDS.put(Collections.singletonList("unclaimall"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_UNCLAIM_ALL_DESC))
                .permission(PluginPermissions.UNCLAIM_ALL_COMMAND)
                .executor(new UnclaimAllCommand(this))
                .build());

        //Map command
        SUBCOMMANDS.put(Collections.singletonList("map"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_MAP_DESC))
                .permission(PluginPermissions.MAP_COMMAND)
                .executor(new MapCommand(this))
                .build());

        //Sethome command
        SUBCOMMANDS.put(Collections.singletonList("sethome"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_SET_HOME_DESC))
                .permission(PluginPermissions.SET_HOME_COMMAND)
                .executor(new SetHomeCommand(this))
                .build());

        //Home command
        SUBCOMMANDS.put(Collections.singletonList("home"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_HOME_DESC))
                .permission(PluginPermissions.HOME_COMMAND)
                .addParameter(Parameter.builder(Faction.class)
                        .key("faction")
                        .addParser(new FactionArgument.ValueParser(this.factionLogic))
                        .completer(new FactionArgument.Completer(this.factionLogic))
                        .optional()
                        .build())
                .executor(new HomeCommand(this))
                .build());

        //Autoclaim command.
        SUBCOMMANDS.put(Collections.singletonList("autoclaim"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_AUTO_CLAIM_DESC))
                .permission(PluginPermissions.AUTO_CLAIM_COMMAND)
                .executor(new AutoClaimCommand(this))
                .build());

        //Automap command
        SUBCOMMANDS.put(Collections.singletonList("automap"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_AUTO_MAP_DESC))
                .permission(PluginPermissions.AUTO_MAP_COMMAND)
                .executor(new AutoMapCommand(this))
                .build());

        //Coords Command
        SUBCOMMANDS.put(Collections.singletonList("coords"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_COORDS_DESC))
                .permission(PluginPermissions.COORDS_COMMAND)
                .addParameter(EagleFactionsCommandParameters.faction())
                .executor(new CoordsCommand(this))
                .build());

        //Admin command
        SUBCOMMANDS.put(Collections.singletonList("admin"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_ADMIN_DESC))
                .permission(PluginPermissions.ADMIN_MODE_COMMAND)
                .executor(new AdminCommand(this))
                .build());

        //SetFaction Command
        SUBCOMMANDS.put(Collections.singletonList("setfacion"), Command.builder()
                .shortDescription(Component.text(Messages.SET_FACTION_COMMAND))
                .permission(PluginPermissions.SET_FACTION_COMMAND)
                .addParameters(CommonParameters.PLAYER,
                        EagleFactionsCommandParameters.faction(),
                        Parameter.enumValue(FactionMemberType.class).key("rank").build())
                .executor(new SetFactionCommand(this))
                .build());

        //SetPower Command
        SUBCOMMANDS.put(Collections.singletonList("setpower"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_SET_POWER_DESC))
                .permission(PluginPermissions.SET_POWER_COMMAND)
                .addParameters(Parameter.player().key("player").build(),
                        Parameter.doubleNumber().key("power").build())
                .executor(new SetPowerCommand(this))
                .build());

        //MaxPower Command
        SUBCOMMANDS.put(Collections.singletonList("setmaxpower"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_SET_MAX_POWER_DESC))
                .permission(PluginPermissions.MAX_POWER_COMMAND)
                .addParameters(Parameter.player().key("player").build(),
                        Parameter.doubleNumber().key("power").build())
                .executor(new SetMaxPowerCommand(this))
                .build());

        // MaxPowerAll Command
        SUBCOMMANDS.put(Collections.singletonList("setmaxpower_forall"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_SET_MAX_POWER_FOR_ALL_DESC))
                .permission(PluginPermissions.MAX_POWER_FOR_ALL_COMMAND)
                .addParameter(Parameter.doubleNumber()
                        .key("power")
                        .build())
                .executor(new SetMaxPowerForAllCommand(this))
                .build());

        //Attack Command
        SUBCOMMANDS.put(Collections.singletonList("attack"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_ATTACK_DESC))
                .permission(PluginPermissions.ATTACK_COMMAND)
                .executor(new AttackCommand(this))
                .build());

        //Reload Command
        SUBCOMMANDS.put(Collections.singletonList("reload"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_RELOAD_DESC))
                .permission(PluginPermissions.RELOAD_COMMAND)
                .executor(new ReloadCommand(this))
                .build());

        //Chat Command
        SUBCOMMANDS.put(Collections.singletonList("chat"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_CHAT_DESC))
                .permission(PluginPermissions.CHAT_COMMAND)
                .addParameter(Parameter.enumValue(ChatEnum.class)
                        .key("chat")
                        .optional()
                        .build())
                .executor(new ChatCommand(this))
                .build());

        //Top Command
        SUBCOMMANDS.put(Collections.singletonList("top"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_TOP_DESC))
                .permission(PluginPermissions.TOP_COMMAND)
                .executor(new TopCommand(this))
                .build());

        //Setleader Command
        SUBCOMMANDS.put(Collections.singletonList("setleader"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_SET_LEADER_DESC))
                .permission(PluginPermissions.SET_LEADER_COMMAND)
                .addParameter(EagleFactionsCommandParameters.factionPlayer())
                .executor(new SetLeaderCommand(this))
                .build());

        //Perms Command
        SUBCOMMANDS.put(Collections.singletonList("perms"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_PERMS_DESC))
                .permission(PluginPermissions.PERMS_COMMAND)
                .executor(new PermsCommand(this))
                .build());

        //TagColor Command
        SUBCOMMANDS.put(Collections.singletonList("tagcolor"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_TAG_COLOR_DESC))
                .permission(PluginPermissions.TAG_COLOR_COMMAND)
                .addParameter(Parameter.color().key("color").build())
                .executor(new TagColorCommand(this))
                .build());

        //Rename Command
        SUBCOMMANDS.put(Collections.singletonList("rename"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_RENAME_DESC))
                .permission(PluginPermissions.RENAME_COMMAND)
                .addParameter(Parameter.string().key("name").build())
                .executor(new RenameCommand(this))
                .build());

        //Tag Command
        SUBCOMMANDS.put(Collections.singletonList("tag"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_TAG_DESC))
                .permission(PluginPermissions.TAG_COMMAND)
                .addParameter(Parameter.string().key("tag").build())
                .executor(new TagCommand(this))
                .build());

        //Description Command
        SUBCOMMANDS.put(Arrays.asList("desc", "description"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_DESC_DESC))
                .permission(PluginPermissions.DESCRIPTION_COMMAND)
                .addParameter(Parameter.remainingJoinedStrings().key("description").build())
                .executor(new DescriptionCommand(this))
                .build());

        //Motd Command
        SUBCOMMANDS.put(Collections.singletonList("motd"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_MOTD_DESC))
                .permission(PluginPermissions.MOTD_COMMAND)
                .addParameter(Parameter.remainingJoinedStrings().key("motd").build())
                .executor(new MotdCommand(this))
                .build());

        //EagleFeather Command
        SUBCOMMANDS.put(Collections.singletonList("feather"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_FEATHER_DESC))
                .permission(PluginPermissions.FEATHER_COMMAND)
                .executor(new EagleFeatherCommand(this))
                .build());

        //Chest Command
        SUBCOMMANDS.put(Collections.singletonList("chest"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_CHEST_DESC))
                .permission(PluginPermissions.CHEST_COMMAND)
                .addParameter(EagleFactionsCommandParameters.faction())
                .executor(new ChestCommand(this))
                .build());

        //Public Command
        SUBCOMMANDS.put(Collections.singletonList("public"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_PUBLIC_DESC))
                .permission(PluginPermissions.PUBLIC_COMMAND)
                .addParameter(EagleFactionsCommandParameters.faction())
                .executor(new PublicCommand(this))
                .build());

        //Debug Command
        SUBCOMMANDS.put(Collections.singletonList("debug"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_DEBUG_DESC))
                .permission(PluginPermissions.DEBUG_COMMAND)
                .executor(new DebugCommand(this))
                .build());

        //Backup Command
        SUBCOMMANDS.put(Collections.singletonList("createbackup"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_CREATE_BACKUP_DESC))
                .permission(PluginPermissions.BACKUP_COMMAND)
                .executor(new BackupCommand(this))
                .build());

        //Restore Backup Command
        SUBCOMMANDS.put(Collections.singletonList("restorebackup"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_RESTORE_BACKUP_DESC))
                .permission(PluginPermissions.RESTORE_BACKUP_COMMAND)
                .addParameter(Parameter.string()
                        .key("filename")
                        .addParser(new BackupNameArgument.ValueParser(this.storageManager))
                        .completer(new BackupNameArgument.Completer(this.storageManager))
                        .build())
                .executor(new RestoreBackupCommand(this))
                .build());

        //Regen Command
        SUBCOMMANDS.put(Collections.singletonList("regen"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_REGEN_DESC))
                .permission(PluginPermissions.REGEN_COMMAND)
                .addParameter(EagleFactionsCommandParameters.faction())
                .executor(new RegenCommand(this))
                .build());

        //Access Player Command
        final Command.Parameterized accessPlayerCommand = Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_ACCESS_PLAYER_DESC))
                .permission(PluginPermissions.ACCESS_PLAYER_COMMAND)
                .addParameter(EagleFactionsCommandParameters.factionPlayer())
                .executor(new AccessPlayerCommand(this))
                .build();

        //Access Faction Command
        final Command.Parameterized accessFactionCommand = Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_ACCESS_FACTION_DESC))
                .permission(PluginPermissions.ACCESS_FACTION_COMMAND)
                .executor(new AccessFactionCommand(this))
                .build();

        //Access OwnedBy Command
        final Command.Parameterized accessOwnedByCommand = Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_ACCESS_OWNED_BY_DESC))
                .permission(PluginPermissions.ACCESS_OWNED_BY_COMMAND)
                .addParameter(EagleFactionsCommandParameters.factionPlayer())
                .executor(new OwnedByCommand(this))
                .build();

        //Access AccessibleByFaction Command
        final Command.Parameterized accessibleByFactionCommand = Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_ACCESS_ACCESSIBLE_BY_FACTION_DESC))
                .permission(PluginPermissions.ACCESS_NOT_ACCESSIBLE_BY_FACTION_COMMAND)
                .executor(new NotAccessibleByFactionCommand(this))
                .build();

        //Access Command
        SUBCOMMANDS.put(Collections.singletonList("access"), Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_ACCESS_DESC))
                .permission(PluginPermissions.ACCESS_COMMAND)
                .executor(new AccessCommand(this))
                .addChild(accessPlayerCommand, "player", "p")
                .addChild(accessFactionCommand, "faction", "f")
                .addChild(accessOwnedByCommand, "ownedBy")
                .addChild(accessibleByFactionCommand, "notAccessibleByFaction")
                .build());

        //Build all commands
        Command.Parameterized commandEagleFactions = Command.builder()
                .shortDescription(Component.text(Messages.COMMAND_HELP_DESC))
                .executor(new HelpCommand(this))
                .addChildren(SUBCOMMANDS)
                .build();

        //Register commands
        event.register(this.pluginContainer, commandEagleFactions, "factions", "faction", "f");
    }

    private void registerListeners()
    {
        //Sponge events
        Sponge.eventManager().registerListeners(this.pluginContainer, new EntityDamageListener(this));
        Sponge.eventManager().registerListeners(this.pluginContainer, new PlayerJoinListener(this));
        Sponge.eventManager().registerListeners(this.pluginContainer, new PlayerDeathListener(this));
        Sponge.eventManager().registerListeners(this.pluginContainer, new BlockPlaceListener(this));
        Sponge.eventManager().registerListeners(this.pluginContainer, new BlockBreakListener(this));
        Sponge.eventManager().registerListeners(this.pluginContainer, new PlayerInteractListener(this));
        Sponge.eventManager().registerListeners(this.pluginContainer, new PlayerMoveListener(this));
        Sponge.eventManager().registerListeners(this.pluginContainer, new EntitySpawnListener(this));
        Sponge.eventManager().registerListeners(this.pluginContainer, new PlayerDisconnectListener(this));
        Sponge.eventManager().registerListeners(this.pluginContainer, new SendCommandListener(this));
        Sponge.eventManager().registerListeners(this.pluginContainer, new ExplosionListener(this));
        Sponge.eventManager().registerListeners(this.pluginContainer, new ModifyBlockListener(this));
        Sponge.eventManager().registerListeners(this.pluginContainer, new NotifyNeighborBlockListener(this));

        // Chat
        if(isUltimateChatLoaded())
        {
//            Sponge.eventManager().registerListeners(this.pluginContainer, new UltimateChatMessageListener(this));
        }
        else // Sponge/Vanilla
        {
            Sponge.eventManager().registerListeners(this.pluginContainer, new ChatMessageListener(this));
        }

        //EF events
        Sponge.eventManager().registerListeners(this.pluginContainer, new FactionKickListener(this));
        Sponge.eventManager().registerListeners(this.pluginContainer, new FactionLeaveListener(this));
        Sponge.eventManager().registerListeners(this.pluginContainer, new FactionJoinListener(this));
    }

    private void disablePlugin()
    {
        this.isDisabled = true;
        Sponge.eventManager().unregisterListeners(this);
//        Sponge.server().commandManager().registrar(Command.Parameterized.class).get()
//        Sponge.server().commandManager().getOwnedBy(this).forEach(Sponge.getCommandManager()::removeMapping);
        Sponge.server().sendMessage(PluginInfo.ERROR_PREFIX.append(text("EagleFactions has been disabled!", RED)));
    }

    private boolean isUltimateChatLoaded() {
        return Sponge.pluginManager().plugin("ultimatechat").isPresent();
    }
}
