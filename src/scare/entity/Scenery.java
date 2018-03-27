package scare.entity;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import scare.Input;

/**
 *
 * @author mike
 */
public class Scenery extends Entity
{

    private BufferedImage sprite;

    public Scenery(BufferedImage sprite, int x, int y)
    {
        this.sprite = sprite;
        this.x = x;
        this.y = y;
    }

    @Override
    protected boolean isPassable(int x, int y, int w, int h)
    {
        return true;
    }

    @Override
    public void paint(Graphics g)
    {
        g.drawImage(sprite, getX(), getY(), null);
    }

    @Override
    public void tick(Input input, double delta)
    {
    }
}
