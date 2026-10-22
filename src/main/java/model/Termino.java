package model;

import java.util.Locale;

public class Termino {

    public enum TipoFuncion {
        POLINOMIO, // c * x^n
        SIN,       // c * sin(k*x)
        COS,       // c * cos(k*x)
        TAN,       // c * tan(k*x)
        SEC2,      // c * sec^2(k*x)
        LN,        // c * ln(k*x)
        LN_ABS,    // c * ln|x|
        LN_COS,    // c * ln|cos(k*x)|
        X_LN_X     // c * (x*ln(k*x) - x)
    }

    private double coeficiente;
    private int exponente;
    private final TipoFuncion tipo;
    private final double coefInterno; // k dentro de sin(kx), cos(kx), etc.

    // Constructor clásico exigido por POO para monomios algebraicos
    public Termino(double coeficiente, int exponente) {
        this(coeficiente, exponente, TipoFuncion.POLINOMIO, 1.0);
    }

    // Constructor extendido para funciones trigonométricas y logarítmicas
    public Termino(double coeficiente, int exponente, TipoFuncion tipo, double coefInterno) {
        this.coeficiente = coeficiente;
        this.exponente = exponente;
        this.tipo = tipo;
        this.coefInterno = (coefInterno == 0) ? 1.0 : coefInterno;
    }

    public boolean esSemejante(Termino otro) {
        if (this.tipo != otro.tipo) return false;
        if (this.tipo == TipoFuncion.POLINOMIO) {
            return this.exponente == otro.exponente;
        }
        return this.exponente == otro.exponente && Math.abs(this.coefInterno - otro.coefInterno) < 1e-9;
    }

    public void sumarCoeficiente(double delta) {
        this.coeficiente += delta;
    }

    // Reglas de Derivación simbólica (Polinomios, Trigonométricas y Logaritmo Natural)
    public Termino derivar() {
        switch (tipo) {
            case POLINOMIO:
                if (exponente == 0) return new Termino(0, 0);
                return new Termino(coeficiente * exponente, exponente - 1);

            case SIN:
                // d/dx [c * sin(kx)] = (c * k) * cos(kx)
                return new Termino(coeficiente * coefInterno, 1, TipoFuncion.COS, coefInterno);

            case COS:
                // d/dx [c * cos(kx)] = -(c * k) * sin(kx)
                return new Termino(-coeficiente * coefInterno, 1, TipoFuncion.SIN, coefInterno);

            case TAN:
                // d/dx [c * tan(kx)] = (c * k) * sec^2(kx)
                return new Termino(coeficiente * coefInterno, 1, TipoFuncion.SEC2, coefInterno);

            case LN:
            case LN_ABS:
                // d/dx [c * ln(kx)] = c / x = c * x^-1
                return new Termino(coeficiente, -1, TipoFuncion.POLINOMIO, 1.0);

            case X_LN_X:
                // d/dx [c * (x ln(kx) - x)] = c * ln(kx)
                return new Termino(coeficiente, 1, TipoFuncion.LN, coefInterno);

            default:
                return new Termino(0, 0);
        }
    }

    // Reglas de Integración simbólica
    public Termino integrar() {
        switch (tipo) {
            case POLINOMIO:
                if (exponente == -1) {
                    // Integral de c/x dx = c * ln|x|
                    return new Termino(coeficiente, 1, TipoFuncion.LN_ABS, 1.0);
                }
                double nuevoCoef = coeficiente / (exponente + 1.0);
                return new Termino(nuevoCoef, exponente + 1);

            case SIN:
                // Integral de c * sin(kx) dx = -(c / k) * cos(kx)
                return new Termino(-coeficiente / coefInterno, 1, TipoFuncion.COS, coefInterno);

            case COS:
                // Integral de c * cos(kx) dx = (c / k) * sin(kx)
                return new Termino(coeficiente / coefInterno, 1, TipoFuncion.SIN, coefInterno);

            case TAN:
                // Integral de c * tan(kx) dx = -(c / k) * ln|cos(kx)|
                return new Termino(-coeficiente / coefInterno, 1, TipoFuncion.LN_COS, coefInterno);

            case SEC2:
                // Integral de c * sec^2(kx) dx = (c / k) * tan(kx)
                return new Termino(coeficiente / coefInterno, 1, TipoFuncion.TAN, coefInterno);

            case LN:
                // Integral de c * ln(kx) dx = c * (x * ln(kx) - x)
                return new Termino(coeficiente, 1, TipoFuncion.X_LN_X, coefInterno);

            default:
                return new Termino(0, 0);
        }
    }

