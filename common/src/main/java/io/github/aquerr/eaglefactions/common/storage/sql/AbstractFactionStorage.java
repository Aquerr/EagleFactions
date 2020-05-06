package io.github.aquerr.eaglefactions.common.storage.sql;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.*;
import io.github.aquerr.eaglefactions.common.entities.FactionChestImpl;
import io.github.aquerr.eaglefactions.common.entities.FactionImpl;
import io.github.aquerr.eaglefactions.common.storage.FactionStorage;
import io.github.aquerr.eaglefactions.common.storage.sql.h2.H2Provider;
import io.github.aquerr.eaglefactions.common.storage.sql.mariadb.MariaDbProvider;
import io.github.aquerr.eaglefactions.common.storage.sql.mysql.MySQLProvider;
import io.github.aquerr.eaglefactions.common.storage.util.InventorySerializer;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

public abstract class AbstractFactionStorage implements FactionStorage
{
    private static final String SELECT_FACTION_NAMES = "SELECT Name FROM Factions";
    private static final String SELECT_RECRUITS_WHERE_FACTIONNAME = "SELECT RecruitUUID FROM FactionRecruits WHERE FactionName=?";
    private static final String SELECT_OFFICERS_WHERE_FACTIONNAME = "SELECT OfficerUUID FROM FactionOfficers WHERE FactionName=?";
    private static final String SELECT_MEMBERS_WHERE_FACTIONNAME = "SELECT MemberUUID FROM FactionMembers WHERE FactionName=?";
    private static final String SELECT_CLAIMS_WHERE_FACTIONNAME = "SELECT * FROM Claims WHERE FactionName=?";
    private static final String SELECT_CHEST_WHERE_FACTIONNAME = "SELECT ChestItems FROM FactionChests WHERE FactionName=?";

    private static final String SELECT_OFFICER_PERMS_WHERE_FACTIONNAME = "SELECT * FROM OfficerPerms WHERE FactionName=?";
    private static final String SELECT_MEMBER_PERMS_WHERE_FACTIONNAME = "SELECT * FROM MemberPerms WHERE FactionName=?";
    private static final String SELECT_RECRUIT_PERMS_WHERE_FACTIONNAME = "SELECT * FROM RecruitPerms WHERE FactionName=?";
    private static final String SELECT_TRUCE_PERMS_WHERE_FACTIONNAME = "SELECT * FROM TrucePerms WHERE FactionName=?";
    private static final String SELECT_ALLY_PERMS_WHERE_FACTIONNAME = "SELECT * FROM AllyPerms WHERE FactionName=?";
    private static final String SELECT_FACTION_WHERE_FACTIONNAME = "SELECT * FROM Factions WHERE Name=?";

    private static final String DELETE_FACTIONS = "DELETE FROM Factions";

    private static final String DELETE_FACTION_WHERE_FACTIONNAME = "DELETE FROM Factions WHERE Name=?";
    private static final String DELETE_OFFICERS_WHERE_FACIONNAME = "DELETE FROM FactionOfficers WHERE FactionName=?";
    private static final String DELETE_MEMBERS_WHERE_FACIONNAME = "DELETE FROM FactionMembers WHERE FactionName=?";
    private static final String DELETE_RECRUITS_WHERE_FACIONNAME = "DELETE FROM FactionRecruits WHERE FactionName=?";
    private static final String DELETE_FACTION_CHEST_WHERE_FACTIONNAME = "DELETE FROM FactionChests WHERE FactionName=?";

