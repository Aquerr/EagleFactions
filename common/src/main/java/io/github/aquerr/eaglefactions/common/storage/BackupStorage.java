package io.github.aquerr.eaglefactions.common.storage;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.common.caching.FactionsCache;
import io.github.aquerr.eaglefactions.common.storage.file.hocon.ConfigurateHelper;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class BackupStorage
{
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("YYYY-MM-dd_hh-mm-ss");

    private final Path configPath;
    private final Path backupsPath;
    private final FactionStorage factionStorage;
    private final PlayerStorage playerStorage;

    public BackupStorage(final FactionStorage factionStorage, final PlayerStorage playerStorage, final Path configPath)
    {
        this.configPath = configPath;
        this.backupsPath = configPath.resolve("backups");
        this.factionStorage = factionStorage;
        this.playerStorage = playerStorage;
    }

    public boolean createBackup()
    {
        try
        {
            final Path backupPath = this.backupsPath.resolve("backup-" + DATE_TIME_FORMATTER.format(LocalDateTime.now()));
            createFileIfNotExists(this.backupsPath, true);
            createFileIfNotExists(backupPath, true);

            final Path factionsFile = backupPath.resolve("factions.conf");
            final Path playersDir = backupPath.resolve("players");
            createFileIfNotExists(factionsFile, false);
            createFileIfNotExists(playersDir, true);

            // Backup factions
            final HoconConfigurationLoader configurationLoader = HoconConfigurationLoader.builder().setPath(factionsFile).build();
            final ConfigurationNode rootNode = configurationLoader.createEmptyNode();
            final ConfigurationNode factionsNode = rootNode.getNode("factions");
            final Set<Faction> factions = factionStorage.getFactions();
            for (final Faction faction : factions)
            {
                ConfigurateHelper.putFactionInNode(factionsNode, faction);
            }
            configurationLoader.save(rootNode);

            // Backup players
            final Set<FactionPlayer> players = playerStorage.getServerPlayers();
            for (final FactionPlayer factionPlayer : players)
            {
                final UUID playerUniqueId = factionPlayer.getUniqueId();
                final Path playerFile = playersDir.resolve(playerUniqueId.toString() + ".conf");
                final HoconConfigurationLoader playerConfigLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();
                final ConfigurationNode playerNode = playerConfigLoader.createEmptyNode();
                ConfigurateHelper.putPlayerInNode(playerNode, factionPlayer);
                playerConfigLoader.save(playerNode);
            }

            // Now when factions and players are ready, we can move them into a zip file.
            FileOutputStream fileOutputStream = new FileOutputStream(backupPath.toAbsolutePath().toString() + ".zip");
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream);
            File file = backupPath.toFile();

            zipFile(file, file.getName(), zipOutputStream);
            bufferedOutputStream.flush();
            zipOutputStream.close();
            bufferedOutputStream.close();
            fileOutputStream.close();

            //Delete temp files
            deleteDirectory(backupPath.toFile());

            return true;
        }
        catch (Exception e)
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
                createFileIfNotExists(newFile.toPath(), true);
                zipEntry = zipInputStream.getNextEntry();
                continue;
            }

            FileOutputStream fileOutputStream = new FileOutputStream(newFile);
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
        final Path factionsFilePath = backupTempDirectory.resolve("factions.conf");
        final HoconConfigurationLoader factionsConfigLoader = HoconConfigurationLoader.builder().setPath(factionsFilePath).build();
        final List<Faction> factions = ConfigurateHelper.getFactionsFromNode(factionsConfigLoader.load().getNode("factions"));
        final List<FactionPlayer> players = new ArrayList<>();

        final Path playersDirPath = backupTempDirectory.resolve("players");
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
        deleteDirectory(backupTempDirectory.toFile());
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

    private void createFileIfNotExists(final Path path, final boolean directory) throws IOException
    {
        if (Files.notExists(path))
        {
            if (directory)
            {
                Files.createDirectory(path);
            }
            else
            {
                Files.createFile(path);
            }
        }
    }

    private boolean deleteDirectory(final File directory)
    {
        File[] children = directory.listFiles();
        if (children != null)
        {
            for (final File file : children)
            {
                deleteDirectory(file);
            }
        }
        return directory.delete();
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

    private String getFileNameWithoutExtension(final String fileName)
    {
        final int index = fileName.lastIndexOf(".");
        return fileName.substring(0, index);
    }
}
