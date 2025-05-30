/*
 * This is sample code generated by rpcgen.
 * These are only templates and you can use them
 * as a guideline for developing your own functions.
 */

#include "calculadora.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// Funciones de la calculadora básica
void calculadora_basica(char *host, float arg1, char op, float arg2){
	CLIENT *clnt;
	float  *result = NULL;
	
	clnt = clnt_create(host, CALCPROG, CALCVER, "udp"); // Crear cliente RPC
	if (clnt == NULL) {
		clnt_pcreateerror (host);
		exit (1);
	}

	operands cuenta;
	cuenta.arg1 = arg1;
	cuenta.arg2 = arg2;

	switch (op){ // Selecciona la operacion a realizar
		case '+':
			result = suma_1(&cuenta, clnt);
			break;
		case '-':
			result = resta_1(&cuenta, clnt);
			break;
		case '*':
			result = multiplicacion_1(&cuenta, clnt);
			break;
		case '/':
			if(arg2 == 0){
				printf("Error: No se puede dividir por 0\n");
				clnt_destroy(clnt);
			}
			else
				result = division_1(&cuenta, clnt);
			
			break;
		default:
			printf("Operador invalido\n");
			clnt_destroy(clnt);
			break;
	}

	if(result == NULL){
		clnt_perror(clnt, "Error en la llamada RPC");
	}
	else{
		printf("Resultado: %f\n", *result);
	}

	clnt_destroy(clnt);

}

// Operaciones con vectores

void calcular_vector(char *host, vector_calc v1, vector_calc v2, char operador){
	CLIENT *clnt;
	vector_calc  *result = NULL;
	float *resultado_producto = NULL;
	
	clnt = clnt_create(host, CALCPROG, CALCVER, "udp");
	if (clnt == NULL) {
		clnt_pcreateerror (host);
		exit (1);
	}

	vector_operands vectores;
	vectores.operador = operador;
	vectores.v1 = v1;
	vectores.v2 = v2;

	// Convertir operador a cadena de caracteres
    char operador_str[2] = {operador, '\0'};

	// Llama a la función correspondiente del operador seleccionado
	if(strcmp(operador_str, "+") == 0 || strcmp(operador_str, "-") == 0)
		result = operaciones_vector_1(&vectores, clnt);
	else if (strcmp(operador_str, "*") == 0){
		resultado_producto = producto_escalar_1(&vectores, clnt);
	}
	else{
		printf("Operador invalido\n");
		clnt_destroy(clnt);
		exit(1);
	}

	if (result == NULL && resultado_producto == NULL) { // Error en la llamada RPC
		clnt_perror(clnt, "Error en la llamada RPC");
	} else if (resultado_producto != NULL) {	// Producto escalar
		printf("Resultado: %f\n", *resultado_producto);
	} else if (result != NULL && result->n > 0) {	// Suma o resta
		printf("Resultado: ");
		for (int i = 0; i < result->n; i++) {
			printf("%f ", result->datos.datos_val[i]);
		}
		printf("\n");
	} else {		// Error en la operación
		printf("Error en la operacion de vectores\n");
	}

	clnt_destroy(clnt);
}

// Operaciones con matrices

void calcular_matriz(char *host, matriz_calc m1, matriz_calc m2, char operador) {
    CLIENT *clnt;
    matriz_calc *result = NULL;

    clnt = clnt_create(host, CALCPROG, CALCVER, "udp");
    if (clnt == NULL) {
        clnt_pcreateerror(host);
        exit(1);
    }

    matriz_operands matrices;
    matrices.operador = operador;
    matrices.m1 = m1;
    matrices.m2 = m2;

    // Llama a la función correspondiente del operador seleccionado
    if (operador == '+' || operador == '-') {
        result = operaciones_matriz_1(&matrices, clnt);
    } else if (operador == '*') {
        result = multiplicacion_matriz_1(&matrices, clnt);
    } else {
        printf("Operador invalido\n");
        clnt_destroy(clnt);
        exit(1);
    }

    // Imprimir el resultado
    if (result == NULL) {
        clnt_perror(clnt, "Error en la llamada RPC");
    } else if (result->filas > 0 && result->columnas > 0) {
        printf("Resultado:\n");
        for (int i = 0; i < result->filas; i++) {
            for (int j = 0; j < result->columnas; j++) {
                printf("%f ", result->datos.datos_val[i * result->columnas + j]);
            }
            printf("\n");
        }
    } else {
        printf("Error en la operacion de matrices\n");
    }

    clnt_destroy(clnt);
}


