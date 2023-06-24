package io.github.aquerr.eaglefactions;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.Configuration;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.logic.AttackLogic;
import io.github.aquerr.eaglefactions.api.logic.FactionManager;
import io.github.aquerr.eaglefactions.api.logic.PVPLogger;
import io.github.aquerr.eaglefactions.api.managers.InvitationManager;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.managers.PowerManager;
import io.github.aquerr.eaglefactions.api.managers.ProtectionManager;
import io.github.aquerr.eaglefactions.api.managers.RankManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.api.messaging.placeholder.PlaceholderService;
import io.github.aquerr.eaglefactions.api.storage.StorageManager;
import io.github.aquerr.eaglefactions.commands.EFCommandManager;
import io.github.aquerr.eaglefactions.config.ConfigurationImpl;
import io.github.aquerr.eaglefactions.integrations.IntegrationManager;
import io.github.aquerr.eaglefactions.managers.FactionManagerImpl;
import io.github.aquerr.eaglefactions.managers.claim.provider.DefaultFactionMaxClaimCountProvider;
import io.github.aquerr.eaglefactions.managers.claim.provider.FactionMaxClaimCountByPlayerPowerProvider;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import io.github.aquerr.eaglefactions.util.resource.Resource;
import io.github.aquerr.eaglefactions.util.resource.ResourceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.github.aquerr.eaglefactions.ModInfo.PLUGIN_PREFIX_PLAIN;
import static java.lang.String.format;


@Mod(ModInfo.ID)
public class EagleFactionsPlugin implements EagleFactions
{
    public static final String WAR_ZONE_NAME = "WarZone";
    public static final String SAFE_ZONE_NAME = "SafeZone";
    public static final String WILDERNESS_NAME = "Wilderness";

    //TODO: Convert these fields to instance fields.
//    public static final List<FactionInvite> INVITE_LIST = new LinkedList<>();
//    public static final List<AcceptableInvite> RELATION_INVITES = new LinkedList<>();
    public static final List<UUID> AUTO_CLAIM_LIST = new LinkedList<>();
    public static final List<UUID> AUTO_MAP_LIST = new LinkedList<>();
    public static final Map<UUID, String> REGEN_CONFIRMATION_MAP = new HashMap<>();
    public static final Map<String, Integer> ATTACKED_FACTIONS = new HashMap<>();
    public static final Map<UUID, Integer> BLOCKED_HOME = new HashMap<>();
//    public static final Map<UUID, ChatEnum> CHAT_LIST = new HashMap<>();
    public static final Map<UUID, Integer> HOME_COOLDOWN_PLAYERS = new HashMap<>();
    public static final List<UUID> DEBUG_MODE_PLAYERS = new LinkedList<>();

//    public static Key<Value<Boolean>> IS_EAGLE_FEATHER_KEY;

    private static EagleFactionsPlugin eagleFactions;
    public static final Logger LOGGER = LoggerFactory.getLogger(EagleFactionsPlugin.class);

    private Configuration configuration;
    private EFCommandManager efCommandManager;
    private PVPLogger pvpLogger;
    private PlayerManager playerManager;
    private PermsManager permsManager;
    private ProtectionManager protectionManager;
    private PowerManager powerManager;
    private AttackLogic attackLogic;
    private FactionManager factionManager;
    private InvitationManager invitationManager;
    private RankManager rankManager;
    private StorageManager storageManager;
    private MessageService messageService;
    private IntegrationManager integrationManager;

    private boolean isDisabled = false;

    private Path configDir;

    //Integrations
//    @Inject
//    private Metrics metrics;
//    private PAPIPlaceholderService PAPIPlaceholderService;
//    private EFPlaceholderService efPlaceholderService;
//    private UltimateChatService ultimateChatService;

    public static EagleFactionsPlugin getPlugin()
    {
        return eagleFactions;
    }

    public EagleFactionsPlugin()
    {
        this.efCommandManager = new EFCommandManager(this);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::onSetupDedicatedServer);

        configDir = FMLPaths.CONFIGDIR.get().resolve(ModInfo.ID);

        MinecraftForge.EVENT_BUS.register(this);

        eagleFactions = this;
    }

    public Path getConfigDir()
    {
        return configDir;
    }

    public void onSetupDedicatedServer(final FMLDedicatedServerSetupEvent event)
    {
        setupServerSideMod();
    }

    @SubscribeEvent
    public void onRegisterCommandEvent(final RegisterCommandsEvent event)
    {

        if (this.isDisabled)
            return;

        //Register commands...
        LOGGER.info("Registering commands...");
        this.efCommandManager.initializeCommands(event);
        LOGGER.info(PLUGIN_PREFIX_PLAIN + "Commands loaded!");
    }

    @SubscribeEvent
    public void onPermissionRegisterEvent(final PermissionGatherEvent.Nodes event)
    {
        event.addNodes(PluginPermissions.getAsPermissionNodesList());
    }

    private void setupClientSideMod()
    {
        LOGGER.info(format("Hello %s!", Minecraft.getInstance().getUser().getName()));
        LOGGER.info("Preparing client side integration for Eagle Factions...");
    }

    private void setupServerSideMod()
    {
        try
        {
            LOGGER.info(PLUGIN_PREFIX_PLAIN + "Preparing wings...");

            setupConfigs();

            LOGGER.info(PLUGIN_PREFIX_PLAIN + "Configs loaded.");

            LOGGER.info(PLUGIN_PREFIX_PLAIN + "Loading managers and cache...");
            setupManagers();

            LOGGER.info(PLUGIN_PREFIX_PLAIN + "Managers loaded.");
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
//            disablePlugin();
        }
    }

