package io.github.aquerr.eaglefactions.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.commands.annotations.AllowedGroups;
import io.github.aquerr.eaglefactions.commands.annotations.Subcommand;
import io.github.aquerr.eaglefactions.commands.assembly.FactionCommand;
import io.github.aquerr.eaglefactions.commands.enums.BasicCommandArgument;
import io.github.aquerr.eaglefactions.commands.enums.CommandUser;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;
import java.util.logging.Logger;

@Singleton
@AllowedGroups(getGroups = {CommandUser.PLAYER, CommandUser.CONSOLE})
@Subcommand(aliases = {"admin"}, description = "Toggle admin mode", permission = PluginPermissions.AdminCommand, arguments = {BasicCommandArgument.OPTIONAL_PLAYER})
public class AdminCommand extends FactionCommand
{

    @Inject
    public AdminCommand(FactionsCache cache, Settings settings, FactionLogic factionLogic, @Named("factions") Logger logger)
    {
        super(cache, settings, factionLogic, logger);
    }

    @Override
    protected boolean executeCommand(CommandSource source, CommandContext context)
    {
        Optional<Player> optionalPlayer = context.getOne("optional player");
        if (!optionalPlayer.isPresent())
        {
            if (source instanceof Player)
            {
                optionalPlayer = Optional.of((Player) this);
            } else
            {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must specify a player to enable admin mode for!"));
                return true;
            }
        }

        if (EagleFactions.AdminList.contains(optionalPlayer.get().getUniqueId()))
        {
            EagleFactions.AdminList.remove(optionalPlayer.get().getUniqueId());
            source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GOLD, PluginMessages.ADMIN_MODE, TextColors.WHITE, " " + PluginMessages.HAS_BEEN_TURNED + " ", TextColors.GOLD, PluginMessages.OFF));
            if(!source.equals(optionalPlayer.get())){
                optionalPlayer.get().sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GOLD, PluginMessages.ADMIN_MODE, TextColors.WHITE, " " + PluginMessages.HAS_BEEN_TURNED + " ", TextColors.GOLD, PluginMessages.OFF));
            }
        } else
        {
            EagleFactions.AdminList.add(optionalPlayer.get().getUniqueId());
            source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GOLD, PluginMessages.ADMIN_MODE, TextColors.WHITE, " " + PluginMessages.HAS_BEEN_TURNED + " ", TextColors.GOLD, PluginMessages.ON));
            if(!source.equals(optionalPlayer.get())){
                optionalPlayer.get().sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GOLD, PluginMessages.ADMIN_MODE, TextColors.WHITE, " " + PluginMessages.HAS_BEEN_TURNED + " ", TextColors.GOLD, PluginMessages.ON));
            }
        }
        return true;
    }
}
