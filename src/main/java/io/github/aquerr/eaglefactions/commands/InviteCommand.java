package io.github.aquerr.eaglefactions.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.commands.Helper.BasicCommandArgument;
import io.github.aquerr.eaglefactions.commands.Helper.FactionPlayerCommand;
import io.github.aquerr.eaglefactions.commands.Helper.RequiredRank;
import io.github.aquerr.eaglefactions.commands.Helper.Subcommand;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.entities.Invite;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import io.github.aquerr.eaglefactions.managers.FlagManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Singleton
@RequiredRank(minimumRank = FactionMemberType.OFFICER)
@Subcommand(aliases = {"invite"}, description = "Invites a player to the faction.", permission = PluginPermissions.InviteCommand, arguments = {BasicCommandArgument.PLAYER})
public class InviteCommand extends FactionPlayerCommand
{

    private List<Invite> inviteList;

    @Inject
    public InviteCommand(FactionsCache cache, Settings settings, FactionLogic factionLogic, @Named("factions") Logger logger, List<Invite> inviteList)
    {
        super(cache, settings, factionLogic, logger);
        this.inviteList = inviteList;
    }

    @Override
    protected boolean executeCommand(Player player, Faction faction, CommandContext context)
    {
        Optional<Player> invitedPlayer = context.getOne("player"); //Sponge will make sure it is not empty
        if (settings.isPlayerLimit() && factionLogic.getFactionSize(faction) >= settings.getPlayerLimit())
        {
            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_CANT_INVITE_MORE_PLAYERS_TO_YOUR_FACTION + " " + PluginMessages.FACTIONS_PLAYER_LIMIT_HAS_BEEN_REACHED));
        }else if (!cache.getFactionByPlayer(invitedPlayer.get().getUniqueId()).isPresent())
        {
            Invite invite = new Invite(faction.Name, invitedPlayer.get().getUniqueId());
            inviteList.add(invite);

            invitedPlayer.get().sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.FACTION + " ", TextColors.GOLD, faction.Name, TextColors.GREEN, " " + PluginMessages.HAS_SENT_YOU_AN_INVITE + " " + PluginMessages.YOU_HAVE_TWO_MINUTES_TO_ACCEPT_IT +
                    " " + PluginMessages.TYPE + " ", TextColors.GOLD, "/f join " + faction.Name, TextColors.WHITE, " " + PluginMessages.TO_JOIN));
            factionLogic.informFaction(faction, Text.of(PluginInfo.PluginPrefix, TextColors.GOLD, invitedPlayer.get().getName(), TextColors.GREEN, " has been invited " + PluginMessages.TO_YOUR_FACTION));

            Task.builder().execute(() -> {
                    if (inviteList.contains(invite))
                    {
                        inviteList.remove(invite);
                        invitedPlayer.get().sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Your invitation to the faction " + faction.Name + " expired!"));
                        factionLogic.informFaction(faction, Text.of(PluginInfo.ErrorPrefix, TextColors.RED, invitedPlayer.get().getDisplayNameData(), " did not accept their invite in time!"));
                    }
                }).delay(2, TimeUnit.MINUTES).name("EagleFaction - Remove Invite").submit(EagleFactions.getPlugin());
        } else
        {
            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.PLAYER_IS_ALREADY_IN_A_FACTION));
        }
        return true;
    }
}
