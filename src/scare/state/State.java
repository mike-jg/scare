package scare.state;

import java.awt.Graphics;
import scare.Input;
import scare.Scare;
import scare.entity.Player;

/**
 * A game state
 *
 * @author mike
 */
abstract public class State
{

    private Scare scare;

    public void remove()
    {
    }

    public void init(Scare scare)
    {
        this.scare = scare;
    }

    protected void setState(State state)
    {
        this.scare.setState(state);
    }

    protected void setPlayer(Player player)
    {
        this.scare.setPlayer(player);
    }

    protected void removePlayer()
    {
        this.scare.removePlayer();
    }

    protected double getDelta()
    {
        return this.scare.getDelta();
    }

    abstract public void tick(Input input);

    abstract public void paint(Graphics g);
}
