package io.github.aquerr.eaglefactions.storage.sql;

import java.nio.file.Path;

public class DatabaseProperties
{
    private Path databaseFileDirectory;
    private String databaseName;
    private String databaseUrl;
    private String username;
    private String password;

    public DatabaseProperties()
    {

    }

    public Path getDatabaseFileDirectory()
    {
        return databaseFileDirectory;
    }

    public void setDatabaseFileDirectory(Path databaseFileDirectory)
    {
        this.databaseFileDirectory = databaseFileDirectory;
    }

    public String getDatabaseName()
    {
        return databaseName;
    }

    public void setDatabaseName(String databaseName)
    {
        this.databaseName = databaseName;
    }

    public String getDatabaseUrl()
    {
        return databaseUrl;
    }

    public void setDatabaseUrl(String databaseUrl)
    {
        this.databaseUrl = databaseUrl;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
}
