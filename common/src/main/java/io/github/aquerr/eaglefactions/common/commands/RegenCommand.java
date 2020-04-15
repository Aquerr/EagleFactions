package io.github.aquerr.eaglefactions.common.commands;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class RegenCommand extends AbstractCommand
{
    public RegenCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        final Faction factionToRegen = context.requireOne("faction");

        if (factionToRegen.isSafeZone() || factionToRegen.isWarZone()) {
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "This faction cannot be disbanded!"));
        }

        /* Firstly, we're simply disbanding the faction. */

        boolean didSucceed = super.getPlugin().getFactionLogic().disbandFaction(factionToRegen.getName());
        if(didSucceed)
        {
            if (source instanceof Player) {
                Player player = (Player) source;

                EagleFactionsPlugin.AUTO_CLAIM_LIST.remove(player.getUniqueId());
                EagleFactionsPlugin.CHAT_LIST.remove(player.getUniqueId());
            }
        }
        else
        {
            throw new CommandException((Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, Messages.SOMETHING_WENT_WRONG)));
        }

        /* After a successful disband we can regenerate faction claims. */

        for (Claim claim : factionToRegen.getClaims()) {
            Sponge.getServer().getWorld(claim.getWorldUUID()).ifPresent(world -> {
                world.regenerateChunk(claim.getChunkPosition());
            });
        }


        source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.WHITE, "The faction was successfully regenerated!"));

        return CommandResult.success();
    }
}
