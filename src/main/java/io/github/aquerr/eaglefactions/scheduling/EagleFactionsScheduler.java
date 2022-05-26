package io.github.aquerr.eaglefactions.scheduling;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;

import java.util.concurrent.TimeUnit;

public class EagleFactionsScheduler
{
    private static final EagleFactionsScheduler INSTANCE = new EagleFactionsScheduler();
    private final Scheduler asyncScheduler = Sponge.asyncScheduler();
    private final Scheduler syncScheduler = Sponge.server().scheduler();

    public static EagleFactionsScheduler getInstance()
    {
        return INSTANCE;
    }

    public ScheduledTask scheduleWithDelayAsync(EagleFactionsRunnableTask task, long delay)
    {
        return asyncScheduler.submit(Task.builder()
                .delay(delay, TimeUnit.SECONDS)
                .execute(task)
                .plugin(EagleFactionsPlugin.getPlugin().getPluginContainer())
                .build());
    }

    public ScheduledTask scheduleWithDelayAsync(EagleFactionsConsumerTask<ScheduledTask> task, long delay)
    {
        return asyncScheduler.submit(Task.builder()
                .delay(delay, TimeUnit.SECONDS)
                .execute(task)
                .plugin(EagleFactionsPlugin.getPlugin().getPluginContainer())
                .build());
    }

    public ScheduledTask scheduleWithDelayAsync(EagleFactionsRunnableTask task, long delay, TimeUnit timeUnit)
    {
        return this.asyncScheduler.submit(Task.builder()
                .execute(task)
                .delay(delay, timeUnit)
                .plugin(EagleFactionsPlugin.getPlugin().getPluginContainer())
                .build());
    }

    public ScheduledTask scheduleWithDelay(EagleFactionsRunnableTask task, long delay, TimeUnit timeUnit)
    {
        return this.syncScheduler.submit(Task.builder()
                .execute(task)
                .delay(delay, timeUnit)
                .plugin(EagleFactionsPlugin.getPlugin().getPluginContainer())
                .build());
    }

    public ScheduledTask scheduleWithDelayedIntervalAsync(EagleFactionsRunnableTask task, long delay, TimeUnit delayUnit, long interval, TimeUnit intervalUnit)
    {
        return this.asyncScheduler.submit(Task.builder()
                .execute(task)
                .delay(delay, delayUnit)
                .interval(interval, intervalUnit)
                .plugin(EagleFactionsPlugin.getPlugin().getPluginContainer())
                .build());
    }

    public ScheduledTask scheduleWithDelayedIntervalAsync(EagleFactionsConsumerTask<ScheduledTask> task, long delay, TimeUnit delayUnit, long interval, TimeUnit intervalUnit)
    {
        return this.asyncScheduler.submit(Task.builder()
                .execute(task)
                .delay(delay, delayUnit)
                .interval(interval, intervalUnit)
                .plugin(EagleFactionsPlugin.getPlugin().getPluginContainer())
                .build());
    }

    public ScheduledTask scheduleWithDelayedInterval(EagleFactionsConsumerTask<ScheduledTask> task, long delay, TimeUnit delayUnit, long interval, TimeUnit intervalUnit)
    {
        return this.syncScheduler.submit(Task.builder()
                .execute(task)
                .delay(delay, delayUnit)
                .interval(interval, intervalUnit)
                .plugin(EagleFactionsPlugin.getPlugin().getPluginContainer())
                .build());
    }

    public ScheduledTask scheduleWithDelayedInterval(EagleFactionsRunnableTask task, long delay, TimeUnit delayUnit, long interval, TimeUnit intervalUnit)
    {
        return this.syncScheduler.submit(Task.builder()
                .execute(task)
                .delay(delay, delayUnit)
                .interval(interval, intervalUnit)
                .plugin(EagleFactionsPlugin.getPlugin().getPluginContainer())
                .build());
    }

    public Scheduler getSyncScheduler()
    {
        return this.syncScheduler;
    }

    public Scheduler getAsyncScheduler()
    {
        return this.asyncScheduler;
    }
}
