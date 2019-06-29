package io.github.aquerr.eaglefactions.storage.mysql;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.entities.*;
import io.github.aquerr.eaglefactions.storage.IFactionStorage;
import io.github.aquerr.eaglefactions.storage.InventorySerializer;
import io.github.aquerr.eaglefactions.storage.SqlProvider;
import io.github.aquerr.eaglefactions.storage.h2.H2Provider;
import io.github.aquerr.eaglefactions.storage.sql.AbstractFactionStorage;
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

public class MySQLFactionStorage extends AbstractFactionStorage
{
    public MySQLFactionStorage(final EagleFactions plugin)
    {
        super(plugin, MySQLConnection.getInstance(plugin));
    }
}
