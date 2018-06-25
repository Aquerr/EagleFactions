package io.github.aquerr.eaglefactions.caching;

import com.google.inject.AbstractModule;

public class CacheModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind(FactionsCache.class).asEagerSingleton();
    }
}
