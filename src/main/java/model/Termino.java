package model;

public class Termino {
    public enum TipoFuncion { POLINOMIO, SIN, COS, TAN, LN, SEC2 }

    private double coeficiente;
    private int exponente;
    private TipoFuncion tipo;
    private double coefInterno;

    public Termino(double coeficiente, int exponente) {
        this(coeficiente, exponente, TipoFuncion.POLINOMIO, 1.0);
    }

    public Termino(double coeficiente, int exponente, TipoFuncion tipo, double coefInterno) {
        this.coeficiente = coeficiente;
        this.exponente = exponente;
        this.tipo = tipo;
        this.coefInterno = coefInterno;
    }

    public double getCoeficiente() { return coeficiente; }
    public int getExponente() { return exponente; }
    public TipoFuncion getTipo() { return tipo; }

    public void sumarCoeficiente(double valor) { this.coeficiente += valor; }

    public boolean esSemejante(Termino otro) {
        return this.exponente == otro.exponente && this.tipo == otro.tipo && this.coefInterno == otro.coefInterno;
    }

    public double evaluar(double val) {
        double basePolinomio = coeficiente * Math.pow(val, exponente);
        switch (tipo) {
            case POLINOMIO: return basePolinomio;
            case SIN: return coeficiente * Math.sin(coefInterno * val);
            case COS: return coeficiente * Math.cos(coefInterno * val);
            case TAN: return coeficiente * Math.tan(coefInterno * val);
            case LN: return (val > 0) ? coeficiente * Math.log(coefInterno * val) : Double.NaN;
            case SEC2: return coeficiente * Math.pow(1.0 / Math.cos(coefInterno * val), 2);
            default: return 0.0;
        }
    }

    public Termino derivar() {
        switch (tipo) {
            case POLINOMIO:
                if (exponente == 0) return new Termino(0, 0);
                return new Termino(coeficiente * exponente, exponente - 1);
            case SIN: return new Termino(coeficiente * coefInterno, 1, TipoFuncion.COS, coefInterno);
            case COS: return new Termino(-coeficiente * coefInterno, 1, TipoFuncion.SIN, coefInterno);
            case TAN: return new Termino(coeficiente * coefInterno, 1, TipoFuncion.SEC2, coefInterno);
            case LN: return new Termino(coeficiente, -1, TipoFuncion.POLINOMIO, 1.0);
            default: return new Termino(0, 0);
        }
    }

    public Termino integrar() {
        switch (tipo) {
            case POLINOMIO: return new Termino(coeficiente / (exponente + 1), exponente + 1);
            case SIN: return new Termino(-coeficiente / coefInterno, 1, TipoFuncion.COS, coefInterno);
            case COS: return new Termino(coeficiente / coefInterno, 1, TipoFuncion.SIN, coefInterno);
            case SEC2: return new Termino(coeficiente / coefInterno, 1, TipoFuncion.TAN, coefInterno);
            case LN: return new Termino(coeficiente, 1, TipoFuncion.LN, coefInterno);
            default: return new Termino(0, 0);
        }
    }

    public String toLaTeX(String var) {
        if (Math.abs(coeficiente) < 1e-9) return "0";

        String txtCoef = (coeficiente == 1 && exponente != 0 && tipo == TipoFuncion.POLINOMIO) ? "" :
                (coeficiente == -1 && exponente != 0 && tipo == TipoFuncion.POLINOMIO) ? "-" :
                        String.format("%.2f", coeficiente).replace(".00", "").replace(",", ".");

        String strCoefInt = (coefInterno == 1.0) ? "" : String.format("%.2f", coefInterno).replace(".00", "").replace(",", ".");

        switch (tipo) {
            case POLINOMIO:
                if (exponente == 0) return txtCoef.isEmpty() ? "1" : txtCoef;
                if (exponente == 1) return txtCoef + var;
                return txtCoef + var + "^{" + exponente + "}";
            case SIN: return txtCoef + "\\sin(" + strCoefInt + var + ")";
            case COS: return txtCoef + "\\cos(" + strCoefInt + var + ")";
            case TAN: return txtCoef + "\\tan(" + strCoefInt + var + ")";
            case LN: return txtCoef + "\\ln(" + strCoefInt + var + ")";
            case SEC2: return txtCoef + "\\sec^2(" + strCoefInt + var + ")";
            default: return "";
        }
    }

    // ==============================================================
    // MÉTODOS DE ANDRÉS (Restaurados y adaptados para la variable)
    // ==============================================================
    public String obtenerNombreReglaDerivada() {
        switch (tipo) {
            case POLINOMIO: return "Regla de la Potencia";
            case SIN: return "Derivada del Seno (Regla de la Cadena)";
            case COS: return "Derivada del Coseno (Regla de la Cadena)";
            case TAN: return "Derivada de la Tangente (Regla de la Cadena)";
            case LN: return "Derivada del Logaritmo Natural";
            default: return "Regla de Derivación Básica";
        }
    }

    public String explicarDerivadaLaTeX(String var) {
        Termino der = this.derivar();
        return "\\frac{d}{d" + var + "}\\left(" + this.toLaTeX(var) + "\\right) = " + der.toLaTeX(var);
    }

    public String obtenerNombreReglaIntegral() {
        switch (tipo) {
            case POLINOMIO: return "Regla de la Potencia para Integrales";
            case SIN: return "Integral del Seno";
            case COS: return "Integral del Coseno";
            case SEC2: return "Integral de la Secante Cuadrada";
            case LN: return "Integral del Logaritmo Natural";
            default: return "Regla de Integración Básica";
        }
    }

    public String explicarIntegralLaTeX(String var) {
        Termino integ = this.integrar();
        return "\\int \\left(" + this.toLaTeX(var) + "\\right) d" + var + " = " + integ.toLaTeX(var);
    }
}