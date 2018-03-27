package scare.entity;

import java.awt.Graphics;
import java.awt.Point;
import scare.Input;
import scare.Scare;
import scare.level.Level;

/**
 * An entity is something which exists in the game
 *
 * @author mike
 */
abstract public class Entity
{

    protected Level level;
    private boolean dead = false;
    protected float x, y;
    protected int w, h;
    private Point centrePoint;

    public void setLevel(Level level)
    {
        this.level = level;
    }

    public int getX()
    {
        return Math.round(x);
    }

    public int getY()
    {
        return Math.round(y);
    }

    public Point getCentrePoint()
    {
        if (centrePoint == null) {
            centrePoint = new Point(getX() + (w / 2), getY() + (h / 2));
            return centrePoint;
        }
        centrePoint.setLocation(getX() + (w / 2), getY() + (h / 2));
        return centrePoint;
    }

    public int getW()
    {
        return w;
    }

    public int getH()
    {
        return h;
    }

    public boolean isDead()
    {
        return dead;
    }

    /**
     * An entity has collided with this entity
     *
     * @param e
     */
    public void hit(Entity e)
    {
    }

    public void setDead(boolean dead)
    {
        this.dead = dead;
    }

    /**
     * whether this blocks movement
     *
     * @return
     */
    public boolean blocksPath()
    {
        return true;
    }

    abstract public void paint(Graphics g);

    abstract public void tick(Input input, double delta);

    /**
     * Is a tile passable to an entity (no walls)
     *
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     */
    protected boolean isPassable(int x, int y, int w, int h)
    {
        if (x > Scare.GAME_WIDTH || x < 0) {
            return false;
        }
        if (y > Scare.GAME_HEIGHT || y < 0) {
            return false;
        }

        return !level.isWall(x, y, w, h);
    }

    /**
     * is a tile blocked by another entity
     *
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     */
    protected boolean isBlockedByEntity(int x, int y, int w, int h)
    {
        for (Entity e : level.getEntities()) {
            if (!((e instanceof Zombie) && (this instanceof Zombie)) && (!(e instanceof Explosion)) && e.blocksPath() && e != this) {
                if (EntityUtility.distance(this, e) < ((w + h) / 2) + ((e.getW() + e.getH()) / 2)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Is this a valid move, this is a combination of isPassable and
     * isBlockedByEntity
     *
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     */
    protected boolean isValidMove(int x, int y, int w, int h)
    {
        return isPassable(x, y, w, h) && !isBlockedByEntity(x, y, w, h);
    }
}