//    public void onDataRegister(RegisterDataEvent event)
//    {
//        IS_EAGLE_FEATHER_KEY = Key.from(this.pluginContainer, "is_eagle_feather", Boolean.class);
//        event.register(DataRegistration.of(IS_EAGLE_FEATHER_KEY, ItemStack.class));
//    }

//    public void onPluginLoad(final LoadedGameEvent event)
//    {
//        if (this.isDisabled)
//            return;
//
//        try
//        {
//            registerListeners();
//
//            EventRunner.init(Sponge.eventManager());
//
//            //Display some info text in the console.
//            this.logger.info(PLUGIN_PREFIX_PLAIN + "==========================================");
//            this.logger.info(PLUGIN_PREFIX_PLAIN + "Eagle Factions is ready to use!");
//            this.logger.info(PLUGIN_PREFIX_PLAIN + "Thank you for choosing this plugin!");
//            this.logger.info(PLUGIN_PREFIX_PLAIN + "Current version: " + PluginInfo.VERSION);
//            this.logger.info(PLUGIN_PREFIX_PLAIN + "Have a great time with Eagle Factions! :D");
//            this.logger.info(PLUGIN_PREFIX_PLAIN + "==========================================");
//
//            CompletableFuture.runAsync(() ->
//            {
//                if(!VersionChecker.isLatest(PluginInfo.VERSION))
//                {
//                    this.logger.info(PLUGIN_PREFIX_PLAIN + "Hey! A new version of " + PluginInfo.NAME + " is available online!");
//                    this.logger.info("==========================================");
//                }
//            });
//
//            // Reloads storage and cache.
//            this.storageManager.reloadStorage();
//
//            initializeIntegrations();
//
//            startFactionsRemover();
//
//            preCreateSafeZoneAndWarZone();
//        }
//        catch (Exception exception)
//        {
//            exception.printStackTrace();
//            disablePlugin();
//        }
//    }

//    private void preCreateSafeZoneAndWarZone()
//    {
//        // SafeZone and WarZone factions must always exist!
//        if (this.factionLogic.getFactionByName(EagleFactionsPlugin.WAR_ZONE_NAME) == null)
//        {
//            final Faction warzone = FactionImpl.builder(EagleFactionsPlugin.WAR_ZONE_NAME, text("WZ"), new UUID(0, 0))
//                    .setCreatedDate(Instant.now())
//                    .setProtectionFlags(new HashSet<>(Arrays.asList(
//                    new ProtectionFlagImpl(ProtectionFlagType.PVP, true),
//                    new ProtectionFlagImpl(ProtectionFlagType.FIRE_SPREAD, true),
//                    new ProtectionFlagImpl(ProtectionFlagType.MOB_GRIEF, true),
//                    new ProtectionFlagImpl(ProtectionFlagType.ALLOW_EXPLOSION, true),
//                    new ProtectionFlagImpl(ProtectionFlagType.SPAWN_MONSTERS, true),
//                    new ProtectionFlagImpl(ProtectionFlagType.SPAWN_ANIMALS, true)
//            )))
//                    .build();
//            this.factionLogic.addFaction(warzone);
//        }
//        if (this.factionLogic.getFactionByName(EagleFactionsPlugin.SAFE_ZONE_NAME) == null)
//        {
//            final Faction safezone = FactionImpl.builder(EagleFactionsPlugin.SAFE_ZONE_NAME, text("SZ"), new UUID(0, 0))
//                    .setCreatedDate(Instant.now())
//                    .setProtectionFlags(new HashSet<>(Arrays.asList(
//                            new ProtectionFlagImpl(ProtectionFlagType.PVP, false),
//                            new ProtectionFlagImpl(ProtectionFlagType.FIRE_SPREAD, false),
//                            new ProtectionFlagImpl(ProtectionFlagType.MOB_GRIEF, false),
//                            new ProtectionFlagImpl(ProtectionFlagType.ALLOW_EXPLOSION, false),
//                            new ProtectionFlagImpl(ProtectionFlagType.SPAWN_MONSTERS, false),
//                            new ProtectionFlagImpl(ProtectionFlagType.SPAWN_ANIMALS, true)
//                    )))
//                    .build();
//            this.factionLogic.addFaction(safezone);
//        }
//    }

