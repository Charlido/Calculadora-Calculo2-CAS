package model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Polinomio {
    private List<Termino> terminos;

    public Polinomio() {
        this.terminos = new ArrayList<>();
    }

    // MÉTODO DE ANDRÉS RESTAURADO
    public List<Termino> getTerminos() {
        return terminos;
    }

    public void agregarTermino(Termino t) {
        if (t == null || Math.abs(t.getCoeficiente()) < 1e-9) return;

        for (int i = 0; i < terminos.size(); i++) {
            Termino existente = terminos.get(i);
            if (existente.esSemejante(t)) {
                existente.sumarCoeficiente(t.getCoeficiente());
                if (Math.abs(existente.getCoeficiente()) < 1e-9) {
                    terminos.remove(i);
                }
                return;
            }
        }
        terminos.add(t);
    }

    public double evaluar(double val) {
        double suma = 0;
        for (Termino t : terminos) suma += t.evaluar(val);
        return suma;
    }

    public Polinomio derivar() {
        Polinomio resultado = new Polinomio();
        for (Termino t : terminos) resultado.agregarTermino(t.derivar());
        return resultado;
    }

    public Polinomio integrar() {
        Polinomio resultado = new Polinomio();
        for (Termino t : terminos) resultado.agregarTermino(t.integrar());
        return resultado;
    }

    public String toLaTeX(String var) {
        if (terminos.isEmpty()) return "0";
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < terminos.size(); i++) {
            Termino t = terminos.get(i);
            String termLaTeX = t.toLaTeX(var);

            if (i > 0 && t.getCoeficiente() > 0) {
                sb.append(" + ");
            } else if (t.getCoeficiente() < 0 && i > 0) {
                termLaTeX = termLaTeX.replaceFirst("-", "- ");
            }
            sb.append(termLaTeX);
        }
        return sb.toString();
    }

    public static Polinomio parse(String expr, String varSeleccionada) throws IllegalArgumentException {
        if (expr == null || expr.trim().isEmpty()) {
            throw new IllegalArgumentException("La función no puede estar vacía.");
        }

        expr = expr.toLowerCase().replace(" ", "");

        // VALIDACIÓN DE CONSISTENCIA (Saúl)
        String varContraria = varSeleccionada.equals("x") ? "y" : "x";
        if (expr.contains(varContraria)) {
            throw new IllegalArgumentException("Inconsistencia: Seleccionaste 'd" + varSeleccionada + "' pero ingresaste la variable '" + varContraria + "'.");
        }

        Polinomio p = new Polinomio();
        String regex = "([+-]?(?:(?:\\d*\\.?\\d*)" + varSeleccionada + "\\^\\d+|\\d*\\.?\\d*" + varSeleccionada + "|\\d+\\.?\\d*|(?:\\d*\\.?\\d*)?(?:sin|cos|tan|ln)\\(" + varSeleccionada + "\\)))";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(expr.replace("-", "+-"));

        while (matcher.find()) {
            String token = matcher.group(1).replace("+-", "-");
            p.agregarTermino(parsearTermino(token, varSeleccionada));
        }

        if (p.terminos.isEmpty()) {
            throw new IllegalArgumentException("Error de sintaxis matemática. Ejemplos válidos: 3" + varSeleccionada + "^2 - 5" + varSeleccionada + " + 2");
        }

        return p;
    }

    private static Termino parsearTermino(String token, String var) {
        double coef = 1.0;
        int exp = 0;
        Termino.TipoFuncion tipo = Termino.TipoFuncion.POLINOMIO;

        if (token.contains("sin") || token.contains("cos") || token.contains("tan") || token.contains("ln")) {
            if (token.contains("sin")) tipo = Termino.TipoFuncion.SIN;
            else if (token.contains("cos")) tipo = Termino.TipoFuncion.COS;
            else if (token.contains("tan")) tipo = Termino.TipoFuncion.TAN;
            else if (token.contains("ln")) tipo = Termino.TipoFuncion.LN;

            String strCoef = token.substring(0, token.indexOf(tipo.name().toLowerCase()));
            if (strCoef.equals("-")) coef = -1.0;
            else if (!strCoef.isEmpty() && !strCoef.equals("+")) coef = Double.parseDouble(strCoef);
            return new Termino(coef, 1, tipo, 1.0);
        }

        if (token.contains(var)) {
            String[] partes = token.split(var + "\\^?");
            if (partes.length > 0 && !partes[0].isEmpty() && !partes[0].equals("+")) {
                if (partes[0].equals("-")) coef = -1.0;
                else coef = Double.parseDouble(partes[0]);
            }
            exp = (token.contains("^") && partes.length > 1) ? Integer.parseInt(partes[1]) : 1;
        } else {
            coef = Double.parseDouble(token);
        }

        return new Termino(coef, exp);
    }
}