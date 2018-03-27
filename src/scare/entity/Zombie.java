package scare.entity;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import scare.Input;
import scare.ResourceLoader;
import scare.Sound;
import scare.Stats;
import scare.pathfinder.Mover;
import scare.pathfinder.Path;
import scare.pathfinder.Path.Step;
import scare.pathfinder.PathFinder;
import scare.pathfinder.TileMap;
import scare.pathfinder.astar.AStarHeuristic;
import scare.pathfinder.astar.AStarPathFinder;
import scare.pathfinder.astar.ClosestHeuristic;

/**
 * A zombie
 *
 * @author mike
 */
public class Zombie extends Entity implements Mover
{

    private static final byte MIN_MOV_FREQ = 1;
    private static final byte MAX_MOV_FREQ = 3;

    private static long lastGrowl = 0;
    /**
     * The health
     */
    private byte health;
    /**
     * current sprite
     */
    private BufferedImage sprite;
    /**
     * the sprite family as there are a few different sets
     */
    private BufferedImage[][] spriteFamily;
    /**
     * current state
     */
    private ZState state;
    /**
     * the path finder
     */
    private PathFinder pathFinder;
    private byte tick;
    /**
     * How often to perform a move, this is random so the zombies are more
     * unpredictable
     */
    private byte moveFreq;

    public Zombie(int x, int y, boolean active)
    {
        this.x = x;
        this.y = y;
        this.moveFreq = (byte) EntityUtility.getRandom(MIN_MOV_FREQ, MAX_MOV_FREQ);
        this.health = (byte) EntityUtility.getRandom(75, 125);

        // select a random sprite family
        switch (EntityUtility.nextInt(4)) {
            default:
            case 0:
                spriteFamily = ResourceLoader.z1;
                break;

            case 1:
                spriteFamily = ResourceLoader.z2;
                break;

            case 2:
                spriteFamily = ResourceLoader.z3;
                break;

            case 3:
                spriteFamily = ResourceLoader.z4;
                break;
        }

        sprite = spriteFamily[EntityUtility.nextInt(8)][1];

        this.w = 10;
        this.h = 10;

        if (active) {
            growl();
            state = new ZAttack();
        }
        else {
            state = new ZWaiting();
        }
    }

    private void growl()
    {
        Sound.getRandom(Sound.zombie).play();
    }

    private PathFinder getPathFinder()
    {
        // create a pathfinder, usually has the ZHeuristic but sometimes give them
        // the good heuristic which will make them more dangerous
        pathFinder = new AStarPathFinder(level, health, true,
                EntityUtility.getRandom(0, 3) == 1
                ? new ClosestHeuristic()
                : new ZHeuristic());

        return pathFinder;
    }

    /**
     *
     * @todo make sure blood sprays in right direction
     * @param e
     */
    @Override
    public void hit(Entity e)
    {
        if (isDead()) {
            return;
        }

        if (e instanceof Bullet) {
            collideBullet((Bullet) e);
        }
        if (e instanceof Explosion) {
            collideExplosion((Explosion) e);
        }
    }

    private void collideBullet(Bullet bullet)
    {
        if (state instanceof ZAttack) {
            if (EntityUtility.getRandom(0, 10) < 3) {
                growl();
            }
        } // the player has shot an idle zombie, so make it attack
        else if (!(state instanceof ZAttack)) {
            growl();
            state = new ZAttack();
        }

        health -= bullet.getDamage();
        if (health < 1) {
            // using the bullet push, calculate how far to throw the zombie
            int xPush = (int) (bullet.getPush() * Math.cos(bullet.getAngle()));
            int yPush = (int) (bullet.getPush() * Math.sin(bullet.getAngle()));
            // if the bullet is forceful, randomly explode
            if (bullet.getPush() > 12 && EntityUtility.nextBoolean()) {
                level.addScenery(new Splat(this, bullet.getAngle(), Splat.Type.Blood));
                level.addScenery(new Splat(this, bullet.getAngle(), Splat.Type.Zombie));
                level.addScenery(new Splat(this, bullet.getAngle(), Splat.Type.Zombie));
                level.addScenery(new Splat(this, bullet.getAngle(), Splat.Type.Zombie));
                level.addScenery(new Splat(this, bullet.getAngle(), Splat.Type.Zombie));
                level.addScenery(new Splat(this, bullet.getAngle(), Splat.Type.Zombie));
                level.addScenery(new Splat(this, bullet.getAngle(), Splat.Type.Zombie));
                level.addScenery(new Splat(this, bullet.getAngle(), Splat.Type.Zombie));
                level.addScenery(new Splat(this, bullet.getAngle(), Splat.Type.Zombie));
                level.addScenery(new Splat(this, bullet.getAngle(), Splat.Type.Zombie));
                level.addScenery(new Splat(this, bullet.getAngle(), Splat.Type.Blood));
            }
            else {
                level.addScenery(new Body(spriteFamily[EntityUtility.nextInt(3)][2], getX(), getY(), xPush, yPush, 7));
            }

            Stats.getStats().zKilled();
            this.setDead(true);
            return;
        }

        // zombie is pushed back by the bullet slightly
        int xPush = (int) (bullet.getPush() * Math.cos(bullet.getAngle()));
        int yPush = (int) (bullet.getPush() * Math.sin(bullet.getAngle()));

        while (Math.abs(xPush) > 3 && !isPassable(getX() + xPush, getY(), w, h)) {
            xPush *= 0.5;
        }
        if (Math.abs(xPush) > 0) {
            x = x + xPush;
        }

        while (Math.abs(yPush) > 3 && !isPassable(getX(), getY() + yPush, w, h)) {
            yPush *= 0.5;
        }
        if (Math.abs(yPush) > 0) {
            y = y + yPush;
        }

        level.addScenery(new Splat(this, bullet.getAngle(), Splat.Type.Blood));
        level.addScenery(new Splat(this, bullet.getAngle(), Splat.Type.Blood));
    }