    private static final String INSERT_FACTION = "INSERT INTO Factions (Name, Tag, TagColor, Leader, Home, LastOnline, Truces, Alliances, Enemies, Description, Motd, IsPublic) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_FACTION = "UPDATE Factions SET Name = ?, Tag = ?, TagColor = ?, Leader = ?, Home = ?, LastOnline = ?, Truces = ?, Alliances = ?, Enemies = ?, Description = ?, Motd = ?, IsPublic = ? WHERE Name = ?";


//    private static final String MERGE_FACTION = "MERGE INTO Factions (Name, Tag, TagColor, Leader, Home, LastOnline, Alliances, Enemies, Description, Motd) KEY (Name) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT_CLAIM = "INSERT INTO Claims (FactionName, WorldUUID, ChunkPosition) VALUES (?, ?, ?)";

//    private static final String MERGE_CLAIM = "MERGE INTO Claims (FactionName, WorldUUID, ChunkPosition) KEY (FactionName, WorldUUID, ChunkPosition) VALUES (?, ?, ?)";
    private static final String DELETE_CLAIM_WHERE_FACTIONNAME = "DELETE FROM Claims WHERE FactionName=?";

    private static final String MERGE_CHEST = "MERGE INTO FactionChests (FactionName, ChestItems) KEY (FactionName) VALUES (?, ?)";
    private static final String MERGE_OFFICERS = "MERGE INTO FactionOfficers (OfficerUUID, FactionName) KEY (OfficerUUID) VALUES (?, ?)";
    private static final String MERGE_MEMBERS = "MERGE INTO FactionMembers (MemberUUID, FactionName) KEY (MemberUUID) VALUES (?, ?)";
    private static final String MERGE_RECRUITS = "MERGE INTO FactionRecruits (RecruitUUID, FactionName) KEY (RecruitUUID) VALUES (?, ?)";

    private static final String INSERT_CHEST = "INSERT INTO FactionChests (FactionName, ChestItems) VALUES (?, ?)";
    private static final String INSERT_OFFICERS = "INSERT INTO FactionOfficers (OfficerUUID, FactionName) VALUES (?, ?)";
    private static final String INSERT_MEMBERS = "INSERT INTO FactionMembers (MemberUUID, FactionName) VALUES (?, ?)";
    private static final String INSERT_RECRUITS = "INSERT INTO FactionRecruits (RecruitUUID, FactionName) VALUES (?, ?)";

    private static final String MERGE_LEADER_PERMS = "MERGE INTO LeaderPerms (FactionName, Use, Place, Destroy, Claim, Attack, Invite) KEY (FactionName) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String MERGE_OFFICER_PERMS = "MERGE INTO OfficerPerms (FactionName, Use, Place, Destroy, Claim, Attack, Invite) KEY (FactionName) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String MERGE_MEMBER_PERMS = "MERGE INTO MemberPerms (FactionName, Use, Place, Destroy, Claim, Attack, Invite) KEY (FactionName) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String MERGE_RECRUIT_PERMS = "MERGE INTO RecruitPerms (FactionName, Use, Place, Destroy, Claim, Attack, Invite) KEY (FactionName) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String MERGE_ALLY_PERMS = "MERGE INTO AllyPerms (FactionName, Use, Place, Destroy) KEY (FactionName) VALUES (?, ?, ?, ?)";

    private static final String INSERT_OFFICER_PERMS = "INSERT INTO OfficerPerms (FactionName, `Use`, Place, Destroy, Claim, Attack, Invite) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_MEMBER_PERMS = "INSERT INTO MemberPerms (FactionName, `Use`, Place, Destroy, Claim, Attack, Invite) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_RECRUIT_PERMS = "INSERT INTO RecruitPerms (FactionName, `Use`, Place, Destroy, Claim, Attack, Invite) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_ALLY_PERMS = "INSERT INTO AllyPerms (FactionName, `Use`, Place, Destroy) VALUES (?, ?, ?, ?)";

