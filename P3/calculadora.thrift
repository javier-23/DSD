service Calculadora{

    double suma(1:double num1, 2:double num2),
    double resta(1:double num1, 2:double num2),
    double multiplicacion(1:double num1, 2:double num2),
    double division(1:double num1, 2:double num2),

    i64 factorial(1:i32 n),
    i64 mcd(1:list<i32> numeros),
    i64 mcm(1:list<i32> numeros),

    list<double> operacionVectores(1:string operacion, 2:list<double> v1, 3:list<double> v2),
    list<list<double>> operacionMatrices(1:string operacion, 2:list<list<double>> m1, 3:list<list<double>> m2)
}