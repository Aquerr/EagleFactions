package io.github.aquerr.eaglefactions.common.commands.management;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.common.entities.FactionImpl;
import io.github.aquerr.eaglefactions.common.entities.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.common.events.EventRunner;
import io.github.aquerr.eaglefactions.common.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import io.github.aquerr.eaglefactions.common.messaging.Placeholders;
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

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Aquerr on 2017-07-12.
 */
public class CreateCommand extends AbstractCommand
{
    private final FactionsConfig factionsConfig;

    public CreateCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        final Optional<String> optionalFactionName = context.getOne("name");
        final Optional<String> optionalFactionTag = context.getOne("tag");

        if (!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        //TODO: To test...
        if (!optionalFactionName.isPresent() || !optionalFactionTag.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.WRONG_COMMAND_ARGUMENTS), true);

        final Player player = (Player) source;
        final String factionName = optionalFactionName.get();
        final String factionTag = optionalFactionTag.get();

        if(!factionName.matches("^[A-Za-z][A-Za-z0-9]*$") || !factionTag.matches("^[A-Za-z][A-Za-z0-9]*$")){
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "Faction name and tag must be alphanumeric!"));
        }

        if (factionName.equalsIgnoreCase("SafeZone") || factionName.equalsIgnoreCase("WarZone"))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_USE_THIS_FACTION_NAME));

        final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());

        if (optionalPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_ARE_ALREADY_IN_A_FACTION));


        if (getPlugin().getFactionLogic().getFactionsTags().stream().anyMatch(x -> x.equalsIgnoreCase(factionTag)))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.PROVIDED_FACTION_TAG_IS_ALREADY_TAKEN));

        //Check tag length
        if (factionTag.length() > this.factionsConfig.getMaxTagLength())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.PROVIDED_FACTION_TAG_IS_TOO_LONG + " (" + Messages.MAX + " " + this.factionsConfig.getMaxTagLength() + " " + Messages.CHARS + ")"));
        else if (factionTag.length() < this.factionsConfig.getMinTagLength())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.PROVIDED_FACTION_TAG_IS_TOO_SHORT + " (" + Messages.MIN + " " + this.factionsConfig.getMinTagLength() + " " + Messages.CHARS + ")"));

        if (getPlugin().getFactionLogic().getFactionsNames().contains(factionName.toLowerCase()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.FACTION_WITH_THE_SAME_NAME_ALREADY_EXISTS));

        //Check name length
        if (factionName.length() > this.factionsConfig.getMaxNameLength())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.PROVIDED_FACTION_NAME_IS_TOO_LONG + " (" + Messages.MAX + " " + this.factionsConfig.getMaxNameLength() + " " + Messages.CHARS + ")"));
        else if (factionName.length() < this.factionsConfig.getMinNameLength())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.PROVIDED_FACTION_NAME_IS_TOO_SHORT + " (" + Messages.MIN + " " + this.factionsConfig.getMinNameLength() + " " + Messages.CHARS + ")"));

        if (this.factionsConfig.getFactionCreationByItems())
        {
            return createByItems(factionName, factionTag, player);
        }
        else
        {
            runCreationEventAndCreateFaction(factionName, factionTag, player);
            return CommandResult.success();
        }
    }

    private CommandResult createByItems(String factionName, String factionTag, Player player) throws CommandException
    {
        final Map<String, Integer> requiredItems = this.factionsConfig.getRequiredItemsToCreateFaction();
        final Inventory inventory = player.getInventory();
        final int allRequiredItems = requiredItems.size();
        int foundItems = 0;

        for (String requiredItem : requiredItems.keySet())
        {
            final String[] idAndVariant = requiredItem.split(":");
            final String itemId = idAndVariant[0] + ":" + idAndVariant[1];
            final Optional<ItemType> itemType = Sponge.getRegistry().getType(ItemType.class, itemId);

            if (!itemType.isPresent())
                continue;

            ItemStack itemStack = ItemStack.builder()
                    .itemType(itemType.get()).build();
            itemStack.setQuantity(requiredItems.get(requiredItem));

            if (idAndVariant.length == 3)
            {
                if (itemType.get().getBlock().isPresent())
                {
                    final int variant = Integer.parseInt(idAndVariant[2]);
                    final BlockState blockState = (BlockState) itemType.get().getBlock().get().getAllBlockStates().toArray()[variant];
                    itemStack = ItemStack.builder().fromBlockState(blockState).build();
                }
            }

            if (!inventory.contains(itemStack))
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_DONT_HAVE_ENOUGH_RESOURCES_TO_CREATE_A_FACTION));

            if (inventory.contains(itemStack))
            {
                foundItems += 1;
            }
        }

        if (allRequiredItems != foundItems)
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_DONT_HAVE_ENOUGH_RESOURCES_TO_CREATE_A_FACTION));

        for (final String requiredItem : requiredItems.keySet())
        {
            final String[] idAndVariant = requiredItem.split(":");
            final String itemId = idAndVariant[0] + ":" + idAndVariant[1];

            final Optional<ItemType> itemType = Sponge.getRegistry().getType(ItemType.class, itemId);

            if (!itemType.isPresent())
                continue;

            ItemStack itemStack = ItemStack.builder()
                    .itemType(itemType.get()).build();
            itemStack.setQuantity(requiredItems.get(requiredItem));

            if (idAndVariant.length == 3)
            {
                if (itemType.get().getBlock().isPresent())
                {
                    final int variant = Integer.parseInt(idAndVariant[2]);
                    final BlockState blockState = (BlockState) itemType.get().getBlock().get().getAllBlockStates().toArray()[variant];
                    itemStack = ItemStack.builder().fromBlockState(blockState).build();
                }
            }

            inventory.query(QueryOperationTypes.ITEM_TYPE.of(itemType.get())).poll(itemStack.getQuantity());
        }

        runCreationEventAndCreateFaction(factionName, factionTag, player);
        return CommandResult.success();
    }

    private void runCreationEventAndCreateFaction(final String factionName, final String factionTag, final Player player)
    {
        final Faction faction = FactionImpl.builder(factionName, Text.of(TextColors.GREEN, factionTag), player.getUniqueId()).build();

        final boolean isCancelled = EventRunner.runFactionCreateEventPre(player, faction);
        if (isCancelled)
            return;

        super.getPlugin().getFactionLogic().addFaction(faction);

        //Update player cache...
        final FactionPlayer factionPlayer = super.getPlugin().getStorageManager().getPlayer(player.getUniqueId());
        final FactionPlayer updatedPlayer = new FactionPlayerImpl(factionPlayer.getName(), factionPlayer.getUniqueId(), factionName, factionPlayer.getPower(), factionPlayer.getMaxPower(), factionPlayer.diedInWarZone());
        super.getPlugin().getStorageManager().savePlayer(updatedPlayer);

        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.FACTION_HAS_BEEN_CREATED, TextColors.GREEN, Collections.singletonMap(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, faction.getName())))));
        EventRunner.runFactionCreateEventPost(player, faction);
    }
}
