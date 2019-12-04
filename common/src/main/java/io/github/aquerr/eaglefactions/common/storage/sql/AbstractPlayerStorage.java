package io.github.aquerr.eaglefactions.common.storage.sql;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.common.entities.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.common.storage.IPlayerStorage;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class AbstractPlayerStorage implements IPlayerStorage
{
    private static final String INSERT_PLAYER = "INSERT INTO Players (PlayerUUID, Name, Power, MaxPower, DeathInWarzone) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_PLAYER = "UPDATE Players SET PlayerUUID = ?, Name = ?, Power = ?, MaxPower = ?, DeathInWarzone = ? WHERE PlayerUUID = ?";
    private static final String MERGE_PLAYER = "MERGE INTO Players (PlayerUUID, Name, Power, MaxPower, DeathInWarzone) KEY (PlayerUUID) VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_PLAYER_WHERE_PLAYERUUID = "SELECT Name FROM Players WHERE PlayerUUID=? LIMIT 1";
    private static final String SELECT_PLAYERS = "SELECT * FROM Players";
    private static final String SELECT_PLAYER_NAMES = "SELECT Name FROM Players";
    private static final String SELECT_DEATH_IN_WARZONE_WHERE_PLAYERUUID = "SELECT DeathInWarzone FROM Players WHERE PlayerUUID=? LIMIT 1";
    private static final String SELECT_PLAYER_WHERE_PLAYERUUID_AND_PLAYERNAME = "SELECT * FROM Players WHERE PlayerUUID=? AND Name=? LIMIT 1";
    private static final String SELECT_POWER_WHERE_PLAYERUUID = "SELECT Power FROM Players WHERE PlayerUUID=? LIMIT 1";
    private static final String SELECT_MAXPOWER_WHERE_PLAYERUUID = "SELECT MaxPower FROM Players WHERE PlayerUUID=? LIMIT 1";

    private static final String UPDATE_POWER_WHERE_PLAYERUUID = "UPDATE Players SET Power=? WHERE PlayerUUID=?";
    private static final String UPDATE_MAXPOWER_WHERE_PLAYERUUID = "UPDATE Players SET MaxPower=? WHERE PlayerUUID=?";
    private static final String UPDATE_DEATH_IN_WARZONE_WHERE_PLAYERUUID = "UPDATE Players SET DeathInWarzone=? WHERE PlayerUUID=?";
    private static final String UPDATE_PLAYERNAME_WHERE_PLAYERUUID = "UPDATE Players SET Name=? WHERE PlayerUUID=?";

    private final EagleFactions plugin;
    private final SQLProvider sqlProvider;

    protected AbstractPlayerStorage(final EagleFactions plugin, final SQLProvider sqlProvider)
    {
        if(sqlProvider == null) {
            Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.RED, "Could not establish connection to the database. Aborting..."));
            Sponge.getServer().shutdown();
        }
        this.plugin = plugin;
        this.sqlProvider = sqlProvider;
    }

    @Override
    public boolean checkIfPlayerExists(final UUID playerUUID, final String playerName)
    {
        try(final Connection connection = this.sqlProvider.getConnection())
        {
            final PreparedStatement statement = connection.prepareStatement(SELECT_PLAYER_WHERE_PLAYERUUID_AND_PLAYERNAME);
            statement.setString(1, playerUUID.toString());
            statement.setString(2, playerName);
            final ResultSet resultSet = statement.executeQuery();
            final boolean exists = resultSet.next();
            resultSet.close();
            statement.close();
            return exists;
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean addPlayer(final UUID playerUUID, final String playerName, final float startingPower, final float maxPower)
    {
        try(final Connection connection = this.sqlProvider.getConnection())
        {
            final PreparedStatement statement = connection.prepareStatement(INSERT_PLAYER);
            statement.setString(1, playerUUID.toString());
            statement.setString(2, playerName);
            statement.setFloat(3, startingPower);
            statement.setFloat(4, maxPower);
            statement.setBoolean(5, false);
            final boolean didSucceed = statement.execute();
            statement.close();
            return didSucceed;
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean setDeathInWarzone(final UUID playerUUID, final boolean didDieInWarZone)
    {
        try(final Connection connection = this.sqlProvider.getConnection())
        {
            final PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_DEATH_IN_WARZONE_WHERE_PLAYERUUID);
            preparedStatement.setBoolean(1, didDieInWarZone);
            preparedStatement.setString(2, playerUUID.toString());
            final boolean didSucceed = preparedStatement.execute();
            preparedStatement.close();
            return didSucceed;
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean getLastDeathInWarzone(final UUID playerUUID)
    {
        try(final Connection connection = this.sqlProvider.getConnection())
        {
            final PreparedStatement preparedStatement = connection.prepareStatement(SELECT_DEATH_IN_WARZONE_WHERE_PLAYERUUID);
            preparedStatement.setString(1, playerUUID.toString());
            final ResultSet resultSet = preparedStatement.executeQuery();
            boolean lastDeathInWarzone = false;
            if (resultSet.next())
            {
                lastDeathInWarzone = resultSet.getBoolean("DeathInWarzone");
            }
            resultSet.close();
            preparedStatement.close();
            return lastDeathInWarzone;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public float getPlayerPower(final UUID playerUUID)
    {
        try(final Connection connection = this.sqlProvider.getConnection())
        {
            final PreparedStatement preparedStatement = connection.prepareStatement(SELECT_POWER_WHERE_PLAYERUUID);
            preparedStatement.setString(1, playerUUID.toString());
            final ResultSet resultSet = preparedStatement.executeQuery();
            float power = 0;
            if (resultSet.next())
            {
                power = resultSet.getFloat("Power");
            }
            resultSet.close();
            preparedStatement.close();
            return power;
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean setPlayerPower(final UUID playerUUID, final float power)
    {
        try(final Connection connection = this.sqlProvider.getConnection())
        {
            final PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_POWER_WHERE_PLAYERUUID);
            preparedStatement.setFloat(1, power);
            preparedStatement.setString(2, playerUUID.toString());
            final boolean didSucceed = preparedStatement.execute();
            preparedStatement.close();
            return didSucceed;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public float getPlayerMaxPower(final UUID playerUUID)
    {
        try(final Connection connection = this.sqlProvider.getConnection())
        {
            final PreparedStatement preparedStatement = connection.prepareStatement(SELECT_MAXPOWER_WHERE_PLAYERUUID);
            preparedStatement.setString(1, playerUUID.toString());
            final ResultSet resultSet = preparedStatement.executeQuery();
            float power = 0;
            if (resultSet.next())
            {
                power = resultSet.getFloat("MaxPower");
            }
            resultSet.close();
            preparedStatement.close();
            return power;
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean setPlayerMaxPower(final UUID playerUUID, final float maxpower)
    {
        try(final Connection connection = this.sqlProvider.getConnection())
        {
            final PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_MAXPOWER_WHERE_PLAYERUUID);
            preparedStatement.setFloat(1, maxpower);
            preparedStatement.setString(2, playerUUID.toString());
            final boolean didSucceed = preparedStatement.execute();
            preparedStatement.close();
            return didSucceed;
        }
        catch (SQLException e)
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
                final UUID playerUUID = resultSet.getObject("PlayerUUID", UUID.class);
                final String name = resultSet.getString("Name");
                final float power = resultSet.getInt("Power");
                final float maxpower = resultSet.getInt("Maxpower");
                final FactionPlayer factionPlayer = new FactionPlayerImpl(name, playerUUID, null, null, power, maxpower);
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
    public String getPlayerName(final UUID playerUUID)
    {
        try(final Connection connection = this.sqlProvider.getConnection())
        {
            final PreparedStatement preparedStatement = connection.prepareStatement(SELECT_PLAYER_WHERE_PLAYERUUID);
            preparedStatement.setString(1, playerUUID.toString());
            final ResultSet resultSet = preparedStatement.executeQuery();
            String playerName = "";
            while (resultSet.next())
            {
                playerName = resultSet.getString("Name");
            }
            resultSet.close();
            preparedStatement.close();
            return playerName;
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public boolean updatePlayerName(final UUID playerUUID, final String playerName)
    {
        try(final Connection connection = this.sqlProvider.getConnection())
        {
            final PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_PLAYERNAME_WHERE_PLAYERUUID);
            preparedStatement.setString(1, playerName);
            preparedStatement.setString(2, playerUUID.toString());
            final boolean didSucceed = preparedStatement.execute();
            preparedStatement.close();
            return didSucceed;
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }
}
