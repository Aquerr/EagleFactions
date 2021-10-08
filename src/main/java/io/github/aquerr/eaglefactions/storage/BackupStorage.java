package io.github.aquerr.eaglefactions.storage;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.storage.file.hocon.ConfigurateHelper;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static io.github.aquerr.eaglefactions.util.FileUtils.createDirectoryIfNotExists;
import static io.github.aquerr.eaglefactions.util.FileUtils.deleteDirectoryRecursive;
import static io.github.aquerr.eaglefactions.util.FileUtils.getFileNameWithoutExtension;

public class BackupStorage
{
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_hh-mm-ss");

    private final Path backupsPath;
    private final FactionStorage factionStorage;
    private final PlayerStorage playerStorage;

    public BackupStorage(final FactionStorage factionStorage, final PlayerStorage playerStorage, final Path configPath)
    {
        this.backupsPath = configPath.resolve("backups");
        this.factionStorage = factionStorage;
        this.playerStorage = playerStorage;
    }

    public boolean createBackup()
    {
        try
        {
            final Path backupDirPath = this.backupsPath.resolve("backup-" + DATE_TIME_FORMATTER.format(LocalDateTime.now()));
            createDirectoryIfNotExists(this.backupsPath);
            createDirectoryIfNotExists(backupDirPath);

            final Path factionsDir = backupDirPath.resolve("factions");
            final Path playersDir = backupDirPath.resolve("players");
            createDirectoryIfNotExists(factionsDir);
            createDirectoryIfNotExists(playersDir);

            // Backup factions
            final Set<Faction> factions = factionStorage.getFactions();
            for (final Faction faction : factions)
            {
                final Path factionFilePath = factionsDir.resolve(faction.getName().toLowerCase() + ".conf");
//                createFileIfNotExists(factionFilePath, false);
                final HoconConfigurationLoader configurationLoader = HoconConfigurationLoader.builder().setDefaultOptions(ConfigurateHelper.getDefaultOptions()).setPath(factionFilePath).build();
                final ConfigurationNode configurationNode = configurationLoader.createEmptyNode();
                ConfigurateHelper.putFactionInNode(configurationNode, faction);
                configurationLoader.save(configurationNode);
            }

            // Backup players
            final Set<FactionPlayer> players = playerStorage.getServerPlayers();
            for (final FactionPlayer factionPlayer : players)
            {
                final Path playerFile = playersDir.resolve(factionPlayer.getUniqueId().toString() + ".conf");
//                createFileIfNotExists(playerFile, false);
                final HoconConfigurationLoader playerConfigLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();
                final ConfigurationNode playerNode = playerConfigLoader.createEmptyNode();
                ConfigurateHelper.putPlayerInNode(playerNode, factionPlayer);
                playerConfigLoader.save(playerNode);
            }

            // Now when factions and players are ready, we can move them into a zip file.
            FileOutputStream fileOutputStream = new FileOutputStream(backupDirPath.toAbsolutePath().toString() + ".zip");
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream);
            File file = backupDirPath.toFile();

            zipFile(file, file.getName(), zipOutputStream);
            bufferedOutputStream.flush();
            zipOutputStream.close();
            bufferedOutputStream.close();
            fileOutputStream.close();

            //Delete temp files
            deleteDirectoryRecursive(backupDirPath.toFile());

            return true;
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> listBackups()
    {
        final List<String> backupsNames = new ArrayList<>();
        final File backupDirectory = this.backupsPath.toFile();
        final File[] backupFiles = backupDirectory.listFiles();
        if (backupFiles == null)
            return backupsNames;
        for (final File file : backupFiles)
            if (file.getName().endsWith(".zip"))
                backupsNames.add(file.getName());
        return backupsNames;
    }

    public boolean restoreBackup(final String backupName) throws IOException
    {
        final Path backupPath = this.backupsPath.resolve(backupName);
        if (Files.notExists(backupPath))
            return false;

        // Unzip backup
        Path destPath = this.backupsPath;
        File destDir = destPath.toFile();
        byte[] buffer = new byte[1024];
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(backupPath.toFile()));
        ZipEntry zipEntry = zipInputStream.getNextEntry();
        while (zipEntry != null)
        {
            File newFile = newFile(destDir, zipEntry);
            if (zipEntry.isDirectory())
            {
                createDirectoryIfNotExists(newFile.toPath());
                zipEntry = zipInputStream.getNextEntry();
                continue;
            }

            final FileOutputStream fileOutputStream = new FileOutputStream(newFile);
            int len;
            while ((len = zipInputStream.read(buffer)) > 0)
            {
                fileOutputStream.write(buffer, 0, len);
            }
            fileOutputStream.close();
            zipEntry = zipInputStream.getNextEntry();
        }
        zipInputStream.closeEntry();
        zipInputStream.close();

        // Load data from backup
        final Path backupTempDirectory = destPath.resolve(getFileNameWithoutExtension(backupName));
        final Path factionsDirPath = backupTempDirectory.resolve("factions");
//        createFileIfNotExists(factionsDirPath, true);
        final File factionsDir = factionsDirPath.toFile();
        final File[] factionsFiles = factionsDir.listFiles();
        final List<Faction> factions = new ArrayList<>();
        if (factionsFiles != null)
        {
            for (final File file : factionsFiles)
            {
                final HoconConfigurationLoader hoconConfigurationLoader = HoconConfigurationLoader.builder().setDefaultOptions(ConfigurateHelper.getDefaultOptions()).setFile(file).build();
                final ConfigurationNode configurationNode = hoconConfigurationLoader.load();
                try
                {
                    final Faction faction = ConfigurateHelper.getFactionFromNode(configurationNode);
                    factions.add(faction);
                }
                catch (final ObjectMappingException e)
                {
                    e.printStackTrace();
                }
            }
        }

        final List<FactionPlayer> players = new ArrayList<>();
        final Path playersDirPath = backupTempDirectory.resolve("players");
//        createFileIfNotExists(playersDirPath, true);
        final File playersDir = playersDirPath.toFile();
        final File[] playerFiles = playersDir.listFiles();
        if (playerFiles != null)
        {
            for (final File file : playerFiles)
            {
                final FactionPlayer factionPlayer = ConfigurateHelper.getPlayerFromFile(file);
                if (factionPlayer != null)
                    players.add(factionPlayer);
            }
        }

        // Now when we have data we can put it in current storage.
        this.factionStorage.deleteFactions();
        this.playerStorage.deletePlayers();

        for (final Faction faction : factions)
        {
            this.factionStorage.saveFaction(faction);
            FactionsCache.saveFaction(faction);
        }

        this.playerStorage.savePlayers(players);

        // Remove temp files
        deleteDirectoryRecursive(backupTempDirectory.toFile());
        return true;
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    private void zipFile(final File file, String fileName, ZipOutputStream zipOutputStream) throws IOException
    {
        if (file.isHidden())
            return;
        if (file.isDirectory())
        {
            if (fileName.endsWith("/"))
            {
                zipOutputStream.putNextEntry(new ZipEntry(fileName));
                zipOutputStream.closeEntry();
            }
            else
            {
                zipOutputStream.putNextEntry(new ZipEntry(fileName + "/"));
                zipOutputStream.closeEntry();
            }
            File[] children = file.listFiles();
            for (final File childFile : children)
            {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOutputStream);
            }
            return;
        }
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOutputStream.putNextEntry(zipEntry);
        byte[] bytes = new byte[2048];
        int length;
        while ((length = bufferedInputStream.read(bytes)) >= 0)
        {
            zipOutputStream.write(bytes, 0, length);
        }
        bufferedInputStream.close();
        fileInputStream.close();
    }
}
