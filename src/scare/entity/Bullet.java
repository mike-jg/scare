package scare.entity;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import scare.Input;
import scare.Scare;

/**
 * A bullet which is fired from a pistol or a shotgun
 *
 * @author mike
 */
public class Bullet extends Entity
{

    public static final byte PISTOL_PUSH_AMT = 10;
    public static final byte SHOTGUN_PUSH_AMT = 20;
    private static final byte MOVEMENT_PER_TICK = 9;
    private static final byte BULLET_DAMAGE_DEGRADE_AMOUNT = 10;
    private double angle;
    private byte minDamage;
    private byte currentDamage;
    /**
     * The force of this bullet
     */
    private int push = PISTOL_PUSH_AMT;
    /**
     * How quickly the push should degrade (higher is less frequent)
     *
     * Basically, a recently fired bullet has more 'pushing' power, so will
     * push the zombies back further
     *
     * e.g. every nth tick
     */
    private static final byte PUSH_DEGRADE_TICK_FREQ = 3;
    /**
     * How much the push degrades, if it does
     */
    private static final byte PUSH_DEGRADE_AMOUNT = 2;
    private int pushDegradeTick = 0;
    private double xStep, yStep;

    /**
     *
     * @param x start y
     * @param y start x
     * @param angle the angle in radians that the bullet was fired
     * @param push how much force the bullet has
     */
    public Bullet(int x, int y, double angle, int push)
    {
        this(x, y, angle);
        this.push = push;
    }

    /**
     *
     * @param x start y
     * @param y start x
     * @param angle the angle in radians that the bullet was fired
     */
    public Bullet(int x, int y, double angle)
    {
        this.x = x;
        this.y = y;
        this.angle = angle;
        // bullets do a random damage and degrade back to a given amount
        this.minDamage = EntityUtility.getRandom((byte) 15, (byte) 25);
        this.currentDamage = EntityUtility.getRandom((byte) 75, (byte) 100);

        // pre calculate xStep and yStep
        // so they aren't done every tick
        xStep = Math.cos(angle);
        yStep = Math.sin(angle);
    }

    @Override
    public boolean blocksPath()
    {
        return false;
    }

    public int getPush()
    {
        return push;
    }

    public int getDamage()
    {
        return currentDamage;
    }

    public double getAngle()
    {
        return angle;
    }

    @Override
    public void paint(Graphics g)
    {
        g.setColor(Color.WHITE);
        g.fillOval(getX(), getY(), 2, 2);
    }

    @Override
    public void tick(Input input, double delta)
    {
        if (currentDamage > minDamage) {
            currentDamage -= BULLET_DAMAGE_DEGRADE_AMOUNT;
            if (currentDamage < minDamage) {
                currentDamage = minDamage;
            }
        }

        pushDegradeTick++;

        // degrade the push
        if (push > 1 && pushDegradeTick > 0 && pushDegradeTick % PUSH_DEGRADE_TICK_FREQ == 0) {
            push -= PUSH_DEGRADE_AMOUNT;
            pushDegradeTick = 0;
            if (push < 1) {
                push = 1;
            }
        }

        float xTotal = 0;
        float yTotal = 0;

        boolean hitWall = false;

        // while not dead, move forward a bit
        // this is done in steps because it stops the bullets
        // going through walls
        for (int i = 0; i < MOVEMENT_PER_TICK && !this.isDead(); i++) {
            xTotal += (xStep * delta);
            yTotal += (yStep * delta);

            if (!isPassable(Math.round(x + xTotal), Math.round(y + yTotal), 3, 3)) {
                hitWall = true;
                break;
            }
        }

        checkHit((float) xTotal, (float) yTotal);

        if (hitWall) {
            this.setDead(true);
            return;
        }

        x = x + xTotal;
        y = y + yTotal;
    }

    private void checkHit(float xTotal, float yTotal)
    {
        // this will create a temporary image and draw the nearby entities onto the image,
        // it'll then draw the bullet's path, from its start location to its new location
        // using AlphaComposite.SRC_IN, so only pixels that are common between the entity and the line
        // are filled in

        // the pixels in the temporary image are then checked, if any are filled in in
        // the colour of the line, then we know a hit has taken place
        // this SHOULD mean that the collisions are pixel-perfect
        // start points (current bullet location)
        int startX = getX();
        int startY = getY();
        // end points (projected finish point)
        int endX = Math.round(x + xTotal);
        int endY = Math.round(y + yTotal);

        for (Entity e : level.getEntities()) {
            // inspect only nearby entities
            if (e instanceof Zombie && EntityUtility.distance(startX, startY, e.getX(), e.getY()) < MOVEMENT_PER_TICK + 2) {
                BufferedImage bi = new BufferedImage(Scare.GAME_WIDTH, Scare.GAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gbi = (Graphics2D) bi.createGraphics();

                e.paint(gbi);

                // now set the composite and draw the line
                gbi.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN));

                // magic colour to pick up later
                gbi.setColor(new Color(0x000001));
                gbi.setStroke(new BasicStroke(3));

                // draw the line starting at the current x,y and ending at the projected finish point
                gbi.drawLine(startX, startY, endX, endY);

                // scan the pixels
                for (int xPos = e.getX(), xEnd = e.getX() + e.getW(); xPos < xEnd; xPos++) {
                    for (int yPos = e.getY(), yEnd = e.getY() + e.getH(); yPos < yEnd; yPos++) {
                        // get the pixel from the image and turn off the alpha
                        int pixel = bi.getRGB((int) (xPos), (int) (yPos)) & 0xFFFFFF;
                        // pixel is magic colour then hit
                        if (pixel == 0x000001) {
                            e.hit(this);
                            this.setDead(true);
                            gbi.dispose();
                            return;
                        }
                    }
                }

                gbi.dispose();
            }
        }
    }
}
