package io.github.aquerr.eaglefactions.entities;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Chunk;

import java.util.List;
import java.util.UUID;

/**
 * Created by Aquerr on 2017-07-13.
 */
public class Faction
{
    public String Name;
    public Integer Power;
    public List<Player> Members;
    public UUID Leader;
    public List<Player> Officers;
    public List<Chunk> Claims;
}
