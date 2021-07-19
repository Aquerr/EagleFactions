package io.github.aquerr.eaglefactions.config;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.config.Configuration;
import io.github.aquerr.eaglefactions.api.entities.ChatEnum;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ChatConfigImpl implements ChatConfig
{
	private final Configuration configuration;

	//Chat
	private String chatPrefixType = "tag";
	private boolean suppressOtherFactionsMessagesWhileInTeamChat = false;
	private boolean displayProtectionSystemMessages = true;
	private boolean canColorTags = true;
	private TextComponent factionStartPrefix = Component.text("[");
	private TextComponent factionEndPrefix = Component.text("]");
	private boolean isFactionPrefixFirstInChat = true;
	private TextComponent nonFactionPlayerPrefix = Component.text("");
	private boolean showFactionEnterPhrase = true;

	private Map<ChatEnum, Set<FactionMemberType>> visibleRanks;

	public ChatConfigImpl(final Configuration configuration)
	{
		this.configuration = configuration;
	}

	@Override
	public void reload()
	{
		//Chat
		this.chatPrefixType = this.configuration.getString("tag", "faction-prefix");
		this.suppressOtherFactionsMessagesWhileInTeamChat = this.configuration.getBoolean(false, "suppress-other-factions-messages-while-in-team-chat");
		this.displayProtectionSystemMessages = this.configuration.getBoolean(true, "display-protection-system-messages");
		this.canColorTags = this.configuration.getBoolean(true, "colored-tags-allowed");
		this.factionStartPrefix = LegacyComponentSerializer.legacyAmpersand().deserialize(configuration.getString("[", "faction-prefix-start"));
		this.factionEndPrefix = LegacyComponentSerializer.legacyAmpersand().deserialize(configuration.getString("]", "faction-prefix-end"));
		this.isFactionPrefixFirstInChat = this.configuration.getBoolean(true, "faction-prefix-first-in-chat");
		this.nonFactionPlayerPrefix = LegacyComponentSerializer.legacyAmpersand().deserialize(configuration.getString("", "non-faction-player-prefix"));
		this.showFactionEnterPhrase = this.configuration.getBoolean(true, "show-faction-enter-phrase");

		this.visibleRanks = new HashMap<>();
		final Set<FactionMemberType> globalRanks = new HashSet<>();
		final Set<FactionMemberType> allianceRanks = new HashSet<>();
		final Set<FactionMemberType> factionRanks = new HashSet<>();
		globalRanks.addAll(this.configuration.getListOfStrings(Collections.emptyList(), "visible-ranks", "global-chat").stream().map(FactionMemberType::valueOf).collect(Collectors.toSet()));
		allianceRanks.addAll(this.configuration.getListOfStrings(Collections.emptyList(), "visible-ranks", "alliance-chat").stream().map(FactionMemberType::valueOf).collect(Collectors.toSet()));
		factionRanks.addAll(this.configuration.getListOfStrings(Collections.emptyList(), "visible-ranks", "faction-chat").stream().map(FactionMemberType::valueOf).collect(Collectors.toSet()));
		this.visibleRanks.put(ChatEnum.GLOBAL, globalRanks);
		this.visibleRanks.put(ChatEnum.ALLIANCE, allianceRanks);
		this.visibleRanks.put(ChatEnum.FACTION, factionRanks);
		this.visibleRanks = ImmutableMap.copyOf(this.visibleRanks);
	}

	public TextComponent getFactionStartPrefix()
	{
		return this.factionStartPrefix;
	}

	public TextComponent getFactionEndPrefix()
	{
		return this.factionEndPrefix;
	}

	public boolean canColorTags()
	{
		return this.canColorTags;
	}

	@Override
	public boolean isFactionPrefixFirstInChat()
	{
		return this.isFactionPrefixFirstInChat;
	}

	public String getChatPrefixType()
	{
		return this.chatPrefixType;
	}

	@Override
	public boolean shouldSuppressOtherFactionsMessagesWhileInTeamChat()
	{
		return this.suppressOtherFactionsMessagesWhileInTeamChat;
	}

	@Override
	public boolean shouldDisplayProtectionSystemMessages()
	{
		return this.displayProtectionSystemMessages;
	}

	@Override
	public TextComponent getNonFactionPlayerPrefix()
	{
		return this.nonFactionPlayerPrefix;
	}

	@Override
	public boolean shouldShowFactionEnterPhrase()
	{
		return this.showFactionEnterPhrase;
	}

	@Override
	public Map<ChatEnum, Set<FactionMemberType>> getVisibleRanks()
	{
		return this.visibleRanks;
	}
}
