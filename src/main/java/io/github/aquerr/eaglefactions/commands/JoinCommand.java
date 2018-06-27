package io.github.aquerr.eaglefactions.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.commands.annotations.AllowedGroups;
import io.github.aquerr.eaglefactions.commands.annotations.RequiresFaction;
import io.github.aquerr.eaglefactions.commands.annotations.Subcommand;
import io.github.aquerr.eaglefactions.commands.assembly.FactionCommand;
import io.github.aquerr.eaglefactions.commands.enums.BasicCommandArgument;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.Invite;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Singleton
@AllowedGroups
@RequiresFaction(value = false)
@Subcommand(aliases = {"j", "join"}, description = "Join a specific faction.", permission = PluginPermissions.JoinCommand, arguments = {BasicCommandArgument.IDENTIFIER})
public class JoinCommand extends FactionCommand
{
    private List<Invite> inviteList;

    @Inject
    public JoinCommand(FactionsCache cache, Settings settings, FactionLogic factionLogic, @Named("factions") Logger logger, List<Invite> inviteList)
    {
        super(cache, settings, factionLogic, logger);
        this.inviteList = inviteList;
    }

    @Override
    protected boolean executeCommand(CommandSource source, CommandContext context)
    {
        Player player = (Player) source;
        Optional<String> optionalFactionName = context.getOne("identifier");
        Optional<Faction> faction = factionLogic.getFactionByIdentifier(optionalFactionName);

        if (!faction.isPresent())
        {
            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Could not find a faction or player by the name of \"", TextColors.GOLD, optionalFactionName.get(), TextColors.RED, "\"!"));
            return true;
        } else
        {
            if (EagleFactions.AdminList.contains(player.getUniqueId()) || inviteList.stream().anyMatch(e -> e.getPlayerUUID().equals(player.getUniqueId()) && e.getFactionName().equals(faction.get().Name)))
            {
                if (settings.isPlayerLimit() && factionLogic.getFactionSize(faction.get()) >= settings.getPlayerLimit() && !EagleFactions.AdminList.contains(player.getUniqueId()))
                {
                    player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "This faction is full. No more players can join until someone leaves the faction."));
                } else
                {
                    factionLogic.joinFaction(player.getUniqueId(), faction.get().Name);
                    source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.SUCCESSFULLY_JOINED_FACTION + " ", TextColors.GOLD, faction.get().Name));
                    factionLogic.informFaction(faction.get(), Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, ((Player) source).getDisplayNameData().displayName(), TextColors.GREEN, " joined the faction!"));
                    if (!EagleFactions.AdminList.contains(player.getUniqueId()))
                    {
                        inviteList.remove(new Invite(faction.get().Name, player.getUniqueId()));
                    }
                }
            } else
            {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_HAVENT_BEEN_INVITED_TO_THIS_FACTION));
            }
        }
        return true;
    }
}
