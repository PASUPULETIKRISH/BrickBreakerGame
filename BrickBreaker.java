import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class BrickBreaker extends JFrame {

    public BrickBreaker() {
        setTitle("Brick Breaker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        add(new GamePanel());
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        new BrickBreaker();
    }

    private class GamePanel extends JPanel implements ActionListener {

        private Timer timer;
        private Paddle paddle;
        private Ball ball;
        private Brick[] bricks;
        private boolean gameOver;
        private boolean gameWon;
        private boolean paused;
        private int score;
        private SoundManager soundManager;

        public GamePanel() {
            setFocusable(true);
            setPreferredSize(new Dimension(800, 600));
            setBackground(Color.BLACK);
            initGame();
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    paddle.keyPressed(e);
                    if (e.getKeyCode() == KeyEvent.VK_P) {
                        paused = !paused;
                    }
                    if (e.getKeyCode() == KeyEvent.VK_R && (gameOver || gameWon)) {
                        initGame();
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    paddle.keyReleased(e);
                }
            });
        }

        private void initGame() {
            paddle = new Paddle();
            ball = new Ball();
            bricks = new Brick[30];
            int k = 0;
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 6; j++) {
                    bricks[k] = new Brick(j * 100 + 30, i * 40 + 50);
                    k++;
                }
            }
            score = 0;
            gameOver = false;
            gameWon = false;
            paused = false;
            soundManager = new SoundManager();
            timer = new Timer(10, this);
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            draw(g);
        }

        private void draw(Graphics g) {
            paddle.draw(g);
            ball.draw(g);
            for (Brick brick : bricks) {
                if (!brick.isDestroyed()) {
                    brick.draw(g);
                }
            }
            g.setColor(Color.WHITE);
            g.drawString("Score: " + score, 10, 10);

            if (paused) {
                g.setColor(Color.YELLOW);
                g.drawString("Paused", 370, 300);
            }

            if (gameOver) {
                displayGameOver(g);
            }

            if (gameWon) {
                displayGameWon(g);
            }
        }

        private void displayGameOver(Graphics g) {
            String msg = "Game Over! Press R to Restart.";
            g.setColor(Color.RED);
            g.drawString(msg, 300, 300);
        }

        private void displayGameWon(Graphics g) {
            String msg = "You Won! Press R to Restart.";
            g.setColor(Color.GREEN);
            g.drawString(msg, 300, 300);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!paused) {
                if (!gameOver && !gameWon) {
                    paddle.move();
                    ball.move();
                    checkCollision();
                    checkGameWon();
                }
                repaint();
            }
        }

        private void checkCollision() {
            if (ball.getRect().intersects(paddle.getRect())) {
                ball.setYDir(-ball.getYDir());
                soundManager.playSound("paddle_hit.wav");
            }

            for (Brick brick : bricks) {
                if (!brick.isDestroyed() && ball.getRect().intersects(brick.getRect())) {
                    ball.setYDir(-ball.getYDir());
                    brick.setDestroyed(true);
                    score += 10;
                    soundManager.playSound("brick_break.wav");
                }
            }

            if (ball.getY() > getHeight()) {
                gameOver = true;
            }
        }

        private void checkGameWon() {
            boolean allBricksDestroyed = true;
            for (Brick brick : bricks) {
                if (!brick.isDestroyed()) {
                    allBricksDestroyed = false;
                    break;
                }
            }
            if (allBricksDestroyed) {
                gameWon = true;
            }
        }
    }

    private class Paddle {
        private int x, y;
        private int width, height;
        private int dx;

        public Paddle() {
            x = 400;
            y = 550;
            width = 100;
            height = 10;
        }

        public void move() {
            x += dx;
            if (x < 0) x = 0;
            if (x > 700) x = 700;
        }

        public void draw(Graphics g) {
            g.setColor(Color.GREEN);
            g.fillRect(x, y, width, height);
        }

        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_LEFT) {
                dx = -5;
            }
            if (key == KeyEvent.VK_RIGHT) {
                dx = 5;
            }
        }

        public void keyReleased(KeyEvent e) {
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) {
                dx = 0;
            }
        }

        public Rectangle getRect() {
            return new Rectangle(x, y, width, height);
        }
    }

    private class Ball {
        private int x, y;
        private int diameter;
        private int xDir, yDir;

        public Ball() {
            x = 400;
            y = 300;
            diameter = 20;
            xDir = 1;
            yDir = -1;
        }

        public void move() {
            x += xDir;
            y += yDir;

            if (x < 0 || x > 780) {
                xDir = -xDir;
            }
            if (y < 0) {
                yDir = -yDir;
            }
        }

        public void draw(Graphics g) {
            g.setColor(Color.YELLOW);
            g.fillOval(x, y, diameter, diameter);
        }

        public Rectangle getRect() {
            return new Rectangle(x, y, diameter, diameter);
        }

        public int getY() {
            return y;
        }

        public int getYDir() {
            return yDir;
        }

        public void setYDir(int yDir) {
            this.yDir = yDir;
        }
    }

    private class Brick {
        private int x, y;
        private int width, height;
        private boolean destroyed;

        public Brick(int x, int y) {
            this.x = x;
            this.y = y;
            width = 80;
            height = 30;
            destroyed = false;
        }

        public void draw(Graphics g) {
            g.setColor(Color.RED);
            g.fillRect(x, y, width, height);
        }

        public boolean isDestroyed() {
            return destroyed;
        }

        public void setDestroyed(boolean destroyed) {
            this.destroyed = destroyed;
        }

        public Rectangle getRect() {
            return new Rectangle(x, y, width, height);
        }
    }

    private class SoundManager {
        public void playSound(String soundFile) {
            try {
                File soundPath = new File(soundFile);
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundPath);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.start();
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
        }
    }
}
