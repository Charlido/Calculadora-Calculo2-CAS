package controller;

import model.MathEngine;
import model.Polinomio;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;
import view.CalculatorView;

import javax.swing.*;
import java.awt.*;
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
                view.actualizarEstiloCampos();
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

        view.btnGraficar.addActionListener(e -> {
            audio.reproducirSonidoClick();
            mostrarGrafica();
        });
    }

    private void actualizarEstadoCampos() {
        int modo = view.comboModo.getSelectedIndex();

        boolean requiereLimites = (modo >= 2);
        boolean requiereG = (modo == 3);

        view.txtA.setEnabled(requiereLimites);
        view.txtB.setEnabled(requiereLimites);
        view.txtG.setEnabled(requiereG);

        if (!requiereLimites) {
            view.txtA.setText("");
            view.txtB.setText("");
        }
        if (!requiereG) {
            view.txtG.setText("");
        }

        view.campoActivo = view.txtF;
        view.txtF.requestFocus();
        view.actualizarEstiloCampos();
    }

    private void escribirEnCampo(String texto) {
        if (view.campoActivo != null && view.campoActivo.isEnabled()) {
            view.campoActivo.setText(view.campoActivo.getText() + texto);
            view.campoActivo.requestFocus();
        }
    }

    private void limpiarInterfaz() {
        view.txtF.setText("");
        view.txtG.setText("");
        view.txtA.setText("");
        view.txtB.setText("");
        view.mostrarPlaceholderResultado();
    }

    private void ejecutarCalculo() {
        String f = view.txtF.getText().trim();
        String g = view.txtG.getText().trim();
        String a = view.txtA.getText().trim();
        String b = view.txtB.getText().trim();
        String var = view.rbX.isSelected() ? "x" : "y";
        int modo = view.comboModo.getSelectedIndex();

        if (f.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Debes ingresar una función principal en f(var).", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String latexResult = model.calcularLaTeX(modo, f, g, a, b, var);
            TeXFormula formula = new TeXFormula(latexResult);
            TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 24);
            icon.setForeground(Color.decode("#00E676")); // Fórmula en verde neón sobre visor oscuro
            view.mostrarIconoResultado(icon);

        } catch (IllegalArgumentException ex) {
            view.mostrarPlaceholderResultado();
            JOptionPane.showMessageDialog(view, ex.getMessage(), "Error de Ingreso", JOptionPane.WARNING_MESSAGE);

        } catch (Exception ex) {
            view.mostrarPlaceholderResultado();
            JOptionPane.showMessageDialog(view, "No se pudo renderizar la fórmula matemática.\nDetalle: " + ex.getMessage(), "Error Interno", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarGrafica() {
        String f = view.txtF.getText().trim();
        String g = view.txtG.getText().trim();
        String a = view.txtA.getText().trim();
        String b = view.txtB.getText().trim();

        if (f.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Debes ingresar una función f(var) para graficar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Polinomio polinomioF = Polinomio.parse(f);
            Polinomio polinomioG = null;
            if (view.txtG.isEnabled() && !g.isEmpty()) {
                polinomioG = Polinomio.parse(g);
            }

            Double limA = null;
            Double limB = null;
            if (view.txtA.isEnabled() && !a.isEmpty() && !b.isEmpty()) {
                try {
                    limA = Double.parseDouble(a);
                    limB = Double.parseDouble(b);
                } catch (NumberFormatException ignored) {
                    // Si los límites aún no son válidos, graficamos igualmente la curva
                }
            }

            JDialog dialogGrafica = new JDialog(view, "Gráfica de la Función", true);
            dialogGrafica.setSize(680, 540);
            dialogGrafica.setLocationRelativeTo(view);
            dialogGrafica.setLayout(new BorderLayout());

            JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
            panelInfo.setBackground(Color.decode("#1E1B3A"));
            panelInfo.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#00E676")));

            StringBuilder titulo = new StringBuilder("f(x) = ").append(f);
            if (polinomioG != null) {
                titulo.append("   |   g(x) = ").append(g);
            }
            if (limA != null && limB != null) {
                titulo.append(String.format("   |   Intervalo [%.2f, %.2f]", limA, limB));
            }

            JLabel lblTitulo = new JLabel(titulo.toString());
            lblTitulo.setForeground(Color.decode("#00E676"));
            lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
            panelInfo.add(lblTitulo);

            view.components.PanelGrafico grafico = new view.components.PanelGrafico(polinomioF, polinomioG, limA, limB);

            dialogGrafica.add(panelInfo, BorderLayout.NORTH);
            dialogGrafica.add(grafico, BorderLayout.CENTER);
            dialogGrafica.setVisible(true);

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(view, "Error de sintaxis matemática. Revisa la función antes de graficar.", "Error de Graficador", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarManualUsuario() {
        JDialog dialogManual = new JDialog(view, "Manual de Usuario", true);
        dialogManual.setSize(600, 700);
        dialogManual.setLocationRelativeTo(view);
        dialogManual.getContentPane().setBackground(Color.decode("#0F0D1C"));

        JTextPane textoManual = new JTextPane();
        textoManual.setContentType("text/html");
        textoManual.setEditable(false);
        textoManual.setBackground(Color.decode("#0F0D1C"));

        String htmlContent = "<html>\n" +
                "<body style=\"font-family: 'Segoe UI', Arial, sans-serif; font-size: 14px; color: #D1D1E9; background-color: #0F0D1C; margin: 0; padding: 20px;\">\n" +
                "\n" +
                "    <!-- ENCABEZADO CYBERPUNK -->\n" +
                "    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin-bottom: 25px;\">\n" +
                "        <tr>\n" +
                "            <td bgcolor=\"#1E1B3A\" align=\"center\" valign=\"middle\" style=\"background-color: #1E1B3A; padding: 25px; border: 2px solid #00E676;\">\n" +
                "                <b style=\"font-size: 22px; letter-spacing: 4px; color: #00E676;\">MANUAL DE USUARIO</b>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "\n" +
                "    <!-- SECCIÓN 1 -->\n" +
                "    <div style=\"color: #00E676; font-size: 15px; font-weight: bold; letter-spacing: 2px; text-transform: uppercase; margin-left: 45px; margin-bottom: 5px;\">\n" +
                "        1. SELECCIÓN DEL MODO DE OPERACIÓN\n" +
                "    </div>\n" +
                "    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin-bottom: 20px;\">\n" +
                "        <tr>\n" +
                "            <td width=\"45\" valign=\"top\" align=\"center\" style=\"font-size: 28px; font-weight: bold; color: #E91E63; padding-right: 10px; padding-top: 2px;\">\n" +
                "                &#9881;\n" +
                "            </td>\n" +
                "            <td bgcolor=\"#1E1B3A\" style=\"background-color: #1E1B3A; padding: 15px; color: #D1D1E9; line-height: 1.5; border-left: 3px solid #E91E63;\">\n" +
                "                En la parte superior, utiliza el menú desplegable para elegir el cálculo exacto que deseas realizar:\n" +
                "                <ul style=\"margin-top: 8px; margin-bottom: 0; padding-left: 20px;\">\n" +
                "                    <li style=\"margin-bottom: 5px;\"><b style=\"color:#FFFFFF;\">Derivada Simbólica:</b> Calcula la derivada exacta del polinomio.</li>\n" +
                "                    <li style=\"margin-bottom: 5px;\"><b style=\"color:#FFFFFF;\">Integral Indefinida:</b> Encuentra la antiderivada (+ C).</li>\n" +
                "                    <li><b style=\"color:#FFFFFF;\">Área (Integral Definida):</b> Calcula el área bajo o entre curvas en un intervalo específico.</li>\n" +
                "                </ul>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "\n" +
                "    <!-- SECCIÓN 2 -->\n" +
                "    <div style=\"color: #00E676; font-size: 15px; font-weight: bold; letter-spacing: 2px; text-transform: uppercase; margin-left: 45px; margin-bottom: 5px;\">\n" +
                "        2. ELECCIÓN DE VARIABLE (DX / DY)\n" +
                "    </div>\n" +
                "    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin-bottom: 20px;\">\n" +
                "        <tr>\n" +
                "            <td width=\"45\" valign=\"top\" align=\"center\" style=\"font-size: 28px; font-weight: bold; color: #E91E63; padding-right: 10px;\">\n" +
                "                &#119909;\n" +
                "            </td>\n" +
                "            <td bgcolor=\"#1E1B3A\" style=\"background-color: #1E1B3A; padding: 15px; color: #D1D1E9; line-height: 1.5; border-left: 3px solid #E91E63;\">\n" +
                "                Selecciona respecto a qué variable se operará. Marca <b style=\"color:#FFFFFF;\">dx</b> si tu función utiliza la letra <b>x</b> (Ej: 3x^2). Marca <b style=\"color:#FFFFFF;\">dy</b> si tu función utiliza la letra <b>y</b> (Ej: 5y^3). <span style=\"color: #E91E63;\">Es vital que la variable elegida coincida con la letra escrita en tu función.</span>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "\n" +
                "    <!-- SECCIÓN 3 -->\n" +
                "    <div style=\"color: #00E676; font-size: 15px; font-weight: bold; letter-spacing: 2px; text-transform: uppercase; margin-left: 45px; margin-bottom: 5px;\">\n" +
                "        3. SINTAXIS MATEMÁTICA CORRECTA\n" +
                "    </div>\n" +
                "    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin-bottom: 20px;\">\n" +
                "        <tr>\n" +
                "            <td width=\"45\" valign=\"top\" align=\"center\" style=\"font-size: 28px; font-weight: bold; color: #E91E63; padding-right: 10px;\">\n" +
                "                &#9998;\n" +
                "            </td>\n" +
                "            <td bgcolor=\"#1E1B3A\" style=\"background-color: #1E1B3A; padding: 15px; color: #D1D1E9; line-height: 1.5; border-left: 3px solid #E91E63;\">\n" +
                "                Para que el motor POO interprete tus polinomios correctamente en <b>f(var)</b> o <b>g(var)</b>, sigue estas estrictas reglas:\n" +
                "                <ul style=\"margin-top: 8px; margin-bottom: 0; padding-left: 20px;\">\n" +
                "                    <li style=\"margin-bottom: 5px;\"><b style=\"color:#FFFFFF;\">Exponentes:</b> Usa el símbolo <code>^</code> para elevar a una potencia (Ejemplo: <code style=\"color: #00E676;\">x^3</code>).</li>\n" +
                "                    <li style=\"margin-bottom: 5px;\"><b style=\"color:#FFFFFF;\">Sin Asteriscos:</b> Escribe coeficientes y variables unidos, sin símbolos de multiplicación (Ejemplo Correcto: <code style=\"color: #00E676;\">3x^2 - 5x + 2</code>).</li>\n" +
                "                    <li><b style=\"color:#FFFFFF;\">Sin Paréntesis:</b> Ingresa el polinomio completamente expandido.</li>\n" +
                "                </ul>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "\n" +
                "    <!-- SECCIÓN 4 -->\n" +
                "    <div style=\"color: #00E676; font-size: 15px; font-weight: bold; letter-spacing: 2px; text-transform: uppercase; margin-left: 45px; margin-bottom: 5px;\">\n" +
                "        4. LÍMITES DE INTEGRACIÓN\n" +
                "    </div>\n" +
                "    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin-bottom: 20px;\">\n" +
                "        <tr>\n" +
                "            <td width=\"45\" valign=\"top\" align=\"center\" style=\"font-size: 28px; font-weight: bold; color: #E91E63; padding-right: 10px;\">\n" +
                "                &#8747;\n" +
                "            </td>\n" +
                "            <td bgcolor=\"#1E1B3A\" style=\"background-color: #1E1B3A; padding: 15px; color: #D1D1E9; line-height: 1.5; border-left: 3px solid #E91E63;\">\n" +
                "                Al seleccionar cálculos de Área (Integrales Definidas), las casillas <b>'a'</b> y <b>'b'</b> se habilitarán. Ingresa el <b>Límite Inferior (a)</b> y el <b>Límite Superior (b)</b> utilizando números enteros o decimales. El programa calculará el área neta entre ambos puntos del plano.\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "\n" +
                "    <!-- SECCIÓN 5 -->\n" +
                "    <div style=\"color: #00E676; font-size: 15px; font-weight: bold; letter-spacing: 2px; text-transform: uppercase; margin-left: 45px; margin-bottom: 5px;\">\n" +
                "        5. CONTROLES Y GRAFICADOR NATIVO\n" +
                "    </div>\n" +
                "    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin-bottom: 25px;\">\n" +
                "        <tr>\n" +
                "            <td width=\"45\" valign=\"top\" align=\"center\" style=\"font-size: 28px; font-weight: bold; color: #E91E63; padding-right: 10px;\">\n" +
                "                &#128433;\n" +
                "            </td>\n" +
                "            <td bgcolor=\"#1E1B3A\" style=\"background-color: #1E1B3A; padding: 15px; color: #D1D1E9; line-height: 1.5; border-left: 3px solid #E91E63;\">\n" +
                "                <ul style=\"margin-top: 0; margin-bottom: 0; padding-left: 20px;\">\n" +
                "                    <li style=\"margin-bottom: 5px;\"><b style=\"color:#FFFFFF;\">Botón Calcular (=):</b> Procesa las funciones y genera la imagen matemática en el panel central.</li>\n" +
                "                    <li style=\"margin-bottom: 5px;\"><b style=\"color:#FFFFFF;\">Botón Limpiar (C):</b> Borra todas las casillas para iniciar un nuevo cálculo.</li>\n" +
                "                    <li><b style=\"color:#FFFFFF;\">Botón Graficar:</b> Abre una ventana que dibuja las curvas f(x), g(x), escalas y el área sombreada en [a, b].</li>\n" +
                "                </ul>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "\n" +
                "</body>\n" +
                "</html>";

        textoManual.setText(htmlContent);
        textoManual.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textoManual);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        dialogManual.add(scrollPane);
        dialogManual.setVisible(true);
    }
}
