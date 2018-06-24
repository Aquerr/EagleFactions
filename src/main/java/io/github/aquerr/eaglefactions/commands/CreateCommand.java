package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.MainLogic;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
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
import org.spongepowered.api.text.serializer.TextSerializers;

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
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_CANT_USE_THIS_FACTION_NAME));
                    return CommandResult.success();
                }

                Optional<Faction> optionalPlayerFaction = FactionLogic.getFactionByPlayerUUID(player.getUniqueId());

                if (!optionalPlayerFaction.isPresent())
                {
                    if(FactionsCache.getInstance().getFactions().stream().anyMatch(x -> x.Tag.toPlain().equalsIgnoreCase(factionTag)))
                    {
                        player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.PROVIDED_FACTION_TAG_IS_ALREADY_TAKEN));
                        return CommandResult.success();
                    }
                    else
                    {
                        //Check tag length
                        if(factionTag.length() > MainLogic.getMaxTagLength())
                        {
                            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.PROVIDED_FACTION_TAG_IS_TOO_LONG + " (" + PluginMessages.MAX + " " + MainLogic.getMaxTagLength() + " " + PluginMessages.CHARS + ")"));
                            return CommandResult.success();
                        }
                        if(factionTag.length() < MainLogic.getMinTagLength())
                        {
                            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.PROVIDED_FACTION_TAG_IS_TOO_SHORT + " (" + PluginMessages.MIN + " " + MainLogic.getMinTagLength() + " " + PluginMessages.CHARS + ")"));
                            return CommandResult.success();
                        }
                    }

                    if (!FactionsCache.getInstance().getFaction(factionName.toLowerCase()).isPresent())
                    {
                        //Check name length
                        if(factionName.length() > MainLogic.getMaxNameLength())
                        {
                            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.PROVIDED_FACTION_NAME_IS_TOO_LONG + " (" + PluginMessages.MAX + " " + MainLogic.getMaxNameLength() + " " + PluginMessages.CHARS + ")"));
                            return CommandResult.success();
                        }
                        if(factionName.length() < MainLogic.getMinNameLength())
                        {
                            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.PROVIDED_FACTION_NAME_IS_TOO_SHORT + " (" + PluginMessages.MIN + " " + MainLogic.getMinNameLength() + " " + PluginMessages.CHARS + ")"));
                            return CommandResult.success();
                        }

                        if (MainLogic.getCreateByItems())
                        {
                            return createByItems(factionName, factionTag, player);
                        }
                        else
                        {
                            FactionLogic.createFaction(factionName, factionTag, player.getUniqueId());
                            player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.FACTION + " " + factionName + " " + PluginMessages.HAS_BEEN_CREATED));
                            return CommandResult.success();
                        }
                    }
                    else
                    {
                        player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.FACTION_WITH_THE_SAME_NAME_ALREADY_EXISTS));
                    }
                } else
                {
                    player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_ARE_ALREADY_IN_A_FACTION + " " + PluginMessages.YOU_MUST_LEAVE_OR_DISBAND_YOUR_FACTION_FIRST));
                }


            } else
            {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
            }
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.WRONG_COMMAND_ARGUMENTS));
            source.sendMessage(Text.of(TextColors.RED, PluginMessages.USAGE + " /f create <tag> <faction name>"));
        }

        return CommandResult.success();
    }

    private CommandResult createByItems(String factionName, String factionTag, Player player)
    {
        HashMap<String, Integer> requiredItems = MainLogic.getRequiredItemsToCreate();
        Inventory inventory = player.getInventory();
        int allRequiredItems = requiredItems.size();
        int foundItems = 0;

        for (String requiredItem : requiredItems.keySet())
        {
            String[] idAndVariant = requiredItem.split(":");

            String itemId = idAndVariant[0] + ":" + idAndVariant[1];
            Optional<ItemType> itemType = Sponge.getRegistry().getType(ItemType.class, itemId);

            if(itemType.isPresent())
            {
                ItemStack itemStack = ItemStack.builder()
                        .itemType(itemType.get()).build();
                itemStack.setQuantity(requiredItems.get(requiredItem));

                if (idAndVariant.length == 3)
                {
                    if (itemType.get().getBlock().isPresent())
                    {
                        int variant = Integer.parseInt(idAndVariant[2]);
                        BlockState blockState = (BlockState) itemType.get().getBlock().get().getAllBlockStates().toArray()[variant];
                        itemStack = ItemStack.builder().fromBlockState(blockState).build();
                    }
                }

                if (inventory.contains(itemStack))
                {
                    foundItems += 1;
                }
                else
                {
                    player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_DONT_HAVE_ENOUGH_RESOURCES_TO_CREATE_A_FACTION));
                    break;
                }
            }
        }

        if (allRequiredItems == foundItems)
        {
            for (String requiredItem : requiredItems.keySet())
            {
                String[] idAndVariant = requiredItem.split(":");
                String itemId = idAndVariant[0] + ":" + idAndVariant[1];

                Optional<ItemType> itemType = Sponge.getRegistry().getType(ItemType.class, itemId);

                if(itemType.isPresent())
                {
                    ItemStack itemStack = ItemStack.builder()
                            .itemType(itemType.get()).build();
                    itemStack.setQuantity(requiredItems.get(requiredItem));

                    if (idAndVariant.length == 3)
                    {
                        if (itemType.get().getBlock().isPresent())
                        {
                            int variant = Integer.parseInt(idAndVariant[2]);
                            BlockState blockState = (BlockState) itemType.get().getBlock().get().getAllBlockStates().toArray()[variant];
                            itemStack = ItemStack.builder().fromBlockState(blockState).build();
                        }
                    }

                    inventory.query(QueryOperationTypes.ITEM_TYPE.of(itemType.get())).poll(itemStack.getQuantity());
                }
            }

            FactionLogic.createFaction(factionName, factionTag, player.getUniqueId());
            player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, PluginMessages.FACTION + " " + factionName + " " + PluginMessages.HAS_BEEN_CREATED));
            return CommandResult.success();
        }
        return CommandResult.success();
    }
}
