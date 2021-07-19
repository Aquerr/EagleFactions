package io.github.aquerr.eaglefactions.commands.access;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.util.ParticlesUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Optional;

public class AccessPlayerCommand extends AbstractCommand
{
    public AccessPlayerCommand(EagleFactionsPlugin plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final FactionPlayer factionPlayer = context.requireOne(EagleFactionsCommandParameters.factionPlayer());

        if(!(isPlayer(context)))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND, NamedTextColor.RED)));

        final ServerPlayer player = (ServerPlayer) context.cause().audience();
        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
        if (!optionalPlayerFaction.isPresent())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND, NamedTextColor.RED)));

        final Faction playerFaction = optionalPlayerFaction.get();

        // Access can be run only by leader and officers
        final Optional<Faction> optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(player.world().uniqueId(), player.serverLocation().chunkPosition());
        if (!optionalChunkFaction.isPresent())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.THIS_PLACE_DOES_NOT_BELONG_TO_ANYONE)));

        final Faction chunkFaction = optionalChunkFaction.get();
        if (!playerFaction.getName().equals(chunkFaction.getName()))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.THIS_PLACE_DOES_NOT_BELONG_TO_YOUR_FACTION)));

        if (!playerFaction.getLeader().equals(player.uniqueId()) && !playerFaction.getOfficers().contains(player.uniqueId()) && !super.getPlugin().getPlayerManager().hasAdminMode(player))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS, NamedTextColor.RED)));

        // Get claim at player's location
        final Optional<Claim> optionalClaim = chunkFaction.getClaimAt(player.world().uniqueId(), player.serverLocation().chunkPosition());
        final Claim claim = optionalClaim.get();

        if (claim.getOwners().contains(factionPlayer.getUniqueId()))
        {
            super.getPlugin().getFactionLogic().removeClaimOwner(chunkFaction, claim, factionPlayer.getUniqueId());
            ParticlesUtil.spawnRemoveAccessParticles(claim);
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(factionPlayer.getName(), NamedTextColor.GOLD).append(Component.text(" has been removed from the claim ", NamedTextColor.GREEN).append(Component.text(claim.getChunkPosition().toString(), NamedTextColor.GOLD)))));
        }
        else
        {
            super.getPlugin().getFactionLogic().addClaimOwner(chunkFaction, claim, factionPlayer.getUniqueId());
            ParticlesUtil.spawnAddAccessParticles(claim);
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(factionPlayer.getName(), NamedTextColor.GOLD).append(Component.text(" has been added to the claim ", NamedTextColor.GREEN).append(Component.text(claim.getChunkPosition().toString(), NamedTextColor.GOLD)))));
        }

        return CommandResult.success();
    }
}
