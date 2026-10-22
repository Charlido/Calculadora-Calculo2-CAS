package view;

import view.components.BotonRedondeado;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class CalculatorView extends JFrame {
    public JComboBox<String> comboModo;
    public JTextField txtF, txtG, txtA, txtB, campoActivo;
    public JLabel lblG, lblA, lblB;
    public JRadioButton rbX, rbY;
    public JLabel lblRenderMath;
    public BotonRedondeado btnCalcular, btnLimpiar, btnManual, btnGraficar;
    public JPanel panelTeclado;

    public BotonRedondeado[] botonesTeclado = new BotonRedondeado[20];

    // --- PALETA DE COLORES "CYBERPUNK / MODO OSCURO" ---
    private final Color colorOscuroFondo = Color.decode("#1E1B3A");
    private final Color colorPantallaMath = Color.decode("#131126");
    private final Color colorCajasTexto = Color.decode("#232044");
    private final Color colorCajasFoco = Color.decode("#2D2958");
    private final Color colorCajasDeshabilitado = Color.decode("#16142B");
    private final Color colorTextoGeneral = Color.WHITE;
    private final Color colorTextoSecundario = Color.decode("#8E8AB8");
    private final Color colorTextoDeshabilitado = Color.decode("#56527A");

    // Colores de Botones y Bordes
    private final Color colorBtnNumeros = Color.decode("#35325C");
    private final Color colorBtnOperadores = Color.decode("#4E498A");
    private final Color colorBtnAccionPrincipal = Color.decode("#00E676");
    private final Color colorBtnAccionSecundaria = Color.decode("#E91E63");
    private final Color colorBordeNeón = Color.decode("#00E676");
    private final Color colorBordeNormal = Color.decode("#4E498A");
    private final Color colorBordeDeshabilitado = Color.decode("#2A274E");

    public CalculatorView() {
        setTitle("Calculadora Avanzada - POO & LaTeX");
        setSize(560, 820);
        setMinimumSize(new Dimension(520, 760));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        PanelDegradado fondoPrincipal = new PanelDegradado();
        fondoPrincipal.setLayout(new BorderLayout(10, 10));
        setContentPane(fondoPrincipal);

        // =======================================
        // PANEL SUPERIOR: Entradas dinámicas
        // =======================================
        JPanel panelTop = new JPanel(new GridLayout(7, 2, 8, 7));
        panelTop.setBorder(new EmptyBorder(12, 15, 8, 15));
        panelTop.setOpaque(false);

        Font fuenteLabels = new Font("Segoe UI", Font.BOLD, 13);

        comboModo = new JComboBox<>(new String[]{
                "Derivada Simbólica",
                "Integral Indefinida",
                "Integral Definida (Área)",
                "Área entre curvas f(x) - g(x)",
                "Volumen de Revolución"
        });
        comboModo.setBackground(colorCajasTexto);
        comboModo.setForeground(colorTextoGeneral);
        comboModo.setFont(new Font("Segoe UI", Font.BOLD, 13));

        Font fuenteModerna = new Font("Segoe UI", Font.BOLD, 14);

        txtF = crearTextFieldOscuro(fuenteModerna);
        txtG = crearTextFieldOscuro(fuenteModerna);
        txtA = crearTextFieldOscuro(fuenteModerna);
        txtB = crearTextFieldOscuro(fuenteModerna);
        campoActivo = txtF;

        JPanel panelRadios = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        panelRadios.setOpaque(false);
        rbX = new JRadioButton("dx", true);
        configurarRadio(rbX, fuenteLabels);
        rbY = new JRadioButton("dy");
        configurarRadio(rbY, fuenteLabels);
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbX);
        bg.add(rbY);
        panelRadios.add(rbX);
        panelRadios.add(rbY);

        lblG = crearLabelOscuro("Función g(var):", fuenteLabels);
        lblA = crearLabelOscuro("Límite inferior a:", fuenteLabels);
        lblB = crearLabelOscuro("Límite superior b:", fuenteLabels);

        panelTop.add(crearLabelOscuro("Modo de Operación:", fuenteLabels));
        panelTop.add(comboModo);
        panelTop.add(crearLabelOscuro("Variable:", fuenteLabels));
        panelTop.add(panelRadios);
        panelTop.add(crearLabelOscuro("Función f(var):", fuenteLabels));
        panelTop.add(txtF);
        panelTop.add(lblG);
        panelTop.add(txtG);
        panelTop.add(lblA);
        panelTop.add(txtA);
        panelTop.add(lblB);
        panelTop.add(txtB);

        // --- BOTONES DE ACCIÓN (NEÓN) ---
        JPanel panelAcciones = new JPanel(new GridLayout(1, 2, 8, 0));
        panelAcciones.setOpaque(false);

        btnGraficar = new BotonRedondeado("📈 Graficar", colorBtnAccionPrincipal);
        btnGraficar.setForeground(Color.decode("#0F0D1C"));
        btnGraficar.setFont(new Font("Segoe UI", Font.BOLD, 13));

        btnManual = new BotonRedondeado("📖 Manual", colorBtnOperadores);
        btnManual.setForeground(colorTextoGeneral);
        btnManual.setFont(new Font("Segoe UI", Font.BOLD, 13));

        panelAcciones.add(btnManual);
        panelAcciones.add(btnGraficar);

        panelTop.add(crearLabelOscuro("Opciones Adicionales:", fuenteLabels));
        panelTop.add(panelAcciones);

        // =======================================
        // PANEL CENTRAL: Visor LaTeX Oscuro Neón
        // =======================================
        JPanel panelDisplay = new JPanel(new BorderLayout());
        panelDisplay.setBackground(colorPantallaMath);
        panelDisplay.setPreferredSize(new Dimension(0, 120));

        TitledBorder bordeTitulo = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(colorBordeNeón, 1, true),
                " RESULTADO EXACTO ",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 11),
                colorBordeNeón
        );
        panelDisplay.setBorder(BorderFactory.createCompoundBorder(
                bordeTitulo,
                new EmptyBorder(8, 12, 8, 12)
        ));

        lblRenderMath = new JLabel("", SwingConstants.CENTER);
        lblRenderMath.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        mostrarPlaceholderResultado();
        panelDisplay.add(lblRenderMath, BorderLayout.CENTER);

        JPanel contenedorArriba = new JPanel(new BorderLayout(0, 8));
        contenedorArriba.setOpaque(false);
        contenedorArriba.add(panelTop, BorderLayout.NORTH);
        contenedorArriba.add(panelDisplay, BorderLayout.CENTER);

        JPanel margenArriba = new JPanel(new BorderLayout());
        margenArriba.setBorder(new EmptyBorder(6, 15, 0, 15));
        margenArriba.setOpaque(false);
        margenArriba.add(contenedorArriba, BorderLayout.CENTER);

        add(margenArriba, BorderLayout.NORTH);

        // =======================================
        // PANEL INFERIOR: Teclado sin huecos
        // =======================================
        panelTeclado = new JPanel(new BorderLayout(0, 8));
        panelTeclado.setBorder(new EmptyBorder(6, 15, 18, 15));
        panelTeclado.setOpaque(false);
        add(panelTeclado, BorderLayout.CENTER);

        construirTecladoOscuro();
        actualizarEstiloCampos();
    }

    public void mostrarPlaceholderResultado() {
        lblRenderMath.setIcon(null);
        lblRenderMath.setForeground(colorTextoSecundario);
        lblRenderMath.setText("Ingresa una expresión y presiona '=' para calcular");
    }

    public void mostrarIconoResultado(Icon icon) {
        lblRenderMath.setText("");
        lblRenderMath.setIcon(icon);
    }

    public void actualizarEstiloCampos() {
        aplicarEstiloCampo(txtF, null);
        aplicarEstiloCampo(txtG, lblG);
        aplicarEstiloCampo(txtA, lblA);
        aplicarEstiloCampo(txtB, lblB);
    }

    private void aplicarEstiloCampo(JTextField txt, JLabel labelAsociado) {
        if (!txt.isEnabled()) {
            txt.setBackground(colorCajasDeshabilitado);
            txt.setForeground(colorTextoDeshabilitado);
            txt.setBorder(crearBordeCampo(colorBordeDeshabilitado, 1));
            if (labelAsociado != null) {
                labelAsociado.setForeground(colorTextoDeshabilitado);
            }
        } else if (txt == campoActivo) {
            txt.setBackground(colorCajasFoco);
            txt.setForeground(colorTextoGeneral);
            txt.setBorder(crearBordeCampo(colorBordeNeón, 2));
            if (labelAsociado != null) {
                labelAsociado.setForeground(colorTextoGeneral);
            }
        } else {
            txt.setBackground(colorCajasTexto);
            txt.setForeground(colorTextoGeneral);
            txt.setBorder(crearBordeCampo(colorBordeNormal, 1));
            if (labelAsociado != null) {
                labelAsociado.setForeground(colorTextoGeneral);
            }
        }
    }

    private Border crearBordeCampo(Color colorLinea, int grosor) {
        int padV = 6 - (grosor - 1);
        int padH = 10 - (grosor - 1);
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(colorLinea, grosor, true),
                BorderFactory.createEmptyBorder(padV, padH, padV, padH)
        );
    }

    private JLabel crearLabelOscuro(String texto, Font fuente) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(colorTextoGeneral);
        lbl.setFont(fuente);
        return lbl;
    }

    private JTextField crearTextFieldOscuro(Font fuente) {
        JTextField txt = new JTextField();
        txt.setFont(fuente);
        txt.setBackground(colorCajasTexto);
        txt.setForeground(colorTextoGeneral);
        txt.setCaretColor(colorBordeNeón);
        txt.setBorder(crearBordeCampo(colorBordeNormal, 1));
        return txt;
    }

    private void configurarRadio(JRadioButton rb, Font fuente) {
        rb.setOpaque(false);
        rb.setForeground(colorTextoGeneral);
        rb.setFont(fuente);
        rb.setFocusPainted(false);
    }

    private void construirTecladoOscuro() {
        // Fila superior de funciones (6 columnas distribuidas uniformemente)
        JPanel filaFunciones = new JPanel(new GridLayout(1, 6, 10, 0));
        filaFunciones.setOpaque(false);
        filaFunciones.setPreferredSize(new Dimension(0, 52));

        String[] funciones = {"sin(", "cos(", "tan(", "ln(", "x", "y"};
        for (int i = 0; i < funciones.length; i++) {
            botonesTeclado[i] = new BotonRedondeado(funciones[i], colorBtnOperadores);
            botonesTeclado[i].setForeground(colorTextoGeneral);
            filaFunciones.add(botonesTeclado[i]);
        }

        // Cuadrícula principal numérica y de operadores (5 columnas uniformes, sin columna vacía)
        JPanel gridPrincipal = new JPanel(new GridBagLayout());
        gridPrincipal.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        String[][] centralYDerecho = {
                {"7", "8", "9", "/", "^"},
                {"4", "5", "6", "*", "+"},
                {"1", "2", "3", "-", "="}
        };

        int btnIndex = 6;
        for (int fila = 0; fila < centralYDerecho.length; fila++) {
            for (int col = 0; col < centralYDerecho[fila].length; col++) {
                String txt = centralYDerecho[fila][col];

                gbc.gridx = col;
                gbc.gridy = fila;
                gbc.gridwidth = 1;
                gbc.gridheight = 1;

                if (txt.equals("=")) {
                    btnCalcular = new BotonRedondeado(txt, colorBtnAccionPrincipal);
                    btnCalcular.setForeground(Color.decode("#0F0D1C"));
                    btnCalcular.setFont(new Font("Segoe UI", Font.BOLD, 22));
                    gbc.gridheight = 2;
                    gridPrincipal.add(btnCalcular, gbc);
                } else if (txt.matches("[0-9]")) {
                    botonesTeclado[btnIndex] = new BotonRedondeado(txt, colorBtnNumeros);
                    botonesTeclado[btnIndex].setForeground(colorTextoGeneral);
                    gridPrincipal.add(botonesTeclado[btnIndex], gbc);
                    btnIndex++;
                } else {
                    botonesTeclado[btnIndex] = new BotonRedondeado(txt, colorBtnOperadores);
                    botonesTeclado[btnIndex].setForeground(colorTextoGeneral);
                    gridPrincipal.add(botonesTeclado[btnIndex], gbc);
                    btnIndex++;
                }
            }
        }

        // Última fila (0, ., C y la parte inferior de =)
        gbc.gridheight = 1;
        gbc.gridy = 3;

        gbc.gridx = 0;
        gbc.gridwidth = 2;
        botonesTeclado[18] = new BotonRedondeado("0", colorBtnNumeros);
        botonesTeclado[18].setForeground(colorTextoGeneral);
        gridPrincipal.add(botonesTeclado[18], gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 1;
        botonesTeclado[19] = new BotonRedondeado(".", colorBtnNumeros);
        botonesTeclado[19].setForeground(colorTextoGeneral);
        gridPrincipal.add(botonesTeclado[19], gbc);

        gbc.gridx = 3;
        gbc.gridwidth = 1;
        btnLimpiar = new BotonRedondeado("C", colorBtnAccionSecundaria);
        btnLimpiar.setForeground(Color.WHITE);
        gridPrincipal.add(btnLimpiar, gbc);

        panelTeclado.add(filaFunciones, BorderLayout.NORTH);
        panelTeclado.add(gridPrincipal, BorderLayout.CENTER);
    }

    // --- CLASE INTERNA PARA EL DEGRADADO OSCURO ---
    class PanelDegradado extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            Color colorSuperior = Color.decode("#0F0D1C");
            Color colorInferior = Color.decode("#2E1E3B");

            GradientPaint gp = new GradientPaint(0, 0, colorSuperior, 0, getHeight(), colorInferior);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
