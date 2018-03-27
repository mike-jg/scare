package scare.entity;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import scare.Input;
import scare.ResourceLoader;
import scare.Scare;
import scare.Sound;
import scare.Stats;
import scare.weapon.GrenadeLauncher;
import scare.weapon.Pistol;
import scare.weapon.RocketLauncher;
import scare.weapon.Shotgun;
import scare.weapon.Weapon;

/**
 * The player
 *
 * @author mike
 */
public class Player extends Entity implements LightEmitter
{

    /**
     * How far light beams travel after a gunshot
     */
    private static final short GUNSHOT_LIGHT_DISTANCE = 500;
    /**
     * How long until light cast from a gunshot will last
     */
    private static final short GUNSHOT_LIGHT_TIME_MILLIS = 1250;
    /**
     * How long until light cast by a gunshot will begin to decay
     */
    private static final short GUNSHOT_LIGHT_DECAY_TIME_MILLIS = 250;

    /**
     * Where the mouse is
     */
    private Point mousePos;
    /**
     * Last time the player fired
     */
    private double lastFire = 0;
    /**
     * Player's sprite
     */
    private BufferedImage sprite;
    /**
     * Player's health
     */
    private int health = 100;
    /**
     * Whether the player is firing
     */
    private boolean shooting = false;
    /**
     * Weapons
     */
    private Weapon[] weapons;
    /**
     * Currently armed weapon
     */
    private Weapon currentWeapon;
    /**
     * Last time the player was hit
     */
    private long lastHit = 0;

    private long lastNoAmmo = 0;

    public Player()
    {
        lastHit = System.currentTimeMillis() - (60 * 1000);
        lastNoAmmo = System.currentTimeMillis() - (60 * 1000);
        weapons = new Weapon[]{
            new Pistol(),
            new Shotgun(),
            new GrenadeLauncher(),
            new RocketLauncher()
        };
        currentWeapon = weapons[0];
        sprite = ResourceLoader.player[0][0];
        w = 10;
        h = 10;
    }

    public void resetFromDead()
    {
        // fill health,
        // add a grenade and a few shells
        health = 100;
        weapons[1].addAmmo(5);
        weapons[2].addAmmo(1);
        setDead(false);
        x = level.getSpawnX();
        y = level.getSpawnY();
    }

