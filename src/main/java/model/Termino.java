package model;

public class Termino {
    private double coeficiente;
    private int exponente;

    // Constructor exigido por POO
    public Termino(double coeficiente, int exponente) {
        this.coeficiente = coeficiente;
        this.exponente = exponente;
    }

    // Regla de Derivación: exponente * coeficiente, y al exponente se le resta 1
    public Termino derivar() {
        if (exponente == 0) return new Termino(0, 0); // La derivada de una constante es 0
        return new Termino(coeficiente * exponente, exponente - 1);
    }

    // Regla de Integración: coeficiente / (exponente + 1), y al exponente se le suma 1
    public Termino integrar() {
        double nuevoCoeficiente = coeficiente / (exponente + 1.0);
        return new Termino(nuevoCoeficiente, exponente + 1);
    }

    // Método para evaluar la función en un número (para integrales definidas)
    public double evaluar(double valorX) {
        return coeficiente * Math.pow(valorX, exponente);
    }

    // Genera la sintaxis visual para que JLaTeXMath lo dibuje bonito
    public String toLaTeX() {
        if (coeficiente == 0) return "";

        // Quita los decimales innecesarios (ej. 3.0 -> 3)
        String coefStr = (coeficiente % 1 == 0) ? String.format("%.0f", coeficiente) : String.format("%.2f", coeficiente);

        if (exponente == 0) return coefStr;
        if (exponente == 1) return coefStr.equals("1") ? "x" : (coefStr.equals("-1") ? "-x" : coefStr + "x");

        String base = coefStr.equals("1") ? "x" : (coefStr.equals("-1") ? "-x" : coefStr + "x");
        return base + "^{" + exponente + "}";
    }

    public double getCoeficiente() { return coeficiente; }
}