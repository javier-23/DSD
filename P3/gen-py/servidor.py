import glob
import sys

from calculadora import Calculadora
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol
from thrift.server import TServer
from calculadoraAvanzada import CalculadoraAvanzada

import logging
import math
from math import gcd
from functools import reduce
logging.basicConfig( level = logging.DEBUG )

class CalculadoraHandler:
    
    def __init__(self):
        #Configurar cliente para el segundo servidor:
        self.nuevo_transport = TSocket.TSocket('localhost',9091)
        self.nuevo_transport = TTransport.TBufferedTransport( self.nuevo_transport )
        self.nuevo_protocol = TBinaryProtocol.TBinaryProtocol( self.nuevo_transport )
        # creamos el cliente
        self.nuevo_client = CalculadoraAvanzada.Client( self.nuevo_protocol )
        self.nuevo_transport.open()
    
        self.log = {}

    def suma(self, numero1, numero2):
        print('Sumando ' + str(numero1) + " con " + str(numero2))
        return numero1 + numero2

    def resta(self, numero1, numero2):
        print('Restando ' + str(numero1) + " con " + str(numero2))
        return numero1 - numero2
    
    def multiplicacion(self, numero1, numero2):
        print('Multiplicando ' + str(numero1) + " con " + str(numero2))
        return numero1 * numero2
    
    def division(self, numero1, numero2):
        if numero2 == 0:
            print('Error: División por cero')
            return 0
        print('Dividiendo ' + str(numero1) + " con " + str(numero2))
        return numero1 / numero2
    
    def factorial(self, n):
        if n < 0:
            print('Error: Factorial de un número negativo')
            return 0
        return math.factorial(n)
    
    def mcd(self, numeros):
        return reduce(gcd, numeros) # reduce aplica la función gcd a todos los números de la lista
    
    def mcm(self, numeros):
        def lcm(a, b): 
            return a * b // gcd(a, b) # // divisón que redondea hacia abajo
        return reduce(lcm, numeros)

    #Servidor 2:
    def operacionVectores(self, operacion, v1, v2):
        try:
            if not isinstance(v1, list) or not isinstance(v2, list):
                raise ValueError("Los vectores deben ser listas")
        
            if operacion == '+':
                return self.nuevo_client.sumaVectores(v1, v2)
            elif operacion == '-':
                return self.nuevo_client.restaVectores(v1, v2)
            elif operacion == '*':
                return [self.nuevo_client.productoEscalar(v1, v2)]  # Devolver como lista
            else:
                raise ValueError("Operación no válida")
        except Exception as e:
            print(f"Error en operación con vectores: {e}")
            return None

    def operacionMatrices(self, operacion, m1, m2):
        try:
            if operacion == '+':
                return self.nuevo_client.sumaMatrices(m1, m2)
            elif operacion == '-':
                return self.nuevo_client.restaMatrices(m1, m2)
            elif operacion == '*':
                return self.nuevo_client.multiplicarMatrices(m1, m2)
            else:
                raise ValueError("Operación no válida")
        except Exception as e:
            print(f"Error en operación con matrices: {e}")
            return None
    

if(__name__ == '__main__'):
    handler = CalculadoraHandler()
    processor = Calculadora.Processor(handler)
    transport = TSocket.TServerSocket(host='localhost', port=9090)
    tfactory = TTransport.TBufferedTransportFactory()
    pfactory = TBinaryProtocol.TBinaryProtocolFactory()

    server = TServer.TSimpleServer(processor, transport, tfactory, pfactory)

    print('Iniciando el servidor...')
    server.serve()
    print('done')