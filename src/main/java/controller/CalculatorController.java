package controller;

import model.MathEngine;
import model.MathEngine.PasoResolucion;
import model.Polinomio;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;
import view.CalculatorView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;
import java.util.regex.Pattern;

public class CalculatorController {
    private static final Pattern REGEX_LIMITE_EN_CONSTRUCCION = Pattern.compile("^[+-]?\\d*\\.?\\d*$");

    private final CalculatorView view;
    private final MathEngine model;

    private List<PasoResolucion> pasosActuales;
    private int pasosMostrados = 0;
    private Timer timerPasos;

    public CalculatorController(CalculatorView view, MathEngine model) {
        this.view = view;
        this.model = model;
        inicializarEventos();
        actualizarEstadoCampos();
    }

    private void inicializarEventos() {
        GestorAudio audio = new GestorAudio();

        aplicarFiltroRegexLimites(view.txtA);
        aplicarFiltroRegexLimites(view.txtB);

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

        ActionListener enterListener = e -> {
            audio.reproducirSonidoClick();
            ejecutarCalculo();
        };
        view.txtF.addActionListener(enterListener);
        view.txtG.addActionListener(enterListener);
        view.txtA.addActionListener(enterListener);
        view.txtB.addActionListener(enterListener);

        view.comboModo.addActionListener(e -> actualizarEstadoCampos());

        for (JButton btn : view.botonesTeclado) {
            if (btn != null) {
                btn.addActionListener(e -> {
                    audio.reproducirSonidoClick();
                    escribirEnCampo(btn.getText());
                });
            }
        }

        view.btnBorrar.addActionListener(e -> {
            audio.reproducirSonidoClick();
            borrarUltimoCaracter();
        });

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

    private void aplicarFiltroRegexLimites(JTextField campoLimite) {
        if (!(campoLimite.getDocument() instanceof AbstractDocument)) {
            return;
        }
        ((AbstractDocument) campoLimite.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string == null) return;
                String actual = fb.getDocument().getText(0, fb.getDocument().getLength());
                String candidato = actual.substring(0, offset) + string + actual.substring(offset);
                if (REGEX_LIMITE_EN_CONSTRUCCION.matcher(candidato).matches()) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                String textoNuevo = (text == null) ? "" : text;
                String actual = fb.getDocument().getText(0, fb.getDocument().getLength());
                String candidato = actual.substring(0, offset) + textoNuevo + actual.substring(offset + length);
                if (REGEX_LIMITE_EN_CONSTRUCCION.matcher(candidato).matches()) {
                    super.replace(fb, offset, length, textoNuevo, attrs);
                }
            }
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
            String valorInsertar = texto;
            if (texto.equals("x²")) {
                valorInsertar = "x^2";
            } else if (texto.equals("x³")) {
                valorInsertar = "x^3";
            }

            // Opción 5: Validar con Regex que en los campos de límites (a y b) solo se ingresen números
            if (view.campoActivo == view.txtA || view.campoActivo == view.txtB) {
                String candidato = view.campoActivo.getText() + valorInsertar;
                if (!REGEX_LIMITE_EN_CONSTRUCCION.matcher(candidato).matches()) {
                    view.campoActivo.requestFocus();
                    return;
                }
            }

            view.campoActivo.setText(view.campoActivo.getText() + valorInsertar);
            view.campoActivo.requestFocus();
        }
    }

    private void borrarUltimoCaracter() {
        if (view.campoActivo != null && view.campoActivo.isEnabled()) {
            String actual = view.campoActivo.getText();
            if (!actual.isEmpty()) {
                String[] tokensFuncion = {"sin(", "cos(", "tan(", "ln("};
                for (String token : tokensFuncion) {
                    if (actual.endsWith(token)) {
                        view.campoActivo.setText(actual.substring(0, actual.length() - token.length()));
                        view.campoActivo.requestFocus();
                        return;
                    }
                }
                view.campoActivo.setText(actual.substring(0, actual.length() - 1));
            }
            view.campoActivo.requestFocus();
        }
    }

    private void limpiarInterfaz() {
        detenerTimerPasos();
        pasosActuales = null;
        pasosMostrados = 0;

        view.txtF.setText("");
        view.txtG.setText("");
        view.txtA.setText("");
        view.txtB.setText("");
        view.mostrarPlaceholderResultado();
        view.limpiarPanelPasos();
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
            int fontSize = latexResult.length() > 45 ? 19 : 23;
            TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, fontSize);
            icon.setForeground(Color.decode("#00E676"));
            view.mostrarIconoResultado(icon);

            iniciarDespliegueProgresivoPasos(modo, f, g, a, b, var);

        } catch (IllegalArgumentException ex) {
            detenerTimerPasos();
            view.mostrarPlaceholderResultado();
            view.limpiarPanelPasos();
            JOptionPane.showMessageDialog(view, ex.getMessage(), "Error de Ingreso", JOptionPane.WARNING_MESSAGE);

        } catch (Exception ex) {
            detenerTimerPasos();
            view.mostrarPlaceholderResultado();
            view.limpiarPanelPasos();
            JOptionPane.showMessageDialog(view, "No se pudo renderizar la fórmula matemática.\nDetalle: " + ex.getMessage(), "Error Interno", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void detenerTimerPasos() {
        if (timerPasos != null && timerPasos.isRunning()) {
            timerPasos.stop();
        }
    }

    private void iniciarDespliegueProgresivoPasos(int modo, String f, String g, String a, String b, String var) {
        detenerTimerPasos();
        pasosActuales = model.generarPasosResolucion(modo, f, g, a, b, var);
        pasosMostrados = 0;

        view.contenedorTarjetasPasos.removeAll();
        view.contenedorTarjetasPasos.revalidate();
        view.contenedorTarjetasPasos.repaint();

        if (pasosActuales == null || pasosActuales.isEmpty()) {
            view.limpiarPanelPasos();
            return;
        }

        agregarSiguienteTarjetaPaso();

        if (pasosActuales.size() > 1) {
            timerPasos = new Timer(450, e -> {
                if (pasosMostrados < pasosActuales.size()) {
                    agregarSiguienteTarjetaPaso();
                } else {
                    detenerTimerPasos();
                }
            });
            timerPasos.start();
        }
    }

    private void agregarSiguienteTarjetaPaso() {
        if (pasosActuales == null || pasosMostrados >= pasosActuales.size()) {
            actualizarEstadoProgresoPasos();
            return;
        }

        PasoResolucion paso = pasosActuales.get(pasosMostrados);
        boolean esUltimo = (pasosMostrados == pasosActuales.size() - 1);

        if (pasosMostrados > 0) {
            view.contenedorTarjetasPasos.add(Box.createVerticalStrut(12));
        }
        JPanel tarjeta = crearTarjetaPaso(paso, esUltimo);
        view.contenedorTarjetasPasos.add(tarjeta);
        pasosMostrados++;

        view.contenedorTarjetasPasos.revalidate();
        view.contenedorTarjetasPasos.repaint();
        actualizarEstadoProgresoPasos();

        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = view.scrollPasos.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void actualizarEstadoProgresoPasos() {
        if (pasosActuales == null || pasosActuales.isEmpty()) {
            view.lblProgresoPasos.setText("Esperando cálculo...");
            return;
        }

        int total = pasosActuales.size();
        String estado = (pasosMostrados == total)
                ? String.format("✅ Procedimiento completo (%d pasos)", total)
                : String.format("⏳ Desplegando paso %d de %d...", pasosMostrados, total);
        view.lblProgresoPasos.setText(estado);
    }

    private JPanel crearTarjetaPaso(PasoResolucion paso, boolean esResultadoFinal) {
        Color colorBorde = esResultadoFinal ? Color.decode("#00E676") : Color.decode("#00B0FF");
        Color colorTitulo = esResultadoFinal ? Color.decode("#00E676") : Color.decode("#00B0FF");
        Color colorFormula = esResultadoFinal ? Color.decode("#00E676") : Color.WHITE;

        JPanel tarjeta = new JPanel(new BorderLayout(0, 8));
        tarjeta.setBackground(Color.decode("#1A1733"));
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, colorBorde),
                new EmptyBorder(12, 14, 12, 14)
        ));
        tarjeta.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel panelTexto = new JPanel(new BorderLayout(0, 4));
        panelTexto.setOpaque(false);

        JLabel lblTitulo = new JLabel(paso.titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitulo.setForeground(colorTitulo);

        JLabel lblExplicacion = new JLabel("<html><body style='width: 410px;'>" + paso.explicacion + "</body></html>");
        lblExplicacion.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblExplicacion.setForeground(Color.decode("#D1D1E9"));

        panelTexto.add(lblTitulo, BorderLayout.NORTH);
        panelTexto.add(lblExplicacion, BorderLayout.CENTER);

        JPanel panelFormula = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panelFormula.setBackground(Color.decode("#121024"));
        panelFormula.setBorder(BorderFactory.createLineBorder(Color.decode("#2A274E"), 1, true));

        try {
            TeXFormula tf = new TeXFormula(paso.formulaLaTeX);
            int size = paso.formulaLaTeX.length() > 50 ? 16 : 19;
            TeXIcon icon = tf.createTeXIcon(TeXConstants.STYLE_DISPLAY, size);
            icon.setForeground(colorFormula);
            JLabel lblMath = new JLabel(icon);
            panelFormula.add(lblMath);
        } catch (Exception e) {
            JLabel lblFallback = new JLabel(paso.formulaLaTeX);
            lblFallback.setForeground(colorFormula);
            lblFallback.setFont(new Font("Monospaced", Font.BOLD, 13));
            panelFormula.add(lblFallback);
        }

        tarjeta.add(panelTexto, BorderLayout.NORTH);
        tarjeta.add(panelFormula, BorderLayout.CENTER);
        return tarjeta;
    }

    private void mostrarGrafica() {
        String f = view.txtF.getText().trim();
        String g = view.txtG.getText().trim();
        String a = view.txtA.getText().trim();
        String b = view.txtB.getText().trim();
        String var = view.rbX.isSelected() ? "x" : "y";

        if (f.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Debes ingresar una función f(var) para graficar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Polinomio polinomioF = Polinomio.parse(f, var);
            Polinomio polinomioG = null;
            if (view.txtG.isEnabled() && !g.isEmpty()) {
                polinomioG = Polinomio.parse(g, var);
            }

            Double limA = null;
            Double limB = null;
            if (view.txtA.isEnabled() && !a.isEmpty() && !b.isEmpty()) {
                try {
                    limA = Double.parseDouble(a);
                    limB = Double.parseDouble(b);
                } catch (NumberFormatException ignored) {
                }
            }

            JDialog dialogGrafica = new JDialog(view, "Gráfica de la Función", true);
            dialogGrafica.setSize(680, 540);
            dialogGrafica.setLocationRelativeTo(view);
            dialogGrafica.setLayout(new BorderLayout());

            JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
            panelInfo.setBackground(Color.decode("#1E1B3A"));
            panelInfo.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#00E676")));

            StringBuilder titulo = new StringBuilder("f(" + var + ") = ").append(f);
            if (polinomioG != null) {
                titulo.append("   |   g(" + var + ") = ").append(g);
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
            JOptionPane.showMessageDialog(view, ex.getMessage(), "Error de Graficador", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarManualUsuario() {
        JDialog dialogManual = new JDialog(view, "Manual de Usuario", true);
        dialogManual.setSize(620, 720);
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
                "                En la parte superior, utiliza el menú desplegable para elegir el cálculo que deseas realizar:\n" +
                "                <ul style=\"margin-top: 8px; margin-bottom: 0; padding-left: 20px;\">\n" +
                "                    <li style=\"margin-bottom: 5px;\"><b style=\"color:#FFFFFF;\">Derivada Simbólica:</b> Calcula la derivada exacta de polinomios y funciones trigonométricas/logarítmicas.</li>\n" +
                "                    <li style=\"margin-bottom: 5px;\"><b style=\"color:#FFFFFF;\">Integral Indefinida:</b> Encuentra la antiderivada simbólica (+ C).</li>\n" +
                "                    <li style=\"margin-bottom: 5px;\"><b style=\"color:#FFFFFF;\">Área (Integral Definida / Entre Curvas):</b> Calcula la integral definida o el área geométrica entre dos curvas en [a, b].</li>\n" +
                "                    <li><b style=\"color:#FFFFFF;\">Volumen de Revolución:</b> Calcula el volumen del sólido generado al rotar f(var) en [a, b].</li>\n" +
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
                "                Selecciona respecto a qué variable se operará (<b style=\"color:#FFFFFF;\">dx</b> o <b style=\"color:#FFFFFF;\">dy</b>). El resultado simbólico LaTeX se expresará automáticamente con la variable elegida.\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "\n" +
                "    <!-- SECCIÓN 3 -->\n" +
                "    <div style=\"color: #00E676; font-size: 15px; font-weight: bold; letter-spacing: 2px; text-transform: uppercase; margin-left: 45px; margin-bottom: 5px;\">\n" +
                "        3. SINTAXIS MATEMÁTICA Y FUNCIONES SOPORTADAS\n" +
                "    </div>\n" +
                "    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin-bottom: 20px;\">\n" +
                "        <tr>\n" +
                "            <td width=\"45\" valign=\"top\" align=\"center\" style=\"font-size: 28px; font-weight: bold; color: #E91E63; padding-right: 10px;\">\n" +
                "                &#9998;\n" +
                "            </td>\n" +
                "            <td bgcolor=\"#1E1B3A\" style=\"background-color: #1E1B3A; padding: 15px; color: #D1D1E9; line-height: 1.5; border-left: 3px solid #E91E63;\">\n" +
                "                El motor soporta tanto polinomios como funciones trascendentes:\n" +
                "                <ul style=\"margin-top: 8px; margin-bottom: 0; padding-left: 20px;\">\n" +
                "                    <li style=\"margin-bottom: 5px;\"><b style=\"color:#FFFFFF;\">Polinomios:</b> Usa <code>^</code> o los botones rápidos <code>x²</code> y <code>x³</code> para potencias (Ej: <code style=\"color: #00E676;\">3x^2 - 5x + 2</code>). Los términos semejantes se simplifican automáticamente.</li>\n" +
                "                    <li style=\"margin-bottom: 5px;\"><b style=\"color:#FFFFFF;\">Trigonométricas:</b> Soporta <code>sin</code>, <code>cos</code> y <code>tan</code> (Ej: <code style=\"color: #00E676;\">2sin(3x) - cos(x)</code>).</li>\n" +
                "                    <li><b style=\"color:#FFFFFF;\">Logaritmo Natural:</b> Soporta <code>ln</code> y potencias negativas (Ej: <code style=\"color: #00E676;\">4ln(x) + 1/x</code>).</li>\n" +
                "                </ul>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "\n" +
                "    <!-- SECCIÓN 4 -->\n" +
                "    <div style=\"color: #00E676; font-size: 15px; font-weight: bold; letter-spacing: 2px; text-transform: uppercase; margin-left: 45px; margin-bottom: 5px;\">\n" +
                "        4. PANEL LATERAL PASO A PASO Y CONTROLES\n" +
                "    </div>\n" +
                "    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin-bottom: 25px;\">\n" +
                "        <tr>\n" +
                "            <td width=\"45\" valign=\"top\" align=\"center\" style=\"font-size: 28px; font-weight: bold; color: #E91E63; padding-right: 10px;\">\n" +
                "                &#128433;\n" +
                "            </td>\n" +
                "            <td bgcolor=\"#1E1B3A\" style=\"background-color: #1E1B3A; padding: 15px; color: #D1D1E9; line-height: 1.5; border-left: 3px solid #E91E63;\">\n" +
                "                <ul style=\"margin-top: 0; margin-bottom: 0; padding-left: 20px;\">\n" +
                "                    <li style=\"margin-bottom: 5px;\"><b style=\"color:#FFFFFF;\">Botón Calcular (=) o Enter:</b> Resuelve la expresión y despliega progresivamente todos los pasos en el panel derecho.</li>\n" +
                "                    <li style=\"margin-bottom: 5px;\"><b style=\"color:#FFFFFF;\">Botón Borrar (⌫) y Limpiar (C):</b> <code>⌫</code> borra el último carácter o función; <code>C</code> limpia todos los campos y el procedimiento.</li>\n" +
                "                    <li><b style=\"color:#FFFFFF;\">Botón Graficar (📈):</b> Dibuja en el plano cartesiano las curvas f(x), g(x), escalas y el área sombreada en [a, b].</li>\n" +
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
