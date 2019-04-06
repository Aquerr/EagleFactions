package io.github.aquerr.eaglefactions.placeholders;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.entities.Faction;
import me.rojo8399.placeholderapi.*;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.net.URL;
import java.util.Optional;

public class EFPlaceholderService
{
    private EagleFactions plugin;
    private static EFPlaceholderService INSTANCE;
    private PlaceholderService placeholderService;

    public static EFPlaceholderService getInstance(final EagleFactions plugin, PlaceholderService placeholderService)
    {
        if(INSTANCE == null) {
            INSTANCE = new EFPlaceholderService(plugin, placeholderService);
        }
        return INSTANCE;
    }

    private EFPlaceholderService(final EagleFactions plugin, PlaceholderService placeholderService)
    {
        this.plugin = plugin;
        this.placeholderService = placeholderService;
        registerPlaceholders();
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
                                .tokens("name", "tag", "power")
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
                                .tokens("power")
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
//                case "faction_name":
//                    return builder.description("Parse player's faction's name.");
//                case "faction_tag":
//                    return builder.tokens("a", "b", null).description("Parse player's faction's tag.");
//                    case "multi":
//                        return ((ExpansionBuilder) builder).tokens("a", "b", null).description("Parse the token for player!");
//                    case "msg":
//                        return ((ExpansionBuilder) builder).description("Send a message!");
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

//        if(token.contains("_"))
//        {
            String[] a = token.split("_");
            token = a[0];

            switch(token)
            {
                case "name":
                    return getFactionName(player);
                case "tag":
                    return getFactionTag(player);
                case "power":
                    return getFactionPower(player);
            }
//        }
        return "";
    }

    @Placeholder(id = "factionplayer")
    public Object factionPlayer(@Token(fix = true) @Nullable String token, @Nullable @Source User player)
    {
        if(token == null)
            return "";

//        if(token.contains("_"))
//        {
            String[] a = token.split("_");
            token = a[0];

            switch(token)
            {
                case "power":
                    return getPlayerPower(player);
            }
//        }
        return "";
    }

    public String getFactionName(final User user)
    {
        Optional<Faction> optionalFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(user.getUniqueId());
        if(optionalFaction.isPresent())
        {
            return optionalFaction.get().getName();
        }
        else return "";
    }

    public Text getFactionTag(final User user)
    {
        Optional<Faction> optionalFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(user.getUniqueId());
        if(optionalFaction.isPresent())
        {
            return optionalFaction.get().getTag();
        }
        else return Text.of("");
    }

    private float getPlayerPower(final User player)
    {
        return this.plugin.getPlayerManager().getPlayerPower(player.getUniqueId());
    }

    private float getFactionPower(final User player)
    {
        final Optional<Faction> optionalFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        return optionalFaction.map(faction -> this.plugin.getPowerManager().getFactionPower(faction)).orElse(0F);
    }
}
