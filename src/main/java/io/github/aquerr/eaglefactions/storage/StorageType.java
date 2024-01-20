package io.github.aquerr.eaglefactions.storage;

import java.util.Arrays;
import java.util.Optional;

public enum StorageType
{
    HOCON("hocon", true, false),
    H2("h2", true, true),
    SQLITE("sqlite", true, true),
    MYSQL("mysql", false, true),
    MARIADB("mariadb", false, true);

    private final String name;
    private final boolean file;
    private final boolean sql;

    StorageType(String name, boolean file, boolean sql)
    {
        this.name = name.toLowerCase();
        this.file = file;
        this.sql = sql;
    }

    public String getName()
    {
        return name;
    }

    public boolean isSql()
    {
        return sql;
    }

    public boolean isFile()
    {
        return file;
    }

    public static Optional<StorageType> findByName(String name)
    {
        return Optional.ofNullable(name)
                .flatMap(name1 -> Arrays.stream(values())
                .filter(storageType -> storageType.getName().equalsIgnoreCase(name1))
                .findFirst());
    }
}
