package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionChest;
import io.github.aquerr.eaglefactions.events.FactionCreateEvent;
import io.github.aquerr.eaglefactions.message.PluginMessages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
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
public class CreateCommand extends AbstractCommand
{
    public CreateCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Optional<String> optionalFactionName = context.<String>getOne("faction name");
        Optional<String> optionalFactionTag = context.<String>getOne("tag");

        if (!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        //TODO: To test...
        if (!optionalFactionName.isPresent() || !optionalFactionTag.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.WRONG_COMMAND_ARGUMENTS), true);

        Player player = (Player) source;
        String factionName = optionalFactionName.get();
        String factionTag = optionalFactionTag.get();

        if (factionName.equalsIgnoreCase("SafeZone") || factionName.equalsIgnoreCase("WarZone"))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_CANT_USE_THIS_FACTION_NAME));

        Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());

        if (optionalPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_ARE_ALREADY_IN_A_FACTION));


        if (getPlugin().getFactionLogic().getFactionsTags().stream().anyMatch(x -> x.equalsIgnoreCase(factionTag)))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.PROVIDED_FACTION_TAG_IS_ALREADY_TAKEN));

        //Check tag length
        if (factionTag.length() > getPlugin().getConfiguration().getConfigFields().getMaxTagLength())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.PROVIDED_FACTION_TAG_IS_TOO_LONG + " (" + PluginMessages.MAX + " " + getPlugin().getConfiguration().getConfigFields().getMaxTagLength() + " " + PluginMessages.CHARS + ")"));
        else if (factionTag.length() < getPlugin().getConfiguration().getConfigFields().getMinTagLength())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.PROVIDED_FACTION_TAG_IS_TOO_SHORT + " (" + PluginMessages.MIN + " " + getPlugin().getConfiguration().getConfigFields().getMinTagLength() + " " + PluginMessages.CHARS + ")"));

        if (getPlugin().getFactionLogic().getFactionsNames().contains(factionName.toLowerCase()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.FACTION_WITH_THE_SAME_NAME_ALREADY_EXISTS));

        //Check name length
        if (factionName.length() > getPlugin().getConfiguration().getConfigFields().getMaxNameLength())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.PROVIDED_FACTION_NAME_IS_TOO_LONG + " (" + PluginMessages.MAX + " " + getPlugin().getConfiguration().getConfigFields().getMaxNameLength() + " " + PluginMessages.CHARS + ")"));
        else if (factionName.length() < getPlugin().getConfiguration().getConfigFields().getMinNameLength())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.PROVIDED_FACTION_NAME_IS_TOO_SHORT + " (" + PluginMessages.MIN + " " + getPlugin().getConfiguration().getConfigFields().getMinNameLength() + " " + PluginMessages.CHARS + ")"));

        if (getPlugin().getConfiguration().getConfigFields().getFactionCreationByItems())
        {
            return createByItems(factionName, factionTag, player);
        }
        else
        {
            Faction faction = Faction.builder()
                    .setName(factionName)
                    .setTag(Text.of(TextColors.GREEN, factionTag))
                    .setLeader(player.getUniqueId())
                    .setChest(new FactionChest())
                    .build();

            //Testing with events
            boolean isCancelled = FactionCreateEvent.runCreationEvent(player, faction);
            if (isCancelled)
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "Something prevented faction from creating..."));
            //Testing with events

            getPlugin().getFactionLogic().addFaction(faction);
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.FACTION + " " + factionName + " " + PluginMessages.HAS_BEEN_CREATED));
            return CommandResult.success();
        }
    }

    private CommandResult createByItems(String factionName, String factionTag, Player player) throws CommandException
    {
        HashMap<String, Integer> requiredItems = getPlugin().getConfiguration().getConfigFields().getRequiredItemsToCreateFaction();
        Inventory inventory = player.getInventory();
        int allRequiredItems = requiredItems.size();
        int foundItems = 0;

        for (String requiredItem : requiredItems.keySet())
        {
            String[] idAndVariant = requiredItem.split(":");

            String itemId = idAndVariant[0] + ":" + idAndVariant[1];
            Optional<ItemType> itemType = Sponge.getRegistry().getType(ItemType.class, itemId);

            if (itemType.isPresent())
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

                if (!inventory.contains(itemStack))
                    throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_DONT_HAVE_ENOUGH_RESOURCES_TO_CREATE_A_FACTION));

                if (inventory.contains(itemStack))
                {
                    foundItems += 1;
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

                if (itemType.isPresent())
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

            Faction faction = Faction.builder()
                    .setName(factionName)
                    .setTag(Text.of(TextColors.GREEN, factionTag))
                    .setLeader(player.getUniqueId())
                    .setChest(new FactionChest())
                    .build();

            boolean isCancelled = FactionCreateEvent.runCreationEvent(player, faction);
            if (isCancelled)
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "Something prevented faction from creating..."));

            super.getPlugin().getFactionLogic().addFaction(faction);
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, PluginMessages.FACTION + " " + factionName + " " + PluginMessages.HAS_BEEN_CREATED));
            return CommandResult.success();
        }
        return CommandResult.success();
    }
}
