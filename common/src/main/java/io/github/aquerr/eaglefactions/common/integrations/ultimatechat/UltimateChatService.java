package io.github.aquerr.eaglefactions.common.integrations.ultimatechat;

import br.net.fabiozumbi12.UltimateChat.Sponge.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Tamer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class UltimateChatService
{
    private static final String GREEN_COLOR = "&a";
    private static final String AQUA_COLOR = "&b";

    public static final String FACTION_NAME_TAG = "faction_name";
    public static final String FACTION_TAG_TAG = "faction_tag";
    public static final String FACTION_CHAT_TAG = "faction_chat";
    public static final String FACTION_RANK_TAG = "faction_rank";

    private final ChatConfig chatConfig;

    public UltimateChatService(final ChatConfig chatConfig)
    {
        this.chatConfig = chatConfig;
    }

    public static UCChannel createAllianceChannel(Faction faction)
    {
        //THIS IS SOOOO BEAUTIFUL ;D
        UCChannel ucChannel = new UCChannel(faction.getName() + "-alliance", faction.getName() + "-alliance", AQUA_COLOR);
//        ucChannel.setProperty("use-this-builder", Boolean.TRUE.toString());
//        List<String> existingTags = Arrays.asList(ucChannel.getBuilder());
//        existingTags.addAll(Arrays.asList(FACTION_CHAT_TAG, FACTION_TAG_TAG, FACTION_RANK_TAG));
//        ucChannel.setProperty("tag-builder", String.join(",", existingTags));
        ucChannel.setMembers(faction.getMembers().stream().map(Sponge.getServer()::getPlayer)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Tamer::getName)
                .collect(Collectors.toList()));
        faction.getAlliances().stream()
                .map(EagleFactionsPlugin.getPlugin().getFactionLogic()::getFactionByName)
                .filter(Objects::nonNull)
                .map(Faction::getMembers)
                .flatMap(Collection::stream)
                .map(Sponge.getServer()::getPlayer)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Tamer::getName)
                .forEach(ucChannel::addMember);
    try
        {
            UChat.get().getAPI().registerNewChannel(ucChannel);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return ucChannel;
    }

    public static UCChannel createFactionChannel(Faction faction)
    {
        UCChannel ucChannel = new UCChannel(faction.getName() + "-faction", faction.getName() + "-faction", GREEN_COLOR);
//        ucChannel.setProperty("use-this-builder", Boolean.TRUE.toString());
//        List<String> existingTags = Arrays.asList(ucChannel.getBuilder());
//        existingTags.addAll(Arrays.asList(FACTION_CHAT_TAG, FACTION_TAG_TAG, FACTION_RANK_TAG));
//        ucChannel.setProperty("tag-builder", String.join(",", existingTags));
        ucChannel.setMembers(faction.getMembers().stream().map(Sponge.getServer()::getPlayer)
                .map(Optional::get)
                .map(Tamer::getName)
                .collect(Collectors.toList()));
        try
        {
            UChat.get().getAPI().registerNewChannel(ucChannel);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return ucChannel;
    }

    public void registerTags()
    {
        UChat.get().getAPI().registerNewTag(FACTION_NAME_TAG, "{"  + FACTION_NAME_TAG + "}", null, null);
        UChat.get().getAPI().registerNewTag(FACTION_TAG_TAG,
                "{" + FACTION_TAG_TAG + "}",
                "f info {faction_name}",
                Arrays.asList(TextSerializers.FORMATTING_CODE.serialize(Text.of(TextColors.BLUE, "Click to view info about the faction..."))));
        UChat.get().getAPI().registerNewTag(FACTION_CHAT_TAG, "{" + FACTION_CHAT_TAG + "}", null, null);
        UChat.get().getAPI().registerNewTag(FACTION_RANK_TAG, "{" + FACTION_RANK_TAG + "}", null, null);
    }
}
