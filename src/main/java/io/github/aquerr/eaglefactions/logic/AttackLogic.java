package io.github.aquerr.eaglefactions.logic;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.services.PlayerService;
import io.github.aquerr.eaglefactions.services.PowerService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AttackLogic
{
    public static void attack(Player player, Vector3i attackedChunk, int seconds)
    {
        if(attackedChunk.toString().equals(player.getLocation().getChunkPosition().toString()))
        {
            if(seconds == 10)
            {
                   player.sendMessage(Text.of("Claim destroyed!"));
                   FactionLogic.removeClaim(FactionLogic.getFactionNameByChunk(player.getWorld().getUniqueId(), attackedChunk), player.getWorld().getUniqueId(), attackedChunk);
            }
            else
            {
                player.sendMessage(Text.of("Seconds: " + seconds));
                Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
                taskBuilder.execute(new Runnable()
                {
                     @Override
                     public void run()
                     {
                                  attack(player, attackedChunk, seconds + 1);
                     }

                }).delay(1, TimeUnit.SECONDS).name("EagleFaction - Attack").submit(Sponge.getPluginManager().getPlugin(PluginInfo.Id).get().getInstance().get());
            }
        }
        else
        {
            player.sendMessage(Text.of("You moved from the chunk!"));
        }
    }
}
