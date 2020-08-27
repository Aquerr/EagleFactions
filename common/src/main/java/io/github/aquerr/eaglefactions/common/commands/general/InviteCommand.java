package io.github.aquerr.eaglefactions.common.commands.general;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.Invite;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.common.events.EventRunner;
import io.github.aquerr.eaglefactions.common.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import io.github.aquerr.eaglefactions.common.messaging.Placeholders;
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

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class InviteCommand extends AbstractCommand
{
    private final FactionsConfig factionsConfig;

    public InviteCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        final Player invitedPlayer = context.requireOne("player");

        if(!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        final Player senderPlayer = (Player)source;
        final Optional<Faction> optionalSenderFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(senderPlayer.getUniqueId());

        if(!optionalSenderFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        final Faction senderFaction = optionalSenderFaction.get();

        if (!super.getPlugin().getPermsManager().canInvite(senderPlayer.getUniqueId(), senderFaction))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.PLAYERS_WITH_YOUR_RANK_CANT_INVITE_PLAYERS_TO_FACTION));

        if(this.factionsConfig.isPlayerLimit())
        {
            int playerCount = 0;
            playerCount += senderFaction.getLeader().toString().equals("") ? 0 : 1;
            playerCount += senderFaction.getOfficers().isEmpty() ? 0 : senderFaction.getOfficers().size();
            playerCount += senderFaction.getMembers().isEmpty() ? 0 : senderFaction.getMembers().size();
            playerCount += senderFaction.getRecruits().isEmpty() ? 0 : senderFaction.getRecruits().size();

            if(playerCount >= this.factionsConfig.getPlayerLimit())
            {
                senderPlayer.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_INVITE_MORE_PLAYERS_TO_YOUR_FACTION + " " + Messages.FACTIONS_PLAYER_LIMIT_HAS_BEEN_REACHED));
                return CommandResult.success();
            }
        }

        if(super.getPlugin().getFactionLogic().getFactionByPlayerUUID(invitedPlayer.getUniqueId()).isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.PLAYER_IS_ALREADY_IN_A_FACTION));

        final boolean isCancelled = EventRunner.runFactionInviteEventPre(senderPlayer, invitedPlayer, senderFaction);
        if (isCancelled)
            return CommandResult.success();

        final Invite invite = new Invite(senderFaction.getName(), invitedPlayer.getUniqueId());
        EagleFactionsPlugin.INVITE_LIST.add(invite);

        invitedPlayer.sendMessage(getInviteGetMessage(senderFaction));
        senderPlayer.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX,TextColors.GREEN, Messages.YOU_INVITED + " ", TextColors.GOLD, invitedPlayer.getName(), TextColors.GREEN, " " + Messages.TO_YOUR_FACTION));

        final Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

        taskBuilder.execute(() -> EagleFactionsPlugin.INVITE_LIST.remove(invite)).delay(2, TimeUnit.MINUTES).name("EagleFaction - Remove Invite").submit(EagleFactionsPlugin.getPlugin());
        EventRunner.runFactionInviteEventPost(senderPlayer, invitedPlayer, senderFaction);
        return CommandResult.success();
    }

	private Text getInviteGetMessage(final Faction senderFaction)
    {
        final Text clickHereText = Text.builder()
                .append(Text.of(TextColors.AQUA, "[", TextColors.GOLD, Messages.CLICK_HERE, TextColors.AQUA, "]"))
                .onClick(TextActions.runCommand("/f join " + senderFaction.getName()))
                .onHover(TextActions.showText(Text.of(TextColors.GOLD, "/f join " + senderFaction.getName())))
                .build();

        return Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.FACTION_HAS_SENT_YOU_AN_INVITE, TextColors.GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, senderFaction.getName()))),
                Messages.YOU_HAVE_TWO_MINUTES_TO_ACCEPT_IT,
                "\n", clickHereText, TextColors.GREEN, " ", Messages.TO_ACCEPT_INVITATION_OR_TYPE, " ", TextColors.GOLD, "/f join " + senderFaction.getName());
    }
}
