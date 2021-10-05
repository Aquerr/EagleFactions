package io.github.aquerr.eaglefactions.common.storage;

import java.util.Arrays;
import java.util.Optional;

public enum StorageType
{
    HOCON("hocon"),
    H2("h2"),
    SQLITE("sqlite"),
    MYSQL("mysql"),
    MARIADB("mariadb");

    private final String name;

    StorageType(String name)
    {
        this.name = name.toLowerCase();
    }

    public String getName()
    {
        return name;
    }

    public static Optional<StorageType> findByName(String name)
    {
        return Optional.ofNullable(name)
                .flatMap(name1 -> Arrays.stream(values())
                .filter(storageType -> storageType.getName().equalsIgnoreCase(name1))
                .findFirst());
    }
}
