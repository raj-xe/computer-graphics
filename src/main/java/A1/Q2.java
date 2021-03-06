package A1;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.*;

class BlockConfig {
    int type;
    int colorType;
    int x;
    int y;
    int rotation;

    public BlockConfig(int x, int y, int type, int colorType) {
        this.type = type;
        this.colorType = colorType;
        this.x = x;
        this.y = y;
        rotation = 0;
    }

    public BlockConfig(int x, int y, int type, int colorType, int rotation) {
        this.type = type;
        this.colorType = colorType;
        this.x = x;
        this.y = y;
        this.rotation = rotation;
    }
}

class SingleBlockConfig {
    int colorType;
    int x;
    int y;

    @Override
    public String toString() {
        return "SingleBlockConfig{" +
                "colorType=" + colorType +
                ", x=" + x +
                ", y=" + y +
                '}';
    }

    public SingleBlockConfig(int colorType, int x, int y) {
        this.colorType = colorType;
        this.x = x;
        this.y = y;
    }

    public SingleBlockConfig setColorType(int colorType) {
        this.colorType = colorType;
        return this;
    }

    public SingleBlockConfig setX(int x) {
        this.x = x;
        return this;
    }

    public SingleBlockConfig setY(int y) {
        this.y = y;
        return this;
    }
}


public class Q2 {

    final static Boolean[] isPaused = {false};
    final static Boolean[] isOver = {false};

    static JFrame frame;

    static BlockConfig[] blocks;

    static int cx, cy;

    static BlockConfig currentBlock, nextBlock;

    static Graphics g;

    static float single_unit;

    static ArrayList<SingleBlockConfig> occupied;

    static void init() {
        cx = 3;
        cy = 2;
    }

    static int currentType, nextType, currentRotation;

    static boolean toLeft, toRight;

    static boolean rotateLeft, rotateRight;

    static class SubThread implements Runnable {

        Thread dropThread;

        public SubThread() {
            this.dropThread = new Thread(this,"Drop the block thread");
        }

        @Override
        public void run() {
            // GameLoop
            while(true){
                try{

                    if(isPaused[0] || isOver[0]) {
                        dropThread.sleep(100);
                        continue;
                    }

                    if(currentBlock == null) {
                        dropThread.sleep(100);
                        continue;
                    }

                    if(isBlockCollidingNow(null)) {
                        int[][] coords = Mapper.rotate(Mapper.getByType(currentBlock.type),currentRotation);
                        for (
                                int[] c: coords
                            ) {
                            occupied.add(new SingleBlockConfig(currentBlock.colorType, cx+c[0], cy+c[1]));
                        }
                        reInit();
                        continue;
                    }


                    if(toLeft) {
                        int[][] coords = Mapper.rotate(Mapper.getByType(currentBlock.type),currentRotation);
                        boolean flg = false;
                        for (int[] c: coords) {
                            if(c[0] + cx <= 0 || (occupied != null && occupied.stream().anyMatch(block -> (block.x <= c[0] + cx - 1 && block.y ==  c[1] + cy + 1)))){
                                flg = true;
                            }
                        }

                        if(cx > -1 && !flg)
                            cx--;
                        toLeft = false;
                    } else if(toRight) {
                        int[][] coords = Mapper.rotate(Mapper.getByType(currentBlock.type),currentRotation);
                        boolean flg = false;
                        for (int[] c: coords) {
                            if(c[0] + cx >= 9 || (occupied != null && occupied.stream().anyMatch(block -> (block.x >= c[0] + cx + 1 && block.y ==  c[1] + cy + 1)))){
                                flg = true;
                            }
                        }

                        if(cx < 9 && !flg)
                            cx++;
                        toRight = false;
                    }

                    if(rotateLeft) {
                        if(!isBlockCollidingNow(Mapper.rotate(Mapper.getByType(currentBlock.type),(currentRotation + 1) % 4))){
                            currentRotation = (currentRotation + 1) % 4;
                        }
                        rotateLeft = false;
                    }
                    if(rotateRight) {
                        var new_rotation = 0;
                        if(currentRotation == 0) {
                            new_rotation = 3;
                        } else {
                            new_rotation=currentRotation-1;
                        }
                        if(!isBlockCollidingNow(Mapper.rotate(Mapper.getByType(currentBlock.type),new_rotation))) {
                            currentRotation = new_rotation;
                        }
                        rotateRight = false;
                    }

                    cy++;

                    dropThread.sleep(500);

                    frame.repaint();

                    if(g != null)
                        draw(g,single_unit);

                } catch (Exception e) {
                    System.out.println("ERROR IN DROP THREAD:"+e);
                    e.printStackTrace();
                }
            }
        }

