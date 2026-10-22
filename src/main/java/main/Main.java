package main;

import com.formdev.flatlaf.FlatDarkLaf;
import controller.CalculatorController;
import model.MathEngine;
import view.CalculatorView;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;

public class Main {
    public static void main(String[] args) {
        // Instalación del Look and Feel Oscuro Moderno (Cyberpunk / Dark UI)
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 12);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("ComboBox.background", Color.decode("#2A274E"));
            UIManager.put("ComboBox.foreground", Color.WHITE);
            UIManager.put("ComboBox.popupBackground", Color.decode("#1E1B3A"));
            UIManager.put("ComboBox.selectionBackground", Color.decode("#00E676"));
            UIManager.put("ComboBox.selectionForeground", Color.decode("#0F0D1C"));
            UIManager.put("OptionPane.background", Color.decode("#1E1B3A"));
            UIManager.put("Panel.background", Color.decode("#1E1B3A"));
            UIManager.put("OptionPane.messageForeground", Color.WHITE);
        } catch (Exception ex) {
            System.err.println("Fallo al inicializar FlatDarkLaf");
        }

        // Ejecución en el Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MathEngine model = new MathEngine();
            CalculatorView view = new CalculatorView();
            new CalculatorController(view, model);

            view.setVisible(true);
        });
    }
}
