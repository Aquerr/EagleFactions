package io.github.aquerr.eaglefactions.common.storage.sql;

import io.github.aquerr.eaglefactions.common.storage.StorageType;

import java.sql.Connection;
import java.sql.SQLException;

public interface SQLProvider
{
    Connection getConnection() throws SQLException;

    StorageType getStorageType();
}
