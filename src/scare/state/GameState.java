package scare.state;

import java.awt.Graphics;
import scare.Input;
import scare.Scare;
import scare.Stats;
import scare.entity.Player;
import scare.level.Level;

/**
 * Playing the game
 *
 * @author mike
 */
public class GameState extends State
{

    private Level level;

    public GameState()
    {
        level = new Level(-1, new Player());
        Stats.createNew();
    }

    public GameState(Level level)
    {
        this.level = level;
    }

    @Override
    public void init(Scare scare)
    {
        super.init(scare);
        setPlayer(level.getPlayer());
    }

    public Level getLevel()
    {
        return level;
    }

    public void setLevel(Level level)
    {
        this.level = level;
        setPlayer(level.getPlayer());
    }

    @Override
    public void tick(Input input)
    {
        if (level.getPlayer().isDead()) {
            removePlayer();
            setState(new DeadState(this));
            return;
        }

        if (level.hasWon()) {
            removePlayer();
            Stats.getStats().levelComplete();
            setState(new LevelTransitionState(this));
            return;
        }

        if (input.isPressedThisTick(Input.ESCAPE)) {
            removePlayer();
            setState(new PauseState(this));
            return;
        }

        level.tick(input, getDelta());
    }

    @Override
    public void paint(Graphics g)
    {
        level.paint(g);
    }
}
