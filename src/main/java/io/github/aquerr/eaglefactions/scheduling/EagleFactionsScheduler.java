package io.github.aquerr.eaglefactions.scheduling;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public final class EagleFactionsScheduler
{
    private static final EagleFactionsScheduler INSTANCE = new EagleFactionsScheduler();
    private final Scheduler asyncScheduler = Sponge.asyncScheduler();
    private final Scheduler syncScheduler = Sponge.server().scheduler();

    private static final Map<String, ScheduledTask> UNIQUE_TASKS = new HashMap<>();

    public static EagleFactionsScheduler getInstance()
    {
        return INSTANCE;
    }

    private EagleFactionsScheduler()
    {

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

    public ScheduledTask scheduleWithDelayedIntervalAsync(EagleFactionsConsumerTask<ScheduledTask> task,
                                                          long delay,
                                                          TimeUnit delayUnit,
                                                          long interval,
                                                          TimeUnit intervalUnit)
    {
        return scheduleWithDelayedIntervalAsync(task, delay, delayUnit, interval, intervalUnit, null);
    }

    public ScheduledTask scheduleWithDelayedIntervalAsync(EagleFactionsConsumerTask<ScheduledTask> task,
                                                          long delay,
                                                          TimeUnit delayUnit,
                                                          long interval,
                                                          TimeUnit intervalUnit,
                                                          @Nullable String taskIdentifier)
    {
        Task task1 = Task.builder()
                .execute(task)
                .delay(delay, delayUnit)
                .interval(interval, intervalUnit)
                .plugin(EagleFactionsPlugin.getPlugin().getPluginContainer())
                .build();

        if (taskIdentifier != null)
        {
            cancelTask(taskIdentifier);
        }

        ScheduledTask scheduledTask =  this.asyncScheduler.submit(task1);
        UNIQUE_TASKS.put(taskIdentifier, scheduledTask);
        return scheduledTask;
    }

    public ScheduledTask scheduleWithDelayedInterval(EagleFactionsConsumerTask<ScheduledTask> task,
                                                     long delay,
                                                     TimeUnit delayUnit,
                                                     long interval,
                                                     TimeUnit intervalUnit,
                                                     @Nullable String taskIdentifier)
    {

        Task task1 = Task.builder()
                .execute(task)
                .delay(delay, delayUnit)
                .interval(interval, intervalUnit)
                .plugin(EagleFactionsPlugin.getPlugin().getPluginContainer())
                .build();

        if (taskIdentifier != null)
        {
            cancelTask(taskIdentifier);
        }

        ScheduledTask scheduledTask =  this.syncScheduler.submit(task1);
        UNIQUE_TASKS.put(taskIdentifier, scheduledTask);
        return scheduledTask;
    }

    public ScheduledTask scheduleWithDelayedInterval(EagleFactionsConsumerTask<ScheduledTask> task,
                                                     long delay,
                                                     TimeUnit delayUnit,
                                                     long interval,
                                                     TimeUnit intervalUnit)
    {
        return scheduleWithDelayedInterval(task, delay, delayUnit, interval, intervalUnit, null);
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

    public void cancelTask(String taskIdentifier)
    {
        Optional.ofNullable(UNIQUE_TASKS.get(taskIdentifier)).ifPresent(scheduledTask -> {
            scheduledTask.cancel();
            UNIQUE_TASKS.remove(taskIdentifier);
        });
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
