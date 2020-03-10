package io.github.aquerr.eaglefactions.common.integrations.placeholderapi;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import me.rojo8399.placeholderapi.*;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.time.Instant;
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
        final Optional<Faction> optionalFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(user.getUniqueId());
        if(optionalFaction.isPresent())
        {
            return optionalFaction.get().getName();
        }
        else return "";
    }

    private Text getFactionTag(final User user)
    {
        final Optional<Faction> optionalFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(user.getUniqueId());
        if(optionalFaction.isPresent())
        {
            return optionalFaction.get().getTag();
        }
        else return Text.of("");
    }

    private float getFactionPower(final User player)
    {
        final Optional<Faction> optionalFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        return optionalFaction.map(faction -> this.plugin.getPowerManager().getFactionPower(faction)).orElse(0F);
    }

    private float getFactionMaxPower(final User player)
    {
        final Optional<Faction> optionalFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        return optionalFaction.map(faction -> this.plugin.getPowerManager().getFactionMaxPower(faction)).orElse(0F);
    }

    private Instant getFactionLastOnline(final User player)
    {
        final Optional<Faction> optionalFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        if(optionalFaction.isPresent())
        {
//            final Date date = Date.from(optionalFaction.get().getLastOnline());
//            final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
//            return formatter.format(date);
            return optionalFaction.get().getLastOnline();
        }
        return Instant.now();
    }

    private int getFactionClaimCount(final User player)
    {
        final Optional<Faction> optionalFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        return optionalFaction.map(x->x.getClaims().size()).orElse(0);
    }

    private int getFactionOfficerCount(final User player)
    {
        final Optional<Faction> optionalFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        return optionalFaction.map(x->x.getOfficers().size()).orElse(0);
    }

    private int getFactionMemberCount(final User player)
    {
        final Optional<Faction> optionalFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        return optionalFaction.map(x->x.getMembers().size()).orElse(0);
    }

    private int getFactionRecruitCount(final User player)
    {
        final Optional<Faction> optionalFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        return optionalFaction.map(x->x.getRecruits().size()).orElse(0);
    }

    private Set<String> getFactionAlliances(final User player)
    {
        final Optional<Faction> optionalFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        return optionalFaction.map(Faction::getAlliances).orElse(new HashSet<>());
    }

    private Set<String> getFactionEnemies(final User player)
    {
        final Optional<Faction> optionalFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        return optionalFaction.map(Faction::getEnemies).orElse(new HashSet<>());
    }

    private Set<String> getFactionTruce(final User player)
    {
        final Optional<Faction> optionalFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        return optionalFaction.map(Faction::getTruces).orElse(new HashSet<>());
    }

    //
    //FactionPlayer placeholder methods starts here.
    //

    private float getPlayerPower(final User player)
    {
        return this.plugin.getPlayerManager().getPlayerPower(player.getUniqueId());
    }

    private float getPlayerMaxPower(final User player)
    {
        return this.plugin.getPlayerManager().getPlayerMaxPower(player.getUniqueId());
    }

    private String getPlayerLastOnline(final User player)
    {
        //TODO
//        this.plugin.getPlayerManager().get
        return "";
    }
}
