package scare;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JPanel;
import scare.entity.Player;
import scare.state.GameState;
import scare.state.MenuState;
import scare.state.PauseState;
import scare.state.State;

/**
 * Main entry point
 *
 * @author mike
 */
public final class Scare extends JPanel implements Runnable
{

    /**
     * window size
     */
    public static final int WINDOW_WIDTH = 640;
    public static final int WINDOW_HEIGHT = 550;
    /**
     * game size (the play area)
     */
    public static final int GAME_WIDTH = 640;
    public static final int GAME_HEIGHT = 480;
    /**
     * gui size (below play area)
     */
    public static final int GUI_WIDTH = 640;
    public static final int GUI_HEIGHT = WINDOW_HEIGHT - GAME_HEIGHT - 3;
    /**
     * Current game state
     */
    private State state;
    /**
     * The game GUI
     */
    private final ScareGui gui;

    /**
     * Whether game is running
     */
    private boolean running = false;
    /**
     * Input
     */
    private Input input = new Input();
    /**
     * Whether game as started
     */
    private boolean started = false;
    /**
     * Start of game tick
     */
    private static long tickStartMillis = 0;
    /**
     * Current FPS ratio
     *
     * e.g. 1 if running at target FPS, 0.5 if running at half that
     */
    private double delta = 0;

    /**
     * How long a frame should take for this to run at 60FPS
     *
     * 1/60*1000
     */
    private final double targetFrameTime = 16.6666666667;
    /**
     * Game FONT
     */
    public final static Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, 16);
    /**
     * Number of ticks, only used for FPS
     */
    private short ticks = 60;

    /**
     * If logging FPS is on (below) then log every nth tick
     */
    private final short fpsTickAverage = 180;

    /**
     * Whether to log the FPS to the console
     */
    private final boolean reportAverageFpsToConsole = false;

    public Scare()
    {
        JFrame frame = new JFrame("");

        gui = new ScareGui();

        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));

        addKeyListener(input);
        addMouseMotionListener(input);
        addMouseListener(input);

        addFocusListener(new FocusListener()
        {
            @Override
            public void focusGained(FocusEvent arg0)
            {
            }

            @Override
            public void focusLost(FocusEvent arg0)
            {
                input.releaseAllKeys();
            }
        });

        frame.setLayout(new BorderLayout());
        frame.add(this, BorderLayout.CENTER);

        frame.setSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        //frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setVisible(true);
        Sound.touch();
    }

    @Override
    public void update(Graphics g)
    {
        paint(g);
    }

    @Override
    public void paint(Graphics g)
    {
        if (started) {
            return;
        }

        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setFont(FONT);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, Scare.WINDOW_WIDTH, Scare.WINDOW_HEIGHT);
        g.setColor(Color.WHITE);
        g.drawString("Loading zombies.", 50, 50);
    }

    public void start()
    {
        running = true;
        (new Thread(this)).start();
    }

    public void stop()
    {
        running = false;
    }

    /**
     * Replace the game state
     *
     * @param state
     */
    public void setState(State state)
    {
        if (this.state != null) {
            this.state.remove();
        }
        state.init(this);
        this.state = state;
    }

    /**
     * Replace the player
     *
     * @param player
     */
    public void setPlayer(Player player)
    {
        gui.setPlayer(player);
    }

    /**
     * Remove the player
     */
    public void removePlayer()
    {
        gui.removePlayer();
    }

    @Override
    public void run()
    {
        requestFocus();
        setState(new MenuState());

        long cumulativeTime = System.currentTimeMillis();

        long thisLoop = System.currentTimeMillis();
        long lastLoop = (long) (thisLoop - targetFrameTime);
        long currentFps;

        double averageFps;

        while (running) {

            thisLoop = System.currentTimeMillis();
            long loopDiff = thisLoop - lastLoop;
            lastLoop = thisLoop;
            if (loopDiff > 0) {
                currentFps = 1000 / loopDiff;
            }
            else {
                currentFps = 60;
            }
            delta = Math.abs(60 / (currentFps > 15 ? currentFps : 15));
            if (delta < 0.5) {
                delta = 0.5;
            }

            tickStartMillis = System.currentTimeMillis();
            if (reportAverageFpsToConsole) {
                ticks++;
                if (ticks > 0 && ticks % fpsTickAverage == 0) {
                    ticks = 0;
                    long timeSinceLast = tickStartMillis;
                    averageFps = 1_000 / ((timeSinceLast - cumulativeTime) / fpsTickAverage);
                    cumulativeTime = tickStartMillis;
                    System.out.println("Average FPS: " + averageFps);
                }
            }

            if (!hasFocus() && (state instanceof GameState)) {
                setState(new PauseState(state));
            }

            Image guiImg = new BufferedImage(GUI_WIDTH, GUI_HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D guiG = (Graphics2D) guiImg.getGraphics();
            guiG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            guiG.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Image gameImg = new BufferedImage(GAME_WIDTH, GAME_HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D gameG = (Graphics2D) gameImg.getGraphics();
            gameG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gameG.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            gameG.setFont(FONT);
            guiG.setFont(FONT);

            tickStartMillis = System.currentTimeMillis();
            state.tick(input);
            input.tick();

            gui.tick(input);

            gameG.setColor(Color.white);
            gameG.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

            state.paint(gameG);
            gui.paint(guiG);

            gameG.dispose();
            try {
                started = true;
                Graphics2D graphics2D = (Graphics2D) getGraphics();
                graphics2D.drawImage(gameImg, 0, 0, GAME_WIDTH, GAME_HEIGHT, 0, 0, GAME_WIDTH, GAME_HEIGHT, null);
                graphics2D.drawImage(guiImg, 0, GAME_HEIGHT + 3, WINDOW_WIDTH, WINDOW_HEIGHT, 0, 0, GUI_WIDTH, GUI_HEIGHT, null);
                graphics2D.dispose();
            }
            catch (Throwable e) {
                e.printStackTrace();
            }

            long waitTime = Math.round(targetFrameTime - (System.currentTimeMillis() - thisLoop));
            if (waitTime < 1) {
                waitTime = 1;
            }

            try {
                Thread.sleep(waitTime);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public long getTickStartMillis()
    {
        return tickStartMillis;
    }

    public double getDelta()
    {
        return delta;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        Scare scare = new Scare();
        scare.start();
    }
}
