package io.github.aquerr.eaglefactions.storage.serializers;

import com.google.common.reflect.TypeToken;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.FactionChest;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class EFTypeSerializers
{
    public static final TypeToken<Claim> CLAIM_TYPE_TOKEN = TypeToken.of(Claim.class);
    public static final TypeToken<Set<Claim>> CLAIM_SET_TYPE_TOKEN = new TypeToken<Set<Claim>>(){};

    public static final TypeToken<List<UUID>> UUID_LIST_TYPE_TOKEN = new TypeToken<List<UUID>>() {};
    public static final TypeToken<Set<UUID>> UUID_SET_TYPE_TOKEN = new TypeToken<Set<UUID>>() {};

    public static final TypeToken<List<FactionChest.SlotItem>> LIST_SLOT_ITEM_TYPE_TOKEN = new TypeToken<List<FactionChest.SlotItem>>() {};
    public static final TypeToken<FactionChest.SlotItem> SLOT_ITEM_TYPE_TOKEN = TypeToken.of(FactionChest.SlotItem.class);
}
