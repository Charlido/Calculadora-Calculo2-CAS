package view.components;

import javax.swing.*;
import java.awt.*;

public class BotonRedondeado extends JButton {
    private final Color colorCara;
    private final Color colorHover;
    private final Color colorSombra;
    private final Color colorDeshabilitado = Color.decode("#1E1C36");
    private final Color colorSombraDeshabilitado = Color.decode("#141226");
    private final Color colorTextoDeshabilitado = Color.decode("#56527A");
    private final int radioRedondeo = 22; // Bordes redondeados modernos y proporcionados
    private final int profundidadSombra = 5; // Altura del switch mecánico 3D

    public BotonRedondeado(String texto, Color colorBase) {
        super(texto);
        this.colorCara = colorBase;
        this.colorHover = aclararColor(colorBase, 0.16f);
        this.colorSombra = oscurecerColor(colorBase, 0.35f);

        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);

        setFont(new Font("Segoe UI", Font.BOLD, 20));
        setForeground(new Color(80, 80, 80));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private Color oscurecerColor(Color color, float factor) {
        int r = Math.max((int) (color.getRed() * (1 - factor)), 0);
        int g = Math.max((int) (color.getGreen() * (1 - factor)), 0);
        int b = Math.max((int) (color.getBlue() * (1 - factor)), 0);
        return new Color(r, g, b);
    }

    private Color aclararColor(Color color, float factor) {
        int r = Math.min((int) (color.getRed() + (255 - color.getRed()) * factor), 255);
        int g = Math.min((int) (color.getGreen() + (255 - color.getGreen()) * factor), 255);
        int b = Math.min((int) (color.getBlue() + (255 - color.getBlue()) * factor), 255);
        return new Color(r, g, b);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int ancho = getWidth();
        int alto = getHeight();
        ButtonModel model = getModel();
        boolean habilitado = isEnabled();
        boolean presionado = habilitado && model.isPressed();
        boolean hover = habilitado && model.isRollover();

        int offsetY = presionado ? profundidadSombra : (hover ? 1 : 0);
        int alturaCara = alto - profundidadSombra;

        // 1. Dibujar la sombra (base fija anclada al fondo)
        g2.setColor(habilitado ? colorSombra : colorSombraDeshabilitado);
        g2.fillRoundRect(0, profundidadSombra, ancho, alturaCara, radioRedondeo, radioRedondeo);

        // 2. Dibujar la cara superior (cambia de tono en hover y baja al presionar)
        g2.setColor(!habilitado ? colorDeshabilitado : (hover && !presionado ? colorHover : colorCara));
        g2.fillRoundRect(0, offsetY, ancho, alturaCara, radioRedondeo, radioRedondeo);

        // 3. Borde sutil de brillo en estado hover
        if (hover && !presionado) {
            g2.setColor(new Color(255, 255, 255, 70));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(1, offsetY + 1, ancho - 3, alturaCara - 3, radioRedondeo, radioRedondeo);
        }

        // 4. Dibujar la tipografía centrada
        g2.setFont(getFont());
        g2.setColor(habilitado ? getForeground() : colorTextoDeshabilitado);
        FontMetrics fm = g2.getFontMetrics();
        int textX = (ancho - fm.stringWidth(getText())) / 2;
        int textY = (alturaCara - fm.getHeight()) / 2 + fm.getAscent() + offsetY;
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }
}
