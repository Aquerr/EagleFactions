package io.github.aquerr.eaglefactions.entities;

public class StopWarRequest
{
    private String FactionName;
    private String EnemyFactionName;

    public StopWarRequest(String factionName, String enemyFaction)
    {
        this.FactionName = factionName;
        this.EnemyFactionName = enemyFaction;
    }

    public String getEnemyFactionName()
    {
        return EnemyFactionName;
    }

    public String getFactionName()
    {
        return FactionName;
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
        return this.FactionName.equals(((StopWarRequest) removeEnemy).FactionName) && this.EnemyFactionName.equals(((StopWarRequest) removeEnemy).EnemyFactionName);
    }

    @Override
    public int hashCode()
    {
        return FactionName.length();
    }
}
