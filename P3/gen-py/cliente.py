from calculadora import Calculadora

from thrift import Thrift
from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol

transport = TSocket.TSocket('localhost',9090)
transport = TTransport.TBufferedTransport( transport )
protocol = TBinaryProtocol.TBinaryProtocol( transport )
# creamos el cliente
client = Calculadora.Client( protocol )

transport.open()
resultado = client.suma( 1 , 2 )
print( "La suma es: " + str(resultado) )
resultado = client.resta( 2 , 1 )
print( "La resta es: " + str(resultado) )

transport.close()