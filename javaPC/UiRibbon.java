package javaPC;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Ui ribbon class
 */
public class UiRibbon extends JPanel {

    protected static final int WIDTH = 600;
    protected static final int HEIGHT = 30;

    private File loadedCSV;

    protected UiRibbon(TopWindow parent) {
        // Set the preferred size of the panel
        setPreferredSize(new Dimension(WIDTH, HEIGHT));

        // Set the layout manager to GridBagLayout
        setLayout(new GridBagLayout());

        // Set the background color to a metallic color
        setBackground(Color.LIGHT_GRAY);

        // Create the buttons
        JButton buttonLoadCSV = new JButton("Load CSV");
        JButton buttonRenderPlot = new JButton("Render Plot");
        JButton buttonToggleAxisNames = new JButton("Toggle Labels");

        // Set the preferred width of the buttons
        buttonLoadCSV.setPreferredSize(new Dimension(120, buttonLoadCSV.getPreferredSize().height));
        buttonRenderPlot.setPreferredSize(new Dimension(120, buttonRenderPlot.getPreferredSize().height));
        buttonToggleAxisNames.setPreferredSize(new Dimension(120, buttonToggleAxisNames.getPreferredSize().height));

        // Add action listener to the "Load CSV" button
        buttonLoadCSV.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                loadedCSV = CsvParser.loadCSVFile();
                if (loadedCSV == null) {
                    // Show a basic information message dialog
                    JOptionPane.showMessageDialog(null, "The file selected is not a CSV, please try again.");
                    buttonRenderPlot.setEnabled(false);
                } else {
                    buttonRenderPlot.setEnabled(true);
                }
            }
        });

        // Add action listener to the "Render Plot" button
        buttonRenderPlot.setEnabled(false);
        buttonRenderPlot.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                String[][] data = CsvParser.parseCSVFile(loadedCSV);
                parent.render(loadedCSV.getName(), data);
            }
        });

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

        JButton buttonChangeBackground = new JButton("Change Background");
        buttonChangeBackground.setPreferredSize(new Dimension(160, buttonChangeBackground.getPreferredSize().height));
        buttonChangeBackground.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                // Open a color chooser dialog and get the selected color
                Color newColor = JColorChooser.showDialog(null, "Choose Background Color", parent.getPlotPanel().getBackground());
                if (newColor != null) {
                    parent.getPlotPanel().setBackgroundColor(newColor); // Assuming parent.getPlotPanel() method exists
                }
            }
        });

        add(buttonLoadCSV, constraints);

        constraints.gridx = 1;
        add(buttonRenderPlot, constraints);

        constraints.gridx = 2;
        add(buttonToggleAxisNames, constraints);

        // Adding the button to the UiRibbon
        constraints.gridx = 3;
        add(buttonChangeBackground, constraints);

        setVisible(true);
    }
}
