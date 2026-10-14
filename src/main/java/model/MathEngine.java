package model;

// Importamos la librería de Symja para el verificador de Saul
import org.matheclipse.core.eval.ExprEvaluator;

public class MathEngine {

    public MathEngine() {
    }

    public String calcularLaTeX(int modo, String f, String g, String a, String b, String var) throws Exception {
        Polinomio polinomioF = Polinomio.parse(f);
        String latexResult = "";

        // Variable para guardar el resultado numérico en caso de ser definido
        double resultadoNumerico = 0;
        boolean esDefinida = false;

        switch (modo) {
            case 0: // Derivada
                Polinomio derivada = polinomioF.derivar();
                latexResult = "f'(" + var + ") = " + derivada.toLaTeX();
                break;

            case 1: // Integral Indefinida
                Polinomio integral = polinomioF.integrar();
                latexResult = "\\int f(" + var + ") d" + var + " = " + integral.toLaTeX() + " + C";
                break;

            case 2: // Integral Definida
                Polinomio integralDef = polinomioF.integrar();
                double limA, limB;

                try {
                    limA = Double.parseDouble(a);
                    limB = Double.parseDouble(b);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Los límites 'a' y 'b' deben ser números válidos para una Integral Definida.");
                }

                double evaluacionA = integralDef.evaluar(limA);
                double evaluacionB = integralDef.evaluar(limB);
                resultadoNumerico = evaluacionB - evaluacionA;
                esDefinida = true;

                String resStr = (resultadoNumerico % 1 == 0) ? String.format("%.0f", resultadoNumerico) : String.format("%.2f", resultadoNumerico);
                latexResult = "\\text{Resultado} = " + resStr;
                break;

            case 3: // Área entre curvas
                Polinomio polinomioG = Polinomio.parse(g);
                Polinomio intF = polinomioF.integrar();
                Polinomio intG = polinomioG.integrar();
                double limA_Area, limB_Area;

                try {
                    limA_Area = Double.parseDouble(a);
                    limB_Area = Double.parseDouble(b);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Los límites 'a' y 'b' deben ser números válidos para calcular el Área.");
                }

                double resF = intF.evaluar(limB_Area) - intF.evaluar(limA_Area);
                double resG = intG.evaluar(limB_Area) - intG.evaluar(limA_Area);
                resultadoNumerico = Math.abs(resF - resG);
                esDefinida = true;

                String areaStr = (resultadoNumerico % 1 == 0) ? String.format("%.0f", resultadoNumerico) : String.format("%.2f", resultadoNumerico);
                latexResult = "\\text{Área} = " + areaStr + " u^2";
                break;

            default:
                latexResult = "\\text{Modo en desarrollo}";
        }

        // =========================================================
        // LLAMADA AL ORÁCULO DE PRUEBAS (Se ejecuta en segundo plano)
        // =========================================================
        final boolean finalEsDefinida = esDefinida;
        final double finalResultadoNum = resultadoNumerico;

        // Lo corremos en un hilo separado para que no congele la interfaz si Symja tarda
        new Thread(() -> {
            verificarConSymjaSilencioso(modo, f, g, a, b, var, finalEsDefinida, finalResultadoNum);
        }).start();

        return latexResult;
    }

    /**
     * Motor de verificación en segundo plano propuesto por Saul.
     * Solo imprime en la consola de IntelliJ, no afecta la interfaz del usuario.
     */
    private void verificarConSymjaSilencioso(int modo, String f, String g, String a, String b, String var, boolean esDefinida, double resultadoPOO) {
        try {
            ExprEvaluator util = new ExprEvaluator();

            // TRUCO DE COMPATIBILIDAD: Symja exige asteriscos (ej: 3*x^2).
            // Nuestra POO usa "3x^2". Usamos Expresiones Regulares para agregar el asterisco automáticamente.
            String fSymja = f.replaceAll("([0-9])([xy])", "$1*$2");
            String gSymja = g.replaceAll("([0-9])([xy])", "$1*$2");

            String consulta = "";
            System.out.println("---------------------------------------------------");
            System.out.println("🔍 [ORÁCULO SYMJA] Iniciando verificación...");

            switch (modo) {
                case 0: // Derivada (D[f, x])
                    consulta = "D(" + fSymja + ", " + var + ")";
                    System.out.println("   Calculando: " + consulta);
                    System.out.println("   Resultado Symja: " + util.eval(consulta).toString());
                    break;
                case 1: // Integral Indefinida (Integrate[f, x])
                    consulta = "Integrate(" + fSymja + ", " + var + ")";
                    System.out.println("   Calculando: " + consulta);
                    System.out.println("   Resultado Symja: " + util.eval(consulta).toString());
                    break;
                case 2: // Integral Definida (Integrate[f, {x, a, b}])
                    consulta = "N(Integrate(" + fSymja + ", {" + var + ", " + a + ", " + b + "}))";
                    System.out.println("   Calculando: " + consulta);
                    double resSymjaDef = Double.parseDouble(util.eval(consulta).toString());
                    System.out.println("   Resultado Symja: " + resSymjaDef + " | Resultado POO: " + resultadoPOO);
                    break;
                case 3: // Área (Integrate[Abs[f - g], {x, a, b}])
                    consulta = "N(Integrate(Abs((" + fSymja + ") - (" + gSymja + ")), {" + var + ", " + a + ", " + b + "}))";
                    System.out.println("   Calculando: " + consulta);
                    double resSymjaArea = Double.parseDouble(util.eval(consulta).toString());
                    System.out.println("   Resultado Symja: " + resSymjaArea + " | Resultado POO: " + resultadoPOO);
                    break;
            }
            System.out.println("---------------------------------------------------");

        } catch (Exception e) {
            System.out.println("⚠️ [ORÁCULO SYMJA] Omitido: La expresión tiene un formato que Symja no puede parsear directamente.");
        }
    }
}