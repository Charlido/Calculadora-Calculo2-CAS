package main;

import com.formdev.flatlaf.FlatLightLaf;
import controller.CalculatorController;
import model.MathEngine;
import view.CalculatorView;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        // Instalación del Look and Feel Moderno Minimalista
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            // Opcional: Para esquinas redondeadas o configuraciones extra
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
        } catch (Exception ex) {
            System.err.println("Fallo al inicializar FlatLaf");
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