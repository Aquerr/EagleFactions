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

	public ChatConfigImpl(final Configuration configuration)
	{
		this.configuration = configuration;
		reload();
	}

	@Override
	public void reload()
	{
		//Chat
		this.chatPrefixType = configuration.getString("tag", "faction-prefix");
		this.shouldDisplayRank = configuration.getBoolean(true, "faction-rank");
		this.suppressOtherFactionsMessagesWhileInTeamChat = configuration.getBoolean(false, "suppress-other-factions-messages-while-in-team-chat");
		this.displayProtectionSystemMessages = configuration.getBoolean(true, "display-protection-system-messages");
		this.canColorTags = configuration.getBoolean(true, "colored-tags-allowed");
		this.factionStartPrefix = TextSerializers.FORMATTING_CODE.deserialize(configuration.getString("[", "faction-prefix-start"));
		this.factionEndPrefix = TextSerializers.FORMATTING_CODE.deserialize(configuration.getString("]", "faction-prefix-end"));
		this.isFactionPrefixFirstInChat = configuration.getBoolean(true, "faction-prefix-first-in-chat");
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
	public boolean shouldSupressOtherFactionsMessagesWhileInTeamChat()
	{
		return this.suppressOtherFactionsMessagesWhileInTeamChat;
	}

	@Override
	public boolean shouldDisplayProtectionSystemMessages()
	{
		return this.displayProtectionSystemMessages;
	}
}
