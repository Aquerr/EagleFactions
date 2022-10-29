package io.github.aquerr.eaglefactions.storage.sql;

import io.github.aquerr.eaglefactions.api.entities.ProtectionFlag;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

public interface FactionProtectionFlagsStorage
{
    Set<ProtectionFlag> getProtectionFlags(Connection connection, String factionName) throws SQLException;

    void saveProtectionFlags(Connection connection, String factionName, Set<ProtectionFlag> protectionFlags) throws SQLException;

    void deleteProtectionFlags(Connection connection, String factionName) throws SQLException;
}
