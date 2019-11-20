package io.github.aquerr.eaglefactions.common.commands;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.ArmisticeRequest;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.message.PluginMessages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class EnemyCommand extends AbstractCommand
{
    public EnemyCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        final String factionName = context.requireOne(Text.of("faction name"));
        if(!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        final Player player = (Player) source;
        final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        final Faction enemyFaction = getPlugin().getFactionLogic().getFactionByName(factionName);

        if(enemyFaction == null)
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.THERE_IS_NO_FACTION_CALLED + " ", TextColors.GOLD, factionName, TextColors.RED, "!"));

        if(!optionalPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        final Faction playerFaction = optionalPlayerFaction.get();

        if(playerFaction.getName().equals(enemyFaction.getName()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "Are you serious? You cannot be in war with yourself!"));

        if(EagleFactionsPlugin.ADMIN_MODE_PLAYERS.contains(player.getUniqueId()))
        {
            if(playerFaction.getAlliances().contains(enemyFaction.getName()))
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.THIS_FACTION_IS_YOUR_ALLY + " " + PluginMessages.DISBAND_ALLIANCE_FIRST_TO_DECLARE_A_WAR));

            if(!playerFaction.getEnemies().contains(enemyFaction.getName()))
            {
                getPlugin().getFactionLogic().addEnemy(playerFaction.getName(), enemyFaction.getName());
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.FACTION_HAS_BEEN_ADDED_TO_THE_ENEMIES));
            }
            else
            {
                getPlugin().getFactionLogic().removeEnemy(playerFaction.getName(), enemyFaction.getName());
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.YOU_REMOVED_WAR_STATE_WITH + " ", TextColors.GOLD, enemyFaction, TextColors.GREEN, "!"));
            }
            return CommandResult.success();
        }

        if(!playerFaction.getLeader().equals(player.getUniqueId()) && !playerFaction.getOfficers().contains(player.getUniqueId()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS));

        if(playerFaction.getAlliances().contains(enemyFaction.getName()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.THIS_FACTION_IS_YOUR_ALLY + " " + PluginMessages.DISBAND_ALLIANCE_FIRST_TO_DECLARE_A_WAR));

        if(!playerFaction.getEnemies().contains(enemyFaction.getName()))
        {
            super.getPlugin().getFactionLogic().addEnemy(playerFaction.getName(), enemyFaction.getName());
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.YOUR_FACTION_IS_NOW + " ", TextColors.RED, PluginMessages.ENEMIES + " ", TextColors.WHITE, PluginMessages.WITH + " " + enemyFaction.getName() + "!"));

            //Send message to enemy leader.
            super.getPlugin().getPlayerManager().getPlayer(enemyFaction.getLeader()).ifPresent(x->x.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, PluginMessages.FACTION + " ", TextColors.GOLD, playerFaction.getName(), TextColors.RED, " " + PluginMessages.HAS_DECLARED_YOU_A_WAR + "!")));

            //Send message to enemy officers.
            enemyFaction.getOfficers().forEach(x-> super.getPlugin().getPlayerManager().getPlayer(x).ifPresent(y-> y.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.FACTION + " ", TextColors.GOLD, playerFaction.getName(), TextColors.WHITE, " " + PluginMessages.HAS_DECLARED_YOU_A_WAR + "!"))));
            return CommandResult.success();
        }
        else
        {
            final ArmisticeRequest checkRemove = new ArmisticeRequest(enemyFaction.getName(), playerFaction.getName());
            if(EagleFactionsPlugin.ARMISTICE_REQUEST_LIST.contains(checkRemove))
            {
                super.getPlugin().getFactionLogic().removeEnemy(enemyFaction.getName(), playerFaction.getName());
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.YOU_HAVE_ACCEPTED_ARMISITCE_REQUEST_FROM + " ", TextColors.GOLD, enemyFaction.getName(), TextColors.GREEN, "!"));

                final Optional<Player> enemyFactionLeader = super.getPlugin().getPlayerManager().getPlayer(enemyFaction.getLeader());
                enemyFactionLeader.ifPresent(x->x.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.FACTION + " ", TextColors.GOLD, playerFaction.getName(), TextColors.GREEN, " accepted your armistice request!")));
                enemyFaction.getOfficers().forEach(x-> super.getPlugin().getPlayerManager().getPlayer(x).ifPresent(y->y.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.FACTION + " ", TextColors.GOLD, playerFaction.getName(), TextColors.GREEN, " accepted your armistice request!"))));

                EagleFactionsPlugin.ARMISTICE_REQUEST_LIST.remove(checkRemove);
            }
            else if(!EagleFactionsPlugin.ARMISTICE_REQUEST_LIST.contains(checkRemove))
            {

                final ArmisticeRequest armisticeRequest = new ArmisticeRequest(playerFaction.getName(), enemyFaction.getName());
                if(EagleFactionsPlugin.ARMISTICE_REQUEST_LIST.contains(armisticeRequest))
                {
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, "You have already sent an armistice request to this faction. Wait for their response!"));
                    return CommandResult.success();
                }
                EagleFactionsPlugin.ARMISTICE_REQUEST_LIST.add(armisticeRequest);

                final Optional<Player> enemyFactionLeader = super.getPlugin().getPlayerManager().getPlayer(enemyFaction.getLeader());
                enemyFactionLeader.ifPresent(x->x.sendMessage(getArmisticeRequestMessage(playerFaction)));
                enemyFaction.getOfficers().forEach(x-> super.getPlugin().getPlayerManager().getPlayer(x).ifPresent(y->y.sendMessage(getArmisticeRequestMessage(playerFaction))));
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.WHITE, PluginMessages.YOU_REQUESTED_ARMISTICE_WITH_FACTION + " ", TextColors.GOLD, enemyFaction.getName(), TextColors.RESET, "!"));

                final Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
                taskBuilder.execute(() -> EagleFactionsPlugin.ARMISTICE_REQUEST_LIST.remove(armisticeRequest)).delay(2, TimeUnit.MINUTES).name("EagleFaction - Remove Enemy").submit(super.getPlugin());
                return CommandResult.success();
            }
        }

        return CommandResult.success();
    }

    private Text getArmisticeRequestMessage(final Faction senderFaction)
    {
        final Text clickHereText = Text.builder()
                .append(Text.of(TextColors.AQUA, "[", TextColors.GOLD, PluginMessages.CLICK_HERE, TextColors.AQUA, "]"))
                .onClick(TextActions.runCommand("/f enemy " + senderFaction.getName()))
                .onHover(TextActions.showText(Text.of(TextColors.GOLD, "/f enemy " + senderFaction.getName()))).build();

        return Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.WHITE, PluginMessages.FACTION + " ", TextColors.GOLD, senderFaction.getName(), TextColors.WHITE,
                " " + PluginMessages.WANTS_TO_END_THE + " ", TextColors.RED, PluginMessages.WAR + " ", TextColors.WHITE, PluginMessages.WITH_YOUR_FACTION + "\n",
                TextColors.WHITE, " " + PluginMessages.YOU_HAVE_TWO_MINUTES_TO_ACCEPT_IT + "\n",
                clickHereText, " " + PluginMessages.TO_ACCEPT_IT_OR_TYPE + " ", TextColors.GOLD, "/f enemy " + senderFaction.getName());
    }
}
