package io.github.aquerr.eaglefactions.scheduling;

import java.util.concurrent.Callable;

public interface EagleFactionsCallableTask<V> extends Callable
{
    V call() throws Exception;
}
