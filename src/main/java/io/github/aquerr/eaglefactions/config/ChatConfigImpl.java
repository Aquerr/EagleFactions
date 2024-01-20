package io.github.aquerr.eaglefactions.config;

import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.config.Configuration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

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
	private TextColor defaultTagColor = NamedTextColor.GREEN;
	private boolean displayFactionTagsInTabList = true;

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
		this.displayFactionTagsInTabList = this.configuration.getBoolean(true, "display-faction-tags-in-tablist");

		this.defaultTagColor = TextColor.fromHexString(this.configuration.getString(NamedTextColor.GREEN.asHexString(), "default-tag-color"));
	}

	@Override
	public TextComponent getFactionStartPrefix()
	{
		return this.factionStartPrefix;
	}

	@Override
	public TextComponent getFactionEndPrefix()
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
	public boolean shouldDisplayFactionTagsInTabList()
	{
		return displayFactionTagsInTabList;
	}
}
