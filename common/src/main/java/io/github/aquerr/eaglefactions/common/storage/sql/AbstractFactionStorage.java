package io.github.aquerr.eaglefactions.common.storage.sql;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.*;
import io.github.aquerr.eaglefactions.common.entities.FactionImpl;
import io.github.aquerr.eaglefactions.common.entities.FactionChestImpl;
import io.github.aquerr.eaglefactions.common.storage.IFactionStorage;
import io.github.aquerr.eaglefactions.common.storage.sql.h2.H2Provider;
import io.github.aquerr.eaglefactions.common.storage.sql.mariadb.MariaDbProvider;
import io.github.aquerr.eaglefactions.common.storage.sql.mysql.MySQLProvider;
import io.github.aquerr.eaglefactions.common.storage.utils.InventorySerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
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
import java.nio.file.*;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

public abstract class AbstractFactionStorage implements IFactionStorage
{
    private static final String SELECT_FACTION_NAMES = "SELECT Name FROM Factions";
    private static final String SELECT_RECRUITS_WHERE_FACTIONNAME = "SELECT RecruitUUID FROM FactionRecruits WHERE FactionName=?";
    private static final String SELECT_OFFICERS_WHERE_FACTIONNAME = "SELECT OfficerUUID FROM FactionOfficers WHERE FactionName=?";
    private static final String SELECT_MEMBERS_WHERE_FACTIONNAME = "SELECT MemberUUID FROM FactionMembers WHERE FactionName=?";
    private static final String SELECT_CLAIMS_WHERE_FACTIONNAME = "SELECT * FROM Claims WHERE FactionName=?";
    private static final String SELECT_CHEST_WHERE_FACTIONNAME = "SELECT ChestItems FROM FactionChests WHERE FactionName=?";

    private static final String SELECT_LEADER_FLAGS_WHERE_FACTIONNAME = "SELECT * FROM LeaderFlags WHERE FactionName=?";
    private static final String SELECT_OFFICER_FLAGS_WHERE_FACTIONNAME = "SELECT * FROM OfficerFlags WHERE FactionName=?";
    private static final String SELECT_MEMBER_FLAGS_WHERE_FACTIONNAME = "SELECT * FROM MemberFlags WHERE FactionName=?";
    private static final String SELECT_RECRUIT_FLAGS_WHERE_FACTIONNAME = "SELECT * FROM RecruitFlags WHERE FactionName=?";
    private static final String SELECT_ALLY_FLAGS_WHERE_FACTIONNAME = "SELECT * FROM AllyFlags WHERE FactionName=?";
    private static final String SELECT_FACTION_WHERE_FACTIONNAME = "SELECT * FROM Factions WHERE Name=?";

    private static final String DELETE_FACTION_WHERE_FACTIONNAME = "DELETE FROM Factions WHERE Name=?";
    private static final String DELETE_OFFICERS_WHERE_FACIONNAME = "DELETE FROM FactionOfficers WHERE FactionName=?";
    private static final String DELETE_MEMBERS_WHERE_FACIONNAME = "DELETE FROM FactionMembers WHERE FactionName=?";
    private static final String DELETE_RECRUITS_WHERE_FACIONNAME = "DELETE FROM FactionRecruits WHERE FactionName=?";
    private static final String DELETE_FACTION_CHEST_WHERE_FACTIONNAME = "DELETE FROM FactionChests WHERE FactionName=?";

    private static final String INSERT_FACTION = "INSERT INTO Factions (Name, Tag, TagColor, Leader, Home, LastOnline, Alliances, Enemies, Description, Motd, IsPublic) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_FACTION = "UPDATE Factions SET Name = ?, Tag = ?, TagColor = ?, Leader = ?, Home = ?, LastOnline = ?, Alliances = ?, Enemies = ?, Description = ?, Motd = ?, IsPublic = ? WHERE Name = ?";


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

    private static final String MERGE_LEADER_FLAGS = "MERGE INTO LeaderFlags (FactionName, Use, Place, Destroy, Claim, Attack, Invite) KEY (FactionName) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String MERGE_OFFICER_FLAGS = "MERGE INTO OfficerFlags (FactionName, Use, Place, Destroy, Claim, Attack, Invite) KEY (FactionName) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String MERGE_MEMBER_FLAGS = "MERGE INTO MemberFlags (FactionName, Use, Place, Destroy, Claim, Attack, Invite) KEY (FactionName) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String MERGE_RECRUIT_FLAGS = "MERGE INTO RecruitFlags (FactionName, Use, Place, Destroy, Claim, Attack, Invite) KEY (FactionName) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String MERGE_ALLY_FLAGS = "MERGE INTO AllyFlags (FactionName, Use, Place, Destroy) KEY (FactionName) VALUES (?, ?, ?, ?)";

