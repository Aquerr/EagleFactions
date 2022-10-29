package io.github.aquerr.eaglefactions.storage.sql.h2;

import io.github.aquerr.eaglefactions.api.entities.ProtectionFlag;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlagType;
import io.github.aquerr.eaglefactions.entities.ProtectionFlagImpl;
import io.github.aquerr.eaglefactions.storage.sql.FactionProtectionFlagsStorage;
import io.github.aquerr.eaglefactions.storage.sql.SQLProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class FactionProtectionFlagsH2StorageImpl implements FactionProtectionFlagsStorage
{
    private static final String SELECT_PROTECTION_FLAGS_FOR_FACTION = "SELECT * FROM faction_protection_flag " +
            "JOIN protection_flag_type ON faction_protection_flag.protection_flag_type_id = protection_flag_type.id" +
            " WHERE faction_name = ?";

    private static final String INSERT_PROTECTION_FLAGS_FOR_FACTION = "INSERT INTO faction_protection_flag " +
            "(faction_name, protection_flag_type_id, flag_value) VALUES " +
            "(?, (SELECT id FROM protection_flag_type WHERE flag_type = ?), ?)";

    private static final String DELETE_ALL_PROTECTION_FLAGS_FOR_FACTION = "DELETE FROM faction_protection_flag WHERE faction_name = ?";
    private static final String DELETE_PROTECTION_FLAGS_FOR_FACTION_AND_PROTECTION_FLAG = "DELETE FROM faction_protection_flag " +
            "WHERE faction_name = ? AND protection_flag_type_id = (SELECT id FROM protection_flag_type WHERE flag_type = ?)";
    private static final String UPDATE_PROTECTION_FLAGS_FOR_FACTION = "UPDATE faction_protection_flag SET flag_value = ? " +
            "WHERE faction_name = ?";

    FactionProtectionFlagsH2StorageImpl()
    {

    }

    @Override
    public Set<ProtectionFlag> getProtectionFlags(Connection connection, String factionName) throws SQLException
    {
        PreparedStatement preparedStatement = connection.prepareStatement(SELECT_PROTECTION_FLAGS_FOR_FACTION);
        preparedStatement.setString(1, factionName);
        ResultSet resultSet = preparedStatement.executeQuery();
        Set<ProtectionFlag> protectionFlags = new HashSet<>();
        while (resultSet.next())
        {
            boolean flagValue = resultSet.getBoolean("flag_value");
            ProtectionFlagType flagType = ProtectionFlagType.getProtectionFlagTypeForName(resultSet.getString("flag_type"))
                    .orElse(null);
            if (flagType == null)
                continue;

            ProtectionFlagImpl protectionFlag = new ProtectionFlagImpl(flagType, flagValue);
            protectionFlags.add(protectionFlag);
        }
        resultSet.close();
        preparedStatement.close();
        return protectionFlags;
    }

    @Override
    public void saveProtectionFlags(Connection connection, String factionName, Set<ProtectionFlag> newProtectionFlags) throws SQLException
    {
        Set<ProtectionFlag> existingProtectionFlags = getProtectionFlags(connection, factionName);
        Set<ProtectionFlag> flagsToInsert = newProtectionFlags.stream()
                .filter(newProtectionFlag -> existingProtectionFlags.stream()
                        .noneMatch(flag -> flag.getType().equals(newProtectionFlag.getType())))
                .collect(Collectors.toSet());
        Set<ProtectionFlag> flagsToUpdate = newProtectionFlags.stream()
                .filter(newProtectionFlag -> existingProtectionFlags.stream().anyMatch(flag -> flag.getType().equals(newProtectionFlag.getType())))
                .collect(Collectors.toSet());
        Set<ProtectionFlag> flagsToDelete = existingProtectionFlags.stream()
                .filter(existingFlag -> newProtectionFlags.stream().noneMatch(flag -> flag.getType().equals(existingFlag.getType())))
                .collect(Collectors.toSet());

        // Delete
        deleteProtectionFlags(connection, factionName, flagsToDelete);
        // Update
        updateProtectionFlags(connection, factionName, flagsToUpdate);
        // Insert
        insertProtectionFlags(connection, factionName, flagsToInsert);
    }

    @Override
    public void deleteProtectionFlags(Connection connection, String factionName) throws SQLException
    {
        PreparedStatement preparedStatement = connection.prepareStatement(DELETE_ALL_PROTECTION_FLAGS_FOR_FACTION);
        preparedStatement.setString(1, factionName);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    private void deleteProtectionFlags(Connection connection, String factionName, Set<ProtectionFlag> protectionFlags) throws SQLException
    {
        PreparedStatement preparedStatement = connection.prepareStatement(DELETE_PROTECTION_FLAGS_FOR_FACTION_AND_PROTECTION_FLAG);
        for (ProtectionFlag protectionFlag : protectionFlags)
        {
            preparedStatement.setString(1, factionName);
            preparedStatement.setString(2, protectionFlag.getType().getName());
            preparedStatement.addBatch();
            preparedStatement.clearParameters();
        }
        preparedStatement.executeBatch();
        preparedStatement.close();
    }

    private void updateProtectionFlags(Connection connection, String factionName, Set<ProtectionFlag> protectionFlags) throws SQLException
    {
        PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_PROTECTION_FLAGS_FOR_FACTION);
        for (ProtectionFlag protectionFlag : protectionFlags)
        {
            preparedStatement.setString(1, factionName);
            preparedStatement.setString(2, protectionFlag.getType().getName());
            preparedStatement.addBatch();
            preparedStatement.clearParameters();
        }
        preparedStatement.executeBatch();
        preparedStatement.close();
    }

    private void insertProtectionFlags(Connection connection, String factionName, Set<ProtectionFlag> protectionFlags) throws SQLException
    {
        PreparedStatement preparedStatement = connection.prepareStatement(INSERT_PROTECTION_FLAGS_FOR_FACTION);
        for (ProtectionFlag protectionFlag : protectionFlags)
        {
            preparedStatement.setString(1, factionName);
            preparedStatement.setString(2, protectionFlag.getType().getName());
            preparedStatement.addBatch();
            preparedStatement.clearParameters();
        }
        preparedStatement.executeBatch();
        preparedStatement.close();
    }
}
