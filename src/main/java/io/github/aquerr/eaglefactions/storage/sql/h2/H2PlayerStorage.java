package io.github.aquerr.eaglefactions.storage.sql.h2;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.entities.IFactionPlayer;
import io.github.aquerr.eaglefactions.storage.IPlayerStorage;
import io.github.aquerr.eaglefactions.storage.sql.AbstractFactionStorage;
import io.github.aquerr.eaglefactions.storage.sql.AbstractPlayerStorage;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class H2PlayerStorage extends AbstractPlayerStorage
{
    public H2PlayerStorage(final EagleFactions eagleFactions)
    {
        super(eagleFactions, H2Provider.getInstance(eagleFactions));
    }
}
