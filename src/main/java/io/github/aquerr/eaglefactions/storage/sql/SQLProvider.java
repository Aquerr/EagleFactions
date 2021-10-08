package io.github.aquerr.eaglefactions.storage.sql;

import io.github.aquerr.eaglefactions.storage.StorageType;

import java.sql.Connection;
import java.sql.SQLException;

public interface SQLProvider
{
    Connection getConnection() throws SQLException;

    StorageType getStorageType();
}
