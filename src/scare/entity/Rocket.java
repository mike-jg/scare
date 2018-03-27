package scare.entity;

import java.awt.Color;
import java.awt.Graphics;
import scare.Input;
import scare.Sound;

/**
 *
 * @author Mike
 */
public class Rocket extends Entity
{

    private final float xStep;
    private final float yStep;

    public Rocket(int x, int y, double angle)
    {
        this.x = x;
        this.y = y;
        this.w = 3;
        this.h = 3;

        xStep = (float) (Math.cos(angle) * 15);
        yStep = (float) (Math.sin(angle) * 15);
    }

    @Override
    public boolean blocksPath()
    {
        return false;
    }

    @Override
    public void paint(Graphics g)
    {
        g.setColor(new Color(255, 255, 255));
        g.fillOval(getX(), getY(), w, h);
    }

    @Override
    public void tick(Input input, double delta)
    {
        if (isDead()) {
            return;
        }

        float actualXStep = (float) (xStep * delta);
        float actualYStep = (float) (yStep * delta);

        if (!level.isWall(Math.round(x + actualXStep), getY(), 5, 5)) {
            x = x + actualXStep;
        }
        else {
            detonate();
            return;
        }

        if (!level.isWall(getX(), Math.round(y + actualYStep), 5, 5)) {
            y = y + actualYStep;
        }
        else {
            detonate();
            return;
        }

        for (Entity e : level.getEntities()) {
            if (e instanceof Zombie) {
                double distance = EntityUtility.distance(e, this);
                if (distance < 40) {
                    detonate();
                    break;
                }
            }
        }
    }

    private void detonate()
    {
        if (isDead()) {
            return;
        }
        Sound.getRandom(Sound.explode).play();
        setDead(true);
        level.addEntity(new Explosion(getX() - 10, getY() - 10));
        level.addEntity(new Explosion(getX() + 10, getY() + 10, false));
        level.addEntity(new Explosion(getX() - 10, getY() + 10, false));
        level.addEntity(new Explosion(getX() + 10, getY() - 10, false));
    }
}
