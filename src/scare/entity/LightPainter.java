package scare.entity;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import scare.Input;
import scare.Scare;

/**
 * Responsible for painting the light onto the level
 *
 * Also functions as a helper containing some common functions
 *
 * This works by painting a large back square over the entire screen
 * It then cancels areas of this square out, by calling each 'light emitter' (e.g. the flashlight)
 * in turn and querying them for an array of 'Shapes'. These shapes are then
 * painted onto the black square, cancelling those areas of it out and forming the 'light'
 *
 * @author Mike
 */
public class LightPainter extends Entity
{

    public static final LightEmission NULL_LIGHT_EMISSION = new LightEmission();

    @Override
    public void paint(Graphics g)
    {
        // create a second image to draw the dark areas over
        BufferedImage bi = new BufferedImage(Scare.GAME_WIDTH, Scare.GAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);

        Graphics2D gbi = (Graphics2D) bi.createGraphics();
        gbi.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color black = new Color(0.0f, 0.0f, 0.0f, 0.85f);

        gbi.setColor(black);

        // set the composite and fill the whole screen with black,
        // CLEAR will cancel out pixels which overlap between the
        // light emitter's shapes and black area, rendering the light as a clear area
        gbi.fill(new Rectangle(0, 0, Scare.GAME_WIDTH, Scare.GAME_HEIGHT));

        for (LightEmitter le : level.getLightEmittingEntities()) {
            LightEmission emission = le.emitLight(this);
            gbi.setComposite(emission.getAlphaComposite());
            for (Shape s : emission.getShapes()) {
                gbi.fill(s);
            }
        }
        g.drawImage(bi, 0, 0, null);
        gbi.dispose();
    }

    @Override
    public void tick(Input input, double delta)
    {

    }

    public Shape cast360(Point p, long rayLength)
    {
        Polygon haloPolygon = new Polygon();
        for (short i = -180; i <= 180; i++) {
            Point projected = castRay(p, 0, i, rayLength);
            if (projected != null) {
                haloPolygon.addPoint((int) projected.getX(), (int) projected.getY());
            }
        }
        return haloPolygon;
    }

    public Point castRay(Point originatingPoint, double angleAB, int angleOffset, long distanceMax)
    {
        int distance = 1;

        double angleAC = (angleAB + angleOffset * Math.PI / 180.0f);

        Point p  = new Point((int) (originatingPoint.getX() + distance * Math.cos(angleAC)),
                    (int) (originatingPoint.getY() + distance * Math.sin(angleAC)));

        Point lastP = null;

        for (; distance < distanceMax; distance++) {
            p.setLocation((int) (originatingPoint.getX() + distance * Math.cos(angleAC)),
                    (int) (originatingPoint.getY() + distance * Math.sin(angleAC)));

            if (lastP != null && isPassable((int) lastP.getX(), (int) lastP.getY(), 1, 1) && !isPassable((int) p.getX(), (int) p.getY(), 1, 1)) {
                return lastP;
            }
            if (lastP == null) {
                lastP = new Point(p);
            } else {
                lastP.setLocation(p);
            }
        }

        return p;
    }

}
