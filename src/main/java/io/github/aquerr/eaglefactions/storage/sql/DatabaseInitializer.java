package io.github.aquerr.eaglefactions.storage.sql;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.storage.StorageType;
import io.github.aquerr.eaglefactions.storage.sql.h2.H2ConnectionProvider;
import io.github.aquerr.eaglefactions.storage.sql.mariadb.MariaDbConnectionProvider;
import io.github.aquerr.eaglefactions.storage.sql.mysql.MySQLConnectionProvider;
import io.github.aquerr.eaglefactions.storage.sql.sqlite.SqliteConnectionProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class DatabaseInitializer
{
    public static void initialize(EagleFactions eagleFactions,
                                  SQLConnectionProvider connectionProvider) throws IOException, SQLException
    {
        if (connectionProvider.getStorageType() != StorageType.SQLITE && !databaseExists(connectionProvider))
        {
            createDatabase(connectionProvider);
        }

        updateDatabase(eagleFactions, connectionProvider);
    }

    private static void updateDatabase(EagleFactions eagleFactions,
                                       SQLConnectionProvider connectionProvider) throws IOException, SQLException
    {
        final int currentDatabaseVersion = getDatabaseVersion(connectionProvider);

        //Get all .sql files
        String sqlScriptsLocation = "/assets/eaglefactions/queries/" + connectionProvider.getStorageType().getName().toLowerCase();
        final List<Path> sqlFilesPaths = getSqlFilesPaths(eagleFactions, sqlScriptsLocation);

        if (sqlFilesPaths.isEmpty())
        {
            //TODO: Use logger here...
            System.out.println("There may be a problem with database script files...");
            System.out.println("Searched for them in: " + connectionProvider.getStorageType().getName());
            throw new IllegalStateException("Could not find any database script files in " + sqlScriptsLocation);
        }

        for(final Path resourceFilePath : sqlFilesPaths)
        {
            final int scriptNumber = Integer.parseInt(resourceFilePath.getFileName().toString().substring(0, 3));
            if(scriptNumber <= currentDatabaseVersion)
                continue;

            try(final InputStream inputStream = Files.newInputStream(resourceFilePath, StandardOpenOption.READ);
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                final Connection connection = connectionProvider.getConnection();
                final Statement statement = connection.createStatement()
            )
            {
                connection.setAutoCommit(false);

                final StringBuilder stringBuilder = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null)
                {
                    if(line.startsWith("--"))
                        continue;

                    stringBuilder.append(" ").append(line);

                    if(line.endsWith(";"))
                    {
                        statement.addBatch(stringBuilder.toString().trim());
                        stringBuilder.setLength(0);
                    }
                }
                statement.executeBatch();
                connection.commit();
            }
            catch(Exception exception)
            {
                throw new IllegalStateException("There may be a problem with database script files...", exception);
            }
        }
    }

    private static boolean databaseExists(SQLConnectionProvider connectionProvider) throws SQLException
    {
        try(final Connection connection = connectionProvider.getConnection(); final ResultSet resultSet = connection.getMetaData().getCatalogs())
        {
            while(resultSet.next())
            {
                if(resultSet.getString(1).equalsIgnoreCase(connectionProvider.getDatabaseName()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<Path> getSqlFilesPaths(EagleFactions eagleFactions,
                                               String sqlScriptsLocation) throws IOException
    {
        final List<Path> filePaths = new ArrayList<>();
        final URI uri = eagleFactions.getResource(sqlScriptsLocation);

        if (uri == null)
            throw new IOException("Script directory missing for storage type = " + sqlScriptsLocation);

        Path myPath;
        if (uri.getScheme().equals("jar"))
        {
            try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap()))
            {
                myPath = fileSystem.getPath(sqlScriptsLocation);
            }
        }
        else
        {
            myPath = Paths.get(uri);
        }

        try (Stream<Path> walk = Files.walk(myPath, 1))
        {
            boolean skipFirst = true;
            for (final Iterator<Path> it = walk.iterator(); it.hasNext(); )
            {
                if (skipFirst)
                {
                    it.next();
                    skipFirst = false;
                }

                final Path zipPath = it.next();
                filePaths.add(zipPath);
            }
        }

        //Sort .sql files
        filePaths.sort(Comparator.comparing(x -> x.getFileName().toString()));
        return filePaths;
    }

    public static int getDatabaseVersion(SQLConnectionProvider connectionProvider) throws SQLException
    {
        try(final Connection connection = connectionProvider.getConnection())
        {
            PreparedStatement preparedStatement = null;
            if(connectionProvider instanceof SqliteConnectionProvider)
            {
                preparedStatement = connection.prepareStatement("SELECT * FROM sqlite_master WHERE type='table' AND name='version'");
            }
            else if(connectionProvider instanceof H2ConnectionProvider)
            {
                preparedStatement = connection.prepareStatement("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'VERSION'");
            }
            else if(connectionProvider instanceof MySQLConnectionProvider || connectionProvider instanceof MariaDbConnectionProvider)
            {
                preparedStatement = connection.prepareStatement("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_NAME = 'version'");
                preparedStatement.setString(1, connectionProvider.getDatabaseName());
            }

            final ResultSet resultSet = preparedStatement.executeQuery();
            boolean versionTableExists = false;
            while(resultSet.next())
            {
                versionTableExists = true;
            }
            resultSet.close();

            if(versionTableExists)
            {
                final Statement statement = connection.createStatement();
                final ResultSet resultSet1 = statement.executeQuery("SELECT MAX(version) FROM version");
                if(resultSet1.next())
                {
                    int version = resultSet1.getInt(1);
                    resultSet1.close();
                    statement.close();
                    preparedStatement.close();
                    return version;
                }
                statement.close();
            }
            preparedStatement.close();
        }
        return 0;
    }

    private static void createDatabase(SQLConnectionProvider connectionProvider) throws SQLException
    {
        try(final Connection connection = connectionProvider.getConnection(); final Statement statement = connection.createStatement())
        {
            statement.execute("CREATE SCHEMA " + connectionProvider.getDatabaseName() + ";");
        }
    }
}
