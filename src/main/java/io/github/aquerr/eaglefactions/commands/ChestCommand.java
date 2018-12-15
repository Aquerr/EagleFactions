package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.message.PluginMessages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class ChestCommand extends AbstractCommand
{
    public ChestCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if(!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        if(!super.getPlugin().getConfiguration().getConfigFields().canUseFactionChest())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "Faction's chest is turned off on this server."));

        Player player = (Player)source;
        Optional<Faction> optionalFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());

        if(!optionalFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        Faction faction = optionalFaction.get();

        Optional<Container> optionalContainer = player.openInventory(faction.getChest().toInventory());
        if(optionalContainer.isPresent())
        {
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, Text.of("You opened faction's chest!")));
        }
        return CommandResult.success();
    }

//    private Consumer<InteractInventoryEvent.Close> test()
//    {
//        return new Consumer<InteractInventoryEvent.Close>()
//        {
//            @Override
//            public void accept(InteractInventoryEvent.Close open)
//            {
//                Inventory inventory = open.getTargetInventory();
//                User user = null;
//                if(open.getCause().containsType(Player.class))
//                {
//                    user = open.getCause().first(Player.class).get();
//                }
//                else if(open.getCause().containsType(User.class))
//                {
//                    user = open.getCause().first(User.class).get();
//                }
//
//                if(user == null)
//                    return;
//
//                Optional<Faction> optionalFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(user.getUniqueId());
//                if(optionalFaction.isPresent())
//                {
//                    getPlugin().getFactionLogic().setChest(optionalFaction.get(), inventory);
//                }
//            }
//        };
//    }
}
