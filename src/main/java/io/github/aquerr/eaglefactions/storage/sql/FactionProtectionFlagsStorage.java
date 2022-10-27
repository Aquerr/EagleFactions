package io.github.aquerr.eaglefactions.storage.sql;

import io.github.aquerr.eaglefactions.api.entities.ProtectionFlag;

import java.sql.SQLException;
import java.util.Set;

public interface FactionProtectionFlagsStorage
{
    Set<ProtectionFlag> getProtectionFlags(String factionName) throws SQLException;

    void saveProtectionFlags(String factionName, Set<ProtectionFlag> protectionFlags) throws SQLException;

    void deleteProtectionFlags(String factionName) throws SQLException;
}
