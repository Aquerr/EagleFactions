package io.github.aquerr.eaglefactions.storage.sql;

import java.sql.Connection;
import java.sql.SQLException;

public interface SqlProvider
{
    Connection getConnection() throws SQLException;

    String getProviderName();
}
