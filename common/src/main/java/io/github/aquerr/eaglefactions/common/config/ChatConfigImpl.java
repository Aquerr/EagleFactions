package io.github.aquerr.eaglefactions.common.config;

import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.config.Configuration;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public class ChatConfigImpl implements ChatConfig
{
	private final Configuration configuration;

	//Chat
	private String chatPrefixType = "tag";
	private boolean shouldDisplayRank = true;
	private boolean suppressOtherFactionsMessagesWhileInTeamChat = false;
	private boolean displayProtectionSystemMessages = true;
	private boolean canColorTags = true;
	private Text factionStartPrefix = Text.of("[");
	private Text factionEndPrefix = Text.of("]");
	private boolean isFactionPrefixFirstInChat = true;
	private Text nonFactionPlayerPrefix = Text.of("");
	private boolean showFactionEnterPhrase = true;

	public ChatConfigImpl(final Configuration configuration)
	{
		this.configuration = configuration;
	}

	@Override
	public void reload()
	{
		//Chat
		this.chatPrefixType = this.configuration.getString("tag", "faction-prefix");
		this.shouldDisplayRank = this.configuration.getBoolean(true, "faction-rank");
		this.suppressOtherFactionsMessagesWhileInTeamChat = this.configuration.getBoolean(false, "suppress-other-factions-messages-while-in-team-chat");
		this.displayProtectionSystemMessages = this.configuration.getBoolean(true, "display-protection-system-messages");
		this.canColorTags = this.configuration.getBoolean(true, "colored-tags-allowed");
		this.factionStartPrefix = TextSerializers.FORMATTING_CODE.deserialize(configuration.getString("[", "faction-prefix-start"));
		this.factionEndPrefix = TextSerializers.FORMATTING_CODE.deserialize(configuration.getString("]", "faction-prefix-end"));
		this.isFactionPrefixFirstInChat = this.configuration.getBoolean(true, "faction-prefix-first-in-chat");
		this.nonFactionPlayerPrefix = TextSerializers.FORMATTING_CODE.deserialize(configuration.getString("", "non-faction-player-prefix"));
		this.showFactionEnterPhrase = this.configuration.getBoolean(true, "show-faction-enter-phrase");
	}

	public Text getFactionStartPrefix()
	{
		return this.factionStartPrefix;
	}

	public Text getFactionEndPrefix()
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

	public boolean shouldDisplayRank()
	{
		return this.shouldDisplayRank;
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
}
