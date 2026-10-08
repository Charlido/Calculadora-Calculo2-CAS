package model;

public class MathEngine {

    // Ya no requerimos librerías externas para la lógica, usamos POO puro.
    public MathEngine() {
    }

    public String calcularLaTeX(int modo, String f, String g, String a, String b, String var) throws Exception {
        // Usamos nuestro propio parser POO
        Polinomio polinomioF = Polinomio.parse(f);
        String latexResult = "";

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
                double limA = Double.parseDouble(a);
                double limB = Double.parseDouble(b);

                // Teorema Fundamental del Cálculo (POO puro)
                double evaluacionA = integralDef.evaluar(limA);
                double evaluacionB = integralDef.evaluar(limB);
                double resultado = evaluacionB - evaluacionA;

                String resStr = (resultado % 1 == 0) ? String.format("%.0f", resultado) : String.format("%.2f", resultado);
                latexResult = "\\text{Resultado} = " + resStr;
                break;

            case 3: // Área entre curvas
                Polinomio polinomioG = Polinomio.parse(g);
                Polinomio intF = polinomioF.integrar();
                Polinomio intG = polinomioG.integrar();
                double limA_Area = Double.parseDouble(a);
                double limB_Area = Double.parseDouble(b);

                double resF = intF.evaluar(limB_Area) - intF.evaluar(limA_Area);
                double resG = intG.evaluar(limB_Area) - intG.evaluar(limA_Area);
                double area = Math.abs(resF - resG);

                String areaStr = (area % 1 == 0) ? String.format("%.0f", area) : String.format("%.2f", area);
                latexResult = "\\text{Área} = " + areaStr + " u^2";
                break;

            default:
                latexResult = "\\text{Modo en desarrollo}";
        }
        return latexResult;
    }
}