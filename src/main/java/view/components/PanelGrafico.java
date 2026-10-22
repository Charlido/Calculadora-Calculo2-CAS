package view.components;

import model.Polinomio;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

public class PanelGrafico extends JPanel {
    private final Polinomio polinomioF;
    private final Polinomio polinomioG;
    private final Double limA;
    private final Double limB;

    private double minX = -10.0;
    private double maxX = 10.0;
    private double minY = -10.0;
    private double maxY = 10.0;

    // Colores Neón Cyberpunk
    private final Color fondoOscuro = Color.decode("#121021");
    private final Color colorEjes = Color.decode("#6C65B8");
    private final Color colorCuadricula = Color.decode("#221F3B");
    private final Color colorTextoEscala = Color.decode("#9B96C9");
    private final Color colorCurvaF = Color.decode("#00E676"); // Verde Neón
    private final Color colorCurvaG = Color.decode("#FF2A85"); // Magenta Neón
    private final Color colorRellenoArea = new Color(0, 230, 118, 45);
    private final Color colorLineaLimite = new Color(255, 214, 0, 180);

    public PanelGrafico(Polinomio polinomio) {
        this(polinomio, null, null, null);
    }

    public PanelGrafico(Polinomio polinomioF, Polinomio polinomioG, Double limA, Double limB) {
        this.polinomioF = polinomioF;
        this.polinomioG = polinomioG;
        this.limA = limA;
        this.limB = limB;
        setBackground(fondoOscuro);
        calcularLimites();
    }

    private void calcularLimites() {
        if (polinomioF == null) return;

        if (limA != null && limB != null) {
            double minLim = Math.min(limA, limB);
            double maxLim = Math.max(limA, limB);
            double span = Math.max(maxLim - minLim, 4.0);
            this.minX = Math.min(-10.0, minLim - span * 0.25);
            this.maxX = Math.max(10.0, maxLim + span * 0.25);
        }

        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        double paso = (maxX - minX) / 200.0;

        for (double x = minX; x <= maxX; x += paso) {
            double yF = polinomioF.evaluar(x);
            if (Double.isFinite(yF)) {
                if (yF < min) min = yF;
                if (yF > max) max = yF;
            }
            if (polinomioG != null) {
                double yG = polinomioG.evaluar(x);
                if (Double.isFinite(yG)) {
                    if (yG < min) min = yG;
                    if (yG > max) max = yG;
                }
            }
        }

        if (min == Double.MAX_VALUE || max == -Double.MAX_VALUE) {
            min = -10.0;
            max = 10.0;
        }

        // Asegurar que el eje X (y = 0) sea visible o cercano para referencia visual
        min = Math.min(min, 0);
        max = Math.max(max, 0);

        double margen = (max - min) * 0.2;
        if (margen == 0) margen = 5;
        this.minY = min - margen;
        this.maxY = max + margen;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int ancho = getWidth();
        int alto = getHeight();

        // 1. Dibujar Cuadrícula y Escalas Numéricas
        dibujarCuadriculaYEscalas(g2d, ancho, alto);

        // 2. Dibujar Ejes X e Y
        int origenX = escalarX(0, ancho);
        int origenY = escalarY(0, alto);

        g2d.setColor(colorEjes);
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawLine(0, origenY, ancho, origenY); // Eje X
        g2d.drawLine(origenX, 0, origenX, alto);  // Eje Y

        // 3. Sombrear área entre [a, b] si los límites están activos
        if (polinomioF != null && limA != null && limB != null) {
            dibujarAreaSombreada(g2d, ancho, alto);
        }

        // 4. Dibujar la curva secundaria g(x) si existe
        if (polinomioG != null) {
            dibujarCurva(g2d, polinomioG, colorCurvaG, ancho, alto);
        }

        // 5. Dibujar la curva principal f(x)
        if (polinomioF != null) {
            dibujarCurva(g2d, polinomioF, colorCurvaF, ancho, alto);
        }

        // 6. Dibujar Leyenda informativa
        dibujarLeyenda(g2d, ancho);

        g2d.dispose();
    }

    private void dibujarCuadriculaYEscalas(Graphics2D g2d, int ancho, int alto) {
        int origenX = escalarX(0, ancho);
        int origenY = escalarY(0, alto);

        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        FontMetrics fm = g2d.getFontMetrics();

        // Divisiones en X
        int numDivisionesX = 10;
        double pasoX = (maxX - minX) / numDivisionesX;
        for (int i = 0; i <= numDivisionesX; i++) {
            double valX = minX + i * pasoX;
            int px = escalarX(valX, ancho);

            g2d.setColor(colorCuadricula);
            g2d.setStroke(new BasicStroke(1f));
            g2d.drawLine(px, 0, px, alto);

            if (Math.abs(valX) > 1e-6) {
                g2d.setColor(colorEjes);
                g2d.drawLine(px, origenY - 4, px, origenY + 4);

                String lbl = formatearValorEscala(valX);
                int lblW = fm.stringWidth(lbl);
                int labelY = Math.min(Math.max(origenY + 16, 15), alto - 6);
                g2d.setColor(colorTextoEscala);
                g2d.drawString(lbl, px - lblW / 2, labelY);
            }
        }

        // Divisiones en Y
        int numDivisionesY = 8;
        double pasoY = (maxY - minY) / numDivisionesY;
        for (int i = 0; i <= numDivisionesY; i++) {
            double valY = minY + i * pasoY;
            int py = escalarY(valY, alto);

            g2d.setColor(colorCuadricula);
            g2d.setStroke(new BasicStroke(1f));
            g2d.drawLine(0, py, ancho, py);

            if (Math.abs(valY) > 1e-6) {
                g2d.setColor(colorEjes);
                g2d.drawLine(origenX - 4, py, origenX + 4, py);

                String lbl = formatearValorEscala(valY);
                int labelX = Math.min(Math.max(origenX + 8, 6), ancho - fm.stringWidth(lbl) - 6);
                g2d.setColor(colorTextoEscala);
                g2d.drawString(lbl, labelX, py + 4);
            }
        }
    }

