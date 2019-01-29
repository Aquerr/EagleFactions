package io.github.aquerr.eaglefactions.storage.h2;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.entities.IFactionPlayer;
import io.github.aquerr.eaglefactions.storage.IPlayerStorage;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class H2PlayerStorage implements IPlayerStorage
{
    private static final String MERGE_PLAYER ="MERGE INTO Players (PlayerUUID, Name, Power, Maxpower, DeathInWarzone) KEY (PlayerUUID) VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_PLAYER_WHERE_PLAYERUUID = "SELECT Name FROM Players WHERE PlayerUUID=? LIMIT 1";
    private static final String SELECT_PLAYERS = "SELECT * FROM Players";
    private static final String SELECT_PLAYERNAMES = "SELECT Name FROM Players";
    private static final String SELECT_DEATH_IN_WARZONE_WHERE_PLAYERUUID = "SELECT DeathInWarzone FROM Players WHERE PlayerUUID=? LIMIT 1";
    private static final String SELECT_PLAYER_WHERE_PLAYERUUID_AND_PLAYERNAME = "SELECT * FROM Players WHERE PlayerUUID=? AND Name=? LIMIT 1";
    private static final String SELECT_POWER_WHERE_PLAYERUUID = "SELECT Power FROM Players WHERE PlayerUUID=? LIMIT 1";
    private static final String SELECT_MAXPOWER_WHERE_PLAYERUUID = "SELECT MaxPower FROM Players WHERE PlayerUUID=? LIMIT 1";

    private static final String UPDATE_POWER_WHERE_PLAYERUUID = "UPDATE Players SET Power=? WHERE PlayerUUID=?";
    private static final String UPDATE_MAXPOWER_WHERE_PLAYERUUID = "UPDATE Players SET MaxPower=? WHERE PlayerUUID=?";
    private static final String UPDATE_DEATH_IN_WARZONE_WHERE_PLAYERUUID = "UPDATE Players SET DeathInWarzone=? WHERE PlayerUUID=?";
    private static final String UPDATE_PLAYERNAME_WHERE_PLAYERUUID = "UPDATE Players SET Name=? WHERE PlayerUUID=?";

    private final H2Provider h2provider;

    public H2PlayerStorage(final EagleFactions eagleFactions)
    {
        this.h2provider = H2Provider.getInstance(eagleFactions);
    }

    @Override
    public boolean checkIfPlayerExists(final UUID playerUUID, final String playerName)
    {
        try(Connection connection = this.h2provider.getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(SELECT_PLAYER_WHERE_PLAYERUUID_AND_PLAYERNAME);
            statement.setObject(1, playerUUID);
            statement.setString(2, playerName);
            ResultSet resultSet = statement.executeQuery();
            boolean exists = resultSet.next();
            resultSet.close();
            statement.close();
            return exists;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean addPlayer(final UUID playerUUID, final String playerName, final float startingPower, final float maxPower)
    {
        try(Connection connection = this.h2provider.getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(MERGE_PLAYER);
            statement.setObject(1, playerUUID);
            statement.setString(2, playerName);
            statement.setFloat(3, startingPower);
            statement.setFloat(4, maxPower);
            statement.setBoolean(5, false);
            boolean didSucceed = statement.execute();
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
    public boolean setDeathInWarzone(final UUID playerUUID, final boolean didDieInWarZone)
    {
        try(Connection connection = this.h2provider.getConnection())
        {
            PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_DEATH_IN_WARZONE_WHERE_PLAYERUUID);
            preparedStatement.setBoolean(1, didDieInWarZone);
            preparedStatement.setObject(2, playerUUID);
            boolean didSucceed = preparedStatement.execute();
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
    public boolean getLastDeathInWarzone(final UUID playerUUID)
    {
        try(Connection connection = this.h2provider.getConnection())
        {
            PreparedStatement preparedStatement = connection.prepareStatement(SELECT_DEATH_IN_WARZONE_WHERE_PLAYERUUID);
            preparedStatement.setObject(1, playerUUID);
            ResultSet resultSet = preparedStatement.executeQuery();
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
        try(Connection connection = this.h2provider.getConnection())
        {
            PreparedStatement preparedStatement = connection.prepareStatement(SELECT_POWER_WHERE_PLAYERUUID);
            preparedStatement.setObject(1, playerUUID);
            ResultSet resultSet = preparedStatement.executeQuery();
            float power = 0;
            if (resultSet.next())
            {
                power = resultSet.getFloat("Power");
            }
            resultSet.close();
            preparedStatement.close();
            return power;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean setPlayerPower(final UUID playerUUID, final float power)
    {
        try(Connection connection = this.h2provider.getConnection())
        {
            PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_POWER_WHERE_PLAYERUUID);
            preparedStatement.setFloat(1, power);
            preparedStatement.setObject(2, playerUUID);
            boolean didSucceed = preparedStatement.execute();
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
        try(Connection connection = this.h2provider.getConnection())
        {
            PreparedStatement preparedStatement = connection.prepareStatement(SELECT_MAXPOWER_WHERE_PLAYERUUID);
            preparedStatement.setObject(1, playerUUID);
            ResultSet resultSet = preparedStatement.executeQuery();
            float power = 0;
            if (resultSet.next())
            {
                power = resultSet.getFloat("MaxPower");
            }
            resultSet.close();
            preparedStatement.close();
            return power;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean setPlayerMaxPower(final UUID playerUUID, final float maxpower)
    {
        try(Connection connection = this.h2provider.getConnection())
        {
            PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_MAXPOWER_WHERE_PLAYERUUID);
            preparedStatement.setFloat(1, maxpower);
            preparedStatement.setObject(2, playerUUID);
            boolean didSucceed = preparedStatement.execute();
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
        Set<String> playerNames = new HashSet<>();
        try(Connection connection = this.h2provider.getConnection())
        {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(SELECT_PLAYERNAMES);
            while (resultSet.next())
            {
                String playerName = resultSet.getString("Name");
                playerNames.add(playerName);
            }
            resultSet.close();
            statement.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return playerNames;
    }

    @Override
    public Set<IFactionPlayer> getServerPlayers()
    {
        Set<IFactionPlayer> factionPlayers = new HashSet<>();
        try(Connection connection = this.h2provider.getConnection())
        {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(SELECT_PLAYERS);
            while (resultSet.next())
            {
                UUID playerUUID = resultSet.getObject("PlayerUUID", UUID.class);
                String name = resultSet.getString("Name");
                float power = resultSet.getInt("Power");
                float maxpower = resultSet.getInt("Maxpower");
                IFactionPlayer factionPlayer = new FactionPlayer(name, playerUUID, null, null, power, maxpower);
                factionPlayers.add(factionPlayer);
            }
            resultSet.close();
            statement.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return factionPlayers;
    }

    @Override
    public String getPlayerName(final UUID playerUUID)
    {
        try(Connection connection = this.h2provider.getConnection())
        {
            PreparedStatement preparedStatement = connection.prepareStatement(SELECT_PLAYER_WHERE_PLAYERUUID);
            preparedStatement.setObject(1, playerUUID);
            ResultSet resultSet = preparedStatement.executeQuery();
            String playerName = "";
            while (resultSet.next())
            {
                playerName = resultSet.getString("Name");
            }
            resultSet.close();
            preparedStatement.close();
            return playerName;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public boolean updatePlayerName(final UUID playerUUID, final String playerName)
    {
        try(Connection connection = this.h2provider.getConnection())
        {
            PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_PLAYERNAME_WHERE_PLAYERUUID);
            preparedStatement.setString(1, playerName);
            preparedStatement.setObject(2, playerUUID);
            boolean didSucceed = preparedStatement.execute();
            preparedStatement.close();
            return didSucceed;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }
}