    // Evaluación numérica en un punto (para integrales definidas y graficador)
    public double evaluar(double valorX) {
        switch (tipo) {
            case POLINOMIO:
                return coeficiente * Math.pow(valorX, exponente);
            case SIN:
                return coeficiente * Math.sin(coefInterno * valorX);
            case COS:
                return coeficiente * Math.cos(coefInterno * valorX);
            case TAN:
                return coeficiente * Math.tan(coefInterno * valorX);
            case SEC2:
                double c = Math.cos(coefInterno * valorX);
                return (Math.abs(c) < 1e-9) ? Double.NaN : coeficiente / (c * c);
            case LN:
                return (coefInterno * valorX > 0) ? coeficiente * Math.log(coefInterno * valorX) : Double.NaN;
            case LN_ABS:
                return (Math.abs(coefInterno * valorX) > 1e-12) ? coeficiente * Math.log(Math.abs(coefInterno * valorX)) : Double.NaN;
            case LN_COS:
                double cosVal = Math.abs(Math.cos(coefInterno * valorX));
                return (cosVal > 1e-12) ? coeficiente * Math.log(cosVal) : Double.NaN;
            case X_LN_X:
                return (coefInterno * valorX > 0) ? coeficiente * (valorX * Math.log(coefInterno * valorX) - valorX) : Double.NaN;
            default:
                return 0;
        }
    }

    public String toLaTeX() {
        return toLaTeX("x");
    }

    // Genera la sintaxis visual para que JLaTeXMath lo dibuje con la variable elegida (x o y)
    public String toLaTeX(String var) {
        if (Math.abs(coeficiente) < 1e-9) return "";

        String coefStr = formatearNumero(coeficiente);
        String prefijoCoef = coefStr.equals("1") ? "" : (coefStr.equals("-1") ? "-" : coefStr);
        String argInterno = formatearArgumentoInterno(var);

        switch (tipo) {
            case POLINOMIO:
                if (exponente == 0) return coefStr;
                if (exponente == 1) return prefijoCoef + var;
                return prefijoCoef + var + "^{" + exponente + "}";

            case SIN:
                return prefijoCoef + "\\sin(" + argInterno + ")";

            case COS:
                return prefijoCoef + "\\cos(" + argInterno + ")";

            case TAN:
                return prefijoCoef + "\\tan(" + argInterno + ")";

            case SEC2:
                return prefijoCoef + "\\sec^{2}(" + argInterno + ")";

            case LN:
                return prefijoCoef + "\\ln(" + argInterno + ")";

            case LN_ABS:
                return prefijoCoef + "\\ln|" + argInterno + "|";

            case LN_COS:
                return prefijoCoef + "\\ln|\\cos(" + argInterno + ")|";

            case X_LN_X:
                String factor = coefStr.equals("1") ? "" : (coefStr.equals("-1") ? "-" : coefStr);
                if (factor.isEmpty()) {
                    return var + "\\ln(" + argInterno + ") - " + var;
                }
                return factor + "\\left(" + var + "\\ln(" + argInterno + ") - " + var + "\\right)";

            default:
                return "";
        }
    }

