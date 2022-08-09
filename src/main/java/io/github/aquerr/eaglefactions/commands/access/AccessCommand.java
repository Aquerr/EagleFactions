package io.github.aquerr.eaglefactions.commands.access;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.pagination.PaginationList;

import java.util.ArrayList;
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
        ServerPlayer player = requirePlayerSource(context);
        Faction playerFaction = requirePlayerFaction(player);

        // Access can be run only by leader and officers
        final Faction chunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(player.world().uniqueId(), player.serverLocation().chunkPosition())
                .orElseThrow(() -> this.getPlugin().getMessageService().resolveExceptionWithMessage("error.claim.place-does-not-belong-to-anyone"));

        if (!playerFaction.equals(chunkFaction))
            throw this.getPlugin().getMessageService().resolveExceptionWithMessage("error.claim.place-does-not-belong-to-your-faction");

        if (!playerFaction.getLeader().equals(player.uniqueId()) && !playerFaction.getOfficers().contains(player.uniqueId()) && !super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
            throw this.getPlugin().getMessageService().resolveExceptionWithMessage("error.access.you-must-be-faction-leader-or-officer-to-do-this");

        // Get claim at player's location
        final Optional<Claim> optionalClaim = chunkFaction.getClaimAt(player.world().uniqueId(), player.serverLocation().chunkPosition());
        return optionalClaim.map(claim -> showAccess(player, claim))
                .orElse(CommandResult.success());
    }

    private CommandResult showAccess(final Player player, final Claim claim)
    {
        final TextComponent claimLocation = this.getPlugin().getMessageService().resolveComponentWithMessage("command.access.location", claim.getChunkPosition().toString());
        final TextComponent accessibleByFactionText = this.getPlugin().getMessageService().resolveComponentWithMessage("command.access.accessible-by-faction", claim.isAccessibleByFaction());
        final List<String> ownersNames = claim.getOwners().stream()
                .map(owner -> super.getPlugin().getPlayerManager().getFactionPlayer(owner))
                .filter(Optional::isPresent)
                .map(factionPlayer -> factionPlayer.get().getName())
                .collect(Collectors.toList());

        final TextComponent ownersText = this.getPlugin().getMessageService().resolveComponentWithMessage("command.access.owners", String.join(",", ownersNames));
        final List<Component> contents = new ArrayList<>();
        contents.add(claimLocation);
        contents.add(accessibleByFactionText);
        contents.add(ownersText);
        final PaginationList paginationList = PaginationList.builder()
                .contents(contents)
                .padding(this.getPlugin().getMessageService().resolveComponentWithMessage("command.access.padding-character"))
                .title(this.getPlugin().getMessageService().resolveComponentWithMessage("command.access.header"))
                .build();
        paginationList.sendTo(player);
        return CommandResult.success();
    }
}
