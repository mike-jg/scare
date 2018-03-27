package scare.entity;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import scare.Input;
import scare.Sound;
import scare.level.Level;

/**
 * A dead body
 *
 * @author mike
 */
public class Body extends Entity
{

    private int ticks = 0;
    private BufferedImage sprite;
    private int xVelocity, yVelocity, slideTime;

    /**
     *
     * @param sprite
     * @param x start x
     * @param y start y
     * @param xVelocity x speed
     * @param yVelocity y speed
     * @param slideTime how long to slide for
     */
    public Body(BufferedImage sprite, int x, int y, int xVelocity, int yVelocity, int slideTime)
    {
        this.sprite = sprite;
        this.x = x;
        this.y = y;
        this.w = sprite.getWidth();
        this.h = sprite.getHeight();
        this.xVelocity = xVelocity;
        this.yVelocity = yVelocity;
        this.slideTime = slideTime;
    }

    @Override
    public void paint(Graphics g)
    {
        g.drawImage(sprite, getX(), getY(), null);
    }

    @Override
    public void tick(Input input, double delta)
    {
        // don't do anything if the sliding has finished
        if (slideTime == 0) {
            return;
        }

        // every other tick slide along and leave a trail of blood
        if ((ticks & 1) == 1) {
            level.addScenery(new Splat(this, Splat.Type.Blood));
            level.addScenery(new Splat(this, Splat.Type.Blood));
            slideTime--;
            double actualXVelocity = xVelocity * delta;
            double actualYVelocity = yVelocity * delta;

            // move if we won't hit a wall,
            // otherwise perform wall absorption and
            // reverse velocity
            if (!level.isWall((int) Math.round(x + actualXVelocity), getY(), w, h)) {
                x = (float) (x + actualXVelocity);
            }
            else {
                Sound.getRandom(Sound.gore).play();
                actualXVelocity = (int) -(actualXVelocity * Level.WALL_ABSORPTION);
            }

            if (!level.isWall(getX(), (int) Math.round(y + actualYVelocity), w, h)) {
                y = (float) (y + actualYVelocity);
            }
            else {
                Sound.getRandom(Sound.gore).play();
                yVelocity = (int) -(yVelocity * Level.WALL_ABSORPTION);
            }
        }
        ticks++;
    }
}
