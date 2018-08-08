package io.github.aquerr.eaglefactions.entities;

public class RemoveEnemy
{
    private String FactionName;
    private String EnemyFactionName;

    public RemoveEnemy(String factionName, String enemyFaction)
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
        if(!(removeEnemy instanceof RemoveEnemy))
        {
            return false;
        }
        if(removeEnemy == this)
        {
            return true;
        }
        return this.FactionName.equals(((RemoveEnemy) removeEnemy).FactionName) && this.EnemyFactionName.equals(((RemoveEnemy) removeEnemy).EnemyFactionName);
    }

    @Override
    public int hashCode()
    {
        return FactionName.length();
    }
}
