package view.components;

import model.Polinomio;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

public class PanelGrafico extends JPanel {
    private Polinomio polinomio;
    private final double minX = -10.0;
    private final double maxX = 10.0;
    private double minY = -10.0;
    private double maxY = 10.0;

    // Colores Neón
    private final Color fondoOscuro = Color.decode("#121021"); // Casi negro
    private final Color colorEjes = Color.decode("#4E498A");
    private final Color colorCuadricula = Color.decode("#2A274E");
    private final Color colorCurvaNeon = Color.decode("#00E676"); // Verde Neón

    public PanelGrafico(Polinomio polinomio) {
        this.polinomio = polinomio;
        setBackground(fondoOscuro);
        calcularLimitesY();
    }

    private void calcularLimitesY() {
        if (polinomio == null) return;
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (double x = minX; x <= maxX; x += 0.1) {
            double y = polinomio.evaluar(x);
            if (y < min) min = y;
            if (y > max) max = y;
        }
        double margen = (max - min) * 0.2;
        if (margen == 0) margen = 5;
        this.minY = min - margen;
        this.maxY = max + margen;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int ancho = getWidth();
        int alto = getHeight();

        // 1. Dibujar Cuadrícula Oscura
        g2d.setColor(colorCuadricula);
        g2d.setStroke(new BasicStroke(1));
        for (int i = 0; i < ancho; i += 40) g2d.drawLine(i, 0, i, alto);
        for (int i = 0; i < alto; i += 40) g2d.drawLine(0, i, ancho, i);

        // 2. Dibujar Ejes X e Y
        int origenX = escalarX(0, ancho);
        int origenY = escalarY(0, alto);

        g2d.setColor(colorEjes);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(0, origenY, ancho, origenY); // Eje X
        g2d.drawLine(origenX, 0, origenX, alto);  // Eje Y

        // 3. Dibujar la Curva Matemática (Estilo Neón)
        if (polinomio != null) {
            Path2D curva = new Path2D.Double();
            boolean primerPunto = true;

            for (int pixelX = 0; pixelX < ancho; pixelX++) {
                double xReal = minX + (pixelX / (double) ancho) * (maxX - minX);
                double yReal = polinomio.evaluar(xReal);
                int pixelY = escalarY(yReal, alto);

                if (primerPunto) {
                    curva.moveTo(pixelX, pixelY);
                    primerPunto = false;
                } else {
                    curva.lineTo(pixelX, pixelY);
                }
            }
            // Efecto brillo sutil dibujando una línea gruesa semi-transparente abajo
            g2d.setColor(new Color(0, 230, 118, 50));
            g2d.setStroke(new BasicStroke(7));
            g2d.draw(curva);

            // Línea central fuerte
            g2d.setColor(colorCurvaNeon);
            g2d.setStroke(new BasicStroke(3));
            g2d.draw(curva);
        }
    }

    private int escalarX(double x, int ancho) {
        return (int) ((x - minX) / (maxX - minX) * ancho);
    }

    private int escalarY(double y, int alto) {
        return alto - (int) ((y - minY) / (maxY - minY) * alto);
    }
}