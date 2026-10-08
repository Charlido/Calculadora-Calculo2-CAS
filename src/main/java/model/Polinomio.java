package model;

import java.util.ArrayList;
import java.util.List;

public class Polinomio {
    private List<Termino> terminos;

    public Polinomio() {
        this.terminos = new ArrayList<>();
    }

    public void agregarTermino(Termino t) {
        if (t.getCoeficiente() != 0) {
            terminos.add(t);
        }
    }

    public Polinomio derivar() {
        Polinomio resultado = new Polinomio();
        for (Termino t : terminos) {
            resultado.agregarTermino(t.derivar());
        }
        return resultado;
    }

    public Polinomio integrar() {
        Polinomio resultado = new Polinomio();
        for (Termino t : terminos) {
            resultado.agregarTermino(t.integrar());
        }
        return resultado;
    }

    public double evaluar(double x) {
        double suma = 0;
        for (Termino t : terminos) {
            suma += t.evaluar(x);
        }
        return suma;
    }

    public String toLaTeX() {
        if (terminos.isEmpty()) return "0";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < terminos.size(); i++) {
            Termino t = terminos.get(i);
            String termLaTeX = t.toLaTeX();
            if (i > 0 && t.getCoeficiente() > 0) {
                sb.append(" + ");
            } else if (t.getCoeficiente() < 0) {
                termLaTeX = termLaTeX.replace("-", "- ");
            }
            sb.append(termLaTeX);
        }
        return sb.toString();
    }

    // ==============================================================
    // PARSER: Convierte texto como "3x^2 - 5x + 2" a Objetos Termino
    // ==============================================================
    public static Polinomio parse(String expr) {
        Polinomio p = new Polinomio();
        // Limpiamos la entrada (quitamos espacios y asteriscos para simplificar)
        expr = expr.replace(" ", "").replace("*", "").toLowerCase();

        // Truco para separar los términos usando el signo "+"
        expr = expr.replace("-", "+-");
        if (expr.startsWith("+-")) expr = expr.substring(1);

        String[] fragmentos = expr.split("\\+");

        for (String frag : fragmentos) {
            if (frag.isEmpty()) continue;
            double coef = 1;
            int exp = 0;

            if (frag.contains("x")) {
                String[] partes = frag.split("x");
                // Determinar el coeficiente
                if (partes.length == 0 || partes[0].isEmpty()) coef = 1;
                else if (partes[0].equals("-")) coef = -1;
                else coef = Double.parseDouble(partes[0]);

                // Determinar el exponente
                if (frag.contains("^")) {
                    exp = Integer.parseInt(frag.substring(frag.indexOf("^") + 1));
                } else {
                    exp = 1; // Si tiene x pero no ^, el exponente es 1 (ej. 5x)
                }
            } else {
                // Si no tiene x, es una constante (ej. 2)
                coef = Double.parseDouble(frag);
                exp = 0;
            }
            p.agregarTermino(new Termino(coef, exp));
        }
        return p;
    }
}