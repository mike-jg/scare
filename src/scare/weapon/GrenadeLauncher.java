package scare.weapon;

import scare.Sound;
import scare.entity.Grenade;
import scare.level.Level;

/**
 * Throws grenades
 *
 * @author mike
 */
public class GrenadeLauncher extends Weapon
{

    public GrenadeLauncher()
    {
        ammo = 25;
    }

    @Override
    public String getDescription()
    {
        return "Grenade";
    }

    @Override
    public void fire(Level level, int x, int y, double angle)
    {
        Sound.grenadeThrow.play();
        level.addEntity(new Grenade(x, y, angle));
        ammo -= 1;
    }

    @Override
    public int getFireDelay()
    {
        return 1000;
    }

    @Override
    public int getAmmo()
    {
        return ammo;
    }

    @Override
    public int getAmmoMax()
    {
        return 25;
    }
}
