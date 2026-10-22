package model;

import org.matheclipse.core.eval.ExprEvaluator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MathEngine {

    public static class PasoResolucion {
        public final String titulo;
        public final String explicacion;
        public final String formulaLaTeX;

        public PasoResolucion(String titulo, String explicacion, String formulaLaTeX) {
            this.titulo = titulo;
            this.explicacion = explicacion;
            this.formulaLaTeX = formulaLaTeX;
        }
    }

    public MathEngine() {
    }

    public String calcularLaTeX(int modo, String f, String g, String a, String b, String var) throws Exception {
        Polinomio polinomioF = Polinomio.parse(f);
        String latexResult;

        double resultadoNumerico = 0;
        boolean esDefinida = false;

        switch (modo) {
            case 0: // Derivada Simbólica
                Polinomio derivada = polinomioF.derivar();
                latexResult = "f'(" + var + ") = " + derivada.toLaTeX(var);
                break;

            case 1: // Integral Indefinida
                Polinomio integral = polinomioF.integrar();
                latexResult = "\\int f(" + var + ")\\,d" + var + " = " + integral.toLaTeX(var) + " + C";
                break;

            case 2: // Integral Definida
                double[] limDef = parsearLimites(a, b, "una Integral Definida");
                Polinomio integralDef = polinomioF.integrar();

                double evalA = integralDef.evaluar(limDef[0]);
                double evalB = integralDef.evaluar(limDef[1]);

                if (!Double.isFinite(evalA) || !Double.isFinite(evalB)) {
                    throw new IllegalArgumentException("La función no es continua o no está definida en el intervalo [" + a + ", " + b + "].");
                }

                resultadoNumerico = evalB - evalA;
                esDefinida = true;

                String resStr = formatearDecimal(resultadoNumerico);
                latexResult = "\\int_{" + formatearDecimal(limDef[0]) + "}^{" + formatearDecimal(limDef[1]) + "} f(" + var + ")\\,d" + var + " = " + resStr;
                break;

            case 3: // Área entre curvas
                if (g == null || g.trim().isEmpty()) {
                    throw new IllegalArgumentException("Debes ingresar la segunda función en g(var) para calcular el Área entre curvas.");
                }
                Polinomio polinomioG = Polinomio.parse(g);
                double[] limArea = parsearLimites(a, b, "calcular el Área");

                resultadoNumerico = integrarAreaEntreCurvas(polinomioF, polinomioG, limArea[0], limArea[1]);
                if (!Double.isFinite(resultadoNumerico)) {
                    throw new IllegalArgumentException("Las funciones no están definidas en todo el intervalo [" + a + ", " + b + "].");
                }
                esDefinida = true;

                String areaStr = formatearDecimal(resultadoNumerico);
                latexResult = "\\text{Área} = \\int_{" + formatearDecimal(limArea[0]) + "}^{" + formatearDecimal(limArea[1]) + "} |f(" + var + ") - g(" + var + ")|\\,d" + var + " = " + areaStr + "\\text{ u}^2";
                break;

            case 4: // Volumen de Revolución (Método de Discos: V = pi * int_a^b [f(x)]^2 dx)
                double[] limVol = parsearLimites(a, b, "calcular el Volumen de Revolución");
                double integralCuadrado = integrarCuadrado(polinomioF, limVol[0], limVol[1]);

                if (!Double.isFinite(integralCuadrado)) {
                    throw new IllegalArgumentException("La función no está definida en todo el intervalo [" + a + ", " + b + "].");
                }

                resultadoNumerico = Math.PI * integralCuadrado;
                esDefinida = true;

                String coefPiStr = formatearDecimal(integralCuadrado);
                String volTotalStr = formatearDecimal(resultadoNumerico);
                latexResult = "V = \\pi \\int_{" + formatearDecimal(limVol[0]) + "}^{" + formatearDecimal(limVol[1]) + "} [f(" + var + ")]^2\\,d" + var + " = " + coefPiStr + "\\pi \\approx " + volTotalStr + "\\text{ u}^3";
                break;

            default:
                latexResult = "\\text{Modo no válido}";
        }

        // =========================================================
        // LLAMADA AL ORÁCULO DE PRUEBAS (Se ejecuta en segundo plano)
        // =========================================================
        final boolean finalEsDefinida = esDefinida;
        final double finalResultadoNum = resultadoNumerico;

        new Thread(() -> {
            verificarConSymjaSilencioso(modo, f, g, a, b, var, finalEsDefinida, finalResultadoNum);
        }).start();

        return latexResult;
    }

    /**
     * Genera la lista detallada de pasos de resolución según los teoremas y reglas del curso de Cálculo.
     */
    public List<PasoResolucion> generarPasosResolucion(int modo, String f, String g, String a, String b, String var) {
        List<PasoResolucion> pasos = new ArrayList<>();
        Polinomio polinomioF = Polinomio.parse(f);
        List<Termino> terminosF = polinomioF.getTerminos();

        switch (modo) {
            case 0: { // Derivada Simbólica
                pasos.add(new PasoResolucion(
                        "Paso 1: Planteamiento del Operador Derivada",
                        "Identificamos la función f(" + var + ") (simplificando términos semejantes si los hubiera) y planteamos su derivada respecto a '" + var + "':",
                        "f'(" + var + ") = \\frac{d}{d" + var + "}\\left[" + polinomioF.toLaTeX(var) + "\\right]"
                ));

                if (terminosF.size() > 1) {
                    StringBuilder sumaDerivadas = new StringBuilder();
                    for (int i = 0; i < terminosF.size(); i++) {
                        if (i > 0) sumaDerivadas.append(" + ");
                        sumaDerivadas.append("\\frac{d}{d").append(var).append("}\\left(").append(terminosF.get(i).toLaTeX(var)).append("\\right)");
                    }
                    pasos.add(new PasoResolucion(
                            "Paso 2: Regla de Linealidad (Suma y Resta)",
                            "La derivada de una suma algebraica de funciones es igual a la suma de las derivadas de cada término individual:",
                            "f'(" + var + ") = " + sumaDerivadas
                    ));
                }

                int numPaso = pasos.size() + 1;
                for (int i = 0; i < terminosF.size(); i++) {
                    Termino t = terminosF.get(i);
                    pasos.add(new PasoResolucion(
                            "Paso " + numPaso + "." + (i + 1) + ": Derivación del término #" + (i + 1),
                            "Aplicamos la " + t.obtenerNombreReglaDerivada() + ":",
                            t.explicarDerivadaLaTeX(var)
                    ));
                }

                Polinomio derivada = polinomioF.derivar();
                pasos.add(new PasoResolucion(
                        "Paso Final: Resultado Simplificado",
                        "Reuniendo los resultados de cada término obtenemos la función derivada:",
                        "f'(" + var + ") = " + derivada.toLaTeX(var)
                ));
                break;
            }

            case 1: { // Integral Indefinida
                pasos.add(new PasoResolucion(
                        "Paso 1: Planteamiento de la Integral Indefinida",
                        "Expresamos la integral indefinida de la función respecto al diferencial d" + var + ":",
                        "I = \\int \\left(" + polinomioF.toLaTeX(var) + "\\right)\\,d" + var
                ));

                if (terminosF.size() > 1) {
                    StringBuilder sumaIntegrales = new StringBuilder();
                    for (int i = 0; i < terminosF.size(); i++) {
                        if (i > 0) sumaIntegrales.append(" + ");
                        sumaIntegrales.append("\\int ").append(terminosF.get(i).toLaTeX(var)).append("\\,d").append(var);
                    }
                    pasos.add(new PasoResolucion(
                            "Paso 2: Propiedad de Linealidad de la Integral",
                            "Separamos la integral en una suma de integrales más sencillas para cada término:",
                            "I = " + sumaIntegrales
                    ));
                }

                int numPaso = pasos.size() + 1;
                for (int i = 0; i < terminosF.size(); i++) {
                    Termino t = terminosF.get(i);
                    pasos.add(new PasoResolucion(
                            "Paso " + numPaso + "." + (i + 1) + ": Integración del término #" + (i + 1),
                            "Aplicamos la " + t.obtenerNombreReglaIntegral() + ":",
                            t.explicarIntegralLaTeX(var)
                    ));
                }

                Polinomio integral = polinomioF.integrar();
                pasos.add(new PasoResolucion(
                        "Paso Final: Adición de la Constante de Integración (+ C)",
                        "Sumamos las antiderivadas parciales y añadimos la constante arbitraria C de la familia de primitivas:",
                        "\\int f(" + var + ")\\,d" + var + " = " + integral.toLaTeX(var) + " + C"
                ));
                break;
            }

            case 2: { // Integral Definida (TFC)
                double[] limDef = parsearLimites(a, b, "una Integral Definida");
                String aStr = formatearDecimal(limDef[0]);
                String bStr = formatearDecimal(limDef[1]);
                Polinomio integralDef = polinomioF.integrar();

                double evalA = integralDef.evaluar(limDef[0]);
                double evalB = integralDef.evaluar(limDef[1]);
                if (!Double.isFinite(evalA) || !Double.isFinite(evalB)) {
                    throw new IllegalArgumentException("La función no es continua o no está definida en el intervalo [" + a + ", " + b + "].");
                }
                double resultado = evalB - evalA;

                pasos.add(new PasoResolucion(
                        "Paso 1: Planteamiento de la Integral Definida",
                        "Planteamos la integral definida de f(" + var + ") en el intervalo cerrado [" + aStr + ", " + bStr + "]:",
                        "I = \\int_{" + aStr + "}^{" + bStr + "} \\left(" + polinomioF.toLaTeX(var) + "\\right)\\,d" + var
                ));

                pasos.add(new PasoResolucion(
                        "Paso 2: Cálculo de la Antiderivada F(" + var + ")",
                        "Hallamos la función primitiva F(" + var + ") integrando término a término antes de evaluar los límites:",
                        "F(" + var + ") = \\left[ " + integralDef.toLaTeX(var) + " \\right]_{" + aStr + "}^{" + bStr + "}"
                ));

                pasos.add(new PasoResolucion(
                        "Paso 3: Teorema Fundamental del Cálculo (Regla de Barrow)",
                        "Evaluamos la antiderivada en el límite superior (" + var + " = " + bStr + ") y en el límite inferior (" + var + " = " + aStr + "):",
                        "F(" + bStr + ") = " + formatearDecimal(evalB) + " \\quad ; \\quad F(" + aStr + ") = " + formatearDecimal(evalA)
                ));

                pasos.add(new PasoResolucion(
                        "Paso Final: Diferencia F(b) - F(a)",
                        "Restamos F(" + aStr + ") a F(" + bStr + ") para obtener el valor neto de la integral definida:",
                        "I = F(" + bStr + ") - F(" + aStr + ") = (" + formatearDecimal(evalB) + ") - (" + formatearDecimal(evalA) + ") = " + formatearDecimal(resultado)
                ));
                break;
            }

            case 3: { // Área entre curvas
                if (g == null || g.trim().isEmpty()) {
                    throw new IllegalArgumentException("Debes ingresar la segunda función en g(var) para calcular el Área entre curvas.");
                }
                Polinomio polinomioG = Polinomio.parse(g);
                double[] limArea = parsearLimites(a, b, "calcular el Área");
                String aStr = formatearDecimal(limArea[0]);
                String bStr = formatearDecimal(limArea[1]);

                double area = integrarAreaEntreCurvas(polinomioF, polinomioG, limArea[0], limArea[1]);
                if (!Double.isFinite(area)) {
                    throw new IllegalArgumentException("Las funciones no están definidas en todo el intervalo [" + a + ", " + b + "].");
                }

                Polinomio intF = polinomioF.integrar();
                Polinomio intG = polinomioG.integrar();

                pasos.add(new PasoResolucion(
                        "Paso 1: Identificación de las Funciones y el Intervalo",
                        "Definimos las dos curvas que delimitan la región plana en [" + aStr + ", " + bStr + "]:",
                        "f(" + var + ") = " + polinomioF.toLaTeX(var) + " \\quad ; \\quad g(" + var + ") = " + polinomioG.toLaTeX(var)
                ));

                pasos.add(new PasoResolucion(
                        "Paso 2: Fórmula del Área entre Dos Curvas",
                        "El área geométrica entre f(" + var + ") y g(" + var + ") se define como la integral de la distancia vertical |f(" + var + ") - g(" + var + ")|:",
                        "A = \\int_{" + aStr + "}^{" + bStr + "} \\left|\\left(" + polinomioF.toLaTeX(var) + "\\right) - \\left(" + polinomioG.toLaTeX(var) + "\\right)\\right|\\,d" + var
                ));

                pasos.add(new PasoResolucion(
                        "Paso 3: Antiderivadas de f(" + var + ") y g(" + var + ")",
                        "Las primitivas correspondientes a cada función son:",
                        "F(" + var + ") = " + intF.toLaTeX(var) + " \\quad ; \\quad G(" + var + ") = " + intG.toLaTeX(var)
                ));

                pasos.add(new PasoResolucion(
                        "Paso Final: Evaluación del Área en [" + aStr + ", " + bStr + "]",
                        "Integrando la diferencia absoluta en todo el intervalo obtenemos el área total en unidades cuadradas:",
                        "\\text{Área} = " + formatearDecimal(area) + "\\text{ u}^2"
                ));
                break;
            }

            case 4: { // Volumen de Revolución
                double[] limVol = parsearLimites(a, b, "calcular el Volumen de Revolución");
                String aStr = formatearDecimal(limVol[0]);
                String bStr = formatearDecimal(limVol[1]);

                double integralCuadrado = integrarCuadrado(polinomioF, limVol[0], limVol[1]);
                if (!Double.isFinite(integralCuadrado)) {
                    throw new IllegalArgumentException("La función no está definida en todo el intervalo [" + a + ", " + b + "].");
                }
                double volumen = Math.PI * integralCuadrado;

                pasos.add(new PasoResolucion(
                        "Paso 1: Método de los Discos (Sólido de Revolución)",
                        "Al rotar la región bajo R(" + var + ") = f(" + var + ") alrededor del eje en [" + aStr + ", " + bStr + "], cada sección transversal es un disco de área π·[R(" + var + ")]²:",
                        "V = \\pi \\int_{" + aStr + "}^{" + bStr + "} \\left[f(" + var + ")\\right]^2\\,d" + var
                ));

                pasos.add(new PasoResolucion(
                        "Paso 2: Sustitución del Radio f(" + var + ") en la Integral",
                        "Reemplazamos la expresión de f(" + var + ") dentro del integrando elevado al cuadrado:",
                        "V = \\pi \\int_{" + aStr + "}^{" + bStr + "} \\left(" + polinomioF.toLaTeX(var) + "\\right)^2\\,d" + var
                ));

                pasos.add(new PasoResolucion(
                        "Paso 3: Resolución de la Integral Definida en [" + aStr + ", " + bStr + "]",
                        "Evaluamos la integral del cuadrado del radio a lo largo del intervalo [" + aStr + ", " + bStr + "]:",
                        "\\int_{" + aStr + "}^{" + bStr + "} \\left(" + polinomioF.toLaTeX(var) + "\\right)^2\\,d" + var + " = " + formatearDecimal(integralCuadrado)
                ));

                pasos.add(new PasoResolucion(
                        "Paso Final: Multiplicación por π y Volumen Total",
                        "Expresamos el volumen tanto en múltiplos exactos de π como en su valor numérico en unidades cúbicas:",
                        "V = " + formatearDecimal(integralCuadrado) + "\\pi \\approx " + formatearDecimal(volumen) + "\\text{ u}^3"
                ));
                break;
            }
        }

        return pasos;
    }

    private double[] parsearLimites(String a, String b, String contexto) {
        try {
            double limA = Double.parseDouble(a);
            double limB = Double.parseDouble(b);
            return new double[]{limA, limB};
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Los límites 'a' y 'b' deben ser números válidos para " + contexto + ".");
        }
    }

    // Integración numérica de alta precisión para Área entre curvas: int_a^b |f(x) - g(x)| dx
    private double integrarAreaEntreCurvas(Polinomio f, Polinomio g, double a, double b) {
        if (Math.abs(b - a) < 1e-12) return 0.0;
        double inicio = Math.min(a, b);
        double fin = Math.max(a, b);
        int pasos = 1000;
        double h = (fin - inicio) / pasos;
        double suma = 0.0;

        for (int i = 0; i <= pasos; i++) {
            double x = inicio + i * h;
            double val = Math.abs(f.evaluar(x) - g.evaluar(x));
            if (!Double.isFinite(val)) return Double.NaN;
            if (i == 0 || i == pasos) {
                suma += val;
            } else if (i % 2 == 1) {
                suma += 4.0 * val;
            } else {
                suma += 2.0 * val;
            }
        }
        return (h / 3.0) * suma;
    }

    // Integración por regla de Simpson para Volumen de Revolución: int_a^b [f(x)]^2 dx
    private double integrarCuadrado(Polinomio f, double a, double b) {
        if (Math.abs(b - a) < 1e-12) return 0.0;
        double inicio = Math.min(a, b);
        double fin = Math.max(a, b);
        int pasos = 1000;
        double h = (fin - inicio) / pasos;
        double suma = 0.0;

        for (int i = 0; i <= pasos; i++) {
            double x = inicio + i * h;
            double fx = f.evaluar(x);
            if (!Double.isFinite(fx)) return Double.NaN;
            double val = fx * fx;
            if (i == 0 || i == pasos) {
                suma += val;
            } else if (i % 2 == 1) {
                suma += 4.0 * val;
            } else {
                suma += 2.0 * val;
            }
        }
        return (h / 3.0) * suma;
    }

    private String formatearDecimal(double valor) {
        if (Math.abs(valor - Math.round(valor)) < 1e-6) {
            return String.format(Locale.US, "%.0f", (double) Math.round(valor));
        }
        return String.format(Locale.US, "%.2f", valor);
    }

    /**
     * Motor de verificación en segundo plano propuesto por Saul.
     * Solo imprime en la consola, no afecta la interfaz del usuario.
     */
    private void verificarConSymjaSilencioso(int modo, String f, String g, String a, String b, String var, boolean esDefinida, double resultadoPOO) {
        try {
            ExprEvaluator util = new ExprEvaluator();

            String fSymja = normalizarParaSymja(f);
            String gSymja = normalizarParaSymja(g);

            String consulta;
            System.out.println("---------------------------------------------------");
            System.out.println("🔍 [ORÁCULO SYMJA] Iniciando verificación...");

            switch (modo) {
                case 0:
                    consulta = "D(" + fSymja + ", " + var + ")";
                    System.out.println("   Calculando: " + consulta);
                    System.out.println("   Resultado Symja: " + util.eval(consulta).toString());
                    break;
                case 1:
                    consulta = "Integrate(" + fSymja + ", " + var + ")";
                    System.out.println("   Calculando: " + consulta);
                    System.out.println("   Resultado Symja: " + util.eval(consulta).toString());
                    break;
                case 2:
                    consulta = "N(Integrate(" + fSymja + ", {" + var + ", " + a + ", " + b + "}))";
                    System.out.println("   Calculando: " + consulta);
                    double resSymjaDef = Double.parseDouble(util.eval(consulta).toString());
                    System.out.println("   Resultado Symja: " + resSymjaDef + " | Resultado POO: " + resultadoPOO);
                    break;
                case 3:
                    consulta = "N(Integrate(Abs((" + fSymja + ") - (" + gSymja + ")), {" + var + ", " + a + ", " + b + "}))";
                    System.out.println("   Calculando: " + consulta);
                    double resSymjaArea = Double.parseDouble(util.eval(consulta).toString());
                    System.out.println("   Resultado Symja: " + resSymjaArea + " | Resultado POO: " + resultadoPOO);
                    break;
                case 4:
                    consulta = "N(Pi * Integrate((" + fSymja + ")^2, {" + var + ", " + a + ", " + b + "}))";
                    System.out.println("   Calculando: " + consulta);
                    double resSymjaVol = Double.parseDouble(util.eval(consulta).toString());
                    System.out.println("   Resultado Symja: " + resSymjaVol + " | Resultado POO: " + resultadoPOO);
                    break;
            }
            System.out.println("---------------------------------------------------");

        } catch (Exception e) {
            System.out.println("⚠️ [ORÁCULO SYMJA] Omitido: La expresión tiene un formato que Symja no puede parsear directamente.");
        }
    }

    private String normalizarParaSymja(String expr) {
        if (expr == null) return "";
        String s = expr.replace(" ", "");
        s = s.replaceAll("([0-9])([xy])", "$1*$2");
        s = s.replaceAll("([0-9])(sin|cos|tan|ln)", "$1*$2");
        s = s.replace("ln(", "Log(");
        s = s.replace("sin(", "Sin(");
        s = s.replace("cos(", "Cos(");
        s = s.replace("tan(", "Tan(");
        return s;
    }
}
