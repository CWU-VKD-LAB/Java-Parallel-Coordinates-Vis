package javaPC;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;

/**
 * Ui ribbon class
 */
public class UiRibbon extends JPanel {

    protected static final int WIDTH = 1200;
    protected static final int HEIGHT = 60;

    private File loadedCSV;
    private JComboBox<String> classSelector;  // Dropdown for class names

    protected UiRibbon(TopWindow parent) {
        // Initialize UI components and layout
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setLayout(new GridBagLayout());
        setBorder(new BevelBorder(BevelBorder.RAISED, Color.BLACK, Color.BLACK));
        setBackground(Color.LIGHT_GRAY);

        // Create and configure UI components
        JButton buttonLoadCSV = new JButton("Load CSV");
        JButton buttonToggleAxisNames = new JButton("Toggle Labels");
        JButton buttonChangeBackground = new JButton("Background Color");
        JButton buttonChangeAxisColor = new JButton("Axis Color");
        JButton buttonScaleVertices = new JButton("Histogram");
        JSlider transparencySlider = createTransparencySlider(parent);
        JLabel sliderLabel = new JLabel("Transparency");
        sliderLabel.setLabelFor(transparencySlider);
        classSelector = new JComboBox<>();  // Initialize dropdown
        JButton colorButton = new JButton("Change Class Color");  // Button for changing class color

        // Initially disable buttons that should only be active after a CSV is loaded
        buttonToggleAxisNames.setEnabled(false);
        buttonChangeBackground.setEnabled(false);
        buttonChangeAxisColor.setEnabled(false);
        classSelector.setEnabled(false);
        colorButton.setEnabled(false);
        buttonScaleVertices.setEnabled(false);

        // Add components to the panel
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(0, 10, 0, 0);
        addComponent(constraints, buttonLoadCSV);
        addComponent(constraints, buttonToggleAxisNames);
        addComponent(constraints, buttonChangeBackground);
        addComponent(constraints, buttonChangeAxisColor);
        addComponent(constraints, buttonScaleVertices);
        addComponent(constraints, sliderLabel);
        addComponent(constraints, transparencySlider);

        // Handle CSV loading
        buttonLoadCSV.addActionListener(event -> {
            loadedCSV = CsvParser.loadCSVFile();
            if (loadedCSV == null) {
                JOptionPane.showMessageDialog(null, "The file selected is not a CSV, please try again.");
                disableComponents(buttonToggleAxisNames, buttonChangeBackground, buttonChangeAxisColor, classSelector, colorButton, buttonScaleVertices);
            } else {
                String[][] data = CsvParser.parseCSVFile(loadedCSV);
                parent.render(loadedCSV.getName(), data);
                updateClassSelector(data);  // Populate dropdown with class names
                enableComponents(buttonToggleAxisNames, buttonChangeBackground, buttonChangeAxisColor, classSelector, colorButton, buttonScaleVertices);
            }
        });

        buttonScaleVertices.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                parent.getPlotPanel().toggleScaleVertices();
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

        // Handle class color change
        colorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(null, "Choose a color for " + classSelector.getSelectedItem(), Color.white);
            if (newColor != null) {
                ((TopWindow) parent).getPlotPanel().setClassColor(classSelector.getSelectedItem().toString(), newColor);
            }
        });

        // Add action listener to the "Change Background" button
        buttonChangeBackground.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                ThemedColorPicker colorPicker = new ThemedColorPicker(parent);
                Color newColor = colorPicker.pickColor();
                if (newColor != null) {
                    parent.getPlotPanel().setBackgroundColor(newColor);
                }
            }
        });

        // Add action listener to the "Change Axis Color" button
        buttonChangeAxisColor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                ThemedColorPicker colorPicker = new ThemedColorPicker(parent);
                Color newColor = colorPicker.pickColor();
                if (newColor != null) {
                    parent.getPlotPanel().setAxisColor(newColor);
                }
            }
        });

        // Add class selector and color button
        addComponent(constraints, classSelector);
        addComponent(constraints, colorButton);
    }

    private JSlider createTransparencySlider(TopWindow parent) {  // Accept TopWindow as an argument
        JSlider slider = new JSlider(0, 100, 100);
        slider.setPreferredSize(new Dimension(150, 50));
        slider.setMajorTickSpacing(25);
        slider.setMinorTickSpacing(5);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(e -> {
            if (!slider.getValueIsAdjusting()) {
                float transparency = (float) slider.getValue() / 100;
                parent.getPlotPanel().setAlpha(transparency); // Use the passed parent reference here
            }
        });
        
        return slider;
    }    

    private void addComponent(GridBagConstraints constraints, Component component) {
        add(component, constraints);
        constraints.gridx++;
    }

    private void disableComponents(JComponent... components) {
        for (JComponent component : components) {
            component.setEnabled(false);
        }
    }

    private void enableComponents(JComponent... components) {
        for (JComponent component : components) {
            component.setEnabled(true);
        }
    }

    private void updateClassSelector(String[][] data) {
        classSelector.removeAllItems();
        String[] classNames = extractClassNames(data);
        for (String className : classNames) {
            classSelector.addItem(className);
        }
    }

    private String[] extractClassNames(String[][] data) {
        HashSet<String> classNamesSet = new HashSet<>();
        // Assuming class names are in the last column
        int lastColumnIndex = data[0].length - 1;
        // Start loop from index 1 to skip the first row (header)
        for (int i = 1; i < data.length; i++) {
            classNamesSet.add(data[i][lastColumnIndex]);
        }
        return classNamesSet.toArray(new String[0]);
    }       
}
