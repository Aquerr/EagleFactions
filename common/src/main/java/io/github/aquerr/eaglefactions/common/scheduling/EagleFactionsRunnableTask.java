package io.github.aquerr.eaglefactions.common.scheduling;

public interface EagleFactionsRunnableTask extends Runnable
{
    String getName();

    void run();
}
