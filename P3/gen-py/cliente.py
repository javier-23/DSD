from calculadora import Calculadora

from thrift import Thrift
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol

def main():
    transport = TSocket.TSocket('localhost',9090)
    transport = TTransport.TBufferedTransport( transport )
    protocol = TBinaryProtocol.TBinaryProtocol( transport )
    # creamos el cliente
    client = Calculadora.Client( protocol )

    transport.open()

    while True:
        print("\n--- Calculadora Distribuida ---")
        print("1. Suma")
        print("2. Resta")
        print("3. Multiplicación")
        print("4. División")
        print("5. Factorial")
        print("6. MCD (4 números)")
        print("7. MCM (4 números)")
        print("8. Salir")
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

            elif opcion == "8":
                break

            else:
                print("Opción no válida")

        except Exception as e:
            print(f"Error: {e}")

    transport.close()

if __name__ == "__main__":
    main()