package io.github.aquerr.eaglefactions.storage.sql;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.*;
import io.github.aquerr.eaglefactions.entities.FactionChestImpl;
import io.github.aquerr.eaglefactions.entities.FactionImpl;
import io.github.aquerr.eaglefactions.storage.FactionStorage;
import io.github.aquerr.eaglefactions.storage.serializers.ClaimTypeSerializer;
import io.github.aquerr.eaglefactions.storage.serializers.InventorySerializer;
import io.github.aquerr.eaglefactions.storage.sql.h2.H2Provider;
import io.github.aquerr.eaglefactions.storage.sql.mariadb.MariaDbProvider;
import io.github.aquerr.eaglefactions.storage.sql.mysql.MySQLProvider;
import io.github.aquerr.eaglefactions.storage.sql.sqlite.SqliteProvider;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.item.inventory.ContainerTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.math.vector.Vector3i;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.kyori.adventure.text.format.NamedTextColor.RED;

public abstract class AbstractFactionStorage implements FactionStorage
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFactionStorage.class);

    private static final UUID DUMMY_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private static final String SELECT_FACTION_NAMES = "SELECT Name FROM Factions";
    private static final String SELECT_RECRUITS_WHERE_FACTIONNAME = "SELECT RecruitUUID FROM FactionRecruits WHERE FactionName=?";
    private static final String SELECT_OFFICERS_WHERE_FACTIONNAME = "SELECT OfficerUUID FROM FactionOfficers WHERE FactionName=?";
    private static final String SELECT_MEMBERS_WHERE_FACTIONNAME = "SELECT MemberUUID FROM FactionMembers WHERE FactionName=?";
    private static final String SELECT_CLAIMS_WHERE_FACTIONNAME = "SELECT * FROM Claims WHERE FactionName=?";
    private static final String SELECT_CLAIM_OWNERS_WHERE_WORLD_AND_CHUNK = "SELECT * FROM ClaimOwners WHERE WorldUUID=? AND ChunkPosition=?";
    private static final String SELECT_CHEST_WHERE_FACTIONNAME = "SELECT ChestItems FROM FactionChests WHERE FactionName=?";
    private static final String SELECT_OFFICER_PERMS_WHERE_FACTIONNAME = "SELECT * FROM OfficerPerms WHERE FactionName=?";
    private static final String SELECT_MEMBER_PERMS_WHERE_FACTIONNAME = "SELECT * FROM MemberPerms WHERE FactionName=?";
    private static final String SELECT_RECRUIT_PERMS_WHERE_FACTIONNAME = "SELECT * FROM RecruitPerms WHERE FactionName=?";
    private static final String SELECT_TRUCE_PERMS_WHERE_FACTIONNAME = "SELECT * FROM TrucePerms WHERE FactionName=?";
    private static final String SELECT_ALLY_PERMS_WHERE_FACTIONNAME = "SELECT * FROM AllyPerms WHERE FactionName=?";
    private static final String SELECT_FACTION_WHERE_FACTIONNAME = "SELECT * FROM Factions WHERE Name=?";
    private static final String SELECT_FACTION_ALLIANCES = "SELECT * FROM FactionAlliances WHERE FactionName_1=? OR FactionName_2=?";
    private static final String SELECT_FACTION_ENEMIES = "SELECT * FROM FactionEnemies WHERE FactionName_1=? OR FactionName_2=?";
    private static final String SELECT_FACTION_TRUCES = "SELECT * FROM FactionTruces WHERE FactionName_1=? OR FactionName_2=?";

    private static final String INSERT_FACTION = "INSERT INTO Factions (Name, Tag, TagColor, Leader, Home, LastOnline, Description, Motd, IsPublic) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_CLAIM = "INSERT INTO Claims (FactionName, WorldUUID, ChunkPosition, IsAccessibleByFaction) VALUES (?, ?, ?, ?)";
    private static final String INSERT_CLAIM_OWNER = "INSERT INTO ClaimOwners (WorldUUID, ChunkPosition, PlayerUUID) VALUES (?, ?, ?)";
    private static final String INSERT_CHEST = "INSERT INTO FactionChests (FactionName, ChestItems) VALUES (?, ?)";
    private static final String INSERT_OFFICERS = "INSERT INTO FactionOfficers (OfficerUUID, FactionName) VALUES (?, ?)";
    private static final String INSERT_MEMBERS = "INSERT INTO FactionMembers (MemberUUID, FactionName) VALUES (?, ?)";
    private static final String INSERT_RECRUITS = "INSERT INTO FactionRecruits (RecruitUUID, FactionName) VALUES (?, ?)";
    private static final String INSERT_OFFICER_PERMS = "INSERT INTO OfficerPerms (FactionName, `Use`, Place, Destroy, Claim, Attack, Invite, Chest) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_MEMBER_PERMS = "INSERT INTO MemberPerms (FactionName, `Use`, Place, Destroy, Claim, Attack, Invite, Chest) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_RECRUIT_PERMS = "INSERT INTO RecruitPerms (FactionName, `Use`, Place, Destroy, Claim, Attack, Invite, Chest) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_ALLY_PERMS = "INSERT INTO AllyPerms (FactionName, `Use`, Place, Destroy) VALUES (?, ?, ?, ?)";
    private static final String INSERT_FACTION_ALLIANCE = "INSERT INTO FactionAlliances (FactionName_1, FactionName_2) VALUES (?, ?)";
    private static final String INSERT_FACTION_ENEMY = "INSERT INTO FactionEnemies (FactionName_1, FactionName_2) VALUES (?, ?)";
    private static final String INSERT_FACTION_TRUCE = "INSERT INTO FactionTruces (FactionName_1, FactionName_2) VALUES (?, ?)";

    private static final String UPDATE_FACTION = "UPDATE Factions SET Name = ?, Tag = ?, TagColor = ?, Leader = ?, Home = ?, LastOnline = ?, Description = ?, Motd = ?, IsPublic = ? WHERE Name = ?";
    private static final String UPDATE_OFFICER_PERMS = "UPDATE OfficerPerms SET FactionName = ?, `Use` = ?, Place = ?, Destroy = ?, Claim = ?, Attack = ?, Invite = ?, Chest = ? WHERE FactionName = ?";
    private static final String UPDATE_MEMBER_PERMS = "UPDATE MemberPerms SET FactionName = ?, `Use` = ?, Place = ?, Destroy = ?, Claim = ?, Attack = ?, Invite = ?, Chest = ? WHERE FactionName = ?";
    private static final String UPDATE_RECRUIT_PERMS = "UPDATE RecruitPerms SET FactionName = ?, `Use` = ?, Place = ?, Destroy = ?, Claim = ?, Attack = ?, Invite = ?, Chest = ? WHERE FactionName = ?";
    private static final String UPDATE_ALLY_PERMS = "UPDATE AllyPerms SET FactionName = ?, `Use` = ?, Place = ?, Destroy = ? WHERE FactionName = ?";

    private static final String DELETE_FACTIONS = "DELETE FROM Factions";
    private static final String DELETE_FACTION_WHERE_FACTIONNAME = "DELETE FROM Factions WHERE Name=?";
    private static final String DELETE_OFFICERS_WHERE_FACIONNAME = "DELETE FROM FactionOfficers WHERE FactionName=?";
    private static final String DELETE_MEMBERS_WHERE_FACIONNAME = "DELETE FROM FactionMembers WHERE FactionName=?";
    private static final String DELETE_RECRUITS_WHERE_FACIONNAME = "DELETE FROM FactionRecruits WHERE FactionName=?";
    private static final String DELETE_FACTION_CHEST_WHERE_FACTIONNAME = "DELETE FROM FactionChests WHERE FactionName=?";
    private static final String DELETE_CLAIM_WHERE_FACTIONNAME = "DELETE FROM Claims WHERE FactionName=?";
    private static final String DELETE_ALL_FACTION_ALLIANCES_FOR_FACTION = "DELETE FROM FactionAlliances WHERE FactionName_1=? OR FactionName_2=?";
    private static final String DELETE_ALL_FACTION_ENEMIES_FOR_FACTION = "DELETE FROM FactionEnemies WHERE FactionName_1=? OR FactionName_2=?";
    private static final String DELETE_ALL_FACTION_TRUCES_FOR_FACTION = "DELETE FROM FactionTruces WHERE FactionName_1=? OR FactionName_2=?";
    private static final String DELETE_FACTION_ALLIANCE_BETWEEN_FACTIONS = "DELETE FROM FactionAlliances WHERE (FactionName_1=? AND FactionName_2=?) OR (FactionName_1=? AND FactionName_2=?)";
    private static final String DELETE_FACTION_ENEMY_BETWEEN_FACTIONS = "DELETE FROM FactionEnemies WHERE (FactionName_1=? AND FactionName_2=?) OR (FactionName_1=? AND FactionName_2=?)";
    private static final String DELETE_FACTION_TRUCE_BETWEEN_FACTIONS = "DELETE FROM FactionTruces WHERE (FactionName_1=? AND FactionName_2=?) OR (FactionName_1=? AND FactionName_2=?)";

    private final EagleFactions plugin;
    private final SQLProvider sqlProvider;

    protected AbstractFactionStorage(final EagleFactions plugin, final SQLProvider sqlProvider)
    {
        this.plugin = plugin;
        this.sqlProvider = sqlProvider;

        if(this.sqlProvider == null)
        {
            Sponge.server().sendMessage(Identity.nil(), Component.text("Could not establish connection to the database. Aborting...", RED));
            throw new IllegalStateException("Could not establish connection to the database. Aborting...");
        }
        try
        {
            updateDatabase();
        }
        catch(SQLException | IOException | URISyntaxException e)
        {
            throw new IllegalStateException(e);
        }
    }

    private void updateDatabase() throws IOException, SQLException, URISyntaxException
    {
        final int databaseVersionNumber = getDatabaseVersion();

        //Get all .sql files
        final List<Path> filePaths = getSqlFilesPaths();

        if (!filePaths.isEmpty())
        {
            for(final Path resourceFilePath : filePaths)
            {
                final int scriptNumber = Integer.parseInt(resourceFilePath.getFileName().toString().substring(0, 3));
                if(scriptNumber <= databaseVersionNumber)
                    continue;

                try(final InputStream inputStream = Files.newInputStream(resourceFilePath, StandardOpenOption.READ);
                    final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    final Connection connection = this.sqlProvider.getConnection();
                    final Statement statement = connection.createStatement())
                {
                    final StringBuilder stringBuilder = new StringBuilder();
                    String line;

                    while((line = bufferedReader.readLine()) != null)
                    {
                        if(line.startsWith("--"))
                            continue;

                        stringBuilder.append(line);

                        if(line.endsWith(";"))
                        {
                            statement.addBatch(stringBuilder.toString().trim());
                            stringBuilder.setLength(0);
                        }
                    }
                    statement.executeBatch();
                }
                catch(Exception exception)
                {
                    exception.printStackTrace();
                }
            }
        }
        else
        {
            System.out.println("There may be a problem with database script files...");
            System.out.println("Searched for them in: " + this.sqlProvider.getStorageType().getName());
            throw new IllegalStateException("There may be a problem with database script files...");
        }
        if (databaseVersionNumber == 0)
            precreate();
    }

    private List<Path> getSqlFilesPaths() throws URISyntaxException, IOException
    {
        final List<Path> filePaths = new ArrayList<>();
        final URL url = this.plugin.getResource("/assets/eaglefactions/queries/" + this.sqlProvider.getStorageType().getName());
        if (url != null)
        {
            final URI uri = url.toURI();
            Path myPath;
            if (uri.getScheme().equals("jar"))
            {
                final FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                myPath = fileSystem.getPath("/assets/eaglefactions/queries/" + this.sqlProvider.getStorageType().getName());
            }
            else
            {
                myPath = Paths.get(uri);
            }

            final Stream<Path> walk = Files.walk(myPath, 1);
            boolean skipFirst = true;
            for (final Iterator<Path> it = walk.iterator(); it.hasNext();) {
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

    private int getDatabaseVersion() throws SQLException
    {
        try(final Connection connection = this.sqlProvider.getConnection())
        {
            PreparedStatement preparedStatement = null;
            if(this.sqlProvider instanceof SqliteProvider)
            {
                preparedStatement = connection.prepareStatement("SELECT * FROM sqlite_master WHERE type='table' AND name='Version'");
            }
            else if(this.sqlProvider instanceof H2Provider)
            {
                preparedStatement = connection.prepareStatement("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'VERSION'");
            }
            else if(this.sqlProvider instanceof MySQLProvider || this.sqlProvider instanceof MariaDbProvider)
            {
                preparedStatement = connection.prepareStatement("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_NAME = 'Version'");
                preparedStatement.setString(1, this.plugin.getConfiguration().getStorageConfig().getDatabaseName());
            }

            final ResultSet resultSet = preparedStatement.executeQuery();
            boolean versionTableExists = false;
            while(resultSet.next())
            {
                versionTableExists = true;
            }

            if(versionTableExists)
            {
                final Statement statement = connection.createStatement();
                final ResultSet resultSet1 = statement.executeQuery("SELECT MAX(Version) FROM Version");
                if(resultSet1.next())
                {
                    return resultSet1.getInt(1);
                }
                statement.close();
            }
            preparedStatement.close();
        }
        return 0;
    }

    @Override
    public boolean saveFaction(final Faction faction)
    {
        Connection connection = null;
        try
        {
            connection = this.sqlProvider.getConnection();
            connection.setAutoCommit(false);

            //Add or update?
            PreparedStatement preparedStatement = connection.prepareStatement(SELECT_FACTION_WHERE_FACTIONNAME);
            preparedStatement.setString(1, faction.getName());
            final ResultSet factionSelect = preparedStatement.executeQuery();
            final boolean isUpdate = factionSelect.next();

            String queryToUse = isUpdate ? UPDATE_FACTION : INSERT_FACTION;

            preparedStatement = connection.prepareStatement(queryToUse);
            preparedStatement.setString(1, faction.getName());
            preparedStatement.setString(2, faction.getTag().toString());
            preparedStatement.setString(3, faction.getTag().color().asHexString());
            preparedStatement.setString(4, faction.getLeader().toString());
            if (faction.getHome() != null)
                preparedStatement.setString(5, faction.getHome().toString());
            else preparedStatement.setString(5, null);
            preparedStatement.setString(6, faction.getLastOnline().toString());
            preparedStatement.setString(7, faction.getDescription());
            preparedStatement.setString(8, faction.getMessageOfTheDay());
            preparedStatement.setString(9, faction.isPublic() ? "1" : "0");
            if (isUpdate)
                preparedStatement.setString(10, faction.getName()); //Where part

            preparedStatement.execute();
            preparedStatement.close();

            // Save or update alliance
            saveAlliances(connection, faction);
            saveEnemies(connection, faction);
            saveTruces(connection, faction);

            deleteFactionOfficers(connection, faction.getName());
            deleteFactionMembers(connection, faction.getName());
            deleteFactionRecruits(connection, faction.getName());
            deleteFactionClaims(connection, faction.getName());

            if (!faction.getOfficers().isEmpty())
            {
                preparedStatement = connection.prepareStatement(INSERT_OFFICERS);
                for (final UUID officer : faction.getOfficers())
                {
                    preparedStatement.setString(1, officer.toString());
                    preparedStatement.setString(2, faction.getName());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                preparedStatement.close();
            }

            if (!faction.getMembers().isEmpty())
            {
                preparedStatement = connection.prepareStatement(INSERT_MEMBERS);
                for (UUID member : faction.getMembers())
                {
                    preparedStatement.setString(1, member.toString());
                    preparedStatement.setString(2, faction.getName());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                preparedStatement.close();
            }

            if (!faction.getRecruits().isEmpty())
            {
                preparedStatement = connection.prepareStatement(INSERT_RECRUITS);
                for (final UUID recruit : faction.getRecruits())
                {
                    preparedStatement.setString(1, recruit.toString());
                    preparedStatement.setString(2, faction.getName());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                preparedStatement.close();
            }

            if (!faction.getClaims().isEmpty())
            {
                preparedStatement = connection.prepareStatement(INSERT_CLAIM);
                for (final Claim claim : faction.getClaims())
                {
                    preparedStatement.setString(1, faction.getName());
                    preparedStatement.setString(2, claim.getWorldUUID().toString());
                    preparedStatement.setString(3, claim.getChunkPosition().toString());
                    preparedStatement.setBoolean(4, claim.isAccessibleByFaction());

                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                preparedStatement.close();

                //Can't do it in above loop as it violates the foreign key constraint.
                // Insert owner into the claim
                for (final Claim claim : faction.getClaims())
                {
                    if (!claim.getOwners().isEmpty())
                    {
                        final PreparedStatement ownerPreparedStatement = connection.prepareStatement(INSERT_CLAIM_OWNER);
                        for (final UUID owner : claim.getOwners())
                        {
                            ownerPreparedStatement.setString(1, claim.getWorldUUID().toString());
                            ownerPreparedStatement.setString(2, claim.getChunkPosition().toString());
                            ownerPreparedStatement.setString(3, owner.toString());
                            ownerPreparedStatement.addBatch();
                        }
                        ownerPreparedStatement.executeBatch();
                        ownerPreparedStatement.close();
                    }
                }
            }

            List<DataView> dataViews = InventorySerializer.serializeInventory(faction.getChest().getInventory().inventory());
            final DataContainer dataContainer = DataContainer.createNew(DataView.SafetyMode.ALL_DATA_CLONED);
            dataContainer.set(DataQuery.of("inventory"), dataViews);
            ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
            DataFormats.NBT.get().writeTo(byteArrayStream, dataContainer);
//            String hocon = DataFormats.HOCON.write(test);
            byteArrayStream.flush();
            byte[] chestBytes = byteArrayStream.toByteArray();
            byteArrayStream.close();

            //Delete chest before
            preparedStatement = connection.prepareStatement(DELETE_FACTION_CHEST_WHERE_FACTIONNAME);
            preparedStatement.setString(1, faction.getName());
            preparedStatement.executeUpdate();

            preparedStatement = connection.prepareStatement(INSERT_CHEST);
            preparedStatement.setString(1, faction.getName());
            preparedStatement.setBytes(2, chestBytes);
            preparedStatement.execute();
            preparedStatement.close();

            preparedStatement = connection.prepareStatement(isUpdate ? UPDATE_OFFICER_PERMS : INSERT_OFFICER_PERMS);
            preparedStatement.setString(1, faction.getName());
            preparedStatement.setBoolean(2, faction.getPerms().get(FactionMemberType.OFFICER).get(FactionPermType.USE));
            preparedStatement.setBoolean(3, faction.getPerms().get(FactionMemberType.OFFICER).get(FactionPermType.PLACE));
            preparedStatement.setBoolean(4, faction.getPerms().get(FactionMemberType.OFFICER).get(FactionPermType.DESTROY));
            preparedStatement.setBoolean(5, faction.getPerms().get(FactionMemberType.OFFICER).get(FactionPermType.CLAIM));
            preparedStatement.setBoolean(6, faction.getPerms().get(FactionMemberType.OFFICER).get(FactionPermType.ATTACK));
            preparedStatement.setBoolean(7, faction.getPerms().get(FactionMemberType.OFFICER).get(FactionPermType.INVITE));
            preparedStatement.setBoolean(8, faction.getPerms().get(FactionMemberType.OFFICER).get(FactionPermType.CHEST));
            if(isUpdate)
                preparedStatement.setString(9, faction.getName());

            preparedStatement.execute();
            preparedStatement.close();

            preparedStatement = connection.prepareStatement(isUpdate ? UPDATE_MEMBER_PERMS : INSERT_MEMBER_PERMS);
            preparedStatement.setString(1, faction.getName());
            preparedStatement.setBoolean(2, faction.getPerms().get(FactionMemberType.MEMBER).get(FactionPermType.USE));
            preparedStatement.setBoolean(3, faction.getPerms().get(FactionMemberType.MEMBER).get(FactionPermType.PLACE));
            preparedStatement.setBoolean(4, faction.getPerms().get(FactionMemberType.MEMBER).get(FactionPermType.DESTROY));
            preparedStatement.setBoolean(5, faction.getPerms().get(FactionMemberType.MEMBER).get(FactionPermType.CLAIM));
            preparedStatement.setBoolean(6, faction.getPerms().get(FactionMemberType.MEMBER).get(FactionPermType.ATTACK));
            preparedStatement.setBoolean(7, faction.getPerms().get(FactionMemberType.MEMBER).get(FactionPermType.INVITE));
            preparedStatement.setBoolean(8, faction.getPerms().get(FactionMemberType.MEMBER).get(FactionPermType.CHEST));
            if(isUpdate)
                preparedStatement.setString(9, faction.getName());

            preparedStatement.execute();
            preparedStatement.close();

            preparedStatement = connection.prepareStatement(isUpdate ? UPDATE_RECRUIT_PERMS : INSERT_RECRUIT_PERMS);
            preparedStatement.setString(1, faction.getName());
            preparedStatement.setBoolean(2, faction.getPerms().get(FactionMemberType.RECRUIT).get(FactionPermType.USE));
            preparedStatement.setBoolean(3, faction.getPerms().get(FactionMemberType.RECRUIT).get(FactionPermType.PLACE));
            preparedStatement.setBoolean(4, faction.getPerms().get(FactionMemberType.RECRUIT).get(FactionPermType.DESTROY));
            preparedStatement.setBoolean(5, faction.getPerms().get(FactionMemberType.RECRUIT).get(FactionPermType.CLAIM));
            preparedStatement.setBoolean(6, faction.getPerms().get(FactionMemberType.RECRUIT).get(FactionPermType.ATTACK));
            preparedStatement.setBoolean(7, faction.getPerms().get(FactionMemberType.RECRUIT).get(FactionPermType.INVITE));
            preparedStatement.setBoolean(8, faction.getPerms().get(FactionMemberType.RECRUIT).get(FactionPermType.CHEST));
            if(isUpdate)
                preparedStatement.setString(9, faction.getName());

            preparedStatement.execute();
            preparedStatement.close();

            preparedStatement = connection.prepareStatement(isUpdate ? UPDATE_ALLY_PERMS : INSERT_ALLY_PERMS);
            preparedStatement.setString(1, faction.getName());
            preparedStatement.setBoolean(2, faction.getPerms().get(FactionMemberType.ALLY).get(FactionPermType.USE));
            preparedStatement.setBoolean(3, faction.getPerms().get(FactionMemberType.ALLY).get(FactionPermType.PLACE));
            preparedStatement.setBoolean(4, faction.getPerms().get(FactionMemberType.ALLY).get(FactionPermType.DESTROY));
            if(isUpdate)
                preparedStatement.setString(5, faction.getName());

            preparedStatement.execute();
            preparedStatement.close();

            connection.commit();
            connection.close();
            return true;
        }
        catch (SQLException | IOException e)
        {
            try
            {
                connection.rollback();
            }
            catch (SQLException e1)
            {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        return false;
    }

    private void saveAlliances(final Connection connection, final Faction faction) throws SQLException
    {
        final Set<String> existingAlliancesNames = getFactionAlliances(connection, faction.getName());
        final List<String> alliancesToRemove = existingAlliancesNames.stream().filter(alliance -> !faction.getAlliances().contains(alliance)).collect(Collectors.toList());
        final List<String> alliancesToAdd = faction.getAlliances().stream().filter(alliance -> !existingAlliancesNames.contains(alliance)).collect(Collectors.toList());

        LOGGER.debug("Alliances to add: " + Arrays.toString(alliancesToAdd.toArray()));
        LOGGER.debug("Alliances to remove: " + Arrays.toString(alliancesToRemove.toArray()));

        if(!alliancesToRemove.isEmpty())
        {
            PreparedStatement preparedStatement = connection.prepareStatement(DELETE_FACTION_ALLIANCE_BETWEEN_FACTIONS);
            for (final String allianceToRemove : alliancesToRemove)
            {
                preparedStatement.setString(1, faction.getName());
                preparedStatement.setString(2, allianceToRemove);
                preparedStatement.setString(3, allianceToRemove);
                preparedStatement.setString(4, faction.getName());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            preparedStatement.close();
        }

        if (!alliancesToAdd.isEmpty())
        {
            PreparedStatement preparedStatement = connection.prepareStatement(INSERT_FACTION_ALLIANCE);
            for (final String allianceToAdd : alliancesToAdd)
            {
                preparedStatement.setString(1, faction.getName());
                preparedStatement.setString(2, allianceToAdd);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            preparedStatement.close();
        }
    }

    private void saveEnemies(final Connection connection, final Faction faction) throws SQLException
    {
        final Set<String> existingEnemiesNames = getFactionEnemies(connection, faction.getName());
        final List<String> enemiesToRemove = existingEnemiesNames.stream().filter(enemy -> !faction.getEnemies().contains(enemy)).collect(Collectors.toList());
        final List<String> enemiesToAdd = faction.getEnemies().stream().filter(enemy -> !existingEnemiesNames.contains(enemy)).collect(Collectors.toList());

        LOGGER.debug("Enemies to add: " + Arrays.toString(enemiesToAdd.toArray()));
        LOGGER.debug("Enemies to remove: " + Arrays.toString(enemiesToRemove.toArray()));

        if(!enemiesToRemove.isEmpty())
        {
            PreparedStatement preparedStatement = connection.prepareStatement(DELETE_FACTION_ENEMY_BETWEEN_FACTIONS);
            for (final String enemyToRemove : enemiesToRemove)
            {
                preparedStatement.setString(1, faction.getName());
                preparedStatement.setString(2, enemyToRemove);
                preparedStatement.setString(3, enemyToRemove);
                preparedStatement.setString(4, faction.getName());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            preparedStatement.close();
        }

        if (!enemiesToAdd.isEmpty())
        {
            PreparedStatement preparedStatement = connection.prepareStatement(INSERT_FACTION_ENEMY);
            for (final String enemyToAdd : enemiesToAdd)
            {
                preparedStatement.setString(1, faction.getName());
                preparedStatement.setString(2, enemyToAdd);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            preparedStatement.close();
        }
    }

    private void saveTruces(final Connection connection, final Faction faction) throws SQLException
    {
        final Set<String> existingTrucesNames = getFactionTruces(connection, faction.getName());
        final List<String> trucesToRemove = existingTrucesNames.stream().filter(truce -> !faction.getTruces().contains(truce)).collect(Collectors.toList());
        final List<String> trucesToAdd = faction.getTruces().stream().filter(truce -> !existingTrucesNames.contains(truce)).collect(Collectors.toList());

        LOGGER.debug("Truces to add: " + Arrays.toString(trucesToAdd.toArray()));
        LOGGER.debug("Truces to remove: " + Arrays.toString(trucesToRemove.toArray()));

        if(!trucesToRemove.isEmpty())
        {
            PreparedStatement preparedStatement = connection.prepareStatement(DELETE_FACTION_TRUCE_BETWEEN_FACTIONS);
            for (final String allianceToRemove : trucesToRemove)
            {
                preparedStatement.setString(1, faction.getName());
                preparedStatement.setString(2, allianceToRemove);
                preparedStatement.setString(3, allianceToRemove);
                preparedStatement.setString(4, faction.getName());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            preparedStatement.close();
        }

        if (!trucesToAdd.isEmpty())
        {
            PreparedStatement preparedStatement = connection.prepareStatement(INSERT_FACTION_TRUCE);
            for (final String truceToAdd : trucesToAdd)
            {
                preparedStatement.setString(1, faction.getName());
                preparedStatement.setString(2, truceToAdd);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            preparedStatement.close();
        }
    }

    private void deleteFactionAlliances(final Connection connection, final String factionName) throws SQLException
    {
        final PreparedStatement preparedStatement = connection.prepareStatement(DELETE_ALL_FACTION_ALLIANCES_FOR_FACTION);
        preparedStatement.setString(1, factionName);
        preparedStatement.setString(2, factionName);
        int affectedRows = preparedStatement.executeUpdate();
        LOGGER.debug("Deleted " + affectedRows + " alliances for faction=" + factionName);
        preparedStatement.close();
    }

    private void deleteFactionEnemies(final Connection connection, final String factionName) throws SQLException
    {
        final PreparedStatement preparedStatement = connection.prepareStatement(DELETE_ALL_FACTION_ENEMIES_FOR_FACTION);
        preparedStatement.setString(1, factionName);
        preparedStatement.setString(2, factionName);
        int affectedRows = preparedStatement.executeUpdate();
        LOGGER.debug("Deleted " + affectedRows + " enemies for faction=" + factionName);
        preparedStatement.close();
    }

    private void deleteFactionTruces(final Connection connection, final String factionName) throws SQLException
    {
        final PreparedStatement preparedStatement = connection.prepareStatement(DELETE_ALL_FACTION_TRUCES_FOR_FACTION);
        preparedStatement.setString(1, factionName);
        preparedStatement.setString(2, factionName);
        int affectedRows = preparedStatement.executeUpdate();
        LOGGER.debug("Deleted " + affectedRows + " truces for faction=" + factionName);
        preparedStatement.close();
    }

    private boolean deleteFactionOfficers(final Connection connection, final String name) throws SQLException
    {
        final PreparedStatement preparedStatement = connection.prepareStatement(DELETE_OFFICERS_WHERE_FACIONNAME);
        preparedStatement.setString(1, name);
        final boolean didSucceed = preparedStatement.execute();
        preparedStatement.close();
        return didSucceed;
    }

    private boolean deleteFactionMembers(final Connection connection, final String name) throws SQLException
    {
        final PreparedStatement preparedStatement = connection.prepareStatement(DELETE_MEMBERS_WHERE_FACIONNAME);
        preparedStatement.setString(1, name);
        final boolean didSucceed = preparedStatement.execute();
        preparedStatement.close();
        return didSucceed;
    }

    private boolean deleteFactionRecruits(final Connection connection, final String name) throws SQLException
    {
        final PreparedStatement preparedStatement = connection.prepareStatement(DELETE_RECRUITS_WHERE_FACIONNAME);
        preparedStatement.setString(1, name);
        final boolean didSucceed = preparedStatement.execute();
        preparedStatement.close();
        return didSucceed;
    }

    //Removal of claim owners is handled by database through foreign keys constraints.
    private boolean deleteFactionClaims(final Connection connection, final String name) throws SQLException
    {
        final PreparedStatement preparedStatement = connection.prepareStatement(DELETE_CLAIM_WHERE_FACTIONNAME);
        preparedStatement.setString(1, name);
        final boolean didSucceed = preparedStatement.execute();
        preparedStatement.close();
        return didSucceed;
    }

    @Override
    public Faction getFaction(final String factionName)
    {
        try(final Connection connection = this.sqlProvider.getConnection())
        {
            final Set<String> alliances = getFactionAlliances(connection, factionName);
            final Set<String> enemies = getFactionEnemies(connection, factionName);
            final Set<String> truces = getFactionTruces(connection, factionName);

            PreparedStatement statement = connection.prepareStatement(SELECT_FACTION_WHERE_FACTIONNAME);
            statement.setString(1, factionName);
            ResultSet factionsResultSet = statement.executeQuery();
            if (factionsResultSet.next())
            {
                final String tag = factionsResultSet.getString("Tag");
                final String tagColor = factionsResultSet.getString("TagColor");
                final TextColor textColor = TextColor.fromHexString(tagColor);
                final UUID leaderUUID = UUID.fromString(factionsResultSet.getString("Leader"));
                final String factionHomeAsString = factionsResultSet.getString("Home");
                final String description = factionsResultSet.getString("Description");
                final String messageOfTheDay = factionsResultSet.getString("Motd");
                final boolean isPublic = factionsResultSet.getBoolean("IsPublic");
                FactionHome factionHome = null;
                if (factionHomeAsString != null)
                    factionHome = FactionHome.from(factionHomeAsString);
                final String lastOnlineString = factionsResultSet.getString("LastOnline");
                final Instant lastOnline = Instant.parse(lastOnlineString);

                final Set<UUID> officers = getFactionOfficers(connection, factionName);
                final Set<UUID> recruits = getFactionRecruits(connection, factionName);
                final Set<UUID> members = getFactionMembers(connection, factionName);
                final Set<Claim> claims = getFactionClaims(connection, factionName);

                final FactionChest factionChest = getFactionChest(connection, factionName);
                final Map<FactionMemberType, Map<FactionPermType, Boolean>> perms = getFactionPerms(connection, factionName);

                final Faction faction = FactionImpl.builder(factionName, Component.text(tag, textColor), leaderUUID)
                        .setHome(factionHome)
                        .setTruces(truces)
                        .setAlliances(alliances)
                        .setEnemies(enemies)
                        .setClaims(claims)
                        .setLastOnline(lastOnline)
                        .setMembers(members)
                        .setRecruits(recruits)
                        .setOfficers(officers)
                        .setChest(factionChest)
                        .setPerms(perms)
                        .setDescription(description)
                        .setMessageOfTheDay(messageOfTheDay)
                        .setIsPublic(isPublic)
                        .build();
                return faction;
            }
        }
        catch (IOException | SQLException | ClassNotFoundException exception)
        {
            exception.printStackTrace();
        }
        return null;
    }

    @Override
    public Set<Faction> getFactions()
    {
        final Set<Faction> factions = new HashSet<>();
        try
        {
            final Connection connection = this.sqlProvider.getConnection();
            final ResultSet resultSet = connection.createStatement().executeQuery(SELECT_FACTION_NAMES);
            List<String> factionsNames = new ArrayList<>();
            while (resultSet.next())
            {
                factionsNames.add(resultSet.getString("Name"));
            }
            connection.close();

            for (final String factionName : factionsNames)
            {
                final Faction faction = getFaction(factionName);
                if(faction != null)
                    factions.add(faction);
            }
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
        }
        return factions;
    }

    @Override
    public void load()
    {

    }

    @Override
    public boolean deleteFaction(final String factionName)
    {
        try(final Connection connection = this.sqlProvider.getConnection())
        {
            deleteFactionAlliances(connection, factionName);
            deleteFactionEnemies(connection, factionName);
            deleteFactionTruces(connection, factionName);

            final PreparedStatement preparedStatement = connection.prepareStatement(DELETE_FACTION_WHERE_FACTIONNAME);
            preparedStatement.setString(1, factionName);
            final int affectedRows = preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
            return affectedRows == 1;
        }
        catch (final SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void deleteFactions()
    {
        try(final Connection connection = this.sqlProvider.getConnection())
        {
            final Statement statement = connection.createStatement();
            statement.execute(DELETE_FACTIONS);
            statement.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private Set<String> getFactionAlliances(final Connection connection, final String factionName) throws SQLException
    {
        final Set<String> existingAlliancesNames = new HashSet<>();
        PreparedStatement preparedStatement = connection.prepareStatement(SELECT_FACTION_ALLIANCES);
        preparedStatement.setString(1, factionName);
        preparedStatement.setString(2, factionName);
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next())
        {
            String firstFactionName = resultSet.getString(1);
            String secondFactionName = resultSet.getString(2);
            if (factionName.equals(firstFactionName))
                existingAlliancesNames.add(secondFactionName);
            else
                existingAlliancesNames.add(firstFactionName);
        }
        resultSet.close();
        preparedStatement.close();
        return existingAlliancesNames;
    }

    private Set<String> getFactionEnemies(final Connection connection, final String factionName) throws SQLException
    {
        final Set<String> existingEnemiesNames = new HashSet<>();
        PreparedStatement preparedStatement = connection.prepareStatement(SELECT_FACTION_ENEMIES);
        preparedStatement.setString(1, factionName);
        preparedStatement.setString(2, factionName);
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next())
        {
            String firstFactionName = resultSet.getString(1);
            String secondFactionName = resultSet.getString(2);
            if (factionName.equals(firstFactionName))
                existingEnemiesNames.add(secondFactionName);
            else
                existingEnemiesNames.add(firstFactionName);
        }
        resultSet.close();
        preparedStatement.close();
        return existingEnemiesNames;
    }

    private Set<String> getFactionTruces(final Connection connection, final String factionName) throws SQLException
    {
        final Set<String> existingTrucesNames = new HashSet<>();
        PreparedStatement preparedStatement = connection.prepareStatement(SELECT_FACTION_TRUCES);
        preparedStatement.setString(1, factionName);
        preparedStatement.setString(2, factionName);
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next())
        {
            String firstFactionName = resultSet.getString(1);
            String secondFactionName = resultSet.getString(2);
            if (factionName.equals(firstFactionName))
                existingTrucesNames.add(secondFactionName);
            else
                existingTrucesNames.add(firstFactionName);
        }
        resultSet.close();
        preparedStatement.close();
        return existingTrucesNames;
    }

    private Set<UUID> getFactionRecruits(final Connection connection, final String factionName) throws SQLException
    {
        final Set<UUID> recruits = new HashSet<>();
        final PreparedStatement preparedStatement = connection.prepareStatement(SELECT_RECRUITS_WHERE_FACTIONNAME);
        preparedStatement.setString(1, factionName);
        final ResultSet recruitsResultSet = preparedStatement.executeQuery();
        while (recruitsResultSet.next())
        {
            final UUID recruitUUID = UUID.fromString(recruitsResultSet.getString("RecruitUUID"));
            recruits.add(recruitUUID);
        }
        recruitsResultSet.close();
        preparedStatement.close();
        return recruits;
    }

    private Set<UUID> getFactionMembers(final Connection connection, final String factionName) throws SQLException
    {
        Set<UUID> members = new HashSet<>();
        PreparedStatement preparedStatement = connection.prepareStatement(SELECT_MEMBERS_WHERE_FACTIONNAME);
        preparedStatement.setString(1, factionName);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next())
        {
            UUID memberUUID = UUID.fromString(resultSet.getString("MemberUUID"));
            members.add(memberUUID);
        }
        resultSet.close();
        preparedStatement.close();
        return members;
    }

    private Set<UUID> getFactionOfficers(final Connection connection, final String factionName) throws SQLException
    {
        Set<UUID> officers = new HashSet<>();
        PreparedStatement preparedStatement = connection.prepareStatement(SELECT_OFFICERS_WHERE_FACTIONNAME);
        preparedStatement.setString(1, factionName);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next())
        {
            UUID officerUUID = UUID.fromString(resultSet.getString("OfficerUUID"));
            officers.add(officerUUID);
        }
        resultSet.close();
        preparedStatement.close();
        return officers;
    }

    private Set<Claim> getFactionClaims(final Connection connection, final String factionName) throws SQLException
    {
        Set<Claim> claims = new HashSet<>();
        PreparedStatement preparedStatement = connection.prepareStatement(SELECT_CLAIMS_WHERE_FACTIONNAME);
        preparedStatement.setString(1, factionName);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next())
        {
            String worldUUID = resultSet.getString("WorldUUID");
            String chunkPositionAsString = resultSet.getString("ChunkPosition");
            final boolean isAccessibleByFaction = resultSet.getBoolean("IsAccessibleByFaction");
            final Vector3i chunkPosition = ClaimTypeSerializer.deserializeVector3i(chunkPositionAsString);
            final Set<UUID> owners = new HashSet<>();

            final PreparedStatement preparedStatement1 = connection.prepareStatement(SELECT_CLAIM_OWNERS_WHERE_WORLD_AND_CHUNK);
            preparedStatement1.setString(1, worldUUID);
            preparedStatement1.setString(2, chunkPositionAsString);
            final ResultSet ownersResultSet = preparedStatement1.executeQuery();
            while (ownersResultSet.next())
            {
                final String playerUniqueIdAsString = ownersResultSet.getString("PlayerUUID");
                owners.add(UUID.fromString(playerUniqueIdAsString));
            }
            ownersResultSet.close();
            preparedStatement1.close();

            Claim claim = new Claim(UUID.fromString(worldUUID), chunkPosition, owners, isAccessibleByFaction);
            claims.add(claim);
        }
        resultSet.close();
        preparedStatement.close();
        return claims;
    }

    private FactionChest getFactionChest(final Connection connection, final String factionName) throws SQLException, IOException, ClassNotFoundException
    {
        FactionChest factionChest = new FactionChestImpl(factionName);
        PreparedStatement preparedStatement = connection.prepareStatement(SELECT_CHEST_WHERE_FACTIONNAME);
        preparedStatement.setString(1, factionName);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next())
        {
            byte[] factionChestItems = resultSet.getBytes("ChestItems");
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(factionChestItems);
            DataContainer dataContainer = DataFormats.NBT.get().readFrom(byteArrayInputStream);
            byteArrayInputStream.close();
            Inventory inventory = ViewableInventory.builder()
                    .type(ContainerTypes.GENERIC_9X3)
                    .completeStructure()
                    .plugin(EagleFactionsPlugin.getPlugin().getPluginContainer())
                    .build();
            InventorySerializer.deserializeInventory(dataContainer.getViewList(DataQuery.of("inventory")).orElse(new ArrayList<>()), inventory);
            factionChest = new FactionChestImpl(factionName, inventory);
        }
        resultSet.close();
        preparedStatement.close();
        return factionChest;
    }

    private Map<FactionMemberType, Map<FactionPermType, Boolean>> getFactionPerms(final Connection connection, final String factionName) throws SQLException
    {
        Map<FactionMemberType, Map<FactionPermType, Boolean>> permMap = new LinkedHashMap<>();
        final Map<FactionPermType, Boolean> officerMap = new LinkedHashMap<>();
        final Map<FactionPermType, Boolean> membersMap = new LinkedHashMap<>();
        final Map<FactionPermType, Boolean> recruitMap = new LinkedHashMap<>();
        final Map<FactionPermType, Boolean> allyMap = new LinkedHashMap<>();
        final Map<FactionPermType, Boolean> truceMap = new LinkedHashMap<>();

        //Get officer perms
        PreparedStatement preparedStatement = connection.prepareStatement(SELECT_OFFICER_PERMS_WHERE_FACTIONNAME);
        preparedStatement.setString(1, factionName);
        ResultSet officerResult = preparedStatement.executeQuery();
        boolean officerUSE = true;
        boolean officerPLACE = true;
        boolean officerDESTROY = true;
        boolean officerCLAIM = true;
        boolean officerATTACK = true;
        boolean officerINVITE = true;
        if (officerResult.next())
        {
            officerUSE = officerResult.getBoolean("Use");
            officerPLACE = officerResult.getBoolean("Place");
            officerDESTROY = officerResult.getBoolean("Destroy");
            officerCLAIM = officerResult.getBoolean("Claim");
            officerATTACK = officerResult.getBoolean("Attack");
            officerINVITE = officerResult.getBoolean("Invite");
        }
        officerResult.close();
        preparedStatement.close();

        //Get member perms
        preparedStatement = connection.prepareStatement(SELECT_MEMBER_PERMS_WHERE_FACTIONNAME);
        preparedStatement.setString(1, factionName);
        ResultSet memberResult = preparedStatement.executeQuery();
        boolean memberUSE = true;
        boolean memberPLACE = true;
        boolean memberDESTROY = true;
        boolean memberCLAIM = false;
        boolean memberATTACK = false;
        boolean memberINVITE = true;
        if (memberResult.next())
        {
            memberUSE = memberResult.getBoolean("Use");
            memberPLACE = memberResult.getBoolean("Place");
            memberDESTROY = memberResult.getBoolean("Destroy");
            memberCLAIM = memberResult.getBoolean("Claim");
            memberATTACK = memberResult.getBoolean("Attack");
            memberINVITE = memberResult.getBoolean("Invite");
        }
        memberResult.close();
        preparedStatement.close();

        //Get recruit perms
        preparedStatement = connection.prepareStatement(SELECT_RECRUIT_PERMS_WHERE_FACTIONNAME);
        preparedStatement.setString(1, factionName);
        ResultSet recruitResult = preparedStatement.executeQuery();
        boolean recruitUSE = true;
        boolean recruitPLACE = true;
        boolean recruitDESTROY = true;
        boolean recruitCLAIM = false;
        boolean recruitATTACK = false;
        boolean recruitINVITE = false;
        if (recruitResult.next())
        {
            recruitUSE = recruitResult.getBoolean("Use");
            recruitPLACE = recruitResult.getBoolean("Place");
            recruitDESTROY = recruitResult.getBoolean("Destroy");
            recruitCLAIM = recruitResult.getBoolean("Claim");
            recruitATTACK = recruitResult.getBoolean("Attack");
            recruitINVITE = recruitResult.getBoolean("Invite");
        }
        recruitResult.close();
        preparedStatement.close();

        //Get truce perms
        preparedStatement = connection.prepareStatement(SELECT_TRUCE_PERMS_WHERE_FACTIONNAME);
        preparedStatement.setString(1, factionName);
        ResultSet truceResult = preparedStatement.executeQuery();
        boolean truceUSE = true;
        boolean trucePLACE = false;
        boolean truceDESTROY = false;
        if (truceResult.next())
        {
            truceUSE = truceResult.getBoolean("Use");
            trucePLACE = truceResult.getBoolean("Place");
            truceDESTROY = truceResult.getBoolean("Destroy");
        }
        truceResult.close();
        preparedStatement.close();

        //Get ally perms
        preparedStatement = connection.prepareStatement(SELECT_ALLY_PERMS_WHERE_FACTIONNAME);
        preparedStatement.setString(1, factionName);
        ResultSet allyResult = preparedStatement.executeQuery();
        boolean allyUSE = true;
        boolean allyPLACE = true;
        boolean allyDESTROY = true;
        if (allyResult.next())
        {
            allyUSE = allyResult.getBoolean("Use");
            allyPLACE = allyResult.getBoolean("Place");
            allyDESTROY = allyResult.getBoolean("Destroy");
        }
        allyResult.close();
        preparedStatement.close();

        officerMap.put(FactionPermType.USE, officerUSE);
        officerMap.put(FactionPermType.PLACE, officerPLACE);
        officerMap.put(FactionPermType.DESTROY, officerDESTROY);
        officerMap.put(FactionPermType.CLAIM, officerCLAIM);
        officerMap.put(FactionPermType.ATTACK, officerATTACK);
        officerMap.put(FactionPermType.INVITE, officerINVITE);

        membersMap.put(FactionPermType.USE, memberUSE);
        membersMap.put(FactionPermType.PLACE, memberPLACE);
        membersMap.put(FactionPermType.DESTROY, memberDESTROY);
        membersMap.put(FactionPermType.CLAIM, memberCLAIM);
        membersMap.put(FactionPermType.ATTACK, memberATTACK);
        membersMap.put(FactionPermType.INVITE, memberINVITE);

        recruitMap.put(FactionPermType.USE, recruitUSE);
        recruitMap.put(FactionPermType.PLACE, recruitPLACE);
        recruitMap.put(FactionPermType.DESTROY, recruitDESTROY);
        recruitMap.put(FactionPermType.CLAIM, recruitCLAIM);
        recruitMap.put(FactionPermType.ATTACK, recruitATTACK);
        recruitMap.put(FactionPermType.INVITE, recruitINVITE);

        truceMap.put(FactionPermType.USE, truceUSE);
        truceMap.put(FactionPermType.PLACE, trucePLACE);
        truceMap.put(FactionPermType.DESTROY, truceDESTROY);

        allyMap.put(FactionPermType.USE, allyUSE);
        allyMap.put(FactionPermType.PLACE, allyPLACE);
        allyMap.put(FactionPermType.DESTROY, allyDESTROY);

        permMap.put(FactionMemberType.OFFICER, officerMap);
        permMap.put(FactionMemberType.MEMBER, membersMap);
        permMap.put(FactionMemberType.RECRUIT, recruitMap);
        permMap.put(FactionMemberType.TRUCE, truceMap);
        permMap.put(FactionMemberType.ALLY, allyMap);

        return permMap;
    }

    private void precreate()
    {
        try(final Connection connection = this.sqlProvider.getConnection())
        {
            connection.setAutoCommit(false);
            PreparedStatement warZoneStatement = connection.prepareStatement(INSERT_FACTION);
            warZoneStatement.setString(1, "WarZone");
            warZoneStatement.setString(2, "WZ");
            warZoneStatement.setString(3, "");
            warZoneStatement.setString(4, DUMMY_UUID.toString());
            warZoneStatement.setString(5, null);
            warZoneStatement.setString(6, Instant.now().toString());
            warZoneStatement.setString(7, "");
            warZoneStatement.setString(8, "");
            warZoneStatement.setString(9, "0");

            PreparedStatement safeZoneStatement = connection.prepareStatement(INSERT_FACTION);
            safeZoneStatement.setString(1, "SafeZone");
            safeZoneStatement.setString(2, "SZ");
            safeZoneStatement.setString(3, "");
            safeZoneStatement.setString(4, DUMMY_UUID.toString());
            safeZoneStatement.setString(5, null);
            safeZoneStatement.setString(6, Instant.now().toString());
            safeZoneStatement.setString(7, "");
            safeZoneStatement.setString(8, "");
            safeZoneStatement.setString(9, "0");

            warZoneStatement.execute();
            safeZoneStatement.execute();
            safeZoneStatement.close();
            warZoneStatement.close();
            connection.commit();
        }
        catch(final SQLException e)
        {
            e.printStackTrace();
        }
    }
}
