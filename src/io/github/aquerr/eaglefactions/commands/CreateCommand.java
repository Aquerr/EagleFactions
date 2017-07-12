package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.managers.FactionManager;
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
        String factionName = context.<String> getOne ("faction name").get ();

        if(source instanceof Player)
        {
            Player player = (Player) source;

            if(factionName.equalsIgnoreCase ("SafeZone") || factionName.equalsIgnoreCase ("WarZone"))
            {
                source.sendMessage (Text.of (TextColors.DARK_RED, "[ERROR]", TextColors.RED, "You can't use this faction name!"));
                return CommandResult.success ();
            }

            //TODO:Block long faction names. Make a config file!
            //if(factionName.length() > ConfigManager.getMaxNameLength()){}
            //if(factionName.length() < ConfigManager.getMinNameLength()){}

            String playerFactionName = FactionManager.getFaction (player.getUniqueId ());

            if(playerFactionName == null)
            {
                    try
                    {
                        if(!FactionManager.getFactions().contains(factionName))
                        {

                        }
                    }
                    catch (NullPointerException exception)
                    {
                        player.sendMessage (Text.of (TextColors.DARK_RED, "[ERROR]", TextColors.RED, "Filed to create a faction."));
                    }
            }
            else
            {
                player.sendMessage(Text.of(TextColors.DARK_RED, "[Error] ", TextColors.RED, "You are already in a faction. You must leave or disband your faction first."));
            }


        }
        else
        {
            source.sendMessage (Text.of (TextColors.DARK_RED, "[ERROR]",TextColors.RED, "Only in-game players can use this command!"));
        }

        return CommandResult.success ();
    }
}
