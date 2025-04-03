
from calculadora import Calculadora

from thrift import Thrift
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol

def ingresar_vector(nombre):
    datos = input(f"Ingrese {nombre} (ej: 1,2,3): ")
    return [float(x) for x in datos.split(',')]

def ingresar_matriz(filas, columnas, num_matriz):
    matriz = []
    for i in range(filas):
        while True:
            fila = input(f"Ingrese fila {i+1} de la matriz {num_matriz} (ej: 1,2,3): ")
            elementos = fila.split(',')
            if len(elementos) != columnas: # Verifica la longitud de la fila
                    print(f"Error: La fila debe tener {columnas} elementos.")
            else:
                matriz.append([float(x) for x in elementos])
                break
    return matriz

def mostrar_cabecera():
    print("\n")
    print("---------------------------------")
    print("|    CALCULADORA DISTRIBUIDA    |")
    print("---------------------------------")


def main():
    transport = TSocket.TSocket('localhost',9090)
    transport = TTransport.TBufferedTransport( transport )
    protocol = TBinaryProtocol.TBinaryProtocol( transport )
    # creamos el cliente
    client = Calculadora.Client( protocol )
    transport.open()

    while True:
        mostrar_cabecera()
        print("Seleccione una operación:")
        print("1. Suma")
        print("2. Resta")
        print("3. Multiplicación")
        print("4. División")
        print("5. Factorial")
        print("6. MCD (4 números)")
        print("7. MCM (4 números)")
        print("8. Operaciones con vectores")
        print("9. Operaciones con matrices")
        print("10. Salir")
        opcion = input("Seleccione una opción: ")

        try:
            if opcion == "1":
                num1 = float(input("Número 1: "))
                num2 = float(input("Número 2: "))
                print(f"Resultado: {client.suma(num1, num2)}")

            elif opcion == "2":
                num1 = float(input("Número 1: "))
                num2 = float(input("Número 2: "))
                print(f"Resultado: {client.resta(num1, num2)}")

            elif opcion == "3":
                num1 = float(input("Número 1: "))
                num2 = float(input("Número 2: "))
                print(f"Resultado: {client.multiplicacion(num1, num2)}")

            elif opcion == "4":
                num1 = float(input("Número 1: "))
                num2 = float(input("Número 2: "))
                try:
                    resultado = client.division(num1, num2)
                    if num2 == 0:
                        print("Error: No se puede dividir por cero")
                    else:
                        print(f"Resultado: {resultado}")
                except Exception as e:
                    print(f"Error: {e}")

            elif opcion == "5":
                n = int(input("Número para factorial: "))
                try:
                    resultado = client.factorial(n)
                    if n < 0:
                        print("Error: No se pueden números negativos")
                    else:
                        print(f"Resultado: {resultado}")
                except Exception as e:
                    print(f"Error: {e}")

            elif opcion == "6":
                numeros = []
                for i in range(4):
                    num = int(input(f"Número {i+1}: "))
                    numeros.append(num)

                print(f"Resultado: {client.mcd(numeros)}")

            elif opcion == "7":
                numeros = []
                for i in range(4):
                    num = int(input(f"Número {i+1}: "))
                    numeros.append(num)

                print(f"Resultado: {client.mcm(numeros)}")

            elif opcion == "8":  # Suma de vectores
                print("Operaciones disponibles: + (suma), - (resta), * (producto escalar)")
                operacion = input("Seleccione operación: ")
                if(operacion not in ['+', '-', '*']):
                    print("Error: Operación no válida")
                    continue

                v1 = ingresar_vector("Vector 1")
                v2 = ingresar_vector("Vector 2")
                if(len(v1) != len(v2)):
                    print("Error: Los vectores deben tener la misma longitud")
                else:
                    resultado = client.operacionVectores(operacion, v1, v2)
                    print(f"Resultado: {resultado}")

            elif opcion == "9":  # Suma de matrices
                print("Operaciones disponibles: + (suma), - (resta), * (multiplicación)")
                operacion = input("Seleccione operación: ")
                if(operacion not in ['+', '-', '*']):
                    print("Error: Operación no válida")
                    continue
                
                print("Dimensiones de las matriz 1 (ej: 2x3):")
                filas1, columnas1 = map(int, input().split('x'))
                print("Dimensiones de la matriz 2 (ej: 2x3):")
                filas2, columnas2 = map(int, input().split('x'))

                if( (filas1 != filas2 or columnas1 != columnas2) and operacion != '*'): # Comprobación de suma y resta de matrices
                    print("Error: Las dimensiones de las matrices no son compatibles para la operación seleccionada")
                elif(operacion == '*' and columnas1 != filas2): # Comprobacion para multiplicacion de matrices
                    print("Error: Las dimensiones de las matrices no son compatibles para la operación seleccionada")
                else:
                    m1 = ingresar_matriz(filas1, columnas1, 1)
                    print("\n")
                    m2 = ingresar_matriz(filas2, columnas2, 2)
                    resultado = client.operacionMatrices(operacion, m1, m2)
                    print("Resultado:")
                    for fila in resultado:
                        print(fila)

            elif opcion == "10":
                break

            else:
                print("Opción no válida")

        except Exception as e:
            print(f"Error: {e}")

    transport.close()

if __name__ == "__main__":
    main()