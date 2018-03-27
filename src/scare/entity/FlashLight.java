package scare.entity;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import scare.Input;

/**
 * The player's flashlight
 *
 * @author Mike
 */
public class FlashLight extends Entity implements LightEmitter
{

    /**
     * Total rays for the flashlight, dictates width
     */
    public static final byte BEAM_RAYS = 65;
    /**
     * Radius of each beam which is cast 360 degrees around the player
     */
    public static final byte BEAM_HALO_RADIUS = 35;
    /**
     * Number of beans to cast around player
     */
    public static final byte BEAM_HALO_NUM = 1;

    private final Player player;

    /**
     * Where the mouse is
     */
    private Point mousePos;

    public FlashLight(Player player)
    {
        this.player = player;
    }

    @Override
    public void paint(Graphics g)
    {
    }

    @Override
    public void tick(Input input, double delta)
    {
        if (input.getMousePosition() != mousePos && input.getMousePosition() != null) {
            mousePos = input.getMousePosition();
        }
    }

    @Override
    public LightEmission emitLight(LightPainter painter)
    {
        if (mousePos == null) {
            return LightPainter.NULL_LIGHT_EMISSION;
        }

        return castLightShapes(painter);
    }

    private LightEmission castLightShapes(LightPainter painter)
    {
        Polygon flashLightPoly = new Polygon();

        Point playerPoint = player.getCentrePoint();

        flashLightPoly.addPoint((int) playerPoint.getX(), (int) playerPoint.getY());

        double AB = Math.atan2(mousePos.getY() - playerPoint.getY(), mousePos.getX() - playerPoint.getX());
        int end = BEAM_RAYS / 2;
        int start = -end;

        for (int i = start; i < end; i++) {
            Point projected = painter.castRay(playerPoint, AB, i, 640);
            if (projected != null) {
                flashLightPoly.addPoint((int) projected.getX(), (int) projected.getY());
            }
        }

        // fire off rays in a circle, of varying lengths to create a 'halo' of light around
        // the player, so it's less impossible to shoot zombies that are nearby
        Shape[] returnShapes = new Shape[BEAM_HALO_NUM + 1];
        returnShapes[0] = flashLightPoly;

        for (byte hindex = 1; hindex < returnShapes.length; hindex++) {
            returnShapes[hindex] = painter.cast360(playerPoint, hindex * BEAM_HALO_RADIUS);
        }

        return new LightEmission(returnShapes);
    }
}
