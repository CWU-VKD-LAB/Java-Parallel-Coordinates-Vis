package javaPC;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Ui ribbon class
 */
public class UiRibbon extends JPanel {

    protected static final int WIDTH = 1000;
    protected static final int HEIGHT = 60;

    private File loadedCSV;

    protected UiRibbon(TopWindow parent) {
        // Set the preferred size of the panel
        setPreferredSize(new Dimension(WIDTH, HEIGHT));

        // Set the layout manager to GridBagLayout
        setLayout(new GridBagLayout());
        setBorder(new BevelBorder(BevelBorder.RAISED, Color.BLACK, Color.BLACK));
        setBackground(Color.LIGHT_GRAY);

        // Create the buttons
        JButton buttonLoadCSV = new JButton("Load CSV");
        JButton buttonToggleAxisNames = new JButton("Toggle Labels");
        JButton buttonChangeBackground = new JButton("Change Background");
        JButton buttonChangeAxisColor = new JButton("Change Axis Color");
        JSlider transparencySlider = new JSlider(0, 100, 100);

        // Configure the slider for transparency
        transparencySlider.setPreferredSize(new Dimension(150, 50));
        transparencySlider.setMajorTickSpacing(25);
        transparencySlider.setMinorTickSpacing(5);
        transparencySlider.setPaintTicks(true);
        transparencySlider.setPaintLabels(true);
        JLabel sliderLabel = new JLabel("Transparency");
        sliderLabel.setLabelFor(transparencySlider);

        // Add action listener for changing axis color
        buttonChangeAxisColor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ThemedColorPicker colorPicker = new ThemedColorPicker(parent);
                Color newColor = colorPicker.pickColor();
                if (newColor != null) {
                    parent.getPlotPanel().setAxisColor(newColor);  // Assumes setAxisColor method in PlotPanel
                }
            }
        });

        // Listener for the transparency slider
        transparencySlider.addChangeListener(e -> {
            if (!transparencySlider.getValueIsAdjusting()) {
                float alpha = transparencySlider.getValue() / 100f;
                parent.getPlotPanel().setAlpha(alpha); // Assuming there's a method to set background alpha
            }
        });

        // Set the preferred width of the buttons
        buttonLoadCSV.setPreferredSize(new Dimension(120, buttonLoadCSV.getPreferredSize().height));
        buttonToggleAxisNames.setPreferredSize(new Dimension(120, buttonToggleAxisNames.getPreferredSize().height));
        buttonChangeBackground.setPreferredSize(new Dimension(160, buttonChangeBackground.getPreferredSize().height));

        buttonChangeBackground.setPreferredSize(new Dimension(160, buttonChangeBackground.getPreferredSize().height));
        buttonChangeBackground.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                // Open a color chooser dialog and get the selected color
                ThemedColorPicker colorPicker = new ThemedColorPicker(parent);
                Color newColor = colorPicker.pickColor();
                if (newColor != null) {
                    parent.getPlotPanel().setBackgroundColor(newColor); // Assuming parent.getPlotPanel() method exists
                }
            }
        });

        // Initially disable buttons that should only be active after a CSV is loaded
        buttonToggleAxisNames.setEnabled(false);
        buttonChangeBackground.setEnabled(false);
        buttonChangeAxisColor.setEnabled(false);

        // Add action listener to the "Load CSV" button
        buttonLoadCSV.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                loadedCSV = CsvParser.loadCSVFile();
                if (loadedCSV == null) {
                    JOptionPane.showMessageDialog(null, "The file selected is not a CSV, please try again.");
                    buttonToggleAxisNames.setEnabled(false);
                    buttonChangeBackground.setEnabled(false);
                } else {
                    String[][] data = CsvParser.parseCSVFile(loadedCSV);
                    parent.render(loadedCSV.getName(), data);
                    buttonToggleAxisNames.setEnabled(true);
                    buttonChangeBackground.setEnabled(true);
                    buttonChangeAxisColor.setEnabled(true);
                }
            }
        });

        // Set the preferred width of the buttons
        buttonLoadCSV.setPreferredSize(new Dimension(120, buttonLoadCSV.getPreferredSize().height));
        buttonToggleAxisNames.setPreferredSize(new Dimension(120, buttonToggleAxisNames.getPreferredSize().height));

        // Add action listener to the "Toggle Axis Names" button
        buttonToggleAxisNames.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                // Assuming parent.getPlotPanel() method exists and it toggles axis name visibility
                parent.getPlotPanel().toggleAxisNames();
            }
        });

        // Add the buttons to the panel with center alignment and left anchor
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 10, 0, 0);
        
        add(buttonLoadCSV, constraints);
        constraints.gridx++;
        add(buttonToggleAxisNames, constraints);
        constraints.gridx++;
        add(buttonChangeBackground, constraints);
        constraints.gridx++;
        add(buttonChangeAxisColor, constraints);
        constraints.gridx++;
        add(sliderLabel, constraints);
        constraints.gridx++;
        add(transparencySlider, constraints);

        setVisible(true);
    }
}
