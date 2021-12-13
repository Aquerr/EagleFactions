package io.github.aquerr.eaglefactions.logic;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class CommandBlockerOtherFactionTerritory
{
    private final FactionLogic factionLogic;
    private final List<String> commandsToBlock;
    private boolean shouldBlockAllCommands = false;

    public CommandBlockerOtherFactionTerritory(final FactionLogic factionLogic, final List<String> commandsToBlock)
    {
        this.factionLogic = factionLogic;
        this.commandsToBlock = commandsToBlock;
        if (this.commandsToBlock.contains("*"))
            this.shouldBlockAllCommands = true;
    }

    public boolean shouldBlockCommand(final Player player, final String command)
    {
        if (this.shouldBlockAllCommands)
            return true;
        else if (this.commandsToBlock.size() == 0)
            return false;
        else return isPlayerInOthersTerritory(player) && isCommandBlocked(command);
    }

    private boolean isCommandBlocked(String command)
    {
        String usedCommand = command;
        if (command.charAt(0) == '/') //TODO: This is possibly not required... Need to check this.
        {
            usedCommand = command.substring(1);
        }

        usedCommand = usedCommand.toLowerCase();

        for (String blockedCommand : this.commandsToBlock)
        {
            if (blockedCommand.charAt(0) == '/')
            {
                blockedCommand = blockedCommand.substring(1);
            }

            if (usedCommand.startsWith(blockedCommand))
            {
                return true;
            }

            try
            {
                final Pattern pattern = Pattern.compile(blockedCommand);
                if(pattern.matcher(usedCommand).matches())
                    return true;
            }
            catch(final PatternSyntaxException exception)
            {
                Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.RED, "The syntax of your blocked command pattern is wrong. Command = " + blockedCommand));
            }
        }

        return false;
    }

    private boolean isPlayerInOthersTerritory(Player player)
    {
        return !Objects.equals(this.factionLogic.getFactionByChunk(player.getWorld().getUniqueId(), player.getLocation().getChunkPosition())
                .map(Faction::getName)
                .orElse(null), this.factionLogic.getFactionByPlayerUUID(player.getUniqueId())
                .map(Faction::getName)
                .orElse(null));
    }
}
