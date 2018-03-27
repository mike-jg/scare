package scare.entity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import scare.Input;
import scare.Sound;
import scare.level.Level;

/**
 * A grenade, this will stick to zombies if it isn't armed
 *
 * @author mike
 */
public class Grenade extends Entity
{

    private float xStep;
    private float yStep;
    /**
     * whether this is armed, if not armed it won't explode, but it'll stick to
     * stuff instead
     */
    private boolean armed;
    /**
     * the entity that this has hit and stuck to
     */
    private Entity stuckTo = null;

    public Grenade(int x, int y, double angle)
    {
        this.x = x;
        this.y = y;
        this.w = 5;
        this.h = 5;
        armed = false;

        xStep = (float) (Math.cos(angle) * 4);
        yStep = (float) (Math.sin(angle) * 4);

        // timer to arm the grenade
        Timer armTimer = new Timer(450, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                armed = true;
            }
        });
        armTimer.setRepeats(false);

        // timer to detonate the grenade
        Timer detonateTimer = new Timer(2000, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                detonate();
            }
        });
        detonateTimer.setRepeats(false);

        armTimer.start();
        detonateTimer.start();
    }

    @Override
    public boolean blocksPath()
    {
        return false;
    }

    @Override
    public void paint(Graphics g)
    {
        g.setColor(new Color(38, 127, 0));
        g.fillOval(getX(), getY(), 5, 5);
    }

    @Override
    public void tick(Input input, double delta)
    {
        if (isDead()) {
            return;
        }

        if (stuckTo != null) {
            this.x = stuckTo.getX();
            this.y = stuckTo.getY();
            return;
        }

        float actualXStep = (float) (xStep * delta);
        float actualYStep = (float) (yStep * delta);

        // check for collisions,
        // if colliding with a wall then absorb some of the velocity
        // and reverse direction
        if (!level.isWall(Math.round(x + actualXStep), getY(), 5, 5)) {
            x = x + actualXStep;
        }
        else {
            Sound.getRandom(Sound.grenadeImpact).play();
            xStep = (float) -(actualXStep * Level.WALL_ABSORPTION);
        }

        if (!level.isWall(getX(), Math.round(y + actualYStep), 5, 5)) {
            y = y + actualYStep;
        }
        else {
            Sound.getRandom(Sound.grenadeImpact).play();
            yStep = (float) -(actualYStep * Level.WALL_ABSORPTION);
        }

        for (Entity e : level.getEntities()) {
            if (e instanceof Zombie || e instanceof Player) {
                double distance = EntityUtility.distance(e, this);
                if (distance < 20 && armed) {
                    detonate();
                    break;
                }
                // don't stick to the player,
                // grenades are hard enough to not kill yourself with as it is
                else if (distance < 20 && stuckTo == null && e instanceof Zombie) {
                    stuckTo = e;
                    xStep = 0;
                    yStep = 0;
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
        level.addEntity(new Explosion(getX() + (getW() / 2), getY() + (getH() / 2)));
    }
}
