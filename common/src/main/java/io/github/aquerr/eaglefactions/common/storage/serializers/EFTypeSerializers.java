package io.github.aquerr.eaglefactions.common.storage.serializers;

import com.google.common.reflect.TypeToken;
import io.github.aquerr.eaglefactions.api.entities.Claim;

import java.util.Set;

public class EFTypeSerializers
{
    public static final TypeToken<Claim> CLAIM_TYPE_TOKEN = TypeToken.of(Claim.class);
    public static final TypeToken<Set<Claim>> CLAIM_SET_TYPE_TOKEN = new TypeToken<Set<Claim>>(){};
}
