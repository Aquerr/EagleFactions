package io.github.aquerr.eaglefactions.common.storage.sql;

import java.sql.Connection;
import java.sql.SQLException;

public interface SQLProvider
{
    Connection getConnection() throws SQLException;

    String getProviderName();
}
