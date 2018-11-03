package com.alomardev.kfugraph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

public class GraphFrame extends JFrame implements ActionListener, ItemListener, GraphSketch.GraphSketchListener {

    private GraphSketch gs;

    private JTable table;
    private JCheckBox showLabelCb, showCostCb;
    private JComboBox editModeBox, algoBox;
    private JButton genBtn, fromBtn, toBtn, clearLogBtn, execBtn, scaleBtn, randomBtn, addImgBtn, rmvImgBtn;
    private IconButton importBtn, exportBtn, newBtn;
    private JTextArea logArea;
    private JFileChooser fileChooser;

    private GraphUtils.Analysis analysis;
    private FileFilter graphFilter, imageFilter;

    public GraphFrame(GraphSketch gs) {
        this.gs = gs;
        analysis = new GraphUtils.Analysis();
        graphFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                boolean isGraphFile;
                String name = f.getName();
                int len = name.length();
                if (isGraphFile = len > 6) {
                    String ext = name.substring(len - 6).toLowerCase();
                    isGraphFile = ext.equals(".graph");
                }
                return f.isDirectory() || isGraphFile;
            }

            @Override
            public String getDescription() {
                return "Graph File";
            }
        };
        imageFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                boolean isImage;
                String name = f.getName();
                int len = name.length();
                if (isImage = len > 4) {
                    String ext = name.substring(len - 4).toLowerCase();
                    isImage = ext.equals(".png") || ext.equals(".jpg") || ext.equals(".bmp");
                }
                return f.isDirectory() || isImage;
            }

            @Override
            public String getDescription() {
                return "Image File";
            }
        };
        init();
    }

    private void init() {
        fileChooser = new JFileChooser();
        showLabelCb = new JCheckBox("Show label");
        showCostCb = new JCheckBox("Show cost");
        editModeBox = new JComboBox(new String[]{"Read Only", "Insert Vertex", "Remove Vertex", "Insert Edge", "Remove Edge",
            "Move Vertex", "Vertex Properties", "Edge Properties"});
        algoBox = new JComboBox(new String[]{"Uniform Cost Search", "A* Algorithm", "Greedy Best First Search"});
        table = new JTable(new HeuristicTableModel(gs));
        logArea = new JTextArea(10, 0);
        genBtn = new JButton("Generate");
        fromBtn = new JButton("??");
        toBtn = new JButton("??");
        clearLogBtn = new JButton("Clear");
        execBtn = new JButton("Execute");
        scaleBtn = new JButton("Scale Graph");
        randomBtn = new JButton("Generate Graph");
        importBtn = new IconButton("ic_import.png");
        exportBtn = new IconButton("ic_export.png");
        newBtn = new IconButton("ic_new_graph.png");
        addImgBtn = new JButton("Add Image");
        rmvImgBtn = new JButton("Remove Image");

        JLabel tableLbl = new JLabel("Heuristics");
        JLabel modeLbl = new JLabel("Mode:");
        JLabel algoLbl = new JLabel("Shortest Path Algorithm");
        JLabel logLbl = new JLabel("Log");
        JLabel fromLbl = new JLabel("From");
        JLabel toLbl = new JLabel("To");

        JScrollPane tableSp = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        table.setFillsViewportHeight(true);
        JScrollPane logSp = new JScrollPane(logArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        TableCellRenderer renderer = (JTable table1, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) -> {
            JLabel label = new JLabel();
            if (value != null) label.setText(value.toString());
            int padding = LayoutUtils.getPad(1);
            label.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
            label.setHorizontalAlignment(SwingConstants.LEFT);
            if (table1.isEnabled() && isSelected && column == 1) {
                label.setOpaque(true);
                label.setBackground(table1.getSelectionBackground());
                label.setForeground(table1.getSelectionForeground());
            } else if (!table1.isEnabled()) {
                label.setOpaque(true);
                label.setBackground(GraphFrame.this.getBackground());
            }
            return label;
        };

        // Graph area
        gs.setBorder(new Border() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {

                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_OFF);

                g.setColor(Color.BLACK);
                g.drawRect(x + 10, y + 10, width - 20, height - 21);
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(12, 12, 13, 12);
            }

            @Override
            public boolean isBorderOpaque() {
                return true;
            }
        });
        GridBagConstraints c = new GridBagConstraints();

        // Top
        JPanel topPanel = new JPanel(new GridBagLayout());

        LayoutUtils.gbc(c, 0, 0, 1, 1, 0, 0, -1, false, false, 2, 2, 1, 2);
        topPanel.add(newBtn, c);
        LayoutUtils.gbc(c, 1, 0, 1, 1, 0, 0, -1, false, false, 0, 2, 1, 2);
        topPanel.add(importBtn, c);
        LayoutUtils.gbc(c, 2, 0, 1, 1, 0, 0, -1, false, false, 0, 2, 1, 2);
        topPanel.add(exportBtn, c);

        LayoutUtils.gbc(c, 3, 0, 1, 1, 0, 0, -1, false, true, 0, 2, 0, 2);
        topPanel.add(new JSeparator(JSeparator.VERTICAL), c);

        LayoutUtils.gbc(c, 4, 0, 1, 1, 0, 0, -1, false, false, 2, 2, 1, 2);
        topPanel.add(addImgBtn, c);
        LayoutUtils.gbc(c, 5, 0, 1, 1, 0, 0, -1, false, false, 0, 2, 1, 2);
        topPanel.add(rmvImgBtn, c);
        LayoutUtils.gbc(c, 6, 0, 1, 1, 0, 0, -1, false, false, 0, 2, 1, 2);
        topPanel.add(scaleBtn, c);
        LayoutUtils.gbc(c, 7, 0, 1, 1, 0, 0, -1, false, false, 0, 2, 2, 2);
        topPanel.add(randomBtn, c);

        LayoutUtils.gbc(c, 8, 0, 1, 1, 0, 0, -1, false, true, 0, 2, 0, 2);
        topPanel.add(new JSeparator(JSeparator.VERTICAL), c);

        LayoutUtils.gbc(c, 9, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, false, false, 2, 2, 0, 2);
        topPanel.add(showLabelCb, c);
        LayoutUtils.gbc(c, 10, 0, 1, 1, 1, 0, GridBagConstraints.WEST, false, false, 1, 2, 0, 2);
        topPanel.add(showCostCb, c);
        LayoutUtils.gbc(c, 11, 0, 1, 1, 0, 0, -1, false, false, 1, 2, 0, 2);
        topPanel.add(modeLbl, c);
        LayoutUtils.gbc(c, 12, 0, 1, 1, 0, 0, -1, false, false, 1, 2, 2, 2);
        topPanel.add(editModeBox, c);

        LayoutUtils.gbc(c, 0, 1, 15, 1, 1, 0, -1, true, false, 0, 0, 0, 0);
        topPanel.add(new JSeparator(JSeparator.HORIZONTAL), c);

        // Bottom
        JPanel logPanel = new JPanel(new GridBagLayout());
        LayoutUtils.gbc(c, 0, 0, 2, 1, 1, 0, GridBagConstraints.CENTER, true, false, 0, 0, 0, 0);
        logPanel.add(new JSeparator(JSeparator.HORIZONTAL), c);
        LayoutUtils.gbc(c, 0, 1, 1, 1, 1, 0, GridBagConstraints.SOUTHWEST, false, false, 2, 2, 2, 1);
        logPanel.add(logLbl, c);
        LayoutUtils.gbc(c, 1, 1, 1, 1, 0, 0, -1, false, false, 0, 2, 2, 1);
        logPanel.add(clearLogBtn, c);
        LayoutUtils.gbc(c, 0, 2, 2, 1, 1, 1, -1, true, true, 2, 0, 2, 2);
        logPanel.add(logSp, c);

        // Right
        JPanel rightPanel = new JPanel(new GridBagLayout());
        JPanel fromToPanel = new JPanel(new GridLayout(2, 2, LayoutUtils.getPad(1), 0));
        JPanel tablePanel = new JPanel(new GridBagLayout());

        LayoutUtils.gbc(c, 0, 0, 2, 1, 0, 0, -1, false, false, 0, 2, 2, 1);
        rightPanel.add(algoLbl, c);
        LayoutUtils.gbc(c, 0, 1, 1, 1, 1, 0, GridBagConstraints.WEST, true, false, 0, 0, 1, 2);
        rightPanel.add(algoBox, c);
        LayoutUtils.gbc(c, 1, 1, 1, 1, 0, 0, -1, false, false, 0, 0, 2, 2);
        rightPanel.add(execBtn, c);

        fromToPanel.add(fromLbl);
        fromToPanel.add(toLbl);
        fromToPanel.add(fromBtn);
        fromToPanel.add(toBtn);

        LayoutUtils.gbc(c, 0, 2, 2, 1, 1, 0, -1, true, false, 0, 0, 2, 2);
        rightPanel.add(fromToPanel, c);

        LayoutUtils.gbc(c, 0, 0, 1, 1, 1, 0, GridBagConstraints.SOUTHWEST, false, false, 0, 0, 2, 1);
        tablePanel.add(tableLbl, c);
        LayoutUtils.gbc(c, 1, 0, 1, 1, 0, 0, -1, false, false, 0, 0, 2, 1);
        tablePanel.add(genBtn, c);
        LayoutUtils.gbc(c, 0, 1, 2, 1, 1, 1, -1, true, true, 0, 0, 2, 2);
        tablePanel.add(tableSp, c);

        LayoutUtils.gbc(c, 0, 3, 2, 1, 1, 1, -1, true, true, 0, 0, 0, 0);
        rightPanel.add(tablePanel, c);

        // Main
        add(gs, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);
        add(logPanel, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.EAST);

        showLabelCb.setSelected(gs.isLabelShown());
        showLabelCb.setSelected(gs.isCostShown());
        editModeBox.setSelectedIndex(gs.getMode());
        logArea.setEditable(false);
        logArea.setWrapStyleWord(true);
        logArea.setLineWrap(true);
        logArea.setFont(LayoutUtils.FONT_MONO);
        table.getColumnModel().getColumn(0).setPreferredWidth(4);
        table.getColumnModel().getColumn(1).setPreferredWidth(4);
        table.setPreferredScrollableViewportSize(new Dimension(240, 50));
        table.setRowHeight(20);
        table.setDefaultRenderer(Double.class, renderer);
        table.setDefaultRenderer(String.class, renderer);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Tooltips
        newBtn.setToolTipText("New Graph");
        importBtn.setToolTipText("Import Graph");
        exportBtn.setToolTipText("Export Graph");
        scaleBtn.setToolTipText("Recalculate the required graph area");

        // Listeners
        fromBtn.addActionListener(this);
        toBtn.addActionListener(this);
        showCostCb.addActionListener(this);
        showLabelCb.addActionListener(this);
        editModeBox.addItemListener(this);
        gs.setGraphSketchListener(this);
        algoBox.addItemListener(this);
        clearLogBtn.addActionListener(this);
        newBtn.addActionListener(this);
        importBtn.addActionListener(this);
        exportBtn.addActionListener(this);
        scaleBtn.addActionListener(this);
        genBtn.addActionListener(this);
        execBtn.addActionListener(this);
        randomBtn.addActionListener(this);
        addImgBtn.addActionListener(this);
        rmvImgBtn.addActionListener(this);

        // Fixes
        table.setEnabled(algoBox.getSelectedIndex() > 0);
        genBtn.setEnabled(table.isEnabled());

        // Frame
        setTitle("Graph GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setMinimumSize(new Dimension((int) Math.max(getSize().getWidth(), 820), 650));
    }

    public int getFrom() {
        return gs.getVertices().indexOf(gs.getFrom());
    }

    public int getTo() {
        return gs.getVertices().indexOf(gs.getTo());
    }

    public Graph getGraph() {
        return gs.getGraph();
    }

    public void log(Object message) {
        String old = logArea.getText();
        logArea.setText(!old.isEmpty() ? old + "\n" + message : "" + message);
    }

    public double[] getHeuristics() {
        int count = table.getModel().getRowCount();
        double h[] = new double[count];
        for (int i = 0; i < count; i++) {
            h[i] = (double) table.getModel().getValueAt(i, 1);
        }
        return h;
    }

    public void generateHeuristics() {
        if (gs.getTo() == null) {
            Utils.error(this, "You must specify the destination to generate heuristics.");
            return;
        }
        if (Utils.confirm(this, "Every edge cost will be updated to ensure\n"
            + "the admissibility of the generated heuristics.\n"
            + "This is done by picking a random edge to\n"
            + "calculate the cost per pixel. Continue?")) {

            Random ran = new Random();
            GraphSketch.Edge ranEdge = gs.getEdges().get(ran.nextInt(gs.getEdges().size()));
            double disX = ranEdge.from.x - ranEdge.to.x;
            double disY = ranEdge.from.y - ranEdge.to.y;
            double distance = Math.sqrt(disX * disX + disY * disY);
            double cpp = ranEdge.cost / distance; // Cost per pixel

            for (GraphSketch.Edge e : gs.getEdges()) {
                disX = e.from.x - e.to.x;
                disY = e.from.y - e.to.y;
                distance = Math.sqrt(disX * disX + disY * disY);
                e.cost = cpp * distance;
                e.cost = (double) Math.round(e.cost * 100) / 100d;
            }

            int i = 0;
            GraphSketch.Vertex target = gs.getTo();
            for (GraphSketch.Vertex v : gs.getVertices()) {
                disX = v.x - target.x;
                disY = v.y - target.y;
                distance = Math.sqrt(disX * disX + disY * disY);
                double h = distance * cpp;
                table.setValueAt((double) Math.round(h * 100) / 100d, i++, 1);
                ((HeuristicTableModel) table.getModel()).fireTableDataChanged();
            }

            gs.repaint();
        }
    }

    public void executeAlgorithm() {
        if (gs.getFrom() == null || gs.getTo() == null) {
            Utils.error(this, "You must specify starting and ending points.");
            return;
        }
        analysis.header = algoBox.getSelectedItem().toString();
        analysis.edges = gs.getEdges().size();
        analysis.vertices = gs.getVertices().size();
        int[] path = null;
        switch (algoBox.getSelectedIndex()) {
            case 0:
                path = GraphUtils.performUCS(gs.getGraph(), getFrom(), getTo(), analysis);
                break;
            case 1:
                path = GraphUtils.performAStar(gs.getGraph(), getFrom(), getTo(), getHeuristics(), analysis);
                break;
            case 2:
                path = GraphUtils.performGBFS(gs.getGraph(), getFrom(), getTo(), getHeuristics(), analysis);
                break;
        }
        if (path != null) {
            String[] pathLabels = new String[path.length];
            for (int i = 0; i < path.length; i++) {
                pathLabels[i] = gs.getVertices().get(path[i]).label;
            }
            analysis.path = pathLabels;
            gs.setPath(path);
        }
        log(analysis);
    }
    
    public void generateRandomGraph(int maxv) {
        if (maxv < 4) {
            Utils.error(this, "Number of vertices must be greater than 5");
            return;
        }
        reset();
        Random ran = new Random();
        int width = gs.getGraphArea().getWidth();
        int height = gs.getGraphArea().getHeight();
        for (int i = 0; i < maxv; i++) {
            int x = ran.nextInt(width);
            int y = ran.nextInt(height);
            gs.addVertex("V" + i, x, y);
        }
        for (int i = 0; i < maxv * 1.5; i++) {
            int frm = i;
            int to;
            double cost;
            do {
                if (i >= maxv) {
                    frm = ran.nextInt(maxv);
                }
            to = ran.nextInt(maxv);
            GraphSketch.Vertex vf = gs.getVertices().get(frm);
            GraphSketch.Vertex vt = gs.getVertices().get(to);
            int disx = vf.x - vt.x;
            int disy = vf.y - vt.y;
            cost = Math.sqrt(disx * disx + disy * disy);
            } while (!gs.addEdge(frm, to, (double) Math.round(cost * 100) / 100d));
        }
        gs.scaleGraph(50);
    }

    public void exportGraph() {
        fileChooser.setFileFilter(graphFilter);
        fileChooser.setDialogTitle("Export the graph");
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile();
            if (f.exists() && !Utils.confirm(this, "Overwrite the file \"" + f.getName() + "\"?")) {
                return;
            }

            String data = "";
            boolean first = true;
            for (GraphSketch.Vertex v : gs.getVertices()) {
                if (!first) {
                    data += ";";
                }
                data += v.label + "," + v.x + "," + v.y;
                first = false;
            }
            data += "|";
            first = true;
            for (GraphSketch.Edge e : gs.getEdges()) {
                if (!first) {
                    data += ";";
                }
                data += (gs.getVertices().indexOf(e.from) + 1) + ","
                    + (gs.getVertices().indexOf(e.to) + 1) + "," + e.cost;
                first = false;
            }
            data += "|" + gs.getGraphArea().getWidth() + "," + gs.getGraphArea().getHeight();
            String name = f.getName();
            if (name.indexOf(".graph") != name.length() - 6) {
                name += ".graph";
            }
            f = new File(f.getParent() + "//" + name);
            try {
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(f.getAbsolutePath()), StandardCharsets.UTF_8);
                writer.write(data);
                writer.close();
            } catch (IOException ex) {
                System.err.println("IOException: " + ex.getMessage());
            }
        }
    }

    public void importGraph() {
        fileChooser.setFileFilter(graphFilter);
        fileChooser.setDialogTitle("Import a graph");
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                reset();
                String data = "";
                Scanner scanner = new Scanner(fileChooser.getSelectedFile());
                boolean first = true;
                while (scanner.hasNext()) {
                    if (!first) {
                        data += "\n";
                    }
                    data += scanner.nextLine();
                    first = false;
                }
                if (!gs.parseGraph(data)) {
                    Utils.error(this, "File has incorrect format!");
                }
            } catch (FileNotFoundException ex) {
                System.err.println("FileNotFoundException: " + ex.getMessage());
            }
        }
    }

    public void newGraph() {
        if (gs.getVertices().size() > 0
            && Utils.confirm(this, "Are you sure you want to erase the drawn graph?")) {
            reset();
        }
    }
    
    public void reset() {
        gs.clear();
        ((HeuristicTableModel) table.getModel()).clear();
        ((HeuristicTableModel) table.getModel()).fireTableDataChanged();
        fromBtn.setText("??");
        toBtn.setText("??");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == showCostCb) {
            gs.setShowCost(showCostCb.isSelected());
        } else if (e.getSource() == showLabelCb) {
            gs.setShowLabel(showLabelCb.isSelected());
        } else if (e.getSource() == fromBtn) {
            if (gs.getMode() != GraphSketch.MODE_SELECT_TO_VERTEX) {
                fromBtn.setText("...");
                gs.setMode(GraphSketch.MODE_SELECT_FROM_VERTEX);
                fromBtn.setEnabled(false);
                toBtn.setEnabled(false);
            }
        } else if (e.getSource() == toBtn) {
            if (gs.getMode() != GraphSketch.MODE_SELECT_FROM_VERTEX) {
                toBtn.setText("...");
                gs.setMode(GraphSketch.MODE_SELECT_TO_VERTEX);
                fromBtn.setEnabled(false);
                toBtn.setEnabled(false);
            }
        } else if (e.getSource() == newBtn) {
            newGraph();
        } else if (e.getSource() == importBtn) {
            importGraph();
        } else if (e.getSource() == exportBtn) {
            exportGraph();
        } else if (e.getSource() == scaleBtn) {
            gs.scaleGraph();
        } else if (e.getSource() == clearLogBtn) {
            logArea.setText("");
        } else if (e.getSource() == genBtn) {
            generateHeuristics();
        } else if (e.getSource() == execBtn) {
            executeAlgorithm();
        } else if (e.getSource() == randomBtn) {
            try {
                int maxv = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter the number of vertices"));
                generateRandomGraph(maxv);
            } catch (NumberFormatException ex) {
                Utils.error(this, "Please enter an integer");
            }
        } else if (e.getSource() == addImgBtn) {
            fileChooser.setFileFilter(imageFilter);
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File f = fileChooser.getSelectedFile();
                if (f.exists()) {
                    try {
                        BufferedImage img = ImageIO.read(f);
                        gs.setImage(img);
                    } catch (IOException ex) {
                        Logger.getLogger(GraphFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } else if (e.getSource() == rmvImgBtn) {
            fileChooser.setFileFilter(imageFilter);
            gs.setImage(null);
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == editModeBox) {
            gs.setMode(editModeBox.getSelectedIndex());
        } else if (e.getSource() == algoBox) {
            table.setEnabled(algoBox.getSelectedIndex() > 0);
            genBtn.setEnabled(table.isEnabled());
        }
    }

    @Override
    public void onAddVertex(GraphSketch.Vertex v) {
        ((HeuristicTableModel) table.getModel()).fireTableDataChanged();
    }

    @Override
    public void onRemoveVertex(GraphSketch.Vertex v) {
        ((HeuristicTableModel) table.getModel()).fireTableDataChanged();
    }

    @Override
    public void onEditVertex(GraphSketch.Vertex v) {
        ((HeuristicTableModel) table.getModel()).fireTableDataChanged();
    }

    @Override
    public void onFromSelected(GraphSketch.Vertex v) {
        fromBtn.setText(v == null ? "??" : v.label);
        gs.setMode(editModeBox.getSelectedIndex());
        fromBtn.setEnabled(true);
        toBtn.setEnabled(true);
    }

    @Override
    public void onToSelected(GraphSketch.Vertex v) {
        toBtn.setText(v == null ? "??" : v.label);
        gs.setMode(editModeBox.getSelectedIndex());
        fromBtn.setEnabled(true);
        toBtn.setEnabled(true);
    }

    private static class HeuristicTableModel extends AbstractTableModel {

        private static final String COLS[] = new String[]{"Vertex Label", "Heuristic Value"};
        private static final Class CLSS[] = new Class[]{String.class, Double.class};

        private GraphSketch gs;
        private List<Double> values;

        public HeuristicTableModel(GraphSketch gs) {
            values = new ArrayList<>();
            this.gs = gs;
        }

        @Override
        public int getRowCount() {
            return gs.getVertices().size();
        }

        @Override
        public int getColumnCount() {
            return COLS.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return gs.getVertices().get(rowIndex).label;
            } else if (columnIndex == 1) {
                if (rowIndex >= values.size()) {
                    values.add(rowIndex, 0d);
                }
                return values.get(rowIndex);
            }
            return null;
        }

        @Override
        public String getColumnName(int column) {
            return COLS[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return CLSS[columnIndex];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 1;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            values.add(rowIndex, (Double) aValue);
        }
        
        public void clear() {
            values.clear();
        }
    }
}
