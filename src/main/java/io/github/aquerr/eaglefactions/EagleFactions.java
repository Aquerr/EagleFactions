package io.github.aquerr.eaglefactions;

import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.commands.*;
import io.github.aquerr.eaglefactions.commands.assembly.SubcommandFactory;
import io.github.aquerr.eaglefactions.entities.AllyInvite;
import io.github.aquerr.eaglefactions.entities.ChatEnum;
import io.github.aquerr.eaglefactions.entities.Invite;
import io.github.aquerr.eaglefactions.entities.RemoveEnemy;
import io.github.aquerr.eaglefactions.listeners.GenericListener;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.MessageLoader;
import io.github.aquerr.eaglefactions.logic.PVPLogger;
import io.github.aquerr.eaglefactions.managers.PlayerManager;
import io.github.aquerr.eaglefactions.managers.PowerManager;
import io.github.aquerr.eaglefactions.parsers.FactionNameArgument;
import io.github.aquerr.eaglefactions.storage.HOCONFactionStorage;
import io.github.aquerr.eaglefactions.storage.IStorage;
import io.github.aquerr.eaglefactions.style.StyleModule;
import io.github.aquerr.eaglefactions.version.VersionChecker;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.nio.file.Path;
import java.util.*;

@Plugin(id = PluginInfo.Id, name = PluginInfo.Name, version = PluginInfo.Version, description = PluginInfo.Description, authors = PluginInfo.Author)
public class EagleFactions extends AbstractModule
{
    public static Map<List<String>, CommandSpec> Subcommands;
    public static List<Invite> InviteList;
    public static List<AllyInvite> AllayInviteList;
    public static List<RemoveEnemy> RemoveEnemyList;
    public static List<UUID> AutoClaimList;
    public static List<UUID> AutoMapList;
    public static List<UUID> AdminList;
    public static Map<String, Integer> AttackedFactions;
    public static Map<UUID, Integer> BlockedHome;
    public static Map<UUID, ChatEnum> ChatList;
    public static Map<UUID, Integer> HomeCooldownPlayers;
    private static EagleFactions eagleFactions;

    private PVPLogger _pvpLogger;
    private Injector injector;
    @Inject
    private Logger _logger;
    @Inject
    @ConfigDir(sharedRoot = false)
    private Path _configDir;

    @Provides
    @Named("config dir")
    public Path getConfigDir()
    {
        return _configDir;
    }

    @Provides
    public static EagleFactions getPlugin()
    {
        return eagleFactions;
    }

    @Provides
    @Named("main injector")
    Injector getInjector(){
        return injector;
    }

    @Provides
    @Named("factions")
    public Logger getLogger()
    {
        return _logger;
    }

    @Provides
    List<Invite> getInviteList(){
        return InviteList;
    }

