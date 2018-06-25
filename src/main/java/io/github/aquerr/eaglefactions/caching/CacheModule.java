package io.github.aquerr.eaglefactions.caching;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.storage.HOCONFactionStorage;
import io.github.aquerr.eaglefactions.storage.IStorage;

import java.nio.file.Path;

public class CacheModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind(IStorage.class).to(HOCONFactionStorage.class);
    }

    @Provides
    @Named("config dir")
    public Path getConfigDir(EagleFactions eagleFactions)
    {
        return eagleFactions.getConfigDir();
    }

    @Provides
    public EagleFactions eagleFactions(){
        return EagleFactions.getPlugin();
    }
}
