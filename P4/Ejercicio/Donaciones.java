import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

public class Donaciones extends UnicastRemoteObject implements DonacionesInterfaz {
    
    // Identificador del servidor
    private int idServidor;
    private String ipOtroServidor;
    private static final int PUERTO_RMI = 1099;
    private String nombreServicioRemoto;
    
    // Datos locales
    private List<String> entidadesRegistradas;
    private Map<String, Double> donacionesPorEntidad;
    private double subtotalDonaciones;
    
    // Variables para exclusión mutua
    private int relojLogico;
    private boolean accediendoARecursoCompartido;
    private boolean esperandoRespuesta;
    private boolean respuestaRecibida;
    
    public Donaciones(int idServidor, String ipOtroServidor, String nombreServicioRemoto) throws RemoteException {
        this.idServidor = idServidor;
        this.ipOtroServidor = ipOtroServidor;
        this.nombreServicioRemoto = nombreServicioRemoto;
        
        entidadesRegistradas = new ArrayList<>();
        donacionesPorEntidad = new HashMap<>();
        subtotalDonaciones = 0.0;
        relojLogico = 0;
        accediendoARecursoCompartido = false;
        esperandoRespuesta = false;
        respuestaRecibida = false;
    }
    
    // Incrementa el reloj lógico al recibir un mensaje
    private synchronized void actualizarReloj(int relojExterno) {
        relojLogico = Math.max(relojLogico, relojExterno) + 1;
    }
    
    @Override
    public int registrarCliente(String nombreEntidad) throws RemoteException {
        System.out.println("[Servidor " + idServidor + "] Solicitud de registro para: " + nombreEntidad);
        
        // Verificar si ya está registrado localmente
        if (entidadesRegistradas.contains(nombreEntidad)) {
            System.out.println("[Servidor " + idServidor + "] El cliente ya está registrado localmente.");
            return 0;
        }
        
        try {
            // Verificar con el otro servidor
            DonacionesInterfaz otroServidor = conectarConOtroServidor();
            
            if (otroServidor.verificarEntidadRegistrada(nombreEntidad)) {
                System.out.println("[Servidor " + idServidor + "] El cliente ya está registrado en el otro servidor.");
                return 0;
            }
            
            // Decidir dónde registrar (en el servidor con menos entidades)
            int numEntidadesLocales = entidadesRegistradas.size();
            int numEntidadesOtroServidor = otroServidor.getNumeroEntidades();
            
            if (numEntidadesLocales <= numEntidadesOtroServidor) {
                // Registrar localmente
                entidadesRegistradas.add(nombreEntidad);
                donacionesPorEntidad.put(nombreEntidad, 0.0);
                System.out.println("[Servidor " + idServidor + "] Cliente registrado localmente: " + nombreEntidad);
                return idServidor;
            } else {
                // Registrar en el otro servidor
                otroServidor.sincronizarRegistro(nombreEntidad);
                System.out.println("[Servidor " + idServidor + "] Cliente redirigido en otro servidor: " + nombreEntidad);
                return otroServidor.getIdServidor();
            }
        } catch (Exception e) {
            System.err.println("[Servidor " + idServidor + "] Error al comunicarse con otro servidor: " + e.getMessage());
            return 0; // Error en la comunicación
        }
    }
    
    @Override
    public boolean donar(String nombreEntidad, double cantidad) throws RemoteException {
        System.out.println("[Servidor " + idServidor + "] Solicitud de donación de: " + nombreEntidad + " por " + cantidad);
        
        // Verificar si la entidad está registrada localmente
        if (!entidadesRegistradas.contains(nombreEntidad)) {
            System.out.println("[Servidor " + idServidor + "] La entidad no está registrada en este servidor.");
            return false;
        }
        
        // Iniciar protocolo de exclusión mutua
        solicitarAccesoRecursoCompartido();
        
        try {
            // Registrar la donación
            double donacionActual = donacionesPorEntidad.getOrDefault(nombreEntidad, 0.0);
            donacionesPorEntidad.put(nombreEntidad, donacionActual + cantidad);
            subtotalDonaciones += cantidad;
            
            System.out.println("[Servidor " + idServidor + "] Donación registrada: " + cantidad + " de " + nombreEntidad);
            return true;
        } finally {
            // Liberar acceso al recurso compartido
            liberarAccesoRecursoCompartido();
        }
    }
    
