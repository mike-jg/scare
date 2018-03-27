package scare.level;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import scare.Input;
import scare.ResourceLoader;
import scare.entity.Ammo;
import scare.entity.Entity;
import scare.entity.EntityUtility;
import scare.entity.FlashLight;
import scare.entity.Health;
import scare.entity.LightEmitter;
import scare.entity.LightPainter;
import scare.entity.Player;
import scare.entity.Scenery;
import scare.entity.Splat;
import scare.entity.Zombie;
import scare.pathfinder.Mover;
import scare.pathfinder.TileMap;

/**
 * a level
 *
 * The X,Y coordinates are represented here as integers in the grid 64x48
 *
 * Other entities internally store their X,Y as floats in the grid 640x480 for
 * more precise movements, so they have to be converted to ints and divided by
 * 10
 *
 * @author mike
 */
public class Level implements TileMap
{

    public final static int maxLevel = 7;
    public final static int minLevel = 1;
    private int levelNumber;
    private int spawnX;
    private int spawnY;
    /**
     * level representation
     */
    private byte[] level;
    private int width;
    private int height;
    /**
     * scenery
     */
    public static final int Spawn = 1;
    public static final int Wall = 2;
    public static final int Floor = 3;
    public static final int Win = 4;
    /**
     * map colour keys
     */
    /**
     * floor, a passable area
     */
    public static final int C_FLOOR = 0xFFFFFF;
    /**
     * wall
     */
    public static final int C_WALL = 0x000000;
    /**
     * player spawn
     */
    public static final int C_SPAWN = 0xFFD800;
    /**
     * idle zombie (begins with ZWaiting state)
     */
    public static final int C_ZOMB = 0xB6FF00;
    /**
     * active zombie (begins in ZAttack state)
     */
    public static final int C_ZOMB_ACTIVE = 0x38FF5F;
    /**
     * ammo pickup
     */
    public static final int C_AMMO = 0x00FFFF;
    /**
     * health pickup
     */
    public static final int C_HEALTH = 0xFF0000;
    /**
     * win tile (level ends when player reaches this)
     */
    public static final int C_WIN = 0xF700D7;
    /**
     * pentagram decoration
     */
    public static final int C_PENTAGRAM = 0x949494;
    private Player player;
    private LightPainter lightPainter;
    private ArrayList<Entity> entities;
    private ArrayList<Entity> scenery;
    /**
     * these buffers are so that entities can place new scenery/entities in the
     * level during the tick loop, without causing a concurrent modification
     * exception
     *
     * basically, at the end of the loop, each entity in these buffers is placed
     * into the active list, then the buffers are emptied
     */
    private ArrayList<Entity> entityBuffer;
    private ArrayList<Entity> sceneryBuffer;
    public static double WALL_ABSORPTION = 0.5;