    private static final String UPDATE_OFFICER_PERMS = "UPDATE OfficerPerms SET FactionName = ?, `Use` = ?, Place = ?, Destroy = ?, Claim = ?, Attack = ?, Invite = ? WHERE FactionName = ?";
    private static final String UPDATE_MEMBER_PERMS = "UPDATE MemberPerms SET FactionName = ?, `Use` = ?, Place = ?, Destroy = ?, Claim = ?, Attack = ?, Invite = ? WHERE FactionName = ?";
    private static final String UPDATE_RECRUIT_PERMS = "UPDATE RecruitPerms SET FactionName = ?, `Use` = ?, Place = ?, Destroy = ?, Claim = ?, Attack = ?, Invite = ? WHERE FactionName = ?";
    private static final String UPDATE_ALLY_PERMS = "UPDATE AllyPerms SET FactionName = ?, `Use` = ?, Place = ?, Destroy = ? WHERE FactionName = ?";

    private final EagleFactions plugin;
    private final SQLProvider sqlProvider;

    protected AbstractFactionStorage(final EagleFactions plugin, final SQLProvider sqlProvider)
    {
        this.plugin = plugin;
        this.sqlProvider = sqlProvider;

        if(this.sqlProvider == null) {
            Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.RED, "Could not establish connection to the database. Aborting..."));
            Sponge.getServer().shutdown();
        }
        try
        {
            final int databaseVersionNumber = getDatabaseVersion();

            //Get all .sql files
            final List<Path> filePaths = new ArrayList<>();
            final URL url = this.plugin.getResource("/assets/eaglefactions/queries/" + this.sqlProvider.getProviderName());
            if (url != null)
            {
                final URI uri = url.toURI();
                Path myPath;
                if (uri.getScheme().equals("jar"))
                {
                    final FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                    myPath = fileSystem.getPath("/assets/eaglefactions/queries/" + this.sqlProvider.getProviderName());
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

            if (!filePaths.isEmpty())
            {
                for(final Path resourceFilePath : filePaths)
                {
                    final int scriptNumber = Integer.parseInt(resourceFilePath.getFileName().toString().substring(0, 3));
                    if(scriptNumber <= databaseVersionNumber)
                        continue;

                    try(final InputStream inputStream = Files.newInputStream(resourceFilePath, StandardOpenOption.READ);
                        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream)))
                    {
                        try(final Statement statement = this.sqlProvider.getConnection().createStatement())
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
                System.out.println("Searched for them in: " + this.sqlProvider.getProviderName());
                Sponge.getServer().shutdown();
            }
            if (databaseVersionNumber == 0)
                precreate();
        }
        catch(SQLException | IOException | URISyntaxException e)
        {
            e.printStackTrace();
            Sponge.getServer().shutdown();
        }
    }

    private int getDatabaseVersion() throws SQLException
    {
        try(final Connection connection = this.sqlProvider.getConnection())
        {
            PreparedStatement preparedStatement = null;
            if(this.sqlProvider instanceof H2Provider)
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
                final ResultSet resultSet1 = statement.executeQuery("SELECT Version FROM Version");
                if(resultSet1.last())
                {
                    return resultSet1.getInt("Version");
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
            StringBuilder stringBuilder = new StringBuilder();
            for (String truce : faction.getTruces())
            {
                stringBuilder.append(truce);
                stringBuilder.append(",");
            }
            String truces = stringBuilder.toString();
            stringBuilder.setLength(0);
            for (String alliance : faction.getAlliances())
            {
                stringBuilder.append(alliance);
                stringBuilder.append(",");
            }
            String alliances = stringBuilder.toString();
            stringBuilder.setLength(0);
            for (String enemy : faction.getEnemies())
            {
                stringBuilder.append(enemy);
                stringBuilder.append(",");
            }
            String enemies = stringBuilder.toString();

            if(truces.endsWith(","))
                truces = truces.substring(0, truces.length() - 1);
            if (alliances.endsWith(","))
                alliances = alliances.substring(0, alliances.length() - 1);
            if (enemies.endsWith(","))
                enemies = enemies.substring(0, enemies.length() - 1);

            connection = this.sqlProvider.getConnection();
            connection.setAutoCommit(false);

            //Add or update?
            PreparedStatement preparedStatement = connection.prepareStatement(SELECT_FACTION_WHERE_FACTIONNAME);
            preparedStatement.setString(1, faction.getName());
            final ResultSet factionSelect = preparedStatement.executeQuery();
            final boolean exists = factionSelect.next();

            String queryToUse = exists ? UPDATE_FACTION : INSERT_FACTION;

            preparedStatement = connection.prepareStatement(queryToUse);
            preparedStatement.setString(1, faction.getName());
            preparedStatement.setString(2, faction.getTag().toPlain());
            preparedStatement.setString(3, faction.getTag().getColor().getId());
            preparedStatement.setString(4, faction.getLeader().toString());
            if (faction.getHome() != null)
                preparedStatement.setString(5, faction.getHome().toString());
            else preparedStatement.setString(5, null);
            preparedStatement.setString(6, faction.getLastOnline().toString());
            preparedStatement.setString(7, truces);
            preparedStatement.setString(8, alliances);
            preparedStatement.setString(9, enemies);
            preparedStatement.setString(10, faction.getDescription());
            preparedStatement.setString(11, faction.getMessageOfTheDay());
            preparedStatement.setString(12, faction.isPublic() ? "1" : "0");
            if(exists)
                preparedStatement.setString(13, faction.getName()); //Where part

            preparedStatement.execute();
            preparedStatement.close();

            deleteFactionOfficers(connection, faction.getName());
            deleteFactionMembers(connection, faction.getName());
            deleteFactionRecruits(connection, faction.getName());
            deleteFactionClaims(connection, faction.getName());

            //TODO: Convert to batch
            for (final UUID officer : faction.getOfficers())
            {
                if (faction.getName().equalsIgnoreCase("anticonstitutionnellement"))
                {
                    String test = "";
                }
                else if(officer.toString().equalsIgnoreCase("anticonstitutionnellement"))
                {
                    String test = "";
                }

                preparedStatement = connection.prepareStatement(INSERT_OFFICERS);
                preparedStatement.setString(1, officer.toString());
                preparedStatement.setString(2, faction.getName());
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }

            //TODO: Convert to batch
            for (UUID member : faction.getMembers())
            {
                preparedStatement = connection.prepareStatement(INSERT_MEMBERS);
                preparedStatement.setString(1, member.toString());
                preparedStatement.setString(2, faction.getName());
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }

            //TODO: Convert to batch
            for (final UUID recruit : faction.getRecruits())
            {
                preparedStatement = connection.prepareStatement(INSERT_RECRUITS);
                preparedStatement.setString(1, recruit.toString());
                preparedStatement.setString(2, faction.getName());
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }

            //TODO: Convert to batch
            for (final Claim claim : faction.getClaims())
            {
                preparedStatement = connection.prepareStatement(INSERT_CLAIM);
                preparedStatement.setString(1, faction.getName());
                preparedStatement.setString(2, claim.getWorldUUID().toString());
                preparedStatement.setString(3, claim.getChunkPosition().toString());
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }

            List<DataView> dataViews = InventorySerializer.serializeInventory(faction.getChest().getInventory());
            final DataContainer dataContainer = DataContainer.createNew(DataView.SafetyMode.ALL_DATA_CLONED);
            dataContainer.set(DataQuery.of("inventory"), dataViews);
            ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
            DataFormats.NBT.writeTo(byteArrayStream, dataContainer);
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

            preparedStatement = connection.prepareStatement(exists ? UPDATE_OFFICER_PERMS : INSERT_OFFICER_PERMS);
            preparedStatement.setString(1, faction.getName());
            preparedStatement.setBoolean(2, faction.getPerms().get(FactionMemberType.OFFICER).get(FactionPermType.USE));
            preparedStatement.setBoolean(3, faction.getPerms().get(FactionMemberType.OFFICER).get(FactionPermType.PLACE));
            preparedStatement.setBoolean(4, faction.getPerms().get(FactionMemberType.OFFICER).get(FactionPermType.DESTROY));
            preparedStatement.setBoolean(5, faction.getPerms().get(FactionMemberType.OFFICER).get(FactionPermType.CLAIM));
            preparedStatement.setBoolean(6, faction.getPerms().get(FactionMemberType.OFFICER).get(FactionPermType.ATTACK));
            preparedStatement.setBoolean(7, faction.getPerms().get(FactionMemberType.OFFICER).get(FactionPermType.INVITE));
            if(exists)
                preparedStatement.setString(8, faction.getName());

            preparedStatement.execute();
            preparedStatement.close();

            preparedStatement = connection.prepareStatement(exists ? UPDATE_MEMBER_PERMS : INSERT_MEMBER_PERMS);
            preparedStatement.setString(1, faction.getName());
            preparedStatement.setBoolean(2, faction.getPerms().get(FactionMemberType.MEMBER).get(FactionPermType.USE));
            preparedStatement.setBoolean(3, faction.getPerms().get(FactionMemberType.MEMBER).get(FactionPermType.PLACE));
            preparedStatement.setBoolean(4, faction.getPerms().get(FactionMemberType.MEMBER).get(FactionPermType.DESTROY));
            preparedStatement.setBoolean(5, faction.getPerms().get(FactionMemberType.MEMBER).get(FactionPermType.CLAIM));
            preparedStatement.setBoolean(6, faction.getPerms().get(FactionMemberType.MEMBER).get(FactionPermType.ATTACK));
            preparedStatement.setBoolean(7, faction.getPerms().get(FactionMemberType.MEMBER).get(FactionPermType.INVITE));
            if(exists)
                preparedStatement.setString(8, faction.getName());

            preparedStatement.execute();
            preparedStatement.close();

            preparedStatement = connection.prepareStatement(exists ? UPDATE_RECRUIT_PERMS : INSERT_RECRUIT_PERMS);
            preparedStatement.setString(1, faction.getName());
            preparedStatement.setBoolean(2, faction.getPerms().get(FactionMemberType.RECRUIT).get(FactionPermType.USE));
            preparedStatement.setBoolean(3, faction.getPerms().get(FactionMemberType.RECRUIT).get(FactionPermType.PLACE));
            preparedStatement.setBoolean(4, faction.getPerms().get(FactionMemberType.RECRUIT).get(FactionPermType.DESTROY));
            preparedStatement.setBoolean(5, faction.getPerms().get(FactionMemberType.RECRUIT).get(FactionPermType.CLAIM));
            preparedStatement.setBoolean(6, faction.getPerms().get(FactionMemberType.RECRUIT).get(FactionPermType.ATTACK));
            preparedStatement.setBoolean(7, faction.getPerms().get(FactionMemberType.RECRUIT).get(FactionPermType.INVITE));
            if(exists)
                preparedStatement.setString(8, faction.getName());

            preparedStatement.execute();
            preparedStatement.close();

            preparedStatement = connection.prepareStatement(exists ? UPDATE_ALLY_PERMS : INSERT_ALLY_PERMS);
            preparedStatement.setString(1, faction.getName());
            preparedStatement.setBoolean(2, faction.getPerms().get(FactionMemberType.ALLY).get(FactionPermType.USE));
            preparedStatement.setBoolean(3, faction.getPerms().get(FactionMemberType.ALLY).get(FactionPermType.PLACE));
            preparedStatement.setBoolean(4, faction.getPerms().get(FactionMemberType.ALLY).get(FactionPermType.DESTROY));
            if(exists)
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
            PreparedStatement statement = connection.prepareStatement(SELECT_FACTION_WHERE_FACTIONNAME);
            statement.setString(1, factionName);
            ResultSet factionsResultSet = statement.executeQuery();
            if (factionsResultSet.first())
            {
                final String tag = factionsResultSet.getString("Tag");
                final String tagColor = factionsResultSet.getString("TagColor");
                final TextColor textColor = Sponge.getRegistry().getType(TextColor.class, tagColor).orElse(TextColors.RESET);
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
                final Set<String> truces = new HashSet<>(Arrays.asList(factionsResultSet.getString("Truces").split(",")));
                final Set<String> alliances = new HashSet<>(Arrays.asList(factionsResultSet.getString("Alliances").split(",")));
                final Set<String> enemies = new HashSet<>(Arrays.asList(factionsResultSet.getString("Enemies").split(",")));

                final Set<UUID> officers = getFactionOfficers(connection, factionName);
                final Set<UUID> recruits = getFactionRecruits(connection, factionName);
                final Set<UUID> members = getFactionMembers(connection, factionName);
                final Set<Claim> claims = getFactionClaims(connection, factionName);

                final FactionChest factionChest = getFactionChest(connection, factionName);
                final Map<FactionMemberType, Map<FactionPermType, Boolean>> perms = getFactionPerms(connection, factionName);

                final Faction faction = FactionImpl.builder(factionName, Text.of(textColor, tag), leaderUUID)
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
            final PreparedStatement preparedStatement = connection.prepareStatement(DELETE_FACTION_WHERE_FACTIONNAME);
            preparedStatement.setString(1, factionName);
            final boolean didSucceed = preparedStatement.execute();
            preparedStatement.close();
            connection.close();
            return didSucceed;
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
            String chunkPosition = resultSet.getString("ChunkPosition");
            Claim claim = new Claim(UUID.randomUUID(), Vector3i.ZERO);
//            Claim claim = new Claim(UUID.fromString(worldUUID), chunkPosition);
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
        if (resultSet.first())
        {
            byte[] factionChestItems = resultSet.getBytes("ChestItems");
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(factionChestItems);
            DataContainer dataContainer = DataFormats.NBT.readFrom(byteArrayInputStream);
            byteArrayInputStream.close();
            Inventory inventory = Inventory.builder().of(InventoryArchetypes.CHEST).build(this.plugin);
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
        if (officerResult.first())
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
        if (memberResult.first())
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
        if (recruitResult.first())
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
        if (truceResult.first())
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
        if (allyResult.first())
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
            PreparedStatement preparedStatement = connection.prepareStatement(INSERT_FACTION);
            preparedStatement.setString(1, "WarZone");
            preparedStatement.setString(2, "SZ");
            preparedStatement.setString(3, "");
            preparedStatement.setString(4, new UUID(0, 0).toString());
            preparedStatement.setString(5, null);
            preparedStatement.setString(6, Instant.now().toString());
            preparedStatement.setString(7, "");
            preparedStatement.setString(8, "");
            preparedStatement.setString(9, "");
            preparedStatement.setString(10, "");
            preparedStatement.setString(11, "");
            preparedStatement.setString(12, "0");

            PreparedStatement preparedStatement1 = connection.prepareStatement(INSERT_FACTION);
            preparedStatement1.setString(1, "SafeZone");
            preparedStatement1.setString(2, "WZ");
            preparedStatement1.setString(3, "");
            preparedStatement1.setString(4, new UUID(0, 0).toString());
            preparedStatement1.setString(5, null);
            preparedStatement1.setString(6, Instant.now().toString());
            preparedStatement1.setString(7, "");
            preparedStatement1.setString(8, "");
            preparedStatement1.setString(9, "");
            preparedStatement1.setString(10, "");
            preparedStatement1.setString(11, "");
            preparedStatement1.setString(12, "0");

            preparedStatement.execute();
            preparedStatement1.execute();
            preparedStatement1.close();
            preparedStatement.close();
            connection.commit();
        }
        catch(final SQLException e)
        {
            e.printStackTrace();
        }
    }
}
