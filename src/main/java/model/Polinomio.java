package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Polinomio {
    private final List<Termino> terminos;

    public Polinomio() {
        this.terminos = new ArrayList<>();
    }

    public List<Termino> getTerminos() {
        return Collections.unmodifiableList(terminos);
    }

    public void agregarTermino(Termino t) {
        if (t == null || Math.abs(t.getCoeficiente()) < 1e-9) {
            return;
        }
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
        return toLaTeX("x");
    }

    public String toLaTeX(String var) {
        if (terminos.isEmpty()) return "0";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < terminos.size(); i++) {
            Termino t = terminos.get(i);
            String termLaTeX = t.toLaTeX(var);
            if (termLaTeX.isEmpty()) continue;

            if (sb.length() > 0) {
                if (t.getCoeficiente() > 0 && !termLaTeX.startsWith("-")) {
                    sb.append(" + ").append(termLaTeX);
                } else {
                    String limpio = termLaTeX.startsWith("-") ? termLaTeX.substring(1).trim() : termLaTeX;
                    sb.append(" - ").append(limpio);
                }
            } else {
                sb.append(termLaTeX);
            }
        }
        return sb.length() == 0 ? "0" : sb.toString();
    }

    public static Polinomio parse(String expr, String varSeleccionada) throws IllegalArgumentException {
        if (expr == null || expr.trim().isEmpty()) {
            throw new IllegalArgumentException("La función no puede estar vacía.");
        }

        String limpia = expr.replace(" ", "").replace("*", "").toLowerCase();

        // VALIDACIÓN SEGURA DE SAÚL (Remueve funciones primero para evitar falsos positivos con dx/dy)
        String varContraria = varSeleccionada.equals("x") ? "y" : "x";
        String textoSinFunciones = limpia.replaceAll("(sin|cos|tan|ln)", "");
        if (textoSinFunciones.contains(varContraria)) {
            throw new IllegalArgumentException("Inconsistencia: Seleccionaste 'd" + varSeleccionada + "' pero ingresaste la variable '" + varContraria + "'.");
        }

        // BALANCEO AUTOMÁTICO DE PARÉNTESIS
        int abiertos = 0;
        for (char c : limpia.toCharArray()) {
            if (c == '(') abiertos++;
            else if (c == ')') abiertos = Math.max(0, abiertos - 1);
        }
        while (abiertos > 0) {
            limpia += ")";
            abiertos--;
        }

        Polinomio p = new Polinomio();
        try {
            List<String> fragmentos = separarTerminosNivelSuperior(limpia);
            if (fragmentos.isEmpty()) {
                throw new IllegalArgumentException("Expresión inválida.");
            }

            for (String frag : fragmentos) {
                if (frag.isEmpty() || frag.equals("+") || frag.equals("-")) {
                    throw new IllegalArgumentException("Término incompleto en la función.");
                }
                p.agregarTermino(parsearTerminoIndividual(frag, varSeleccionada));
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (Exception e) {
            throw new IllegalArgumentException("Error de sintaxis matemática. Ejemplos válidos: 3" + varSeleccionada + "^2, sin(" + varSeleccionada + "), 1/" + varSeleccionada);
        }

        return p;
    }

    private static List<String> separarTerminosNivelSuperior(String expr) {
        List<String> lista = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        int nivelParentesis = 0;

        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (c == '(') {
                nivelParentesis++;
                actual.append(c);
            } else if (c == ')') {
                nivelParentesis = Math.max(0, nivelParentesis - 1);
                actual.append(c);
            } else if ((c == '+' || c == '-') && nivelParentesis == 0 && i > 0 && expr.charAt(i - 1) != '^') {
                if (actual.length() > 0) {
                    lista.add(actual.toString());
                    actual.setLength(0);
                }
                if (c == '-') {
                    actual.append('-');
                }
            } else {
                if (c != '+' || nivelParentesis > 0 || actual.length() > 0) {
                    if (c == '+' && actual.length() == 0) continue;
                    actual.append(c);
                }
            }
        }
        if (actual.length() > 0) {
            lista.add(actual.toString());
        }
        return lista;
    }

    private static Termino parsearTerminoIndividual(String frag, String var) {
        // 1. Verificar si es una función trigonométrica o logarítmica dinámicamente
        String regex = "^([+-]?(?:\\d+(?:\\.\\d+)?)?)?(sin|cos|tan|ln)\\(([+-]?(?:\\d+(?:\\.\\d+)?)?)?" + var + "\\)$";
        Matcher mFunc = Pattern.compile(regex).matcher(frag);

        if (mFunc.matches()) {
            double coef = parsearCoeficientePrefijo(mFunc.group(1));
            String nombreFunc = mFunc.group(2);
            double coefInterno = parsearCoeficientePrefijo(mFunc.group(3));

            Termino.TipoFuncion tipo;
            switch (nombreFunc) {
                case "sin": tipo = Termino.TipoFuncion.SIN; break;
                case "cos": tipo = Termino.TipoFuncion.COS; break;
                case "tan": tipo = Termino.TipoFuncion.TAN; break;
                case "ln":
                    if (coefInterno <= 0) throw new IllegalArgumentException("El argumento de ln debe ser positivo.");
                    tipo = Termino.TipoFuncion.LN;
                    break;
                default: throw new IllegalArgumentException("Función desconocida: " + nombreFunc);
            }
            return new Termino(coef, 1, tipo, coefInterno);
        }

        // 2. Soporte para fracciones simples del tipo c/var o c/var^n (ej: 1/x, 3/x^2)
        if (frag.contains("/")) {
            String[] divPartes = frag.split("/");
            if (divPartes.length != 2) throw new IllegalArgumentException("División inválida.");

            double numerador = Double.parseDouble(divPartes[0]);
            String denom = divPartes[1];

            if (denom.equals(var)) {
                return new Termino(numerador, -1);
            } else if (denom.matches(var + "\\^[+-]?\\d+")) {
                int expDenom = Integer.parseInt(denom.substring(denom.indexOf('^') + 1));
                return new Termino(numerador, -expDenom);
            } else {
                double valDenom = Double.parseDouble(denom);
                if (Math.abs(valDenom) < 1e-12) throw new IllegalArgumentException("División por cero.");
                return new Termino(numerador / valDenom, 0);
            }
        }

        // 3. Monomio algebraico clásico
        if (frag.contains(var)) {
            int idxVar = frag.indexOf(var);
            String parteCoef = frag.substring(0, idxVar);
            String parteResto = frag.substring(idxVar + 1);

            double coef = parsearCoeficientePrefijo(parteCoef);
            int exp = 1;

            if (!parteResto.isEmpty()) {
                if (!parteResto.startsWith("^")) throw new IllegalArgumentException("Formato de exponente inválido.");
                exp = Integer.parseInt(parteResto.substring(1));
            }
            return new Termino(coef, exp);
        } else {
            double coef = Double.parseDouble(frag);
            return new Termino(coef, 0);
        }
    }

    private static double parsearCoeficientePrefijo(String texto) {
        if (texto == null || texto.isEmpty() || texto.equals("+")) return 1.0;
        if (texto.equals("-")) return -1.0;
        return Double.parseDouble(texto);
    }
}