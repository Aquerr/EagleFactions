package io.github.aquerr.eaglefactions.common.scheduling;

import java.util.concurrent.Callable;

public interface EagleFactionsCallableTask<V> extends Callable
{
    V call() throws Exception;
}
