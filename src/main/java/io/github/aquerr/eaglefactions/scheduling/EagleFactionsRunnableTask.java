package io.github.aquerr.eaglefactions.scheduling;

public interface EagleFactionsRunnableTask extends Runnable
{
    String getName();

    void run();
}
