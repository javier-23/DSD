/* calc.x: Definición de la interfaz RPC para la calculadora */

struct operands{
    float arg1;
    float arg2;
};

struct vector_calc{
            int n;
            float datos<100>;
};

struct vector_operands{
    vector_calc v1;
    vector_calc v2;
    char operador;
};

struct matriz_calc{
            int filas;
            int columnas;
            float datos<10000>;
};

struct matriz_operands{
    matriz_calc m1;
    matriz_calc m2;
    char operador;
};

program CALCPROG {
    version CALCVER {
        float SUMA(operands) = 1;
        float RESTA(operands) = 2;
        float MULTIPLICACION(operands) = 3;
        float DIVISION(operands) = 4;

        vector_calc OPERACIONES_VECTOR(vector_operands) = 5;
        float PRODUCTO_ESCALAR(vector_operands) = 6;

        matriz_calc OPERACIONES_MATRIZ(matriz_operands) = 7;
        matriz_calc MULTIPLICACION_MATRIZ(matriz_operands) = 8;
    } = 1;
} = 0x20000199;