        boolean isBlockCollidingNow(int[][] coords){
            if(coords == null){
                coords = Mapper.rotate(Mapper.getByType(currentBlock.type),currentRotation);
            }
            int cy_next = cy+1;
            for (int[] c:
                 coords) {
                if((occupied != null && occupied.stream().anyMatch(block -> (block.x == c[0]+cx && block.y ==  c[1]+cy_next))) || c[1] + cy_next > 19){
                    return true;
                }
            }
            return false;
        }

        public void start() {
            dropThread.start();
            System.out.println(dropThread);
            System.out.println("starting a sub thread: drop thread");
        }
    }

    public static void reInit(){
        if(isOver[0]) {
            return;
        }
        if(occupied.stream().anyMatch(a -> (a.x == 3 && a.y == 5))){
            isOver[0] = true;
            return;
        }
        cx = 3;
        cy = 2;
        currentType = nextType;
        nextType = (int) (Math.random() * 10);
        currentRotation=0;
        frame.repaint();
    }



    public static void main(String[] args) {
        occupied = new ArrayList<>();
        final float ratio = (float) (1.7/2);
        int height = 600;
        Dimension d = new Dimension((int) (height * ratio),height);
        frame = new JFrame();
        frame.setSize(d);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        toLeft = false;
        toRight = false;
        rotateLeft = false;
        rotateRight = false;

        currentRotation = 0;

        init();

        currentType = (int) (Math.random() * 10);
        nextType= (int) (Math.random() * 10);

        SubThread dropThread = new SubThread();
        dropThread.start();

        JPanel panel = new JPanel() {

            protected void paintComponent(Graphics g_) {

                blocks = new BlockConfig[] {
                        new BlockConfig(12,1, nextType, (int) nextType),
                };

                currentBlock =
                        new BlockConfig(cx,cy, currentType, currentType,currentRotation);

                single_unit = (float) getHeight() / 20;
                int single_unit_int = (int) single_unit;
                this.setSize((int) (getHeight() * ratio + 10), getHeight());

                g = g_;

                if(isOver[0]) {
                    Color prev = g.getColor();
                    g.setColor(Color.white);
                    g.fillRect(single_unit_int * 2, (int)(single_unit_int * 9.25), (int) (single_unit_int * 5.5), single_unit_int * 1);
                    g.setColor(Color.RED);
                    g.setFont(new Font("arial",100, (int) (single_unit * 0.7)));
                    g.drawString("GAME OVER", (int)(single_unit_int * 2.5), single_unit_int * 10);
                    g.drawRect(single_unit_int * 2, (int)(single_unit_int * 9.25), (int) (single_unit_int * 5.5), single_unit_int * 1);
                    g.setColor(prev);
                } else {
                    try{
                        occupied.forEach(block -> {
                            Color prevColor = g.getColor();
                            g.setColor(Mapper.getColorByType(block.colorType));
                            g.fillRect((block.x) * single_unit_int,(block.y) * single_unit_int, single_unit_int, single_unit_int);
                            g.setColor(prevColor);
                            g.drawRect((block.x) * single_unit_int,(block.y) * single_unit_int, single_unit_int, single_unit_int);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                draw(g, single_unit);



                if(isPaused[0] && !isOver[0]) {
                    Color prev = g.getColor();
                    g.setColor(Color.white);
                    g.fillRect(single_unit_int * 4, (int)(single_unit_int * 9.25), (int) (single_unit_int * 2.7), single_unit_int * 1);
                    g.setColor(Color.BLUE);
                    g.drawString("PAUSE", (int)(single_unit_int * 4.2), single_unit_int * 10);
                    g.drawRect(single_unit_int * 4, (int)(single_unit_int * 9.25), (int) (single_unit_int * 2.7), single_unit_int * 1);
                    g.setColor(prev);
                }



                g.drawString("QUIT", (int) (single_unit * 11.5), (int) single_unit * 16);
                g.drawRect(single_unit_int * 11, (int)(single_unit_int * 15.25), (int) (single_unit_int * 2.7), single_unit_int * 1);

                MouseMotionAdapter a = new MouseMotionAdapter() {
                    @Override
                    public void mouseMoved(MouseEvent e) {
                        super.mouseMoved(e);

                        if(e.getX() <= (single_unit_int * 10 + 5) && e.getY() <= (single_unit_int*20)) {
                            isPaused[0] = true;
                        } else {
                            isPaused[0] = false;
                        }

//                        frame.repaint();
                    }
                };

                MouseAdapter q = new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // for exit button
                        if(
                                e.getX() >= (single_unit_int * 11)
                                && e.getY() >= (single_unit_int * 15.8)
                                && e.getX() <= (single_unit_int * 13.8)
                                && e.getY() <= (single_unit_int * 17.25)
                        ) {
                            System.exit(0);
                        }

                        if(isPaused[0]) {
                            return;
                        }

                        // for moving the block on left or right
                        if(e.getButton() == 1) {
                            // move left
                            toLeft = true;
                        } else if(e.getButton() == 3) {
                            // move right
                            toRight = true;
                        }
                    }
                };

                frame.addMouseMotionListener(a);
                frame.addMouseListener(q);
                frame.addMouseWheelListener(e -> {
                    if(e.getWheelRotation() == -1){
                        rotateLeft = true;
                    } else if (e.getWheelRotation() == 1) {
                        rotateRight = true;
                    }
                });
            }
        };

        panel.setVisible(true);

        frame.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                Rectangle b = e.getComponent().getBounds();
                e.getComponent().setBounds(b.x, b.y, (int) (b.height * ratio + 10), b.height);
            }

            @Override
            public void componentMoved(ComponentEvent e) {

            }

            @Override
            public void componentShown(ComponentEvent e) {

            }

            @Override
            public void componentHidden(ComponentEvent e) {

            }
        });

