package scare.state;

import java.awt.Color;
import java.awt.Graphics;
import scare.Input;
import scare.Scare;
import scare.Sound;
import scare.Stats;
import scare.entity.EntityUtility;
import scare.entity.Splat;

/**
 * Player has died
 *
 * @author mike
 */
public class DeadState extends State
{

    private Splat[] splats;
    private GameState previous;

    public DeadState(GameState previous)
    {
        this.previous = previous;
        splats = new Splat[100];

        for (int i = 0; i < splats.length; i++) {
            splats[i] = new Splat(EntityUtility.getRandom(0, Scare.GAME_WIDTH),
                    EntityUtility.getRandom(0, Scare.GAME_HEIGHT), Splat.Type.Blood);
        }
    }

    @Override
    public void tick(Input input)
    {
        if (input.isPressedThisTick(Input.ESCAPE)) {
            Sound.bandage.play();
            previous.getLevel().getPlayer().resetFromDead();
            setState(previous);
        }
    }

    @Override
    public void paint(Graphics g)
    {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, Scare.GAME_WIDTH, Scare.GAME_HEIGHT);

        for (Splat s : splats) {
            s.paint(g);
        }

        g.setColor(Color.WHITE);
        g.drawString("DEAD. Press escape to continue.", 50, 50);
        g.drawString("Score: " + Stats.getStats().getScore(), 50, 80);
        g.drawString("Z kills: " + Stats.getStats().getZKilled(), 50, 105);
    }
}
