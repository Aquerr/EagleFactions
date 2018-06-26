package io.github.aquerr.eaglefactions.commands.enums;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.Player;

/**
 * I really feel like there is already a built in enum/flag for this, but I haven't seen one.
 */
public enum CommandUser
{
    PLAYER, CONSOLE, COMMAND_BLOCK;

    public static CommandUser getUserType(CommandSource source){
         if(source instanceof ConsoleSource){
            return CommandUser.CONSOLE;
        }else if(source instanceof Player){
            return CommandUser.PLAYER;
        }
        return CommandUser.COMMAND_BLOCK;
    }
}
