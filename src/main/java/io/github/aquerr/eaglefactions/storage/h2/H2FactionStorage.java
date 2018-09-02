package io.github.aquerr.eaglefactions.storage.h2;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.storage.IFactionStorage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.sql.*;
import java.util.Map;

public class H2FactionStorage implements IFactionStorage
{
    private static final String SELECT_FACTIONS = "SELECT * FROM FACTIONS";
    private static final String UPDATE_FACTION = "UPDATE Factions SET Name=?, Tag=?, TagColor=?, Leader=?, Home=?, LastOnline=? WHERE Name=?";

    private final EagleFactions plugin;
    private Connection _connection;

    public H2FactionStorage(EagleFactions plugin)
    {
        this.plugin = plugin;

        try
        {
            this._connection = DriverManager.getConnection("jdbc:h2:" + plugin.getConfigDir().resolve("data/h2").toAbsolutePath().toString() + "/database",
                    this.plugin.getConfiguration().getConfigFileds().getStorageUserName(),
                    this.plugin.getConfiguration().getConfigFileds().getStoragePassword());
            int databaseVersionNumber = getDatabaseVersion();

            //Get all .sql files
            URL resourcesFolderURL = this.plugin.getResource("queries/h2");
            File resouresFolder = new File(resourcesFolderURL.getPath());
            File[] resources = resouresFolder.listFiles();
            for(File resource : resources)
            {
                int scriptNumber = Integer.parseInt(resource.getName().substring(0, 3));
                if(scriptNumber <= databaseVersionNumber)
                    continue;

                try(BufferedReader bufferedReader = new BufferedReader(new FileReader(resource)))
                {
                    try(Statement statement = _connection.createStatement())
                    {
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;

                        while((line = bufferedReader.readLine()) != null)
                        {
                            if(line.startsWith("--"))
                                continue;

                            stringBuilder.append(line);

                            if(line.endsWith(";"))
                            {
                                statement.addBatch(stringBuilder.toString().trim());
                                stringBuilder = new StringBuilder();
                            }
                        }
                        statement.executeBatch();
                    }
                }
                catch(Exception exception)
                {
                    exception.printStackTrace();
                }
            }
            _connection.close();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

    private int getDatabaseVersion() throws SQLException
    {
        ResultSet resultSet = _connection.getMetaData().getTables(null, null, "Version", null);
        while(resultSet.next())
        {
            if(resultSet.getString(3).equalsIgnoreCase("Version"))
            {
                try(Statement statement = _connection.createStatement())
                {
                    ResultSet resultSet1 = statement.executeQuery("SELECT Version FROM Version");
                    if(resultSet1.last())
                    {
                        return resultSet1.getInt("Version");
                    }
                }
            }
        }
        return 0;
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