        frame.add(panel);

    }

    public static void draw(Graphics g, float single_unit){

        int offset = 0;
        // big rectangle
        g.drawRect(offset,offset,(int) single_unit*10, (int) single_unit*20);

        // small rectangle
        g.drawRect((int) single_unit * 11, offset, (int) single_unit * 6, (int) single_unit * 6);

        g.setFont(new Font("arial",100, (int) (single_unit * 0.7)));
        g.drawString("Level: 1", (int) single_unit * 11, (int) single_unit * 7);
        g.drawString("Lines: 0", (int) single_unit * 11, (int) single_unit * 8);
        g.drawString("Score: 0", (int) single_unit * 11, (int) single_unit * 9);

        if(isOver[0]) {
            return;
        }

        for(BlockConfig config: blocks) {
            new TetrisBlock(config.x, config.y, config.type, config.type, g, (int) single_unit, 0);
        }

        TetrisBlock cb = new TetrisBlock(currentBlock.x, currentBlock.y, currentBlock.type, currentBlock.type, g, (int) single_unit, currentBlock.rotation);
//        cb.rotate(Mapper.getByType(cb.type),1);
//        g.drawRect( (int) (cx * single_unit),(int) (cy * single_unit),5,5); // debug point

    }
}

class Mapper {
    static private final int[][][] coords = {
            {{0,0}, {1,0}, {2,0}, {3,0}}, // linear horizontal
            {{0,0}, {0,1}, {1,1}, {2,1}}, // L shape - v1
            {{0,1}, {1,1}, {2,1}, {2,0}}, // L shape - v2
            {{0,0}, {0,1}, {1,0}, {1,1}}, // square
            {{1,0}, {2,0}, {0,1}, {1,1}}, // zig-zag left - top to bottom right
            {{0,0}, {1,0}, {1,1}, {2,1}}, // zig-zag right - top to bottom left
            {{0,1}, {1,0}, {2,1}, {1,1}}, // T shaped
    };

    static Color[] colors = {
            Color.GREEN,
            new Color(102,0,153),
            Color.RED,
            new Color(3, 177, 252),
            Color.YELLOW,
            new Color(2, 102, 224),
            new Color(255, 170, 0)
    };

    static int[][][] getAll() {
        return coords;
    }