    // ==============================================================
    // MÉTODOS PEDAGÓGICOS PARA EXPLICACIÓN PASO A PASO (CÁLCULO)
    // ==============================================================
    public String obtenerNombreReglaDerivada() {
        switch (tipo) {
            case POLINOMIO:
                if (exponente == 0) return "Regla de la Constante: d/dx [c] = 0";
                if (exponente == 1) return "Regla de la Identidad Lineal: d/dx [c·x] = c";
                return "Regla de la Potencia: d/dx [c·x^n] = c · n · x^(n-1)";
            case SIN:
                return "Derivada del Seno + Regla de la Cadena: d/dx [c·sin(kx)] = c·k·cos(kx)";
            case COS:
                return "Derivada del Coseno + Regla de la Cadena: d/dx [c·cos(kx)] = -c·k·sin(kx)";
            case TAN:
                return "Derivada de la Tangente + Regla de la Cadena: d/dx [c·tan(kx)] = c·k·sec²(kx)";
            case LN:
            case LN_ABS:
                return "Derivada del Logaritmo Natural: d/dx [c·ln(kx)] = c / x = c·x^(-1)";
            default:
                return "Regla de Derivación Directa";
        }
    }

    public String explicarDerivadaLaTeX(String var) {
        String original = toLaTeX(var);
        Termino derivado = derivar();
        String res = derivado.toLaTeX(var);
        if (res.isEmpty()) res = "0";

        if (tipo == TipoFuncion.POLINOMIO && exponente != 0 && exponente != 1) {
            String coefStr = formatearNumero(coeficiente);
            return "\\frac{d}{d" + var + "}\\left[" + original + "\\right] = (" + coefStr + ")(" + exponente + ")" + var + "^{" + exponente + "-1} = " + res;
        }
        if ((tipo == TipoFuncion.SIN || tipo == TipoFuncion.COS || tipo == TipoFuncion.TAN) && Math.abs(coefInterno - 1.0) > 1e-9) {
            String kStr = formatearNumero(coefInterno);
            return "\\frac{d}{d" + var + "}\\left[" + original + "\\right] = (" + formatearNumero(coeficiente) + ")(" + kStr + ") \\implies " + res;
        }
        return "\\frac{d}{d" + var + "}\\left[" + original + "\\right] = " + res;
    }

    public String obtenerNombreReglaIntegral() {
        switch (tipo) {
            case POLINOMIO:
                if (exponente == 0) return "Integral de una Constante: ∫ c dx = c·x";
                if (exponente == -1) return "Integral Logarítmica (n = -1): ∫ (c/x) dx = c·ln|x|";
                return "Regla de la Potencia Inversa: ∫ c·x^n dx = [c / (n+1)] · x^(n+1)";
            case SIN:
                return "Integral del Seno (Sustitución u = kx): ∫ c·sin(kx) dx = -(c/k)·cos(kx)";
            case COS:
                return "Integral del Coseno (Sustitución u = kx): ∫ c·cos(kx) dx = (c/k)·sin(kx)";
            case TAN:
                return "Integral de la Tangente: ∫ c·tan(kx) dx = -(c/k)·ln|cos(kx)|";
            case LN:
                return "Integración por Partes (u = ln(kx), dv = dx): ∫ c·ln(kx) dx = c(x·ln(kx) - x)";
            default:
                return "Regla de Integración Directa";
        }
    }

    public String explicarIntegralLaTeX(String var) {
        String original = toLaTeX(var);
        Termino integrado = integrar();
        String res = integrado.toLaTeX(var);
        if (res.isEmpty()) res = "0";

        if (tipo == TipoFuncion.POLINOMIO && exponente != -1 && exponente != 0) {
            String coefStr = formatearNumero(coeficiente);
            return "\\int " + original + "\\,d" + var + " = \\frac{" + coefStr + "}{" + exponente + "+1}" + var + "^{" + exponente + "+1} = " + res;
        }
        return "\\int " + original + "\\,d" + var + " = " + res;
    }

    private String formatearArgumentoInterno(String var) {
        String kStr = formatearNumero(coefInterno);
        if (kStr.equals("1")) return var;
        if (kStr.equals("-1")) return "-" + var;
        return kStr + var;
    }

    private String formatearNumero(double num) {
        if (Math.abs(num - Math.round(num)) < 1e-7) {
            return String.format(Locale.US, "%.0f", (double) Math.round(num));
        }
        return String.format(Locale.US, "%.2f", num);
    }

    public double getCoeficiente() {
        return coeficiente;
    }

    public int getExponente() {
        return exponente;
    }

    public TipoFuncion getTipo() {
        return tipo;
    }
}
