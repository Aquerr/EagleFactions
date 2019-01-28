package io.github.aquerr.eaglefactions.storage.h2;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.entities.*;
import io.github.aquerr.eaglefactions.storage.IFactionStorage;
import io.github.aquerr.eaglefactions.storage.InventorySerializer;
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
import java.net.URL;
import java.sql.*;
import java.time.Instant;
import java.util.*;

public class H2FactionStorage implements IFactionStorage
{
//    private static final String SELECT_FACTIONS = "SELECT * FROM FACTIONS";
    private static final String SELECT_FACTIONNAMES = "SELECT Name FROM Factions";
//    private static final String SELECT_ALLIANCES = "SELECT  FROM ? WHERE";
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

//    private static final String UPDATE_FACTION = "UPDATE Factions SET Name=?, Tag=?, TagColor=?, Leader=?, Home=?, LastOnline=? WHERE Name=?";
    private static final String DELETE_FACTION_WHERE_FACTIONNAME = "DELETE FROM Factions WHERE Name=?";
    private static final String DELETE_OFFICERS_WHERE_FACIONNAME = "DELETE FROM FactionOfficers WHERE FactionName=?";
    private static final String DELETE_MEMBERS_WHERE_FACIONNAME = "DELETE FROM FactionMembers WHERE FactionName=?";
    private static final String DELETE_RECRUITS_WHERE_FACIONNAME = "DELETE FROM FactionRecruits WHERE FactionName=?";
//    private static final String DELETE_CLAIMS_WHERE_FACTIONNAME = "DELETE FROM Claims WHERE FactionName=?";

    //    private static final String DELEE_FROM_WHERE_FACTIONNAME = "DELETE FROM ? WHERE FactionName=?";
//    private static final String INSERT_FIVE_VALUES = "INSERT INTO ? VALUES (?, ?, ?, ?, ?)";
//    private static final String INSERT_FOUR_VALUES = "INSERT INTO ? VALUES (?, ?, ?, ?)";
//    private static final String INSERT_THREE_VALUES = "INSERT INTO ? VALUES (?, ?, ?)";
//    private static final String INSERT_TWO_VALUES = "INSERT INTO ? VALUES (?, ?)";
    private static final String INSERT_FACTION = "INSERT INTO Factions (Name, Tag, TagColor, Leader, Home, LastOnline, Alliances, Enemies) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String MERGE_FACTION = "MERGE INTO Factions (Name, Tag, TagColor, Leader, Home, LastOnline, Alliances, Enemies) KEY (Name) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String MERGE_CLAIM = "MERGE INTO Claims (FactionName, WorldUUID, ChunkPosition) KEY (FactionName) VALUES (?, ?, ?)";
    private static final String DELETE_CLAIM_WHERE_FACTIONNAME = "DELETE FROM Claims WHERE FactionName=?";

    private static final String MERGE_CHEST = "MERGE INTO FactionChests (FactionName, ChestItems) KEY (FactionName) VALUES (?, ?)";
    private static final String MERGE_OFFICERS = "MERGE INTO FactionOfficers (OfficerUUID, FactionName) KEY (OfficerUUID) VALUES (?, ?)";
    private static final String MERGE_MEMBERS = "MERGE INTO FactionMembers (MemberUUID, FactionName) KEY (MemberUUID) VALUES (?, ?)";
    private static final String MERGE_RECRUITS = "MERGE INTO FactionRecruits (RecruitUUID, FactionName) KEY (RecruitUUID) VALUES (?, ?)";


    private static final String MERGE_LEADER_FLAGS = "MERGE INTO LeaderFlags (FactionName, Use, Place, Destroy, Claim, Attack, Invite) KEY (FactionName) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String MERGE_OFFICER_FLAGS = "MERGE INTO OfficerFlags (FactionName, Use, Place, Destroy, Claim, Attack, Invite) KEY (FactionName) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String MERGE_MEMBER_FLAGS = "MERGE INTO MemberFlags (FactionName, Use, Place, Destroy, Claim, Attack, Invite) KEY (FactionName) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String MERGE_RECRUIT_FLAGS = "MERGE INTO RecruitFlags (FactionName, Use, Place, Destroy, Claim, Attack, Invite) KEY (FactionName) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String MERGE_ALLY_FLAGS = "MERGE INTO AllyFlags (FactionName, Use, Place, Destroy) KEY (FactionName) VALUES (?, ?, ?, ?)";


    private final EagleFactions plugin;
    private final H2Provider h2provider;

