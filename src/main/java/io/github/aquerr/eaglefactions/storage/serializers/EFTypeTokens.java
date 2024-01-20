package io.github.aquerr.eaglefactions.storage.serializers;

import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.FactionChest;
import io.github.aquerr.eaglefactions.api.entities.FactionMember;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlag;
import io.github.aquerr.eaglefactions.api.entities.Rank;
import io.leangen.geantyref.TypeToken;
import org.spongepowered.math.vector.Vector3i;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class EFTypeTokens
{
    public static final TypeToken<Claim> CLAIM_TYPE_TOKEN = TypeToken.get(Claim.class);
    public static final TypeToken<Set<Claim>> CLAIM_SET_TYPE_TOKEN = new TypeToken<Set<Claim>>(){};

    public static final TypeToken<List<UUID>> UUID_LIST_TYPE_TOKEN = new TypeToken<List<UUID>>() {};
    public static final TypeToken<Set<UUID>> UUID_SET_TYPE_TOKEN = new TypeToken<Set<UUID>>() {};

    public static final TypeToken<List<FactionChest.SlotItem>> LIST_SLOT_ITEM_TYPE_TOKEN = new TypeToken<List<FactionChest.SlotItem>>() {};
    public static final TypeToken<FactionChest.SlotItem> SLOT_ITEM_TYPE_TOKEN = TypeToken.get(FactionChest.SlotItem.class);

    public static final TypeToken<UUID> UUID_TOKEN = TypeToken.get(UUID.class);

    public static final TypeToken<Vector3i> VECTOR_3I_TOKEN = TypeToken.get(Vector3i.class);
    public static final TypeToken<Set<ProtectionFlag>> PROTECTION_FLAGS_SET_TYPE_TOKEN = new TypeToken<Set<ProtectionFlag>>() {};
    public static final TypeToken<ProtectionFlag> PROTECTION_FLAG_TYPE_TOKEN = TypeToken.get(ProtectionFlag.class);

    public static final TypeToken<FactionMember> FACTION_MEMBER_TYPE_TOKEN = TypeToken.get(FactionMember.class);

    public static final TypeToken<Rank> RANK_TYPE_TOKEN = TypeToken.get(Rank.class);

    private EFTypeTokens()
    {

    }
}
