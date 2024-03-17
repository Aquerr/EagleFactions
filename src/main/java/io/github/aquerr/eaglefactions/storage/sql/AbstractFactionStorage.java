package io.github.aquerr.eaglefactions.storage.sql;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionChest;
import io.github.aquerr.eaglefactions.api.entities.FactionHome;
import io.github.aquerr.eaglefactions.api.entities.FactionMember;
import io.github.aquerr.eaglefactions.api.entities.FactionPermission;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlag;
import io.github.aquerr.eaglefactions.api.entities.Rank;
import io.github.aquerr.eaglefactions.api.entities.RelationType;
import io.github.aquerr.eaglefactions.entities.FactionChestImpl;
import io.github.aquerr.eaglefactions.entities.FactionImpl;
import io.github.aquerr.eaglefactions.entities.FactionMemberImpl;
import io.github.aquerr.eaglefactions.entities.RankImpl;
import io.github.aquerr.eaglefactions.logic.FactionLogicImpl;
import io.github.aquerr.eaglefactions.storage.FactionStorage;
import io.github.aquerr.eaglefactions.storage.serializers.ClaimTypeSerializer;
import io.github.aquerr.eaglefactions.storage.serializers.InventorySerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.item.inventory.ContainerTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.math.vector.Vector3i;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class AbstractFactionStorage implements FactionStorage
{
    private static final String SELECT_FACTION_NAMES = "SELECT name FROM faction";
    private static final String SELECT_MEMBERS_WHERE_FACTIONNAME = "SELECT faction_member.member_uuid, rank_name " +
            "FROM faction_member " +
            "JOIN faction_member_rank ON faction_member.member_uuid = faction_member_rank.member_uuid " +
            "WHERE faction_member.faction_name=?";
    private static final String SELECT_MEMBERS_RANKS_WHERE_MEMBER_UUID = "SELECT rank_name FROM faction_member_rank WHERE faction_name=? AND member_uuid=?";
    private static final String SELECT_CLAIMS_WHERE_FACTIONNAME = "SELECT * FROM claim WHERE faction_name=?";
    private static final String SELECT_CLAIM_OWNERS_WHERE_WORLD_AND_CHUNK = "SELECT * FROM claim_owner WHERE world_uuid=? AND chunk_position=?";
    private static final String SELECT_CHEST_WHERE_FACTIONNAME = "SELECT chest_items FROM faction_chest WHERE faction_name=?";
    private static final String SELECT_FACTION_WHERE_FACTIONNAME = "SELECT * FROM faction WHERE name=?";
    private static final String SELECT_FACTION_RELATIONS_BY_TYPE = "SELECT * FROM faction_relation WHERE relation_type=? AND (faction_name_1=? OR faction_name_2=?)";
    private static final String SELECT_RELATION_PERMISSION_WHERE_FACTION_NAME = "SELECT permission FROM faction_relation_permission WHERE faction_name=? AND relation_type=?";
    private static final String SELECT_FACTION_RANKS_WHERE_FACTION_NAME = "SELECT name, faction_name, display_name, ladder_position, display_in_chat FROM faction_rank WHERE faction_name=?";
    private static final String SELECT_FACTION_RANK_PERMSSIONS_WHERE_FACTION_NAME = "SELECT rank_name, faction_name, permission FROM faction_rank_permission WHERE faction_name=?";
    private static final String INSERT_FACTION = "INSERT INTO faction (name, tag, tag_color, leader, home, last_online, description, motd, is_public, created_date) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_CLAIM = "INSERT INTO claim (faction_name, world_uuid, chunk_position, is_accessible_by_faction) VALUES (?, ?, ?, ?)";
    private static final String INSERT_CLAIM_OWNER = "INSERT INTO claim_owner (world_uuid, chunk_position, player_uuid) VALUES (?, ?, ?)";
    private static final String INSERT_MEMBER = "INSERT INTO faction_member (member_uuid, faction_name) VALUES (?, ?)";
    private static final String INSERT_MEMBER_RANK_MAPPING = "INSERT INTO faction_member_rank (member_uuid, faction_name, rank_name) VALUES (?, ?, ?)";
    private static final String INSERT_RELATION = "INSERT INTO faction_relation (relation_type, faction_name_1, faction_name_2) VALUES (?, ?, ?)";
    private static final String INSERT_RELATION_PERMISSION = "INSERT INTO faction_relation_permission (faction_name, relation_type, permission) VALUES (?, ?, ?)";
    private static final String INSERT_RANK = "INSERT INTO faction_rank (name, faction_name, display_name, ladder_position, display_in_chat) VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_RANK_PERMISSION = "INSERT INTO faction_rank_permission (faction_name, rank_name, permission) VALUES (?, ?, ?)";
    private static final String UPDATE_FACTION = "UPDATE faction SET name = ?, tag = ?, tag_color = ?, leader = ?, home = ?, last_online = ?, description = ?, motd = ?, is_public = ?, created_date = ? " +
            "WHERE name = ?";

    private static final String DELETE_FACTION_WHERE_FACTIONNAME = "DELETE FROM faction WHERE name=?";
    private static final String DELETE_FACTION_RANKS_WHERE_FACTION_NAME = "DELETE FROM faction_rank WHERE faction_name=?";
    private static final String DELETE_MEMBERS_WHERE_FACIONNAME = "DELETE FROM faction_member WHERE faction_name=?";
    private static final String DELETE_MEMBERS_RANKS_WHERE_FACTIONNAME = "DELETE FROM faction_member_rank WHERE faction_name=?";
    private static final String DELETE_RANK_PERMISSIONS_WHERE_FACTION_NAME = "DELETE FROM faction_rank_permission WHERE faction_name=?";
    private static final String DELETE_MEMBER_RANK_MAPPINGS_WHERE_FACTION_NAME_AND_MEMBER_UUID = "DELETE FROM faction_member_rank WHERE faction_name=? AND member_uuid=?";
    private static final String DELETE_MEMBER_RANK_MAPPING_WHERE_FACTIONp_NAME_AND_RANK_NAME = "DELTE FROM faction_member_rank WHERE faction_name=? AND rank_name=?";
    private static final String DELETE_RANK_WHERE_FACTION_NAME = "DELETE faction_rank WHERE faction_name=? AND name=?";
    private static final String DELETE_RANK_PERMISSIONS_WHERE_FACTION_NAME_AND_RANK_NAME = "DELETE faction_rank_permission WHERE faction_name=? AND rank_name=?";
    private static final String DELETE_RELATION_BETWEEN_FACTIONS = "DELETE FROM faction_relation WHERE relation_type=? AND (faction_name_1=? AND faction_name_2=?) OR (faction_name_1=? AND faction_name_2=?)";
    private static final String DELETE_RELATION_PERMISSION_WHERE_FACTION_NAME = "DELETE FROM faction_relation_permission WHERE faction_name=?";
    private static final String DELETE_CLAIM_WHERE_FACTIONNAME = "DELETE FROM claim WHERE faction_name=?";
    private static final String DELETE_FACTION_CLAIM_OWNERS_WHERE_WORLDUUID_AND_CHUNKPOSITION = "DELETE FROM claim_owner WHERE world_uuid=? AND chunk_position=?";
    private final SQLConnectionProvider sqlConnectionProvider;

    private final FactionProtectionFlagsStorage factionProtectionFlagsStorage;
    private final FactionChestSqlHelper factionChestSqlHelper;

    private final Logger logger;

    protected AbstractFactionStorage (
            final Logger logger,
            final SQLConnectionProvider sqlConnectionProvider,
            final FactionProtectionFlagsStorage factionProtectionFlagsStorage,
            final FactionChestSqlHelper factionChestSqlHelper)
    {
        this.logger = logger;
        this.sqlConnectionProvider = sqlConnectionProvider;
        this.factionProtectionFlagsStorage = factionProtectionFlagsStorage;
        this.factionChestSqlHelper = factionChestSqlHelper;

        if(this.sqlConnectionProvider == null)
        {
            this.logger.error("Could not establish connection to the database. Aborting...");
            throw new IllegalStateException("Could not establish connection to the database. Aborting...");
        }
    }

    @Override
    public boolean saveFaction(final Faction faction)
    {
        Connection connection = null;
        try
        {
            connection = this.sqlConnectionProvider.getConnection();
            connection.setAutoCommit(false);

            //Add or update?
            PreparedStatement preparedStatement = connection.prepareStatement(SELECT_FACTION_WHERE_FACTIONNAME);
            preparedStatement.setString(1, faction.getName());
            final ResultSet factionSelect = preparedStatement.executeQuery();
            final boolean isUpdate = factionSelect.next();

            String queryToUse = isUpdate ? UPDATE_FACTION : INSERT_FACTION;

            preparedStatement = connection.prepareStatement(queryToUse);
            preparedStatement.setString(1, faction.getName());
            preparedStatement.setString(2, PlainTextComponentSerializer.plainText().serialize(faction.getTag()));
            preparedStatement.setString(3, Optional.ofNullable(faction.getTag())
                    .map(Component::color)
                    .map(TextColor::asHexString)
                    .orElse("GREEN"));

            String leaderUUID = faction.getLeader()
                    .map(FactionMember::getUniqueId)
                    .map(UUID::toString)
                    .orElse(null);

            preparedStatement.setString(4, leaderUUID);
            if (faction.getHome() != null)
                preparedStatement.setString(5, faction.getHome().toString());
            else preparedStatement.setString(5, null);
            preparedStatement.setTimestamp(6, Timestamp.from(faction.getLastOnline()));
            preparedStatement.setString(7, faction.getDescription());
            preparedStatement.setString(8, faction.getMessageOfTheDay());
            preparedStatement.setString(9, faction.isPublic() ? "1" : "0");
            preparedStatement.setTimestamp(10, Timestamp.from(faction.getCreatedDate()));
            if (isUpdate)
                preparedStatement.setString(11, faction.getName()); //Where part

            preparedStatement.execute();
            preparedStatement.close();

            // Save relations
            saveRelations(connection, faction.getName(), faction.getAlliances(), RelationType.ALLIANCE);
            saveRelations(connection, faction.getName(), faction.getTruces(), RelationType.TRUCE);
            saveRelations(connection, faction.getName(), faction.getEnemies(), RelationType.ENEMY);
            saveRelationsPermissions(connection, faction);

            this.factionProtectionFlagsStorage.saveProtectionFlags(connection, faction.getName(), faction.getProtectionFlags());

            saveRanks(connection, faction);
            saveMembers(connection, faction);
            saveClaims(connection, faction);
            saveChest(connection, faction);

            connection.commit();
            connection.close();
            return true;
        }
        catch (Exception e)
        {
            try
            {
                e.printStackTrace();
                connection.rollback();
                connection.close();
            }
            catch (SQLException e1)
            {
                throw new RuntimeException(e1);
            }
            throw new RuntimeException(e);
        }
    }

    private void saveRelationsPermissions(Connection connection, Faction faction) throws SQLException
    {
        deleteAllRelationsPermissions(connection, faction.getName());

        // Save relation permissions
        Map<RelationType, Set<FactionPermission>> relationPermissions = new HashMap<>();
        relationPermissions.put(RelationType.ALLIANCE, faction.getRelationPermissions(RelationType.ALLIANCE));
        relationPermissions.put(RelationType.TRUCE, faction.getRelationPermissions(RelationType.TRUCE));
        relationPermissions.put(RelationType.ENEMY, faction.getRelationPermissions(RelationType.ENEMY));

        final PreparedStatement preparedStatement = connection.prepareStatement(INSERT_RELATION_PERMISSION);
        for (Map.Entry<RelationType, Set<FactionPermission>> entry : relationPermissions.entrySet())
        {
            for (FactionPermission permission : entry.getValue())
            {
                preparedStatement.setString(1, faction.getName());
                preparedStatement.setString(2, entry.getKey().getName());
                preparedStatement.setString(3, permission.name());
                preparedStatement.addBatch();
            }
        }

        preparedStatement.executeBatch();
        preparedStatement.close();
    }

    private void saveChest(Connection connection, Faction faction) throws SQLException, IOException
    {
        this.factionChestSqlHelper.saveChest(connection, faction);
    }

    private void saveClaims(Connection connection, Faction faction) throws SQLException
    {
        deleteFactionClaims(connection, faction.getName());

        if (!faction.getClaims().isEmpty())
        {
            PreparedStatement preparedStatement = connection.prepareStatement(INSERT_CLAIM);
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

    }

    private void saveRanks(Connection connection, Faction faction) throws SQLException
    {
        deleteAllRanks(connection, faction.getName());

        final List<Rank> ranks = faction.getRanks();

        // Save ranks
        PreparedStatement preparedStatement = connection.prepareStatement(INSERT_RANK);
        for (Rank rank : ranks)
        {
            preparedStatement.setString(1, rank.getName());
            preparedStatement.setString(2, faction.getName());
            preparedStatement.setString(3, rank.getDisplayName());
            preparedStatement.setInt(4, rank.getLadderPosition());
            preparedStatement.setBoolean(5, rank.canDisplayInChat());
            preparedStatement.executeUpdate();
        }

        preparedStatement.executeBatch();
        preparedStatement.close();

        // Save rank permissions
        for (Rank rank: ranks)
        {
            if (!rank.getPermissions().isEmpty())
            {
                preparedStatement = connection.prepareStatement(INSERT_RANK_PERMISSION);
                for (FactionPermission permission : rank.getPermissions())
                {
                    preparedStatement.setString(1, faction.getName());
                    preparedStatement.setString(2, rank.getName());
                    preparedStatement.setString(3, permission.name());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                preparedStatement.close();
            }
        }
    }

    private void deleteAllRanks(Connection connection, String factionName) throws SQLException
    {
        deleteAllFactionMemberRanksMappings(connection, factionName);
        deleteAllFactionRankPermissions(connection, factionName);

        final PreparedStatement preparedStatement = connection.prepareStatement(DELETE_FACTION_RANKS_WHERE_FACTION_NAME);
        preparedStatement.setString(1, factionName);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    private void saveMembers(Connection connection, Faction faction) throws SQLException
    {
        deleteAllFactionMembers(connection, faction.getName());

        Set<FactionMember> factionMembers = faction.getMembers();

        if (!factionMembers.isEmpty())
        {
            // Save member
            PreparedStatement preparedStatement = connection.prepareStatement(INSERT_MEMBER);
            for (final FactionMember factionMember : factionMembers)
            {
                preparedStatement.setString(1, factionMember.getUniqueId().toString());
                preparedStatement.setString(2, faction.getName());
                preparedStatement.executeUpdate();

                // Save member rank mapping
                if (!factionMember.getRankNames().isEmpty())
                {
                    preparedStatement = connection.prepareStatement(INSERT_MEMBER_RANK_MAPPING);
                    for (final String rankName : factionMember.getRankNames())
                    {
                        preparedStatement.setString(1, factionMember.getUniqueId().toString());
                        preparedStatement.setString(2, faction.getName());
                        preparedStatement.setString(3, rankName);
                        preparedStatement.addBatch();
                    }
                    preparedStatement.executeBatch();
                }
            }
            preparedStatement.close();
        }
    }

    private Set<String> getRelations(Connection connection, String factionName, RelationType relationType) throws SQLException
    {
        final Set<String> existingRelationsNames = new HashSet<>();
        PreparedStatement preparedStatement = connection.prepareStatement(SELECT_FACTION_RELATIONS_BY_TYPE);
        preparedStatement.setString(1, relationType.getName());
        preparedStatement.setString(2, factionName);
        preparedStatement.setString(3, factionName);
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next())
        {
            String firstFactionName = resultSet.getString("faction_name_1");
            String secondFactionName = resultSet.getString("faction_name_2");
            if (factionName.equals(firstFactionName))
                existingRelationsNames.add(secondFactionName);
            else
                existingRelationsNames.add(firstFactionName);
        }
        resultSet.close();
        preparedStatement.close();
        return existingRelationsNames;
    }

    private void saveRelations(Connection connection, String factionName, Set<String> relations, RelationType relationType) throws SQLException
    {
        final Set<String> existingRelationsNames = getRelations(connection, factionName, relationType);
        final List<String> alliancesToRemove = existingRelationsNames.stream().filter(alliance -> !relations.contains(alliance)).collect(Collectors.toList());
        final List<String> alliancesToAdd = relations.stream().filter(alliance -> !existingRelationsNames.contains(alliance)).collect(Collectors.toList());

        logger.debug("Alliances to add: " + Arrays.toString(alliancesToAdd.toArray()));
        logger.debug("Alliances to remove: " + Arrays.toString(alliancesToRemove.toArray()));

        if(!alliancesToRemove.isEmpty())
        {
            PreparedStatement preparedStatement = connection.prepareStatement(DELETE_RELATION_BETWEEN_FACTIONS);
            for (final String allianceToRemove : alliancesToRemove)
            {
                preparedStatement.setString(1, relationType.getName());
                preparedStatement.setString(2, factionName);
                preparedStatement.setString(3, allianceToRemove);
                preparedStatement.setString(4, allianceToRemove);
                preparedStatement.setString(5, factionName);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            preparedStatement.close();
        }

        if (!alliancesToAdd.isEmpty())
        {
            PreparedStatement preparedStatement = connection.prepareStatement(INSERT_RELATION);
            for (final String allianceToAdd : alliancesToAdd)
            {
                preparedStatement.setString(1, relationType.getName());
                preparedStatement.setString(2, factionName);
                preparedStatement.setString(3, allianceToAdd);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            preparedStatement.close();
        }
    }

    private void deleteRelations(final Connection connection, final String factionName, RelationType relationType) throws SQLException
    {
        final PreparedStatement preparedStatement = connection.prepareStatement(DELETE_RELATION_BETWEEN_FACTIONS);
        preparedStatement.setString(1, relationType.getName());
        preparedStatement.setString(2, factionName);
        preparedStatement.setString(3, factionName);
        preparedStatement.setString(4, factionName);
        preparedStatement.setString(5, factionName);
        int affectedRows = preparedStatement.executeUpdate();
        if (logger.isDebugEnabled())
        {
            logger.debug("Deleted {} relations of type {} for faction={}", affectedRows, relationType.getName(), factionName);
        }
        preparedStatement.close();
    }

    private Set<FactionPermission> getRelationPermissions(Connection connection, String factionName, RelationType relationType) throws SQLException
    {
        final Set<FactionPermission> permissions = new HashSet<>();
        final PreparedStatement preparedStatement = connection.prepareStatement(SELECT_RELATION_PERMISSION_WHERE_FACTION_NAME);
        preparedStatement.setString(1, factionName);
        preparedStatement.setString(2, relationType.getName());
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next())
        {
            permissions.add(FactionPermission.valueOf(resultSet.getString("permission").toUpperCase()));
        }

        resultSet.close();
        preparedStatement.close();
        return permissions;
    }

    private boolean deleteAllFactionMembers(final Connection connection, final String factionName) throws SQLException
    {
        final PreparedStatement preparedStatement = connection.prepareStatement(DELETE_MEMBERS_WHERE_FACIONNAME);
        preparedStatement.setString(1, factionName);
        final boolean didSucceed = preparedStatement.execute();
        preparedStatement.close();
        return didSucceed;
    }

    private boolean deleteAllFactionMemberRanksMappings(final Connection connection, final String factionName) throws SQLException
    {
        final PreparedStatement preparedStatement = connection.prepareStatement(DELETE_MEMBERS_RANKS_WHERE_FACTIONNAME);
        preparedStatement.setString(1, factionName);
        final boolean didSucceed = preparedStatement.execute();
        preparedStatement.close();
        return didSucceed;
    }

    private boolean deleteAllFactionRankPermissions(final Connection connection, final String factionName) throws SQLException
    {
        final PreparedStatement preparedStatement = connection.prepareStatement(DELETE_RANK_PERMISSIONS_WHERE_FACTION_NAME);
        preparedStatement.setString(1, factionName);
        final boolean didSucceed = preparedStatement.execute();
        preparedStatement.close();
        return didSucceed;
    }

    private boolean deleteRanksForMember(final Connection connection, final String factionName, final UUID memberUUID) throws SQLException
    {
        final PreparedStatement preparedStatement = connection.prepareStatement(DELETE_MEMBER_RANK_MAPPINGS_WHERE_FACTION_NAME_AND_MEMBER_UUID);
        preparedStatement.setString(1, factionName);
        preparedStatement.setString(2, memberUUID.toString());
        final boolean didSucceed = preparedStatement.execute();
        preparedStatement.close();
        return didSucceed;
    }

    private boolean deleteRank(final Connection connection, final String factionName, final String rankName) throws SQLException
    {
        deleteFactionMemberRankMapping(connection, factionName, rankName);
        deleteFactionRankPermissions(connection, factionName, rankName);

        final PreparedStatement preparedStatement = connection.prepareStatement(DELETE_RANK_WHERE_FACTION_NAME);
        preparedStatement.setString(1, factionName);
        preparedStatement.setString(2, rankName);
        boolean didSucceed = preparedStatement.execute();
        preparedStatement.close();
        return didSucceed;
    }

    private List<Rank> getFactionRanks(Connection connection, String factionName) throws SQLException
    {
        final Map<String, Rank.Builder> rankBuilders = new HashMap<>();
        PreparedStatement preparedStatement = connection.prepareStatement(SELECT_FACTION_RANKS_WHERE_FACTION_NAME);
        preparedStatement.setString(1, factionName);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next())
        {
            String rankName = resultSet.getString("name");
            Rank.Builder rankBuilder = rankBuilders.computeIfAbsent(rankName, t -> new RankImpl.BuilderImpl());
            rankBuilder.name(rankName);
            rankBuilder.displayName(resultSet.getString("display_name"));
            rankBuilder.ladderPosition(resultSet.getInt("ladder_position"));
            rankBuilder.displayInChat(resultSet.getBoolean("display_in_chat"));
        }
        resultSet.close();
        preparedStatement.close();

        // Fetch rank permissions
        preparedStatement = connection.prepareStatement(SELECT_FACTION_RANK_PERMSSIONS_WHERE_FACTION_NAME);
        preparedStatement.setString(1, factionName);
        resultSet = preparedStatement.executeQuery();
        while (resultSet.next())
        {
            Rank.Builder builder = rankBuilders.get(resultSet.getString("rank_name"));
            builder.permission(FactionPermission.valueOf(resultSet.getString("permission").toUpperCase()));
        }

        return rankBuilders.values().stream()
                .map(Rank.Builder::build)
                .collect(Collectors.toList());
    }

    private void deleteFactionRankPermissions(Connection connection, String factionName, String rankName) throws SQLException
    {
        final PreparedStatement preparedStatement = connection.prepareStatement(DELETE_RANK_PERMISSIONS_WHERE_FACTION_NAME_AND_RANK_NAME);
        preparedStatement.setString(1, factionName);
        preparedStatement.setString(2, rankName);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    private void deleteFactionMemberRankMapping(Connection connection, String factionName, String rankName) throws SQLException
    {
        final PreparedStatement preparedStatement = connection.prepareStatement(DELETE_MEMBER_RANK_MAPPING_WHERE_FACTIONp_NAME_AND_RANK_NAME);
        preparedStatement.setString(1, factionName);
        preparedStatement.setString(2, rankName);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    private boolean deleteFactionClaims(final Connection connection, final String factionName) throws SQLException
    {
        Set<Claim> claims = getFactionClaims(connection, factionName);
        if (claims.isEmpty())
            return true;

        deleteFactionClaimOwners(connection, claims);

        final PreparedStatement preparedStatement = connection.prepareStatement(DELETE_CLAIM_WHERE_FACTIONNAME);
        preparedStatement.setString(1, factionName);
        final boolean didSucceed = preparedStatement.execute();
        preparedStatement.close();
        return didSucceed;
    }

    private void deleteFactionClaimOwners(Connection connection, Set<Claim> claims) throws SQLException
    {
        final PreparedStatement preparedStatement = connection.prepareStatement(DELETE_FACTION_CLAIM_OWNERS_WHERE_WORLDUUID_AND_CHUNKPOSITION);
        for (final Claim claim : claims)
        {
            preparedStatement.setString(1, claim.getWorldUUID().toString());
            preparedStatement.setString(2, claim.getChunkPosition().toString());
            preparedStatement.addBatch();
        }
        preparedStatement.executeBatch();
        preparedStatement.close();
    }

    @Override
    public Faction getFaction(final String factionName)
    {
        try(final Connection connection = this.sqlConnectionProvider.getConnection())
        {
            connection.setAutoCommit(false);
            PreparedStatement statement = connection.prepareStatement(SELECT_FACTION_WHERE_FACTIONNAME);
            statement.setString(1, factionName);
            ResultSet factionsResultSet = statement.executeQuery();
            return mapToFaction(connection, factionsResultSet, factionName);
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
            final Connection connection = this.sqlConnectionProvider.getConnection();
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
        Connection connection = null;
        try
        {
            connection = this.sqlConnectionProvider.getConnection();
            connection.setAutoCommit(false);

            deleteAllFactionMemberRanksMappings(connection, factionName);
            deleteAllFactionMembers(connection, factionName);
            deleteAllRanks(connection, factionName);
            deleteFactionClaims(connection, factionName);
            deleteRelations(connection, factionName, RelationType.ALLIANCE);
            deleteRelations(connection, factionName, RelationType.TRUCE);
            deleteRelations(connection, factionName, RelationType.ENEMY);
            deleteAllRelationsPermissions(connection, factionName);

            this.factionProtectionFlagsStorage.deleteProtectionFlags(connection, factionName);

            final PreparedStatement preparedStatement = connection.prepareStatement(DELETE_FACTION_WHERE_FACTIONNAME);
            preparedStatement.setString(1, factionName);
            final int affectedRows = preparedStatement.executeUpdate();
            connection.commit();
            preparedStatement.close();
            connection.close();
            return affectedRows == 1;
        }
        catch (final Exception e)
        {
            try
            {
                connection.rollback();
                connection.close();
            }
            catch (SQLException e1)
            {
                throw new RuntimeException(e1);
            }
            throw new RuntimeException(e);
        }
    }

    private void deleteAllRelationsPermissions(Connection connection, String factionName) throws SQLException
    {
        final PreparedStatement preparedStatement = connection.prepareStatement(DELETE_RELATION_PERMISSION_WHERE_FACTION_NAME);
        preparedStatement.setString(1, factionName);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    @Override
    public void deleteFactions()
    {
        Set<Faction> factions = getFactions();
        for (final Faction faction : factions)
        {
            deleteFaction(faction.getName());
        }
    }

    private Set<FactionMember> getFactionMembers(final Connection connection, final String factionName) throws SQLException
    {
        Map<UUID, Set<String>> membersWithRanks = new HashMap<>();

        PreparedStatement preparedStatement = connection.prepareStatement(SELECT_MEMBERS_WHERE_FACTIONNAME);
        preparedStatement.setString(1, factionName);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next())
        {
            UUID memberUUID = UUID.fromString(resultSet.getString("member_uuid"));
            Set<String> ranks = membersWithRanks.computeIfAbsent(memberUUID, k -> new HashSet<>());
            ranks.add(resultSet.getString("rank_name"));
        }
        resultSet.close();
        preparedStatement.close();

        return membersWithRanks.entrySet().stream()
                .map(entry -> new FactionMemberImpl(entry.getKey(), entry.getValue()))
                .collect(Collectors.toSet());
    }

    private Set<Claim> getFactionClaims(final Connection connection, final String factionName) throws SQLException
    {
        Set<Claim> claims = new HashSet<>();
        PreparedStatement preparedStatement = connection.prepareStatement(SELECT_CLAIMS_WHERE_FACTIONNAME);
        preparedStatement.setString(1, factionName);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next())
        {
            String worldUUID = resultSet.getString("world_uuid");
            String chunkPositionAsString = resultSet.getString("chunk_position");
            final boolean isAccessibleByFaction = resultSet.getBoolean("is_accessible_by_faction");
            final Vector3i chunkPosition = ClaimTypeSerializer.deserializeVector3i(chunkPositionAsString);
            final Set<UUID> owners = new HashSet<>();

            final PreparedStatement preparedStatement1 = connection.prepareStatement(SELECT_CLAIM_OWNERS_WHERE_WORLD_AND_CHUNK);
            preparedStatement1.setString(1, worldUUID);
            preparedStatement1.setString(2, chunkPositionAsString);
            final ResultSet ownersResultSet = preparedStatement1.executeQuery();
            while (ownersResultSet.next())
            {
                final String playerUniqueIdAsString = ownersResultSet.getString("player_uuid");
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
            byte[] factionChestItems = resultSet.getBytes("chest_items");
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

    private Faction mapToFaction(Connection connection, ResultSet factionsResultSet, String factionName) throws SQLException, IOException, ClassNotFoundException
    {
        final Set<String> alliances = getRelations(connection, factionName, RelationType.ALLIANCE);
        final Set<String> enemies = getRelations(connection, factionName, RelationType.ENEMY);
        final Set<String> truces = getRelations(connection, factionName, RelationType.TRUCE);
        final Set<FactionMember> members = getFactionMembers(connection, factionName);
        final Set<Claim> claims = getFactionClaims(connection, factionName);

        if (factionsResultSet.next())
        {
            final String tag = factionsResultSet.getString("tag");
            final String tagColor = factionsResultSet.getString("tag_color");
            final TextColor textColor = TextColor.fromHexString(tagColor);
            final UUID leaderUUID = Optional.ofNullable(factionsResultSet.getString("leader"))
                    .map(UUID::fromString)
                    .orElse(null);
            final String factionHomeAsString = factionsResultSet.getString("home");
            final String description = factionsResultSet.getString("description");
            final String messageOfTheDay = factionsResultSet.getString("motd");
            final boolean isPublic = factionsResultSet.getBoolean("is_public");
            FactionHome factionHome = null;
            if (factionHomeAsString != null)
                factionHome = FactionHome.from(factionHomeAsString);
            final Instant createdDate = factionsResultSet.getTimestamp("created_date").toInstant();
            final Instant lastOnline = factionsResultSet.getTimestamp("last_online").toInstant();

            final FactionChest factionChest = getFactionChest(connection, factionName);
            final Set<ProtectionFlag> protectionFlags = factionProtectionFlagsStorage.getProtectionFlags(connection, factionName);

            final Set<FactionPermission> alliancePermissions = getRelationPermissions(connection, factionName, RelationType.ALLIANCE);
            final Set<FactionPermission> trucePermissions = getRelationPermissions(connection, factionName, RelationType.TRUCE);

            final List<Rank> ranks = getFactionRanks(connection, factionName);

            final Faction faction = FactionImpl.builder(factionName, Component.text(tag, textColor))
                    .leader(leaderUUID)
                    .home(factionHome)
                    .truces(truces)
                    .alliances(alliances)
                    .enemies(enemies)
                    .claims(claims)
                    .lastOnline(lastOnline)
                    .members(members)
                    .chest(factionChest)
                    .createdDate(createdDate)
                    .ranks(ranks)
                    .alliancePermissions(alliancePermissions)
                    .trucePermissions(trucePermissions)
                    .description(description)
                    .messageOfTheDay(messageOfTheDay)
                    .isPublic(isPublic)
                    .protectionFlags(protectionFlags)
                    .build();
            return faction;
        }
        return null;
    }
}
