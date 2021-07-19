package io.github.aquerr.eaglefactions.commands.access;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.pagination.PaginationList;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AccessCommand extends AbstractCommand
{
    public AccessCommand(EagleFactionsPlugin plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
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
        return showAccess(player, claim);
    }

    private CommandResult showAccess(final Player player, final Claim claim)
    {
        final TextComponent claimLocation = Component.text("Location: ", NamedTextColor.AQUA).append(Component.text(claim.getChunkPosition().toString(), NamedTextColor.GOLD));
        final TextComponent text = Component.text("Accessible by faction: ", NamedTextColor.AQUA).append(Component.text(claim.isAccessibleByFaction(), NamedTextColor.GOLD));
        final List<String> ownersNames = claim.getOwners().stream()
                .map(owner -> super.getPlugin().getPlayerManager().getFactionPlayer(owner))
                .filter(Optional::isPresent)
                .map(factionPlayer -> factionPlayer.get().getName())
                .collect(Collectors.toList());
        final TextComponent text1 = Component.text("Owners: ", NamedTextColor.AQUA).append(Component.text(String.join(", ", ownersNames), NamedTextColor.GOLD));
        final List<TextComponent> contents = new LinkedList<>();
        contents.add(claimLocation);
        contents.add(text);
        contents.add(text1);
        final PaginationList paginationList = PaginationList.builder()
                .contents(contents.toArray(new Component[0]))
                .padding(Component.text("="))
                .title(Component.text("Claim Access", NamedTextColor.GREEN))
                .build();
        paginationList.sendTo(player);
        return CommandResult.success();
    }
}
