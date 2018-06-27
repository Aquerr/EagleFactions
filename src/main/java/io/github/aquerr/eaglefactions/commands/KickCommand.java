package io.github.aquerr.eaglefactions.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.commands.assembly.FactionPlayerCommand;
import io.github.aquerr.eaglefactions.commands.annotations.RequiredRank;
import io.github.aquerr.eaglefactions.commands.annotations.Subcommand;
import io.github.aquerr.eaglefactions.commands.enums.BasicCommandArgument;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import io.github.aquerr.eaglefactions.managers.PlayerManager;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;
import java.util.logging.Logger;

@Singleton
@RequiredRank(minimumRank = FactionMemberType.OFFICER)
@Subcommand(aliases = {"kick"}, description = "Kicks a player from the faction.", permission = PluginPermissions.KickCommand, arguments = {BasicCommandArgument.PLAYER})
public class KickCommand extends FactionPlayerCommand
{
    @Inject
    public KickCommand(FactionsCache cache, Settings settings, FactionLogic factionLogic, @Named("factions") Logger logger)
    {
        super(cache, settings, factionLogic, logger);
    }

    @Override
    protected boolean executeCommand(Player player, Faction faction, CommandContext context)
    {
        Optional<Player> optionalSelectedPlayer = context.getOne(Text.of("player"));
        Player selectedPlayer = optionalSelectedPlayer.get();
        Optional<Faction> optionalSelectedfaction = cache.getFactionByPlayer(selectedPlayer.getUniqueId());

        if (optionalSelectedfaction.isPresent() && (optionalSelectedfaction.get().Name.equals(faction.Name) || EagleFactions.AdminList.contains(player.getUniqueId())))
        {
            if (PlayerManager.getFactionMemberType(player, faction).compareTo(PlayerManager.getFactionMemberType(selectedPlayer, faction)) < 0 || EagleFactions.AdminList.contains(player.getUniqueId()))
            {
                factionLogic.kickPlayer(selectedPlayer.getUniqueId(), faction.Name);
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.YOU_KICKED + " ", TextColors.GOLD, selectedPlayer.getName(), TextColors.GREEN, " " + PluginMessages.FROM_THE_FACTION));
                selectedPlayer.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.YOU_WERE_KICKED_FROM_THE_FACTION));
                factionLogic.informFaction(optionalSelectedfaction.get(), Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, selectedPlayer.getDisplayNameData().displayName(), TextColors.GREEN, " was kicked from the faction!"));
                EagleFactions.AutoClaimList.remove(selectedPlayer.getUniqueId());
            } else
            {
                player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_CANT_KICK_THIS_PLAYER));
            }
        } else
        {
            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THIS_PLAYER_IS_NOT_IN_YOUR_FACTION));
        }
        return true;
    }
}
