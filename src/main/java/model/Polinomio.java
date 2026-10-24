package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Polinomio {
    private final List<Termino> terminos;

    // Expresiones regulares de validación de entrada (Opciones 1 y 2)
    private static final Pattern REGEX_FUNCIONES_VALIDAS = Pattern.compile("(sin|cos|tan|ln)(?=\\()");
    private static final Pattern REGEX_LETRAS_NO_RECONOCIDAS = Pattern.compile(".*[a-wz].*");
    private static final Pattern REGEX_CARACTERES_PERMITIDOS = Pattern.compile("^[0-9xy+\\-*/^().]+$");
    private static final Pattern REGEX_PARENTESIS_VACIOS = Pattern.compile(".*\\(\\).*");
    private static final Pattern REGEX_OPERADORES_INVALIDOS = Pattern.compile(".*(\\+\\+|--|\\+-|-\\+|\\*\\*|//|\\^\\^|\\^[*/]|[+\\-*/]\\^).*");
    private static final Pattern REGEX_INICIO_FIN_OPERADOR = Pattern.compile("(^[*/^].*|.*[+\\-*/^]$)");
    private static final Pattern REGEX_DECIMAL_INVALIDO = Pattern.compile(".*(\\d+\\.\\d+\\.\\d+|\\.\\.|(^|[^\\d])\\.(?!\\d)).*");

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

    public static Polinomio parse(String expr) throws IllegalArgumentException {
        return parse(expr, "x");
    }

    public static Polinomio parse(String expr, String varSeleccionada) throws IllegalArgumentException {
        if (expr == null || expr.trim().isEmpty()) {
            throw new IllegalArgumentException("La función no puede estar vacía.");
        }

        String var = (varSeleccionada == null || varSeleccionada.trim().isEmpty())
                ? "x"
                : varSeleccionada.trim().toLowerCase();

        String preLimpia = expr.replace(" ", "")
                .replace("²", "^2")
                .replace("³", "^3")
                .toLowerCase();

        // VALIDACIONES CON EXPRESIONES REGULARES (Opciones 1 y 2)
        validarSintaxisConRegex(preLimpia);

        String limpia = preLimpia.replace("*", "");

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

        // Simplificar paréntesis simples alrededor de una variable con potencia: (x)^2 -> x^2
        limpia = limpia.replaceAll("\\(([xy])\\)\\^([+-]?\\d+)", "$1^$2");

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
                if (agregarGrupoParentesisSiAplica(p, frag, var)) {
                    continue;
                }
                p.agregarTermino(parsearTerminoIndividual(frag, var));
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (Exception e) {
            throw new IllegalArgumentException("Error de sintaxis matemática. Ejemplos válidos: 3" + var + "^2, sin(" + var + "), 1/" + var);
        }

        return p;
    }

    private static void validarSintaxisConRegex(String expr) {
        // 1. Validar variables o funciones desconocidas (Opción 1)
        String sinFunciones = REGEX_FUNCIONES_VALIDAS.matcher(expr).replaceAll("");
        if (REGEX_LETRAS_NO_RECONOCIDAS.matcher(sinFunciones).matches()) {
            throw new IllegalArgumentException(
                    "Variable o función no reconocida. Solo se permiten 'x', 'y' y las funciones sin(, cos(, tan(, ln(."
            );
        }
        if (!REGEX_CARACTERES_PERMITIDOS.matcher(sinFunciones).matches()) {
            throw new IllegalArgumentException("La expresión contiene caracteres o símbolos no permitidos.");
        }

        // 2. Validar errores de sintaxis en paréntesis, operadores y decimales (Opción 2)
        if (REGEX_PARENTESIS_VACIOS.matcher(expr).matches()) {
            throw new IllegalArgumentException("Error de sintaxis: No se permiten paréntesis vacíos '()'.");
        }
        if (REGEX_OPERADORES_INVALIDOS.matcher(expr).matches()) {
            throw new IllegalArgumentException("Error de sintaxis: Operadores consecutivos o mal combinados.");
        }
        if (REGEX_INICIO_FIN_OPERADOR.matcher(expr).matches()) {
            throw new IllegalArgumentException("Error de sintaxis: Expresión incompleta al inicio o al final.");
        }
        if (REGEX_DECIMAL_INVALIDO.matcher(expr).matches()) {
            throw new IllegalArgumentException("Error de sintaxis: Formato de número decimal inválido.");
        }
    }

    private static boolean agregarGrupoParentesisSiAplica(Polinomio destino, String frag, String var) {
        int idxOpen = frag.indexOf('(');
        if (idxOpen < 0 || !frag.endsWith(")")) {
            return false;
        }

        String prefijo = frag.substring(0, idxOpen);
        // Si el prefijo termina en una función (sin, cos, tan, ln), no es un grupo algebraico simple
        if (prefijo.endsWith("sin") || prefijo.endsWith("cos") || prefijo.endsWith("tan") || prefijo.endsWith("ln")) {
            return false;
        }
        if (!prefijo.matches("^[+-]?(?:\\d+(?:\\.\\d+)?)?$")) {
            return false;
        }

        // Verificar que el '(' en idxOpen cierre exactamente al final del fragmento
        int nivel = 0;
        for (int i = idxOpen; i < frag.length(); i++) {
            char c = frag.charAt(i);
            if (c == '(') nivel++;
            else if (c == ')') {
                nivel--;
                if (nivel == 0 && i < frag.length() - 1) {
                    return false;
                }
            }
        }
        if (nivel != 0) {
            return false;
        }

        double factor = parsearCoeficientePrefijo(prefijo);
        String interior = frag.substring(idxOpen + 1, frag.length() - 1);
        Polinomio subPolinomio = parse(interior, var);
        for (Termino t : subPolinomio.getTerminos()) {
            destino.agregarTermino(new Termino(
                    t.getCoeficiente() * factor,
                    t.getExponente(),
                    t.getTipo(),
                    1.0,
                    t.getConstanteSimbolica()
            ));
        }
        return true;
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
        String varContraria = var.equals("x") ? "y" : "x";

        // 1. Verificar si es una función trigonométrica o logarítmica (en var o en varContraria)
        String regexFunc = "^([+-]?(?:\\d+(?:\\.\\d+)?)?)?(sin|cos|tan|ln)\\(([+-]?(?:\\d+(?:\\.\\d+)?)?)?([xy])\\)$";
        Matcher mFunc = Pattern.compile(regexFunc).matcher(frag);

        if (mFunc.matches()) {
            double coef = parsearCoeficientePrefijo(mFunc.group(1));
            String nombreFunc = mFunc.group(2);
            double coefInterno = parsearCoeficientePrefijo(mFunc.group(3));
            String varInterna = mFunc.group(4);

            if (nombreFunc.equals("ln") && coefInterno <= 0) {
                throw new IllegalArgumentException("El argumento de ln debe ser positivo.");
            }

            // Si la función depende de la otra variable, es una constante simbólica respecto al diferencial seleccionado
            if (varInterna.equals(varContraria)) {
                String kStr = formatearCoefInternoSimbolico(coefInterno);
                String funcConstante = "\\" + nombreFunc + "(" + kStr + varContraria + ")";
                return new Termino(coef, 0, Termino.TipoFuncion.POLINOMIO, 1.0, funcConstante);
            }

            Termino.TipoFuncion tipo;
            switch (nombreFunc) {
                case "sin": tipo = Termino.TipoFuncion.SIN; break;
                case "cos": tipo = Termino.TipoFuncion.COS; break;
                case "tan": tipo = Termino.TipoFuncion.TAN; break;
                case "ln": tipo = Termino.TipoFuncion.LN; break;
                default: throw new IllegalArgumentException("Función desconocida: " + nombreFunc);
            }
            return new Termino(coef, 1, tipo, coefInterno);
        }

        // 2. Soporte para fracciones simples del tipo c/var, c/var^n o con la variable constante
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
            } else if (denom.equals(varContraria)) {
                return new Termino(numerador, 0, Termino.TipoFuncion.POLINOMIO, 1.0, varContraria + "^{-1}");
            } else if (denom.matches(varContraria + "\\^[+-]?\\d+")) {
                int expDenom = Integer.parseInt(denom.substring(denom.indexOf('^') + 1));
                return new Termino(numerador, 0, Termino.TipoFuncion.POLINOMIO, 1.0, varContraria + "^{" + (-expDenom) + "}");
            } else {
                double valDenom = Double.parseDouble(denom);
                if (Math.abs(valDenom) < 1e-12) throw new IllegalArgumentException("División por cero.");
                return new Termino(numerador / valDenom, 0);
            }
        }

        // 3. Monomio algebraico (soporta variable activa, variable constante o combinación como 3xy)
        String regexMonomio = "^([+-]?(?:\\d+(?:\\.\\d+)?)?)(?:([xy])(?:\\^([+-]?\\d+))?)?(?:([xy])(?:\\^([+-]?\\d+))?)?$";
        Matcher mMono = Pattern.compile(regexMonomio).matcher(frag);
        if (!mMono.matches()) {
            throw new IllegalArgumentException("Formato de término inválido.");
        }

        String coefTexto = mMono.group(1);
        String var1 = mMono.group(2);
        String exp1Texto = mMono.group(3);
        String var2 = mMono.group(4);
        String exp2Texto = mMono.group(5);

        if ((coefTexto == null || coefTexto.isEmpty() || coefTexto.equals("+") || coefTexto.equals("-"))
                && var1 == null && var2 == null) {
            throw new IllegalArgumentException("Término incompleto en la función.");
        }

        double coef = parsearCoeficientePrefijo(coefTexto);
        int expVarActiva = 0;
        int expVarConstante = 0;

        if (var1 != null) {
            int e1 = (exp1Texto != null) ? Integer.parseInt(exp1Texto) : 1;
            if (var1.equals(var)) expVarActiva += e1;
            else expVarConstante += e1;
        }
        if (var2 != null) {
            int e2 = (exp2Texto != null) ? Integer.parseInt(exp2Texto) : 1;
            if (var2.equals(var)) expVarActiva += e2;
            else expVarConstante += e2;
        }

        String constanteSimbolica = "";
        if (expVarConstante != 0) {
            constanteSimbolica = (expVarConstante == 1)
                    ? varContraria
                    : varContraria + "^{" + expVarConstante + "}";
        }

        return new Termino(coef, expVarActiva, Termino.TipoFuncion.POLINOMIO, 1.0, constanteSimbolica);
    }

    private static String formatearCoefInternoSimbolico(double num) {
        if (Math.abs(num - 1.0) < 1e-7) return "";
        if (Math.abs(num + 1.0) < 1e-7) return "-";
        if (Math.abs(num - Math.round(num)) < 1e-7) {
            return String.format(Locale.US, "%.0f", (double) Math.round(num));
        }
        return String.format(Locale.US, "%.2f", num);
    }

    private static double parsearCoeficientePrefijo(String texto) {
        if (texto == null || texto.isEmpty() || texto.equals("+")) return 1.0;
        if (texto.equals("-")) return -1.0;
        return Double.parseDouble(texto);
    }
}
