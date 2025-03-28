import glob
import sys

from calculadora import Calculadora
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol
from thrift.server import TServer

import logging
import math
from math import gcd
from functools import reduce
logging.basicConfig( level = logging.DEBUG )

class CalculadoraHandler:
    def __init__(self):
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