package io.github.aquerr.eaglefactions.common.listeners;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class ModifyBlockListener extends AbstractListener
{
    public ModifyBlockListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener
    public void onBlockModify(ChangeBlockEvent.Modify event)
    {
        User user = null;
        if(event.getCause().containsType(Player.class))
        {
            user = event.getCause().first(Player.class).get();
        }
        else if(event.getCause().containsType(User.class))
        {
            user = event.getCause().first(User.class).get();
        }

//        if(event.getContext().containsKey(EventContextKeys.OWNER)
//            && event.getContext().get(EventContextKeys.OWNER).isPresent()
//            && event.getContext().get(EventContextKeys.OWNER).get() instanceof Player)
//        {
//            Player player = (Player) event.getContext().get(EventContextKeys.OWNER).get();
        if(user != null)
        {
            for (Transaction<BlockSnapshot> transaction : event.getTransactions())
            {
                final Optional<Location<World>> optionalLocation = transaction.getFinal().getLocation();
                if(optionalLocation.isPresent() && !super.getPlugin().getProtectionManager().canInteractWithBlock(optionalLocation.get(), user))
                    event.setCancelled(true);
            }
        }
//        }
    }
}