    private void collideExplosion(Explosion explosion)
    {
        this.health = 0;
        Stats.getStats().zKilled();
        this.setDead(true);
        // randomly gib the zombie
        if (EntityUtility.nextBoolean() && EntityUtility.nextBoolean()) {
            Sound.bigGore.play();
            level.addScenery(new Splat(this, Splat.Type.Zombie));
            level.addScenery(new Splat(this, Splat.Type.Zombie));
            level.addScenery(new Splat(this, Splat.Type.Blood));
            level.addScenery(new Splat(this, Splat.Type.Blood));
            level.addScenery(new Splat(this, Splat.Type.Zombie));
            level.addScenery(new Splat(this, Splat.Type.Zombie));
            level.addScenery(new Splat(this, Splat.Type.Blood));
            level.addScenery(new Splat(this, Splat.Type.Blood));
        }
        else {
            Sound.getRandom(Sound.gore).play();
            // create a body and send it flying
            level.addScenery(new Splat(this, Splat.Type.Blood));
            level.addScenery(new Splat(this, Splat.Type.Blood));
            double explosionAngle = Math.atan2(y - explosion.getY(), x - explosion.getX());
            int xPush = (int) (EntityUtility.getRandom(18, 25) * Math.cos(explosionAngle));
            int yPush = (int) (EntityUtility.getRandom(18, 25) * Math.sin(explosionAngle));
            level.addScenery(new Body(spriteFamily[EntityUtility.nextInt(3)][2], getX(), getY(), xPush, yPush, 15));
        }
    }

    @Override
    public void paint(Graphics g)
    {
        if (isDead()) {
            return;
        }
        g.drawImage(sprite, getX(), getY(), null);
    }

    @Override
    public void tick(Input input, double delta)
    {
        if (isDead()) {
            return;
        }

        if (lastGrowl - System.currentTimeMillis() > 2400 && EntityUtility.getRandom(0, 2000) == 1) {
            lastGrowl = System.currentTimeMillis();
            growl();
        }

        state.tick(input);
    }

    /**
     * Whether this zombie can see the player
     *
     * @return
     */
    private boolean canSeePlayer()
    {
        Player player = level.getPlayer();

        //z a, player b
        // try to cast a line to the player to see if it's visible
        double angle = Math.atan2(player.getY() - y, player.getX() - x);

        Point p = new Point();
        Point lastP = null;

        for (int distance = 1, dTotal = (int) EntityUtility.distance(this, player) + 10; distance < dTotal; distance++) {

            p.setLocation((int) (x + distance * Math.cos(angle)),
                    (int) (y + distance * Math.sin(angle)));

            if (lastP != null && isPassable((int) lastP.getX(), (int) lastP.getY(), 1, 1)
                    && !isPassable((int) p.getX(), (int) p.getY(), 1, 1)) {
                return false;
            }
            if (lastP == null) {
                lastP = new Point(p);
            }
            else {
                lastP.setLocation(p);
            }
        }

        return true;
    }

    /**
     * Can this zombie move this tick?
     *
     * @return
     */
    private boolean isMovableTick()
    {
        if (tick > 0 && tick % moveFreq == 0) {
            tick = 0;
            return true;
        }
        tick++;
        return false;
    }

    /**
     * Decide which sprite to use
     *
     * @param north
     * @param east
     * @param south
     * @param west
     */
    private void determineSprite(boolean north, boolean east, boolean south, boolean west)
    {
        int spriteIdx = -1;

        if (north && east) {
            spriteIdx = 1;
        }
        else if (east && south) {
            spriteIdx = 3;
        }
        else if (south && west) {
            spriteIdx = 5;
        }
        else if (west && north) {
            spriteIdx = 7;
        }
        else if (north) {
            spriteIdx = 0;
        }
        else if (east) {
            spriteIdx = 2;
        }
        else if (south) {
            spriteIdx = 4;
        }
        else if (west) {
            spriteIdx = 6;
        }

        if (spriteIdx < 0) {
            return;
        }

        if (sprite != spriteFamily[spriteIdx][0]);
        {
            sprite = spriteFamily[spriteIdx][0];
        }
    }

