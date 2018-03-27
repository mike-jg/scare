package scare.state;

import java.awt.Color;
import java.awt.Graphics;
import scare.Input;
import scare.Scare;
import scare.Stats;

/**
 * Player has won
 *
 * @author mike
 */
public class WonState extends State
{

    @Override
    public void init(Scare scare)
    {
        super.init(scare);
        removePlayer();
    }

    @Override
    public void tick(Input input)
    {
        if (input.isPressedThisTick(Input.SPACE)) {
            setState(new GameState());
        }
    }

    @Override
    public void paint(Graphics g)
    {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, Scare.GAME_WIDTH, Scare.GAME_HEIGHT);
        g.setColor(Color.WHITE);
        g.drawString("You won. Press space to restart.", 50, 50);
        g.drawString("Z kills: " + Stats.getStats().getZKilled(), 50, 80);
        g.drawString("Payer deaths: " + Stats.getStats().getPlayerDeaths(), 50, 105);
        g.drawString("Final score: " + Stats.getStats().getScore(), 50, 135);
    }
}
