package io.github.aquerr.eaglefactions.commands.admin;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.Messages;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.Optional;

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
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.THIS_FACTION_CANNOT_BE_DISBANDED, NamedTextColor.RED)));
        }

        /*
        Update: 29.04.2020
        Checking for a confirmation here. Since every command source can run this command, if the source is not a player,
        a UUID from its name is being generated.
         */

        Identity identity = isPlayer(context) ? ((ServerPlayer)context.cause().audience()).identity() : Identity.nil();

        if (!EagleFactionsPlugin.REGEN_CONFIRMATION_MAP.containsKey(identity.uuid()) || !EagleFactionsPlugin.REGEN_CONFIRMATION_MAP.get(identity.uuid()).equals(factionToRegen.getName()))
        {
            context.sendMessage(Identity.nil(), PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.REGEN_WARNING_CONFIRMATION_REQUIRED, NamedTextColor.YELLOW)));

            EagleFactionsPlugin.REGEN_CONFIRMATION_MAP.put(identity.uuid(), factionToRegen.getName());

            return CommandResult.success();
        }

        EagleFactionsPlugin.REGEN_CONFIRMATION_MAP.remove(identity.uuid());

        /* Firstly, we're simply disbanding the faction. */

        boolean didSucceed = super.getPlugin().getFactionLogic().disbandFaction(factionToRegen.getName());
        if(didSucceed)
        {
            if (isPlayer(context))
            {
                ServerPlayer player = (ServerPlayer) context.cause().audience();

                EagleFactionsPlugin.AUTO_CLAIM_LIST.remove(player.uniqueId());
                EagleFactionsPlugin.CHAT_LIST.remove(player.uniqueId());
            }
        }
        else
        {
            throw new CommandException(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.SOMETHING_WENT_WRONG, NamedTextColor.RED)));
        }

        /* After a successful disband we can regenerate faction claims. */

        for (Claim claim : factionToRegen.getClaims())
        {
            Optional<ServerWorld> serverWorld = Sponge.server().worldManager().worlds().stream()
                    .filter(world -> world.uniqueId().equals(claim.getWorldUUID()))
                    .findFirst();

            if (!serverWorld.isPresent())
            {
                throw new CommandException(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.SOMETHING_WENT_WRONG, NamedTextColor.RED)));
            }

            serverWorld.get().regenerateChunk(claim.getChunkPosition());
        }

        context.sendMessage(Identity.nil(), PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.FACTION_HAS_BEEN_REGENERATED)));
        return CommandResult.success();
    }
}