//    public void onFactoryRegister(RegisterFactoryEvent event)
//    {
//        if (this.isDisabled)
//            return;
//
//        registerAPI(event);
//    }

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

//        this.integrationManager.activateIntegrations();

//        if (isUltimateChatLoaded())
//        {
//            this.ultimateChatService = new UltimateChatService(this.configuration.getChatConfig());
//            this.ultimateChatService.registerTags();
//        }
    }

//    public void onRegisterCommand(final RegisterCommandEvent<Command.Parameterized> event)
//    {
//        if (this.isDisabled)
//            return;
//
//        //Register commands...
//        initializeCommands(event);
//        this.logger.info(PLUGIN_PREFIX_PLAIN + "Commands loaded!");
//    }
//
//    private void setDefaultPermissions()
//    {
//        final Optional<PermissionService> optionalPermissionService = Sponge.serviceProvider().provide(PermissionService.class);
//        if(optionalPermissionService.isPresent())
//        {
//            final PermissionService permissionService = optionalPermissionService.get();
//            final Map<String, Boolean> permissionContext = permissionService.defaults().subjectData().permissions(SubjectData.GLOBAL_CONTEXT);
//            boolean hasEagleFactionsPermission = false;
//            for (final String permission : permissionContext.keySet())
//            {
//                if (permission.contains("eaglefactions"))
//                {
//                    hasEagleFactionsPermission = true;
//                    break;
//                }
//            }
//
//            //If eaglefactions already exists then don't add default permissions.
//            if (!hasEagleFactionsPermission)
//            {
//                permissionService.defaults().subjectData().setPermission(SubjectData.GLOBAL_CONTEXT, "eaglefactions.player", Tristate.TRUE);
//            }
//        }
//    }
//
//    @Listener
//    public void onReload(final RefreshGameEvent event)
//    {
//        if (isDisabled)
//            return;
//
//        try
//        {
//            this.configuration.reloadConfiguration();
//            this.storageManager.reloadStorage();
//
//            this.integrationManager.reloadIntegrations();
//
//            if(event.source() instanceof Player)
//            {
//                Player player = (Player)event.source();
//                player.sendMessage(messageService.resolveMessageWithPrefix("command.reload.config-reloaded"));
//            }
//        }
//        catch (IOException e)
//        {
//            Player player = (Player)event.source();
//            player.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage("error.command.reload")));
//            e.printStackTrace();
//        }
//    }
//
//    @Listener
//    public void onRegisterPlaceholderEvent(RegisterRegistryValueEvent.GameScoped event)
//    {
//        this.efPlaceholderService.onRegisterPlaceholderEvent(event);
//    }
//
//    public Logger getLogger()
//    {
//        return this.logger;
//    }
//
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
    public FactionManager getFactionManager()
    {
        return this.factionManager;
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

    @Override
    public PlaceholderService getPlaceholderService()
    {
        return null;
//        return this.efPlaceholderService;
    }

    @Override
    public MessageService getMessageService()
    {
        return this.messageService;
    }

    public IntegrationManager getIntegrationManager()
    {
        return integrationManager;
    }

    @Override
    public Faction.Builder getBuilderForFaction(String name, Component tag, UUID leader)
    {
        return null;
//        return new FactionImpl.BuilderImpl(name, tag, leader);
    }

    @Override
    public FactionPlayer createNewFactionPlayer(final String playerName, final UUID uniqueId, final String factionName, final float power, final float maxpower, final boolean diedInWarZone)
    {
        return null;
//        return new FactionPlayerImpl(playerName, uniqueId, factionName, power, maxpower, diedInWarZone);
    }

    public URI getResource(final String fileName)
    {
        try
        {
            return ResourceUtils.getResource(fileName).getUrl().toURI();
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public void printInfo(final String message)
    {
        LOGGER.info(PLUGIN_PREFIX_PLAIN + message);
    }
//
    private void setupConfigs() throws IOException
    {
        Resource resource = ResourceUtils.getResource("assets/eaglefactions/Settings.conf");
        if (resource == null)
            return;

        System.out.println("Loaded resource: " + resource);

        configuration = new ConfigurationImpl(configDir, resource);
//        pvpLogger = PVPLoggerImpl.getInstance(this);
    }

    private void setupManagers()
    {
//        this.efPlaceholderService = new EFPlaceholderService(this);

        EFMessageService.init(this.configuration.getFactionsConfig().getLanguageFileName());
        this.messageService = EFMessageService.getInstance();
//        this.storageManager = new StorageManagerImpl(this, this.configuration.getStorageConfig(), this.configDir);
//        this.playerManager = new PlayerManagerImpl(this.storageManager, this.factionLogic, this.getConfiguration().getFactionsConfig(), this.configuration.getPowerConfig());

//        this.powerManager = new PowerManagerImpl(this.playerManager, this.configuration.getPowerConfig());
//        this.powerManager.addFactionPowerProvider(new DefaultFactionPowerProvider(new FactionPowerByPlayerPowerProvider(this.playerManager)));
//        this.powerManager.addFactionMaxPowerProvider(new DefaultFactionMaxPowerProvider(new FactionMaxPowerByPlayerMaxPowerProvider(this.playerManager)));

//        this.permsManager = new PermsManagerImpl();

        this.factionManager = new FactionManagerImpl(this.playerManager, this.storageManager, this.getConfiguration().getFactionsConfig(), this.messageService);
        this.factionManager.addFactionMaxClaimCountProvider(new DefaultFactionMaxClaimCountProvider(new FactionMaxClaimCountByPlayerPowerProvider(this.powerManager)));

//        this.attackLogic = new AttackLogicImpl(this.factionLogic, this.getConfiguration().getFactionsConfig(), this.messageService);
//        this.protectionManager = new ProtectionManagerImpl(this.factionLogic, this.permsManager, this.playerManager, this.messageService, this.configuration.getProtectionConfig(), this.configuration.getChatConfig(), this.configuration.getFactionsConfig());
//        this.invitationManager = new InvitationManagerImpl(this.storageManager, this.factionLogic, this.playerManager, this.messageService);
//        this.rankManager = new RankManagerImpl(this.factionLogic, this.storageManager);

        this.integrationManager = new IntegrationManager(this);
    }
//
//    private void startFactionsRemover()
//    {
//        //Do not turn on faction's remover if max inactive time == 0
//        if(this.getConfiguration().getFactionsConfig().getMaxInactiveTime() == 0)
//            return;
//
//        EagleFactionsScheduler.getInstance().scheduleWithDelayedIntervalAsync(new FactionRemoverTask(eagleFactions), 0, TimeUnit.SECONDS, 1, TimeUnit.HOURS);
//    }
//
//    private void registerAPI(RegisterFactoryEvent event)
//    {
//        //This is not really needed as api consumers can access managers classes through EagleFactions interface instance.
//        //But we are still registering these managers just in case someone will try to access not through EagleFactions interface.
//        event.register(FactionLogic.class, this.factionLogic);
//        event.register(PowerManager.class, this.powerManager);
//        event.register(PlayerManager.class, this.playerManager);
//        event.register(ProtectionManager.class, this.protectionManager);
//        event.register(PermsManager.class, this.permsManager);
//        event.register(PVPLogger.class, this.pvpLogger);
//        event.register(AttackLogic.class, this.attackLogic);
//        event.register(InvitationManager.class, this.invitationManager);
//        event.register(RankManager.class, this.rankManager);
//    }
//

//        EagleFactionsCommandParameters.init(this.factionLogic);
//
//        //Help command should display all possible commands in plugin.
//        SUBCOMMANDS.put(Collections.singletonList("help"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.help.desc"))
//                .permission(PluginPermissions.HELP_COMMAND)
//                .addParameter(Parameter.integerNumber().optional().key("page").build())
//                .executor(new HelpCommand(this))
//                .build());
//
//        //Create faction command.
//        SUBCOMMANDS.put(Arrays.asList("c", "create"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.create.desc"))
//                .permission(PluginPermissions.CREATE_COMMAND)
//                .addParameters(Parameter.string().key("tag").build(),
//                        Parameter.string().key("name").build())
//                .executor(new CreateCommand(this))
//                .build());
//
//        //Disband faction command.
//        SUBCOMMANDS.put(Collections.singletonList("disband"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.disband.desc"))
//                .permission(PluginPermissions.DISBAND_COMMAND)
//                .addParameter(EagleFactionsCommandParameters.optionalFaction())
//                .executor(new DisbandCommand(this))
//                .build());
//
//        //List all factions.
//        SUBCOMMANDS.put(Collections.singletonList("list"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.list.desc"))
//                .permission(PluginPermissions.LIST_COMMAND)
//                .executor(new ListCommand(this))
//                .build());
//
//        //Invite a player to the faction.
//        SUBCOMMANDS.put(Collections.singletonList("invite"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.invite.desc"))
//                .permission(PluginPermissions.INVITE_COMMAND)
//                .addParameter(Parameter.player().key("player").build())
//                .executor(new InviteCommand(this))
//                .build());
//
//        //Kick a player from the faction.
//        SUBCOMMANDS.put(Collections.singletonList("kick"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.kick.desc"))
//                .permission(PluginPermissions.KICK_COMMAND)
//                .addParameter(Parameter.player().key("player").build())
//                .executor(new KickCommand(this))
//                .build());
//
//        //Join faction command
//        SUBCOMMANDS.put(Arrays.asList("j", "join"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.join.desc"))
//                .permission(PluginPermissions.JOIN_COMMAND)
//                .addParameter(EagleFactionsCommandParameters.faction())
//                .executor(new JoinCommand(this))
//                .build());
//
//        //Leave faction command
//        SUBCOMMANDS.put(Collections.singletonList("leave"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.leave.desc"))
//                .permission(PluginPermissions.LEAVE_COMMAND)
//                .executor(new LeaveCommand(this))
//                .build());
//
//        //Version command
//        SUBCOMMANDS.put(Arrays.asList("v", "version"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.version.desc"))
//                .permission(PluginPermissions.VERSION_COMMAND)
//                .executor(new VersionCommand(this))
//                .build());
//
//        //Info command. Shows info about a faction.
//        SUBCOMMANDS.put(Arrays.asList("i", "info"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.info.desc"))
//                .addParameter(EagleFactionsCommandParameters.optionalFaction())
//                .executor(new InfoCommand(this))
//                .build());
//
//        //Player command. Shows info about a player. (its factions etc.)
//        SUBCOMMANDS.put(Arrays.asList("p", "player"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.player.desc"))
//                .permission(PluginPermissions.PLAYER_COMMAND)
//                .addParameter(EagleFactionsCommandParameters.optionalFactionPlayer())
//                .executor(new PlayerCommand(this))
//                .build());
//
//        //Truce command
//        SUBCOMMANDS.put(Collections.singletonList("truce"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.truce.desc"))
//                .permission(PluginPermissions.TRUCE_COMMAND)
//                .addParameter(EagleFactionsCommandParameters.faction())
//                .executor(new TruceCommand(this))
//                .build());
//
//        //Ally command
//        SUBCOMMANDS.put(Collections.singletonList("ally"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.ally.desc"))
//                .permission(PluginPermissions.ALLY_COMMAND)
//                .addParameter(EagleFactionsCommandParameters.faction())
//                .executor(new AllyCommand(this))
//                .build());
//
//        //Enemy command
//        SUBCOMMANDS.put(Collections.singletonList("enemy"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.enemy.desc"))
//                .permission(PluginPermissions.ENEMY_COMMAND)
//                .addParameter(EagleFactionsCommandParameters.faction())
//                .executor(new EnemyCommand(this))
//                .build());
//
//        //Promote command
//        SUBCOMMANDS.put(Collections.singletonList("promote"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.promote.desc"))
//                .addParameter(EagleFactionsCommandParameters.factionPlayer())
//                .permission(PluginPermissions.PROMOTE_COMMAND)
//                .executor(new PromoteCommand(this))
//                .build());
//
//        //Demote command
//        SUBCOMMANDS.put(Collections.singletonList("demote"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.demote.desc"))
//                .addParameter(EagleFactionsCommandParameters.factionPlayer())
//                .permission(PluginPermissions.DEMOTE_COMMAND)
//                .executor(new DemoteCommand(this))
//                .build());
//
//        //Claims command
//        SUBCOMMANDS.put(Arrays.asList("claims", "listclaims"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.list-claims.desc"))
//                .permission(PluginPermissions.CLAIMS_LIST_COMMAND)
//                .addParameter(EagleFactionsCommandParameters.optionalFaction())
//                .executor(new ClaimsListCommand(this))
//                .build());
//
//        //Claim command
//        SUBCOMMANDS.put(Collections.singletonList("claim"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.claim.desc"))
//                .permission(PluginPermissions.CLAIM_COMMAND)
//                .addParameter(EagleFactionsCommandParameters.optionalFaction())
//                .executor(new ClaimCommand(this))
//                .build());
//
//        //Square Claim command
//        SUBCOMMANDS.put(Collections.singletonList("squareclaim"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.square-claim.desc"))
//                .permission(PluginPermissions.RADIUS_CLAIM_COMMAND)
//                .addParameter(Parameter.integerNumber().key("radius").build())
//                .executor(new SquareClaimCommand(this))
//                .build());
//
//        // Fill Command
//        SUBCOMMANDS.put(Collections.singletonList("fillclaim"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.fill-claim.desc"))
//                .permission(PluginPermissions.COMMAND_FILL_CLAIM_COMMAND)
//                .executor(new FillCommand(this))
//                .build());
//
//        //Unclaim command
//        SUBCOMMANDS.put(Collections.singletonList("unclaim"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.unclaim.desc"))
//                .permission(PluginPermissions.UNCLAIM_COMMAND)
//                .executor(new UnclaimCommand(this))
//                .build());
//
//        //Unclaimall Command
//        SUBCOMMANDS.put(Collections.singletonList("unclaimall"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.unclaim-all.desc"))
//                .permission(PluginPermissions.UNCLAIM_ALL_COMMAND)
//                .executor(new UnclaimAllCommand(this))
//                .build());
//
//        //Map command
//        SUBCOMMANDS.put(Collections.singletonList("map"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.map.desc"))
//                .permission(PluginPermissions.MAP_COMMAND)
//                .executor(new MapCommand(this))
//                .build());
//
//        //Sethome command
//        SUBCOMMANDS.put(Collections.singletonList("sethome"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.set-home.desc"))
//                .permission(PluginPermissions.SET_HOME_COMMAND)
//                .executor(new SetHomeCommand(this))
//                .build());
//
//        //Home command
//        SUBCOMMANDS.put(Collections.singletonList("home"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.home.desc"))
//                .permission(PluginPermissions.HOME_COMMAND)
//                .addParameter(Parameter.builder(Faction.class)
//                        .key("faction")
//                        .addParser(new FactionArgument.ValueParser(this.factionLogic))
//                        .completer(new FactionArgument.Completer(this.factionLogic))
//                        .optional()
//                        .build())
//                .executor(new HomeCommand(this))
//                .build());
//
//        //Autoclaim command.
//        SUBCOMMANDS.put(Collections.singletonList("autoclaim"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.auto-claim.desc"))
//                .permission(PluginPermissions.AUTO_CLAIM_COMMAND)
//                .executor(new AutoClaimCommand(this))
//                .build());
//
//        //Automap command
//        SUBCOMMANDS.put(Collections.singletonList("automap"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.auto-map.desc"))
//                .permission(PluginPermissions.AUTO_MAP_COMMAND)
//                .executor(new AutoMapCommand(this))
//                .build());
//
//        //Coords Command
//        SUBCOMMANDS.put(Collections.singletonList("coords"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.coords.desc"))
//                .permission(PluginPermissions.COORDS_COMMAND)
//                .addParameter(EagleFactionsCommandParameters.optionalFaction())
//                .executor(new CoordsCommand(this))
//                .build());
//
//        //Admin command
//        SUBCOMMANDS.put(Collections.singletonList("admin"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.admin.desc"))
//                .permission(PluginPermissions.ADMIN_MODE_COMMAND)
//                .executor(new AdminCommand(this))
//                .build());
//
//        //SetFaction Command
//        SUBCOMMANDS.put(Collections.singletonList("setfacion"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.set-faction.desc"))
//                .permission(PluginPermissions.SET_FACTION_COMMAND)
//                .addParameters(CommonParameters.PLAYER,
//                        EagleFactionsCommandParameters.optionalFaction(),
//                        Parameter.enumValue(FactionMemberType.class).key("rank").build())
//                .executor(new SetFactionCommand(this))
//                .build());
//
//        //SetPower Command
//        SUBCOMMANDS.put(Collections.singletonList("setpower"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.set-power.desc"))
//                .permission(PluginPermissions.SET_POWER_COMMAND)
//                .addParameters(Parameter.player().key("player").build(),
//                        Parameter.doubleNumber().key("power").build())
//                .executor(new SetPowerCommand(this))
//                .build());
//
//        //MaxPower Command
//        SUBCOMMANDS.put(Collections.singletonList("setmaxpower"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.set-max-power.desc"))
//                .permission(PluginPermissions.MAX_POWER_COMMAND)
//                .addParameters(Parameter.player().key("player").build(),
//                        Parameter.doubleNumber().key("power").build())
//                .executor(new SetMaxPowerCommand(this))
//                .build());
//
//        // MaxPowerAll Command
//        SUBCOMMANDS.put(Collections.singletonList("setmaxpower_for_everyone"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.set-max-power-for-everyone.desc"))
//                .permission(PluginPermissions.MAX_POWER_FOR_EVERYONE_COMMAND)
//                .addParameter(Parameter.doubleNumber()
//                        .key("power")
//                        .build())
//                .executor(new SetMaxPowerForEveryoneCommand(this))
//                .build());
//
//        //Attack Command
//        SUBCOMMANDS.put(Collections.singletonList("attack"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.attack.desc"))
//                .permission(PluginPermissions.ATTACK_COMMAND)
//                .executor(new AttackCommand(this))
//                .build());
//
//        //Reload Command
//        SUBCOMMANDS.put(Collections.singletonList("reload"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.reload.desc"))
//                .permission(PluginPermissions.RELOAD_COMMAND)
//                .executor(new ReloadCommand(this))
//                .build());
//
//        //Chat Command
//        SUBCOMMANDS.put(Collections.singletonList("chat"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.chat.desc"))
//                .permission(PluginPermissions.CHAT_COMMAND)
//                .addParameter(Parameter.enumValue(ChatEnum.class)
//                        .key("chat")
//                        .optional()
//                        .build())
//                .executor(new ChatCommand(this))
//                .build());
//
//        //Top Command
//        SUBCOMMANDS.put(Collections.singletonList("top"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.top.desc"))
//                .permission(PluginPermissions.TOP_COMMAND)
//                .executor(new TopCommand(this))
//                .build());
//
//        //Setleader Command
//        SUBCOMMANDS.put(Collections.singletonList("setleader"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.set-leader.desc"))
//                .permission(PluginPermissions.SET_LEADER_COMMAND)
//                .addParameter(EagleFactionsCommandParameters.factionPlayer())
//                .executor(new SetLeaderCommand(this))
//                .build());
//
//        //Perms Command
//        SUBCOMMANDS.put(Collections.singletonList("perms"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.perms.desc"))
//                .permission(PluginPermissions.PERMS_COMMAND)
//                .executor(new PermsCommand(this))
//                .build());
//
//        //TagColor Command
//        SUBCOMMANDS.put(Collections.singletonList("tagcolor"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.tag-color.desc"))
//                .permission(PluginPermissions.TAG_COLOR_COMMAND)
//                .addParameter(Parameter.color().key("color").build())
//                .executor(new TagColorCommand(this))
//                .build());
//
//        //Rename Command
//        SUBCOMMANDS.put(Collections.singletonList("rename"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.rename.desc"))
//                .permission(PluginPermissions.RENAME_COMMAND)
//                .addParameter(Parameter.string().key("name").build())
//                .executor(new RenameCommand(this))
//                .build());
//
//        //Tag Command
//        SUBCOMMANDS.put(Collections.singletonList("tag"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.tag.desc"))
//                .permission(PluginPermissions.TAG_COMMAND)
//                .addParameter(Parameter.string().key("tag").build())
//                .executor(new TagCommand(this))
//                .build());
//
//        //Description Command
//        SUBCOMMANDS.put(Arrays.asList("desc", "description"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.desc.desc"))
//                .permission(PluginPermissions.DESCRIPTION_COMMAND)
//                .addParameter(Parameter.remainingJoinedStrings().key("description").build())
//                .executor(new DescriptionCommand(this))
//                .build());
//
//        //Motd Command
//        SUBCOMMANDS.put(Collections.singletonList("motd"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.motd.desc"))
//                .permission(PluginPermissions.MOTD_COMMAND)
//                .addParameter(Parameter.remainingJoinedStrings().key("motd").build())
//                .executor(new MotdCommand(this))
//                .build());
//
//        //EagleFeather Command
//        SUBCOMMANDS.put(Collections.singletonList("feather"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.feather.desc"))
//                .permission(PluginPermissions.FEATHER_COMMAND)
//                .executor(new EagleFeatherCommand(this))
//                .build());
//
//        //Chest Command
//        SUBCOMMANDS.put(Collections.singletonList("chest"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.chest.desc"))
//                .permission(PluginPermissions.CHEST_COMMAND)
//                .addParameter(EagleFactionsCommandParameters.optionalFaction())
//                .executor(new ChestCommand(this))
//                .build());
//
//        //Public Command
//        SUBCOMMANDS.put(Collections.singletonList("public"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.public.desc"))
//                .permission(PluginPermissions.PUBLIC_COMMAND)
//                .addParameter(EagleFactionsCommandParameters.optionalFaction())
//                .executor(new PublicCommand(this))
//                .build());
//
//        //Debug Command
//        SUBCOMMANDS.put(Collections.singletonList("debug"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.debug.desc"))
//                .permission(PluginPermissions.DEBUG_COMMAND)
//                .executor(new DebugCommand(this))
//                .build());
//
//        //Backup Command
//        SUBCOMMANDS.put(Collections.singletonList("createbackup"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.create-backup.desc"))
//                .permission(PluginPermissions.BACKUP_COMMAND)
//                .executor(new BackupCommand(this))
//                .build());
//
//        //Restore Backup Command
//        SUBCOMMANDS.put(Collections.singletonList("restorebackup"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.restore-backup.desc"))
//                .permission(PluginPermissions.RESTORE_BACKUP_COMMAND)
//                .addParameter(Parameter.string()
//                        .key("filename")
//                        .addParser(new BackupNameArgument.ValueParser(this.storageManager))
//                        .completer(new BackupNameArgument.Completer(this.storageManager))
//                        .build())
//                .executor(new RestoreBackupCommand(this))
//                .build());
//
//        //Regen Command
//        SUBCOMMANDS.put(Collections.singletonList("regen"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.regen.desc"))
//                .permission(PluginPermissions.REGEN_COMMAND)
//                .addParameter(EagleFactionsCommandParameters.faction())
//                .executor(new RegenCommand(this))
//                .build());
//
//        //Access Player Command
//        final Command.Parameterized accessPlayerCommand = Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.access.player.desc"))
//                .permission(PluginPermissions.ACCESS_PLAYER_COMMAND)
//                .addParameter(EagleFactionsCommandParameters.factionPlayer())
//                .executor(new AccessPlayerCommand(this))
//                .build();
//
//        //Access Faction Command
//        final Command.Parameterized accessFactionCommand = Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.access.faction.desc"))
//                .permission(PluginPermissions.ACCESS_FACTION_COMMAND)
//                .executor(new AccessFactionCommand(this))
//                .build();
//
//        //Access OwnedBy Command
//        final Command.Parameterized accessOwnedByCommand = Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.access.owned-by.desc"))
//                .permission(PluginPermissions.ACCESS_OWNED_BY_COMMAND)
//                .addParameter(EagleFactionsCommandParameters.factionPlayer())
//                .executor(new OwnedByCommand(this))
//                .build();
//
//        //Access AccessibleByFaction Command
//        final Command.Parameterized accessibleByFactionCommand = Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.access.accessible-by-faction.desc"))
//                .permission(PluginPermissions.ACCESS_NOT_ACCESSIBLE_BY_FACTION_COMMAND)
//                .executor(new NotAccessibleByFactionCommand(this))
//                .build();
//
//        //Access Command
//        SUBCOMMANDS.put(Collections.singletonList("access"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.access.desc"))
//                .permission(PluginPermissions.ACCESS_COMMAND)
//                .executor(new AccessCommand(this))
//                .addChild(accessPlayerCommand, "player", "p")
//                .addChild(accessFactionCommand, "faction", "f")
//                .addChild(accessOwnedByCommand, "ownedBy")
//                .addChild(accessibleByFactionCommand, "notAccessibleByFaction")
//                .build());
//
//        //Flags Command
//        SUBCOMMANDS.put(Collections.singletonList("flags"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.flags.desc"))
//                .permission(PluginPermissions.FLAGS_COMMAND)
//                .executor(new FlagsCommand(this))
//                .addParameter(EagleFactionsCommandParameters.faction())
//                .build());
//
//        SUBCOMMANDS.put(Collections.singletonList("setflag"), Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.setflag.desc"))
//                .permission(PluginPermissions.SET_FLAG_COMMAND)
//                .executor(new SetFlagCommand(this))
//                .addParameter(EagleFactionsCommandParameters.faction())
//                .addParameter(Parameter.enumValue(ProtectionFlagType.class).key("flag").build())
//                .addParameter(Parameter.bool().key("value").build())
//                .build());
//
//        //Build all commands
//        Command.Parameterized commandEagleFactions = Command.builder()
//                .shortDescription(messageService.resolveComponentWithMessage("command.help.desc"))
//                .executor(new HelpCommand(this))
//                .addChildren(SUBCOMMANDS)
//                .build();
//
//        //Register commands
//        event.register(this.pluginContainer, commandEagleFactions, "factions", "faction", "f");
//    }
//
//    private void registerListeners()
//    {
//        //Sponge events
//        Sponge.eventManager().registerListeners(this.pluginContainer, new EntityDamageListener(this));
//        Sponge.eventManager().registerListeners(this.pluginContainer, new PlayerJoinListener(this));
//        Sponge.eventManager().registerListeners(this.pluginContainer, new PlayerDeathListener(this));
//        Sponge.eventManager().registerListeners(this.pluginContainer, new BlockPlaceListener(this));
//        Sponge.eventManager().registerListeners(this.pluginContainer, new BlockBreakListener(this));
//        Sponge.eventManager().registerListeners(this.pluginContainer, new PlayerInteractListener(this));
//        Sponge.eventManager().registerListeners(this.pluginContainer, new PlayerMoveListener(this));
//        Sponge.eventManager().registerListeners(this.pluginContainer, new EntitySpawnListener(this));
//        Sponge.eventManager().registerListeners(this.pluginContainer, new PlayerDisconnectListener(this));
//        Sponge.eventManager().registerListeners(this.pluginContainer, new SendCommandListener(this));
//        Sponge.eventManager().registerListeners(this.pluginContainer, new ExplosionListener(this));
//        Sponge.eventManager().registerListeners(this.pluginContainer, new ModifyBlockListener(this));
//        Sponge.eventManager().registerListeners(this.pluginContainer, new NotifyNeighborBlockListener(this));
//
//        // Chat
//        if(isUltimateChatLoaded())
//        {
////            Sponge.eventManager().registerListeners(this.pluginContainer, new UltimateChatMessageListener(this));
//        }
//        else // Sponge/Vanilla
//        {
//            Sponge.eventManager().registerListeners(this.pluginContainer, new ChatMessageListener(this));
//        }
//
//        //EF events
//        Sponge.eventManager().registerListeners(this.pluginContainer, new FactionKickListener(this));
//        Sponge.eventManager().registerListeners(this.pluginContainer, new FactionLeaveListener(this));
//        Sponge.eventManager().registerListeners(this.pluginContainer, new FactionJoinListener(this));
//    }
//
//    private void disablePlugin()
//    {
//        this.isDisabled = true;
//        Sponge.eventManager().unregisterListeners(this);
////        Sponge.server().commandManager().registrar(Command.Parameterized.class).get()
////        Sponge.server().commandManager().getOwnedBy(this).forEach(Sponge.getCommandManager()::removeMapping);
//        this.logger.info(PLUGIN_PREFIX_PLAIN + "EagleFactions has been disabled due to an error!");
//    }

//    private boolean isUltimateChatLoaded() {
//        return Sponge.pluginManager().plugin("ultimatechat").isPresent();
//    }
}
