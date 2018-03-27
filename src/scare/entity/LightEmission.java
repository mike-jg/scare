package scare.entity;

import java.awt.AlphaComposite;
import java.awt.Shape;

/**
 *
 * @author Mike
 */
public class LightEmission
{

    private Shape[] shapes;
    private AlphaComposite alphaComposite;

    public LightEmission()
    {
        this(new Shape[]{});
    }

    public LightEmission(Shape[] shapes)
    {
        this(shapes, AlphaComposite.getInstance(AlphaComposite.CLEAR));
    }

    public LightEmission(Shape[] shapes, AlphaComposite alphaComposite)
    {
        this.shapes = shapes;
        this.alphaComposite = alphaComposite;
    }

    public Shape[] getShapes()
    {
        return shapes;
    }

    public AlphaComposite getAlphaComposite()
    {
        return alphaComposite;
    }

}
