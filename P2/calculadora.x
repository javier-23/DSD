/* calc.x: Definición de la interfaz RPC para la calculadora */

program CALCPROG {
    version CALCVER {
        float SUMA(float, float) = 1;
        float RESTA(float, float) = 2;
        float MULTIPLICACION(float, float) = 3;
        float DIVISION(float, float) = 4;
    } = 1;
} = 0x20000199;
