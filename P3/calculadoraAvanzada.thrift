service CalculadoraAvanzada {
    // Operaciones con vectores
    list<double> sumaVectores(1:list<double> v1, 2:list<double> v2),
    list<double> restaVectores(1:list<double> v1, 2:list<double> v2),
    double productoEscalar(1:list<double> v1, 2:list<double> v2),

    // Operaciones con matrices
    list<list<double>> sumaMatrices(1:list<list<double>> m1, 2:list<list<double>> m2),
    list<list<double>> restaMatrices(1:list<list<double>> m1, 2:list<list<double>> m2),
    list<list<double>> multiplicarMatrices(1:list<list<double>> m1, 2:list<list<double>> m2),
}