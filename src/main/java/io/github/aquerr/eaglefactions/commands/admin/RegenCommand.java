package io.github.aquerr.eaglefactions.commands.admin;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.util.WorldUtil;
import net.kyori.adventure.audience.Audience;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.Optional;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class RegenCommand extends AbstractCommand
{
    public RegenCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final Faction factionToRegen = context.requireOne(EagleFactionsCommandParameters.faction());

        if (factionToRegen.isSafeZone() || factionToRegen.isWarZone())
        {
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.THIS_FACTION_CANNOT_BE_DISBANDED, RED)));
        }

        /*
        Update: 29.04.2020
        Checking for a confirmation here. Since every command source can run this command, if the source is not a player,
        a UUID from its name is being generated.
         */
        Audience audience = context.cause().audience();
        UUID uuid = audience instanceof ServerPlayer ? ((ServerPlayer) audience).uniqueId() : UUID.fromString(context.identifier());

        if (!EagleFactionsPlugin.REGEN_CONFIRMATION_MAP.containsKey(uuid) || !EagleFactionsPlugin.REGEN_CONFIRMATION_MAP.get(uuid).equals(factionToRegen.getName()))
        {
            audience.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text(Messages.REGEN_WARNING_CONFIRMATION_REQUIRED, YELLOW)));

            EagleFactionsPlugin.REGEN_CONFIRMATION_MAP.put(uuid, factionToRegen.getName());

            return CommandResult.success();
        }

        EagleFactionsPlugin.REGEN_CONFIRMATION_MAP.remove(uuid);

        /* Firstly, we're simply disbanding the faction. */

        boolean didSucceed = super.getPlugin().getFactionLogic().disbandFaction(factionToRegen.getName());
        if(didSucceed)
        {
            if (audience instanceof Player)
            {
                Player player = (Player) audience;

                EagleFactionsPlugin.AUTO_CLAIM_LIST.remove(player.uniqueId());
                EagleFactionsPlugin.CHAT_LIST.remove(player.uniqueId());
            }
        }
        else
        {
            throw new CommandException(PluginInfo.PLUGIN_PREFIX.append(text(Messages.SOMETHING_WENT_WRONG, RED)));
        }

        /* After a successful disband we can regenerate faction claims. */

        for (Claim claim : factionToRegen.getClaims())
        {
            Optional<ServerWorld> world = WorldUtil.getWorldByUUID(claim.getWorldUUID());

            if (!world.isPresent())
            {
                throw new CommandException(PluginInfo.PLUGIN_PREFIX.append(text(Messages.SOMETHING_WENT_WRONG, RED)));
            }

            world.get().chunkManager().regenerateChunk(claim.getChunkPosition());
        }

        audience.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text(Messages.FACTION_HAS_BEEN_REGENERATED, WHITE)));
        return CommandResult.success();
    }
}
