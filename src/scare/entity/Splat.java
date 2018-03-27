package scare.entity;

import java.awt.Color;
import java.awt.Graphics;
import scare.Input;

/**
 * A splatter of something
 *
 * @author mike
 */
public class Splat extends Entity
{

    private BabySplat[] splats;

    public enum Type
    {
        // red blood
        Blood,
        // green goo
        Zombie,
        // black scorch marks
        Scorch
    }

    public Splat(Entity source, Type type)
    {
        this(source.getX() - (source.getW() / 2), source.getY() - (source.getH() / 2), type);
    }

    public Splat(int x, int y, Type type)
    {
        this.x = x;
        this.y = y;

        splats = new BabySplat[EntityUtility.nextInt(15) + 3];

        for (int i = 0; i < splats.length; i++) {
            int nx = EntityUtility.nextInt(75) + 1;
            int ny = EntityUtility.nextInt(75) + 1;

            splats[i] = new BabySplat(x + nx, y + ny, type);
        }
    }

    public Splat(Entity source, double angle, Type type)
    {
        this.x = source.getX();
        this.y = source.getY();

        splats = new BabySplat[EntityUtility.nextInt(15) + 3];

        for (int i = 0; i < splats.length; i++) {
            int nx = EntityUtility.nextInt(75) + 10;
            int ny = EntityUtility.nextInt(25) + 5;
            double angleDeviation = Math.random();

            splats[i] = new BabySplat((int) (x + (nx * Math.cos(angle + angleDeviation))), (int) (y + (ny * Math.sin(angle + angleDeviation))), type);
        }
    }

    @Override
    public void paint(Graphics g)
    {
        for (BabySplat spl : splats) {
            g.setColor(spl.getColour());
            g.fillOval(spl.getX(), spl.getY(), spl.getDiameter(), spl.getDiameter());
        }
    }

    @Override
    public void tick(Input input, double delta)
    {
    }

    /**
     * A single splatter
     */
    private class BabySplat
    {

        private int x, y;
        private int diameter;
        private Color colour;

        public BabySplat(int x, int y, Splat.Type type)
        {
            this.x = x;
            this.y = y;
            this.diameter = EntityUtility.getRandom(1, 3);
            if (type == Splat.Type.Blood) {
                this.colour = EntityUtility.nextBoolean() && EntityUtility.nextBoolean() ? new Color(0x800000) : Color.red;
            }
            else if (type == Splat.Type.Zombie) {
                this.colour = EntityUtility.nextBoolean() ? new Color(0xB6FF00) : Color.GREEN;
            }
            else {
                switch (EntityUtility.nextInt(2)) {
                    default:
                    case 0:
                        this.colour = Color.BLACK.brighter();
                        break;
                    case 1:
                        this.colour = Color.DARK_GRAY;
                        break;
                }
            }

            if (EntityUtility.nextBoolean()) {
                this.colour = this.colour.brighter();
            }
            else if (EntityUtility.nextBoolean()) {
                this.colour = this.colour.darker();
            }

        }

        public int getDiameter()
        {
            return diameter;
        }

        public int getX()
        {
            return x;
        }

        public int getY()
        {
            return y;
        }

        public Color getColour()
        {
            return colour;
        }
    }
}
