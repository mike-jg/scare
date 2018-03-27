package scare.weapon;

import scare.Sound;
import scare.entity.Bullet;
import scare.level.Level;

/**
 * Fires a single bullet
 *
 * @author mike
 */
public class Pistol extends Weapon
{

    public Pistol()
    {
        ammo = AMMO_INFINITE;
    }

    @Override
    public void fire(Level level, int x, int y, double angle)
    {
        Sound.pistol.play();
        level.addEntity(new Bullet(x, y, angle, Bullet.PISTOL_PUSH_AMT));
    }

    @Override
    public int getFireDelay()
    {
        return 235;
    }

    @Override
    public int getAmmo()
    {
        return Weapon.AMMO_INFINITE;
    }

    @Override
    public int getAmmoMax()
    {
        return Weapon.AMMO_INFINITE;
    }

    @Override
    public String getDescription()
    {
        return "Pistol";
    }
}
