package scare.state;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import scare.Input;
import scare.Scare;
import scare.Sound;

/**
 * Game is paused
 *
 * @author mike
 */
public class PauseState extends State
{

    private State previous;
    private Point mouse;
    private int selected = 0;

    public PauseState(State previous)
    {
        this.previous = previous;
    }

    @Override
    public void init(Scare scare)
    {
        super.init(scare);
        removePlayer();
    }

    @Override
    public void tick(Input input)
    {
        mouse = input.getMousePosition();

        if (input.isPressedThisTick(Input.ESCAPE)) {
            Sound.reload.play();
            setState(previous);
        }
        else if (input.isPressedThisTick(Input.MOUSE_ONE)) {
            if (selected == 1) {
                Sound.reload.play();
                setState(previous);
            }
            else if (selected == 2) {
                Sound.death.play();
                setState(new MenuState());
            }
        }
    }

    @Override
    public void paint(Graphics g)
    {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, Scare.GAME_WIDTH, Scare.GAME_HEIGHT);
        g.setColor(Color.WHITE);
        g.drawString("PAUSED", 50, 50);

        int y = 0;
        selected = 0;

        if (mouse != null) {
            y = (int) mouse.getY();
        }

        if (y > 60 && y < 100) {
            g.setColor(Color.RED);
            selected = 1;
        }
        else {
            g.setColor(Color.WHITE);
        }
        g.drawString("Continue ", 50, 80);

        if (y < Scare.GAME_HEIGHT - 20 && y > Scare.GAME_HEIGHT - 60) {
            g.setColor(Color.RED);
            selected = 2;
        }
        else {
            g.setColor(Color.WHITE);
        }
        g.drawString("Exit ", 50, Scare.GAME_HEIGHT - 40);
    }
}
