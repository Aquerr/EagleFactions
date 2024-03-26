package io.github.aquerr.eaglefactions;

import com.google.inject.Inject;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.Configuration;
import io.github.aquerr.eaglefactions.api.entities.AcceptableInvite;
import io.github.aquerr.eaglefactions.api.entities.ChatEnum;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionInvite;
import io.github.aquerr.eaglefactions.api.entities.FactionPermission;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlagType;
import io.github.aquerr.eaglefactions.api.entities.RelationType;
import io.github.aquerr.eaglefactions.api.logic.AttackLogic;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.logic.PVPLogger;
import io.github.aquerr.eaglefactions.api.managers.InvitationManager;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.managers.PowerManager;
import io.github.aquerr.eaglefactions.api.managers.ProtectionManager;
import io.github.aquerr.eaglefactions.api.managers.RankManager;
import io.github.aquerr.eaglefactions.api.managers.claim.ClaimByItemsStrategy;
import io.github.aquerr.eaglefactions.api.managers.claim.ClaimStrategy;
import io.github.aquerr.eaglefactions.api.managers.claim.DelayedClaimStrategy;
import io.github.aquerr.eaglefactions.api.managers.claim.NoCostClaimStrategy;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.api.messaging.placeholder.PlaceholderService;
import io.github.aquerr.eaglefactions.api.storage.StorageManager;
import io.github.aquerr.eaglefactions.commands.VersionCommand;
import io.github.aquerr.eaglefactions.commands.access.AccessCommand;
import io.github.aquerr.eaglefactions.commands.access.AccessFactionCommand;
import io.github.aquerr.eaglefactions.commands.access.AccessPlayerCommand;
import io.github.aquerr.eaglefactions.commands.access.NotAccessibleByFactionCommand;
import io.github.aquerr.eaglefactions.commands.access.OwnedByCommand;
import io.github.aquerr.eaglefactions.commands.admin.AdminCommand;
import io.github.aquerr.eaglefactions.commands.admin.DebugCommand;
import io.github.aquerr.eaglefactions.commands.admin.FlagsCommand;
import io.github.aquerr.eaglefactions.commands.admin.RegenCommand;
import io.github.aquerr.eaglefactions.commands.admin.ReloadCommand;
import io.github.aquerr.eaglefactions.commands.admin.SetFactionCommand;
import io.github.aquerr.eaglefactions.commands.admin.SetFlagCommand;
import io.github.aquerr.eaglefactions.commands.admin.SetMaxPowerCommand;
import io.github.aquerr.eaglefactions.commands.admin.SetMaxPowerForEveryoneCommand;
import io.github.aquerr.eaglefactions.commands.admin.SetPowerCommand;
import io.github.aquerr.eaglefactions.commands.args.BackupNameArgument;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
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
import io.github.aquerr.eaglefactions.commands.management.PublicCommand;
import io.github.aquerr.eaglefactions.commands.management.RenameCommand;
import io.github.aquerr.eaglefactions.commands.management.SetHomeCommand;
import io.github.aquerr.eaglefactions.commands.management.TagColorCommand;
import io.github.aquerr.eaglefactions.commands.management.TagCommand;
import io.github.aquerr.eaglefactions.commands.rank.AssignRankCommand;
import io.github.aquerr.eaglefactions.commands.rank.CreateRankCommand;
import io.github.aquerr.eaglefactions.commands.rank.DeleteRankCommand;
import io.github.aquerr.eaglefactions.commands.rank.ListRanksCommand;
import io.github.aquerr.eaglefactions.commands.rank.RankInfoCommand;
import io.github.aquerr.eaglefactions.commands.rank.RankListPermissionsCommand;
import io.github.aquerr.eaglefactions.commands.rank.SetLeaderCommand;
import io.github.aquerr.eaglefactions.commands.rank.SetRankDisplayInChatCommand;
import io.github.aquerr.eaglefactions.commands.rank.SetRankDisplayNameCommand;
import io.github.aquerr.eaglefactions.commands.rank.SetRankPermissionCommand;
import io.github.aquerr.eaglefactions.commands.rank.SetRankPositionCommand;
import io.github.aquerr.eaglefactions.commands.relation.AllyCommand;
import io.github.aquerr.eaglefactions.commands.relation.EnemyCommand;
import io.github.aquerr.eaglefactions.commands.relation.ListRelationPermissionsCommand;
import io.github.aquerr.eaglefactions.commands.relation.RelationsCommand;
import io.github.aquerr.eaglefactions.commands.relation.SetRelationPermissionCommand;
import io.github.aquerr.eaglefactions.commands.relation.TruceCommand;
import io.github.aquerr.eaglefactions.config.ConfigurationImpl;
import io.github.aquerr.eaglefactions.entities.FactionImpl;
import io.github.aquerr.eaglefactions.entities.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.entities.ProtectionFlagImpl;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.integrations.IntegrationManager;
import io.github.aquerr.eaglefactions.listeners.BlockBreakListener;
import io.github.aquerr.eaglefactions.listeners.BlockPlaceListener;
import io.github.aquerr.eaglefactions.listeners.ChangeBlockEventListener;
import io.github.aquerr.eaglefactions.listeners.ChatMessageListener;
import io.github.aquerr.eaglefactions.listeners.CollideBlockEventListener;
import io.github.aquerr.eaglefactions.listeners.CollideEntityEventListener;
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
import io.github.aquerr.eaglefactions.listeners.faction.FactionCreateListener;
import io.github.aquerr.eaglefactions.listeners.faction.FactionJoinListener;
import io.github.aquerr.eaglefactions.listeners.faction.FactionKickListener;
import io.github.aquerr.eaglefactions.listeners.faction.FactionLeaveListener;
import io.github.aquerr.eaglefactions.listeners.faction.FactionTagColorUpdateListener;
import io.github.aquerr.eaglefactions.listeners.faction.FactionTagUpdateListener;
import io.github.aquerr.eaglefactions.logic.AttackLogicImpl;
import io.github.aquerr.eaglefactions.logic.FactionLogicImpl;
import io.github.aquerr.eaglefactions.logic.PVPLoggerImpl;
import io.github.aquerr.eaglefactions.managers.InvitationManagerImpl;
import io.github.aquerr.eaglefactions.managers.PermsManagerImpl;
import io.github.aquerr.eaglefactions.managers.PlayerManagerImpl;
import io.github.aquerr.eaglefactions.managers.PowerManagerImpl;
import io.github.aquerr.eaglefactions.managers.ProtectionManagerImpl;
import io.github.aquerr.eaglefactions.managers.RankManagerImpl;
import io.github.aquerr.eaglefactions.managers.claim.ClaimStrategyManager;
import io.github.aquerr.eaglefactions.managers.claim.provider.DefaultFactionMaxClaimCountProvider;
import io.github.aquerr.eaglefactions.managers.claim.provider.FactionMaxClaimCountByPlayerPowerProvider;
import io.github.aquerr.eaglefactions.managers.power.provider.DefaultFactionMaxPowerProvider;
import io.github.aquerr.eaglefactions.managers.power.provider.DefaultFactionPowerProvider;
import io.github.aquerr.eaglefactions.managers.power.provider.FactionMaxPowerByPlayerMaxPowerProvider;
import io.github.aquerr.eaglefactions.managers.power.provider.FactionPowerByPlayerPowerProvider;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import io.github.aquerr.eaglefactions.messaging.placeholder.parser.EFPlaceholderService;
import io.github.aquerr.eaglefactions.scheduling.EagleFactionsScheduler;
import io.github.aquerr.eaglefactions.scheduling.FactionRemoverTask;
import io.github.aquerr.eaglefactions.scheduling.TabListUpdater;
import io.github.aquerr.eaglefactions.storage.StorageManagerImpl;
import io.github.aquerr.eaglefactions.util.ItemUtil;
import io.github.aquerr.eaglefactions.util.resource.Resource;
import io.github.aquerr.eaglefactions.util.resource.ResourceUtils;
import io.github.aquerr.eaglefactions.version.VersionChecker;
import net.kyori.adventure.text.TextComponent;
import org.apache.logging.log4j.Logger;
import org.bstats.sponge.Metrics;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.parameter.CommonParameters;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.LoadedGameEvent;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.RegisterDataEvent;
import org.spongepowered.api.event.lifecycle.RegisterFactoryEvent;
import org.spongepowered.api.event.lifecycle.RegisterRegistryValueEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static io.github.aquerr.eaglefactions.PluginInfo.PLUGIN_PREFIX_PLAIN;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static net.kyori.adventure.text.Component.text;

