package snakepackage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import enums.GridSize;

public class SnakeApp {

    private static SnakeApp app;
    public static final int MAX_THREADS = 8;
    Snake[] snakes = new Snake[MAX_THREADS];
    private static final Cell[] spawn = {
        new Cell(1, (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell(GridSize.GRID_WIDTH - 2, 3 * (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell(3 * (GridSize.GRID_WIDTH / 2) / 2, 1),
        new Cell((GridSize.GRID_WIDTH / 2) / 2, GridSize.GRID_HEIGHT - 2),
        new Cell(1, 3 * (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell(GridSize.GRID_WIDTH - 2, (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell((GridSize.GRID_WIDTH / 2) / 2, 1),
        new Cell(3 * (GridSize.GRID_WIDTH / 2) / 2, GridSize.GRID_HEIGHT - 2)
    };
    private JFrame frame;
    private static Board board;
    int nr_selected = 0;
    Thread[] thread = new Thread[MAX_THREADS];
    private boolean running = false;

    private JLabel longestSnakeLabel;
    private JLabel worstSnakeLabel;


    public SnakeApp() {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        frame = new JFrame("The Snake Race");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(GridSize.GRID_WIDTH * GridSize.WIDTH_BOX + 17,
                GridSize.GRID_HEIGHT * GridSize.HEIGH_BOX + 40);
        frame.setLocation(dimension.width / 2 - frame.getWidth() / 2,
                dimension.height / 2 - frame.getHeight() / 2);
        board = new Board();
        
        frame.add(board, BorderLayout.CENTER);
        
        // Crear el panel de acciones y añadir los botones
        JPanel actionsPanel = new JPanel();
        actionsPanel.setLayout(new FlowLayout());

        JButton startButton = new JButton("INICIAR");
        JButton pauseButton = new JButton("PAUSAR");
        JButton resumeButton = new JButton("REANUDAR");

        actionsPanel.add(startButton);
        actionsPanel.add(pauseButton);
        actionsPanel.add(resumeButton);

        // Añadir el panel de acciones al sur del frame
        frame.add(actionsPanel, BorderLayout.SOUTH);

        // Añadir ActionListeners a los botones
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startSnakes();
            }
        });

        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pauseSnakes();
            }
        });

        resumeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resumeSnakes();
            }
        });

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(2, 1));

        longestSnakeLabel = new JLabel("Serpiente viva más larga: ");
        worstSnakeLabel = new JLabel("Peor serpiente: ");

        infoPanel.add(longestSnakeLabel);
        infoPanel.add(worstSnakeLabel);

        frame.add(infoPanel, BorderLayout.NORTH);

        frame.setVisible(true);
    }

    private void startSnakes() {
        if (!running) {
            for (int i = 0; i < MAX_THREADS; i++) {
                snakes[i] = new Snake(i + 1, spawn[i], i + 1);
                snakes[i].addObserver(board);
                thread[i] = new Thread(snakes[i]);
                thread[i].start();
            }
            running = true;
        }
    }

    private void pauseSnakes() {
        for (Snake snake : snakes) {
            if (snake != null) {
                snake.pause();
            }
        }
        updateInfo();
        board.repaint();
    }

    private void resumeSnakes() {
        for (Snake snake : snakes) {
            if (snake != null) {
                snake.resume();
            }
        }
        
    }

    private void updateInfo() {
        Snake longestSnake = null;
        Snake worstSnake = null;
        int maxLength = 0;
        int earliestDeath = Integer.MAX_VALUE;

        for (Snake snake : snakes) {
            if (snake != null) {
                if (!snake.isSnakeEnd() && snake.getLength() > maxLength) {
                    maxLength = snake.getLength();
                    longestSnake = snake;
                }
                if (snake.isSnakeEnd() && snake.getDeathTime() < earliestDeath) {
                    earliestDeath = snake.getDeathTime();
                    worstSnake = snake;
                }
            }
        }
        for (Snake snake : snakes) {
            if (snake != null) {
                if (snake == longestSnake) {
                    snake.setColor(Color.BLUE); // Color para la serpiente más larga
                } else if (snake == worstSnake) {
                    snake.setColor(Color.RED); // Color para la peor serpiente
                } else {
                    snake.setColor(Color.GREEN); // Color original para las demás serpientes
                }
            }
        }

        if (longestSnake != null) {
            longestSnakeLabel.setText("Serpiente viva más larga: " + longestSnake.getId());
        } else {
            longestSnakeLabel.setText("Serpiente viva más larga: N/A");
        }

        if (worstSnake != null) {
            worstSnakeLabel.setText("Peor serpiente: " + worstSnake.getId());
        } else {
            worstSnakeLabel.setText("Peor serpiente: N/A");
        }
    }

    public static void main(String[] args) {
        app = new SnakeApp();
        app.init();
    }

    private void init() {
        for (int i = 0; i != MAX_THREADS; i++) {
            snakes[i] = new Snake(i + 1, spawn[i], i + 1);
            snakes[i].addObserver(board);
            thread[i] = new Thread(snakes[i]);
            thread[i].start();
        }

        frame.setVisible(true);

        while (true) {
            int x = 0;
            for (int i = 0; i != MAX_THREADS; i++) {
                if (snakes[i].isSnakeEnd()) {
                    x++;
                }
            }
            if (x == MAX_THREADS) {
                break;
            }
        }

        System.out.println("Thread (snake) status:");
        for (int i = 0; i != MAX_THREADS; i++) {
            System.out.println("[" + i + "] :" + thread[i].getState());
        }
    }

    public static SnakeApp getApp() {
        return app;
    }
}
