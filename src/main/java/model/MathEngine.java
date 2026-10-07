package model;

import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.eval.TeXUtilities;
import org.matheclipse.core.interfaces.IExpr;
import java.io.StringWriter;

public class MathEngine {
    private final ExprEvaluator evaluator;
    private final TeXUtilities texUtil;

    public MathEngine() {
        evaluator = new ExprEvaluator(false, (short) 100);
        // AQUÍ ESTÁ LA CORRECCIÓN: Se extrae el EvalEngine interno
        texUtil = new TeXUtilities(evaluator.getEvalEngine(), true);
    }

    public String calcularLaTeX(int modo, String f, String g, String a, String b, String var) throws Exception {
        String fLimpio = formatearExpresion(f);
        String gLimpio = formatearExpresion(g);
        String expresionSymja = "";

        switch (modo) {
            case 0: // Derivada
                expresionSymja = "D(" + fLimpio + ", " + var + ")";
                break;
            case 1: // Integral Indefinida
                expresionSymja = "Integrate(" + fLimpio + ", " + var + ")";
                break;
            case 2: // Integral Definida
                expresionSymja = "Integrate(" + fLimpio + ", {" + var + ", " + a + ", " + b + "})";
                break;
            case 3: // Área entre curvas
                expresionSymja = "Integrate((" + fLimpio + ") - (" + gLimpio + "), {" + var + ", " + a + ", " + b + "})";
                break;
            case 4: // Volumen (Eje)
                expresionSymja = "Integrate(Pi * (" + fLimpio + ")^2, {" + var + ", " + a + ", " + b + "})";
                break;
        }

        // 1. Evalúa el cálculo matemáticamente
        IExpr resultado = evaluator.evaluate(expresionSymja);

        // 2. Convierte el resultado a sintaxis LaTeX
        StringWriter writer = new StringWriter();
        texUtil.toTeX(resultado, writer);
        String latex = writer.toString();

        // Agrega la constante C a las integrales indefinidas
        if (modo == 1) {
            latex += " + C";
        }

        return latex;
    }

    private String formatearExpresion(String expr) {
        if (expr == null) return "";
        return expr.toLowerCase()
                .replace("sin", "Sin")
                .replace("cos", "Cos")
                .replace("tan", "Tan")
                .replace("ln", "Log")
                .replace("exp", "Exp")
                .replace("pi", "Pi")
                .replace("e^", "E^");
    }
}