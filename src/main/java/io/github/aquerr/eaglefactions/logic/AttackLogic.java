package io.github.aquerr.eaglefactions.logic;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class AttackLogic
{
    public static void attack(Player player, Vector3i attackedChunk)
    {
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

        taskBuilder.interval(1, TimeUnit.SECONDS).execute(new Consumer<Task>()
        {
            int seconds = 1;

            @Override
            public void accept(Task task)
            {
                if(attackedChunk.toString().equals(player.getLocation().getChunkPosition().toString()))
                {
                    if(seconds == MainLogic.getAttackTime())
                    {
                        String chunkFactionName = FactionLogic.getFactionNameByChunk(player.getWorld().getUniqueId(), attackedChunk);
                        Faction chunkFaction = FactionLogic.getFaction(chunkFactionName);

                        informAboutDestroying(chunkFaction);
                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Claim destroyed!"));

                        FactionLogic.removeClaim(chunkFaction, player.getWorld().getUniqueId(), attackedChunk);
                        task.cancel();
                    }
                    else
                    {
                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RESET, seconds));
                        seconds++;
                    }
                }
                else
                {
                    player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You moved from the chunk!"));
                    task.cancel();
                }
            }
        }).submit(EagleFactions.getEagleFactions());
    }

    public static void blockClaiming(String factionName)
    {
        if(EagleFactions.AttackedFactions.containsKey(factionName))
        {
            EagleFactions.AttackedFactions.replace(factionName, 120);
        }
        else
        {
            EagleFactions.AttackedFactions.put(factionName, 120);
            runClaimingRestorer(factionName);
        }
    }

    public static void runClaimingRestorer(String factionName)
    {

        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

        taskBuilder.interval(1, TimeUnit.SECONDS).execute(new Consumer<Task>()
        {
            @Override
            public void accept(Task task)
            {

                if(EagleFactions.AttackedFactions.containsKey(factionName))
                {
                    int seconds = EagleFactions.AttackedFactions.get(factionName);

                    if (seconds <= 0)
                    {
                        EagleFactions.AttackedFactions.remove(factionName);
                        task.cancel();
                    }
                    else
                    {
                        EagleFactions.AttackedFactions.replace(factionName, seconds, seconds - 1);
                    }
                }
            }
        }).submit(EagleFactions.getEagleFactions());
    }

    public static void informAboutAttack(Faction faction)
    {
        List<Player> playersList = FactionLogic.getOnlinePlayers(faction);

        playersList.forEach(x -> x.sendMessage(Text.of(PluginInfo.PluginPrefix, "Your faction is under ", TextColors.RED, "attack", TextColors.RESET, "!")));
    }

    public static void informAboutDestroying(Faction faction)
    {
        List<Player> playersList = FactionLogic.getOnlinePlayers(faction);

        playersList.forEach(x -> x.sendMessage(Text.of(PluginInfo.PluginPrefix, "One of your claims has been ", TextColors.RED, "destroyed", TextColors.RESET, " by an enemy!")));
    }

    public static void blockHome(UUID playerUUID)
    {
        if(EagleFactions.BlockedHome.containsKey(playerUUID))
        {
            EagleFactions.BlockedHome.replace(playerUUID, MainLogic.getHomeBlockTimeAfterDeath());
        }
        else
        {
            EagleFactions.BlockedHome.put(playerUUID, MainLogic.getHomeBlockTimeAfterDeath());
            runHomeUsageRestorer(playerUUID);
        }
    }

    public static void runHomeUsageRestorer(UUID playerUUID)
    {
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

        taskBuilder.interval(1, TimeUnit.SECONDS).execute(new Consumer<Task>()
        {
            @Override
            public void accept(Task task)
            {
                if (EagleFactions.BlockedHome.containsKey(playerUUID))
                {
                    int seconds = EagleFactions.BlockedHome.get(playerUUID);

                    if (seconds <= 0)
                    {
                        EagleFactions.BlockedHome.remove(playerUUID);
                        task.cancel();
                    }
                    else
                    {
                        EagleFactions.BlockedHome.replace(playerUUID, seconds, seconds - 1);
                    }
                }
            }
        }).submit(EagleFactions.getEagleFactions());
    }

}
