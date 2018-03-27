package scare;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.event.MouseInputListener;

/**
 * Track keyboard/mouse input
 *
 * @author mike
 */
public class Input implements KeyListener, MouseMotionListener, MouseInputListener
{

    /**
     * Mouse position
     */
    private Point mouse;
    /**
     * Keys
     */
    public static final int W = 0;
    public static final int S = 1;
    public static final int A = 2;
    public static final int D = 3;
    public static final int SPACE = 4;
    public static final int ESCAPE = 5;
    public static final int ONE = 6;
    public static final int TWO = 7;
    public static final int THREE = 8;
    public static final int FOUR = 10;
    public static final int MOUSE_ONE = 9;
    /**
     * Pressed keys
     */
    private boolean[] keys = new boolean[]{
        false, false, false, false, false, false, false, false, false, false, false
    };
    /**
     * Keys pressed last frame
     */
    private boolean[] lastKeys = new boolean[]{
        false, false, false, false, false, false, false, false, false, false, false
    };

    /**
     * Game tick
     */
    public void tick()
    {
        System.arraycopy(keys, 0, lastKeys, 0, keys.length);
    }

    /**
     * Set a key
     *
     * @param key
     * @param down
     */
    private void set(int key, boolean down)
    {
        int pressed = -1;
        switch (key) {
            case KeyEvent.VK_W:
                pressed = W;
                break;
            case KeyEvent.VK_S:
                pressed = S;
                break;
            case KeyEvent.VK_A:
                pressed = A;
                break;
            case KeyEvent.VK_D:
                pressed = D;
                break;
            case KeyEvent.VK_SPACE:
                pressed = SPACE;
                break;
            case KeyEvent.VK_ESCAPE:
                pressed = ESCAPE;
                break;
            case KeyEvent.VK_1:
                pressed = ONE;
                break;
            case KeyEvent.VK_2:
                pressed = TWO;
                break;
            case KeyEvent.VK_3:
                pressed = THREE;
                break;
            case KeyEvent.VK_4:
                pressed = FOUR;
                break;
            case MOUSE_ONE:
                pressed = MOUSE_ONE;
                break;
        }

        if (pressed >= 0) {
            keys[pressed] = down;
        }
    }

    /**
     * Whether a key was pressed this tick but not last tick
     *
     * This helps respond to an input only once
     *
     * @param key
     * @return
     */
    public boolean isPressedThisTick(int key)
    {
        return isPressed(key) && !wasPressed(key);
    }

    /**
     * Whether a key is pressed this tick
     *
     * @param key
     * @return
     */
    public boolean isPressed(int key)
    {
        return keys[key];
    }

    /**
     * Whether a key was pressed last tick
     *
     * @param key
     * @return
     */
    public boolean wasPressed(int key)
    {
        return lastKeys[key];
    }

    /**
     * Where the mouse is
     *
     * @return
     */
    public Point getMousePosition()
    {
        return mouse;
    }

    /**
     * Release all input
     */
    public void releaseAllKeys()
    {
        for (int i = 0; i < keys.length; i++) {
            keys[i] = false;
        }
    }

    /**
     * implement interfaces *
     */
    @Override
    public void keyTyped(KeyEvent e)
    {
    }

    @Override
    public void keyPressed(KeyEvent ke)
    {
        set(ke.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent ke)
    {
        set(ke.getKeyCode(), false);
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
        if (mouse instanceof Point) {
            mouse.setLocation(e.getX(), e.getY());
            return;
        }
        mouse = new Point(e.getX(), e.getY());
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        set(MOUSE_ONE, true);
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        set(MOUSE_ONE, false);
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
    }
}
