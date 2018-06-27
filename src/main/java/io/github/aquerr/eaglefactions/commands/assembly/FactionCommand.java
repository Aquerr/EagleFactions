package io.github.aquerr.eaglefactions.commands.assembly;

import com.google.inject.Inject;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.commands.annotations.AllowedGroups;
import io.github.aquerr.eaglefactions.commands.annotations.RequiredRank;
import io.github.aquerr.eaglefactions.commands.annotations.RequiresFaction;
import io.github.aquerr.eaglefactions.commands.enums.CommandUser;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import io.github.aquerr.eaglefactions.managers.PlayerManager;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * A collection of anything a command could need. Anything else can be injected by the specific class.
 */
public abstract class FactionCommand implements CommandExecutor
{
    protected FactionsCache cache;
    protected Settings settings;
    protected FactionLogic factionLogic;
    protected Logger logger;

    @Inject
    public FactionCommand(FactionsCache cache, Settings settings, FactionLogic factionLogic, Logger logger){
        this.cache = cache;
        this.settings = settings;
        this.factionLogic = factionLogic;
        this.logger = logger;
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if(verifyConstraints(source)){
            if(!executeCommand(source, context)){
                throw new CommandException(Text.of(TextColors.RED, "Something went wrong in ", getClass().getCanonicalName()));
            }
        }
        return CommandResult.success();
    }


    protected boolean verifyConstraints(CommandSource source){
        if(this instanceof AllowedGroups){
            if(!Arrays.asList(((AllowedGroups) this).getGroups()).contains(CommandUser.getUserType(source))){
                return false;
            }
        }
        if(this instanceof Player){
            Optional<Faction> faction = cache.getFactionByPlayer(((Player) source).getUniqueId());
            if(this instanceof RequiresFaction){
                if(((RequiresFaction) this).value() != faction.isPresent()){
                    if(faction.isPresent()){
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You need to leave your faction in order to use this command!"));
                    }else {
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
                    }
                    return false;
                }
            }
            if(faction.isPresent() && this instanceof RequiredRank){
                FactionMemberType memberType = PlayerManager.getFactionMemberType((Player) source, faction.get());
                if (memberType.compareTo(((RequiredRank) this).minimumRank()) > 0 && !EagleFactions.AdminList.contains(((Player) source).getUniqueId()))
                {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You need to be a ", TextColors.WHITE,
                            memberType.toString().toLowerCase(), TextColors.RED, " in your faction to use this command!"));
                }
            }
        }
        return true;
    }

    protected abstract boolean executeCommand(CommandSource source, CommandContext context);

}
