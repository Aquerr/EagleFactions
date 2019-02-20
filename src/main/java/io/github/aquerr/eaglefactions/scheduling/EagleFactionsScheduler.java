package io.github.aquerr.eaglefactions.scheduling;

import io.github.aquerr.eaglefactions.EagleFactions;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EagleFactionsScheduler
{
    private static final EagleFactionsScheduler INSTANCE = new EagleFactionsScheduler();

    private final List<EagleFactionsTask> tasks = new ArrayList<>();

    public static EagleFactionsScheduler getInstance()
    {
        return INSTANCE;
    }

    public void scheduleWithDelay(EagleFactionsTask task, long delay)
    {
        if(this.tasks.contains(task))
            throw new IllegalStateException("This task is already scheduled!");

        this.tasks.add(task);
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
        taskBuilder.delay(delay, TimeUnit.SECONDS).execute(task).submit(EagleFactions.getPlugin());
    }
}
