from calculadoraAvanzada import CalculadoraAvanzada
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol
from thrift.server import TServer

class CalculadoraAvanzadaHandler:
    def sumaVectores(self, v1, v2):
        return [a + b for a, b in zip(v1, v2)]

    def restaVectores(self, v1, v2):
        return [a - b for a, b in zip(v1, v2)]

    def productoEscalar(self, v1, v2):
        return sum(a * b for a, b in zip(v1, v2))

    def sumaMatrices(self, m1, m2):
        return [[a + b for a, b in zip(row1, row2)] for row1, row2 in zip(m1, m2)]

    def restaMatrices(self, m1, m2):
        return [[a - b for a, b in zip(row1, row2)] for row1, row2 in zip(m1, m2)]

    def multiplicarMatrices(self, m1, m2):
        return [[sum(a * b for a, b in zip(row, col)) for col in zip(*m2)] for row in m1]


if __name__ == "__main__":
    handler = CalculadoraAvanzadaHandler()
    processor = CalculadoraAvanzada.Processor(handler)
    transport = TSocket.TServerSocket(host='localhost', port=9091)  # Puerto diferente
    tfactory = TTransport.TBufferedTransportFactory()
    pfactory = TBinaryProtocol.TBinaryProtocolFactory()

    server = TServer.TSimpleServer(processor, transport, tfactory, pfactory)
    print("Iniciando servidor de la calculadora avanzada...")
    server.serve()