    public int getHealth()
    {
        return health;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    /**
     * Work out which sprite to display based on the position of the mouse
     *
     * @param mousepos
     */
    private void selectSprite(Point mousepos)
    {
        if (mousepos == null) {
            return;
        }

        double angleMouse = Math.atan2(y - mousepos.getY(), x - mousepos.getX());
        double mouseDegrees = angleMouse * (180.0 / Math.PI);
        mouseDegrees = (mouseDegrees > 0.0 ? mouseDegrees : (360.0 + mouseDegrees));

        for (int spriteIdx = 0, spriteDegrees = 90; spriteIdx < 8; spriteIdx++, spriteDegrees += 45) {
            for (int deg = spriteDegrees - 23; deg <= spriteDegrees + 23; deg++) {
                int tmp = deg;
                tmp %= 360;

                if (Math.round((float) mouseDegrees) == tmp) {
                    sprite = ResourceLoader.player[spriteIdx][shooting ? 1 : 0];
                    return;
                }
            }
        }
    }

    private void screamInPain()
    {
        Sound.getRandom(Sound.pain).playIfTimeElapsed(500);
    }

    @Override
    public void hit(Entity e)
    {
        if (isDead()) {
            return;
        }
        if (e instanceof Health) {
            health = 100;
        }
        else if (e instanceof Ammo) {
            weapons[1].addAmmo(25);
            weapons[2].addAmmo(15);
            weapons[3].addAmmo(10);
        }
        else if (e instanceof Zombie) {
            lastHit = System.currentTimeMillis();
            level.addScenery(new Splat(this, Splat.Type.Blood));
            level.addScenery(new Splat(this, Splat.Type.Blood));
            level.addScenery(new Splat(this, Splat.Type.Blood));
            health -= 10;
            if (health <= 0) {
                Stats.getStats().playerKilled();
                setDead(true);
                Sound.death.play();
                return;
            }
            screamInPain();
        }
        else if (e instanceof Explosion) {
            lastHit = System.currentTimeMillis();
            level.addScenery(new Splat(this, Splat.Type.Blood));
            level.addScenery(new Splat(this, Splat.Type.Blood));
            level.addScenery(new Splat(this, Splat.Type.Blood));
            // because of the area of effect of explosions,
            // this will probably equate to more like -80 health
            // possibly instant death
            health -= 2;
            if (health <= 0) {
                Stats.getStats().playerKilled();
                setDead(true);
                Sound.death.play();
                return;
            }
            screamInPain();
        }
    }

    @Override
    public void paint(Graphics g)
    {
        g.drawImage(sprite, getX(), getY(), null);

        // draw a damage indicator around the screen
        long currTime = System.currentTimeMillis();
        int diff = (int) (currTime - lastHit);
        if (diff < 1000) {
            int alpha = 255;
            if (diff > 500) {
                alpha = Math.abs((diff - 1000) / 2);
            }

            Color c = new Color(255, 0, 0, alpha);
            Graphics2D g2 = (Graphics2D) g;

            g2.setColor(c);
            g2.setStroke(new BasicStroke(4));
            g2.drawRect(2, 2, Scare.GAME_WIDTH - 9, Scare.GAME_HEIGHT - 4);
        }
    }

    @Override
    public LightEmission emitLight(LightPainter painter)
    {
        double millisSinceLastFire = (System.currentTimeMillis() - lastFire);

        if (millisSinceLastFire < GUNSHOT_LIGHT_TIME_MILLIS) {
            double dyingRayDistanceFactor = 1;
            if (millisSinceLastFire > GUNSHOT_LIGHT_DECAY_TIME_MILLIS) {
                double millisForScale = GUNSHOT_LIGHT_TIME_MILLIS - millisSinceLastFire;
                dyingRayDistanceFactor = (millisForScale / (double) GUNSHOT_LIGHT_TIME_MILLIS);
            }

            if (GUNSHOT_LIGHT_DISTANCE * dyingRayDistanceFactor > FlashLight.BEAM_HALO_RADIUS * FlashLight.BEAM_HALO_NUM) {
                // if fired a shot light up the whole room
                Shape lightShape = painter.cast360(getCentrePoint(), Math.round(GUNSHOT_LIGHT_DISTANCE * dyingRayDistanceFactor));
                return new LightEmission(new Shape[]{lightShape});
            }
        }
        return LightPainter.NULL_LIGHT_EMISSION;
    }

    public Weapon[] getWeapons()
    {
        return weapons;
    }

    public Weapon getCurrentWeapon()
    {
        return currentWeapon;
    }

    @Override
    public void tick(Input input, double delta)
    {
        // check weapon changes
        if (input.isPressedThisTick(Input.ONE)) {
            Sound.armWeapon.play();
            currentWeapon = weapons[0];
        }
        else if (input.isPressedThisTick(Input.TWO)) {
            Sound.armWeapon.play();
            currentWeapon = weapons[1];
        }
        else if (input.isPressedThisTick(Input.THREE)) {
            Sound.armWeapon.play();
            currentWeapon = weapons[2];
        }
        else if (input.isPressedThisTick(Input.FOUR)) {
            Sound.armWeapon.play();
            currentWeapon = weapons[3];
        }

        // check whether the mouse has moved
        boolean newMouse = false;
        boolean newPos = false;

        if (input.getMousePosition() != mousePos && input.getMousePosition() != null) {
            newMouse = true;
            mousePos = input.getMousePosition();
        }

        // check movement keys
        if (input.isPressed(Input.S)) {
            if (isValidMove(getX(), getY() + 1, w, h)) {
                y++;
                newPos = true;
            }
        }
        if (input.isPressed(Input.A)) {
            if (isValidMove(getX() - 1, getY(), w, h)) {
                x--;
                newPos = true;
            }
        }
        if (input.isPressed(Input.D)) {
            if (isValidMove(getX() + 1, getY(), w, h)) {
                x++;
                newPos = true;
            }
        }
        if (input.isPressed(Input.W)) {
            if (isValidMove(getX(), getY() - 1, w, h)) {
                y--;
                newPos = true;
            }
        }

        // check shooting
        double currTime = System.currentTimeMillis();
        boolean oldShooting = shooting;
        if (input.isPressed(Input.SPACE) || input.isPressed(Input.MOUSE_ONE)) {
            if (currentWeapon.canFire()) {
                lastFire = currTime;
                double mouseAngle = Math.atan2(mousePos.getY() - y, mousePos.getX() - x);
                currentWeapon.fire(level, getX() + (w / 2), getY() + (h / 2), mouseAngle);
                shooting = true;
            }

            else if (currentWeapon.getAmmo() == 0 && System.currentTimeMillis() - lastNoAmmo > 250) {
                lastNoAmmo = System.currentTimeMillis();
                Sound.noAmmo.play();
            }
        }
        else if (currTime - lastFire > 250) {
            shooting = false;
        }

        // if mouse has moved, player has moved, or the player has started/stopped firing
        // then select a new sprite
        if (newMouse || newPos || oldShooting != shooting) {
            selectSprite(input.getMousePosition());
        }
    }
}
