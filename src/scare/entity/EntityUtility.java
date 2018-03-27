package scare.entity;

import java.util.Random;

/**
 * Some utility functions for entities
 *
 * @author mike
 */
public class EntityUtility
{

    private static final Random RANDOM = new Random();

    /**
     * Euclidean distance between two entities
     *
     * @param a
     * @param b
     * @return
     */
    public static double distance(Entity a, Entity b)
    {
        return distance(a.getX(), a.getY(), b.getX(), b.getY());
    }

    /**
     * Euclidean distance between two points
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    public static double distance(double x1, double y1, double x2, double y2)
    {
        double xdiff = x1 - x2;
        double ydiff = y1 - y2;
        return Math.sqrt((xdiff * xdiff) + (ydiff * ydiff));
    }

    /**
     * random functions *
     */
    public static int nextInt()
    {
        return RANDOM.nextInt();
    }

    public static int nextInt(int n)
    {
        return RANDOM.nextInt(n);
    }

    public static boolean nextBoolean()
    {
        return RANDOM.nextBoolean();
    }

    /**
     * Get a random byte between min and max inclusive
     *
     * @param min
     * @param max
     * @return
     */
    public static byte getRandom(byte min, byte max)
    {
        return (byte) ((RANDOM.nextFloat() * (max - min + 1)) + min);
    }

    /**
     * Get a random integer between min and max inclusive
     *
     * @param min
     * @param max
     * @return
     */
    public static int getRandom(int min, int max)
    {
        return (int) ((RANDOM.nextFloat() * (max - min + 1)) + min);
    }

    /**
     * Get a random double between min and max inclusive
     *
     * @param min
     * @param max
     * @return
     */
    public static double getRandomArbitrary(double min, double max)
    {
        return (RANDOM.nextFloat() * (max - min)) + min;
    }

    private EntityUtility()
    {
    }
}
