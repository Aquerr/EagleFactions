package io.github.aquerr.eaglefactions.common.config;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.config.Configuration;
import io.github.aquerr.eaglefactions.api.entities.ChatEnum;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.*;
import java.util.stream.Collectors;

public class ChatConfigImpl implements ChatConfig
{
	private final Configuration configuration;

	//Chat
	private String chatPrefixType = "tag";
	private boolean suppressOtherFactionsMessagesWhileInTeamChat = false;
	private boolean displayProtectionSystemMessages = true;
	private boolean canColorTags = true;
	private Text factionStartPrefix = Text.of("[");
	private Text factionEndPrefix = Text.of("]");
	private boolean isFactionPrefixFirstInChat = true;
	private Text nonFactionPlayerPrefix = Text.of("");
	private boolean showFactionEnterPhrase = true;
	private TextColor defaultTagColor = TextColors.GREEN;

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
		this.factionStartPrefix = TextSerializers.FORMATTING_CODE.deserialize(configuration.getString("[", "faction-prefix-start"));
		this.factionEndPrefix = TextSerializers.FORMATTING_CODE.deserialize(configuration.getString("]", "faction-prefix-end"));
		this.isFactionPrefixFirstInChat = this.configuration.getBoolean(true, "faction-prefix-first-in-chat");
		this.nonFactionPlayerPrefix = TextSerializers.FORMATTING_CODE.deserialize(configuration.getString("", "non-faction-player-prefix"));
		this.showFactionEnterPhrase = this.configuration.getBoolean(true, "show-faction-enter-phrase");

		this.visibleRanks = loadVisibleRanks();
		this.defaultTagColor = Sponge.getRegistry().getType(TextColor.class, this.configuration.getString(TextColors.GREEN.getId(), "default-tag-color")).orElse(TextColors.GREEN);
	}

	@Override
	public Text getFactionStartPrefix()
	{
		return this.factionStartPrefix;
	}

	@Override
	public Text getFactionEndPrefix()
	{
		return this.factionEndPrefix;
	}

	@Override
	public boolean canColorTags()
	{
		return this.canColorTags;
	}

	@Override
	public TextColor getDefaultTagColor()
	{
		return this.defaultTagColor;
	}

	@Override
	public boolean isFactionPrefixFirstInChat()
	{
		return this.isFactionPrefixFirstInChat;
	}

	@Override
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
	public Text getNonFactionPlayerPrefix()
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

	private Map<ChatEnum, Set<FactionMemberType>> loadVisibleRanks()
	{
		Map<ChatEnum, Set<FactionMemberType>> visibleRanks = new HashMap<>();
		final Set<FactionMemberType> globalRanks = new HashSet<>();
		final Set<FactionMemberType> allianceRanks = new HashSet<>();
		final Set<FactionMemberType> factionRanks = new HashSet<>();
		globalRanks.addAll(this.configuration.getListOfStrings(Collections.emptyList(), "visible-ranks", "global-chat").stream().map(FactionMemberType::valueOf).collect(Collectors.toSet()));
		allianceRanks.addAll(this.configuration.getListOfStrings(Collections.emptyList(), "visible-ranks", "alliance-chat").stream().map(FactionMemberType::valueOf).collect(Collectors.toSet()));
		factionRanks.addAll(this.configuration.getListOfStrings(Collections.emptyList(), "visible-ranks", "faction-chat").stream().map(FactionMemberType::valueOf).collect(Collectors.toSet()));
		visibleRanks.put(ChatEnum.GLOBAL, globalRanks);
		visibleRanks.put(ChatEnum.ALLIANCE, allianceRanks);
		visibleRanks.put(ChatEnum.FACTION, factionRanks);
		visibleRanks = ImmutableMap.copyOf(visibleRanks);
		return visibleRanks;
	}
}
