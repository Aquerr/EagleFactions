//package io.github.aquerr.eaglefactions.integrations.ultimatechat.listener;
//
//import br.net.fabiozumbi12.UltimateChat.Sponge.API.SendChannelMessageEvent;
//import br.net.fabiozumbi12.UltimateChat.Sponge.UCChannel;
//import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
//import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
//import io.github.aquerr.eaglefactions.api.EagleFactions;
//import io.github.aquerr.eaglefactions.api.config.ChatConfig;
//import io.github.aquerr.eaglefactions.api.entities.ChatEnum;
//import io.github.aquerr.eaglefactions.api.entities.Faction;
//import io.github.aquerr.eaglefactions.api.events.FactionDisbandEvent;
//import io.github.aquerr.eaglefactions.integrations.ultimatechat.UltimateChatService;
//import io.github.aquerr.eaglefactions.listeners.AbstractListener;
//import io.github.aquerr.eaglefactions.messaging.chat.ChatMessageHelper;
//import org.spongepowered.api.Sponge;
//import org.spongepowered.api.entity.Tamer;
//import org.spongepowered.api.entity.living.player.Player;
//import org.spongepowered.api.event.Listener;
//import org.spongepowered.api.event.filter.IsCancelled;
//import org.spongepowered.api.util.Tristate;
//
//import java.io.IOException;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//public class  UltimateChatMessageListener extends AbstractListener
//{
//    private final ChatConfig chatConfig;
//
//    public UltimateChatMessageListener(final EagleFactions plugin)
//    {
//        super(plugin);
//        this.chatConfig = plugin.getConfiguration().getChatConfig();
//    }
//
//    @Listener
//    @IsCancelled(Tristate.FALSE)
//    public void onUchatMessage(final SendChannelMessageEvent event)
//    {
//        MessageReceiver sender = event.getSender();
//        if (!(sender instanceof Player))
//            return;
//        final Player player = (Player) sender;
//        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
//
//        ChatEnum chatType = Optional.ofNullable(EagleFactionsPlugin.CHAT_LIST.get(player.getUniqueId()))
//                .orElse(ChatEnum.GLOBAL);
//
//        if (optionalPlayerFaction.isPresent())
//        {
//            event.addTag("{" + UltimateChatService.FACTION_CHAT_TAG + "}", TextSerializers.FORMATTING_CODE.serialize(ChatMessageHelper.getChatPrefix(player)));
//            event.addTag("{" + UltimateChatService.FACTION_NAME_TAG + "}", optionalPlayerFaction.get().getName());
//            event.addTag("{" + UltimateChatService.FACTION_RANK_TAG + "}", TextSerializers.FORMATTING_CODE.serialize(Optional.ofNullable(ChatMessageHelper.getRankPrefix(chatType, optionalPlayerFaction.get(), player)).orElse(Text.EMPTY)));
//            event.addTag("{" + UltimateChatService.FACTION_TAG_TAG + "}", TextSerializers.FORMATTING_CODE.serialize(optionalPlayerFaction.get().getTag()));
//            event.addTag("{" + UltimateChatService.FACTION_TAG_WITH_PREFIX_SUFFIX + "}", TextSerializers.FORMATTING_CODE.serialize(this.chatConfig.getFactionStartPrefix())
//                    + TextSerializers.FORMATTING_CODE.serialize(optionalPlayerFaction.get().getTag())
//                    + TextSerializers.FORMATTING_CODE.serialize(this.chatConfig.getFactionEndPrefix()));
//
//            //TODO: Send message in correct UltimateChat channel (general, alliance, faction)
//
//            if (chatType == ChatEnum.ALLIANCE)
//            {
//                UCChannel chatChannel = UltimateChatService.getAllianceChannel(optionalPlayerFaction.get());
//                event.setChannel(chatChannel.getName());
//            }
//            else if (chatType == ChatEnum.FACTION)
//            {
//                UCChannel chatChannel = UltimateChatService.getFactionChannel(optionalPlayerFaction.get());
//                event.setChannel(chatChannel.getName());
//            }
//        }
//        else
//        {
//            //Suppress message for other people
//            if(this.chatConfig.shouldSuppressOtherFactionsMessagesWhileInTeamChat())
//            {
//                UCChannel ucChannel = new UCChannel(UUID.randomUUID().toString());
//                List<String> members = new LinkedList<>();
//                event.getChannel().getMembers().stream()
//                        .map(Sponge.getServer()::getPlayer)
//                        .map(Optional::get)
//                        .filter(this::filterPlayerWithFactionChat)
//                        .map(Tamer::getName)
//                        .forEach(members::add);
//                ucChannel.setMembers(members);
//                try
//                {
//                    UChat.get().getAPI().registerNewChannel(ucChannel);
//                }
//                catch (IOException e)
//                {
//                    e.printStackTrace();
//                }
//                event.setChannel(ucChannel.getName());
//            }
//
//            //Add non-faction prefix tag.
//            if(!this.chatConfig.getNonFactionPlayerPrefix().toPlain().equals(""))
//            {
//                event.addTag(UltimateChatService.FACTION_TAG_TAG, TextSerializers.FORMATTING_CODE.serialize(this.chatConfig.getNonFactionPlayerPrefix()));
//            }
//        }
//    }
//
//    @Listener
//    @IsCancelled(value = Tristate.FALSE)
//    public void onFactionDisband(FactionDisbandEvent.Post factionDisbandEvent)
//    {
//        // Delete alliance and faction channels for disbanded faction.
//        Optional.ofNullable(UChat.get().getAPI().getChannel(factionDisbandEvent.getFaction().getName() + "-alliance"))
//                .ifPresent(UChat.get().getConfig()::delChannel);
//        Optional.ofNullable(UChat.get().getAPI().getChannel(factionDisbandEvent.getFaction().getName() + "-faction"))
//                .ifPresent(UChat.get().getConfig()::delChannel);
//    }
//
//    private boolean filterPlayerWithFactionChat(Player player)
//    {
//        return EagleFactionsPlugin.CHAT_LIST.containsKey(player.getUniqueId()) && EagleFactionsPlugin.CHAT_LIST.get(player.getUniqueId()) != ChatEnum.GLOBAL;
//    }
//}
