package io.github.aquerr.eaglefactions.storage.sql;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.entities.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.storage.PlayerStorage;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.sql.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class AbstractPlayerStorage implements PlayerStorage
{
    private static final String INSERT_PLAYER = "INSERT INTO Players (PlayerUUID, Name, Faction, Power, MaxPower, DeathInWarzone) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_PLAYER = "UPDATE Players SET PlayerUUID = ?, Name = ?, Faction = ?, Power = ?, MaxPower = ?, DeathInWarzone = ? WHERE PlayerUUID = ?";
//    private static final String MERGE_PLAYER = "MERGE INTO Players (PlayerUUID, Name, Power, MaxPower, DeathInWarzone) KEY (PlayerUUID) VALUES (?, ?, ?, ?, ?)";

//    private static final String SELECT_PLAYER_WHERE_PLAYERUUID = "SELECT Name FROM Players WHERE PlayerUUID=? LIMIT 1";
    private static final String SELECT_PLAYERS = "SELECT * FROM Players";
    private static final String SELECT_PLAYER_NAMES = "SELECT Name FROM Players";
    private static final String SELECT_PLAYER_WHERE_UUID = "SELECT * FROM Players WHERE PlayerUUID=? LIMIT 1";
//    private static final String SELECT_DEATH_IN_WARZONE_WHERE_PLAYERUUID = "SELECT DeathInWarzone FROM Players WHERE PlayerUUID=? LIMIT 1";
//    private static final String SELECT_PLAYER_WHERE_PLAYERUUID_AND_PLAYERNAME = "SELECT * FROM Players WHERE PlayerUUID=? AND Name=? LIMIT 1";
//    private static final String SELECT_POWER_WHERE_PLAYERUUID = "SELECT Power FROM Players WHERE PlayerUUID=? LIMIT 1";
//    private static final String SELECT_MAXPOWER_WHERE_PLAYERUUID = "SELECT MaxPower FROM Players WHERE PlayerUUID=? LIMIT 1";

//    private static final String UPDATE_POWER_WHERE_PLAYERUUID = "UPDATE Players SET Power=? WHERE PlayerUUID=?";
//    private static final String UPDATE_MAXPOWER_WHERE_PLAYERUUID = "UPDATE Players SET MaxPower=? WHERE PlayerUUID=?";
//    private static final String UPDATE_DEATH_IN_WARZONE_WHERE_PLAYERUUID = "UPDATE Players SET DeathInWarzone=? WHERE PlayerUUID=?";
//    private static final String UPDATE_PLAYERNAME_WHERE_PLAYERUUID = "UPDATE Players SET Name=? WHERE PlayerUUID=?";

    private static final String DELETE_PLAYERS = "DELETE FROM Players";

    private final EagleFactions plugin;
    private final SQLProvider sqlProvider;

    protected AbstractPlayerStorage(final EagleFactions plugin, final SQLProvider sqlProvider)
    {
        if(sqlProvider == null)
        {
            Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.RED, "Could not establish connection to the database. Aborting..."));
            throw new IllegalStateException("Could not establish connection to the database. Aborting...");
        }
        this.plugin = plugin;
        this.sqlProvider = sqlProvider;
    }

    @Override
    public FactionPlayer getPlayer(final UUID playerUUID)
    {
        FactionPlayer factionPlayer = null;
        try(final Connection connection = this.sqlProvider.getConnection())
        {
            final PreparedStatement statement = connection.prepareStatement(SELECT_PLAYER_WHERE_UUID);
            statement.setString(1, playerUUID.toString());
            final ResultSet resultSet = statement.executeQuery();
            if (resultSet.next())
            {
                final String name = resultSet.getString("Name");
                final String factionName = resultSet.getString("Faction");
                final float power = resultSet.getFloat("Power");
                final float maxpower = resultSet.getFloat("MaxPower");
                final boolean deathInWarzone = resultSet.getBoolean("DeathInWarzone");

                factionPlayer = new FactionPlayerImpl(name, playerUUID, factionName, power, maxpower, deathInWarzone);
            }
            resultSet.close();
            statement.close();
            return factionPlayer;
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean savePlayer(final FactionPlayer player)
    {
        try(final Connection connection = this.sqlProvider.getConnection())
        {
            //Add or update?
            PreparedStatement preparedStatement = connection.prepareStatement(SELECT_PLAYER_WHERE_UUID);
            preparedStatement.setString(1, player.getUniqueId().toString());
            final ResultSet factionSelect = preparedStatement.executeQuery();
            final boolean exists = factionSelect.next();
            preparedStatement.close();

            String queryToUse = exists ? UPDATE_PLAYER : INSERT_PLAYER;

            final PreparedStatement statement = connection.prepareStatement(queryToUse);
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, player.getName());
            statement.setString(3, player.getFactionName().orElse(null));
            statement.setFloat(4, player.getPower());
            statement.setFloat(5, player.getMaxPower());
            statement.setBoolean(6, player.diedInWarZone());
            if(exists)
                statement.setString(7, player.getUniqueId().toString()); //Where part
            final boolean didSucceed = statement.execute();
            statement.close();
            return didSucceed;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean savePlayers(final List<FactionPlayer> players)
    {
        try(final Connection connection = this.sqlProvider.getConnection())
        {
            final PreparedStatement statement = connection.prepareStatement(INSERT_PLAYER);
            for (final FactionPlayer player : players)
            {
                statement.setString(1, player.getUniqueId().toString());
                statement.setString(2, player.getName());
                statement.setString(3, player.getFactionName().orElse(null));
                statement.setFloat(4, player.getPower());
                statement.setFloat(5, player.getMaxPower());
                statement.setBoolean(6, player.diedInWarZone());
                statement.addBatch();
            }
            final int[] results = statement.executeBatch();

            statement.close();
            return results.length > 0;
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Set<String> getServerPlayerNames()
    {
        final Set<String> playerNames = new HashSet<>();
        try(final Connection connection = this.sqlProvider.getConnection())
        {
            final Statement statement = connection.createStatement();
            final ResultSet resultSet = statement.executeQuery(SELECT_PLAYER_NAMES);
            while (resultSet.next())
            {
                final String playerName = resultSet.getString("Name");
                playerNames.add(playerName);
            }
            resultSet.close();
            statement.close();
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
        }
        return playerNames;
    }

    @Override
    public Set<FactionPlayer> getServerPlayers()
    {
        final Set<FactionPlayer> factionPlayers = new HashSet<>();
        try(final Connection connection = this.sqlProvider.getConnection())
        {
            final Statement statement = connection.createStatement();
            final ResultSet resultSet = statement.executeQuery(SELECT_PLAYERS);
            while (resultSet.next())
            {
                final UUID playerUUID = UUID.fromString(resultSet.getString("PlayerUUID"));
                final String name = resultSet.getString("Name");
                final String factionName = resultSet.getString("Faction");
                final float power = resultSet.getFloat("Power");
                final float maxpower = resultSet.getFloat("MaxPower");
                final boolean deathInWarzone = resultSet.getBoolean("DeathInWarzone");

                FactionPlayer factionPlayer = new FactionPlayerImpl(name, playerUUID, factionName, power, maxpower, deathInWarzone);
                factionPlayers.add(factionPlayer);
            }
            resultSet.close();
            statement.close();
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
        }
        return factionPlayers;
    }

    @Override
    public void deletePlayers()
    {
        try(final Connection connection = this.sqlProvider.getConnection())
        {
            final Statement statement = connection.createStatement();
            statement.execute(DELETE_PLAYERS);
            statement.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}
