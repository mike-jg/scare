package scare;

/**
 * Keep track of play stats and create score
 *
 * @author mike
 */
public class Stats
{

    private static Stats inst;
    private int levelsComplete = 0;
    private int zKills = 0;
    private int deaths = 0;

    /**
     * Reset the stats
     *
     * @return
     */
    public static Stats createNew()
    {
        inst = new Stats();
        return inst;
    }

    /**
     * Get the game stats for the current game
     *
     * @return
     */
    public static Stats getStats()
    {
        if (inst == null) {
            inst = new Stats();
        }

        return inst;
    }

    public void zKilled()
    {
        zKills++;
    }

    public void playerKilled()
    {
        deaths++;
    }

    public int getPlayerDeaths()
    {
        return deaths;
    }

    public int getZKilled()
    {
        return zKills;
    }

    public void levelComplete()
    {
        levelsComplete++;
    }

    public int getLevelsComplete()
    {
        return levelsComplete;
    }

    /**
     * get the final score
     *
     * @return
     */
    public int getScore()
    {
        int zScore = (getZKilled() * 250);

        int levelMod = 500;
        int levelBase = 1000;
        int levelScore = 0;
        for (int i = 0; i < levelsComplete; i++) {
            levelScore += (i * levelMod) + levelBase;
        }

        int deathScore = 500 * deaths;

        return levelScore + zScore - deathScore;
    }

    private Stats()
    {
    }
}
