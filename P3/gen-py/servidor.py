import glob
import sys

from calculadora import Calculadora
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol
from thrift.server import TServer

import logging
logging.basicConfig( level = logging . DEBUG )

class CalculadoraHandler:
    def __init__(self):
        self.log = {}

    def suma(self, numero1, numero2):
        print('Sumando ' + str(numero1) + " con " + str(numero2))
        return numero1 + numero2

    def resta(self, numero1, numero2):
        print('Restando ' + str(numero1) + " con " + str(numero2))
        return numero1 - numero2
    

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