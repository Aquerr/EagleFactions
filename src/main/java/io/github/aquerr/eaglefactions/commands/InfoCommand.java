package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by Aquerr on 2017-08-03.
 */
public class InfoCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        String factionName = context.<String>getOne("faction name").get();

            if(FactionLogic.getFactions().contains(factionName))
            {
                Faction faction = FactionLogic.getFaction(factionName);

                List<Text> factionInfo = new ArrayList<>();

                String membersList = "";

                if(!faction.Members.isEmpty() && faction.Members != null)
                {
                    for (String member: faction.Members)
                    {
                        EagleFactions.getEagleFactions().getLogger().info("Adding a member...");

                        membersList += member + ", ";
                    }
                }

                String officersList = "";

                if(!faction.Officers.isEmpty() && faction.Officers != null)
                {
                    for (String officer: faction.Officers)
                    {
                        EagleFactions.getEagleFactions().getLogger().info("Adding an officer...");
                        officersList += officer + ", ";
                    }
                }

                String alliancesList = "";

                if(!faction.Alliances.isEmpty() && faction.Alliances != null)
                {
                    for (String alliance: faction.Alliances)
                    {
                        alliancesList += alliance + ", ";
                    }
                }

                String enemiesList = "";

                if(!faction.Enemies.isEmpty() && faction.Enemies != null)
                {
                    for (String enemy: faction.Enemies)
                    {
                        enemiesList += enemy + ", ";
                    }
                }



                Text info = Text.builder()
                        .append(Text.of(TextColors.AQUA, "Name: ", TextColors.GOLD, faction.Name + "\n"))
                        .append(Text.of(TextColors.AQUA, "Leader: ", TextColors.GOLD, getNameFromUUID(faction.Leader).get() + "\n"))
                        .append(Text.of(TextColors.AQUA, "Officers: ", TextColors.GOLD, officersList + "\n"))
                        .append(Text.of(TextColors.AQUA, "Alliances: ", TextColors.GOLD, alliancesList + "\n"))
                        .append(Text.of(TextColors.AQUA, "Enemies: ", TextColors.GOLD, enemiesList + "\n"))
                        .append(Text.of(TextColors.AQUA, "Members: ", TextColors.GOLD, membersList + "\n"))
                        .append(Text.of(TextColors.AQUA, "Power: ", TextColors.GOLD, faction.Power))
                        .build();

                factionInfo.add(info);

                PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
                PaginationList.Builder paginationBuilder = paginationService.builder().title(Text.of(TextColors.GREEN, "Faction Info")).contents(factionInfo);
                paginationBuilder.sendTo(source);

                CommandResult.success();

            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "There is no faction called ", TextColors.GOLD, factionName));
            }

        return CommandResult.success();
    }

    private Optional<String> getNameFromUUID(UUID playerUUID)
    {
        UserStorageService userStorageService = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
        Optional<User> oUser = userStorageService.get(playerUUID);

        if(oUser.isPresent())
        {
            String name = oUser.get().getName();
            return Optional.of(name);
        }
        else
        {
            return Optional.empty();
        }

    }
}
