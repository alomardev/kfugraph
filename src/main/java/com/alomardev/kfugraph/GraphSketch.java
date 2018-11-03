package com.alomardev.kfugraph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JComponent;

public class GraphSketch extends JComponent implements ComponentListener,
    MouseListener, MouseMotionListener {

    public interface GraphSketchListener {

        void onAddVertex(Vertex v);

        void onRemoveVertex(Vertex v);

        void onEditVertex(Vertex v);

        void onFromSelected(Vertex v);

        void onToSelected(Vertex v);
    }

    public static final int MODE_READ_ONLY = 0;

    public static final int MODE_INSERT_VERTEX = 1;
    public static final int MODE_REMOVE_VERTEX = 2;
    public static final int MODE_INSERT_EDGE = 3;
    public static final int MODE_REMOVE_EDGE = 4;
    public static final int MODE_MOVE_VERTEX = 5;
    public static final int MODE_VERTEX_PROPERTIES = 6;
    public static final int MODE_EDGE_PROPERTIES = 7;
    public static final int MODE_SELECT_FROM_VERTEX = 8;
    public static final int MODE_SELECT_TO_VERTEX = 9;

    private static final int RADIUS = 5;
    private static final int DIAMETER = RADIUS * 2;

    private static final double RANGE = 15;
    private static final Color FROM_COLOR = Color.GREEN;
    private static final Color TO_COLOR = Color.RED;
    private static final Color FROM_LABEL_COLOR = new Color(0, .65f, 0);
    private static final Color TO_LABEL_COLOR = Color.RED.darker();
    private static final Color INSERT_EDGE_COLOR = new Color(0, .75f, 0, 1f);
    private static final Color REMOVE_EDGE_COLOR = Color.RED;
    private static final Color EDIT_EDGE_COLOR = Color.ORANGE;
    private static final Color CLOSEST_VERTEX_COLOR = Color.PINK;
    private static final Color FLOATING_TEXT_COLOR = new Color(1f, 1f, 1f, .9f);
    private static final Stroke CLOSEST_EDGE_STROKE = new BasicStroke(4);

    private static final Color VISITED_COLOR = Color.RED;
    private static final Color UNVISITED_COLOR = Color.BLUE;
    private static final Color LABEL_COLOR = Color.WHITE;
    private static final Color LABEL_BG_COLOR = new Color(0, 0, .75f, .8f);
    private static final Color COST_COLOR = Color.WHITE;
    private static final Color COST_BG_COLOR = new Color(0xbb444400, true);
    private static final Color VISITED_COST_BG_COLOR = new Color(0xbb330000, true);
    private static final Color VISITED_LABEL_BG_COLOR = new Color(0xbb660000, true);
    private static final BasicStroke EDGE_STROKE = new BasicStroke(2);

    private boolean showLabel, showCost, highlightVertex, highlightEdge;
    private int mode;
    private int[] path;
    private double scale;

    private String title;

    private GraphArea ga;
    private List<Vertex> vertices;
    private List<Edge> edges;
    private Vertex movedVertex, closestVertex, from, to;
    private Edge closestEdge;
    private SimpleLine insertEdgeLine;
    private Image mImg;

    private GraphSketchListener callback;

    private PropertiesDialog.Property[] propertiesVertex = new PropertiesDialog.Property[]{
        new PropertiesDialog.Property("label", "Label:")
    };
    private PropertiesDialog.Property[] propertiesEdge = new PropertiesDialog.Property[]{
        new PropertiesDialog.Property("cost", "Cost:")
    };

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        highlightVertex = mode == MODE_MOVE_VERTEX || mode == MODE_REMOVE_VERTEX || mode == MODE_VERTEX_PROPERTIES
            || mode == MODE_INSERT_EDGE || mode == MODE_SELECT_FROM_VERTEX || mode == MODE_SELECT_TO_VERTEX;
        highlightEdge = mode == MODE_REMOVE_EDGE || mode == MODE_EDGE_PROPERTIES;
        this.mode = mode;
        switch (this.mode) {
            case MODE_SELECT_TO_VERTEX:
                title = "Select the destination vertex";
                break;
            case MODE_SELECT_FROM_VERTEX:
                title = "Select the starting vertex";
                break;
            default:
                title = "";
                break;
        }
        repaint();
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public GraphArea getGraphArea() {
        return ga;
    }

    public void setGraphSketchListener(GraphSketchListener listener) {
        callback = listener;
    }

    public Vertex getFrom() {
        return from;
    }

    public void setFrom(Vertex from) {
        this.from = from == to ? null : from;
    }

    public Vertex getTo() {
        return to;
    }

    public void setTo(Vertex to) {
        this.to = to == from ? null : to;
    }

    public GraphSketch() {
        this(340, 260);
    }

    public GraphSketch(int graphWidth, int graphHeight) {
        vertices = new ArrayList<>();
        edges = new ArrayList<>();
        ga = new GraphArea(0, 0, graphWidth, graphHeight);

        init();
    }

    private void init() {
        setMode(MODE_READ_ONLY);
        setBackground(Color.WHITE);
        setFont(new Font(Font.SERIF, Font.PLAIN, 14));
        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public boolean parseGraph(String data) {
        return parseGraph(data, 0);
    }

    public boolean parseGraph(String data, int padding) {
        clear();
        String ls[];
        int xs[], ys[], shiftX, shiftY;
        shiftX = shiftY = Integer.MAX_VALUE;

        try {
            String[] segs = data.split("\\|");
            String[] first = segs[0].split(";");
            ls = new String[first.length];
            xs = new int[first.length];
            ys = new int[first.length];
            for (int i = 0; i < first.length; i++) {
                String d[] = first[i].split(",");
                ls[i] = d[0];
                xs[i] = Integer.parseInt(d[1]);
                ys[i] = Integer.parseInt(d[2]);

                if (shiftX > xs[i]) {
                    shiftX = xs[i];
                }
                if (shiftY > ys[i]) {
                    shiftY = ys[i];
                }
            }

            if (segs.length > 2) {
                shiftX = shiftY = 0;
            }

            for (int i = 0; i < xs.length; i++) {
                addVertex(ls[i], xs[i] - shiftX + padding, ys[i] - shiftY + padding);
            }

            for (String v : segs[1].split(";")) {
                String d[] = v.split(",");
                addEdge(Integer.parseInt(d[0]) - 1, Integer.parseInt(d[1]) - 1, Double.parseDouble(d[2]));
            }

            if (segs.length > 2) {
                String dims[] = segs[2].split(",");
                ga.setWidth(Integer.parseInt(dims[0]));
                ga.setHeight(Integer.parseInt(dims[1]));
            } else {
                calculateGraphDimension(padding);
            }
            componentResized(null);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public UndirectedGraph getGraph() {
        UndirectedGraph graph = new UndirectedGraph(vertices.size());
        for (Vertex v : vertices) {
            graph.addVertex(v.label);
        }
        for (Edge e : edges) {
            graph.addEdge(vertices.indexOf(e.from), vertices.indexOf(e.to), e.cost);
        }
        return graph;
    }

    public void addVertex(String label, int x, int y) {
        Vertex v = new Vertex(label, x, y);
        vertices.add(v);
        if (callback != null) {
            callback.onAddVertex(v);
        }
    }

    public void removeVertex(String label) {
        for (Vertex v : vertices) {
            if (v.label.equals(label)) {
                removeVertex(v);
            }
        }
    }

    public void removeVertex(int index) {
        removeVertex(vertices.get(index));
    }

    public void removeVertex(Vertex v) {
        path = null;
        if (callback != null) {
            callback.onRemoveVertex(v);
        }
        List<Edge> removeList = new ArrayList<>();
        for (Edge ed : edges) {
            if (ed.from == v || ed.to == v) {
                removeList.add(ed);
            }
        }
        edges.removeAll(removeList);
        vertices.remove(v);
    }

    public boolean addEdge(int from, int to, double cost) {
        Vertex v1 = vertices.get(from);
        Vertex v2 = vertices.get(to);

        if (v1 == v2) {
            return false;
        }

        for (Edge e : edges) {
            if (e.from == v1 && e.to == v2 || e.to == v1 && e.from == v2) {
                return false;
            }
        }

        edges.add(new Edge(v1, v2, cost));

        return true;
    }

    public void removeEdge(Vertex v1, Vertex v2) {
        path = null;
        removeEdge(vertices.indexOf(v1), vertices.indexOf(v2));
    }

    public void removeEdge(int from, int to) {
        Vertex v1 = vertices.get(from);
        Vertex v2 = vertices.get(to);
        for (Edge ed : getEdges()) {
            if (ed.from == v1 && ed.to == v2 || ed.to == v1 && ed.from == v2) {
                getEdges().remove(ed);
                break;
            }
        }
    }

    public void setShowLabel(boolean showLabel) {
        this.showLabel = showLabel;
        repaint();
    }

    public void setShowCost(boolean showCost) {
        this.showCost = showCost;
        repaint();
    }

    public boolean isLabelShown() {
        return showLabel;
    }

    public boolean isCostShown() {
        return showCost;
    }

    public void setPath(int... v) {
        path = v;
        repaint();
    }

    public void clear() {
        path = null;
        from = to = null;
        vertices.clear();
        edges.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        ((Graphics2D) g).setStroke(EDGE_STROKE);

        // Background
        int cx = getInsets().left;
        int cy = getInsets().top;

        g.setColor(getBackground());
        g.fillRect(cx, cy, getContentWidth(), getContentHeight());

        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);

        if (mImg != null) {
            g.drawImage(mImg, getWidth() / 2 - mImg.getWidth(this) / 2, getHeight() / 2 - mImg.getHeight(this) / 2, this);
        }

        if (getContentWidth() < DIAMETER || getContentHeight() < DIAMETER) {
            return;
        }

        // Drawing lines
        for (Edge e : edges) {
            int x1 = ga.getAbsoluteX(e.from.x);
            int y1 = ga.getAbsoluteY(e.from.y);
            int x2 = ga.getAbsoluteX(e.to.x);
            int y2 = ga.getAbsoluteY(e.to.y);

            boolean isvisited = isVisited(e.from, e.to);

            g.setColor(isvisited ? VISITED_COLOR : UNVISITED_COLOR);
            g.drawLine(x1, y1, x2, y2);

            if (showCost && (e != closestEdge || !highlightEdge)) {
                drawText(g, Double.toString(e.cost), (x1 + x2) / 2,
                    (y1 + y2) / 2, 4, isvisited ? VISITED_COST_BG_COLOR : COST_BG_COLOR, COST_COLOR);
            }
        }

        // Drawing vertices/labels
        int i = 0;
        for (Vertex v : vertices) {
            int x = ga.getAbsoluteX(v.x);
            int y = ga.getAbsoluteY(v.y);

            boolean isvisited = isVisited(i);
            if (!showLabel || v.label.isEmpty() || closestVertex != null && closestVertex == v && highlightVertex) {
                int radius = v == from || v == to ? (int) (RADIUS * 1.5) : RADIUS;
                int diameter = v == from || v == to ? (int) (DIAMETER * 1.5) : DIAMETER;
                g.setColor(v == from ? FROM_COLOR : v == to ? TO_COLOR : isvisited ? VISITED_COLOR : UNVISITED_COLOR);
                g.fillOval(x - radius, y - radius, diameter, diameter);
                if (v == from || v == to) {
                    g.setColor(isvisited ? VISITED_COLOR : UNVISITED_COLOR);
                    g.drawOval(x - radius, y - radius, diameter, diameter);
                }
            } else if (!v.label.isEmpty()) {
                drawText(g, v.label, x, y, 8, v == from ? FROM_LABEL_COLOR : v == to ? TO_LABEL_COLOR
                    : isvisited ? VISITED_LABEL_BG_COLOR : LABEL_BG_COLOR, LABEL_COLOR);
            }
            i++;
        }

        // Edit Mode
        if (insertEdgeLine != null) {
            drawLine(g, insertEdgeLine, INSERT_EDGE_COLOR);
        }

        if (closestVertex != null && highlightVertex) {
            g.setColor(CLOSEST_VERTEX_COLOR);
            int x = getGraphArea().getAbsoluteX(closestVertex.x);
            int y = getGraphArea().getAbsoluteY(closestVertex.y);
            int d = (int) (RANGE * 2);
            g.drawOval(x - d / 2, y - d / 2, d, d);
            if (!closestVertex.label.isEmpty()) {
                drawText(g, closestVertex.label, x, y - d / 2 - 12, 4, FLOATING_TEXT_COLOR, Color.BLACK);
            }
        }

        if (closestEdge != null && highlightEdge) {
            Stroke old = ((Graphics2D) g).getStroke();
            ((Graphics2D) g).setStroke(CLOSEST_EDGE_STROKE);
            if (mode == MODE_REMOVE_EDGE) {
                g.setColor(REMOVE_EDGE_COLOR);
            } else {
                g.setColor(EDIT_EDGE_COLOR);
            }
            int x1 = getGraphArea().getAbsoluteX(closestEdge.from.x);
            int y1 = getGraphArea().getAbsoluteY(closestEdge.from.y);
            int x2 = getGraphArea().getAbsoluteX(closestEdge.to.x);
            int y2 = getGraphArea().getAbsoluteY(closestEdge.to.y);
            g.drawLine(x1, y1, x2, y2);
            ((Graphics2D) g).setStroke(old);
        }

        if (!title.isEmpty()) {
            drawText(g, title, getWidth() / 2, getInsets().top + 10, 5, new Color(0, 0, 0, 0), Color.RED);
        }
    }

    private void drawLine(Graphics g, SimpleLine l, Color color) {
        g.setColor(color);
        g.drawLine(
            (int) l.x1,
            (int) l.y1,
            (int) l.x2,
            (int) l.y2
        );
    }

    private void drawText(Graphics g, String text, int x, int y, int padding, Color bg, Color t) {
        FontMetrics fm = g.getFontMetrics();
        int width = fm.stringWidth(text);
        int height = fm.getAscent() + fm.getDescent();
        int bheight = height + padding - fm.getDescent();
        int bwidth = Math.max(width + padding, bheight);
        g.setColor(bg);
        g.fillRoundRect(x - bwidth / 2,
            y - bheight / 2, bwidth, bheight, 10, 10);
        g.setColor(t);
        g.drawString(text, x - width / 2,
            y + height / 2 - fm.getDescent());
    }

    public int getContentWidth() {
        return getWidth() - getInsets().left - getInsets().right;
    }

    public int getContentHeight() {
        return getHeight() - getInsets().top - getInsets().bottom;
    }

    private void calculateGraphDimension(int padding) {
        int minX, minY, maxX, maxY;
        minX = minY = Integer.MAX_VALUE;
        maxX = maxY = Integer.MIN_VALUE;

        for (Vertex v : vertices) {
            if (maxX < v.x) {
                maxX = v.x;
            }
            if (minX > v.x) {
                minX = v.x;
            }
            if (maxY < v.y) {
                maxY = v.y;
            }
            if (minY > v.y) {
                minY = v.y;
            }
        }

        ga.setWidth(Math.abs(maxX - minX) + padding * 2);
        ga.setHeight(Math.abs(maxY - minY) + padding * 2);
    }

    protected void expandGraphArea() {
        int xs[], ys[];
        xs = new int[vertices.size()];
        ys = new int[vertices.size()];

        for (int i = 0; i < vertices.size(); i++) {
            xs[i] = ga.getAbsoluteX(vertices.get(i).x);
            ys[i] = ga.getAbsoluteY(vertices.get(i).y);
        }

        ga.setWidth(getContentWidth());
        ga.setHeight(getContentHeight());
        ga.setScale(1);
        ga.calculateXandY(getWidth(), getHeight());

        for (int i = 0; i < vertices.size(); i++) {
            vertices.get(i).x = xs[i] - ga.getX();
            vertices.get(i).y = ys[i] - ga.getY();
        }
    }

    public void scaleGraph() {
        scaleGraph(20);
    }

    public void scaleGraph(int padding) {
        int shiftX, shiftY;
        shiftX = shiftY = Integer.MAX_VALUE;

        for (Vertex v : vertices) {
            if (shiftX > v.x) {
                shiftX = v.x;
            }
            if (shiftY > v.y) {
                shiftY = v.y;
            }
        }

        for (Vertex v : vertices) {
            v.x -= shiftX - padding;
            v.y -= shiftY - padding;
        }

        calculateGraphDimension(padding);
        componentResized(null);
        repaint();
    }

    public void setImage(Image img) {
        mImg = img;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(ga.getWidth() + DIAMETER + getInsets().left + getInsets().right,
            ga.getHeight() + DIAMETER + getInsets().top + getInsets().bottom);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        double scaleX = (double) getContentWidth() / (double) ga.getWidth();
        double scaleY = (double) getContentHeight() / (double) ga.getHeight();
        ga.setScale(Math.min(scaleX, scaleY));
        ga.calculateXandY(getWidth(), getHeight());
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

    private void findClosestVertex(int x, int y) {
        closestVertex = getClosestVertex(x, y, RANGE);
        repaint();
    }

    private void findClosestEdge(int x, int y) {
        closestEdge = getClosestEdge(x, y, RANGE / 1.5);
        repaint();
    }

    protected Vertex getClosestVertex(int x, int y, double range) {
        Vertex closest = null;
        double distance = Double.MAX_VALUE;
        for (Vertex v : vertices) {
            int xd = Math.abs(x - ga.getAbsoluteX(v.x));
            int yd = Math.abs(y - ga.getAbsoluteY(v.y));
            double newDistance = (int) Math.sqrt(xd * xd + yd * yd);
            if (newDistance < distance && newDistance < range) {
                distance = newDistance;
                closest = v;
            }
        }

        return closest;
    }

    protected Edge getClosestEdge(int x, int y, double range) {
        Edge closest = null;
        double distance = Double.MAX_VALUE;
        for (Edge e : edges) {
            int x1 = ga.getAbsoluteX(e.from.x);
            int y1 = ga.getAbsoluteY(e.from.y);
            int x2 = ga.getAbsoluteX(e.to.x);
            int y2 = ga.getAbsoluteY(e.to.y);

            int minx = Math.min(x1, x2) - (int) range / 2;
            int maxx = Math.max(x1, x2) + (int) range / 2;
            int miny = Math.min(y1, y2) - (int) range / 2;
            int maxy = Math.max(y1, y2) + (int) range / 2;

            if (x > maxx || x < minx || y > maxy || y < miny) {
                continue;
            }

            double newDistance = Line2D.ptLineDist(x1, y1, x2, y2, x, y);
            if (newDistance < distance && newDistance < range) {
                distance = newDistance;
                closest = e;
            }
        }
        return closest;
    }

    private boolean isVisited(Vertex f, Vertex t) {
        if (path == null) {
            return false;
        }
        for (int i = 0; i < path.length - 1; i++) {
            if (vertices.indexOf(f) == path[i] && vertices.indexOf(t) == path[i + 1]) {
                return true;
            }
            if (vertices.indexOf(t) == path[i] && vertices.indexOf(f) == path[i + 1]) {
                return true;
            }
        }
        return false;
    }

    private boolean isVisited(int i) {
        if (path == null) {
            return false;
        }
        for (int p : path) {
            if (i == p) {
                return true;
            }
        }

        return false;
    }

    protected static class Vertex {

        protected String label;
        protected int x, y;

        protected Vertex(String label, int x, int y) {
            this.label = label;
            this.x = x;
            this.y = y;
        }
    }

    protected static class Edge {

        protected Vertex from, to;
        protected double cost;

        protected Edge(Vertex from, Vertex to, double cost) {
            this.from = from;
            this.to = to;
            this.cost = cost;
        }
    }

    protected static class GraphArea {

        private double scale;
        private int x, y, width, height;

        protected GraphArea() {
            this(0, 0, 0, 0);
        }

        protected GraphArea(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;

            scale = 1;
        }

        protected void calculateXandY(int width, int height) {
            x = width / 2 - getScaledWidth() / 2;
            y = height / 2 - getScaledHeight() / 2;
        }

        public double getScale() {
            return scale;
        }

        public void setScale(double scale) {
            this.scale = scale;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        protected int getScaledWidth() {
            return (int) (width * scale);
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        protected int getScaledHeight() {
            return (int) (height * scale);
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        protected int getAbsoluteX(int x) {
            return (int) (this.x + x * scale);
        }

        protected int getAbsoluteY(int y) {
            return (int) (this.y + y * scale);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        switch (mode) {
            case MODE_SELECT_FROM_VERTEX:
                setFrom(closestVertex);
                if (callback != null) {
                    callback.onFromSelected(closestVertex);
                }
                repaint();
                break;
            case MODE_SELECT_TO_VERTEX:
                setTo(closestVertex);
                if (callback != null) {
                    callback.onToSelected(closestVertex);
                }
                repaint();
                break;
            case MODE_REMOVE_VERTEX:
                if (closestVertex != null) {
                    removeVertex(closestVertex);
                    findClosestVertex(e.getX(), e.getY());
                    repaint();
                }
                break;
            case MODE_REMOVE_EDGE:
                if (closestEdge != null) {
                    removeEdge(closestEdge.from, closestEdge.to);
                    findClosestEdge(e.getX(), e.getY());
                    repaint();
                }
                break;
            case MODE_VERTEX_PROPERTIES:
                if (closestVertex == null) {
                    break;
                }

                Vertex targetVertex = closestVertex;
                closestVertex = null;
                repaint();
                propertiesVertex[0].setValue(targetVertex.label);
                PropertiesDialog vd = new PropertiesDialog("Vertex Properties", propertiesVertex);
                vd.setPropertiesListener(new PropertiesDialog.PropertiesListener() {
                    @Override
                    public void onPositive(HashMap<String, String> values) {
                        targetVertex.label = values.get("label");
                        if (callback != null) {
                            callback.onEditVertex(targetVertex);
                        }
                        repaint();
                    }
                });
                vd.setLocation(this, e);
                vd.setVisible(true);
                break;
            case MODE_EDGE_PROPERTIES:
                if (closestEdge == null) {
                    break;
                }

                Edge targetEdge = closestEdge;
                closestEdge = null;
                repaint();
                propertiesEdge[0].setValue(targetEdge.cost + "");
                PropertiesDialog ed = new PropertiesDialog("Edge Properties", propertiesEdge);
                ed.setPropertiesListener(new PropertiesDialog.PropertiesListener() {
                    @Override
                    public void onPositive(HashMap<String, String> values) {
                        try {
                            targetEdge.cost = Double.parseDouble(values.get("cost"));
                            repaint();
                        } catch (NumberFormatException ex) {
                            System.err.println("NumberFormatException: " + ex.getMessage());
                        }
                    }
                });
                ed.setLocation(this, e);
                ed.setVisible(true);
                break;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        switch (mode) {
            case MODE_INSERT_VERTEX:
                if (e.getX() > getInsets().left
                    && e.getX() < getWidth() - getInsets().right
                    && e.getY() > getInsets().top
                    && e.getY() < getHeight() - getInsets().bottom) {
                    expandGraphArea();
                    addVertex("V" + getVertices().size(), e.getX() - getGraphArea().getX(), e.getY() - getGraphArea().getY());
                    repaint();
                }
                break;
            case MODE_INSERT_EDGE:
                if (closestVertex != null) {
                    insertEdgeLine = new SimpleLine();
                    insertEdgeLine.x1 = getGraphArea().getAbsoluteX(closestVertex.x);
                    insertEdgeLine.y1 = getGraphArea().getAbsoluteY(closestVertex.y);
                }
                break;
            case MODE_MOVE_VERTEX:
                movedVertex = closestVertex;
                if (movedVertex != null) {
                    expandGraphArea();
                }
                repaint();
                break;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        switch (mode) {
            case MODE_INSERT_EDGE:
                if (insertEdgeLine != null) {
                    Vertex v1 = getClosestVertex(insertEdgeLine.x1, insertEdgeLine.y1, RANGE);
                    Vertex v2 = closestVertex;
                    if (v1 != null && v2 != null) {
                        addEdge(getVertices().indexOf(v1), getVertices().indexOf(v2), 1);
                    }
                    insertEdgeLine = null;
                    repaint();
                }
                break;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        findClosestVertex(e.getX(), e.getY());
        switch (mode) {
            case MODE_INSERT_EDGE:
                if (insertEdgeLine != null) {
                    insertEdgeLine.x2 = e.getX();
                    insertEdgeLine.y2 = e.getY();
                    repaint();
                }
                break;
            case MODE_MOVE_VERTEX:
                if (movedVertex != null) {
                    movedVertex.x = e.getX() - getGraphArea().getX();
                    movedVertex.y = e.getY() - getGraphArea().getY();
                }
                repaint();
                break;
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        findClosestVertex(e.getX(), e.getY());
        findClosestEdge(e.getX(), e.getY());
    }

    public static class SimpleLine {

        public int x1, x2, y1, y2;
    }

}
