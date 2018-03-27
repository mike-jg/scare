package scare.weapon;

import scare.Sound;
import scare.entity.Rocket;
import scare.level.Level;

/**
 *
 * @author Mike
 */
public class RocketLauncher extends Weapon
{

    public RocketLauncher()
    {
        ammo = 10;
    }

    @Override
    public void fire(Level level, int x, int y, double angle)
    {
        Sound.rocketLauncher.play();
        level.addEntity(new Rocket(x, y, angle));
        ammo -= 1;
    }

    @Override
    public int getFireDelay()
    {
        return 2000;
    }

    @Override
    public int getAmmo()
    {
        return ammo;
    }

    @Override
    public int getAmmoMax()
    {
        return 10;
    }

    @Override
    public String getDescription()
    {
        return "Rocket Launcher";
    }

}