    @Listener
    public void onServerInitialization(GameInitializationEvent event)
    {
        Subcommands = new HashMap<>();
        InviteList = new ArrayList<>();
        AllayInviteList = new ArrayList<>();
        RemoveEnemyList = new ArrayList<>();
        AutoClaimList = new ArrayList<>();
        AutoMapList = new ArrayList<>();
        AdminList = new ArrayList<>();
        AttackedFactions = new HashMap<>();
        BlockedHome = new HashMap<>();
        ChatList = new HashMap<>();
        HomeCooldownPlayers = new HashMap<>();
        eagleFactions = this;

        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.AQUA, "Preparing wings..."));
        injector = Guice.createInjector(this);
        injector.getInstance(IStorage.class);
        cache = injector.getInstance(FactionsCache.class);
        injector.getInstance(FactionLogic.class);
        injector.getInstance(PlayerManager.class);
        injector.getInstance(PowerManager.class);
        injector.getInstance(MessageLoader.class);
        injector.getInstance(PVPLogger.class);
        injector = injector.createChildInjector(injector.getInstance(StyleModule.class), injector.getInstance(SubcommandFactory.class));

        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.AQUA, "Configs loaded..."));

        InitializeCommands();

        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.AQUA, "Commands loaded..."));

        GenericListener.initListeners(injector);

        //Display some info text in the console.
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.GREEN, "=========================================="));
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.AQUA, "Eagle Factions", TextColors.WHITE, " is ready to use!"));
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.WHITE, "Thank you for choosing this plugin!"));
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.WHITE, "Current version: " + PluginInfo.Version));
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.WHITE, "Have a great time with Eagle Factions! :D"));
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.GREEN, "=========================================="));

        if (!VersionChecker.isLatest(PluginInfo.Version))
        {
            Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.GOLD, "Hey! A new version of ", TextColors.AQUA, PluginInfo.Name, TextColors.GOLD, " is available online!"));
            Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.GREEN, "=========================================="));
        }
    }


    private FactionsCache cache;

    @Listener
    public void onServerStop(GameStoppingServerEvent event)
    {
        cache.doSave();
    }

    private void InitializeCommands()
    {
        //Special case!
        Subcommands.put(Collections.singletonList("help"), CommandSpec.builder()
                .description(Text.of("Help"))
                .permission(PluginPermissions.HelpCommand)
                .executor(new HelpCommand())
                .build());

        //TODO: Any subcommands still in this list. If anyone else is also working on this, please remove the Subcommands.put when finished

        //List all factions.
        Subcommands.put(Collections.singletonList("list"), CommandSpec.builder()
                .description(Text.of("List all factions"))
                .permission(PluginPermissions.ListCommand)
                .executor(new ListCommand())
                .build());

        //Info command. Shows info about a faction.
        Subcommands.put(Arrays.asList("i", "info"), CommandSpec.builder()
                .description(Text.of("Show info about a faction"))
                .arguments(new FactionNameArgument(Text.of("faction name")))
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
                .arguments(new FactionNameArgument(Text.of("faction name")))
                .executor(new AddAllyCommand())
                .build();

        //Build remove ally command.
        CommandSpec removeAllyCommand = CommandSpec.builder()
                .description(Text.of("Remove faction from the alliance"))
                .permission(PluginPermissions.RemoveAllyCommand)
                .arguments(new FactionNameArgument(Text.of("faction name")))
                .executor(new RemoveAllyCommand())
                .build();

        //Build alliance commands.
        Subcommands.put(Collections.singletonList("ally"), CommandSpec.builder()
                .description(Text.of("Invite faction to the alliance"))
                .permission(PluginPermissions.AllyCommands)
                .child(addAllyCommand, "a", "add")
                .child(removeAllyCommand, "r", "remove")
                .build());

        //Build add enemy command.
        CommandSpec addEnemyCommand = CommandSpec.builder()
                .description(Text.of("Set faction as enemy"))
                .permission(PluginPermissions.AddEnemyCommand)
                .arguments(new FactionNameArgument(Text.of("faction name")))
                .executor(new AddEnemyCommand())
                .build();

        //Build remove enemy command.
        CommandSpec removeEnemyCommand = CommandSpec.builder()
                .description(Text.of("Remove faction from the enemies"))
                .permission(PluginPermissions.RemoveEnemyCommand)
                .arguments(new FactionNameArgument(Text.of("faction name")))
                .executor(new RemoveEnemyCommand())
                .build();

        //Build enemy commands.
        Subcommands.put(Collections.singletonList("enemy"), CommandSpec.builder()
                .description(Text.of("Declare someone a war"))
                .permission(PluginPermissions.EnemyCommands)
                .child(addEnemyCommand, "a", "add")
                .child(removeEnemyCommand, "r", "remove")
                .build());

        //Officer command. Add or remove officers.
        Subcommands.put(Collections.singletonList("officer"), CommandSpec.builder()
                .description(Text.of("Add or Remove officer"))
                .arguments(GenericArguments.optional(GenericArguments.player(Text.of("player"))))
                .permission(PluginPermissions.OfficerCommand)
                .executor(new OfficerCommand())
                .build());

        //Member command.
        Subcommands.put(Collections.singletonList("member"), CommandSpec.builder()
                .description(Text.of("Add or remove member"))
                .arguments(GenericArguments.optional(GenericArguments.player(Text.of("player"))))
                .permission(PluginPermissions.MemberCommand)
                .executor(new MemberCommand())
                .build());

        //Claim command.
        Subcommands.put(Collections.singletonList("claim"), CommandSpec.builder()
                .description(Text.of("Claim a land for your faction"))
                .permission(PluginPermissions.ClaimCommand)
                .executor(new ClaimCommand())
                .build());

        //Unclaim command.
        Subcommands.put(Collections.singletonList("unclaim"), CommandSpec.builder()
                .description(Text.of("Unclaim a land captured by your faction."))
                .permission(PluginPermissions.UnclaimCommand)
                .executor(new UnclaimCommand())
                .build());

        //Add Unclaimall Command
        Subcommands.put(Collections.singletonList("unclaimall"), CommandSpec.builder()
                .description(Text.of("Remove all claims"))
                .permission(PluginPermissions.UnclaimAllCommand)
                .executor(new UnclaimallCommand())
                .build());

        //Map command
        Subcommands.put(Collections.singletonList("map"), CommandSpec.builder()
                .description(Text.of("Turn on/off factions map"))
                .permission(PluginPermissions.MapCommand)
                .executor(new MapCommand())
                .build());

        //Sethome command
        Subcommands.put(Collections.singletonList("sethome"), CommandSpec.builder()
                .description(Text.of("Set faction's home"))
                .permission(PluginPermissions.SetHomeCommand)
                .executor(new SetHomeCommand())
                .build());

        //Home command
        Subcommands.put(Collections.singletonList("home"), CommandSpec.builder()
                .description(Text.of("Teleport to faction's home"))
                .permission(PluginPermissions.HomeCommand)
                .executor(new HomeCommand())
                .build());

        //Add autoclaim command.
        Subcommands.put(Collections.singletonList("autoclaim"), CommandSpec.builder()
                .description(Text.of("Autoclaim Command"))
                .permission(PluginPermissions.AutoClaimCommand)
                .executor(new AutoClaimCommand())
                .build());

        //Add automap command
        Subcommands.put(Collections.singletonList("automap"), CommandSpec.builder()
                .description(Text.of("Automap command"))
                .permission(PluginPermissions.AutoMapCommand)
                .executor(new AutoMapCommand())
                .build());

        //Add Coords Command
        Subcommands.put(Collections.singletonList("coords"), CommandSpec.builder()
                .description(Text.of("Show your teammates coords"))
                .permission(PluginPermissions.CoordsCommand)
                .executor(new CoordsCommand())
                .build());

        //Add SetPower Command
        Subcommands.put(Collections.singletonList("setpower"), CommandSpec.builder()
                .description(Text.of("Set player's power"))
                .permission(PluginPermissions.SetPowerCommand)
                .arguments(GenericArguments.optional(GenericArguments.player(Text.of("player"))),
                        GenericArguments.optional(GenericArguments.string(Text.of("power"))))
                .executor(new SetPowerCommand())
                .build());

        //Add MaxPower Command
        Subcommands.put(Collections.singletonList("maxpower"), CommandSpec.builder()
                .description(Text.of("Set player's maxpower"))
                .permission(PluginPermissions.MaxPowerCommand)
                .arguments(GenericArguments.optional(GenericArguments.player(Text.of("player"))),
                        GenericArguments.optional(GenericArguments.string(Text.of("power"))))
                .executor(new MaxPowerCommand())
                .build());

        //Add Attack Command
        Subcommands.put(Collections.singletonList("attack"), CommandSpec.builder()
                .description(Text.of("Destroy a claim"))
                .permission(PluginPermissions.AttackCommand)
                .executor(new AttackCommand())
                .build());

        //Reload Command
        Subcommands.put(Collections.singletonList("reload"), CommandSpec.builder()
                .description(Text.of("Reload config file"))
                .permission(PluginPermissions.ReloadCommand)
                .executor(new ReloadCommand())
                .build());

        //Chat Command
        Subcommands.put(Collections.singletonList("chat"), CommandSpec.builder()
                .description(Text.of("Chat command"))
                .permission(PluginPermissions.ChatCommand)
                .arguments(GenericArguments.optional(GenericArguments.enumValue(Text.of("chat"), ChatEnum.class)))
                .executor(new ChatCommand())
                .build());

        //Top Command
        Subcommands.put(Collections.singletonList("top"), CommandSpec.builder()
                .description(Text.of("Top Command"))
                .permission(PluginPermissions.TopCommand)
                .executor(new TopCommand())
                .build());

        //Setleader Command
        Subcommands.put(Collections.singletonList("setleader"), CommandSpec.builder()
                .description(Text.of("Set someone as leader (removes you as a leader if you are one)"))
                .permission(PluginPermissions.SetLeaderCommand)
                .arguments(GenericArguments.optional(GenericArguments.player(Text.of("player"))))
                .executor(new SetLeaderCommand())
                .build());

        //Flags Command
        Subcommands.put(Collections.singletonList("flags"), CommandSpec.builder()
                .description(Text.of("Set flags/privileges for members in faction."))
                .permission(PluginPermissions.FlagsCommand)
                .executor(new FlagsCommand())
                .build());

        //TagColor Command
        Subcommands.put(Collections.singletonList("tagcolor"), CommandSpec.builder()
                .description(Text.of("Change faction's tag color"))
                .permission(PluginPermissions.TagColorCommand)
                .arguments(GenericArguments.optional(GenericArguments.catalogedElement(Text.of("color"), TextColor.class)))
                .executor(new TagColorCommand())
                .build());

        //-----------------------------------------------------------------
        //What will remain once all of the commands have been rewritten:


        CommandSpec commandEagleFactions = CommandSpec.builder()
                .description(Text.of("Help Command"))
                .executor(injector.getInstance(HelpCommand.class))
                .children(injector.getInstance(Key.get(HashMap.class, Names.named("subcommands"))))
                .build();

        //Register commands
        Sponge.getCommandManager().register(this, commandEagleFactions, "factions", "faction", "f");
    }

    @Provides
    public PVPLogger getPVPLogger()
    {
        return _pvpLogger;
    }

    @Override
    protected void configure()
    {
        bind(IStorage.class).to(HOCONFactionStorage.class);
    }
}