@Plugin(PluginInfo.ID)
public class EagleFactionsPlugin implements EagleFactions
{
    public static final String WAR_ZONE_NAME = "WarZone";
    public static final String SAFE_ZONE_NAME = "SafeZone";
    public static final String WILDERNESS_NAME = "Wilderness";

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

    public static Key<Value<Boolean>> IS_EAGLE_FEATHER_KEY;

    private static EagleFactionsPlugin eagleFactions;
    private final Logger logger;

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
    private MessageService messageService;
    private IntegrationManager integrationManager;
    private ClaimStrategyManager claimStrategyManager;

    private boolean isDisabled = false;

    private final PluginContainer pluginContainer;
    private final Path configDir;

    //Integrations
    private final Metrics metrics;
    private EFPlaceholderService efPlaceholderService;

    public static EagleFactionsPlugin getPlugin()
    {
        return eagleFactions;
    }

    @Inject
    public EagleFactionsPlugin(final PluginContainer pluginContainer,
                               @ConfigDir(sharedRoot = false) final Path configDir,
                               Metrics.Factory metricsFactory)
    {
        if (eagleFactions != null)
            throw new IllegalStateException();

        eagleFactions = this;
        this.pluginContainer = pluginContainer;
        this.logger = pluginContainer.logger();
        this.configDir = configDir;
        metrics = metricsFactory.make(6831);
    }

