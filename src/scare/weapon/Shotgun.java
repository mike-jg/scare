package scare.weapon;

import scare.Sound;
import scare.entity.Bullet;
import scare.level.Level;

/**
 * Fires an arc of bullets
 *
 * @author mike
 */
public class Shotgun extends Weapon
{

    public Shotgun()
    {
        ammo = 50;
    }

    @Override
    public void fire(Level level, int x, int y, double angle)
    {
        Sound.shotgun.play();
        for (double angleMod = -0.10; angleMod < 0.10; angleMod += 0.02) {
            level.addEntity(new Bullet(x, y, angle + angleMod, Bullet.SHOTGUN_PUSH_AMT));
        }
        ammo -= 1;
    }

    @Override
    public int getFireDelay()
    {
        return 780;
    }

    @Override
    public int getAmmo()
    {
        return ammo;
    }

    @Override
    public int getAmmoMax()
    {
        return 50;
    }

    @Override
    public String getDescription()
    {
        return "Shotgun";
    }
}