int main (int argc, char *argv[]){
	
	if (argc < 2) {
        printf("Uso: %s <host>\n", argv[0]);
        exit(1);
    }

	char *host = argv[1];

	int opcion;
	printf("\n---------------------------------\n");
	printf("|	Calculadora remota	|\n");
	printf("---------------------------------\n\n");
	printf("Seleccione una opcion:\n");
	printf("1. Calculadora basica\n");
	printf("2. Operaciones con vectores\n");
	printf("3. Operaciones con matrices\n");
	printf("\n-> ");
	scanf("%d", &opcion); // Leer la opcion seleccionada
	getchar(); // Limpiar el buffer de entrada
	
	if(opcion == 1){ // Opción calculadora básica
		float arg1, arg2;
		char op;

		printf("Ingrese la operacion: ");
		scanf("%f %c %f", &arg1, &op, &arg2);

		calculadora_basica(host, arg1, op, arg2);
	}
	else if(opcion == 2){ // Opción vectores
		vector_calc v1, v2;
		char operador;

		// Leer tamaño vectores
		printf("Ingrese el número de elementos del vector:");
		scanf("%d", &v1.n);
		v2.n = v1.n;
		
		// Leer elementos del vector 1
		v1.datos.datos_len = v1.n;
		v1.datos.datos_val = (float *) malloc(v1.n * sizeof(float));
		printf("Ingrese los elementos del vector 1: ");
		for(int i = 0; i < v1.n; i++){
			scanf("%f", &v1.datos.datos_val[i]);
		}

		// Leer elementos del vector 2
		v2.datos.datos_len = v2.n;
		v2.datos.datos_val = (float *) malloc(v2.n * sizeof(float));
		printf("Ingrese los elementos del vector 2: ");
		for(int i = 0; i < v2.n; i++){
			scanf("%f", &v2.datos.datos_val[i]);
		}
		
		printf("Ingrese el operador ( '+', '-' o '*'(producto escalar) ): ");
		scanf(" %c", &operador);
		
		calcular_vector(host, v1, v2, operador);
		free(v1.datos.datos_val);
		free(v2.datos.datos_val);
	}
	else if(opcion == 3){ // Opción matrices
		matriz_calc m1, m2;
		char operador;

		// Leer dimensiones de la matriz 1
		printf("Ingrese el número de filas y columnas de la matriz 1: ");
		scanf("%d %d", &m1.filas, &m1.columnas);

		// Leer elementos de la matriz 1
		m1.datos.datos_len = m1.filas * m1.columnas;
		m1.datos.datos_val = (float *) malloc(m1.datos.datos_len * sizeof(float));
		printf("Ingrese los elementos de la matriz 1:\n");
		for (int i = 0; i < m1.filas; i++) {
			for (int j = 0; j < m1.columnas; j++) {
				scanf("%f", &m1.datos.datos_val[i * m1.columnas + j]);
			}
		}

		// Leer dimensiones de la matriz 2
		printf("Ingrese el número de filas y columnas de la matriz 2: ");
		scanf("%d %d", &m2.filas, &m2.columnas);

		// Leer elementos de la matriz 2
		m2.datos.datos_len = m2.filas * m2.columnas;
		m2.datos.datos_val = (float *) malloc(m2.datos.datos_len * sizeof(float));
		printf("Ingrese los elementos de la matriz 2:\n");
		for (int i = 0; i < m2.filas; i++) {
			for (int j = 0; j < m2.columnas; j++) {
				scanf("%f", &m2.datos.datos_val[i * m2.columnas + j]);
			}
		}

		// Leer el operador
		printf("Ingrese el operador (+, -, *): ");
		scanf(" %c", &operador);

		// Realizar la operación
		calcular_matriz(host, m1, m2, operador);

		// Liberar memoria
		free(m1.datos.datos_val);
		free(m2.datos.datos_val);
	}
	else{
		printf("Opcion invalida\n");
	}
	
	exit (0);
}