    public PluginContainer getPluginContainer()
    {
        return pluginContainer;
    }

    public Path getConfigDir()
    {
        return configDir;
    }

    @Listener
    public void onPluginConstruct(final ConstructPluginEvent event)
    {
        try
        {
            this.logger.info(PLUGIN_PREFIX_PLAIN + "Preparing wings...");

            setupConfigs();

            this.logger.info(PLUGIN_PREFIX_PLAIN + "Configs loaded.");

            this.logger.info(PLUGIN_PREFIX_PLAIN + "Loading managers and cache...");
            setupManagers();

            this.logger.info(PLUGIN_PREFIX_PLAIN + "Managers loaded.");
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            disablePlugin();
        }
    }

    @Listener
    public void onDataRegister(RegisterDataEvent event)
    {
        IS_EAGLE_FEATHER_KEY = Key.from(this.pluginContainer, "is_eagle_feather", Boolean.class);
        event.register(DataRegistration.of(IS_EAGLE_FEATHER_KEY, ItemStack.class));
    }

    @Listener
    public void onPluginLoad(final LoadedGameEvent event)
    {
        if (this.isDisabled)
            return;

        try
        {
            registerListeners();

            EventRunner.init(Sponge.eventManager());

            //Display some info text in the console.
            this.logger.info(PLUGIN_PREFIX_PLAIN + "==========================================");
            this.logger.info(PLUGIN_PREFIX_PLAIN + "Eagle Factions is ready to use!");
            this.logger.info(PLUGIN_PREFIX_PLAIN + "Thank you for choosing this plugin!");
            this.logger.info(PLUGIN_PREFIX_PLAIN + "Current version: " + PluginInfo.VERSION);
            this.logger.info(PLUGIN_PREFIX_PLAIN + "Have a great time with Eagle Factions! :D");
            this.logger.info(PLUGIN_PREFIX_PLAIN + "==========================================");

            CompletableFuture.runAsync(this::checkVersionAndInform);

            // Reloads storage and cache.
            this.storageManager.reloadStorage();

            determineClaimStrategy();

            initializeIntegrations();

            startFactionsRemover();
            startTabListUpdater();

            preCreateSafeZoneAndWarZone();
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            disablePlugin();
        }
    }

    private void determineClaimStrategy()
    {
        ClaimStrategy claimStrategy = null;

        if (this.configuration.getFactionsConfig().shouldClaimByItems())
            claimStrategy = new ClaimByItemsStrategy(this.factionLogic, ItemUtil.convertToItemStackList(this.configuration.getFactionsConfig().getRequiredItemsToClaim()));
        else
            claimStrategy = new NoCostClaimStrategy(this.factionLogic);

        if (this.configuration.getFactionsConfig().shouldDelayClaim())
            claimStrategy = new DelayedClaimStrategy(claimStrategy, this.configuration.getFactionsConfig().getClaimDelay(), true);

        this.factionLogic.setClaimStrategy(claimStrategy);
    }

    private void preCreateSafeZoneAndWarZone()
    {
        // SafeZone and WarZone factions must always exist!
        if (this.factionLogic.getFactionByName(EagleFactionsPlugin.WAR_ZONE_NAME) == null)
        {
            final Faction warzone = FactionImpl.builder(EagleFactionsPlugin.WAR_ZONE_NAME, text("WZ"))
                    .createdDate(Instant.now())
                    .ranks(this.configuration.getFactionsConfig().getDefaultRanks())
                    .protectionFlags(new HashSet<>(asList(
                    new ProtectionFlagImpl(ProtectionFlagType.PVP, true),
                    new ProtectionFlagImpl(ProtectionFlagType.FIRE_SPREAD, true),
                    new ProtectionFlagImpl(ProtectionFlagType.MOB_GRIEF, true),
                    new ProtectionFlagImpl(ProtectionFlagType.ALLOW_EXPLOSION, true),
                    new ProtectionFlagImpl(ProtectionFlagType.SPAWN_MONSTERS, true),
                    new ProtectionFlagImpl(ProtectionFlagType.SPAWN_ANIMALS, true)
            )))
                    .build();
            this.factionLogic.addFaction(warzone);
        }
        if (this.factionLogic.getFactionByName(EagleFactionsPlugin.SAFE_ZONE_NAME) == null)
        {
            final Faction safezone = FactionImpl.builder(EagleFactionsPlugin.SAFE_ZONE_NAME, text("SZ"))
                    .createdDate(Instant.now())
                    .ranks(this.configuration.getFactionsConfig().getDefaultRanks())
                    .protectionFlags(new HashSet<>(asList(
                            new ProtectionFlagImpl(ProtectionFlagType.PVP, false),
                            new ProtectionFlagImpl(ProtectionFlagType.FIRE_SPREAD, false),
                            new ProtectionFlagImpl(ProtectionFlagType.MOB_GRIEF, false),
                            new ProtectionFlagImpl(ProtectionFlagType.ALLOW_EXPLOSION, false),
                            new ProtectionFlagImpl(ProtectionFlagType.SPAWN_MONSTERS, false),
                            new ProtectionFlagImpl(ProtectionFlagType.SPAWN_ANIMALS, true)
                    )))
                    .build();
            this.factionLogic.addFaction(safezone);
        }
    }

