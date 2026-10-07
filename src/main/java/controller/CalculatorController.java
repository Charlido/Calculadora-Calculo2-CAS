package controller;

import model.MathEngine;
import view.CalculatorView;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class CalculatorController {
    private final CalculatorView view;
    private final MathEngine model;

    public CalculatorController(CalculatorView view, MathEngine model) {
        this.view = view;
        this.model = model;
        inicializarEventos();
        actualizarEstadoCampos(); // Estado inicial
    }

    private void inicializarEventos() {
        // ¡ESTA LÍNEA ES VITAL! Si no está, el audio nunca se prepara.
        GestorAudio audio = new GestorAudio();

        // Tracker de Foco
        // ... (tu código de FocusAdapter) ...

        // Eventos del Teclado
        for (JButton btn : view.botonesTeclado) {
            if (btn != null) {
                btn.addActionListener(e -> {
                    audio.reproducirSonidoClick(); // ¡Hace sonar la tecla!
                    escribirEnCampo(btn.getText());
                });
            }
        }

        view.btnLimpiar.addActionListener(e -> {
            audio.reproducirSonidoClick();
            limpiarInterfaz();
        });

        view.btnCalcular.addActionListener(e -> {
            audio.reproducirSonidoClick();
            ejecutarCalculo();
        });
    }

    private void actualizarEstadoCampos() {
        int modo = view.comboModo.getSelectedIndex();

        // Reglas de negocio UI
        boolean requiereLimites = (modo >= 2);
        boolean requiereG = (modo == 3);

        view.txtA.setEnabled(requiereLimites);
        view.txtB.setEnabled(requiereLimites);
        view.txtG.setEnabled(requiereG);

        if (!requiereLimites) { view.txtA.setText(""); view.txtB.setText(""); }
        if (!requiereG) view.txtG.setText("");

        view.campoActivo = view.txtF;
        view.txtF.requestFocus();
    }

    private void escribirEnCampo(String texto) {
        if (view.campoActivo != null && view.campoActivo.isEnabled()) {
            view.campoActivo.setText(view.campoActivo.getText() + texto);
            view.campoActivo.requestFocus();
        }
    }

    private void limpiarInterfaz() {
        view.txtF.setText(""); view.txtG.setText("");
        view.txtA.setText(""); view.txtB.setText("");
        view.lblRenderMath.setIcon(null);
    }

    private void ejecutarCalculo() {
        String f = view.txtF.getText().trim();
        String g = view.txtG.getText().trim();
        String a = view.txtA.getText().trim();
        String b = view.txtB.getText().trim();
        String var = view.rbX.isSelected() ? "x" : "y";
        int modo = view.comboModo.getSelectedIndex();

        if (f.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Debes ingresar una función f(var).", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Genera la cadena LaTeX a través del modelo
            String latexResult = model.calcularLaTeX(modo, f, g, a, b, var);

            // Renderiza LaTeX usando JLaTeXMath
            TeXFormula formula = new TeXFormula(latexResult);
            Icon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 22);
            view.lblRenderMath.setIcon(icon);

        } catch (Exception ex) {
            view.lblRenderMath.setIcon(null);
            JOptionPane.showMessageDialog(view,
                    "Error de Sintaxis Matemática.\nDetalle: " + ex.getMessage() + "\n\nAsegúrate de escribir operadores explícitos (ej. 2*x).",
                    "Excepción de Symja",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
