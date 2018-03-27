package scare.weapon;

import scare.level.Level;

/**
 * A weapon
 *
 * @author mike
 */
abstract public class Weapon
{

    protected int ammo;
    private double lastFire = 0;
    public static final int AMMO_INFINITE = 0xdeadbeef;

    abstract public String getDescription();

    abstract public void fire(Level level, int x, int y, double angle);

    abstract public int getFireDelay();

    public boolean canFire()
    {
        if (getAmmo() == 0) {
            return false;
        }
        double currTime = System.currentTimeMillis();
        if (currTime - lastFire > getFireDelay()) {
            lastFire = currTime;
            return true;
        }
        return false;
    }

    public void addAmmo(int amount)
    {
        if (ammo == AMMO_INFINITE) {
            return;
        }

        ammo += amount;

        if (ammo > getAmmoMax()) {
            ammo = getAmmoMax();
        }
    }

    abstract public int getAmmo();

    abstract public int getAmmoMax();
}