    public Level(int number, Player player)
    {
        if (number < minLevel || number > maxLevel) {
            number = 1;
        }

        this.player = player;
        this.levelNumber = number;
        lightPainter = new LightPainter();
        lightPainter.setLevel(this);

        String name = "/scare/resource/" + number + ".png";

        BufferedImage img = ResourceLoader.loadImage(name);

        width = img.getWidth();
        height = img.getHeight();

        int[] pixels = new int[width * height];

        level = new byte[width * height];

        img.getRGB(0, 0, width, height, pixels, 0, width);

        sceneryBuffer = new ArrayList<>();
        scenery = new ArrayList<>();
        entityBuffer = new ArrayList<>();
        entities = new ArrayList<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // eliminate alpha
                int noAlpha = pixels[x + y * width] & 0xFFFFFF;
                doTile(noAlpha, x, y);
            }
        }
    }

    private void doTile(int pixel, int x, int y)
    {
        if (pixel == C_FLOOR) {
            level[x + y * width] = Floor;
        }
        else if (pixel == C_WALL) {
            level[x + y * width] = Wall;
        }
        else if (pixel == C_ZOMB) {
            addEntity(new Zombie(x * 10, y * 10, false));
            level[x + y * width] = Floor;
        }
        else if (pixel == C_ZOMB_ACTIVE) {
            addEntity(new Zombie(x * 10, y * 10, true));
            level[x + y * width] = Floor;
        }
        else if (pixel == C_AMMO) {
            level[x + y * width] = Floor;
            addEntity(new Ammo(x * 10, y * 10));
        }
        else if (pixel == C_HEALTH) {
            level[x + y * width] = Floor;
            addEntity(new Health(x * 10, y * 10));
        }
        else if (pixel == C_SPAWN) {
            spawnX = x * 10;
            spawnY = y * 10;
            level[x + y * width] = Floor;
            player.setX(x * 10);
            player.setY(y * 10);
            entities.add(player);
            FlashLight fl = new FlashLight(player);
            fl.setLevel(this);
            entities.add(fl);
            player.setLevel(this);
        }
        else if (pixel == C_WIN) {
            level[x + y * width] = Win;
        }
        else if (pixel == C_PENTAGRAM) {
            int stX = x * 10;
            int stY = y * 10;

            addScenery(new Scenery(ResourceLoader.misc[6][0], stX, stY));
            addScenery(new Scenery(ResourceLoader.misc[7][0], stX + 16, stY));
            addScenery(new Scenery(ResourceLoader.misc[6][1], stX, stY + 16));
            addScenery(new Scenery(ResourceLoader.misc[7][1], stX + 16, stY + 16));
            level[x + y * width] = Floor;
        }
        else {
            throw new RuntimeException("Invalid tile colour: " + Integer.toHexString(pixel) + " at position " + x + " " + y);
        }

        if (EntityUtility.nextInt(EntityUtility.getRandom(150, 200)) == 1) {
            addScenery(new Splat(x * 10, y * 10, Splat.Type.Scorch));
        }
        if (EntityUtility.nextInt(EntityUtility.getRandom(200, 250)) == 1) {
            addScenery(new Splat(x * 10, y * 10, Splat.Type.Blood));
        }
        if (EntityUtility.nextInt(EntityUtility.getRandom(90, 110)) == 1) {
            addScenery(new Scenery(ResourceLoader.misc[EntityUtility.getRandom(2, 5)][0], x * 10, y * 10));
        }
        if (EntityUtility.nextInt(EntityUtility.getRandom(190, 250)) == 1) {
            addScenery(new Scenery(ResourceLoader.misc[EntityUtility.getRandom(0, 5)][1], x * 10, y * 10));
        }
    }

    public int getLevelNumber()
    {
        return levelNumber;
    }

    public void setPlayer(Player player)
    {
        player.setLevel(this);
        entityBuffer.add(player);
    }

    public Player getPlayer()
    {
        return this.player;
    }

    /**
     * is there a wall occupying this space
     *
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     */
    public boolean isWall(int x, int y, int w, int h)
    {
        return isTileArea(x, y, w, h, Wall);
    }

    /**
     * Is this exact point a wall
     *
     * @param x
     * @param y
     * @return
     */
    public boolean isWall(int x, int y)
    {
        return isTile(x, y, Wall);
    }

    /**
     * Does a particular tile appear within the given area
     *
     * @param x
     * @param y
     * @param w
     * @param h
     * @param tile
     * @return
     */
    private boolean isTileArea(int x, int y, int w, int h, int tile)
    {
        if (isTile(x, y, tile)) {
            return true;
        }

        for (int wAdd = 1; wAdd <= w; wAdd++) {
            for (int hAdd = 1; hAdd <= h; hAdd++) {
                if (isTile(x + wAdd, y, tile)) {
                    return true;
                }

                if (isTile(x, y + hAdd, tile)) {
                    return true;
                }

                if (isTile(x + wAdd, y + hAdd, tile)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Does this exact location contain a particular tile
     *
     * @param x
     * @param y
     * @param tile
     * @return
     */
    private boolean isTile(int x, int y, int tile)
    {
        // out of bounds check, sometimes a particle will move fast
        // and break out of the level
        // this doesn't really matter, as the particle will be killed off

        // this check just regards out of bounds cells as walls and nothing else
        int cell = (x / 10) + (y / 10) * width;
        if (cell >= level.length || cell < 0) {
            if (tile == Wall) {
                return true;
            }
            return false;
        }
        return level[cell] == tile;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public int getSpawnX()
    {
        return spawnX;
    }

    public int getSpawnY()
    {
        return spawnY;
    }

    /**
     * Add scenery entity (scenery is not collided with)
     *
     * @param e
     */
    public void addScenery(Entity e)
    {
        e.setLevel(this);
        sceneryBuffer.add(e);
    }

    /**
     * Add game entity
     *
     * @param e
     */
    public void addEntity(Entity e)
    {
        e.setLevel(this);
        entityBuffer.add(e);
    }

    /**
     * Get the game entities
     *
     * @return
     */
    public List<Entity> getEntities()
    {
        return entities;
    }

    public List<LightEmitter> getLightEmittingEntities()
    {
        List<LightEmitter> emitters = new ArrayList<>();
        for (Entity e : entities) {
            if (e instanceof LightEmitter) {
                emitters.add((LightEmitter) e);
            }
        }
        return emitters;
    }

    /**
     * Has the player won?
     *
     * @return
     */
    public boolean hasWon()
    {
        return isTileArea(player.getX(), player.getY(), player.getW(), player.getH(), Win);
    }

    public void paint(Graphics g)
    {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int tile = level[x + y * width];

                switch (tile) {
                    case Wall:
                        g.setColor(Color.BLACK);
                        g.fillRect(x * 10, y * 10, 10, 10);
                        break;

                    case Spawn:
                    case Floor:
                        g.setColor(Color.GRAY);
                        g.fillRect(x * 10, y * 10, 10, 10);
                        break;

                    case Win:
                        g.setColor(new Color(0xF700D7));
                        g.fillRect(x * 10, y * 10, 10, 10);
                        break;
                }
            }
        }

        for (Entity e : scenery) {
            e.paint(g);
        }

        for (Entity e : entities) {
            if (!(e instanceof Player)) {
                e.paint(g);
            }
        }

        if (player != null) {
            player.paint(g);
        }
        // paint light last so it's painted over everything else
        lightPainter.paint(g);
    }

    public void tick(Input input, double delta)
    {
        for (Entity e : scenery) {
            e.tick(input, delta);
        }

        // add alive entities to the entity buffer
        // so they'l be readded once the entity list is cleared
        // tick all of the entities in the process
        // fire off all of the light emitters too
        for (Entity e : entities) {
            if (!(e instanceof Player)) {
                e.tick(input, delta);
            }
            if (!e.isDead()) {
                entityBuffer.add(e);
            }
        }

        // clear the entities, to remove dead ones
        entities.clear();

        // take everything from the buffer and place it in
        // the entity list,
        // then repeat for scenery
        if (!entityBuffer.isEmpty()) {
            entities.addAll(entityBuffer);
            entityBuffer.clear();
        }
        if (!sceneryBuffer.isEmpty()) {
            scenery.addAll(sceneryBuffer);
            sceneryBuffer.clear();
        }

        if (player != null) {
            player.tick(input, delta);
        }
    }

    /**
     * tile map interface *
     */
    @Override
    public int getWidthInTiles()
    {
        return getWidth();
    }

    @Override
    public int getHeightInTiles()
    {
        return getHeight();
    }

    @Override
    public boolean blocked(Mover mover, int x, int y)
    {
        Entity e = (Entity) mover;
        return isWall(x * 10, y * 10, e.getW(), e.getH());
    }

    @Override
    public float getCost(Mover mover, int sx, int sy, int tx, int ty)
    {
        return 1;
    }

    @Override
    public void pathFinderVisited(int x, int y)
    {
    }
}