    @Listener
    public void onFactoryRegister(RegisterFactoryEvent event)
    {
        if (this.isDisabled)
            return;

        registerAPI(event);
    }

    private void initializeIntegrations()
    {
        if (isDisabled)
            return;

        setDefaultPermissions();

        this.integrationManager.activateIntegrations();
    }

    @Listener
    public void onRegisterCommand(final RegisterCommandEvent<Command.Parameterized> event)
    {
        if (this.isDisabled)
            return;

        //Register commands...
        initializeCommands(event);
        this.logger.info(PLUGIN_PREFIX_PLAIN + "Commands loaded!");
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

    @Listener
    public void onReload(final RefreshGameEvent event)
    {
        if (isDisabled)
            return;

        try
        {
            this.configuration.reloadConfiguration();
            this.storageManager.reloadStorage();

            this.integrationManager.reloadIntegrations();

            if(event.source() instanceof Player)
            {
                Player player = (Player)event.source();
                player.sendMessage(messageService.resolveMessageWithPrefix("command.reload.config-reloaded"));
            }
        }
        catch (IOException e)
        {
            Player player = (Player)event.source();
            player.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage("error.command.reload")));
            e.printStackTrace();
        }
    }

    @Listener
    public void onRegisterPlaceholderEvent(RegisterRegistryValueEvent.GameScoped event)
    {
        this.efPlaceholderService.onRegisterPlaceholderEvent(event);
    }

    public Logger getLogger()
    {
        return this.logger;
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

    @Override
    public PlaceholderService getPlaceholderService()
    {
        return this.efPlaceholderService;
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
    public Faction.Builder getBuilderForFaction(String name, TextComponent tag)
    {
        return new FactionImpl.BuilderImpl(name, tag);
    }

    @Override
    public FactionPlayer createNewFactionPlayer(final String playerName, final UUID uniqueId, final String factionName, final float power, final float maxpower, final boolean diedInWarZone)
    {
        return new FactionPlayerImpl(playerName, uniqueId, factionName, power, maxpower, diedInWarZone);
    }

    public URI getResource(final String fileName)
    {
        return this.getPluginContainer().locateResource(URI.create(fileName))
                .orElse(null);
    }

    public void printInfo(final String message)
    {
        this.logger.info(PLUGIN_PREFIX_PLAIN + message);
    }

    private void setupConfigs() throws IOException
    {
        Resource resource = ResourceUtils.getResource("assets/eaglefactions/Settings.conf");
        if (resource == null)
            return;

        configuration = new ConfigurationImpl(this.pluginContainer, configDir, resource);
        pvpLogger = PVPLoggerImpl.getInstance(this);
    }

    private void setupManagers()
    {
        this.efPlaceholderService = new EFPlaceholderService(this);

        EFMessageService.init(this.configuration.getFactionsConfig().getLanguageTag());
        this.messageService = EFMessageService.getInstance();
        this.storageManager = new StorageManagerImpl(this, this.configuration.getStorageConfig(), this.configDir);
        this.playerManager = new PlayerManagerImpl(this.storageManager, this.configuration.getPowerConfig());

        this.powerManager = new PowerManagerImpl(this.playerManager, this.configuration.getPowerConfig());
        this.powerManager.addFactionPowerProvider(new DefaultFactionPowerProvider(new FactionPowerByPlayerPowerProvider(this.playerManager)));
        this.powerManager.addFactionMaxPowerProvider(new DefaultFactionMaxPowerProvider(new FactionMaxPowerByPlayerMaxPowerProvider(this.playerManager)));

        this.permsManager = new PermsManagerImpl();

        this.claimStrategyManager = new ClaimStrategyManager(messageService);
        this.factionLogic = new FactionLogicImpl(this.playerManager, this.storageManager, this.messageService, this.claimStrategyManager);
        this.factionLogic.addFactionMaxClaimCountProvider(new DefaultFactionMaxClaimCountProvider(new FactionMaxClaimCountByPlayerPowerProvider(this.powerManager)));


        this.attackLogic = new AttackLogicImpl(this.factionLogic, this.getConfiguration().getFactionsConfig(), this.messageService, this.getConfiguration().getHomeConfig());
        this.protectionManager = new ProtectionManagerImpl(this.factionLogic, this.permsManager, this.playerManager, this.messageService, this.configuration.getProtectionConfig(), this.configuration.getChatConfig(), this.configuration.getFactionsConfig());
        this.invitationManager = new InvitationManagerImpl(this.storageManager, this.factionLogic, this.playerManager, this.messageService, permsManager);
        this.rankManager = new RankManagerImpl(this.storageManager);

        this.integrationManager = new IntegrationManager(this);
    }

    private void startFactionsRemover()
    {
        //Do not turn on faction's remover if max inactive time == 0
        if(this.getConfiguration().getFactionsConfig().getMaxInactiveTime() == 0)
            return;

        EagleFactionsScheduler.getInstance().scheduleWithDelayedIntervalAsync(new FactionRemoverTask(eagleFactions), 0, TimeUnit.SECONDS, 1, TimeUnit.HOURS);
    }

    private void startTabListUpdater()
    {
        EagleFactionsScheduler.getInstance().scheduleWithDelayedIntervalAsync(
                new TabListUpdater(this.configuration, this.playerManager),
                5, TimeUnit.SECONDS,
                10, TimeUnit.SECONDS
        );
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

        registerCommand(singletonList("help"), "command.help.desc", PluginPermissions.HELP_COMMAND, new HelpCommand(this),
                Parameter.integerNumber().optional().key("page").build());
        registerCommand(asList("c", "create"), "command.create.desc", PluginPermissions.CREATE_COMMAND, new CreateCommand(this),
                Parameter.string().key("tag").build(),
                Parameter.string().key("name").build());
        registerCommand(singletonList("disband"), "command.disband.desc", PluginPermissions.DISBAND_COMMAND, new DisbandCommand(this),
                EagleFactionsCommandParameters.optionalFaction());
        registerCommand(singletonList("list"), "command.list.desc", PluginPermissions.LIST_COMMAND, new ListCommand(this));
        registerCommand(singletonList("invite"), "command.invite.desc", PluginPermissions.INVITE_COMMAND, new InviteCommand(this),
                CommonParameters.PLAYER);
        registerCommand(singletonList("kick"), "command.kick.desc", PluginPermissions.KICK_COMMAND, new KickCommand(this),
                EagleFactionsCommandParameters.factionPlayer());
        registerCommand(asList("j", "join"), "command.join.desc", PluginPermissions.JOIN_COMMAND, new JoinCommand(this),
                EagleFactionsCommandParameters.faction());
        registerCommand(singletonList("leave"), "command.leave.desc", PluginPermissions.LEAVE_COMMAND, new LeaveCommand(this));
        registerCommand(asList("v", "version"), "command.version.desc", PluginPermissions.VERSION_COMMAND, new VersionCommand(this));
        registerCommand(asList("i", "info"), "command.info.desc", PluginPermissions.INFO_COMMAND, new InfoCommand(this),
                EagleFactionsCommandParameters.optionalFaction());

        Command.Parameterized listRelationPermissionsCommand = prepareCommand("command.relations.permissions.list.desc",
                PluginPermissions.RELATION_LIST_PERMISSIONS_COMMAND,
                new ListRelationPermissionsCommand(this));

        Command.Parameterized setRelationPermissionCommand = prepareCommand("command.relations.permissions.set.desc",
                PluginPermissions.SET_RELATION_PERMISSION_COMMAND,
                new SetRelationPermissionCommand(this));

        registerCommand(singletonList("relations"), "command.relations.desc",
                PluginPermissions.LIST_RELATIONS_COMMAND,
                new RelationsCommand(this),
                Parameter.enumValue(RelationType.class).key("relation_type").optional().build(),
                Parameter.firstOfBuilder(Parameter.firstOf(List.of(
                        Parameter.subcommand(listRelationPermissionsCommand, "list_permissions"),
                        Parameter.subcommand(setRelationPermissionCommand, "set_permission")
                ))).optional().build());

        registerCommand(asList("p", "player"), "command.player.desc", PluginPermissions.PLAYER_COMMAND, new PlayerCommand(this),
                EagleFactionsCommandParameters.optionalFactionPlayer());
        registerCommand(singletonList("truce"), "command.truce.desc", PluginPermissions.TRUCE_COMMAND, new TruceCommand(this),
                EagleFactionsCommandParameters.faction());
        registerCommand(singletonList("ally"), "command.ally.desc", PluginPermissions.ALLY_COMMAND, new AllyCommand(this),
                EagleFactionsCommandParameters.faction());
        registerCommand(singletonList("enemy"), "command.enemy.desc", PluginPermissions.ENEMY_COMMAND, new EnemyCommand(this),
                EagleFactionsCommandParameters.faction());
//        registerCommand(singletonList("promote"), "command.promote.desc", PluginPermissions.PROMOTE_COMMAND, new PromoteCommand(this),
//                EagleFactionsCommandParameters.factionPlayer());
//        registerCommand(singletonList("demote"), "command.demote.desc", PluginPermissions.DEMOTE_COMMAND, new DemoteCommand(this),
//                EagleFactionsCommandParameters.factionPlayer());

        Command.Parameterized rankSetPermissionCommand = prepareCommand("command.rank.permission.set.desc",
                PluginPermissions.SET_RANK_PERMISSION_COMMAND,
                new SetRankPermissionCommand(this),
                Parameter.enumValue(FactionPermission.class).key("permission").build());

        Command.Parameterized rankSetDisplayName = prepareCommand("command.rank.display-name.set.desc",
                PluginPermissions.SET_RANK_DISPLAY_NAME_COMMAND,
                new SetRankDisplayNameCommand(this),
                Parameter.formattingCodeText().key("display_name").build());

        Command.Parameterized rankListPermissionsCommand = prepareCommand("command.rank.list-permissions.desc",
                PluginPermissions.LIST_RANK_PERMISSIONS_COMMAND,
                new RankListPermissionsCommand(this));

        Command.Parameterized setRankPositionCommand = prepareCommand("command.rank.position.set.desc",
                PluginPermissions.SET_RANK_POSITION_COMMAND,
                new SetRankPositionCommand(this),
                Parameter.integerNumber().key("ladder_position").build());

        Command.Parameterized setRankDisplayInChat = prepareCommand("command.rank.display-in-chat.desc",
                PluginPermissions.SET_RANK_DISPLAY_IN_CHAT_COMMAND,
                new SetRankDisplayInChatCommand(this),
                CommonParameters.BOOLEAN);

        registerCommand(singletonList("rank"), "command.rank.desc",
                PluginPermissions.RANK_COMMANDS,
                new RankInfoCommand(this),
                EagleFactionsCommandParameters.factionRank(),
                Parameter.firstOfBuilder(Parameter.firstOf(List.of(
                        Parameter.subcommand(rankSetPermissionCommand, "set_permission"),
                        Parameter.subcommand(rankSetDisplayName, "set_display_name"),
                        Parameter.subcommand(rankListPermissionsCommand, "list_permissions"),
                        Parameter.subcommand(setRankPositionCommand, "set_ladder_position"),
                        Parameter.subcommand(setRankDisplayInChat, "set_display_in_chat")
                ))).optional().build());

        registerCommand(singletonList("list_ranks"), "command.list-ranks.desc", PluginPermissions.LIST_RANKS_COMMAND, new ListRanksCommand(this),
                EagleFactionsCommandParameters.optionalFaction());
        registerCommand(singletonList("create_rank"), "command.create-rank.desc", PluginPermissions.CREATE_RANK_COMMAND, new CreateRankCommand(this),
                Parameter.string().key("rank_name").build(),
                Parameter.integerNumber().key("ladder_position").optional().build());
        registerCommand(singletonList("assign_rank"), "command.assign-rank.desc", PluginPermissions.ASSIGN_RANK_COMMAND, new AssignRankCommand(this),
                EagleFactionsCommandParameters.factionPlayer(), EagleFactionsCommandParameters.factionRank());
        registerCommand(singletonList("delete_rank"), "command.delete-rank.desc", PluginPermissions.DELETE_RANK, new DeleteRankCommand(this),
                EagleFactionsCommandParameters.factionRank());

        registerCommand(asList("claims", "list_claims"), "command.list-claims.desc", PluginPermissions.CLAIMS_LIST_COMMAND, new ClaimsListCommand(this),
                EagleFactionsCommandParameters.optionalFaction());
        registerCommand(singletonList("claim"), "command.claim.desc", PluginPermissions.CLAIM_COMMAND, new ClaimCommand(this),
                EagleFactionsCommandParameters.optionalFaction());
        registerCommand(singletonList("squareclaim"), "command.square-claim.desc", PluginPermissions.SQUARE_CLAIM_COMMAND, new SquareClaimCommand(this),
                Parameter.integerNumber().key("radius").build());
        registerCommand(singletonList("fillclaim"), "command.fill-claim.desc", PluginPermissions.COMMAND_FILL_CLAIM_COMMAND, new FillCommand(this));
        registerCommand(singletonList("unclaim"), "command.unclaim.desc", PluginPermissions.UNCLAIM_COMMAND, new UnclaimCommand(this));
        registerCommand(singletonList("unclaimall"), "command.unclaim-all.desc", PluginPermissions.UNCLAIM_ALL_COMMAND, new UnclaimAllCommand(this));
        registerCommand(singletonList("map"), "command.map.desc", PluginPermissions.MAP_COMMAND, new MapCommand(this));
        registerCommand(singletonList("sethome"), "command.set-home.desc", PluginPermissions.SET_HOME_COMMAND, new SetHomeCommand(this));
        registerCommand(singletonList("home"), "command.home.desc", PluginPermissions.HOME_COMMAND, new HomeCommand(this),
                EagleFactionsCommandParameters.optionalFaction());
        registerCommand(singletonList("autoclaim"), "command.auto-claim.desc", PluginPermissions.AUTO_CLAIM_COMMAND, new AutoClaimCommand(this));
        registerCommand(singletonList("automap"), "command.auto-map.desc", PluginPermissions.AUTO_MAP_COMMAND, new AutoMapCommand(this));
        registerCommand(singletonList("coords"), "command.coords.desc", PluginPermissions.COORDS_COMMAND, new CoordsCommand(this),
                EagleFactionsCommandParameters.optionalFaction());
        registerCommand(singletonList("admin"), "command.admin.desc", PluginPermissions.ADMIN_MODE_COMMAND, new AdminCommand(this));
        registerCommand(singletonList("setfacion"), "command.set-faction.desc", PluginPermissions.SET_FACTION_COMMAND, new SetFactionCommand(this),
                CommonParameters.PLAYER,
                EagleFactionsCommandParameters.faction(),
                EagleFactionsCommandParameters.factionRank());
        registerCommand(singletonList("setpower"), "command.set-power.desc", PluginPermissions.SET_POWER_COMMAND, new SetPowerCommand(this),
                CommonParameters.PLAYER,
                Parameter.doubleNumber().key("power").build());
        registerCommand(singletonList("setmaxpower"), "command.set-max-power.desc", PluginPermissions.MAX_POWER_COMMAND, new SetMaxPowerCommand(this),
                CommonParameters.PLAYER,
                Parameter.doubleNumber().key("power").build());
        registerCommand(singletonList("setmaxpower_for_everyone"), "command.set-max-power-for-everyone.desc", PluginPermissions.MAX_POWER_FOR_EVERYONE_COMMAND, new SetMaxPowerForEveryoneCommand(this),
                Parameter.doubleNumber().key("power").build());
        registerCommand(singletonList("attack"), "command.attack.desc", PluginPermissions.ATTACK_COMMAND, new AttackCommand(this));
        registerCommand(singletonList("reload"), "command.reload.desc", PluginPermissions.RELOAD_COMMAND, new ReloadCommand(this));
        registerCommand(singletonList("chat"), "command.chat.desc", PluginPermissions.CHAT_COMMAND, new ChatCommand(this),
                Parameter.enumValue(ChatEnum.class).key("chat").optional().build());
        registerCommand(singletonList("top"), "command.top.desc", PluginPermissions.TOP_COMMAND, new TopCommand(this));
        registerCommand(singletonList("setleader"), "command.set-leader.desc", PluginPermissions.SET_LEADER_COMMAND, new SetLeaderCommand(this),
                EagleFactionsCommandParameters.factionPlayer());

        registerCommand(singletonList("tagcolor"), "command.tag-color.desc", PluginPermissions.TAG_COLOR_COMMAND, new TagColorCommand(this),
                Parameter.color().key("color").build());
        registerCommand(singletonList("rename"), "command.rename.desc", PluginPermissions.RENAME_COMMAND, new RenameCommand(this),
                Parameter.string().key("name").build());
        registerCommand(singletonList("tag"), "command.tag.desc", PluginPermissions.TAG_COMMAND, new TagCommand(this),
                Parameter.string().key("tag").build());
        registerCommand(asList("desc", "description"), "command.desc.desc", PluginPermissions.DESCRIPTION_COMMAND, new DescriptionCommand(this),
                Parameter.remainingJoinedStrings().key("description").build());
        registerCommand(singletonList("motd"), "command.motd.desc", PluginPermissions.MOTD_COMMAND, new MotdCommand(this),
                Parameter.remainingJoinedStrings().key("motd").build());
        registerCommand(singletonList("feather"), "command.feather.desc", PluginPermissions.FEATHER_COMMAND, new EagleFeatherCommand(this));
        registerCommand(singletonList("chest"), "command.chest.desc", PluginPermissions.CHEST_COMMAND, new ChestCommand(this),
                EagleFactionsCommandParameters.optionalFaction());
        registerCommand(singletonList("public"), "command.public.desc", PluginPermissions.PUBLIC_COMMAND, new PublicCommand(this),
                EagleFactionsCommandParameters.optionalFaction());
        registerCommand(singletonList("debug"), "command.debug.desc", PluginPermissions.DEBUG_COMMAND, new DebugCommand(this));
        registerCommand(singletonList("createbackup"), "command.create-backup.desc", PluginPermissions.BACKUP_COMMAND, new BackupCommand(this));
        registerCommand(singletonList("restorebackup"), "command.restore-backup.desc", PluginPermissions.RESTORE_BACKUP_COMMAND, new RestoreBackupCommand(this),
                Parameter.string().key("filename")
                        .addParser(new BackupNameArgument.ValueParser(this.storageManager))
                        .completer(new BackupNameArgument.Completer(this.storageManager))
                        .build());
        registerCommand(singletonList("regen"), "command.regen.desc", PluginPermissions.REGEN_COMMAND, new RegenCommand(this),
                EagleFactionsCommandParameters.faction());

        final Command.Parameterized accessPlayerCommand = prepareCommand("command.access.player.desc",
                PluginPermissions.ACCESS_PLAYER_COMMAND,
                new AccessPlayerCommand(this),
                EagleFactionsCommandParameters.factionPlayer());
        final Command.Parameterized accessFactionCommand = prepareCommand("command.access.faction.desc",
                PluginPermissions.ACCESS_FACTION_COMMAND,
                new AccessFactionCommand(this),
                EagleFactionsCommandParameters.factionPlayer());

        final Command.Parameterized accessOwnedByCommand = prepareCommand("command.access.owned-by.desc",
                PluginPermissions.ACCESS_OWNED_BY_COMMAND,
                new OwnedByCommand(this),
                EagleFactionsCommandParameters.factionPlayer());

        final Command.Parameterized accessibleByFactionCommand = prepareCommand("command.access.accessible-by-faction.desc",
                PluginPermissions.ACCESS_NOT_ACCESSIBLE_BY_FACTION_COMMAND,
                new NotAccessibleByFactionCommand(this));

        registerCommand(singletonList("access"), "command.access.desc", PluginPermissions.ACCESS_COMMAND, new AccessCommand(this),
                Parameter.firstOfBuilder(Parameter.firstOf(
                        Parameter.subcommand(accessPlayerCommand, "player", "p"),
                        Parameter.subcommand(accessFactionCommand, "faction", "f"),
                        Parameter.subcommand(accessOwnedByCommand, "ownedBy"),
                        Parameter.subcommand(accessibleByFactionCommand, "notAccessibleByFaction")
                )).optional().build());

        registerCommand(singletonList("flags"), "command.flags.desc", PluginPermissions.FLAGS_COMMAND, new FlagsCommand(this),
                EagleFactionsCommandParameters.faction());
        registerCommand(singletonList("setflag"), "command.setflag.desc", PluginPermissions.SET_FLAG_COMMAND, new SetFlagCommand(this),
                EagleFactionsCommandParameters.faction(),
                Parameter.enumValue(ProtectionFlagType.class).key("flag").build(),
                Parameter.bool().key("value").build());

        // Main command
        Command.Parameterized commandEagleFactions = Command.builder()
                .shortDescription(messageService.resolveComponentWithMessage("command.help.desc"))
                .executor(new HelpCommand(this))
                .addChildren(SUBCOMMANDS)
                .build();

        //Register commands
        event.register(this.pluginContainer, commandEagleFactions, "factions", "faction", "f");
    }

    private Command.Parameterized prepareCommand(String descriptionKey, String permission, CommandExecutor commandExecutor, Parameter... parameters)
    {
        return Command.builder()
                .shortDescription(messageService.resolveComponentWithMessage(descriptionKey))
                .permission(permission)
                .executor(commandExecutor)
                .addParameters(parameters)
                .build();
    }

    private void registerCommand(List<String> aliases, String descriptionKey, String permission, CommandExecutor commandExecutor, Parameter... parameters)
    {
        SUBCOMMANDS.put(aliases, prepareCommand(descriptionKey, permission, commandExecutor, parameters));
    }

    private void registerListeners()
    {
        //Sponge events
        Sponge.eventManager().registerListeners(this.pluginContainer, new EntityDamageListener(this));
        Sponge.eventManager().registerListeners(this.pluginContainer, new PlayerJoinListener(this));
        Sponge.eventManager().registerListeners(this.pluginContainer, new PlayerDeathListener(this));
        Sponge.eventManager().registerListeners(this.pluginContainer, new ChangeBlockEventListener(this));
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
        Sponge.eventManager().registerListeners(this.pluginContainer, new CollideBlockEventListener(this));
        Sponge.eventManager().registerListeners(this.pluginContainer, new CollideEntityEventListener(this));



        Sponge.eventManager().registerListeners(this.pluginContainer, new ChatMessageListener(this));

        //EF events
        Sponge.eventManager().registerListeners(this.pluginContainer, new FactionKickListener(this));
        Sponge.eventManager().registerListeners(this.pluginContainer, new FactionLeaveListener(this));
        Sponge.eventManager().registerListeners(this.pluginContainer, new FactionJoinListener(this));
        Sponge.eventManager().registerListeners(this.pluginContainer, new FactionCreateListener(this));
        Sponge.eventManager().registerListeners(this.pluginContainer, new FactionTagUpdateListener(this));
        Sponge.eventManager().registerListeners(this.pluginContainer, new FactionTagColorUpdateListener(this));
    }

    private void disablePlugin()
    {
        this.isDisabled = true;
        Sponge.eventManager().unregisterListeners(this);
        this.logger.info(PLUGIN_PREFIX_PLAIN + "EagleFactions has been disabled due to an error!");
    }

    private void checkVersionAndInform()
    {
        if (!this.configuration.getVersionConfig().shouldPerformVersionCheck())
        {
            this.logger.info("Version check: Disabled.");
            return;
        }

        if(!VersionChecker.getInstance().isLatest(PluginInfo.VERSION))
        {
            this.logger.info(PLUGIN_PREFIX_PLAIN + "Hey! A new version of " + PluginInfo.NAME + " is available online!");
            this.logger.info("==========================================");
        }
    }
}
