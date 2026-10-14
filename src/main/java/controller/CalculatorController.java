package controller;

import model.MathEngine;
import view.CalculatorView;
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
        actualizarEstadoCampos();
    }

    private void inicializarEventos() {
        GestorAudio audio = new GestorAudio();

        FocusAdapter tracker = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                view.campoActivo = (JTextField) e.getSource();
            }
        };
        view.txtF.addFocusListener(tracker);
        view.txtG.addFocusListener(tracker);
        view.txtA.addFocusListener(tracker);
        view.txtB.addFocusListener(tracker);

        view.comboModo.addActionListener(e -> actualizarEstadoCampos());

        for (JButton btn : view.botonesTeclado) {
            if (btn != null) {
                btn.addActionListener(e -> {
                    audio.reproducirSonidoClick();
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

        view.btnManual.addActionListener(e -> {
            audio.reproducirSonidoClick();
            mostrarManualUsuario();
        });
    }

    private void actualizarEstadoCampos() {
        int modo = view.comboModo.getSelectedIndex();

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
            String latexResult = model.calcularLaTeX(modo, f, g, a, b, var);
            org.scilab.forge.jlatexmath.TeXFormula formula = new org.scilab.forge.jlatexmath.TeXFormula(latexResult);
            Icon icon = formula.createTeXIcon(org.scilab.forge.jlatexmath.TeXConstants.STYLE_DISPLAY, 22);
            view.lblRenderMath.setIcon(icon);
        } catch (Exception ex) {
            view.lblRenderMath.setIcon(null);
            JOptionPane.showMessageDialog(view,
                    "Error de Sintaxis Matemática.\nDetalle: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarManualUsuario() {
        JDialog dialogManual = new JDialog(view, "Manual de Usuario", true);
        dialogManual.setSize(500, 600);
        dialogManual.setLocationRelativeTo(view);
        dialogManual.getContentPane().setBackground(new java.awt.Color(208, 232, 242));

        JTextPane textoManual = new JTextPane();
        textoManual.setContentType("text/html");
        textoManual.setEditable(false);
        textoManual.setBackground(new java.awt.Color(255, 255, 255));

        String htmlContent = "<html>\n" +
                "<body style=\"font-family: 'Segoe UI', Arial, sans-serif; font-size: 11px; color: #383C3F; background-color: #FFFFFF; margin: 0; padding: 15px;\">\n" +
                "\n" +
                "    <!-- ENCABEZADO ESTILO BROCHURE GEOMÉTRICO -->\n" +
                "    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin-bottom: 18px;\">\n" +
                "        <tr>\n" +
                "            <!-- Bloque principal azul pizarra ocupando todo el ancho -->\n" +
                "            <td bgcolor=\"#4A5264\" align=\"center\" valign=\"middle\" style=\"background-color: #4A5264; padding: 20px; color: #FFFFFF;\">\n" +
                "                <b style=\"font-size: 18px; letter-spacing: 3px;\">MANUAL DE USUARIO</b>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "\n" +
                "    <!-- SECCIÓN 1 -->\n" +
                "    <div style=\"color: #4A5264; font-size: 11px; font-weight: bold; letter-spacing: 2px; text-transform: uppercase; margin-left: 42px; margin-bottom: 4px;\">\n" +
                "        SELECCIÓN DEL MODO DE OPERACIÓN\n" +
                "    </div>\n" +
                "    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin-bottom: 16px;\">\n" +
                "        <tr>\n" +
                "            <td width=\"38\" valign=\"middle\" align=\"center\" style=\"font-size: 24px; font-weight: bold; color: #383C3F; padding-right: 6px;\">\n" +
                "                1\n" +
                "            </td>\n" +
                "            <td bgcolor=\"#EFECE6\" style=\"background-color: #EFECE6; padding: 10px 12px; color: #383C3F; line-height: 1.4;\">\n" +
                "                En la parte superior, utiliza el menú desplegable <b>Modo de Operación</b> para elegir el cálculo:\n" +
                "                <ul style=\"margin-top: 5px; margin-bottom: 5px; padding-left: 18px;\">\n" +
                "                    <li><b>Derivada Simbólica:</b> Calcula la derivada exacta del polinomio.</li>\n" +
                "                    <li><b>Integral Indefinida:</b> Encuentra la antiderivada.</li>\n" +
                "                    <li><b>Área / Integral Definida:</b> Calcula el área bajo la curva o entre dos curvas en un intervalo <code>[a, b]</code>.</li>\n" +
                "                </ul>\n" +
                "            </td>\n" +
                "            <td width=\"8\" bgcolor=\"#383C3F\" style=\"background-color: #383C3F;\">&nbsp;</td>\n" +
                "            <td width=\"32\" bgcolor=\"#8FA4A6\" align=\"center\" valign=\"middle\" style=\"background-color: #8FA4A6; color: #FFFFFF; font-size: 14px; font-weight: bold;\">\n" +
                "                &#9881;\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "\n" +
                "    <!-- SECCIÓN 2 -->\n" +
                "    <div style=\"color: #4A5264; font-size: 11px; font-weight: bold; letter-spacing: 2px; text-transform: uppercase; margin-left: 42px; margin-bottom: 4px;\">\n" +
                "        ELECCIÓN DE LA VARIABLE (DX / DY)\n" +
                "    </div>\n" +
                "    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin-bottom: 16px;\">\n" +
                "        <tr>\n" +
                "            <td width=\"38\" valign=\"middle\" align=\"center\" style=\"font-size: 24px; font-weight: bold; color: #383C3F; padding-right: 6px;\">\n" +
                "                2\n" +
                "            </td>\n" +
                "            <td bgcolor=\"#EFECE6\" style=\"background-color: #EFECE6; padding: 10px 12px; color: #383C3F; line-height: 1.4;\">\n" +
                "                Debajo del modo, selecciona respecto a qué variable deseas derivar o integrar:\n" +
                "                <ul style=\"margin-top: 5px; margin-bottom: 5px; padding-left: 18px;\">\n" +
                "                    <li>Marca <b>dx</b> si tu función depende de <b>x</b> (Ej: <code style=\"background-color: #FFFFFF; padding: 1px 4px;\">3x^2 + 2x</code>).</li>\n" +
                "                    <li>Marca <b>dy</b> si tu función depende de <b>y</b> (Ej: <code style=\"background-color: #FFFFFF; padding: 1px 4px;\">y^3 - 5y</code>).</li>\n" +
                "                </ul>\n" +
                "                <b style=\"color: #4A5264;\">¡Importante!:</b> Asegúrate de que la letra coincida con la variable seleccionada.\n" +
                "            </td>\n" +
                "            <td width=\"8\" bgcolor=\"#383C3F\" style=\"background-color: #383C3F;\">&nbsp;</td>\n" +
                "            <td width=\"32\" bgcolor=\"#8FA4A6\" align=\"center\" valign=\"middle\" style=\"background-color: #8FA4A6; color: #FFFFFF; font-size: 12px; font-weight: bold;\">\n" +
                "                dx\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "\n" +
                "    <!-- SECCIÓN 3 -->\n" +
                "    <div style=\"color: #4A5264; font-size: 11px; font-weight: bold; letter-spacing: 2px; text-transform: uppercase; margin-left: 42px; margin-bottom: 4px;\">\n" +
                "        INGRESO DE FUNCIONES: F(VAR) Y G(VAR)\n" +
                "    </div>\n" +
                "    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin-bottom: 16px;\">\n" +
                "        <tr>\n" +
                "            <td width=\"38\" valign=\"middle\" align=\"center\" style=\"font-size: 24px; font-weight: bold; color: #383C3F; padding-right: 6px;\">\n" +
                "                3\n" +
                "            </td>\n" +
                "            <td bgcolor=\"#EFECE6\" style=\"background-color: #EFECE6; padding: 10px 12px; color: #383C3F; line-height: 1.4;\">\n" +
                "                <ul style=\"margin-top: 2px; margin-bottom: 2px; padding-left: 18px;\">\n" +
                "                    <li><b>Función f(var):</b> Casilla principal para ingresar tu polinomio.</li>\n" +
                "                    <li><b>Función g(var):</b> Se activa al calcular área entre dos curvas.</li>\n" +
                "                </ul>\n" +
                "            </td>\n" +
                "            <td width=\"8\" bgcolor=\"#383C3F\" style=\"background-color: #383C3F;\">&nbsp;</td>\n" +
                "            <td width=\"32\" bgcolor=\"#8FA4A6\" align=\"center\" valign=\"middle\" style=\"background-color: #8FA4A6; color: #FFFFFF; font-size: 13px; font-weight: bold;\">\n" +
                "                <i>f(x)</i>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "\n" +
                "    <!-- SECCIÓN 4 -->\n" +
                "    <div style=\"color: #4A5264; font-size: 11px; font-weight: bold; letter-spacing: 2px; text-transform: uppercase; margin-left: 42px; margin-bottom: 4px;\">\n" +
                "        ¿CÓMO ESCRIBIR LAS FUNCIONES CORRECTAMENTE?\n" +
                "    </div>\n" +
                "    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin-bottom: 16px;\">\n" +
                "        <tr>\n" +
                "            <td width=\"38\" valign=\"middle\" align=\"center\" style=\"font-size: 24px; font-weight: bold; color: #383C3F; padding-right: 6px;\">\n" +
                "                4\n" +
                "            </td>\n" +
                "            <td bgcolor=\"#EFECE6\" style=\"background-color: #EFECE6; padding: 10px 12px; color: #383C3F; line-height: 1.4;\">\n" +
                "                Para que el motor POO interprete los polinomios, respeta estas reglas:\n" +
                "                <ul style=\"margin-top: 5px; margin-bottom: 2px; padding-left: 18px;\">\n" +
                "                    <li><b>Exponentes (<code>^</code>):</b> Usa el símbolo <code>^</code> (Ejemplo: <code style=\"background-color: #FFFFFF; padding: 1px 4px;\">x^2</code>).</li>\n" +
                "                    <li><b>Sin Asteriscos:</b> Escribe los coeficientes y variables juntos (Ejemplo: <code style=\"background-color: #FFFFFF; padding: 1px 4px;\">3x^2 - 5x + 2</code>).</li>\n" +
                "                    <li><b>Sin Paréntesis:</b> Ingresa el polinomio en su forma expandida.</li>\n" +
                "                </ul>\n" +
                "            </td>\n" +
                "            <td width=\"8\" bgcolor=\"#383C3F\" style=\"background-color: #383C3F;\">&nbsp;</td>\n" +
                "            <td width=\"32\" bgcolor=\"#8FA4A6\" align=\"center\" valign=\"middle\" style=\"background-color: #8FA4A6; color: #FFFFFF; font-size: 14px; font-weight: bold;\">\n" +
                "                x<sup>n</sup>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "\n" +
                "    <!-- SECCIÓN 5 -->\n" +
                "    <div style=\"color: #4A5264; font-size: 11px; font-weight: bold; letter-spacing: 2px; text-transform: uppercase; margin-left: 42px; margin-bottom: 4px;\">\n" +
                "        LÍMITES DE INTEGRACIÓN (A Y B)\n" +
                "    </div>\n" +
                "    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin-bottom: 16px;\">\n" +
                "        <tr>\n" +
                "            <td width=\"38\" valign=\"middle\" align=\"center\" style=\"font-size: 24px; font-weight: bold; color: #383C3F; padding-right: 6px;\">\n" +
                "                5\n" +
                "            </td>\n" +
                "            <td bgcolor=\"#EFECE6\" style=\"background-color: #EFECE6; padding: 10px 12px; color: #383C3F; line-height: 1.4;\">\n" +
                "                Cuando selecciones una operación definida (como <i>Área</i>), se habilitarán los límites:\n" +
                "                <ul style=\"margin-top: 5px; margin-bottom: 2px; padding-left: 18px;\">\n" +
                "                    <li><b>Límite inferior a:</b> Valor donde inicia el intervalo.</li>\n" +
                "                    <li><b>Límite superior b:</b> Valor donde termina el intervalo.</li>\n" +
                "                </ul>\n" +
                "            </td>\n" +
                "            <td width=\"8\" bgcolor=\"#383C3F\" style=\"background-color: #383C3F;\">&nbsp;</td>\n" +
                "            <td width=\"32\" bgcolor=\"#8FA4A6\" align=\"center\" valign=\"middle\" style=\"background-color: #8FA4A6; color: #FFFFFF; font-size: 15px; font-weight: bold;\">\n" +
                "                &int;\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "\n" +
                "    <!-- SECCIÓN 6 -->\n" +
                "    <div style=\"color: #4A5264; font-size: 11px; font-weight: bold; letter-spacing: 2px; text-transform: uppercase; margin-left: 42px; margin-bottom: 4px;\">\n" +
                "        USO DEL TECLADO EN PANTALLA Y CONTROLES\n" +
                "    </div>\n" +
                "    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin-bottom: 18px;\">\n" +
                "        <tr>\n" +
                "            <td width=\"38\" valign=\"middle\" align=\"center\" style=\"font-size: 24px; font-weight: bold; color: #383C3F; padding-right: 6px;\">\n" +
                "                6\n" +
                "            </td>\n" +
                "            <td bgcolor=\"#EFECE6\" style=\"background-color: #EFECE6; padding: 10px 12px; color: #383C3F; line-height: 1.4;\">\n" +
                "                Puedes ingresar las expresiones con el teclado de tu computadora o con los botones en pantalla:\n" +
                "                <ul style=\"margin-top: 5px; margin-bottom: 2px; padding-left: 18px;\">\n" +
                "                    <li><b>Botón <code>=</code> (Calcular):</b> Ejecuta la operación y la renderiza en el <b>Resultado Exacto</b>.</li>\n" +
                "                    <li><b>Botón <code>C</code> (Limpiar):</b> Vacía todas las casillas para iniciar un cálculo nuevo.</li>\n" +
                "                </ul>\n" +
                "            </td>\n" +
                "            <td width=\"8\" bgcolor=\"#383C3F\" style=\"background-color: #383C3F;\">&nbsp;</td>\n" +
                "            <td width=\"32\" bgcolor=\"#8FA4A6\" align=\"center\" valign=\"middle\" style=\"background-color: #8FA4A6; color: #FFFFFF; font-size: 15px; font-weight: bold;\">\n" +
                "                =\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "\n" +

                "</body>\n" +
                "</html>";
        textoManual.setText(htmlContent);

        JScrollPane scrollPane = new JScrollPane(textoManual);
        scrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        dialogManual.add(scrollPane);
        dialogManual.setVisible(true);
    }
}