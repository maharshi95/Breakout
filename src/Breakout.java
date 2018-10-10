/*
 * File: Breakout.java
 * -------------------
 * Author: Maharshi Gor
 * This file will eventually implement the game of Breakout.
 */

import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class Breakout extends GraphicsProgram {

    private static final int WIDTH = 400;
    private static final int HEIGHT = 600;

    private static final int SIDE_SPACE = 150;
    private static final int BOTTOM_SPACE = 75;

    private static final int APPLICATION_WIDTH = WIDTH + SIDE_SPACE;
    private static final int APPLICATION_HEIGHT = HEIGHT + BOTTOM_SPACE;

    private static final int PADDLE_WIDTH = 60;
    private static final int PADDLE_HEIGHT = 10;
    private static final int PADDLE_Y_OFFSET = 60;
    private static final int BOUNDRY_LINE = HEIGHT - 40;

    private static final int NBRICKS_PER_ROW = 8;
    private static final int NBRICK_ROWS = 5;
    private static final int BRICK_SEP = 10;

    private static final int BRICK_HEIGHT = 10;
    private static final int BRICK_WIDTH = (WIDTH - (NBRICKS_PER_ROW + 1)
            * BRICK_SEP) / NBRICKS_PER_ROW;

    private static final int BALL_RADIUS = 5;

    private static final int NTURNS = 3;
    private static final int SCORE_UNIT = 10;
    private static final double LABEL_ALIGN = WIDTH + 0.1 * SIDE_SPACE;
    private static final double UP_SPACE = 0.3 * SIDE_SPACE;

    private static double DELAY = 20;

    private String hit_audio_file = "bounce.au";

    private RandomGenerator rgen = RandomGenerator.getInstance();

    private GRect paddle;
    private GOval ball;
    private GLabel gameLoseLabel;
    private GLabel gameWinLabel;
    private GLabel turnsLeftLabel;
    private GLabel scoreLabel;

    private static int turnsLeft = NTURNS - 1;
    private static int score = 0;
    private static int bricks = NBRICKS_PER_ROW * NBRICK_ROWS;

    private double xVel = rgen.nextDouble(1.0, 4.0);
    private double yVel = rgen.nextDouble(2.0, 4.0);

    private AudioClip bounceClip = MediaTools.loadAudioClip(hit_audio_file);

    public static void main(String args[]) {
        new Breakout().start(args);
    }

    public void run() {
        setupGame();
        addMouseListeners();
        waitForClick();
        if (rgen.nextBoolean(0.5))
            xVel = -xVel;
        do {
            moveBall();
            checkWallCollision();
            checkPaddleCollision();
            checkBrickCollision();
            checkTurnOver();
            pause(DELAY - (score / 50.0));
        } while (!gameOver());

        if (gameWon())
            declareWin();
        else
            declareLose();
        exit();

    }

    private void setupGame() {
        setSize(APPLICATION_WIDTH, APPLICATION_HEIGHT);
        setupBricks();
        setupPaddle();
        setupBall();
        setupWalls();
        setupLabels();
    }

    /**
     * This method sets up the bricks into their initial order
     */
    private void setupBricks() {
        Color color;
        for (int j = 0; j < NBRICK_ROWS; j++) {
            color = getBrickColor(j);
            for (int i = 0; i < NBRICKS_PER_ROW; i++) {
                GRect rect = new GRect(BRICK_WIDTH, BRICK_HEIGHT);
                rect.setFillColor(color);
                rect.setFilled(true);
                rect.setLocation(BRICK_SEP + i * (BRICK_WIDTH + BRICK_SEP),
                        BRICK_SEP + j * (BRICK_HEIGHT + BRICK_SEP));
                add(rect);
            }

        }

    }

    /**
     * This Method is called by setupBricks to setup the colour of the bricks in
     * a particular row
     *
     * @param index depecting row number
     * @return Color type object
     */
    private Color getBrickColor(int index) {
        switch (index) {
            case 0:
            case 1:
                return Color.red;
            case 2:
            case 3:
                return Color.orange;
            case 4:
            case 5:
                return Color.yellow;
            case 6:
            case 7:
                return Color.green;
            case 8:
            case 9:
                return Color.cyan;
            default:
                return Color.blue;
        }
    }

    private void setupPaddle() {
        paddle = new GRect(PADDLE_WIDTH, PADDLE_HEIGHT);
        paddle.setFillColor(Color.black);
        paddle.setFilled(true);
        add(paddle, (WIDTH - PADDLE_WIDTH) / 2.0, HEIGHT - PADDLE_Y_OFFSET);
    }

    private void setupBall() {
        ball = new GOval(BALL_RADIUS * 2, BALL_RADIUS * 2);
        ball.setFillColor(Color.black);
        ball.setFilled(true);
        add(ball, WIDTH / 2.0 - BALL_RADIUS, HEIGHT / 2.0 - BALL_RADIUS);
    }

    private void setupWalls() {
        add(new GLine(0, HEIGHT, WIDTH, HEIGHT));
        add(new GLine(WIDTH, 0, WIDTH, HEIGHT));
    }

    private void setupLabels() {
        setupScoreLabel();
        setupTurnsLeftLabel();
        setupGameLoseLabel();
        setupGameWinLabel();
    }

    private void setupScoreLabel() {
        scoreLabel = new GLabel("SCORE: " + score);
        scoreLabel.setFont("SansSerif-20");
        scoreLabel.setLocation(LABEL_ALIGN, UP_SPACE);
        add(scoreLabel);

    }

    private void setupTurnsLeftLabel() {
        turnsLeftLabel = new GLabel("Balls Left: " + turnsLeft);
        turnsLeftLabel.setFont("SansSerif-20");
        turnsLeftLabel.setLocation(LABEL_ALIGN, scoreLabel.getHeight()
                + UP_SPACE);
        add(turnsLeftLabel);

    }

    private void setupGameWinLabel() {
        gameWinLabel = new GLabel("LEVEL CLEARED!! BRAVO!!");
        gameWinLabel.setFont("SansSerif-36");
        gameWinLabel.setColor(Color.black);
    }

    private void setupGameLoseLabel() {
        gameLoseLabel = new GLabel("GAME OVER!!  YOU LOSE!!");
        gameLoseLabel.setFont("SansSerif-36");
        gameLoseLabel.setColor(Color.black);

    }

    public void mouseMoved(MouseEvent e) {
        if (e.getX() >= PADDLE_WIDTH / 2
                && e.getX() <= (WIDTH - PADDLE_WIDTH / 2))
            paddle.move(e.getX() - paddle.getX() - PADDLE_WIDTH / 2.0, 0);
    }

    private void moveBall() {
        ball.move(xVel, yVel);
    }

    private void checkWallCollision() {
        double diff;
        if (ball.getX() + 2 * BALL_RADIUS >= WIDTH) {
            xVel = -xVel;
            diff = (ball.getX() + 2 * BALL_RADIUS - WIDTH);
            ball.move(-2 * diff, 0);
            bounceClip.play();
        } else if (ball.getX() < 0) {
            xVel = -xVel;
            diff = -ball.getX();
            ball.move(2 * diff, 0);
            bounceClip.play();
        } else if (ball.getY() < 0) {
            yVel = -yVel;
            diff = -ball.getY();
            ball.move(0, 2 * diff);
            bounceClip.play();
        }
    }

    private void checkPaddleCollision() {
        if (ball.getY() + 2 * BALL_RADIUS >= paddle.getY()
                && ball.getX() >= paddle.getX()
                && ball.getX() <= paddle.getX() + paddle.getWidth()) {
            yVel = -yVel;
            double diff = (ball.getY() + 2 * BALL_RADIUS - paddle.getY());
            ball.move(0, -2 * diff);
            bounceClip.play();

        }
    }

    private void checkBrickCollision() {

        GObject obj = getElementAt(ball.getX(), ball.getY());
        if (obj != null && obj != ball && obj != paddle) {
            remove(obj);
            yVel = -yVel;
            bounceClip.play();
            bricks--;
            score += SCORE_UNIT;
            remove(scoreLabel);
            setupScoreLabel();
        }
    }

    private void checkTurnOver() {
        if (ball.getY() + 2 * BALL_RADIUS >= BOUNDRY_LINE) {
            turnsLeft--;
            remove(turnsLeftLabel);
            setupTurnsLeftLabel();
            ball.move(WIDTH / 2.0 - ball.getX(), HEIGHT / 2.0 - ball.getY());
            changeBallAndPaddle();
            if (turnsLeft >= 0)
                waitForClick();

        }
    }

    private void changeBallAndPaddle() {
        if (turnsLeft == 1) {
            ball.setFillColor(Color.gray);
            ball.setFilled(true);
            paddle.setFillColor(Color.gray);
            paddle.setFilled(true);
        } else if (turnsLeft == 0) {
            ball.setFillColor(Color.white);
            ball.setFilled(true);
            paddle.setFillColor(Color.white);
            paddle.setFilled(true);
        }
    }

    private boolean gameOver() {
        return (turnsLeft < 0 || bricks == 0);
    }

    private boolean gameWon() {
        return (turnsLeft >= 0 && bricks == 0);
    }

    private void declareWin() {
        removeAll();
        add(gameWinLabel, (APPLICATION_WIDTH - gameWinLabel.getWidth()) / 2,
                (APPLICATION_HEIGHT + gameWinLabel.getAscent()) / 2);
    }

    private void declareLose() {
        removeAll();
        add(gameLoseLabel, (APPLICATION_WIDTH - gameLoseLabel.getWidth()) / 2,
                (APPLICATION_HEIGHT + gameLoseLabel.getAscent()) / 2);
    }
}