    static int[][] getByType(int type) {
        return coords[type % coords.length];
    }

    static Color[] getAllColors(){
        return colors;
    }

    static Color getColorByType(int type) {
        return colors[type % colors.length];
    }

    static int[][] rotate(int[][] raw, int n){
        int[][] rotated = rotate_without_translation(raw, n);

        if(n == 0) {
            return rotated;
        }

        int[] min = new int[]{3,3};

        for (int i = 0; i < rotated.length; i++) {


            if(rotated[i][0] < min[0]) {
                min[0] = rotated[i][0];
            }

            if(rotated[i][1] < min[1]) {
                min[1] = rotated[i][1];
            }
        }

        if(min[0]>0 || min[1]>0){
            for (int j = 0; j < rotated.length; j++) {
                rotated[j][0] -= min[0] ;
                rotated[j][1] -= min[1] ;
            }
        }

        return rotated;
    }

    static int[][] rotate_without_translation(int[][] raw,int n) {

        if(n == 0) {
            return raw;
        }

        int[][] rotated = new int[raw.length][2];

        int i = 0;

        for(int[] p: raw) {
            if (Arrays.equals(new int[]{0, 0}, p)) {
                rotated[i] = new int[]{0, 3};
            }
            else if (Arrays.equals(new int[]{1, 0}, p)) {
                rotated[i] = new int[]{0, 2};
            }
            else if (Arrays.equals(new int[]{2, 0}, p)) {
                rotated[i] = new int[]{0, 1};
            }
            else if (Arrays.equals(new int[]{3, 0}, p)) {
                rotated[i] = new int[]{0, 0};
            }
            else if (Arrays.equals(new int[]{0, 1}, p)) {
                rotated[i] = new int[]{1, 3};
            }
            else if (Arrays.equals(new int[]{1, 1}, p)) {
                rotated[i] = new int[]{1, 2};
            }
            else if (Arrays.equals(new int[]{2, 1}, p)) {
                rotated[i] = new int[]{1, 1};
            }
            else if (Arrays.equals(new int[]{3, 1}, p)) {
                rotated[i] = new int[]{1, 0};
            }
            else if (Arrays.equals(new int[]{0, 2}, p)) {
                rotated[i] = new int[]{2, 3};
            }
            else if (Arrays.equals(new int[]{1, 2}, p)) {
                rotated[i] = new int[]{2, 2};
            }
            else if (Arrays.equals(new int[]{2, 2}, p)) {
                rotated[i] = new int[]{2, 1};
            }
            else if (Arrays.equals(new int[]{3, 2}, p)) {
                rotated[i] = new int[]{2, 0};
            }
            else if (Arrays.equals(new int[]{0, 3}, p)) {
                rotated[i] = new int[]{3, 3};
            }
            else if (Arrays.equals(new int[]{1, 3}, p)) {
                rotated[i] = new int[]{3, 2};
            }
            else if (Arrays.equals(new int[]{2, 3}, p)) {
                rotated[i] = new int[]{3, 1};
            }
            else if (Arrays.equals(new int[]{3, 3}, p)) {
                rotated[i] = new int[]{3, 0};
            }
            i++;
        }

        return rotate(rotated,n-1);
    }
}

class TetrisBlock {
    int x, y, type, colorType;
    private Color color;
    Graphics g;
    int single_unit;
    int rotation;

    public TetrisBlock(int x, int y, int type, int colorType, Graphics g, int single_unit, int rotation) {
        this.x = x;
        this.y = y;
        this.type = type % Mapper.getAll().length;
        this.colorType = colorType % Mapper.getAllColors().length;
        this.color = Mapper.getColorByType(this.colorType);
        this.g = g;
        this.single_unit = single_unit;
        this.rotation = rotation;
        draw();
    }

    public void draw(){
        int[][] raw_points = Mapper.getByType(type);
        for(int[] point : Mapper.rotate(raw_points,rotation)){
            int x_ = point[0], y_ = point[1];
            Color prevColor = g.getColor();
            g.setColor(color);
            g.fillRect((x+x_) * single_unit,(y+y_) * single_unit, single_unit, single_unit);
            g.setColor(prevColor);
            g.drawRect((x+x_) * single_unit,(y+y_) * single_unit, single_unit, single_unit);
        }
    }
}
