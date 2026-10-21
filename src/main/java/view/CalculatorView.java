package view;

import view.components.BotonRedondeado;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CalculatorView extends JFrame {
    public JComboBox<String> comboModo;
    public JTextField txtF, txtG, txtA, txtB, campoActivo;
    public JRadioButton rbX, rbY;
    public JLabel lblRenderMath;
    public BotonRedondeado btnCalcular, btnLimpiar, btnManual, btnGraficar;
    public JPanel panelTeclado;

    public BotonRedondeado[] botonesTeclado = new BotonRedondeado[20];

    // --- NUEVA PALETA DE COLORES "CYBERPUNK / MODO OSCURO" ---
    // Fondo principal manejado por un degradado, estos son los colores base
    private final Color colorOscuroFondo = Color.decode("#1E1B3A"); // Azul oscuro noche
    private final Color colorCajasTexto = Color.decode("#2A274E"); // Un poco más claro que el fondo
    private final Color colorTextoGeneral = Color.WHITE;

    // Colores de Botones
    private final Color colorBtnNumeros = Color.decode("#35325C"); // Azul grisáceo
    private final Color colorBtnOperadores = Color.decode("#4E498A"); // Morado apagado
    private final Color colorBtnAccionPrincipal = Color.decode("#00E676"); // Verde Neón (como el botón "Get Started")
    private final Color colorBtnAccionSecundaria = Color.decode("#E91E63"); // Rosa/Rojo oscuro para "C"
    private final Color colorBordeNeón = Color.decode("#00E676");

    public CalculatorView() {
        setTitle("Calculadora Avanzada - POO & LaTeX");
        setSize(550, 780);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Usamos nuestro nuevo panel con degradado como fondo principal
        PanelDegradado fondoPrincipal = new PanelDegradado();
        fondoPrincipal.setLayout(new BorderLayout(10, 10));
        setContentPane(fondoPrincipal);

        // =======================================
        // PANEL SUPERIOR: Entradas dinámicas
        // =======================================
        JPanel panelTop = new JPanel(new GridLayout(7, 2, 5, 5));
        panelTop.setBorder(new EmptyBorder(15, 15, 5, 15));
        panelTop.setOpaque(false);

        // Estilo de los Labels
        Font fuenteLabels = new Font("Segoe UI", Font.BOLD, 12);

        comboModo = new JComboBox<>(new String[]{
                "Derivada Simbólica",
                "Integral Indefinida",
                "Integral Definida (Área)",
                "Área entre curvas f(x) - g(x)",
                "Volumen de Revolución"
        });
        comboModo.setBackground(colorCajasTexto);
        comboModo.setForeground(colorTextoGeneral);

        // --- ESTILO OSCURO PARA CAJAS DE TEXTO ---
        Font fuenteModerna = new Font("Segoe UI", Font.BOLD, 14);
        javax.swing.border.Border bordeNeón = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(colorBordeNeón, 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        );

        txtF = crearTextFieldOscuro(fuenteModerna, bordeNeón);
        txtG = crearTextFieldOscuro(fuenteModerna, bordeNeón);
        txtA = crearTextFieldOscuro(fuenteModerna, bordeNeón);
        txtB = crearTextFieldOscuro(fuenteModerna, bordeNeón);
        campoActivo = txtF;

        JPanel panelRadios = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelRadios.setOpaque(false);
        rbX = new JRadioButton("dx", true); configurarRadio(rbX, fuenteLabels);
        rbY = new JRadioButton("dy"); configurarRadio(rbY, fuenteLabels);
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbX); bg.add(rbY);
        panelRadios.add(rbX); panelRadios.add(rbY);

        panelTop.add(crearLabelOscuro("Modo de Operación:", fuenteLabels)); panelTop.add(comboModo);
        panelTop.add(crearLabelOscuro("Variable:", fuenteLabels)); panelTop.add(panelRadios);
        panelTop.add(crearLabelOscuro("Función f(var):", fuenteLabels)); panelTop.add(txtF);
        panelTop.add(crearLabelOscuro("Función g(var):", fuenteLabels)); panelTop.add(txtG);
        panelTop.add(crearLabelOscuro("Límite inferior a:", fuenteLabels)); panelTop.add(txtA);
        panelTop.add(crearLabelOscuro("Límite superior b:", fuenteLabels)); panelTop.add(txtB);

        // --- BOTONES DE ACCIÓN (NEÓN) ---
        JPanel panelAcciones = new JPanel(new GridLayout(1, 2, 5, 0));
        panelAcciones.setOpaque(false);

        // El botón principal verde neón, con texto oscuro para contraste
        btnGraficar = new BotonRedondeado("📈 Graficar f(x)", colorBtnAccionPrincipal);
        btnGraficar.setForeground(Color.BLACK);

        btnManual = new BotonRedondeado("📖 Manual", colorBtnOperadores);
        btnManual.setForeground(colorTextoGeneral);

        panelAcciones.add(btnManual);
        panelAcciones.add(btnGraficar);

        panelTop.add(crearLabelOscuro("Opciones Adicionales:", fuenteLabels));
        panelTop.add(panelAcciones);

        // =======================================
        // PANEL CENTRAL: Renderizado LaTeX (Claro para que se lea la fórmula)
        // =======================================
        JPanel panelDisplay = new JPanel(new BorderLayout());
        // Borde verde neón sutil para el área de resultado
        panelDisplay.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(colorBordeNeón), "Resultado Exacto", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, fuenteLabels, colorTextoGeneral));
        panelDisplay.setBackground(Color.WHITE); // Fondo blanco para LaTeX

        lblRenderMath = new JLabel("", SwingConstants.CENTER);
        panelDisplay.add(lblRenderMath, BorderLayout.CENTER);

        JPanel contenedorArriba = new JPanel(new BorderLayout());
        contenedorArriba.setOpaque(false);
        contenedorArriba.add(panelTop, BorderLayout.NORTH);
        contenedorArriba.add(panelDisplay, BorderLayout.CENTER);

        JPanel margenArriba = new JPanel(new BorderLayout());
        margenArriba.setBorder(new EmptyBorder(10, 15, 0, 15));
        margenArriba.setOpaque(false);
        margenArriba.add(contenedorArriba, BorderLayout.CENTER);

        add(margenArriba, BorderLayout.NORTH);

        // =======================================
        // PANEL INFERIOR: Teclado GridBagLayout
        // =======================================
        panelTeclado = new JPanel(new GridBagLayout());
        panelTeclado.setBorder(new EmptyBorder(10, 15, 20, 15));
        panelTeclado.setOpaque(false);
        add(panelTeclado, BorderLayout.CENTER);

        construirTecladoOscuro();
    }

    // Métodos auxiliares para aplicar el estilo a los componentes
    private JLabel crearLabelOscuro(String texto, Font fuente) {
        JLabel lbl = new JLabel(texto);
        lbl.setForeground(colorTextoGeneral);
        lbl.setFont(fuente);
        return lbl;
    }

    private JTextField crearTextFieldOscuro(Font fuente, javax.swing.border.Border borde) {
        JTextField txt = new JTextField();
        txt.setFont(fuente);
        txt.setBorder(borde);
        txt.setBackground(colorCajasTexto);
        txt.setForeground(colorTextoGeneral);
        txt.setCaretColor(colorBordeNeón); // Cursor parpadeante color neón
        return txt;
    }

    private void configurarRadio(JRadioButton rb, Font fuente) {
        rb.setOpaque(false);
        rb.setForeground(colorTextoGeneral);
        rb.setFont(fuente);
    }

    private void construirTecladoOscuro() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        String[] funciones = {"sin(", "cos(", "tan(", "ln(", "x", "y"};
        for (int i = 0; i < funciones.length; i++) {
            gbc.gridx = i;
            gbc.gridy = 0;
            botonesTeclado[i] = new BotonRedondeado(funciones[i], colorBtnOperadores);
            botonesTeclado[i].setForeground(colorTextoGeneral);
            panelTeclado.add(botonesTeclado[i], gbc);
        }

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
                if (col >= 3) {
                    gbc.gridx = col + 1;
                }
                gbc.gridy = fila + 1;
                gbc.gridwidth = 1;
                gbc.gridheight = 1;

                if (txt.equals("=")) {
                    btnCalcular = new BotonRedondeado(txt, colorBtnAccionPrincipal);
                    btnCalcular.setForeground(Color.BLACK); // Contraste con el verde neón
                    gbc.gridheight = 2;
                    panelTeclado.add(btnCalcular, gbc);
                } else if (txt.matches("[0-9]")) {
                    botonesTeclado[btnIndex] = new BotonRedondeado(txt, colorBtnNumeros);
                    botonesTeclado[btnIndex].setForeground(colorTextoGeneral);
                    panelTeclado.add(botonesTeclado[btnIndex], gbc);
                    btnIndex++;
                } else {
                    botonesTeclado[btnIndex] = new BotonRedondeado(txt, colorBtnOperadores);
                    botonesTeclado[btnIndex].setForeground(colorTextoGeneral);
                    panelTeclado.add(botonesTeclado[btnIndex], gbc);
                    btnIndex++;
                }
            }
        }

        gbc.gridheight = 1;
        gbc.gridy = 4;

        gbc.gridx = 0;
        gbc.gridwidth = 2;
        botonesTeclado[18] = new BotonRedondeado("0", colorBtnNumeros);
        botonesTeclado[18].setForeground(colorTextoGeneral);
        panelTeclado.add(botonesTeclado[18], gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 1;
        botonesTeclado[19] = new BotonRedondeado(".", colorBtnNumeros);
        botonesTeclado[19].setForeground(colorTextoGeneral);
        panelTeclado.add(botonesTeclado[19], gbc);

        gbc.gridx = 4;
        gbc.gridwidth = 1;
        btnLimpiar = new BotonRedondeado("C", colorBtnAccionSecundaria);
        btnLimpiar.setForeground(Color.WHITE);
        panelTeclado.add(btnLimpiar, gbc);
    }

    // --- NUEVA CLASE INTERNA PARA EL DEGRADADO OSCURO ---
    class PanelDegradado extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // Degradado de negro a morado oscuro (estilo la imagen de referencia)
            Color colorSuperior = Color.decode("#0F0D1C");
            Color colorInferior = Color.decode("#2E1E3B");

            GradientPaint gp = new GradientPaint(0, 0, colorSuperior, 0, getHeight(), colorInferior);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}