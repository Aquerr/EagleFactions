package io.github.aquerr.eaglefactions.common.scheduling;

import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;

import java.util.concurrent.TimeUnit;

public class EagleFactionsScheduler
{
    private static final EagleFactionsScheduler INSTANCE = new EagleFactionsScheduler();
    private final Scheduler underlyingScheduler = Sponge.getScheduler();

    public static EagleFactionsScheduler getInstance()
    {
        return INSTANCE;
    }

    public Task scheduleWithDelayAsync(EagleFactionsRunnableTask task, long delay)
    {
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
        return taskBuilder.delay(delay, TimeUnit.SECONDS).execute(task).async().submit(EagleFactionsPlugin.getPlugin());
    }

    public Task scheduleWithDelayAsync(EagleFactionsConsumerTask<Task> task, long delay)
    {
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
        return taskBuilder.delay(delay, TimeUnit.SECONDS).execute(task).async().submit(EagleFactionsPlugin.getPlugin());
    }

    public Task scheduleWithDelay(EagleFactionsRunnableTask task, long delay)
    {
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
        return taskBuilder.delay(delay, TimeUnit.SECONDS).execute(task).submit(EagleFactionsPlugin.getPlugin());
    }

    public Task scheduleWithDelayedIntervalAsync(EagleFactionsRunnableTask task, long delay, TimeUnit delayUnit, long interval, TimeUnit intervalUnit)
    {
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
        return taskBuilder.delay(delay, delayUnit)
                .interval(interval, intervalUnit)
                .execute(task).async().submit(EagleFactionsPlugin.getPlugin());
    }

    public Task scheduleWithDelayedIntervalAsync(EagleFactionsConsumerTask<Task> task, long delay, TimeUnit delayUnit, long interval, TimeUnit intervalUnit)
    {
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
        return taskBuilder.delay(delay, delayUnit)
                .interval(interval, intervalUnit)
                .execute(task).async().submit(EagleFactionsPlugin.getPlugin());
    }

    public Task scheduleWithDelayedInterval(EagleFactionsConsumerTask<Task> task, long delay, TimeUnit delayUnit, long interval, TimeUnit intervalUnit)
    {
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
        return taskBuilder.delay(delay, delayUnit)
                .interval(interval, intervalUnit)
                .execute(task).submit(EagleFactionsPlugin.getPlugin());
    }

    public Task scheduleWithDelayedInterval(EagleFactionsRunnableTask task, long delay, TimeUnit delayUnit, long interval, TimeUnit intervalUnit)
    {
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();
        return taskBuilder.delay(delay, delayUnit)
                .interval(interval, intervalUnit)
                .execute(task).submit(EagleFactionsPlugin.getPlugin());
    }

    public Scheduler getUnderlyingScheduler()
    {
        return this.underlyingScheduler;
    }
}
