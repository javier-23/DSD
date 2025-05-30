# DSD ☁️
Prácticas de la asignatura Desarrollo de Sistemas Distribuidos.

## :one: Práctica RPC
<p align="justify">
Implementación de programas distribuidos con llamadas a procedimientos remotos RPC. Para ello se desarrollará en esta práctica una calculadora distribuido con cliente y servidor utilizando las distintas funcionalidades que ofrece Sum RPC.
</p>

<p align="justify">
La calculadora implementada resuelve operaciones básicas como suma, resta, multiplicación y división, y operaciones más complejas con vectores (suma, resta y producto escalar) y con matrices (suma, resta y multiplicación).
</p>

> [Código práctica RPC](https://github.com/javier-23/DSD/tree/06ee78666559a2578d748e762c113f442b51fc67/P2 "Código práctica")

## 2️⃣ Práctica Apache Thrift
<p align="justify">
Realizar una calculadora distribuida, usando las funcionalidades de Apache Thrift, que realiza operaciones básicas, factorial, máximo común divisor entre cuatro números, mínimo común múltiplo entre cuatro números, operaciones con vectores (suma, resta y producto escalar) y matrices (suma, resta y multiplicación).

El lenguaje que se utiliza es Python, y para la solución se han implementado un cliente y dos servidores, uno para las operaciones más sencillas,, el cual será el cliente del segundo, y el segundo servidor que hará las operaciones con vectores y matrices.
</p>

>[Código práctica Apache Thrift](https://github.com/javier-23/DSD/tree/3647db5ecfaeb9fd5fbcd2207f9f9fafbe90f3f7/P3)

## 3️⃣ Práctica RMI (Remote Method Invocation)
<p align="justify">
En esta práctica he probado diferentes ejemplos ya implementados para aprender el funcionamiento de RMI. 

### Ejemplo 1
- El primer ejemplo es una aplicación cliente-servidor, donde el servidor cuando recibe una petición de un cliente imprime el argumento enviado en la llamada. Si es 0, es argumento espera un tiempo antes de volver a imprimir el mensaje.
>[Código Ejemplo 1](https://github.com/javier-23/DSD/tree/701f26e3b44501268ee475bfc9def5dd24931953/P4/Ejemplo1)

### Ejemplo 2
- El segundo ejemplo es similar, pero, en lugar de lanzar varios clientes, creamos múltiples hebras que realizan la misma tarea de imprimir un mensaje remoto accediendo al stub de un objeto remoto. Este ejemplo nos permite ver la gestión de la concurrencia en RMI. En esta ocasión, en vez de pasar al objeto remoto un número entero, pasamos una cadena String. Se utilizará el modificador synchronized para ver las diferencias.
>[Código Ejemplo 2](https://github.com/javier-23/DSD/tree/701f26e3b44501268ee475bfc9def5dd24931953/P4/Ejemplo2)

### Ejemplo 3
- El tercer ejemplo crea por una parte el objeto remoto y por otra el servidor. El servidor (servidor.java) exporta los métodos contenidos en la interfaz icontador.java del objeto remoto instanciado como micontador de la clase definida en contador.java.
El cliente es un aplicación normal de Java que realiza las siguientes acciones: pone un valor inicial en el contador del servidor, invoca el método incrementar del contador 1.000 veces y finalmente imprime el valor final del contador junto con el tiempo de respuesta medio que se ha calculado a partir de las invocaciones remotas del método incrementar.
>[Código Ejemplo 3](https://github.com/javier-23/DSD/tree/701f26e3b44501268ee475bfc9def5dd24931953/P4/Ejemplo3)

### Servidor replicado
- El ejercicio final consiste en desarrollar en RMI un sistema cliente-servidor teniendo en cuenta los siguientes requisitos:
    - El servidor será un servidor replicado (con exactamente 2 replicas), cada replica desplegada en una máquina diferente, y estará encargado de recibir donaciones de entidades (clientes) para una causa.
    - El servidor proporcionará dos operaciones, registro de una entidad interesada (cliente) en la causa, y depósito de una donación a la causa. No es posible realizar un depósito (o más) sin haberse registrado como cliente previamente.
    - Cuando una entidad desea registrarse y contacta con cualquiera de las dos réplicas del servidor, entonces el registro del cliente debe ocurrir realmente y de forma transparente en la réplica con menos entidades registradas. Es decir, el cliente sólo se ha dirigido a una réplica aunque esta no haya sido donde realmente ha quedado registrado, pero a partir de ese momento, el cliente realizará los depósitos en la réplica del servidor donde ha sido registrado.
    - Cada réplica del servidor mantendrá el subtotal de las donaciones realizadas en dicha replica.
    - Un cliente no podrá registrarse más de una vez, ni siquiera en replicas distintas.
    - Los servidores también ofrecerán dos operaciones de consulta: total donado y listado de donantes. Dichas operaciones sólo podrán llevarse a cabo si el cliente previamente se ha registrado y ha realizado al menos un depósito. Cuando un cliente realice alguna de estas consultas, sólo hará la petición a la réplica donde se encuentra registrado, y ésta será la encargada de devolver el resultado después de solicitar la información oportuna a la otra replica.

>[Servidor replicado de donaciones](https://github.com/javier-23/DSD/tree/701f26e3b44501268ee475bfc9def5dd24931953/P4/Ejercicio)

</p>

## 4️⃣ Node.js
<p align="justify">
En esta práctica tuve que hacer en primer lugar una serie de ejemplos ya implementados para trabajar con Node.js y aprender su funcionamiento. Algunos de estos se utilizarán diferentes herramientas que servirán para aprender a usarlas y utilizarlas en el ejercicio final, como son Socket.io y MongoDB.

>[Ejemplos](https://github.com/javier-23/DSD/tree/701f26e3b44501268ee475bfc9def5dd24931953/P5-Nodejs/Ejemplos)

Para el ejercicio final se implementará un sistema domótico, el cual tiene las siguientes funcionalidades:
- Está compuesto de tres sensores (luminosidad, temperatura y agua).
- Sus respectivos actuadores: persianas, aire acondicionado y aspersores.
- Un servidor que muestra las páginas para mostrar el estado y actaur sobre los elementos.
- Un agente para notificar alarmas y tomar decisiones.

En concreto, el sistema se comporta de la siguiente manera:

#### Sensores y Simulación
- Los sensores (temperatura, luminosidad, agua) difunden información a través del servidor
- Las mediciones se simulan mediante un formulario web
- Cada nueva medida genera un evento en tiempo real

#### Servidor
- Proporciona la interfaz web para control y simulación
- Gestiona las suscripciones de usuarios y del agente automático
- Almacena un historial de eventos con marcas temporales en MongoDB
- Mantiene el estado actual del sistema

#### Interfaz de Usuario
- Muestra en tiempo real las medidas de los sensores
- Presenta el historial de eventos y notificaciones
- Permite control manual de actuadores (persianas, aire acondicionado, aspersores)
- Permite configurar umbrales para las automatizaciones

#### Agente
Implementa las siguientes reglas:

- Luminosidad alta → Cierra automáticamente la persiana
- Temperatura baja → Apaga el aire acondicionado
- Temperatura alta → Enciende el aire acondicionado
- Nivel de agua bajo → Activa los aspersores

El sistema utiliza comunicación en tiempo real mediante Socket.IO, permitiendo que todos los dispositivos conectados reciban actualizaciones instantáneas y que el agente responda inmediatamente a los cambios en las condiciones monitorizadas.

Además hace uso de un bot de Telegram para que cada vez que salta una alarma, llega un mensaje a un chat creado con el bot.

</p>

