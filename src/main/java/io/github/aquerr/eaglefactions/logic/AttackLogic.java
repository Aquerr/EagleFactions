package io.github.aquerr.eaglefactions.logic;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class AttackLogic
{
    public static void attack(Player player, Vector3i attackedChunk, int seconds)
    {
        if(attackedChunk.toString().equals(player.getLocation().getChunkPosition().toString()))
        {
            if(seconds == MainLogic.getAttackTime())
            {
                informAboutDestroying(FactionLogic.getFactionNameByChunk(player.getWorld().getUniqueId(), attackedChunk));
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Claim destroyed!"));
                
                FactionLogic.removeClaim(FactionLogic.getFactionNameByChunk(player.getWorld().getUniqueId(), attackedChunk), player.getWorld().getUniqueId(), attackedChunk);
            }
            else
            {
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RESET, seconds));
                Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
                taskBuilder.execute(new Runnable()
                {
                     @Override
                     public void run()
                     {
                                  attack(player, attackedChunk, seconds + 1);
                     }

                }).delay(1, TimeUnit.SECONDS).name("EagleFactions - Attack").submit(Sponge.getPluginManager().getPlugin(PluginInfo.Id).get().getInstance().get());
            }
        }
        else
        {
            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You moved from the chunk!"));
        }
    }

    public static void blockClaiming(String factionName)
    {
        if(!EagleFactions.AttackedFactions.contains(factionName)) EagleFactions.AttackedFactions.add(factionName);

        restoreClaiming(factionName);
    }

    public static void restoreClaiming(String factionName)
    {
        if(Sponge.getScheduler().getScheduledTasks(EagleFactions.getEagleFactions()).stream().anyMatch(x->x.getName().equals("EagleFactions - Restore Claiming for " + factionName)))
        {
            Task scheduledTask = (Task)Sponge.getScheduler().getTasksByName("EagleFactions - Restore Claiming for " + factionName).toArray()[0];
            scheduledTask.cancel();

            Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder().name("EagleFactions - Restore Claiming for " + factionName);
            taskBuilder.execute(new Runnable() {
                @Override
                public void run() {
                    if(EagleFactions.AttackedFactions.contains(factionName)) EagleFactions.AttackedFactions.remove(factionName);
                }
            }).delay(2, TimeUnit.MINUTES).submit(Sponge.getPluginManager().getPlugin(PluginInfo.Id).get().getInstance().get());
        }
        else
        {
            Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder().name("EagleFactions - Restore Claiming for " + factionName);
            taskBuilder.execute(new Runnable() {
                @Override
                public void run() {
                    if(EagleFactions.AttackedFactions.contains(factionName)) EagleFactions.AttackedFactions.remove(factionName);
                }
            }).delay(2, TimeUnit.MINUTES).submit(Sponge.getPluginManager().getPlugin(PluginInfo.Id).get().getInstance().get());
        }
    }

    public static void informAboutAttack(String factionName)
    {
        List<Player> playersList = FactionLogic.getPlayersOnline(factionName);

        playersList.forEach(x -> x.sendMessage(Text.of(PluginInfo.PluginPrefix, "Your faction is under ", TextColors.RED, "attack", TextColors.RESET, "!")));
    }

    public static void informAboutDestroying(String factionName)
    {
        List<Player> playersList = FactionLogic.getPlayersOnline(factionName);

        playersList.forEach(x -> x.sendMessage(Text.of(PluginInfo.PluginPrefix, "One of your claims has been ", TextColors.RED, "destroyed", TextColors.RESET, " by an enemy!")));
    }
}
