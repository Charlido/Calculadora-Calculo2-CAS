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
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;

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

        // Atajo con tecla Enter desde cualquier caja de texto
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

        view.btnPasos.addActionListener(e -> {
            audio.reproducirSonidoClick();
            mostrarPasosResolucion();
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
            int fontSize = latexResult.length() > 45 ? 19 : 23;
            TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, fontSize);
            icon.setForeground(Color.decode("#00E676"));
            view.mostrarIconoResultado(icon);

        } catch (IllegalArgumentException ex) {
            view.mostrarPlaceholderResultado();
            JOptionPane.showMessageDialog(view, ex.getMessage(), "Error de Ingreso", JOptionPane.WARNING_MESSAGE);

        } catch (Exception ex) {
            view.mostrarPlaceholderResultado();
            JOptionPane.showMessageDialog(view, "No se pudo renderizar la fórmula matemática.\nDetalle: " + ex.getMessage(), "Error Interno", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarPasosResolucion() {
        String f = view.txtF.getText().trim();
        String g = view.txtG.getText().trim();
        String a = view.txtA.getText().trim();
        String b = view.txtB.getText().trim();
        String var = view.rbX.isSelected() ? "x" : "y";
        int modo = view.comboModo.getSelectedIndex();
        String nombreModo = (String) view.comboModo.getSelectedItem();

        if (f.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Debes ingresar una función en f(var) para ver los pasos de resolución.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // También actualizamos el visor principal al consultar los pasos
            String latexFinal = model.calcularLaTeX(modo, f, g, a, b, var);
            TeXFormula formulaFinal = new TeXFormula(latexFinal);
            int mainSize = latexFinal.length() > 45 ? 19 : 23;
            TeXIcon mainIcon = formulaFinal.createTeXIcon(TeXConstants.STYLE_DISPLAY, mainSize);
            mainIcon.setForeground(Color.decode("#00E676"));
            view.mostrarIconoResultado(mainIcon);

            List<PasoResolucion> pasos = model.generarPasosResolucion(modo, f, g, a, b, var);

            JDialog dialogPasos = new JDialog(view, "Procedimiento Paso a Paso - " + nombreModo, true);
            dialogPasos.setSize(680, 650);
            dialogPasos.setLocationRelativeTo(view);
            dialogPasos.setLayout(new BorderLayout());
            dialogPasos.getContentPane().setBackground(Color.decode("#0F0D1C"));

            // Encabezado del modal de pasos
            JPanel panelHeader = new JPanel(new BorderLayout());
            panelHeader.setBackground(Color.decode("#1E1B3A"));
            panelHeader.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, Color.decode("#00B0FF")),
                    new EmptyBorder(16, 20, 16, 20)
            ));

            JLabel lblHeaderTitulo = new JLabel("PROCEDIMIENTO DE CÁLCULO: " + nombreModo.toUpperCase(), SwingConstants.CENTER);
            lblHeaderTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblHeaderTitulo.setForeground(Color.decode("#00B0FF"));
            panelHeader.add(lblHeaderTitulo, BorderLayout.CENTER);

            // Contenedor vertical de tarjetas de pasos
            JPanel contenedorPasos = new JPanel();
            contenedorPasos.setLayout(new BoxLayout(contenedorPasos, BoxLayout.Y_AXIS));
            contenedorPasos.setBackground(Color.decode("#0F0D1C"));
            contenedorPasos.setBorder(new EmptyBorder(15, 18, 15, 18));

            for (int i = 0; i < pasos.size(); i++) {
                PasoResolucion paso = pasos.get(i);
                boolean esUltimo = (i == pasos.size() - 1);
                contenedorPasos.add(crearTarjetaPaso(paso, esUltimo));
                contenedorPasos.add(Box.createVerticalStrut(14));
            }

            JScrollPane scrollPasos = new JScrollPane(contenedorPasos);
            scrollPasos.setBorder(null);
            scrollPasos.getVerticalScrollBar().setUnitIncrement(16);
            scrollPasos.setBackground(Color.decode("#0F0D1C"));
            scrollPasos.getViewport().setBackground(Color.decode("#0F0D1C"));

            dialogPasos.add(panelHeader, BorderLayout.NORTH);
            dialogPasos.add(scrollPasos, BorderLayout.CENTER);
            dialogPasos.setVisible(true);

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(view, ex.getMessage(), "Error de Ingreso", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Error al generar los pasos de resolución:\n" + ex.getMessage(), "Error Interno", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel crearTarjetaPaso(PasoResolucion paso, boolean esResultadoFinal) {
        Color colorBorde = esResultadoFinal ? Color.decode("#00E676") : Color.decode("#4E498A");
        Color colorTitulo = esResultadoFinal ? Color.decode("#00E676") : Color.decode("#00B0FF");
        Color colorFormula = esResultadoFinal ? Color.decode("#00E676") : Color.WHITE;

        JPanel tarjeta = new JPanel(new BorderLayout(0, 8));
        tarjeta.setBackground(Color.decode("#1A1733"));
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, colorBorde),
                new EmptyBorder(12, 16, 14, 16)
        ));

        // Encabezado y explicación teórica del paso
        JPanel panelTexto = new JPanel(new BorderLayout(0, 4));
        panelTexto.setOpaque(false);

        JLabel lblTitulo = new JLabel(paso.titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitulo.setForeground(colorTitulo);

        JLabel lblExplicacion = new JLabel("<html><body style='width: 520px;'>" + paso.explicacion + "</body></html>");
        lblExplicacion.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblExplicacion.setForeground(Color.decode("#D1D1E9"));

        panelTexto.add(lblTitulo, BorderLayout.NORTH);
        panelTexto.add(lblExplicacion, BorderLayout.CENTER);

        // Visor LaTeX del paso
        JPanel panelFormula = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panelFormula.setBackground(Color.decode("#121024"));
        panelFormula.setBorder(BorderFactory.createLineBorder(Color.decode("#2A274E"), 1, true));

        try {
            TeXFormula tf = new TeXFormula(paso.formulaLaTeX);
            int size = paso.formulaLaTeX.length() > 55 ? 17 : 20;
            TeXIcon icon = tf.createTeXIcon(TeXConstants.STYLE_DISPLAY, size);
            icon.setForeground(colorFormula);
            JLabel lblMath = new JLabel(icon);
            panelFormula.add(lblMath);
        } catch (Exception e) {
            JLabel lblFallback = new JLabel(paso.formulaLaTeX);
            lblFallback.setForeground(colorFormula);
            lblFallback.setFont(new Font("Monospaced", Font.BOLD, 14));
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
                "                    <li style=\"margin-bottom: 5px;\"><b style=\"color:#FFFFFF;\">Polinomios:</b> Usa <code>^</code> para potencias (Ej: <code style=\"color: #00E676;\">3x^2 - 5x + 2</code>). Los términos semejantes se simplifican automáticamente.</li>\n" +
                "                    <li style=\"margin-bottom: 5px;\"><b style=\"color:#FFFFFF;\">Trigonométricas:</b> Soporta <code>sin</code>, <code>cos</code> y <code>tan</code> (Ej: <code style=\"color: #00E676;\">2sin(3x) - cos(x)</code>).</li>\n" +
                "                    <li><b style=\"color:#FFFFFF;\">Logaritmo Natural:</b> Soporta <code>ln</code> y potencias negativas (Ej: <code style=\"color: #00E676;\">4ln(x) + 1/x</code>).</li>\n" +
                "                </ul>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "\n" +
                "    <!-- SECCIÓN 4 -->\n" +
                "    <div style=\"color: #00E676; font-size: 15px; font-weight: bold; letter-spacing: 2px; text-transform: uppercase; margin-left: 45px; margin-bottom: 5px;\">\n" +
                "        4. CONTROLES, PASOS Y GRAFICADOR\n" +
                "    </div>\n" +
                "    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"margin-bottom: 25px;\">\n" +
                "        <tr>\n" +
                "            <td width=\"45\" valign=\"top\" align=\"center\" style=\"font-size: 28px; font-weight: bold; color: #E91E63; padding-right: 10px;\">\n" +
                "                &#128433;\n" +
                "            </td>\n" +
                "            <td bgcolor=\"#1E1B3A\" style=\"background-color: #1E1B3A; padding: 15px; color: #D1D1E9; line-height: 1.5; border-left: 3px solid #E91E63;\">\n" +
                "                <ul style=\"margin-top: 0; margin-bottom: 0; padding-left: 20px;\">\n" +
                "                    <li style=\"margin-bottom: 5px;\"><b style=\"color:#FFFFFF;\">Botón Calcular (=) o Tecla Enter:</b> Ejecuta el cálculo inmediatamente.</li>\n" +
                "                    <li style=\"margin-bottom: 5px;\"><b style=\"color:#FFFFFF;\">Botón Pasos (📝):</b> Abre el desglose paso a paso explicando las reglas y teoremas del curso de Cálculo utilizados.</li>\n" +
                "                    <li style=\"margin-bottom: 5px;\"><b style=\"color:#FFFFFF;\">Botón Borrar (⌫) y Limpiar (C):</b> <code>⌫</code> borra el último carácter o función; <code>C</code> limpia todos los campos.</li>\n" +
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
