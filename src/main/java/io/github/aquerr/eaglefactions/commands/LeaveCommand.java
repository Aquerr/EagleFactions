package io.github.aquerr.eaglefactions.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.commands.annotations.Subcommand;
import io.github.aquerr.eaglefactions.commands.assembly.FactionPlayerCommand;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.logging.Logger;

@Singleton
@Subcommand(aliases = {"leave", "exit"}, description = "Leave a faction.", permission = PluginPermissions.LeaveCommand)
public class LeaveCommand extends FactionPlayerCommand
{
    @Inject
    public LeaveCommand(FactionsCache cache, Settings settings, FactionLogic factionLogic, @Named("factions") Logger logger)
    {
        super(cache, settings, factionLogic, logger);
    }

    @Override
    protected boolean executeCommand(Player player, Faction faction, CommandContext context)
    {
        if (!faction.Leader.equals(player.getUniqueId().toString()))
        {
            faction.Recruits.remove(player.getUniqueId().toString());
            faction.Members.remove(player.getUniqueId().toString());
            faction.Officers.remove(player.getUniqueId().toString());
            cache.removePlayer(player.getUniqueId());
            cache.saveFaction(faction);
            player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.YOU_LEFT_FACTION + " ", TextColors.GOLD, faction.Name));
            factionLogic.informFaction(faction, Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, player.getDisplayNameData().displayName(), TextColors.WHITE, " left the faction!"));
            EagleFactions.AutoClaimList.remove(player.getUniqueId());
        } else
        {
            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_CANT_LEAVE_YOUR_FACTION_BECAUSE_YOU_ARE_ITS_LEADER + " " + PluginMessages.DISBAND_YOUR_FACTION_OR_SET_SOMEONE_AS_LEADER));
        }
        return true;
    }
}
