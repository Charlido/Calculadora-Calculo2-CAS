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
    public BotonRedondeado btnCalcular, btnLimpiar, btnManual; // Declaramos btnManual
    public JPanel panelTeclado;

    public BotonRedondeado[] botonesTeclado = new BotonRedondeado[20];

    // Paleta de colores Retro-Kawaii Pastel
    private final Color fondoPastel = Color.decode("#D0E8F2");     // Azul cielo
    private final Color colorNumeros = Color.decode("#E8DAEF");    // Morado pastel
    private final Color colorOperadores = Color.decode("#FCF3CF"); // Amarillo pastel
    private final Color colorFunciones = Color.decode("#FADBD8");  // Rosa pastel
    private final Color colorAcciones = Color.decode("#F5B7B1");   // Rojo/Naranja pastel

    public CalculatorView() {
        setTitle("Calculadora Kawaii - Symja & LaTeX");
        setSize(550, 780); // Ligeramente más ancha para los botones
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        getContentPane().setBackground(fondoPastel);

        // =======================================
        // PANEL SUPERIOR: Entradas dinámicas
        // =======================================
        // Configuramos 7 filas exactas
        JPanel panelTop = new JPanel(new GridLayout(7, 2, 5, 5));
        panelTop.setBorder(new EmptyBorder(15, 15, 5, 15));
        panelTop.setOpaque(false);

        comboModo = new JComboBox<>(new String[]{
                "Derivada Simbólica",
                "Integral Indefinida",
                "Integral Definida (Área)",
                "Área entre curvas f(x) - g(x)",
                "Volumen de Revolución"
        });

        txtF = new JTextField();
        txtG = new JTextField();
        txtA = new JTextField();
        txtB = new JTextField();
        campoActivo = txtF;

        JPanel panelRadios = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelRadios.setOpaque(false);
        rbX = new JRadioButton("dx", true); rbX.setOpaque(false);
        rbY = new JRadioButton("dy"); rbY.setOpaque(false);
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbX); bg.add(rbY);
        panelRadios.add(rbX); panelRadios.add(rbY);

        panelTop.add(new JLabel("Modo de Operación:")); panelTop.add(comboModo);
        panelTop.add(new JLabel("Variable:")); panelTop.add(panelRadios);
        panelTop.add(new JLabel("Función f(var):")); panelTop.add(txtF);
        panelTop.add(new JLabel("Función g(var):")); panelTop.add(txtG);
        panelTop.add(new JLabel("Límite inferior a:")); panelTop.add(txtA);
        panelTop.add(new JLabel("Límite superior b:")); panelTop.add(txtB);

        // El botón de ayuda bien instanciado
        panelTop.add(new JLabel("Ayuda:"));
        btnManual = new BotonRedondeado("📖 Ver Manual", colorFunciones);
        panelTop.add(btnManual);

        // =======================================
        // PANEL CENTRAL: Renderizado LaTeX
        // =======================================
        JPanel panelDisplay = new JPanel(new BorderLayout());
        panelDisplay.setBorder(BorderFactory.createTitledBorder("Resultado Exacto"));
        panelDisplay.setBackground(Color.WHITE);

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

        construirTecladoKawaii();
    }

    private void construirTecladoKawaii() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        String[] funciones = {"sin(", "cos(", "tan(", "ln(", "x", "y"};
        for (int i = 0; i < funciones.length; i++) {
            gbc.gridx = i; gbc.gridy = 0;
            botonesTeclado[i] = new BotonRedondeado(funciones[i], colorFunciones);
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
                    btnCalcular = new BotonRedondeado(txt, colorAcciones);
                    gbc.gridheight = 2;
                    panelTeclado.add(btnCalcular, gbc);
                } else if (txt.matches("[0-9]")) {
                    botonesTeclado[btnIndex] = new BotonRedondeado(txt, colorNumeros);
                    panelTeclado.add(botonesTeclado[btnIndex], gbc);
                    btnIndex++;
                } else {
                    botonesTeclado[btnIndex] = new BotonRedondeado(txt, colorOperadores);
                    panelTeclado.add(botonesTeclado[btnIndex], gbc);
                    btnIndex++;
                }
            }
        }

        gbc.gridheight = 1;
        gbc.gridy = 4;

        gbc.gridx = 0; gbc.gridwidth = 2;
        botonesTeclado[18] = new BotonRedondeado("0", colorNumeros);
        panelTeclado.add(botonesTeclado[18], gbc);

        gbc.gridx = 2; gbc.gridwidth = 1;
        botonesTeclado[19] = new BotonRedondeado(".", colorNumeros);
        panelTeclado.add(botonesTeclado[19], gbc);

        gbc.gridx = 4; gbc.gridwidth = 1;
        btnLimpiar = new BotonRedondeado("C", colorAcciones);
        panelTeclado.add(btnLimpiar, gbc);
    }
}