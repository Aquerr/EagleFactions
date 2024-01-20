package io.github.aquerr.eaglefactions.storage.sql;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.storage.serializers.InventorySerializer;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class FactionChestSqlHelper
{
    private static final String DELETE_FACTION_CHEST_WHERE_FACTIONNAME = "DELETE FROM faction_chest WHERE faction_name=?";
    private static final String INSERT_CHEST = "INSERT INTO faction_chest (faction_name, chest_items) VALUES (?, ?)";

    public void saveChest(Connection connection, Faction faction) throws SQLException, IOException
    {
        List<DataView> dataViews = InventorySerializer.serializeInventory(faction.getChest().getInventory().inventory());
        final DataContainer dataContainer = DataContainer.createNew(DataView.SafetyMode.ALL_DATA_CLONED);
        dataContainer.set(DataQuery.of("inventory"), dataViews);
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        DataFormats.NBT.get().writeTo(byteArrayStream, dataContainer);
        byteArrayStream.flush();
        byte[] chestBytes = byteArrayStream.toByteArray();
        byteArrayStream.close();

        //Delete chest before
        PreparedStatement preparedStatement = connection.prepareStatement(DELETE_FACTION_CHEST_WHERE_FACTIONNAME);
        preparedStatement.setString(1, faction.getName());
        preparedStatement.executeUpdate();
        preparedStatement.close();

        preparedStatement = connection.prepareStatement(INSERT_CHEST);
        preparedStatement.setString(1, faction.getName());
        preparedStatement.setBytes(2, chestBytes);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }
}
