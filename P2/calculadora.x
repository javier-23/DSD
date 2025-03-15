/* calc.x: Definición de la interfaz RPC para la calculadora */

program CALCPROG {
    version CALCVER {
        float SUMA(int, int) = 1;
        float RESTA(int, int) = 2;
        float MULTIPLICACION(int, int) = 3;
        float DIVISION(int, int) = 4;
    } = 1;
} = 0x20000199;
