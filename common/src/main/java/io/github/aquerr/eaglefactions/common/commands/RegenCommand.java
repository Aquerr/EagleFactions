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
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.UUID;

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

        if (factionToRegen.isSafeZone() || factionToRegen.isWarZone())
        {
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "This faction cannot be disbanded!"));
        }

        /*
        Update: 29.04.2020
        Checking for a confirmation here. Since every command source can run this command, if the source is not a player,
        a UUID from its name is being generated.
         */

        UUID uuid = source instanceof Player ? ((Player) source).getUniqueId() : UUID.fromString(source.getName());

        if (!EagleFactionsPlugin.REGEN_CONFIRMATION_MAP.containsKey(uuid) || !EagleFactionsPlugin.REGEN_CONFIRMATION_MAP.get(uuid).equals(factionToRegen.getName()))
        {
            source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.YELLOW, Messages.REGEN_WARNING_CONFIRMATION_REQUIRED));

            EagleFactionsPlugin.REGEN_CONFIRMATION_MAP.put(uuid, factionToRegen.getName());

            return CommandResult.success();
        }

        EagleFactionsPlugin.REGEN_CONFIRMATION_MAP.remove(uuid);

        /* Firstly, we're simply disbanding the faction. */

        boolean didSucceed = super.getPlugin().getFactionLogic().disbandFaction(factionToRegen.getName());
        if(didSucceed)
        {
            if (source instanceof Player)
            {
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

        for (Claim claim : factionToRegen.getClaims())
        {
            Optional<World> world = Sponge.getServer().getWorld(claim.getWorldUUID());

            if (!world.isPresent())
            {
                throw new CommandException((Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, Messages.SOMETHING_WENT_WRONG)));
            }

            world.get().regenerateChunk(claim.getChunkPosition());
        }

        source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.WHITE, Messages.FACTION_HAS_BEEN_REGENERATED));

        return CommandResult.success();
    }
}
