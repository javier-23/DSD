from calculadoraAvanzada import CalculadoraAvanzada
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol
from thrift.server import TServer

class CalculadoraAvanzadaHandler:
    
    def sumaVectores(self, v1, v2):
        resultado = []
        for i in range(len(v1)):
            resultado.append(v1[i] + v2[i])
        return resultado

    def restaVectores(self, v1, v2):
        resultado = []
        for i in range(len(v1)):
            resultado.append(v1[i] - v2[i])
        return resultado

    def productoEscalar(self, v1, v2):
        suma = 0
        for i in range(len(v1)):
            suma += v1[i] * v2[i]
        return suma

    def sumaMatrices(self, m1, m2):
        resultado = []
        for i in range(len(m1)):
            fila_resultado = []
            for j in range(len(m1[0])):
                fila_resultado.append(m1[i][j] + m2[i][j])
            resultado.append(fila_resultado)
        return resultado

    def restaMatrices(self, m1, m2):
        resultado = []
        for i in range(len(m1)):
            fila_resultado = []
            for j in range(len(m1[0])):
                fila_resultado.append(m1[i][j] - m2[i][j])
            resultado.append(fila_resultado)
        return resultado

    def multiplicarMatrices(self, m1, m2):
        filas_m1 = len(m1)
        columnas_m1 = len(m1[0])
        columnas_m2 = len(m2[0])
        
        resultado = []
        for i in range(filas_m1):
            fila_resultado = []
            for j in range(columnas_m2):
                suma = 0
                for k in range(columnas_m1):
                    suma += m1[i][k] * m2[k][j]
                fila_resultado.append(suma)
            resultado.append(fila_resultado)
        return resultado


if __name__ == "__main__":
    handler = CalculadoraAvanzadaHandler()
    processor = CalculadoraAvanzada.Processor(handler)
    transport = TSocket.TServerSocket(host='localhost', port=9091)  # Puerto diferente
    tfactory = TTransport.TBufferedTransportFactory()
    pfactory = TBinaryProtocol.TBinaryProtocolFactory()

    server = TServer.TSimpleServer(processor, transport, tfactory, pfactory)
    print("Iniciando servidor de la calculadora avanzada...")
    server.serve()