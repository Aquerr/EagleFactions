package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.ChatEnum;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class ChatCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Optional<ChatEnum> optionalChatType = context.<ChatEnum>getOne("chat");

        if(source instanceof Player)
        {
            Player player = (Player)source;

            if (FactionLogic.getFactionName(player.getUniqueId()) != null)
            {
                if(optionalChatType.isPresent())
                {
                    if(EagleFactions.ChatList.containsKey(player.getUniqueId()))
                    {
                        if (optionalChatType.get().equals(ChatEnum.Global))
                        {
                            EagleFactions.ChatList.remove(player.getUniqueId());
                            player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Changed chat to ", TextColors.GOLD, "Global", TextColors.RESET, "!"));
                        }
                        else
                        {
                            EagleFactions.ChatList.replace(player.getUniqueId(), EagleFactions.ChatList.get(player.getUniqueId()), optionalChatType.get());
                            player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Changed chat to ", TextColors.GOLD, optionalChatType.get(), TextColors.RESET, "!"));
                        }
                    }
                    else
                    {
                        EagleFactions.ChatList.put(player.getUniqueId(), optionalChatType.get());
                    }
                }
                else
                {
                    //If player is in alliance chat or faction chat.
                    if(EagleFactions.ChatList.containsKey(player.getUniqueId()))
                    {
                        if(EagleFactions.ChatList.get(player.getUniqueId()).equals(ChatEnum.Alliance))
                        {
                            EagleFactions.ChatList.replace(player.getUniqueId(), ChatEnum.Alliance, ChatEnum.Faction);
                            player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Changed chat to ", TextColors.GOLD, "Faction", TextColors.RESET, "!"));
                        }
                        else
                        {
                            EagleFactions.ChatList.remove(player.getUniqueId());
                            player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Changed chat to ", TextColors.GOLD, "Global", TextColors.RESET, "!"));
                        }
                    }
                    else
                    {
                        EagleFactions.ChatList.put(player.getUniqueId(), ChatEnum.Alliance);
                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, "Changed chat to ", TextColors.GOLD, "Alliance", TextColors.RESET, "!"));
                    }
                }
            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be in a faction in order to do this!"));
            }
        }
        else
        {
            source.sendMessage (Text.of (PluginInfo.ErrorPrefix, TextColors.RED, "Only in-game players can use this command!"));
        }
        return CommandResult.success();
    }
}