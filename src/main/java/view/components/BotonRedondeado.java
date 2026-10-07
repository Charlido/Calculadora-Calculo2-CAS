package view.components;

import javax.swing.*;
import java.awt.*;

public class BotonRedondeado extends JButton {
    private final Color colorCara;
    private final Color colorSombra;
    private final int radioRedondeo = 35; // Bordes muy circulares (estilo píldora)
    private final int profundidadSombra = 6; // Altura del switch mecánico

    public BotonRedondeado(String texto, Color colorBase) {
        super(texto);
        this.colorCara = colorBase;
        // La sombra se calcula oscureciendo dinámicamente el color pastel base
        this.colorSombra = oscurecerColor(colorBase, 0.25f);

        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);

        setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 18));
        setForeground(new Color(80, 80, 80)); // Gris oscuro carbón para contraste suave
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private Color oscurecerColor(Color color, float factor) {
        int r = Math.max((int) (color.getRed() * (1 - factor)), 0);
        int g = Math.max((int) (color.getGreen() * (1 - factor)), 0);
        int b = Math.max((int) (color.getBlue() * (1 - factor)), 0);
        return new Color(r, g, b);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int ancho = getWidth();
        int alto = getHeight();
        boolean presionado = getModel().isPressed();

        // Calcular desplazamiento en el eje Y al hacer clic
        int offsetY = presionado ? profundidadSombra : 0;
        int alturaCara = alto - profundidadSombra;

        // 1. Dibujar la sombra (base fija anclada al fondo)
        g2.setColor(colorSombra);
        g2.fillRoundRect(0, profundidadSombra, ancho, alturaCara, radioRedondeo, radioRedondeo);

        // 2. Dibujar la cara superior (se desplaza hacia abajo si se presiona)
        g2.setColor(colorCara);
        g2.fillRoundRect(0, offsetY, ancho, alturaCara, radioRedondeo, radioRedondeo);

        // 3. Dibujar la tipografía con cálculo exacto de centro
        g2.setFont(getFont());
        g2.setColor(getForeground());
        FontMetrics fm = g2.getFontMetrics();
        int textX = (ancho - fm.stringWidth(getText())) / 2;
        int textY = (alturaCara - fm.getHeight()) / 2 + fm.getAscent() + offsetY;
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }
}