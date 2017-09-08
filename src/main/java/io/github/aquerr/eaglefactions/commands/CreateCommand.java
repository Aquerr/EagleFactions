package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.MainLogic;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

/**
 * Created by Aquerr on 2017-07-12.
 */
public class CreateCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        String factionName = context.<String>getOne("factionName").get();
        String factionTag = context.<String>getOne("tag").get();

        if (source instanceof Player)
        {
            Player player = (Player) source;

            if (factionName.equalsIgnoreCase("SafeZone") || factionName.equalsIgnoreCase("WarZone"))
            {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You can't use this faction name!"));
                return CommandResult.success();
            }

            String playerFactionName = FactionLogic.getFactionName(player.getUniqueId());

            if (playerFactionName == null)
            {
                if(FactionLogic.getFactionsTags().contains(factionTag))
                {
                    player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Provided faction tag is already taken!"));
                    return CommandResult.success();
                }
                else
                {
                    //Check tag length
                    if(factionTag.length() > MainLogic.getMaxTagLength())
                    {
                        player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Provided faction tag is too long! (Max " + MainLogic.getMaxTagLength() + " chars)"));
                        return CommandResult.success();
                    }
                    if(factionTag.length() < MainLogic.getMinTagLength())
                    {
                        player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Provided faction tag is too short! (Min " + MainLogic.getMinTagLength() + " chars)"));
                        return CommandResult.success();
                    }
                }

                if (!FactionLogic.getFactionsNames().contains(factionName))
                {
                    //Check name length
                    if(factionName.length() > MainLogic.getMaxNameLength())
                    {
                        player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Provided faction name is too long! (Max " + MainLogic.getMaxNameLength() + " chars)"));
                        return CommandResult.success();
                    }
                    if(factionName.length() < MainLogic.getMinNameLength())
                    {
                        player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Provided faction name is too short! (Min " + MainLogic.getMinNameLength() + " chars)"));
                        return CommandResult.success();
                    }

                    boolean didSucceed = FactionLogic.createFaction(factionName, factionTag, player.getUniqueId());

                    if (didSucceed)
                    {
                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Faction " + factionName + " has been created!"));
                        return CommandResult.success();
                    }
                    else
                    {
                        player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Something went wrong while creating faction."));
                    }
                }
                else
                {
                    player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Faction with the same name already exists!"));
                }
            } else
            {
                player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You are already in a faction. You must leave or disband your faction first."));
            }


        } else
        {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Only in-game players can use this command!"));
        }

        return CommandResult.success();
    }
}
