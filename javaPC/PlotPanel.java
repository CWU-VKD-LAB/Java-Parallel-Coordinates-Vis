package javaPC;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Plot renderer class
 */

public class PlotPanel extends JPanel {

    private static final int WIDTH = 1600;
    private static final int HEIGHT = 750;

    private HashMap<String, Color> colorMap;

    private String[][] data;
    private boolean showAxisNames = true;
    private static Color backgroundColor = Color.GRAY;
    private static Color axisColor = Color.BLACK;
    private static Boolean scaleVertices = false;

    HashMap<Point, Integer> pointOverlays = new HashMap<>();

    public void toggleScaleVertices() {
        scaleVertices = !scaleVertices;
        removeAll();
        repaint();
    }

    public void setAlpha(float alpha) {
        // set all class color alphas
        for (String className : colorMap.keySet()) {
            Color color = colorMap.get(className);
            colorMap.put(className, new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.round(alpha * 255)));
        }
        removeAll();
        repaint();
    }

    public HashMap<String, Color> generateUniqueColors(List<String> classNames, Color axisColor, Color backgroundColor) {
        colorMap = new HashMap<>();
        float[] axisHsv = Color.RGBtoHSB(axisColor.getRed(), axisColor.getGreen(), axisColor.getBlue(), null);
        float[] backgroundHsv = Color.RGBtoHSB(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), null);

        // Calculate the hue step to distribute colors evenly across the hue circle
        float hueStep = 1.0f / classNames.size();

        // Generate unique colors for each class
        for (int i = 0; i < classNames.size(); i++) {
            float hue = (hueStep * i) % 1.0f;
            // Avoid hues too close to the background or axis colors by checking the hue distance
            if (Math.abs(hue - axisHsv[0]) < 0.05 || Math.abs(hue - backgroundHsv[0]) < 0.05) {
                hue = (hue + hueStep / 2) % 1.0f;  // Adjust hue to avoid similarity
            }
            // Create colors with the same saturation and brightness levels
            Color classColor = new Color(Color.HSBtoRGB(hue, 0.7f, 0.9f));  // Using a fixed saturation and brightness
            colorMap.put(classNames.get(i), classColor);
        }
        return colorMap;
    }

    public void setAxisColor(Color color) {
        axisColor = color;
        removeAll();
        repaint();
    }

    public void setClassColor(String className, Color color) {
        if (colorMap.containsKey(className)) {
            colorMap.put(className, color);
        } else {
            JOptionPane.showMessageDialog(this, "Class name not found: " + className);
        }
        removeAll();
        repaint();
    }    

    public void setBackgroundColor(Color color) {
        setBackground(color);
        backgroundColor = color;
        removeAll();
        repaint();
    }

    public void setShowAxisNames(boolean showAxisNames) {
        this.showAxisNames = showAxisNames;
        removeAll();
        repaint();
    }

    protected PlotPanel(String[][] data) {
        this.data = data;

        // Set the preferred size of the panel
        setPreferredSize(new Dimension(WIDTH, HEIGHT));

        setBorder(new BevelBorder(BevelBorder.LOWERED, Color.BLACK, Color.BLACK));
        
        ArrayList<String> classNames = new ArrayList<>();
        for (int j = 1; j < data.length; j++) {
            for (int i = 0; i < data[0].length; i++) {
                if (i == data[0].length - 1) {
                    String name = data[j][i];
                    if (!classNames.contains(name)) {
                        classNames.add(name);
                    }
                    continue;
                }
            }
        }       

        colorMap = generateUniqueColors(classNames, axisColor, backgroundColor);
        
        setBackground(backgroundColor);

        setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int panelWidth = getWidth();
        int panelHeight = getHeight() + 40;
        int margin = 70;

        int axisCount = data[0].length;
        int lineSpacing = panelWidth / axisCount;

        g.setColor(Color.BLACK);

        float[] maxes = new float[axisCount - 1]; // init to all 0s
        float[] mins = new float[axisCount - 1]; // init to all max values
        Arrays.fill(mins, Float.MAX_VALUE);

        pointOverlays.clear();

        for (int j = 1; j < data.length; j++) {
            for (int i = 0; i < axisCount - 1; i++) {
                float dataPnt = 0;
                try {
                    dataPnt = Float.parseFloat(data[j][i]);
                } catch (NumberFormatException e) {
                    SwingUtilities.invokeLater(() -> {
                        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                        JOptionPane.showMessageDialog(parentFrame, "Dataset contains non-numeric values or class column is not last column.");
                        parentFrame.setTitle(TopWindow.TITLE);
                    });
                    return;
                }
                if (dataPnt > maxes[i]) {
                    maxes[i] = dataPnt;
                }
                if (dataPnt < mins[i]) {
                    mins[i] = dataPnt;
                }
            }
        }

        // draw axis lines
        g.setColor(axisColor);
        for (int i = 1; i < axisCount; i++) {
            int x = lineSpacing * i;
            g.drawLine(x-1, 35, x-1, panelHeight - margin);
            g.drawLine(x, 35, x, panelHeight - margin);
            g.drawLine(x+1, 35, x+1, panelHeight - margin);
        }
        
        HashMap<String, Integer> classNums = new HashMap<>();
        for (int j = 1; j < data.length; j++) {
            int lastAxisIndex = axisCount - 1; // Index of the last axis

            // Iterate over each axis including the last one
            for (int i = 1; i <= lastAxisIndex; i++) {
                int x = lineSpacing * i;
                g.setColor(colorMap.get(data[j][lastAxisIndex])); // Use color from last axis
                Float dataPnt;
                int pos;
                if (i < lastAxisIndex) {
                    // For non-last axes, calculate position and draw vertex
                    dataPnt = Float.parseFloat(data[j][i - 1]);
                    pos = Math.round((panelHeight + 35 - margin - margin) * ((dataPnt - mins[i - 1]) / (maxes[i - 1] - mins[i - 1])) + margin);
                    Point point = new Point(x, panelHeight - pos);
                    pointOverlays.put(point, pointOverlays.getOrDefault(point, 0) + 1);
                    int size = 7;
                    if (scaleVertices) {
                        int sizeRange = 20 - 7; // Range of sizes
                        int maxOverlays = 8; // Maximum overlays before reaching the maximum size
                        int overlays = pointOverlays.get(point) - 1; // Number of overlays

                        // Calculate the scaled size
                        size = Math.min(20, 7 + (int) ((double) overlays / maxOverlays * sizeRange));
                    }
                    g.fillOval(x - size / 2, panelHeight - pos - size / 2, size, size);
                } else {
                    // For the last axis, draw vertex using the last data point
                    dataPnt = Float.parseFloat(data[j][lastAxisIndex - 1]);
                    pos = Math.round((panelHeight + 35 - margin - margin) * ((dataPnt - mins[lastAxisIndex - 1]) / (maxes[lastAxisIndex - 1] - mins[lastAxisIndex - 1])) + margin);
                    Point point = new Point(x, panelHeight - pos);
                    pointOverlays.put(point, pointOverlays.getOrDefault(point, 0) + 1);
                    int size = 7;
                    if (scaleVertices) {
                        int sizeRange = 20 - 7; // Range of sizes
                        int maxOverlays = 8; // Maximum overlays before reaching the maximum size
                        int overlays = pointOverlays.get(point) - 1; // Number of overlays

                        // Calculate the scaled size
                        size = Math.min(20, 7 + (int) ((double) overlays / maxOverlays * sizeRange));
                    }
                    g.fillOval(x - size / 2, panelHeight - pos - size / 2, size, size);
                }
                
                if (i < lastAxisIndex) {
                    // For non-last axes, draw edge to the next data point
                    Float nextDataPnt = Float.parseFloat(data[j][i]);
                    int nextPos = Math.round((panelHeight + 35 - margin - margin) * ((nextDataPnt - mins[i]) / (maxes[i] - mins[i])) + margin);
                    g.drawLine(x, panelHeight - pos, lineSpacing * (i + 1), panelHeight - nextPos);
                }
            }
        }
        
        if (showAxisNames) {
            // draw axis labels
            for (int i = 1; i < axisCount; i++) {
                String name = data[0][i-1];
                JLabel label = new JLabel(name);
                // Set the position of the label
                int xPos = lineSpacing * i;
                xPos -= (int)Math.floor(3*name.length());
                int yPos = 5;
                Point position = new Point(xPos, yPos);
                label.setLocation(position);

                // Set the size of the label
                int width = 150;
                int height = 15;
                Dimension size = new Dimension(width, height);
                label.setSize(size);
                label.setForeground(axisColor);
                add(label);

                if (i < axisCount) {
                    // label float formatting
                    DecimalFormat formatter = new DecimalFormat("0.##");
                    String maxName = formatter.format(maxes[i-1]);
                    
                    // Max axis value label
                    JLabel maxLabel = new JLabel(maxName);

                    // label position
                    int maxXPos = lineSpacing * i;
                    maxXPos -= (int)Math.floor(3*maxName.length());
                    int maxYPos = 18;
                    Point maxPosition = new Point(maxXPos, maxYPos);
                    maxLabel.setLocation(maxPosition);

                    // label size
                    int maxWidth = 50;
                    int maxHeight = 15;
                    Dimension maxSize = new Dimension(maxWidth, maxHeight);
                    maxLabel.setSize(maxSize);
                    maxLabel.setForeground(axisColor);
                    add(maxLabel);

                    // min axis value label
                    String minName = formatter.format(mins[i-1]);
                    JLabel minLabel = new JLabel(minName);

                    // label position
                    int minXPos = lineSpacing * i;
                    minXPos -= (int)Math.floor(3*minName.length());
                    int minYPos = panelHeight - 65;
                    Point minPosition = new Point(minXPos, minYPos);
                    minLabel.setLocation(minPosition);

                    // label size
                    int minWidth = 50;
                    int minHeight = 15;
                    Dimension minSize = new Dimension(minWidth, minHeight);
                    minLabel.setSize(minSize);
                    minLabel.setForeground(axisColor);
                    add(minLabel);
                }
            }
        }
        
        // Draw a legend for the class colors
        int legendX = 10;
        int legendY = 50;
        int legendWidth = 20;
        // get longest class name
        int longest = 0;
        classNums = new HashMap<>();
        for (int j = 1; j < data.length; j++) {
            String className = data[j][axisCount - 1];
            classNums.put(className, classNums.getOrDefault(className, 0) + 1);
        }
        for (String className : classNums.keySet()) {
            if (className.length() > longest) {
                longest = className.length();
            }
        }
        // calculate legendwidth
        legendWidth += longest * 5 + 8;

        int legendHeight = 20 * classNums.size();
        g.setColor(Color.WHITE);
        g.fillRect(legendX, legendY, legendWidth, legendHeight);
        g.setColor(Color.BLACK);
        g.drawRect(legendX, legendY, legendWidth, legendHeight);
        int legendMargin = 5;
        int legendSpacing = 20;
        int legendTextX = legendX + legendMargin;
        int legendTextY = legendY + legendMargin + 10;
        for (String className : classNums.keySet()) {
            g.setColor(colorMap.get(className));
            g.fillRect(legendX + legendMargin, legendTextY - 10, 10, 10);
            g.setColor(Color.BLACK);
            g.setFont(g.getFont().deriveFont(11.0f));
            g.drawString(className, legendTextX + 15, legendTextY);
            legendTextY += legendSpacing;
        }
    }

    public boolean isShowingAxisNames() {
        return showAxisNames;
    }

    public void toggleAxisNames() {
        showAxisNames = !showAxisNames;
        // clear screen
        removeAll();
        repaint();
    }
}
