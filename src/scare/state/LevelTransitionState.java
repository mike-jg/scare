package scare.state;

import java.awt.Color;
import java.awt.Graphics;
import scare.Input;
import scare.Scare;
import scare.Stats;
import scare.entity.Player;
import scare.level.Level;

/**
 * Transition from current level to the next level
 *
 * @author mike
 */
public class LevelTransitionState extends State
{

    private State newState;
    private long initTime;
    private int nextLevel;

    public LevelTransitionState(GameState previous)
    {
        initTime = System.currentTimeMillis();
        int prevLevel = previous.getLevel().getLevelNumber();
        if (prevLevel + 1 <= Level.maxLevel) {
            // get player from old level and add it to the new level
            Player p = previous.getLevel().getPlayer();
            nextLevel = prevLevel + 1;
            Level nLevel = new Level(nextLevel, p);
            newState = new GameState(nLevel);
        }
        else {
            newState = new WonState();
        }
    }

    @Override
    public void tick(Input input)
    {
        if (newState instanceof WonState || System.currentTimeMillis() - initTime > 1500) {
            setState(newState);
        }
    }

    @Override
    public void paint(Graphics g)
    {
        if (!(newState instanceof WonState)) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, Scare.GAME_WIDTH, Scare.GAME_HEIGHT);
            g.setColor(Color.WHITE);
            g.drawString("Entering level " + nextLevel + "", 50, 50);
            g.drawString("Score: " + Stats.getStats().getScore(), 50, 80);
            g.drawString("Z kills: " + Stats.getStats().getZKilled(), 50, 105);
        }
    }
}
