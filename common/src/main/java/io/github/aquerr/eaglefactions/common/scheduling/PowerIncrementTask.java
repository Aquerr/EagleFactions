package io.github.aquerr.eaglefactions.common.scheduling;

import io.github.aquerr.eaglefactions.api.config.PowerConfig;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.managers.PowerManager;
import org.spongepowered.api.scheduler.Task;

import java.util.UUID;

public class PowerIncrementTask implements EagleFactionsConsumerTask<Task>
{
    private final PlayerManager playerManager;
    private final PowerManager powerManager;
    private final PowerConfig powerConfig;

    private final UUID playerUUID;

    public PowerIncrementTask(final PlayerManager playerManager, final PowerManager powerManager, final PowerConfig powerConfig, final UUID playerUUID)
    {
        this.playerManager = playerManager;
        this.powerManager = powerManager;
        this.powerConfig = powerConfig;
        this.playerUUID = playerUUID;
    }

    @Override
    public void accept(final Task task)
    {
        if (!this.playerManager.isPlayerOnline(playerUUID))
            task.cancel();

        if(this.playerManager.getPlayerPower(playerUUID) + this.powerConfig.getPowerIncrement() < this.playerManager.getPlayerMaxPower(playerUUID))
            this.powerManager.addPower(playerUUID, false);
        else
            this.powerManager.setPower(playerUUID, this.playerManager.getPlayerMaxPower(playerUUID));
    }
}
