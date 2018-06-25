package io.github.aquerr.eaglefactions.commands.Helper;

import com.google.inject.Inject;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.config.Settings;

public class FactionCommand
{
    protected FactionsCache cache;
    protected Settings settings;

    @Inject
    public FactionCommand(FactionsCache cache, Settings settings){
        this.cache = cache;
        this.settings = settings;
    }

}