    @Override
    public double consultarTotalDonado(String nombreEntidad) throws RemoteException {
        // Verificar si la entidad está registrada y ha donado
        if (!entidadesRegistradas.contains(nombreEntidad) || donacionesPorEntidad.get(nombreEntidad) <= 0) {
            throw new RemoteException("No autorizado: Entidad no registrada o sin donaciones");
        }
        
        double totalDonado = subtotalDonaciones;
        
        try {
            // Obtener subtotal del otro servidor
            DonacionesInterfaz otroServidor = conectarConOtroServidor();
            totalDonado += otroServidor.getSubtotalLocal();
        } catch (Exception e) {
            System.err.println("[Servidor " + idServidor + "] Error al obtener subtotal del otro servidor: " + e.getMessage());
        }
        
        return totalDonado;
    }
    
    @Override
    public List<String> listadoDonantes(String nombreEntidad) throws RemoteException {
        // Verificar si la entidad está registrada y ha donado
        if (!entidadesRegistradas.contains(nombreEntidad) || donacionesPorEntidad.get(nombreEntidad) <= 0) {
            throw new RemoteException("No autorizado: Entidad no registrada o sin donaciones");
        }
        
        List<String> todosLosDonantes = new ArrayList<>();
        
        // Añadir donantes locales
        for (int i=0; i<entidadesRegistradas.size(); i++) {
            String entidad = entidadesRegistradas.get(i);
            if (donacionesPorEntidad.get(entidad) > 0) {
                todosLosDonantes.add(entidad);
            }
        }
        
        try {
            // Obtener donantes del otro servidor
            DonacionesInterfaz otroServidor = conectarConOtroServidor();
            todosLosDonantes.addAll(otroServidor.getDonantes());
        } catch (Exception e) {
            System.err.println("[Servidor " + idServidor + "] Error al obtener donantes del otro servidor: " + e.getMessage());
        }
        
        return todosLosDonantes;
    }
    
    // Métodos para comunicación entre servidores
    @Override
    public boolean verificarEntidadRegistrada(String nombreEntidad) throws RemoteException {
        return entidadesRegistradas.contains(nombreEntidad);
    }
    
    @Override
    public int getNumeroEntidades() throws RemoteException {
        return entidadesRegistradas.size();
    }

    @Override
    public int getIdServidor() throws RemoteException {
        return this.idServidor;
    }
    
    // Método para sincronizar el registro de entidades, si en el otro servidor no está registrado y hay más clientes
    @Override
    public void sincronizarRegistro(String nombreEntidad) throws RemoteException {
        if (!entidadesRegistradas.contains(nombreEntidad)) {
            entidadesRegistradas.add(nombreEntidad);
            donacionesPorEntidad.put(nombreEntidad, 0.0);
            System.out.println("[Servidor " + idServidor + "] Cliente registrado por sincronización: " + nombreEntidad);
        }
    }
    
    @Override
    public double getSubtotalLocal() throws RemoteException {
        return subtotalDonaciones;
    }
    
    @Override
    public List<String> getDonantes() throws RemoteException {
        List<String> donantesLocales = new ArrayList<>();
        for (int i=0; i<entidadesRegistradas.size(); i++) {
            String entidad = entidadesRegistradas.get(i);
            if (donacionesPorEntidad.get(entidad) > 0) {
                donantesLocales.add(entidad);
            }
        }

        return donantesLocales;
    }
    
