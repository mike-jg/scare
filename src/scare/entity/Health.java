package scare.entity;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import scare.Input;
import scare.ResourceLoader;
import scare.Sound;

/**
 *
 * @author mike
 */
public class Health extends Entity
{

    private BufferedImage sprite;
    private byte tickSincePlayerCheck = 0;

    public Health(int x, int y)
    {
        this.x = x;
        this.y = y;
        this.w = 12;
        this.h = 12;
        sprite = ResourceLoader.misc[0][0];
    }

    @Override
    public boolean blocksPath()
    {
        return false;
    }

    @Override
    public void paint(Graphics g)
    {
        if (this.isDead()) {
            return;
        }
        g.drawImage(sprite, getX(), getY(), null);
    }

    @Override
    public void tick(Input input, double delta)
    {
        if (this.isDead()) {
            return;
        }
        if (tickSincePlayerCheck > 0 && tickSincePlayerCheck % 15 == 0) {
            tickSincePlayerCheck = 0;

            Player p = level.getPlayer();

            if (EntityUtility.distance(this, p) < 25) {
                Sound.bandage.play();
                this.setDead(true);
                p.hit(this);
            }
        }

        tickSincePlayerCheck++;
    }
}
