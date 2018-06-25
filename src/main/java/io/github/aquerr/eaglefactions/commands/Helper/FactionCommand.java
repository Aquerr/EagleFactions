package io.github.aquerr.eaglefactions.commands.Helper;

import com.google.inject.Inject;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.logic.FactionLogic;

import java.util.logging.Logger;

/**
 * A collection of anything a command could need. Anything else can be injected by the specific class.
 */
public abstract class FactionCommand
{
    protected FactionsCache cache;
    protected Settings settings;
    protected FactionLogic factionLogic;
    protected Logger logger;

    @Inject
    public FactionCommand(FactionsCache cache, Settings settings, FactionLogic factionLogic, Logger logger){
        this.cache = cache;
        this.settings = settings;
        this.factionLogic = factionLogic;
        this.logger = logger;
    }

}
