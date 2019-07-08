package io.github.aquerr.eaglefactions.api.entities;

public class StopWarRequest
{
    private String factionName;
    private String enemyFactionName;

    public StopWarRequest(String factionName, String enemyFaction)
    {
        this.factionName = factionName;
        this.enemyFactionName = enemyFaction;
    }

    public String getEnemyFactionName()
    {
        return enemyFactionName;
    }

    public String getFactionName()
    {
        return factionName;
    }

    @Override
    public boolean equals (Object removeEnemy)
    {
        if(!(removeEnemy instanceof StopWarRequest))
        {
            return false;
        }
        if(removeEnemy == this)
        {
            return true;
        }
        return this.factionName.equals(((StopWarRequest) removeEnemy).factionName) && this.enemyFactionName.equals(((StopWarRequest) removeEnemy).enemyFactionName);
    }

    @Override
    public int hashCode()
    {
        return factionName.hashCode();
    }
}