    private static final String INSERT_LEADER_FLAGS = "INSERT INTO LeaderFlags (FactionName, `Use`, Place, Destroy, Claim, Attack, Invite) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_OFFICER_FLAGS = "INSERT INTO OfficerFlags (FactionName, `Use`, Place, Destroy, Claim, Attack, Invite) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_MEMBER_FLAGS = "INSERT INTO MemberFlags (FactionName, `Use`, Place, Destroy, Claim, Attack, Invite) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_RECRUIT_FLAGS = "INSERT INTO RecruitFlags (FactionName, `Use`, Place, Destroy, Claim, Attack, Invite) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_ALLY_FLAGS = "INSERT INTO AllyFlags (FactionName, `Use`, Place, Destroy) VALUES (?, ?, ?, ?)";

    private static final String UPDATE_LEADER_FLAGS = "UPDATE LeaderFlags SET FactionName = ?, `Use` = ?, Place = ?, Destroy = ?, Claim = ?, Attack = ?, Invite = ? WHERE FactionName = ?";
    private static final String UPDATE_OFFICER_FLAGS = "UPDATE OfficerFlags SET FactionName = ?, `Use` = ?, Place = ?, Destroy = ?, Claim = ?, Attack = ?, Invite = ? WHERE FactionName = ?";
    private static final String UPDATE_MEMBER_FLAGS = "UPDATE MemberFlags SET FactionName = ?, `Use` = ?, Place = ?, Destroy = ?, Claim = ?, Attack = ?, Invite = ? WHERE FactionName = ?";
    private static final String UPDATE_RECRUIT_FLAGS = "UPDATE RecruitFlags SET FactionName = ?, `Use` = ?, Place = ?, Destroy = ?, Claim = ?, Attack = ?, Invite = ? WHERE FactionName = ?";
    private static final String UPDATE_ALLY_FLAGS = "UPDATE AllyFlags SET FactionName = ?, `Use` = ?, Place = ?, Destroy = ? WHERE FactionName = ?";

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
    public boolean addOrUpdateFaction(final Faction faction)
    {
        Connection connection = null;
        try
        {
            StringBuilder stringBuilder = new StringBuilder();
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
            preparedStatement.setString(7, alliances);
            preparedStatement.setString(8, enemies);
            preparedStatement.setString(9, faction.getDescription());
            preparedStatement.setString(10, faction.getMessageOfTheDay());
            preparedStatement.setString(11, faction.isPublic() ? "1" : "0");
            if(exists)
                preparedStatement.setString(12, faction.getName()); //Where part

            preparedStatement.execute();
            preparedStatement.close();

            deleteFactionOfficers(connection, faction.getName());
            deleteFactionMembers(connection, faction.getName());
            deleteFactionRecruits(connection, faction.getName());
            deleteFactionClaims(connection, faction.getName());

            for (final UUID officer : faction.getOfficers())
            {
                preparedStatement = connection.prepareStatement(INSERT_OFFICERS);
                preparedStatement.setString(1, officer.toString());
                preparedStatement.setString(2, faction.getName());
                preparedStatement.execute();
                preparedStatement.close();
            }

            for (UUID member : faction.getMembers())
            {
                preparedStatement = connection.prepareStatement(INSERT_MEMBERS);
                preparedStatement.setString(1, member.toString());
                preparedStatement.setString(2, faction.getName());
                preparedStatement.execute();
                preparedStatement.close();
            }

            for (final UUID recruit : faction.getRecruits())
            {
                preparedStatement = connection.prepareStatement(INSERT_RECRUITS);
                preparedStatement.setString(1, recruit.toString());
                preparedStatement.setString(2, faction.getName());
                preparedStatement.execute();
                preparedStatement.close();
            }

            for (final Claim claim : faction.getClaims())
            {
                preparedStatement = connection.prepareStatement(INSERT_CLAIM);
                preparedStatement.setString(1, faction.getName());
                preparedStatement.setString(2, claim.getWorldUUID().toString());
                preparedStatement.setString(3, claim.getChunkPosition().toString());
                preparedStatement.execute();
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

            preparedStatement = connection.prepareStatement(exists ? UPDATE_LEADER_FLAGS : INSERT_LEADER_FLAGS);
            preparedStatement.setString(1, faction.getName());
            preparedStatement.setBoolean(2, faction.getFlags().get(FactionMemberType.LEADER).get(FactionFlagTypes.USE));
            preparedStatement.setBoolean(3, faction.getFlags().get(FactionMemberType.LEADER).get(FactionFlagTypes.PLACE));
            preparedStatement.setBoolean(4, faction.getFlags().get(FactionMemberType.LEADER).get(FactionFlagTypes.DESTROY));
            preparedStatement.setBoolean(5, faction.getFlags().get(FactionMemberType.LEADER).get(FactionFlagTypes.CLAIM));
            preparedStatement.setBoolean(6, faction.getFlags().get(FactionMemberType.LEADER).get(FactionFlagTypes.ATTACK));
            preparedStatement.setBoolean(7, faction.getFlags().get(FactionMemberType.LEADER).get(FactionFlagTypes.INVITE));
            if(exists)
                preparedStatement.setString(8, faction.getName());

            preparedStatement.execute();
            preparedStatement.close();

            preparedStatement = connection.prepareStatement(exists ? UPDATE_OFFICER_FLAGS : INSERT_OFFICER_FLAGS);
            preparedStatement.setString(1, faction.getName());
            preparedStatement.setBoolean(2, faction.getFlags().get(FactionMemberType.OFFICER).get(FactionFlagTypes.USE));
            preparedStatement.setBoolean(3, faction.getFlags().get(FactionMemberType.OFFICER).get(FactionFlagTypes.PLACE));
            preparedStatement.setBoolean(4, faction.getFlags().get(FactionMemberType.OFFICER).get(FactionFlagTypes.DESTROY));
            preparedStatement.setBoolean(5, faction.getFlags().get(FactionMemberType.OFFICER).get(FactionFlagTypes.CLAIM));
            preparedStatement.setBoolean(6, faction.getFlags().get(FactionMemberType.OFFICER).get(FactionFlagTypes.ATTACK));
            preparedStatement.setBoolean(7, faction.getFlags().get(FactionMemberType.OFFICER).get(FactionFlagTypes.INVITE));
            if(exists)
                preparedStatement.setString(8, faction.getName());

            preparedStatement.execute();
            preparedStatement.close();

            preparedStatement = connection.prepareStatement(exists ? UPDATE_MEMBER_FLAGS : INSERT_MEMBER_FLAGS);
            preparedStatement.setString(1, faction.getName());
            preparedStatement.setBoolean(2, faction.getFlags().get(FactionMemberType.MEMBER).get(FactionFlagTypes.USE));
            preparedStatement.setBoolean(3, faction.getFlags().get(FactionMemberType.MEMBER).get(FactionFlagTypes.PLACE));
            preparedStatement.setBoolean(4, faction.getFlags().get(FactionMemberType.MEMBER).get(FactionFlagTypes.DESTROY));
            preparedStatement.setBoolean(5, faction.getFlags().get(FactionMemberType.MEMBER).get(FactionFlagTypes.CLAIM));
            preparedStatement.setBoolean(6, faction.getFlags().get(FactionMemberType.MEMBER).get(FactionFlagTypes.ATTACK));
            preparedStatement.setBoolean(7, faction.getFlags().get(FactionMemberType.MEMBER).get(FactionFlagTypes.INVITE));
            if(exists)
                preparedStatement.setString(8, faction.getName());

            preparedStatement.execute();
            preparedStatement.close();

            preparedStatement = connection.prepareStatement(exists ? UPDATE_RECRUIT_FLAGS : INSERT_RECRUIT_FLAGS);
            preparedStatement.setString(1, faction.getName());
            preparedStatement.setBoolean(2, faction.getFlags().get(FactionMemberType.RECRUIT).get(FactionFlagTypes.USE));
            preparedStatement.setBoolean(3, faction.getFlags().get(FactionMemberType.RECRUIT).get(FactionFlagTypes.PLACE));
            preparedStatement.setBoolean(4, faction.getFlags().get(FactionMemberType.RECRUIT).get(FactionFlagTypes.DESTROY));
            preparedStatement.setBoolean(5, faction.getFlags().get(FactionMemberType.RECRUIT).get(FactionFlagTypes.CLAIM));
            preparedStatement.setBoolean(6, faction.getFlags().get(FactionMemberType.RECRUIT).get(FactionFlagTypes.ATTACK));
            preparedStatement.setBoolean(7, faction.getFlags().get(FactionMemberType.RECRUIT).get(FactionFlagTypes.INVITE));
            if(exists)
                preparedStatement.setString(8, faction.getName());

            preparedStatement.execute();
            preparedStatement.close();

            preparedStatement = connection.prepareStatement(exists ? UPDATE_ALLY_FLAGS : INSERT_ALLY_FLAGS);
            preparedStatement.setString(1, faction.getName());
            preparedStatement.setBoolean(2, faction.getFlags().get(FactionMemberType.ALLY).get(FactionFlagTypes.USE));
            preparedStatement.setBoolean(3, faction.getFlags().get(FactionMemberType.ALLY).get(FactionFlagTypes.PLACE));
            preparedStatement.setBoolean(4, faction.getFlags().get(FactionMemberType.ALLY).get(FactionFlagTypes.DESTROY));
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
                final Set<String> alliances = new HashSet<>(Arrays.asList(factionsResultSet.getString("Alliances").split(",")));
                final Set<String> enemies = new HashSet<>(Arrays.asList(factionsResultSet.getString("Enemies").split(",")));

                final Set<UUID> officers = getFactionOfficers(connection, factionName);
                final Set<UUID> recruits = getFactionRecruits(connection, factionName);
                final Set<UUID> members = getFactionMembers(connection, factionName);
                final Set<Claim> claims = getFactionClaims(connection, factionName);

                final FactionChest factionChest = getFactionChest(connection, factionName);
                final Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> flags = getFactionFlags(connection, factionName);

                final Faction faction = FactionImpl.builder(factionName, Text.of(textColor, tag), leaderUUID)
                        .setHome(factionHome)
                        .setAlliances(alliances)
                        .setEnemies(enemies)
                        .setClaims(claims)
                        .setLastOnline(lastOnline)
                        .setMembers(members)
                        .setRecruits(recruits)
                        .setOfficers(officers)
                        .setChest(factionChest)
                        .setFlags(flags)
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
            Claim claim = Claim.valueOf(worldUUID + "|" + chunkPosition);
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

    private Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> getFactionFlags(final Connection connection, final String factionName) throws SQLException
    {
        Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> flagMap = new LinkedHashMap<>();

        //    Object flagnode = configNode.getNode("factions", factionName, "flags");

//        //TODO: Test this code.
//        if(flagnode instanceof Map)
//        {
//            Map<String, Object> flags = new HashMap<>();
//
//            for(Map.Entry<String, Object> flagEntry : flags.entrySet())
//            {
//                Map<FactionFlagTypes, Boolean> memberTypeMap = new LinkedHashMap<>();
//                if(flagEntry.getValue() instanceof Map)
//                {
//                    Map<String, Boolean> map = (Map<String, Boolean>)flagEntry.getValue();
//                    for(Map.Entry<String, Boolean> testEntry : map.entrySet())
//                    {
//                        map.put(FactionFlagTypes.valueOf(testEntry.getKey()), testEntry.getValue());
//                    }
//                }
//                flagMap.put(FactionMemberType.valueOf(flagEntry.getKey()), memberTypeMap);
//            }
//        }

        //Use TreeMap instead of LinkedHashMap to sort the map if needed.

        final Map<FactionFlagTypes, Boolean> leaderMap = new LinkedHashMap<>();
        final Map<FactionFlagTypes, Boolean> officerMap = new LinkedHashMap<>();
        final Map<FactionFlagTypes, Boolean> membersMap = new LinkedHashMap<>();
        final Map<FactionFlagTypes, Boolean> recruitMap = new LinkedHashMap<>();
        final Map<FactionFlagTypes, Boolean> allyMap = new LinkedHashMap<>();

        //Get leader flags
        PreparedStatement preparedStatement = connection.prepareStatement(SELECT_LEADER_FLAGS_WHERE_FACTIONNAME);
        preparedStatement.setString(1, factionName);
        ResultSet leaderResult = preparedStatement.executeQuery();
        boolean leaderUSE = true;
        boolean leaderPLACE = true;
        boolean leaderDESTROY = true;
        boolean leaderCLAIM = true;
        boolean leaderATTACK = true;
        boolean leaderINVITE = true;
        if (leaderResult.first())
        {
            leaderUSE = leaderResult.getBoolean("Use");
            leaderPLACE = leaderResult.getBoolean("Place");
            leaderDESTROY = leaderResult.getBoolean("Destroy");
            leaderCLAIM = leaderResult.getBoolean("Claim");
            leaderATTACK = leaderResult.getBoolean("Attack");
            leaderINVITE = leaderResult.getBoolean("Invite");
        }
        leaderResult.close();
        preparedStatement.close();

        //Get officer flags
        preparedStatement = connection.prepareStatement(SELECT_OFFICER_FLAGS_WHERE_FACTIONNAME);
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

        //Get member flags
        preparedStatement = connection.prepareStatement(SELECT_MEMBER_FLAGS_WHERE_FACTIONNAME);
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

        //Get recruit flags
        preparedStatement = connection.prepareStatement(SELECT_RECRUIT_FLAGS_WHERE_FACTIONNAME);
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

        //Get ally flags
        preparedStatement = connection.prepareStatement(SELECT_ALLY_FLAGS_WHERE_FACTIONNAME);
        preparedStatement.setString(1, factionName);
        ResultSet allyResult = preparedStatement.executeQuery();
        boolean allyUSE = true;
        boolean allyPLACE = false;
        boolean allyDESTROY = false;
        if (allyResult.first())
        {
            allyUSE = allyResult.getBoolean("Use");
            allyPLACE = allyResult.getBoolean("Place");
            allyDESTROY = allyResult.getBoolean("Destroy");
        }
        allyResult.close();
        preparedStatement.close();

        leaderMap.put(FactionFlagTypes.USE, leaderUSE);
        leaderMap.put(FactionFlagTypes.PLACE, leaderPLACE);
        leaderMap.put(FactionFlagTypes.DESTROY, leaderDESTROY);
        leaderMap.put(FactionFlagTypes.CLAIM, leaderCLAIM);
        leaderMap.put(FactionFlagTypes.ATTACK, leaderATTACK);
        leaderMap.put(FactionFlagTypes.INVITE, leaderINVITE);

        officerMap.put(FactionFlagTypes.USE, officerUSE);
        officerMap.put(FactionFlagTypes.PLACE, officerPLACE);
        officerMap.put(FactionFlagTypes.DESTROY, officerDESTROY);
        officerMap.put(FactionFlagTypes.CLAIM, officerCLAIM);
        officerMap.put(FactionFlagTypes.ATTACK, officerATTACK);
        officerMap.put(FactionFlagTypes.INVITE, officerINVITE);

        membersMap.put(FactionFlagTypes.USE, memberUSE);
        membersMap.put(FactionFlagTypes.PLACE, memberPLACE);
        membersMap.put(FactionFlagTypes.DESTROY, memberDESTROY);
        membersMap.put(FactionFlagTypes.CLAIM, memberCLAIM);
        membersMap.put(FactionFlagTypes.ATTACK, memberATTACK);
        membersMap.put(FactionFlagTypes.INVITE, memberINVITE);

        recruitMap.put(FactionFlagTypes.USE, recruitUSE);
        recruitMap.put(FactionFlagTypes.PLACE, recruitPLACE);
        recruitMap.put(FactionFlagTypes.DESTROY, recruitDESTROY);
        recruitMap.put(FactionFlagTypes.CLAIM, recruitCLAIM);
        recruitMap.put(FactionFlagTypes.ATTACK, recruitATTACK);
        recruitMap.put(FactionFlagTypes.INVITE, recruitINVITE);

        allyMap.put(FactionFlagTypes.USE, allyUSE);
        allyMap.put(FactionFlagTypes.PLACE, allyPLACE);
        allyMap.put(FactionFlagTypes.DESTROY, allyDESTROY);

        flagMap.put(FactionMemberType.LEADER, leaderMap);
        flagMap.put(FactionMemberType.OFFICER, officerMap);
        flagMap.put(FactionMemberType.MEMBER, membersMap);
        flagMap.put(FactionMemberType.RECRUIT, recruitMap);
        flagMap.put(FactionMemberType.ALLY, allyMap);

        return flagMap;
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
            preparedStatement.setString(11, "0");

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
            preparedStatement1.setString(11, "0");

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
