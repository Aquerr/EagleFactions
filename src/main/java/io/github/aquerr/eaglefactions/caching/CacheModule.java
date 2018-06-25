package io.github.aquerr.eaglefactions.caching;

import com.google.inject.AbstractModule;
import io.github.aquerr.eaglefactions.storage.HOCONFactionStorage;
import io.github.aquerr.eaglefactions.storage.IStorage;

public class CacheModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind(FactionsCache.class).asEagerSingleton();
        bind(IStorage.class).to(HOCONFactionStorage.class);
    }
}
