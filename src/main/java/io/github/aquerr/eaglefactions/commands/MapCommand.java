package io.github.aquerr.eaglefactions.commands;

import com.flowpowered.math.vector.Vector3d;
import io.github.aquerr.eaglefactions.PluginInfo;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

public class MapCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if(source instanceof Player)
        {
            Player player = (Player)source;

            player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Map ", TextColors.GOLD, "Enabled"));

            Text notCapturedLand = Text.of(TextColors.GRAY, "/");
            Text factionLand = Text.of(TextColors.GREEN, "+");
            Text allianceLand = Text.of(TextColors.BLUE, "+");
            Text enemyLand = Text.of(TextColors.RED, "#");
            Text normalFactionLand = Text.of(TextColors.WHITE, "+");
            Text playerLocation = Text.of(TextColors.GOLD, "O");

            World world = player.getWorld();

            Vector3d playerPosition = player.getLocation().getPosition();

            for (int column = 0; column <= 17; column++)
            {
                for (int row = 0; row <= 9; row++)
                {
                    
                }
            }

        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Only in-game players can use this command!"));
        }

        return CommandResult.success();
    }
}
