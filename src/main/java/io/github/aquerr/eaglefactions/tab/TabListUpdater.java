package io.github.aquerr.eaglefactions.tab;

import io.github.aquerr.eaglefactions.scheduling.EagleFactionsRunnableTask;

import java.util.concurrent.atomic.AtomicBoolean;

public class TabListUpdater implements EagleFactionsRunnableTask
{
    private final FactionTabListService factionTabListService;

    public static final AtomicBoolean SHOULD_UPDATE = new AtomicBoolean(false);

    public static void requestUpdate()
    {
        SHOULD_UPDATE.compareAndSet(false, true);
    }

    public TabListUpdater(FactionTabListService factionTabListService)
    {
        this.factionTabListService = factionTabListService;
    }

    /**
     * Main entry point of the updater job.
     *
     * Updates tab-list for every online player.
     */
    @Override
    public void run()
    {
        try
        {
            if (SHOULD_UPDATE.compareAndSet(true, false))
            {
                factionTabListService.updateTabListForAllPlayers();
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }
}
