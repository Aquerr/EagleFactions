package io.github.aquerr.eaglefactions.common.scheduling;

import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;

import java.util.concurrent.TimeUnit;

public class EagleFactionsScheduler
{
    private static final EagleFactionsScheduler INSTANCE = new EagleFactionsScheduler();

//    private final List<EagleFactionsRunnableTask> tasks = new ArrayList<>();
    private final Scheduler underlyingScheduler = Sponge.getScheduler();

    public static EagleFactionsScheduler getInstance()
    {
        return INSTANCE;
    }

    public void scheduleWithDelayAsync(EagleFactionsRunnableTask task, long delay)
    {
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
        taskBuilder.delay(delay, TimeUnit.SECONDS).execute(task).async().submit(EagleFactionsPlugin.getPlugin());
    }

    public void scheduleWithDelayAsync(EagleFactionsConsumerTask<Task> task, long delay)
    {
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
        taskBuilder.delay(delay, TimeUnit.SECONDS).execute(task).async().submit(EagleFactionsPlugin.getPlugin());
    }

    public void scheduleWithDelay(EagleFactionsRunnableTask task, long delay)
    {
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
        taskBuilder.delay(delay, TimeUnit.SECONDS).execute(task).submit(EagleFactionsPlugin.getPlugin());
    }

    public void scheduleWithDelayedIntervalAsync(EagleFactionsRunnableTask task, long delay, TimeUnit delayUnit, long interval, TimeUnit intervalUnit)
    {
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
        taskBuilder.delay(delay, delayUnit)
                .interval(interval, intervalUnit)
                .execute(task).async().submit(EagleFactionsPlugin.getPlugin());
    }

    public void scheduleWithDelayedIntervalAsync(EagleFactionsConsumerTask<Task> task, long delay, TimeUnit delayUnit, long interval, TimeUnit intervalUnit)
    {
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
        taskBuilder.delay(delay, delayUnit)
                .interval(interval, intervalUnit)
                .execute(task).async().submit(EagleFactionsPlugin.getPlugin());
    }

    public void scheduleWithDelayedInterval(EagleFactionsConsumerTask<Task> task, long delay, TimeUnit delayUnit, long interval, TimeUnit intervalUnit)
    {
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
        taskBuilder.delay(delay, delayUnit)
                .interval(interval, intervalUnit)
                .execute(task).submit(EagleFactionsPlugin.getPlugin());
    }

    public void scheduleWithDelayedInterval(EagleFactionsRunnableTask task, long delay, TimeUnit delayUnit, long interval, TimeUnit intervalUnit)
    {
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
        taskBuilder.delay(delay, delayUnit)
                .interval(interval, intervalUnit)
                .execute(task).submit(EagleFactionsPlugin.getPlugin());
    }

    public Scheduler getUnderlyingScheduler()
    {
        return this.underlyingScheduler;
    }
}
