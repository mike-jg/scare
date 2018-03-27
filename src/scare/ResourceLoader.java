package scare;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/**
 * Load resources
 *
 * @author mike
 */
public class ResourceLoader
{

    /**
     * player tiles
     */
    public static final BufferedImage[][] player = split(loadImage("/scare/resource/p.png"), 16, 16);
    /**
     * misc tiles such as ammo/health boxes
     */
    public static final BufferedImage[][] misc = split(loadImage("/scare/resource/misc.png"), 16, 16);
    /**
     * zombie tiles
     */
    public static final BufferedImage[][] z1 = split(loadImage("/scare/resource/z1.png"), 16, 16);
    public static final BufferedImage[][] z2 = split(loadImage("/scare/resource/z2.png"), 16, 16);
    public static final BufferedImage[][] z3 = split(loadImage("/scare/resource/z3.png"), 16, 16);
    public static final BufferedImage[][] z4 = split(loadImage("/scare/resource/z4.png"), 16, 16);

    /**
     * Load an image
     *
     * @param name
     * @return
     */
    public static BufferedImage loadImage(String name)
    {
        try {
            BufferedImage org = ImageIO.read(ResourceLoader.class.getResource(name));

            BufferedImage res = new BufferedImage(org.getWidth(), org.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics g = res.getGraphics();
            g.drawImage(org, 0, 0, null, null);
            g.dispose();
            return res;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Scale an image
     *
     * @param src
     * @param scale
     * @return
     */
    public static BufferedImage[][] scale(BufferedImage[][] src, int scale)
    {
        for (int x = 0; x < src.length; x++) {
            for (int y = 0; y < src[x].length; y++) {
                src[x][y] = scale(src[x][y], scale);
            }
        }
        return src;
    }

    /**
     * Scale an image
     *
     * @param src
     * @param scale
     * @return
     */
    public static BufferedImage scale(BufferedImage src, int scale)
    {
        int w = src.getWidth() * scale;
        int h = src.getHeight() * scale;
        BufferedImage res = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics g = res.getGraphics();
        g.drawImage(src.getScaledInstance(w, h, Image.SCALE_AREA_AVERAGING), 0, 0, null);
        g.dispose();
        return res;
    }

    /**
     * Split an image into separate images
     *
     * @param src
     * @param xs
     * @param ys
     * @return
     */
    public static BufferedImage[][] split(BufferedImage src, int xs, int ys)
    {
        int xSlices = src.getWidth() / xs;
        int ySlices = src.getHeight() / ys;
        BufferedImage[][] res = new BufferedImage[xSlices][ySlices];
        for (int x = 0; x < xSlices; x++) {
            for (int y = 0; y < ySlices; y++) {
                res[x][y] = new BufferedImage(xs, ys, BufferedImage.TYPE_INT_ARGB);
                Graphics g = res[x][y].getGraphics();
                g.drawImage(src, -x * xs, -y * ys, null);
                g.dispose();
            }
        }
        return res;
    }

    private ResourceLoader()
    {
    }
}