    public H2FactionStorage(EagleFactions plugin)
    {
        this.plugin = plugin;
        this.h2provider = H2Provider.getInstance(plugin);
        try
        {
            int databaseVersionNumber = getDatabaseVersion();

            //Get all .sql files
            URL resourcesFolderURL = this.plugin.getResource("queries/h2");
            File resourcesFolder = new File(resourcesFolderURL.getPath());
            File[] resources = resourcesFolder.listFiles();
            if (resources != null)
            {
                for(File resource : resources)
                {
                    int scriptNumber = Integer.parseInt(resource.getName().substring(0, 3));
                    if(scriptNumber <= databaseVersionNumber)
                        continue;

                    try(BufferedReader bufferedReader = new BufferedReader(new FileReader(resource)))
                    {
                        try(Statement statement = this.h2provider.getConnection().createStatement())
                        {
                            StringBuilder stringBuilder = new StringBuilder();
                            String line;

                            while((line = bufferedReader.readLine()) != null)
                            {
                                if(line.startsWith("--"))
                                    continue;

                                stringBuilder.append(line);

                                if(line.endsWith(";"))
                                {
                                    statement.addBatch(stringBuilder.toString().trim());
                                    stringBuilder = new StringBuilder();
                                }
                            }
                            statement.executeBatch();
                            statement.close();
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
                System.out.println("Searched for them in: " + resourcesFolder.getName());
            }
            if (databaseVersionNumber == 0)
                precreate();
            h2provider.getConnection().close();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
            Sponge.getServer().shutdown();
        }
    }

    private int getDatabaseVersion() throws SQLException
    {
        try(Connection connection = this.h2provider.getConnection())
        {
            ResultSet resultSet = connection.getMetaData().getTables(null, null, "VERSION", null);
            while(resultSet.next())
            {
                if(resultSet.getString(3).equalsIgnoreCase("Version"))
                {
                    try(Statement statement = h2provider.getConnection().createStatement())
                    {
                        ResultSet resultSet1 = statement.executeQuery("SELECT Version FROM Version");
                        if(resultSet1.last())
                        {
                            return resultSet1.getInt("Version");
                        }
                    }
                }
            }
            return 0;
        }
    }

    @Override
    public boolean addOrUpdateFaction(Faction faction)
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

            connection = this.h2provider.getConnection();
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(MERGE_FACTION);
            preparedStatement.setString(1, faction.getName());
            preparedStatement.setString(2, faction.getTag().toPlain());
            preparedStatement.setString(3, faction.getTag().getColor().getId());
            preparedStatement.setObject(4, faction.getLeader());
            if (faction.getHome() != null)
                preparedStatement.setString(5, faction.getHome().toString());
            else preparedStatement.setString(5, null);
            preparedStatement.setString(6, faction.getLastOnline().toString());
            preparedStatement.setString(7, alliances);
            preparedStatement.setString(8, enemies);
            preparedStatement.execute();
            preparedStatement.close();
            connection.close();

            //These create connection explicitly
            deleteFactionOfficers(faction.getName());
            deleteFactionMembers(faction.getName());
            deleteFactionRecruits(faction.getName());
            deleteFactionClaims(faction.getName());
            //End

            connection = this.h2provider.getConnection();

            for (UUID officer : faction.getOfficers())
            {
                preparedStatement = connection.prepareStatement(MERGE_OFFICERS);
                preparedStatement.setObject(1, officer);
                preparedStatement.setString(2, faction.getName());
                preparedStatement.execute();
                preparedStatement.close();
            }

            for (UUID member : faction.getMembers())
            {
                preparedStatement = connection.prepareStatement(MERGE_MEMBERS);
                preparedStatement.setObject(1, member);
                preparedStatement.setString(2, faction.getName());
                preparedStatement.execute();
                preparedStatement.close();
            }

            for (UUID recruit : faction.getRecruits())
            {
                preparedStatement = connection.prepareStatement(MERGE_RECRUITS);
                preparedStatement.setObject(1, recruit);
                preparedStatement.setString(2, faction.getName());
                preparedStatement.execute();
                preparedStatement.close();
            }

            for (Claim claim : faction.getClaims())
            {
                preparedStatement = connection.prepareStatement(MERGE_CLAIM);
                preparedStatement.setString(1, faction.getName());
                preparedStatement.setObject(2, claim.getWorldUUID());
                preparedStatement.setString(3, claim.getChunkPosition().toString());
                preparedStatement.execute();
                preparedStatement.close();
            }

            List<DataView> dataViews = InventorySerializer.serializeInventory(faction.getChest().toInventory());
            final DataContainer dataContainer = DataContainer.createNew(DataView.SafetyMode.ALL_DATA_CLONED);
            dataContainer.set(DataQuery.of("inventory"), dataViews);
            ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
            DataFormats.NBT.writeTo(byteArrayStream, dataContainer);
//            String hocon = DataFormats.HOCON.write(test);
            byteArrayStream.flush();
            byte[] chestBytes = byteArrayStream.toByteArray();
            byteArrayStream.close();

            preparedStatement = connection.prepareStatement(MERGE_CHEST);
            preparedStatement.setString(1, faction.getName());
            preparedStatement.setBytes(2, chestBytes);
            preparedStatement.execute();
            preparedStatement.close();

            preparedStatement = connection.prepareStatement(MERGE_LEADER_FLAGS);
            preparedStatement.setString(1, faction.getName());
            preparedStatement.setBoolean(2, faction.getFlags().get(FactionMemberType.LEADER).get(FactionFlagTypes.USE));
            preparedStatement.setBoolean(3, faction.getFlags().get(FactionMemberType.LEADER).get(FactionFlagTypes.PLACE));
            preparedStatement.setBoolean(4, faction.getFlags().get(FactionMemberType.LEADER).get(FactionFlagTypes.DESTROY));
            preparedStatement.setBoolean(5, faction.getFlags().get(FactionMemberType.LEADER).get(FactionFlagTypes.CLAIM));
            preparedStatement.setBoolean(6, faction.getFlags().get(FactionMemberType.LEADER).get(FactionFlagTypes.ATTACK));
            preparedStatement.setBoolean(7, faction.getFlags().get(FactionMemberType.LEADER).get(FactionFlagTypes.INVITE));
            preparedStatement.execute();
            preparedStatement.close();

            preparedStatement = connection.prepareStatement(MERGE_OFFICER_FLAGS);
            preparedStatement.setString(1, faction.getName());
            preparedStatement.setBoolean(2, faction.getFlags().get(FactionMemberType.OFFICER).get(FactionFlagTypes.USE));
            preparedStatement.setBoolean(3, faction.getFlags().get(FactionMemberType.OFFICER).get(FactionFlagTypes.PLACE));
            preparedStatement.setBoolean(4, faction.getFlags().get(FactionMemberType.OFFICER).get(FactionFlagTypes.DESTROY));
            preparedStatement.setBoolean(5, faction.getFlags().get(FactionMemberType.OFFICER).get(FactionFlagTypes.CLAIM));
            preparedStatement.setBoolean(6, faction.getFlags().get(FactionMemberType.OFFICER).get(FactionFlagTypes.ATTACK));
            preparedStatement.setBoolean(7, faction.getFlags().get(FactionMemberType.OFFICER).get(FactionFlagTypes.INVITE));
            preparedStatement.execute();
            preparedStatement.close();

            preparedStatement = connection.prepareStatement(MERGE_MEMBER_FLAGS);
            preparedStatement.setString(1, faction.getName());
            preparedStatement.setBoolean(2, faction.getFlags().get(FactionMemberType.MEMBER).get(FactionFlagTypes.USE));
            preparedStatement.setBoolean(3, faction.getFlags().get(FactionMemberType.MEMBER).get(FactionFlagTypes.PLACE));
            preparedStatement.setBoolean(4, faction.getFlags().get(FactionMemberType.MEMBER).get(FactionFlagTypes.DESTROY));
            preparedStatement.setBoolean(5, faction.getFlags().get(FactionMemberType.MEMBER).get(FactionFlagTypes.CLAIM));
            preparedStatement.setBoolean(6, faction.getFlags().get(FactionMemberType.MEMBER).get(FactionFlagTypes.ATTACK));
            preparedStatement.setBoolean(7, faction.getFlags().get(FactionMemberType.MEMBER).get(FactionFlagTypes.INVITE));
            preparedStatement.execute();
            preparedStatement.close();

            preparedStatement = connection.prepareStatement(MERGE_RECRUIT_FLAGS);
            preparedStatement.setString(1, faction.getName());
            preparedStatement.setBoolean(2, faction.getFlags().get(FactionMemberType.RECRUIT).get(FactionFlagTypes.USE));
            preparedStatement.setBoolean(3, faction.getFlags().get(FactionMemberType.RECRUIT).get(FactionFlagTypes.PLACE));
            preparedStatement.setBoolean(4, faction.getFlags().get(FactionMemberType.RECRUIT).get(FactionFlagTypes.DESTROY));
            preparedStatement.setBoolean(5, faction.getFlags().get(FactionMemberType.RECRUIT).get(FactionFlagTypes.CLAIM));
            preparedStatement.setBoolean(6, faction.getFlags().get(FactionMemberType.RECRUIT).get(FactionFlagTypes.ATTACK));
            preparedStatement.setBoolean(7, faction.getFlags().get(FactionMemberType.RECRUIT).get(FactionFlagTypes.INVITE));
            preparedStatement.execute();
            preparedStatement.close();

            preparedStatement = connection.prepareStatement(MERGE_ALLY_FLAGS);
            preparedStatement.setString(1, faction.getName());
            preparedStatement.setBoolean(2, faction.getFlags().get(FactionMemberType.ALLY).get(FactionFlagTypes.USE));
            preparedStatement.setBoolean(3, faction.getFlags().get(FactionMemberType.ALLY).get(FactionFlagTypes.PLACE));
            preparedStatement.setBoolean(4, faction.getFlags().get(FactionMemberType.ALLY).get(FactionFlagTypes.DESTROY));
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

    private boolean deleteFactionOfficers(final String name) throws SQLException
    {
        PreparedStatement preparedStatement = this.h2provider.getConnection().prepareStatement(DELETE_OFFICERS_WHERE_FACIONNAME);
        preparedStatement.setString(1, name);
        boolean didSucceed = preparedStatement.execute();
        preparedStatement.close();
        return didSucceed;
    }

    private boolean deleteFactionMembers(final String name) throws SQLException
    {
        PreparedStatement preparedStatement = this.h2provider.getConnection().prepareStatement(DELETE_MEMBERS_WHERE_FACIONNAME);
        preparedStatement.setString(1, name);
        boolean didSucceed = preparedStatement.execute();
        preparedStatement.close();
        return didSucceed;
    }

    private boolean deleteFactionRecruits(final String name) throws SQLException
    {
        PreparedStatement preparedStatement = this.h2provider.getConnection().prepareStatement(DELETE_RECRUITS_WHERE_FACIONNAME);
        preparedStatement.setString(1, name);
        boolean didSucceed = preparedStatement.execute();
        preparedStatement.close();
        return didSucceed;
    }

    private boolean deleteFactionClaims(final String name) throws SQLException
    {
        PreparedStatement preparedStatement = this.h2provider.getConnection().prepareStatement(DELETE_CLAIM_WHERE_FACTIONNAME);
        preparedStatement.setString(1, name);
        boolean didSucceed = preparedStatement.execute();
        preparedStatement.close();
        return didSucceed;
    }

    @Override
    public Faction getFaction(String factionName)
    {
        try(Connection connection = this.h2provider.getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(SELECT_FACTION_WHERE_FACTIONNAME);
            statement.setString(1, factionName);
            ResultSet factionsResultSet = statement.executeQuery();
            if (factionsResultSet.first())
            {
                String tag = factionsResultSet.getString("Tag");
                String tagColor = factionsResultSet.getString("TagColor");
                TextColor textColor = Sponge.getRegistry().getType(TextColor.class, tagColor).orElse(TextColors.RESET);
                UUID leaderUUID = UUID.fromString(factionsResultSet.getString("Leader"));
                String factionHomeAsString = factionsResultSet.getString("Home");
                FactionHome factionHome = null;
                if (factionHomeAsString != null)
                    factionHome = FactionHome.from(factionHomeAsString);
                String lastOnlineString = factionsResultSet.getString("LastOnline");
                Instant lastOnline = Instant.parse(lastOnlineString);
                Set<String> alliances = new HashSet<>(Arrays.asList(factionsResultSet.getString("Alliances").split(",")));
                Set<String> enemies = new HashSet<>(Arrays.asList(factionsResultSet.getString("Enemies").split(",")));

                Set<UUID> officers = getFactionOfficers(factionName);
                Set<UUID> recruits = getFactionRecruits(factionName);
                Set<UUID> members = getFactionMembers(factionName);
                Set<Claim> claims = getFactionClaims(factionName);

                FactionChest factionChest = getFactionChest(factionName);
                Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> flags = getFactionFlags(factionName);

                Faction faction = Faction.builder()
                        .setName(factionName)
                        .setTag(Text.of(textColor, tag))
                        .setHome(factionHome)
                        .setLeader(leaderUUID)
                        .setAlliances(alliances)
                        .setEnemies(enemies)
                        .setClaims(claims)
                        .setLastOnline(lastOnline)
                        .setMembers(members)
                        .setRecruits(recruits)
                        .setOfficers(officers)
                        .setChest(factionChest)
                        .setFlags(flags)
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
        Set<Faction> factions = new HashSet<>();
        try
        {
            Connection connection = this.h2provider.getConnection();
            ResultSet resultSet = connection.createStatement().executeQuery(SELECT_FACTIONNAMES);
            List<String> factionsNames = new ArrayList<>();
            while (resultSet.next())
            {
                factionsNames.add(resultSet.getString("Name"));
            }
            h2provider.getConnection().close();
            connection.close();

            for (String factionName : factionsNames)
            {
                Faction faction = getFaction(factionName);
                factions.add(faction);
            }
        }
        catch (SQLException e)
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
        try(Connection connection = this.h2provider.getConnection())
        {
            PreparedStatement preparedStatement = connection.prepareStatement(DELETE_FACTION_WHERE_FACTIONNAME);
            preparedStatement.setString(1, factionName);
            boolean didSucceed = preparedStatement.execute();
            preparedStatement.close();
            connection.close();
            return didSucceed;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    private Set<UUID> getFactionRecruits(String factionName) throws SQLException
    {
        Set<UUID> recruits = new HashSet<>();
        Connection connection = this.h2provider.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(SELECT_RECRUITS_WHERE_FACTIONNAME);
        preparedStatement.setString(1, factionName);
        ResultSet recruitsResultSet = preparedStatement.executeQuery();
        while (recruitsResultSet.next())
        {
            UUID recruitUUID = UUID.fromString(recruitsResultSet.getString("RecruitUUID"));
            recruits.add(recruitUUID);
        }
        recruitsResultSet.close();
        preparedStatement.close();
        connection.close();
        return recruits;
    }

    private Set<UUID> getFactionMembers(final String factionName) throws SQLException
    {
        Set<UUID> members = new HashSet<>();
        Connection connection = this.h2provider.getConnection();
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
        connection.close();
        return members;
    }

    private Set<UUID> getFactionOfficers(final String factionName) throws SQLException
    {
        Set<UUID> officers = new HashSet<>();
        Connection connection = this.h2provider.getConnection();
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
        connection.close();
        return officers;
    }

    private Set<Claim> getFactionClaims(final String factionName) throws SQLException
    {
        Set<Claim> claims = new HashSet<>();
        Connection connection = this.h2provider.getConnection();
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
        connection.close();
        return claims;
    }

    private FactionChest getFactionChest(final String factionName) throws SQLException, IOException, ClassNotFoundException
    {
        FactionChest factionChest = new FactionChest();
        Connection connection = this.h2provider.getConnection();
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
            factionChest = FactionChest.fromInventory(inventory);
        }
        resultSet.close();
        preparedStatement.close();
        connection.close();
        return factionChest;
    }

    private Map<FactionMemberType, Map<FactionFlagTypes, Boolean>> getFactionFlags(final String factionName) throws SQLException
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

        Map<FactionFlagTypes, Boolean> leaderMap = new LinkedHashMap<>();
        Map<FactionFlagTypes, Boolean> officerMap = new LinkedHashMap<>();
        Map<FactionFlagTypes, Boolean> membersMap = new LinkedHashMap<>();
        Map<FactionFlagTypes, Boolean> recruitMap = new LinkedHashMap<>();
        Map<FactionFlagTypes, Boolean> allyMap = new LinkedHashMap<>();

        //Get leader flags
        Connection connection = this.h2provider.getConnection();
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
        connection.close();

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
        try(Connection connection = h2provider.getConnection())
        {
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(INSERT_FACTION);
            preparedStatement.setString(1, "WarZone");
            preparedStatement.setString(2, "SZ");
            preparedStatement.setString(3, "");
            preparedStatement.setString(4, "0");
            preparedStatement.setString(5, null);
            preparedStatement.setString(6, Instant.now().toString());
            preparedStatement.setString(7, "");
            preparedStatement.setString(8, "");

            PreparedStatement preparedStatement1 = connection.prepareStatement(INSERT_FACTION);
            preparedStatement1.setString(1, "SafeZone");
            preparedStatement1.setString(2, "WZ");
            preparedStatement1.setString(3, "");
            preparedStatement1.setString(4, "0");
            preparedStatement1.setString(5, null);
            preparedStatement1.setString(6, Instant.now().toString());
            preparedStatement1.setString(7, "");
            preparedStatement1.setString(8, "");

            preparedStatement.execute();
            preparedStatement1.execute();
            preparedStatement1.close();
            preparedStatement.close();
            connection.commit();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    }
}
