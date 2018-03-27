package scare.entity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Shape;
import java.util.ArrayList;
import scare.Input;

/**
 * An explosion
 *
 * @author mike
 */
public class Explosion extends Entity implements LightEmitter
{

    /**
     * Degree (power) of the explosion
     */
    private int degree;
    /**
     * The flames
     */
    private ArrayList<Flame> flames;
    /**
     * Splash area
     */
    private final static int SPLASH = 50;

    private final Point pos;

    private boolean isDying = false;
    private int ticksUntilDeath = 50;
    private boolean emitNoLight;

    public Explosion(int x, int y, boolean emitNoLight)
    {
        this.x = x;
        this.y = y;
        this.w = SPLASH;
        this.h = SPLASH;
        this.degree = EntityUtility.getRandom(80, 120);
        this.flames = new ArrayList<>(200);
        this.emitNoLight = emitNoLight;
        pos = getCentrePoint();
    }

    public Explosion(int x, int y)
    {
        this(x, y, false);
    }

    @Override
    public void paint(Graphics g)
    {
        for (Flame f : flames) {
            f.paint(g);
        }
    }

    @Override
    public void tick(Input input, double delta)
    {
        if (this.isDead()) {
            return;
        }

        if (this.isDying) {
            this.ticksUntilDeath--;
            if (this.ticksUntilDeath < 0) {
                this.setDead(true);
            }
            return;
        }

        // scorch the level
        level.addScenery(new Splat(this, Splat.Type.Scorch));

        // filter out dead flames
        ArrayList<Flame> newFlames = new ArrayList<>(flames.size());

        for (Flame f : flames) {
            f.tick(delta);
            if (f.dead == false) {
                newFlames.add(f);
            }
        }

        this.flames = newFlames;

        // add new flames
        for (int i = 0; i < degree; i++) {
            this.flames.add(new Flame(getX(), getY()));
        }

        // if there are no flames left, then this explosion is dead
        if (this.flames.isEmpty()) {
            this.isDying = true;
            return;
        }

        for (Entity e : level.getEntities()) {
            // hit anything within the splash distance
            if (EntityUtility.distance(this, e) < SPLASH) {
                e.hit(this);
            }
        }
        // degrade the degree
        degree -= (35 * delta);
    }

    @Override
    public LightEmission emitLight(LightPainter painter)
    {
        if (this.emitNoLight) {
            return LightPainter.NULL_LIGHT_EMISSION;
        }

        Shape[] returnShapes = new Shape[3];

        double dyingRayDistanceFactor = 1;
        if (isDying) {
            dyingRayDistanceFactor = (double) ticksUntilDeath / (double) 50;
        }

        for (byte hindex = 0; hindex < returnShapes.length; hindex++) {
            returnShapes[hindex] = painter.cast360(pos, Math.round(hindex * 60 * dyingRayDistanceFactor));
        }

        return new LightEmission(returnShapes, AlphaComposite.getInstance(AlphaComposite.DST_OUT, 0.3f));
    }

    /**
     * A flame
     */
    private class Flame
    {

        private int x, y;
        private Color colour;
        private double born;
        private boolean dead;
        private int life;

        public Flame(int x, int y)
        {
            this.x = x;
            this.y = y;
            // how many milliseconds this lives for
            life = EntityUtility.getRandom(50, 100);
            born = System.currentTimeMillis();

            switch (EntityUtility.nextInt(3)) {
                default:
                case 0:
                    colour = Color.RED;
                    break;
                case 1:
                    colour = Color.YELLOW;
                    break;
                case 2:
                    colour = Color.ORANGE;
                    break;
            }
        }

        public void paint(Graphics g)
        {
            if (dead) {
                return;
            }

            g.setColor(colour);
            g.fillOval(x, y, 2, 2);
        }

        public void tick(double delta)
        {
            if (dead) {
                return;
            }

            if (System.currentTimeMillis() - born > life) {
                dead = true;
                return;
            }

            x += EntityUtility.getRandom((byte) (-15 * delta), (byte) (15 * delta));
            y += EntityUtility.getRandom((byte) (-15 * delta), (byte) (15 * delta));
        }
    }
}
