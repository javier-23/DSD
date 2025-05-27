import http from 'node:http';
import { readFile } from 'node:fs';
import { join } from 'node:path';
import { Server } from 'socket.io';
import {MongoClient } from 'mongodb';
import TelegramBot from 'node-telegram-bot-api';
import dotenv from 'dotenv';

// Cargar variables de entorno desde el archivo .env
dotenv.config();

// Configuración del bot de Telegram
const token = process.env.TELEGRAM_TOKEN;
const chatId = process.env.TELEGRAM_CHAT_ID;
const bot = new TelegramBot(token, { polling: false });

// Configuración del servidor HTTP
const httpServer = http.createServer((req, res) => {
    let { url } = req;
    if (url === '/') {
        url = '/sistema-domotico.html';
        const filePath = join(process.cwd(), url);
        readFile(filePath, (err, data) => {
            if (!err) {
                res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
                res.write(data);
            } else {
                res.writeHead(500, { 'Content-Type': 'text/plain' });
                res.write('Error al cargar la página');
            }
            res.end();
        });
    } else {
        res.writeHead(404, { 'Content-Type': 'text/plain' });
        res.write('404 Not Found');
        res.end();
    }
});

// Configuración de MongoDB
const mongoUrl = "mongodb://localhost:27017/";
const dbName = "sistemaDomotico";

let umbrales = {
    temperatura: {
        min: 18,  // Valor por defecto
        max: 25   // Valor por defecto
    },
    luminosidad: 80,
    agua: 50  // Valor por defecto
    
};

let estado = {
    luminosidad: 0,
    temperatura: 0,
    agua: 0,
    Persiana: 'Abierta',
    Aire: 'Apagado',
    Aspersores: 'Apagado',
};


MongoClient.connect(mongoUrl)
    .then((db) => {
        const dbo = db.db(dbName);
        const collection = dbo.collection("dispositivos");
        const io = new Server(httpServer);
        
        io.sockets.on('connection', (client) => {
            
            client.emit('estado-inicial', estado);

            // Manejar la configuración de umbrales
            client.on('configurar-umbrales', (nuevosUmbrales) => {
                // Actualizar solo los valores no nulos
                if (nuevosUmbrales.temperatura.min !== null) 
                    umbrales.temperatura.min = nuevosUmbrales.temperatura.min;
                if (nuevosUmbrales.temperatura.max !== null) 
                    umbrales.temperatura.max = nuevosUmbrales.temperatura.max;
                if (nuevosUmbrales.luminosidad !== null) 
                    umbrales.luminosidad = nuevosUmbrales.luminosidad;
                
                // Guardar en la base de datos
                collection.updateOne(
                    { tipo: 'umbrales' },
                    { $set: { valores: umbrales, timestamp: new Date() } },
                    { upsert: true } // Crear el documento si no existe
                );
                
                // Notificar a todos los clientes
                io.emit('umbrales-actuales', umbrales);
            });

            client.on('actualizar-sensor', (data) => {
                const { tipo, valor } = data;
                const timestamp = new Date().toLocaleString();

                // Actualizar estado
                estado[tipo] = valor;

                // Guardar evento en la base de datos
                collection.insertOne({ tipo, valor, timestamp });

                // Notificar a todos los clientes
                io.emit('actualizacion', { tipo, valor, timestamp });

                // Lógica del agente
                if (tipo === 'luminosidad' && valor > umbrales.luminosidad) {
                    estado.Persiana = 'Cerrada';
                    io.emit('actualizacion-actuador', { actuador: 'Persiana', estadoNuevo: estado.Persiana });
                    io.emit('alarma', 'Luminosidad alta: Persiana cerrada automáticamente');

                    // Enviar el mensaje a Telegram
                    bot.sendMessage(chatId, '💡 ALERTA: Persiana BAJADA por superar la luminosidad máxima');
                }

                // En el evento 'actualizar-sensor' del servidor
                if (tipo === 'agua' && valor < umbrales.agua) {  // Si hay menos de 100ml de agua
                    estado.Aspersores = 'Encendido';
                    io.emit('actualizacion-actuador', { actuador: 'Aspersores', estadoNuevo: estado.Aspersores });
                    io.emit('alarma', 'Nivel de agua bajo: Aspersores encendidos automáticamente');
                    
                    // Si tienes configurado Telegram
                    bot.sendMessage(chatId, '💧 ALERTA: Nivel de agua bajo, aspersores activados');
                }

                if (tipo === 'temperatura' && valor < umbrales.temperatura.min) {
                    estado.Aire = 'Apagado';
                    io.emit('actualizacion-actuador', { actuador: 'Aire', estadoNuevo: estado.Aire });
                    io.emit('alarma', 'Temperatura baja: Aire acondicionado apagado automáticamente');

                    // Enviar el mensaje a Telegram
                    bot.sendMessage(chatId, '⚠️ ALERTA: Aire acondicionado APAGADO por temperatura inferior');
                } else if (tipo === 'temperatura' && valor > umbrales.temperatura.max) {
                    estado.Aire = 'Encendido';
                    io.emit('actualizacion-actuador', { actuador: 'Aire', estadoNuevo: estado.Aire });
                    io.emit('alarma', 'Temperatura alta: Aire acondicionado encendido automáticamente');

                    // Enviar el mensaje a Telegram
                    bot.sendMessage(chatId, '⚠️ ALERTA: Aire acondicionado ENCENDIDO por temperatura superior al umbral 🌡');
                }
            });

            client.on('cambiar-actuador', (data) => {
                const { actuador } = data;
                
                if(actuador === 'Persiana') {
                    estado.Persiana = estado.Persiana === 'Abierta' ? 'Cerrada' : 'Abierta';
                    io.emit('actualizacion-actuador', { actuador, estadoNuevo: estado.Persiana });
                }
                else if(actuador === 'Aire') {
                    estado.Aire = estado.Aire === 'Apagado' ? 'Encendido' : 'Apagado';
                    io.emit('actualizacion-actuador', { actuador, estadoNuevo: estado.Aire });
                }
                else{
                    // Para otros actuadores:
                    if (estado[actuador]) {
                        estado[actuador] = estado[actuador] === 'Encendido' ? 'Apagado' : 'Encendido';
                        io.emit('actualizacion-actuador', { actuador, estadoNuevo: estado[actuador] });
                    }
                }

                collection.insertOne({
                    dispositivo: actuador,
                    estado: estado[actuador],
                    timestamp: new Date().toLocaleString()
                });
            });

            client.on('nuevo-dispositivo', (data) => {
                const { tipo, nombre } = data;
                const timestamp = new Date().toLocaleString();
                
                // Agregar el nuevo dispositivo al estado
                if (tipo === 'sensor') {
                    estado[nombre] = 0; // Valor inicial para sensores
                } else if (tipo === 'actuador') {
                    estado[nombre] = 'Apagado'; // Estado inicial para actuadores
                }
                
                // Guardar en la base de datos
                collection.insertOne({ notificacion:"Nuevo Dispositivo", nombre, tipo, timestamp });
                
                // Notificar a todos los clientes
                io.emit('dispositivo-agregado', { tipo, nombre, timestamp });
            });
    });

        // Iniciar el servidor HTTP
        httpServer.listen(8080);
    })
    .catch(err => {
        console.error('Error al conectar a MongoDB:', err);
    });
    
console.log('Servicio MongoDB iniciado');