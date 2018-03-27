package scare;

import java.awt.Color;
import java.awt.Graphics;
import scare.entity.Player;
import scare.entity.Splat;
import scare.weapon.Weapon;

/**
 * Player GUI
 *
 * @author mike
 */
public class ScareGui
{

    /**
     * The player to represent
     */
    private Player player;
    /**
     * splatters of blood to put around the health
     */
    private Splat[] healthSplats;
    /**
     * The number of splats
     */
    private byte numSplats;
    private Splat[] randomSplats;

    public ScareGui()
    {
        // some random splats of blood to the right of the gui
        randomSplats = new Splat[20];
        for (int i = 0; i < 20; i++) {
            randomSplats[i] = new Splat((Scare.GUI_WIDTH - 15) - (i * 15), 5, Splat.Type.Blood);
        }

        healthSplats = new Splat[10];
        numSplats = 0;
        // some splatters when the health gets low
        for (int i = 0; i < 10; i++) {
            healthSplats[i] = new Splat(25 + (i * 5), 8, Splat.Type.Blood);
        }
    }

    /**
     * set the player to represent
     *
     * @param player
     */
    public void setPlayer(Player player)
    {
        this.player = player;
    }

    public void removePlayer()
    {
        player = null;
    }

    public void tick(Input input)
    {
    }

    public void paint(Graphics g)
    {
        if (this.player == null) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, Scare.GUI_WIDTH, Scare.GUI_HEIGHT - 3);
        }
        else {
            paintPlayerStat(g);
        }
    }

    private void paintPlayerStat(Graphics g)
    {
        for (Splat spl : randomSplats) {
            spl.paint(g);
        }
        // depending on health, draw more splats of blood around
        // the health meter
        if (player.getHealth() <= 30) {
            for (int i = 0; i < 10; i++) {
                healthSplats[i].paint(g);
            }
            g.setColor(Color.RED);
        }
        else if (player.getHealth() <= 70 && numSplats != 5) {
            for (int i = 0; i < 10; i += 2) {
                healthSplats[i].paint(g);
            }
            g.setColor(Color.WHITE);
        }
        else {
            g.setColor(Color.WHITE);
        }

        g.drawString("Health: " + player.getHealth(), 30, 14);

        // draw weapons
        Weapon currentWeapon = player.getCurrentWeapon();

        int xOffset = 0;
        for (Weapon weapon : player.getWeapons()) {
            String weaponString = weapon.getDescription();
            if (weapon.getAmmo() == Weapon.AMMO_INFINITE) {
                weaponString = weaponString + ": ∞";
            }
            else if (weapon.getAmmo() != Weapon.AMMO_INFINITE) {
                weaponString = weaponString + ": " + weapon.getAmmo();
            }

            if (weapon == currentWeapon) {
                g.setColor(Color.WHITE);
            }
            else {
                g.setColor(Color.GRAY);
            }

            g.drawString(weaponString, 30 + xOffset, 30);
            xOffset += 125;
        }

    }
}