    // Métodos para exclusión mutua distribuida
    private void solicitarAccesoRecursoCompartido() {
        relojLogico++; // Incrementa el reloj antes de solicitar acceso
        int relojSolicitud = relojLogico;
        
        esperandoRespuesta = true;
        respuestaRecibida = false;

        System.out.println("[Servidor " + idServidor + "] Solicitando acceso al recurso compartido...");
        
        try {
            // Solicitar acceso al otro servidor
            DonacionesInterfaz otroServidor = conectarConOtroServidor();
            otroServidor.solicitarAcceso(relojSolicitud, idServidor);

            // Esperar respuesta con timeout ***
            int intentos = 0;
            final int MAX_INTENTOS = 50; // 5 segundos máximo
        
            // Esperar respuesta
            while (!respuestaRecibida && intentos < MAX_INTENTOS) {
                Thread.sleep(100);
                intentos++; //
            }

            //**
            if (!respuestaRecibida) {
                System.out.println("[Servidor " + idServidor + "] Timeout esperando respuesta. Asumiendo acceso.");
            } else {
             System.out.println("[Servidor " + idServidor + "] Respuesta recibida, acceso concedido.");
            }//** 
            
            accediendoARecursoCompartido = true;
            esperandoRespuesta = false;
        } catch (Exception e) {
            System.err.println("[Servidor " + idServidor + "] Error en exclusión mutua: " + e.getMessage());
            accediendoARecursoCompartido = true; // En caso de error, asumimos acceso
            esperandoRespuesta = false;
        }
    }
    
    private void liberarAccesoRecursoCompartido() {
        accediendoARecursoCompartido = false;
        relojLogico++;
        
        try {
            // Notificar liberación al otro servidor
            DonacionesInterfaz otroServidor = conectarConOtroServidor();
            otroServidor.liberarAcceso(relojLogico, idServidor);
        } catch (Exception e) {
            System.err.println("[Servidor " + idServidor + "] Error al liberar acceso: " + e.getMessage());
        }
    }
    
    @Override
    public synchronized void solicitarAcceso(int relojSolicitante, int idServidor) throws RemoteException {
        actualizarReloj(relojSolicitante);
        
        // Si no estamos accediendo o esperando, o si el reloj del solicitante es menor
        // (o igual pero con ID menor), respondemos inmediatamente
        if (!accediendoARecursoCompartido && !esperandoRespuesta) {
            responderSolicitud(relojLogico);
        } else if (esperandoRespuesta && 
                  (relojSolicitante < relojLogico || 
                  (relojSolicitante == relojLogico && idServidor < this.idServidor))) {
            responderSolicitud(relojLogico);
        } else {
            // Poner en cola la respuesta para cuando liberemos el recurso
            new Thread(() -> {
                try{
                    while (accediendoARecursoCompartido || esperandoRespuesta) {
                        Thread.sleep(100);
                    }
                    try {
                        DonacionesInterfaz otroServidor = conectarConOtroServidor();
                        otroServidor.responderSolicitud(relojLogico);
                    } catch (Exception e) {
                        System.err.println("[Servidor " + this.idServidor + "] Error al responder solicitud: " + e.getMessage());
                    }
                } catch (InterruptedException e) {
                    System.err.println("[Servidor " + this.idServidor + "] Error en la espera de respuesta: " + e.getMessage());
                }
            }).start();
        }
    }
    
    @Override
    public synchronized void liberarAcceso(int relojLiberacion, int idServidor) throws RemoteException {
        actualizarReloj(relojLiberacion);
        System.out.println("[Servidor " + this.idServidor + "] Servidor " + idServidor + " ha liberado el acceso.");

    }
    
    @Override
    public synchronized void responderSolicitud(int relojRespuesta) throws RemoteException {
        actualizarReloj(relojRespuesta);
        System.out.println("[Servidor " + idServidor + "] Recibida respuesta de acceso al recurso.");
        this.respuestaRecibida = true;
    }
    
    // Método para conectar con el otro servidor
    private DonacionesInterfaz conectarConOtroServidor() throws RemoteException, NotBoundException {
        int puertoRMI = PUERTO_RMI + ((idServidor == 1) ? 1 : 0); // Si soy Servidor 1, conecto a puerto 1100, si soy Servidor 2, conecto a puerto 1099
        Registry registry = LocateRegistry.getRegistry(ipOtroServidor, puertoRMI);
        return (DonacionesInterfaz) registry.lookup(nombreServicioRemoto);
    }
}