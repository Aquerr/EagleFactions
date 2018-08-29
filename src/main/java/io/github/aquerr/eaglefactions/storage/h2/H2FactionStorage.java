package io.github.aquerr.eaglefactions.storage.h2;

import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.storage.IFactionStorage;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

public class H2FactionStorage implements IFactionStorage
{
    public H2FactionStorage(Path configDir)
    {
        try
        {
            Connection connection = DriverManager.getConnection("jdbc:h2:" + configDir.toAbsolutePath().toString() + "/test", "sa", "");
            connection.close();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean addOrUpdateFaction(Faction faction)
    {
        return false;
    }

    @Override
    public boolean removeFaction(String factionName)
    {
        return false;
    }

    @Override
    public Faction getFaction(String factionName)
    {
        return null;
    }

    @Override
    public Map<String, Faction> getFactionsMap()
    {
        return null;
    }

    @Override
    public void load()
    {

    }
}
