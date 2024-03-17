package io.github.aquerr.eaglefactions.storage.sql;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.entities.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.storage.PlayerStorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class AbstractPlayerStorage implements PlayerStorage
{
    private static final String INSERT_PLAYER = "INSERT INTO player (player_uuid, name, faction_name, power, max_power, death_in_warzone) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_PLAYER = "UPDATE player SET player_uuid = ?, name = ?, faction_name = ?, power = ?, max_power = ?, death_in_warzone = ? WHERE player_uuid = ?";
    private static final String SELECT_PLAYERS = "SELECT * FROM player";
    private static final String SELECT_PLAYER_NAMES = "SELECT Name FROM player";
    private static final String SELECT_PLAYER_WHERE_UUID = "SELECT * FROM player WHERE player_uuid=? LIMIT 1";

    private static final String DELETE_PLAYERS = "DELETE FROM player";

    private final SQLConnectionProvider sqlConnectionProvider;

    protected AbstractPlayerStorage(final SQLConnectionProvider sqlConnectionProvider)
    {
        if(sqlConnectionProvider == null)
        {
            EagleFactionsPlugin.getPlugin().getLogger().error("Could not establish connection to the database. Aborting...");
            throw new IllegalStateException("Could not establish connection to the database. Aborting...");
        }
        this.sqlConnectionProvider = sqlConnectionProvider;
    }

    @Override
    public FactionPlayer getPlayer(final UUID playerUUID)
    {
        FactionPlayer factionPlayer = null;
        try(final Connection connection = this.sqlConnectionProvider.getConnection())
        {
            final PreparedStatement statement = connection.prepareStatement(SELECT_PLAYER_WHERE_UUID);
            statement.setString(1, playerUUID.toString());
            final ResultSet resultSet = statement.executeQuery();
            if (resultSet.next())
            {
                final String name = resultSet.getString("name");
                final String factionName = resultSet.getString("faction_name");
                final float power = resultSet.getFloat("power");
                final float maxpower = resultSet.getFloat("max_power");
                final boolean deathInWarzone = resultSet.getBoolean("death_in_warzone");

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
        try(final Connection connection = this.sqlConnectionProvider.getConnection())
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
            final boolean didSucceed = statement.executeUpdate() >= 1;
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
        try(final Connection connection = this.sqlConnectionProvider.getConnection())
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
        try(final Connection connection = this.sqlConnectionProvider.getConnection())
        {
            final Statement statement = connection.createStatement();
            final ResultSet resultSet = statement.executeQuery(SELECT_PLAYER_NAMES);
            while (resultSet.next())
            {
                final String playerName = resultSet.getString("name");
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
        try(final Connection connection = this.sqlConnectionProvider.getConnection())
        {
            final Statement statement = connection.createStatement();
            final ResultSet resultSet = statement.executeQuery(SELECT_PLAYERS);
            while (resultSet.next())
            {
                final UUID playerUUID = UUID.fromString(resultSet.getString("player_uuid"));
                final String name = resultSet.getString("name");
                final String factionName = resultSet.getString("faction_name");
                final float power = resultSet.getFloat("power");
                final float maxpower = resultSet.getFloat("max_power");
                final boolean deathInWarzone = resultSet.getBoolean("death_in_warzone");

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
        try(final Connection connection = this.sqlConnectionProvider.getConnection())
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