    /**
     * Handle creating/following paths
     */
    private class PathHandler
    {

        private Path path = null;
        private int currStepIdx = 0;
        private Step step;
        private int targetX, targetY;

        public void createPath(int targetX, int targetY)
        {
            path = getPathFinder().findPath(Zombie.this, getX() / 10, getY() / 10,
                    targetX, targetY);

            this.targetX = targetX;
            this.targetY = targetY;

            if (path != null) {
                currStepIdx = 0;
                step = path.getStep(currStepIdx);
            }
        }

        /**
         * follow the path
         */
        public void seekTarget()
        {
            if (step == null) {
                step = null;
                currStepIdx++;
                if (currStepIdx < path.getLength()) {
                    step = path.getStep(currStepIdx);
                }
            }
            else {
                boolean north = false;
                boolean east = false;
                boolean south = false;
                boolean west = false;

                if (step.getY() * 10 > y && isValidMove(getX(), getY() + 1, w, h)) {
                    south = true;
                    y += 1;
                }
                else if (step.getY() * 10 < y && isValidMove(getX(), getY() - 1, w, h)) {
                    north = true;
                    y -= 1;
                }

                if (step.getX() * 10 > x && isValidMove(getX() + 1, getY(), w, h)) {
                    east = true;
                    x += 1;
                }
                else if (step.getX() * 10 < x && isValidMove(getX() - 1, getY(), w, h)) {
                    west = true;
                    x -= 1;
                }

                determineSprite(north, east, south, west);

                if (step.getX() * 10 == x && step.getY() * 10 == y) {
                    step = null;
                    currStepIdx++;
                    if (currStepIdx < path.getLength()) {
                        step = path.getStep(currStepIdx);
                    }
                }
            }
        }

        public boolean hasPath()
        {
            return path != null;
        }

        public boolean hasReachedTarget()
        {
            return y / 10 == targetY && x / 10 == targetX;
        }
    }

    /**
     * A heuristic to return random values, this makes the zombies shamble about
     * unpredictably 2
     */
    private class ZHeuristic implements AStarHeuristic
    {

        @Override
        public float getCost(TileMap map, Mover mover, int x, int y, int tx, int ty)
        {
            return EntityUtility.getRandom(1, 5);
        }
    }

    /**
     * behaviour state
     */
    private abstract class ZState
    {

        public abstract void tick(Input input);
    }

    /**
     * Just standing idle, occasionally looking to see if the player is visible
     */
    private class ZWaiting extends ZState
    {

        private long lastTick = 0;

        @Override
        public void tick(Input input)
        {
            long currTick = System.currentTimeMillis();

            if (currTick - lastTick < 500) {
                return;
            }
            lastTick = currTick;

            if (canSeePlayer()) {
                if (EntityUtility.nextInt(100) < 50) {
                    state = new ZAttack();
                    return;
                }
            }
        }
    }

    /**
     * Moving towards the player
     */
    private class ZAttack extends ZState
    {

        private long lastHit = 0;
        private long lastPath = 0;
        private int px = 0, py = 0;
        private final PathHandler ph;

        public ZAttack()
        {
            ph = new PathHandler();
        }

        @Override
        public void tick(Input input)
        {
            if (!isMovableTick()) {
                return;
            }

            Player player = level.getPlayer();
            boolean pathedThisTick = false;

            // don't know the player location yet so get it
            if (px == 0 && py == 0) {
                px = player.getX();
                py = player.getY();
            }

            long now = System.currentTimeMillis();

            // we have a path, but haven't reached the target yet
            if (ph.hasPath() && !ph.hasReachedTarget()) {
                ph.seekTarget();
            } // don't have a path yet,
            // but also check that it's been a while since last creating a path,
            // otherwise the zombies might spam the path generation
            else if (!ph.hasPath() && now - lastPath > 3000) {
                ph.createPath((player.getX() + (player.getW() / 2)) / 10,
                        (player.getY() + (player.getH() / 2)) / 10);
                lastPath = now;
                pathedThisTick = true;
            }

            // if player is close, hit
            if (EntityUtility.distance(Zombie.this, player) < 22) {
                // allow one hit every 350ms

                if (now - lastHit > 350) {
                    lastHit = now;
                    player.hit(Zombie.this);
                }
            }

            // player has moved
            if (!pathedThisTick && (px != player.getX() || py != player.getY())) {
                // if it's been a sensible time since last pathing,
                // create a new path to the players new location
                if (now - lastPath > 3500) {
                    px = player.getX();
                    py = player.getY();
                    lastPath = now;
                    pathedThisTick = true;
                    ph.createPath((level.getPlayer().getX() + (level.getPlayer().getW() / 2)) / 10,
                            (level.getPlayer().getY() + (level.getPlayer().getH() / 2)) / 10);
                }
            }
        }
    }
}
