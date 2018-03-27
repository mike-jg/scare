package scare.state;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import scare.Input;
import scare.Scare;
import scare.Sound;
import scare.entity.Ammo;
import scare.entity.Health;

/**
 * Game has just started
 *
 * @author mike
 */
public class MenuState extends State
{

    private SubMenuState state;

    public MenuState()
    {
        state = new MainMenuState();
    }

    @Override
    public void tick(Input input)
    {
        state.tick(input);
    }

    @Override
    public void paint(Graphics g)
    {
        state.paint(g);
    }

    private abstract class SubMenuState
    {

        abstract void tick(Input input);

        abstract void paint(Graphics g);
    }

    private class MainMenuState extends SubMenuState
    {

        private String[] menuOptions;
        private int selectedOption;

        public MainMenuState()
        {
            menuOptions = new String[]{
                "Start",
                "?",
                "Quit"
            };
            selectedOption = 0;
        }

        @Override
        public void tick(Input input)
        {
            Point mouse = input.getMousePosition();

            if (mouse == null) {
                return;
            }

            for (int y = 150, i = 0; i < menuOptions.length; i++, y += 45) {
                if (mouse.y > y - 20 && mouse.y < y + 20) {
                    selectedOption = i;
                }
            }

            if (input.isPressedThisTick(Input.MOUSE_ONE)) {
                if (selectedOption == 0) {
                    Sound.pistol.play();
                    setState(new GameState());
                    return;
                }
                else if (selectedOption == 1) {
                    Sound.pistol.play();
                    state = new HelpMenuState();
                }
                else if (selectedOption == 2) {
                    Sound.death.play();
                    try {
                        Thread.sleep(1800);
                    }
                    catch (InterruptedException ex) {
                    }
                    System.exit(0);
                }
            }
        }

        @Override
        public void paint(Graphics g)
        {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, Scare.GAME_WIDTH, Scare.GAME_HEIGHT);

            for (int y = 150, i = 0; i < menuOptions.length; i++, y += 45) {
                String opt = menuOptions[i];

                if (selectedOption == i) {
                    g.setColor(Color.RED);
                }
                else {
                    g.setColor(Color.WHITE);
                }
                g.drawString(opt, 50, y);
            }
        }
    }

    private class HelpMenuState extends SubMenuState
    {

        private Point mouse;
        private final Ammo ammo;
        private final Health health;

        public HelpMenuState()
        {
            ammo = new Ammo(50, 210);
            health = new Health(50, 230);
        }

        @Override
        public void tick(Input input)
        {
            if (input.isPressedThisTick(Input.MOUSE_ONE) && mouse != null && mouse.y > 250 && mouse.y < 290) {
                Sound.pistol.play();
                state = new MainMenuState();
            }
            mouse = input.getMousePosition();
        }

        @Override
        public void paint(Graphics g)
        {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, Scare.GAME_WIDTH, Scare.GAME_HEIGHT);
            g.setColor(Color.WHITE);
            g.drawString("Movement:  W A S D", 50, 100);
            g.drawString("Look:      Mouse", 50, 120);
            g.drawString("Fire:      Mouse 1 // Space", 50, 140);
            g.drawString("Weapons:   1-4", 50, 160);
            g.drawString("Pause:     Esc", 50, 180);
            g.drawString("Pickups: ", 50, 200);
            ammo.paint(g);
            g.drawString(" ammo", 70, 220);
            health.paint(g);
            g.drawString(" health", 70, 240);

            if (mouse != null && mouse.y > 250 && mouse.y < 290) {
                g.setColor(Color.RED);
            }

            g.drawString("Back", 50, 270);
        }
    }
}
