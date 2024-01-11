package io.github.aquerr.eaglefactions.managers;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.entities.AllyRequest;
import io.github.aquerr.eaglefactions.api.entities.ArmisticeRequest;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionInvite;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.entities.TruceRequest;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.InvitationManager;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.api.storage.StorageManager;
import io.github.aquerr.eaglefactions.entities.AllyRequestImpl;
import io.github.aquerr.eaglefactions.entities.ArmisticeRequestImpl;
import io.github.aquerr.eaglefactions.entities.FactionInviteImpl;
import io.github.aquerr.eaglefactions.entities.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.entities.TruceRequestImpl;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import io.github.aquerr.eaglefactions.scheduling.EagleFactionsScheduler;
import io.github.aquerr.eaglefactions.scheduling.RemoveInviteTask;
import io.github.aquerr.eaglefactions.scheduling.RemoveRelationRequestTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class InvitationManagerImpl implements InvitationManager
{
    private final StorageManager storageManager;
    private final FactionLogic factionLogic;
    private final PlayerManager playerManager;
    private final MessageService messageService;

    public InvitationManagerImpl(StorageManager storageManager,
                                 FactionLogic factionLogic,
                                 PlayerManager playerManager,
                                 MessageService messageService)
    {
        this.factionLogic = factionLogic;
        this.playerManager = playerManager;
        this.storageManager = storageManager;
        this.messageService = messageService;
    }

    @Override
    public boolean acceptInvitation(FactionInvite factionInvite)
    {
        checkNotNull(factionInvite);

        final ServerPlayer player = this.playerManager.getPlayer(factionInvite.getInvited().getUniqueId())
            .orElseThrow(() -> new IllegalArgumentException("Player with the given UUID does not exist!"));
        final Faction faction = this.factionLogic.getFactionByName(factionInvite.getSender().getName());
        checkNotNull(faction);

        joinAndNotify(player, faction);
        EagleFactionsPlugin.INVITE_LIST.remove(factionInvite);
        return true;
    }

    @Override
    public boolean sendInvitation(ServerPlayer senderPlayer, ServerPlayer invitedPlayer, final Faction senderFaction)
    {
        final boolean isCancelled = EventRunner.runFactionInviteEventPre(senderPlayer, invitedPlayer, senderFaction);
        if (isCancelled)
            return false;

        final FactionInvite invite = new FactionInviteImpl(senderFaction, playerManager.getFactionPlayer(invitedPlayer.uniqueId()).get());
        EagleFactionsPlugin.INVITE_LIST.add(invite);

        invitedPlayer.sendMessage(getInviteReceivedMessage(senderFaction));
        senderPlayer.sendMessage(messageService.resolveMessageWithPrefix("command.invite.you-invited-player-to-your-faction", invitedPlayer.name()));

        EagleFactionsScheduler.getInstance().scheduleWithDelay(new RemoveInviteTask(invite), 2, TimeUnit.MINUTES);
        EventRunner.runFactionInviteEventPost(senderPlayer, invitedPlayer, senderFaction);
        return true;
    }

    @Override
    public boolean joinAndNotify(ServerPlayer player, Faction faction)
    {
        checkNotNull(player);
        checkNotNull(faction);

        final boolean isCancelled = EventRunner.runFactionJoinEventPre(player, faction);
        if (isCancelled)
            return false;

        final Set<UUID> recruits = new HashSet<>(faction.getRecruits());
        recruits.add(player.uniqueId());
        Faction updatedFaction = faction.toBuilder().setRecruits(recruits).build();
        this.storageManager.saveFaction(updatedFaction);

        //Save player...
        final FactionPlayer factionPlayer = this.playerManager.getFactionPlayer(player.uniqueId()).get();
        final FactionPlayer updatedPlayer = new FactionPlayerImpl(factionPlayer.getName(), factionPlayer.getUniqueId(), faction.getName(), factionPlayer.getPower(), factionPlayer.getMaxPower(), factionPlayer.diedInWarZone());
        this.storageManager.savePlayer(updatedPlayer);

        player.sendMessage(messageService.resolveMessageWithPrefix("command.join.success", faction.getName()));
        EventRunner.runFactionJoinEventPost(player, factionLogic.getFactionByName(faction.getName()));
        return true;
    }

    @Override
    public boolean sendAllyRequest(ServerPlayer player, Faction sourceFaction, Faction targetFaction)
    {
        checkNotNull(player);
        checkNotNull(sourceFaction);
        checkNotNull(targetFaction);

        if(sourceFaction.getName().equals(targetFaction.getName()))
        {
            player.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage("error.relations.you-cannot-invite-yourself-to-the-alliance")));
            return false;
        }
        else if (targetFaction.isSafeZone() || targetFaction.isWarZone())
        {
            player.sendMessage(PluginInfo.ERROR_PREFIX.append(text("You can't do this!", RED)));
            return false;
        }

        boolean hasAdminMode = this.playerManager.hasAdminMode(player.user());
        boolean isLeader = isLeader(sourceFaction, player);
        boolean isOfficer = isOfficer(sourceFaction, player);

        if(sourceFaction.isAlly(targetFaction) && (hasAdminMode || isLeader || isOfficer))
        {
            //Remove ally
            this.factionLogic.removeAlly(sourceFaction.getName(), targetFaction.getName());
            player.sendMessage(messageService.resolveMessageWithPrefix("command.relations.you-disbanded-your-alliance-with-faction", targetFaction.getName()));
            return true;
        }

        if (hasAdminMode)
        {
            forceAllianceBetween(sourceFaction, targetFaction);
            player.sendMessage(messageService.resolveMessageWithPrefix("command.relations.you-force-set-alliance-between-you-and-faction", targetFaction.getName()));
            return true;
        }
        else
        {
            if(!isLeader && !isOfficer)
            {
                player.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage(EFMessageService.ERROR_YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS)));
                return false;
            }
            else if(sourceFaction.isEnemy(targetFaction))
            {
                player.sendMessage(PluginInfo.ERROR_PREFIX
                        .append(messageService.resolveComponentWithMessage("command.relations.you-are-in-war-with-this-faction"))
                        .append(messageService.resolveComponentWithMessage("command.relations.send-this-faction-a-peace-request-before-inviting-them-to-alliance")));
                return false;
            }
            else if(sourceFaction.isTruce(targetFaction))
            {
                player.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage("command.relations.disband-truce-first-before-inviting-this-faction-to-alliance")));
                return false;
            }
            else
            {
                // Preform send operation
                final AllyRequest invite = new AllyRequestImpl(sourceFaction, targetFaction);
                EagleFactionsPlugin.RELATION_INVITES.add(invite);

                final Optional<ServerPlayer> optionalInvitedFactionLeader = this.playerManager.getPlayer(targetFaction.getLeader());

                optionalInvitedFactionLeader.ifPresent(x-> optionalInvitedFactionLeader.get().sendMessage(getAllyInviteGetMessage(sourceFaction)));
                targetFaction.getOfficers().forEach(x-> this.playerManager.getPlayer(x)
                        .ifPresent(y-> y.sendMessage(getAllyInviteGetMessage(sourceFaction))));

                player.sendMessage(messageService.resolveMessageWithPrefix("command.relations.you-have-invited-faction-to-the-alliance", targetFaction.getName()));
                EagleFactionsScheduler.getInstance().scheduleWithDelay(new RemoveRelationRequestTask(invite), 2, TimeUnit.MINUTES);
                return true;
            }
        }
    }

    @Override
    public boolean sendTruceRequest(ServerPlayer player, Faction sourceFaction, Faction targetFaction)
    {
        checkNotNull(player);
        checkNotNull(sourceFaction);
        checkNotNull(targetFaction);

        if(sourceFaction.getName().equals(targetFaction.getName()))
        {
            player.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage("error.relations.you-cannot-invite-yourself-to-the-truce")));
            return false;
        }
        else if (targetFaction.isSafeZone() || targetFaction.isWarZone())
        {
            player.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS)));
            return false;
        }

        boolean hasAdminMode = this.playerManager.hasAdminMode(player.user());
        boolean isLeader = isLeader(sourceFaction, player);
        boolean isOfficer = isOfficer(sourceFaction, player);

        if(sourceFaction.isTruce(targetFaction) && (hasAdminMode || isLeader || isOfficer))
        {
            this.factionLogic.removeAlly(sourceFaction.getName(), targetFaction.getName());

            player.sendMessage(messageService.resolveMessageWithPrefix("command.relations.you-have-disbanded-your-truce-with-faction", targetFaction.getName()));
            return true;
        }

        if(hasAdminMode)
        {
            forceTruceBetween(sourceFaction, targetFaction);
            player.sendMessage(messageService.resolveMessageWithPrefix("command.relations.you-force-set-truce-between-you-and-faction", targetFaction.getName()));
            return true;
        }
        else
        {
            if(!isLeader && !isOfficer)
            {
                player.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage(EFMessageService.ERROR_YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS)));
                return false;
            }
            else if(sourceFaction.isEnemy(targetFaction))
            {
                player.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage("command.relations.you-are-in-war-with-this-faction")
                        .append(text(" "))
                        .append(messageService.resolveComponentWithMessage("command.relations.send-this-faction-a-peace-request-before-inviting-them-to-alliance"))));
                return false;
            }
            else if(sourceFaction.isAlly(targetFaction))
            {
                player.sendMessage(messageService.resolveMessageWithPrefix("command.relations.disband-alliance-first-before-inviting-this-faction-to-truce"));
                return false;
            }
            else
            {
                // Preform send operation
                final TruceRequest invite = new TruceRequestImpl(sourceFaction, targetFaction);
                EagleFactionsPlugin.RELATION_INVITES.add(invite);

                final Optional<ServerPlayer> optionalInvitedFactionLeader = this.playerManager.getPlayer(targetFaction.getLeader());

                optionalInvitedFactionLeader.ifPresent(x-> optionalInvitedFactionLeader.get().sendMessage(getTruceInviteGetMessage(sourceFaction)));
                targetFaction.getOfficers().forEach(x-> this.playerManager.getPlayer(x)
                        .ifPresent(y-> y.sendMessage(getTruceInviteGetMessage(sourceFaction))));

                player.sendMessage(messageService.resolveMessageWithPrefix("command.relations.you-have-invited-faction-to-the-truce", targetFaction.getName()));

                EagleFactionsScheduler.getInstance().scheduleWithDelay(new RemoveRelationRequestTask(invite), 2, TimeUnit.MINUTES);
                return true;
            }
        }
    }

    @Override
    public boolean sendArmisticeOrWarRequest(ServerPlayer player, Faction sourceFaction, Faction targetFaction)
    {
        checkNotNull(player);
        checkNotNull(sourceFaction);
        checkNotNull(targetFaction);

        if(sourceFaction.getName().equals(targetFaction.getName()))
        {
            player.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS)));
            return false;
        }
        else if (targetFaction.isSafeZone() || targetFaction.isWarZone())
        {
            player.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS)));
            return false;
        }

        boolean hasAdminMode = this.playerManager.hasAdminMode(player.user());
        boolean isLeader = isLeader(sourceFaction, player);
        boolean isOfficer = isOfficer(sourceFaction, player);

        if(hasAdminMode)
        {
            if (sourceFaction.isEnemy(targetFaction))
            {
                forceArmisticeBetween(sourceFaction, targetFaction);
                player.sendMessage(messageService.resolveMessageWithPrefix("command.relations.you-force-set-armistice-between-you-and-faction", targetFaction.getName()));
            }
            else
            {
                forceWarBetween(sourceFaction, targetFaction);
                player.sendMessage(messageService.resolveMessageWithPrefix("command.relations.you-force-set-war-between-you-and-faction", targetFaction.getName()));
            }
            return true;
        }
        else
        {
            if (!isLeader && !isOfficer)
            {
                player.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage(EFMessageService.ERROR_YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS)));
                return false;
            }
            else if (sourceFaction.isAlly(targetFaction) || sourceFaction.isTruce(targetFaction))
            {
                player.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage("error.relations.this-faction-is-your-ally")
                        .append(text(" ")
                        .append(messageService.resolveComponentWithMessage("command.relations.disband-alliance-first-before-declaring-war")))));
                return false;
            }
            else if (sourceFaction.isEnemy(targetFaction))
            {
                return sendArmisticeRequest(player, sourceFaction, targetFaction);
            }
            else
            {
                return declareWar(player, sourceFaction, targetFaction);
            }
        }
    }

    @Override
    public void acceptAllyRequest(AllyRequest allyRequest)
    {
        this.factionLogic.addAlly(allyRequest.getSender().getName(), allyRequest.getInvited().getName());
        final Faction senderFaction = this.factionLogic.getFactionByName(allyRequest.getSender().getName());
        final Optional<ServerPlayer> optionalSenderFactionLeader = this.playerManager.getPlayer(senderFaction.getLeader());
        optionalSenderFactionLeader.ifPresent(x -> optionalSenderFactionLeader.get()
                .sendMessage(messageService.resolveMessageWithPrefix("command.relations.faction-accepted-your-invite-to-the-alliance", allyRequest.getInvited().getName())));
        senderFaction.getOfficers().forEach(x -> this.playerManager.getPlayer(x)
                .ifPresent(y -> y.sendMessage(messageService.resolveMessageWithPrefix("command.relations.faction-accepted-your-invite-to-the-alliance", allyRequest.getInvited().getName()))));
        EagleFactionsPlugin.RELATION_INVITES.remove(allyRequest);
    }

    @Override
    public void acceptTruceRequest(TruceRequest truceRequest)
    {
        this.factionLogic.addTruce(truceRequest.getSender().getName(), truceRequest.getInvited().getName());
        final Faction senderFaction = this.factionLogic.getFactionByName(truceRequest.getSender().getName());
        final Optional<ServerPlayer> optionalSenderFactionLeader = this.playerManager.getPlayer(senderFaction.getLeader());
        optionalSenderFactionLeader.ifPresent(x -> optionalSenderFactionLeader.get().sendMessage(messageService.resolveMessageWithPrefix("command.relations.faction-accepted-your-invite-to-the-truce", truceRequest.getInvited().getName())));
        senderFaction.getOfficers().forEach(x-> this.playerManager.getPlayer(x)
                .ifPresent(y -> y.sendMessage(messageService.resolveMessageWithPrefix("command.relations.faction-accepted-your-invite-to-the-truce", truceRequest.getInvited().getName()))));
        EagleFactionsPlugin.RELATION_INVITES.remove(truceRequest);
    }

    @Override
    public void acceptArmisticeRequest(ArmisticeRequest armisticeRequest)
    {
        this.factionLogic.removeEnemy(armisticeRequest.getInvited().getName(), armisticeRequest.getSender().getName());
        final Faction senderFaction = this.factionLogic.getFactionByName(armisticeRequest.getSender().getName());
        final Optional<ServerPlayer> senderFactionLeader = this.playerManager.getPlayer(senderFaction.getLeader());
        senderFactionLeader.ifPresent(x->x.sendMessage(messageService.resolveMessageWithPrefix("command.relations.faction-accepted-your-armistice-request", armisticeRequest.getInvited().getName())));
        senderFaction.getOfficers().forEach(x-> this.playerManager.getPlayer(x)
                .ifPresent(y->y.sendMessage(messageService.resolveMessageWithPrefix("command.relations.faction-accepted-your-armistice-request", armisticeRequest.getInvited().getName()))));
        EagleFactionsPlugin.RELATION_INVITES.remove(armisticeRequest);
    }

    private boolean isLeader(final Faction faction, final Player player)
    {
        return player.uniqueId().equals(faction.getLeader());
    }

    private boolean isOfficer(final Faction faction, final Player player)
    {
        return faction.getOfficers().contains(player.uniqueId());
    }

    private void forceAllianceBetween(Faction senderFaction, Faction selectedFaction)
    {
        this.factionLogic.removeEnemy(senderFaction.getName(), selectedFaction.getName());
        this.factionLogic.removeTruce(senderFaction.getName(), selectedFaction.getName());
        this.factionLogic.addAlly(senderFaction.getName(), selectedFaction.getName());
    }

    private void forceTruceBetween(Faction senderFaction, Faction targetFaction)
    {
        this.factionLogic.removeEnemy(senderFaction.getName(), targetFaction.getName());
        this.factionLogic.removeAlly(senderFaction.getName(), targetFaction.getName());
        this.factionLogic.addTruce(senderFaction.getName(), targetFaction.getName());
    }

    private void forceWarBetween(Faction senderFaction, Faction targetFaction)
    {
        this.factionLogic.removeAlly(senderFaction.getName(), targetFaction.getName());
        this.factionLogic.removeTruce(senderFaction.getName(), targetFaction.getName());
        this.factionLogic.addEnemy(senderFaction.getName(), targetFaction.getName());
    }

    private void forceArmisticeBetween(Faction senderFaction, Faction targetFaction)
    {
        this.factionLogic.removeEnemy(senderFaction.getName(), targetFaction.getName());
    }

    private boolean sendArmisticeRequest(Player player, Faction sourceFaction, Faction targetFaction)
    {
        final ArmisticeRequest armisticeRequest = new ArmisticeRequestImpl(sourceFaction, targetFaction);
        if(EagleFactionsPlugin.RELATION_INVITES.contains(armisticeRequest))
        {
            player.sendMessage(PluginInfo.ERROR_PREFIX.append(messageService.resolveComponentWithMessage("error.relations.you-have-already-sent-armistice-request")));
            return false;
        }
        EagleFactionsPlugin.RELATION_INVITES.add(armisticeRequest);

        final Optional<ServerPlayer> targetFactionLeader = this.playerManager.getPlayer(targetFaction.getLeader());
        targetFactionLeader.ifPresent(x->x.sendMessage(getArmisticeRequestMessage(sourceFaction)));
        targetFaction.getOfficers().forEach(x-> playerManager.getPlayer(x).ifPresent(y->y.sendMessage(getArmisticeRequestMessage(sourceFaction))));
        player.sendMessage(messageService.resolveMessageWithPrefix("command.relations.you-have-sent-armistice-request-to-faction", targetFaction.getName()));

        EagleFactionsScheduler.getInstance().scheduleWithDelayAsync(new RemoveRelationRequestTask(armisticeRequest), 2, TimeUnit.MINUTES);
        return true;
    }

    private boolean declareWar(Player player, Faction sourceFaction, Faction targetFaction)
    {
        factionLogic.addEnemy(sourceFaction.getName(), targetFaction.getName());
        player.sendMessage(messageService.resolveMessageWithPrefix("command.relations.your-faction-is-now-enemies-with-faction", targetFaction.getName()));

        //Send message to enemy leader.
        playerManager.getPlayer(targetFaction.getLeader()).ifPresent(x->x.sendMessage(messageService.resolveMessageWithPrefix("command.relations.faction-has-declared-you-war", sourceFaction.getName())));

        //Send message to enemy officers.
        targetFaction.getOfficers().forEach(x-> playerManager.getPlayer(x).ifPresent(y-> y.sendMessage(messageService.resolveMessageWithPrefix("command.relations.faction-has-declared-you-war", sourceFaction.getName()))));
        return true;
    }

    private Component getInviteReceivedMessage(final Faction senderFaction)
    {
        final TextComponent clickHereText = messageService.resolveComponentWithMessage("command.relations.click-here")
                .clickEvent(ClickEvent.runCommand("/f join " + senderFaction.getName()))
                .hoverEvent(HoverEvent.showText(text("/f join " + senderFaction.getName(), GOLD)));

        return messageService.resolveMessageWithPrefix("command.relations.faction-has-sent-you-invite", senderFaction.getName())
                .append(messageService.resolveComponentWithMessage("command.relations.you-have-two-minutes-to-accept-it"))
                .append(newline())
                .append(messageService.resolveComponentWithMessage("command.relations.click-here-to-accept-invitation-or-type",
                        clickHereText,
                        text(" /f join " + senderFaction.getName(), GOLD)));
    }

    private Component getAllyInviteGetMessage(final Faction senderFaction)
    {
        final TextComponent clickHereText = messageService.resolveComponentWithMessage("command.relations.click-here")
                .clickEvent(ClickEvent.runCommand("/f ally " + senderFaction.getName()))
                .hoverEvent(HoverEvent.showText(text("/f ally " + senderFaction.getName(), GOLD)));

        return messageService.resolveMessageWithPrefix("command.relations.faction-has-sent-you-invite-to-the-alliance", senderFaction.getName())
                .append(messageService.resolveComponentWithMessage("command.relations.you-have-two-minutes-to-accept-it"))
                .append(newline())
                .append(messageService.resolveComponentWithMessage("command.relations.click-here-to-accept-invitation-or-type",
                        clickHereText,
                        text(" /f ally " + senderFaction.getName(), GOLD)));
    }

    private Component getTruceInviteGetMessage(final Faction senderFaction)
    {
        final TextComponent clickHereText = messageService.resolveComponentWithMessage("command.relations.click-here")
                .clickEvent(ClickEvent.runCommand("/f truce " + senderFaction.getName()))
                .hoverEvent(HoverEvent.showText(text("/f truce " + senderFaction.getName(), GOLD)));

        return messageService.resolveMessageWithPrefix("command.relations.faction-has-sent-you-invite-to-the-truce", senderFaction.getName())
                .append(messageService.resolveComponentWithMessage("command.relations.you-have-two-minutes-to-accept-it"))
                .append(newline())
                .append(messageService.resolveComponentWithMessage("command.relations.click-here-to-accept-invitation-or-type",
                        clickHereText,
                        text(" /f truce " + senderFaction.getName(), GOLD)));
    }

    private Component getArmisticeRequestMessage(final Faction senderFaction)
    {
        final TextComponent clickHereText = messageService.resolveComponentWithMessage("command.relations.click-here")
                .clickEvent(ClickEvent.runCommand("/f enemy " + senderFaction.getName()))
                .hoverEvent(HoverEvent.showText(text("/f enemy " + senderFaction.getName(), GOLD)));

        return messageService.resolveMessageWithPrefix("command.relations.faction-has-sent-you-armistice-request", senderFaction.getName())
                .append(messageService.resolveComponentWithMessage("command.relations.you-have-two-minutes-to-accept-it"))
                .append(newline())
                .append(messageService.resolveComponentWithMessage("command.relations.click-here-to-accept-invitation-or-type",
                        clickHereText,
                        text(" /f enemy " + senderFaction.getName(), GOLD)));
    }
}
