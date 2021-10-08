package io.github.aquerr.eaglefactions.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileUtils
{
    public static void createFileIfNotExists(final Path path) throws IOException
    {
        if (Files.notExists(path))
        {
            Files.createFile(path);
        }
    }

    public static void createDirectoryIfNotExists(final Path path) throws IOException
    {
        if (Files.notExists(path))
        {
            Files.createDirectory(path);
        }
    }


    public static boolean deleteDirectoryRecursive(final File directory)
    {
        File[] children = directory.listFiles();
        if (children != null)
        {
            for (final File file : children)
            {
                deleteDirectoryRecursive(file);
            }
        }
        return directory.delete();
    }

    public static String getFileNameWithoutExtension(final String fileName)
    {
        final int index = fileName.lastIndexOf(".");
        return fileName.substring(0, index);
    }

    private FileUtils()
    {

    }
}
