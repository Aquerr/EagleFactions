package io.github.aquerr.eaglefactions.common.integrations.placeholderapi;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.managers.PowerManager;
import me.rojo8399.placeholderapi.*;
import org.spongepowered.api.data.manipulator.mutable.entity.JoinData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class EFPlaceholderService
{
    private static EFPlaceholderService INSTANCE = null;

    private final EagleFactions plugin;
    private final PlaceholderService placeholderService;

    public static EFPlaceholderService getInstance(final EagleFactions plugin, Object placeholderService)
    {
        if(INSTANCE == null) {
            INSTANCE = new EFPlaceholderService(plugin, (PlaceholderService) placeholderService);
        }
        return INSTANCE;
    }

    private EFPlaceholderService(final EagleFactions plugin, PlaceholderService placeholderService)
    {
        this.plugin = plugin;
        this.placeholderService = placeholderService;
        registerPlaceholders();
    }

    public PlaceholderService getPlaceholderService()
    {
        return placeholderService;
    }

    private void registerPlaceholders()
    {
        placeholderService.loadAll(this, this.plugin).stream().map(builder -> {
            switch(builder.getId())
            {
                case "faction": {
                    try
                    {
                        return ((ExpansionBuilder) builder)
                                .tokens("name", "tag", "power", "maxpower", "last_online", "claims_count", "alliances",
                                        "enemies", "truce", "officers_count", "members_count", "recruits_count")
                                .description("Player's faction's placeholders.")
                                .url("https://github.com/Aquerr/EagleFactions")
                                .author("Aquerr (Nerdi)")
                                .version("1.0")
                                .plugin(this.plugin);
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                case "factionplayer":
                {
                    try
                    {
                        return ((ExpansionBuilder) builder)
                                .tokens("power", "maxpower", "last_online")
                                .description("Player's placeholders.")
                                .url("https://github.com/Aquerr/EagleFactions")
                                .author("Aquerr (Nerdi)")
                                .version("1.0")
                                .plugin(this.plugin);
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            return builder;
        }).map(builder -> builder.author("Aquerr (Nerdi)").version("1.0")).forEach(builder -> {
            try
            {
                builder.buildAndRegister();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    @Placeholder(id = "faction")
    public Object faction(@Token(fix = true) @Nullable String token, @Nullable @Source User player)
    {
        if(token == null)
            return "";

        switch(token)
        {
            case "name":
                return getFactionName(player);
            case "tag":
                return getFactionTag(player);
            case "power":
                return getFactionPower(player);
            case "maxpower":
                return getFactionMaxPower(player);
            case "last_online":
                return getFactionLastOnline(player);
            case "claims_count":
                return getFactionClaimCount(player);
            case "officers_count":
                return getFactionOfficerCount(player);
            case "members_count":
                return getFactionMemberCount(player);
            case "recruits_count":
                return getFactionRecruitCount(player);
            case "alliances":
                return getFactionAlliances(player);
            case "enemies":
                return getFactionEnemies(player);
            case "truce":
                return getFactionTruce(player);
        }
        return "";
    }

    @Placeholder(id = "factionplayer")
    public Object factionPlayer(@Token(fix = true) @Nullable String token, @Nullable @Source User player)
    {
        if(token == null)
            return "";

        switch(token)
        {
            case "power":
                return getPlayerPower(player);
            case "maxpower":
                return getPlayerMaxPower(player);
            case "last_online":
                return getPlayerLastOnline(player);
        }
        return "";
    }

    private String getFactionName(final User user)
    {
        return this.plugin.getFactionLogic().getFactionByPlayerUUID(user.getUniqueId())
                .map(Faction::getName)
                .orElse("");
    }

    private Text getFactionTag(final User user)
    {
        return this.plugin.getFactionLogic().getFactionByPlayerUUID(user.getUniqueId())
                .map(Faction::getTag)
                .orElse(Text.EMPTY);
    }

    private float getFactionPower(final User player)
    {
        return this.plugin.getFactionLogic().getFactionByPlayerUUID(player.getUniqueId())
                .map(this.plugin.getPowerManager()::getFactionPower)
                .orElse(0F);
    }

    private float getFactionMaxPower(final User player)
    {
        return this.plugin.getFactionLogic().getFactionByPlayerUUID(player.getUniqueId())
                .map(this.plugin.getPowerManager()::getFactionMaxPower)
                .orElse(0F);
    }

    private Instant getFactionLastOnline(final User player)
    {
        return this.plugin.getFactionLogic().getFactionByPlayerUUID(player.getUniqueId())
                .map(Faction::getLastOnline)
                .orElse(Instant.now());
    }

    private int getFactionClaimCount(final User player)
    {
        return this.plugin.getFactionLogic().getFactionByPlayerUUID(player.getUniqueId())
                .map(Faction::getClaims)
                .map(Set::size)
                .orElse(0);
    }

    private int getFactionOfficerCount(final User player)
    {
        return this.plugin.getFactionLogic().getFactionByPlayerUUID(player.getUniqueId())
                .map(Faction::getOfficers)
                .map(Set::size)
                .orElse(0);
    }

    private int getFactionMemberCount(final User player)
    {
        return this.plugin.getFactionLogic().getFactionByPlayerUUID(player.getUniqueId())
                .map(Faction::getMembers)
                .map(Set::size)
                .orElse(0);
    }

    private int getFactionRecruitCount(final User player)
    {
        return this.plugin.getFactionLogic().getFactionByPlayerUUID(player.getUniqueId())
                .map(Faction::getRecruits)
                .map(Set::size)
                .orElse(0);
    }

    private Set<String> getFactionAlliances(final User player)
    {
        return this.plugin.getFactionLogic().getFactionByPlayerUUID(player.getUniqueId())
                .map(Faction::getAlliances)
                .orElse(Collections.emptySet());
    }

    private Set<String> getFactionEnemies(final User player)
    {
        return this.plugin.getFactionLogic().getFactionByPlayerUUID(player.getUniqueId())
                .map(Faction::getEnemies)
                .orElse(Collections.emptySet());
    }

    private Set<String> getFactionTruce(final User player)
    {
        return this.plugin.getFactionLogic().getFactionByPlayerUUID(player.getUniqueId())
                .map(Faction::getTruces)
                .orElse(Collections.emptySet());
    }

    //
    //FactionPlayer placeholder methods starts here.
    //

    private float getPlayerPower(final User player)
    {
        return this.plugin.getPlayerManager().getFactionPlayer(player.getUniqueId())
                .map(FactionPlayer::getPower)
                .orElse(0F);
    }

    private float getPlayerMaxPower(final User player)
    {
        return this.plugin.getPlayerManager().getFactionPlayer(player.getUniqueId())
                .map(FactionPlayer::getMaxPower)
                .orElse(0F);
    }

    private String getPlayerLastOnline(final User player)
    {
        return player.get(JoinData.class)
                .map(JoinData::lastPlayed)
                .map(Value::get)
                .map(instant -> LocalDateTime.ofInstant(instant, ZoneId.systemDefault()))
                .map(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")::format)
                .orElse("");
    }
}
