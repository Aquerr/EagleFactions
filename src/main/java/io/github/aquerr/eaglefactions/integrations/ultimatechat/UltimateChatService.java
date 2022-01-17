package io.github.aquerr.eaglefactions.integrations.ultimatechat;

import br.net.fabiozumbi12.UltimateChat.Sponge.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Tamer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
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
    public static final String FACTION_TAG_WITH_PREFIX_SUFFIX = "faction_tag_prefix_suffix";

    private final ChatConfig chatConfig;

    public UltimateChatService(final ChatConfig chatConfig)
    {
        this.chatConfig = chatConfig;
    }

    public static UCChannel getAllianceChannel(Faction faction)
    {
        UCChannel ucChannel = UChat.get().getAPI().getChannel(faction.getName() + "-alliance");
        boolean channelExists = true;
        if (ucChannel == null)
        {
            ucChannel = new UCChannel(faction.getName() + "-alliance", faction.getName() + "-alliance", AQUA_COLOR);
            channelExists = false;
        }
        ucChannel.setMembers(faction.getMembers().stream()
                .map(Sponge.getServer()::getPlayer)
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
            if (!channelExists)
            {
                UChat.get().getAPI().registerNewChannel(ucChannel);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return ucChannel;
    }

    public static UCChannel getFactionChannel(Faction faction)
    {
        UCChannel ucChannel = UChat.get().getAPI().getChannel(faction.getName() + "-faction");
        boolean channelExists = true;
        if (ucChannel == null)
        {
            channelExists = false;
            ucChannel = new UCChannel(faction.getName() + "-faction", faction.getName() + "-faction", GREEN_COLOR);
        }
        ucChannel.setMembers(faction.getMembers().stream()
                .map(Sponge.getServer()::getPlayer)
                .map(Optional::get)
                .map(Tamer::getName)
                .collect(Collectors.toList()));
        try
        {
            if (!channelExists)
            {
                UChat.get().getAPI().registerNewChannel(ucChannel);
            }
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
                Collections.singletonList(TextSerializers.FORMATTING_CODE.serialize(Text.of(TextColors.BLUE, "Click to view info about the faction..."))));
        UChat.get().getAPI().registerNewTag(FACTION_TAG_WITH_PREFIX_SUFFIX, "{" + FACTION_TAG_WITH_PREFIX_SUFFIX + "}", "f info {faction_name}", Collections.singletonList(TextSerializers.FORMATTING_CODE.serialize(Text.of(TextColors.BLUE, "Click to view info about the faction..."))));
        UChat.get().getAPI().registerNewTag(FACTION_CHAT_TAG, "{" + FACTION_CHAT_TAG + "}", null, null);
        UChat.get().getAPI().registerNewTag(FACTION_RANK_TAG, "{" + FACTION_RANK_TAG + "}", null, null);
    }
}
