import http from 'node:http';

http.createServer((request, response) => {
    console.log(request.headers);
    response.writeHead(200, {'Content−Type': 'text/plain'});
    response.write('Hola mundo');
    response.end();
}).listen(8080);

console.log('Servicio HTTP iniciado');