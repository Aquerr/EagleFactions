package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.MainLogic;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.HashMap;
import java.util.Optional;

/**
 * Created by Aquerr on 2017-07-12.
 */
public class CreateCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Optional<String> optionalFactionName = context.<String>getOne("faction name");
        Optional<String> optionalFactionTag = context.<String>getOne("tag");

        if(optionalFactionName.isPresent() && optionalFactionTag.isPresent())
        {
            String factionName = optionalFactionName.get();
            String factionTag = optionalFactionTag.get();

            if (source instanceof Player)
            {
                Player player = (Player) source;

                if (factionName.equalsIgnoreCase("SafeZone") || factionName.equalsIgnoreCase("WarZone"))
                {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You can't use this faction name!"));
                    return CommandResult.success();
                }

                String playerFactionName = FactionLogic.getFactionName(player.getUniqueId());

                if (playerFactionName == null)
                {
                    if(FactionLogic.getFactionsTags().stream().anyMatch(x -> x.equalsIgnoreCase(factionTag)))
                    {
                        player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Provided faction tag is already taken!"));
                        return CommandResult.success();
                    }
                    else
                    {
                        //Check tag length
                        if(factionTag.length() > MainLogic.getMaxTagLength())
                        {
                            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Provided faction tag is too long! (Max " + MainLogic.getMaxTagLength() + " chars)"));
                            return CommandResult.success();
                        }
                        if(factionTag.length() < MainLogic.getMinTagLength())
                        {
                            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Provided faction tag is too short! (Min " + MainLogic.getMinTagLength() + " chars)"));
                            return CommandResult.success();
                        }
                    }

                    if (!FactionLogic.getFactionsNames().stream().anyMatch(x -> x.equalsIgnoreCase(factionName)))
                    {
                        //Check name length
                        if(factionName.length() > MainLogic.getMaxNameLength())
                        {
                            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Provided faction name is too long! (Max " + MainLogic.getMaxNameLength() + " chars)"));
                            return CommandResult.success();
                        }
                        if(factionName.length() < MainLogic.getMinNameLength())
                        {
                            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Provided faction name is too short! (Min " + MainLogic.getMinNameLength() + " chars)"));
                            return CommandResult.success();
                        }

                        if (MainLogic.getCreateByItems())
                        {
                            return createByItems(factionName, factionTag, player);
                        }
                        else
                        {
                            FactionLogic.createFaction(factionName, factionTag, player.getUniqueId());
                            player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Faction " + factionName + " has been created!"));
                            return CommandResult.success();
                        }
                    }
                    else
                    {
                        player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Faction with the same name already exists!"));
                    }
                } else
                {
                    player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You are already in a faction. You must leave or disband your faction first."));
                }


            } else
            {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Only in-game players can use this command!"));
            }
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Wrong command arguments!"));
            source.sendMessage(Text.of(TextColors.RED, "Usage: /f create <tag> <faction name>"));
        }

        return CommandResult.success();
    }

    private CommandResult createByItems(String factionName, String factionTag, Player player)
    {
        HashMap<String, Integer> requiredItems = MainLogic.getRequiredItemsToCreate();
        Inventory inventory = player.getInventory();
        int allRequiredItems = requiredItems.size();
        int foundItems = 0;

        for (String itemId : requiredItems.keySet())
        {
            Optional<ItemType> itemType = Sponge.getRegistry().getType(ItemType.class, itemId);

            if(itemType.isPresent())
            {
                ItemStack itemStack = ItemStack.builder()
                        .itemType(itemType.get()).build();
                itemStack.setQuantity(requiredItems.get(itemId));

                //TODO: This needs to be tested.
                if (inventory.contains(itemStack))
                {
                    foundItems += 1;
                }
                else
                {
                    player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You don't have enough resources to create a faction!"));
                    break;
                }
            }
        }

        if (allRequiredItems == foundItems)
        {
            for (String itemId : requiredItems.keySet())
            {
                Optional<ItemType> itemType = Sponge.getRegistry().getType(ItemType.class, itemId);

                if(itemType.isPresent())
                {
                    ItemStack itemStack = ItemStack.builder()
                            .itemType(itemType.get()).build();
                    itemStack.setQuantity(requiredItems.get(itemId));

                    //TODO: This needs to be tested.
                    inventory.query(QueryOperationTypes.ITEM_TYPE.of(itemType.get())).poll(itemStack.getQuantity());
                }
            }

            FactionLogic.createFaction(factionName, factionTag, player.getUniqueId());
            player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Faction " + factionName + " has been created!"));
            return CommandResult.success();
        }
        return CommandResult.success();
    }
}
