package io.github.aquerr.eaglefactions.common.storage.serializers;

import com.google.common.reflect.TypeToken;
import io.github.aquerr.eaglefactions.api.entities.Claim;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class EFTypeSerializers
{
    public static final TypeToken<Claim> CLAIM_TYPE_TOKEN = TypeToken.of(Claim.class);
    public static final TypeToken<Set<Claim>> CLAIM_SET_TYPE_TOKEN = new TypeToken<Set<Claim>>(){};

    public static final TypeToken<List<UUID>> UUID_LIST_TYPE_TOKEN = new TypeToken<List<UUID>>() {};
    public static final TypeToken<Set<UUID>> UUID_SET_TYPE_TOKEN = new TypeToken<Set<UUID>>() {};
}
