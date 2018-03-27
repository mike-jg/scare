package scare;

import java.io.*;
import java.util.Random;
import javax.sound.sampled.*;

public class Sound
{

    public static class Clips
    {

        /**
         * clips
         */
        public Clip[] clips;
        /**
         * current clip to play
         */
        private int p;
        /**
         * total clips
         *
         * basically how many of this clip can play simultaneously
         */
        private int count;

        private long lastPlayMillis = 0;

        /**
         *
         * @param buffer
         * @param count
         * @throws LineUnavailableException
         * @throws IOException
         * @throws UnsupportedAudioFileException
         */
        public Clips(byte[] buffer, int count) throws LineUnavailableException, IOException, UnsupportedAudioFileException
        {
            if (buffer == null) {
                return;
            }

            clips = new Clip[count];
            this.count = count;
            for (int i = 0; i < count; i++) {
                clips[i] = AudioSystem.getClip();
                clips[i].open(AudioSystem.getAudioInputStream(new ByteArrayInputStream(buffer)));
            }
        }

        /**
         * Play the clip
         */
        public void play()
        {
            if (clips == null) {
                return;
            }

            lastPlayMillis = System.currentTimeMillis();

            clips[p].stop();
            clips[p].setFramePosition(0);
            clips[p].start();
            p++;
            if (p >= count) {
                p = 0;
            }
        }

        public void playIfTimeElapsed(long timeElapsed)
        {
            if (System.currentTimeMillis() - lastPlayMillis > timeElapsed)
            {
                play();
            }
        }
    }
    /**
     * Pistol shot
     */
    public static Clips pistol = load("/scare/resource/pistol.wav", 8);
    /**
     * Shotgun shot+reload
     */
    public static Clips shotgun = load("/scare/resource/shotgun.wav", 2);
    /**
     * Grenade being thrown
     */
    public static Clips grenadeThrow = load("/scare/resource/grenade-throw.wav", 2);
    /**
     * Rocket being fired
     */
    public static Clips rocketLauncher = load("/scare/resource/rocket-launcher.wav", 2);
    /**
     * Grenade hitting something
     */
    public static Clips[] grenadeImpact = new Clips[]{
        load("/scare/resource/grenade-impact-1.wav", 2),
        load("/scare/resource/grenade-impact-2.wav", 2),
        load("/scare/resource/grenade-impact-3.wav", 2)
    };
    /**
     * Shotgun reload
     */
    public static Clips reload = load("/scare/resource/reload.wav", 1);
    /**
     * Gun out of ammo
     */
    public static Clips noAmmo = load("/scare/resource/no-ammo.wav", 1);
    /**
     * Weapon being armed
     */
    public static Clips armWeapon = load("/scare/resource/arm-weapon.wav", 8);
    /**
     * Explosion
     */
    public static Clips[] explode = new Clips[]{
        load("/scare/resource/explode-1.wav", 4),
        load("/scare/resource/explode-2.wav", 4)
    };
    /**
     * Player death
     */
    public static Clips death = load("/scare/resource/death.wav", 1);
    /**
     * Player screams
     */
    public static Clips[] pain = new Clips[]{
        load("/scare/resource/pain-1.wav", 4),
        load("/scare/resource/pain-2.wav", 4),
        load("/scare/resource/pain-3.wav", 4),
        load("/scare/resource/pain-4.wav", 4),
        load("/scare/resource/pain-5.wav", 4)
    };
    /**
     * Big gore sound, e.g. a corpse exploding
     */
    public static Clips bigGore = load("/scare/resource/big-gore.wav", 4);
    /**
     * Small gore sounds, e.g. a corpse falling or hitting a wall
     */
    public static Clips[] gore = new Clips[]{
        load("/scare/resource/gore-1.wav", 4),
        load("/scare/resource/gore-2.wav", 4),
        load("/scare/resource/gore-3.wav", 4)
    };
    /**
     * Tearing of a bandage
     */
    public static Clips bandage = load("/scare/resource/bandage.wav", 1);

    /**
     * Zombie moans/groans
     */
    public static Clips[] zombie;

    static {
        zombie = new Clips[25];
        for (int i = 0; i < zombie.length; i++) {
            zombie[i] = load("/scare/resource/zombie-" + Integer.toString(i + 1, 10) + ".wav", 4);
        }
    }

    private static Random rnd = new Random();

    /**
     * Get a random clip
     *
     * @param clips
     * @return
     */
    public static Clips getRandom(Clips[] clips)
    {
        return clips[((int) (rnd.nextFloat() * (clips.length)))];
    }

    private static Clips load(String name, int count)
    {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataInputStream dis = new DataInputStream(Sound.class.getResourceAsStream(name));
            byte[] buffer = new byte[1024];
            int read = 0;
            while ((read = dis.read(buffer)) >= 0) {
                baos.write(buffer, 0, read);
            }
            dis.close();

            byte[] data = baos.toByteArray();
            return new Clips(data, count);
        }
        catch (Exception e) {
            e.printStackTrace();
            try {
                return new Clips(null, 0);
            }
            catch (Exception ee) {
                ee.printStackTrace();
                return null;
            }
        }
    }

    public static void touch()
    {
    }
}