    private void dibujarAreaSombreada(Graphics2D g2d, int ancho, int alto) {
        double inicio = Math.min(limA, limB);
        double fin = Math.max(limA, limB);

        int pxInicio = Math.max(0, escalarX(inicio, ancho));
        int pxFin = Math.min(ancho - 1, escalarX(fin, ancho));
        if (pxFin <= pxInicio) return;

        Path2D area = new Path2D.Double();
        double xInicioReal = minX + (pxInicio / (double) ancho) * (maxX - minX);
        double yBaseInicio = (polinomioG != null) ? polinomioG.evaluar(xInicioReal) : 0.0;
        area.moveTo(pxInicio, escalarY(yBaseInicio, alto));

        // Borde superior por f(x)
        for (int px = pxInicio; px <= pxFin; px++) {
            double xReal = minX + (px / (double) ancho) * (maxX - minX);
            double yF = polinomioF.evaluar(xReal);
            area.lineTo(px, escalarY(yF, alto));
        }

        // Borde inferior por g(x) o eje X (y = 0)
        for (int px = pxFin; px >= pxInicio; px--) {
            double xReal = minX + (px / (double) ancho) * (maxX - minX);
            double yBase = (polinomioG != null) ? polinomioG.evaluar(xReal) : 0.0;
            area.lineTo(px, escalarY(yBase, alto));
        }
        area.closePath();

        g2d.setColor(colorRellenoArea);
        g2d.fill(area);

        // Líneas punteadas en los límites a y b
        Stroke dashed = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{6, 6}, 0);
        g2d.setStroke(dashed);
        g2d.setColor(colorLineaLimite);
        g2d.drawLine(pxInicio, 0, pxInicio, alto);
        g2d.drawLine(pxFin, 0, pxFin, alto);
    }

    private void dibujarCurva(Graphics2D g2d, Polinomio poly, Color colorCurva, int ancho, int alto) {
        Path2D curva = new Path2D.Double();
        boolean primerPunto = true;

        for (int pixelX = 0; pixelX < ancho; pixelX++) {
            double xReal = minX + (pixelX / (double) ancho) * (maxX - minX);
            double yReal = poly.evaluar(xReal);
            int pixelY = escalarY(yReal, alto);

            if (primerPunto) {
                curva.moveTo(pixelX, pixelY);
                primerPunto = false;
            } else {
                curva.lineTo(pixelX, pixelY);
            }
        }

        // Efecto brillo neón exterior
        g2d.setColor(new Color(colorCurva.getRed(), colorCurva.getGreen(), colorCurva.getBlue(), 55));
        g2d.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(curva);

        // Línea central nítida
        g2d.setColor(colorCurva);
        g2d.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(curva);
    }

    private void dibujarLeyenda(Graphics2D g2d, int ancho) {
        int boxWidth = 175;
        int boxHeight = 32 + (polinomioG != null ? 20 : 0) + (limA != null && limB != null ? 20 : 0);
        int x = ancho - boxWidth - 15;
        int y = 15;

        g2d.setColor(new Color(24, 21, 46, 215));
        g2d.fillRoundRect(x, y, boxWidth, boxHeight, 12, 12);
        g2d.setColor(Color.decode("#4E498A"));
        g2d.setStroke(new BasicStroke(1f));
        g2d.drawRoundRect(x, y, boxWidth, boxHeight, 12, 12);

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        int textY = y + 20;

        // Item f(x)
        g2d.setColor(colorCurvaF);
        g2d.fillOval(x + 12, textY - 9, 10, 10);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Curva f(x)", x + 30, textY);

        // Item g(x)
        if (polinomioG != null) {
            textY += 20;
            g2d.setColor(colorCurvaG);
            g2d.fillOval(x + 12, textY - 9, 10, 10);
            g2d.setColor(Color.WHITE);
            g2d.drawString("Curva g(x)", x + 30, textY);
        }

        // Item Intervalo [a, b]
        if (limA != null && limB != null) {
            textY += 20;
            g2d.setColor(colorLineaLimite);
            g2d.fillRect(x + 12, textY - 9, 10, 10);
            g2d.setColor(Color.WHITE);
            g2d.drawString(String.format("Área [%.1f, %.1f]", limA, limB), x + 30, textY);
        }
    }

    private String formatearValorEscala(double val) {
        if (Math.abs(val) >= 100 || Math.abs(val % 1) < 0.05) {
            return String.format("%.0f", val);
        }
        return String.format("%.1f", val);
    }

    private int escalarX(double x, int ancho) {
        return (int) ((x - minX) / (maxX - minX) * ancho);
    }

    private int escalarY(double y, int alto) {
        return alto - (int) ((y - minY) / (maxY - minY) * alto);
    }
}
