import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServidorDonaciones {
    // Configuración común
    private static final int PUERTO_RMI = 1099;
    
    // Configuración específica para cada servidor
    private static final String[] NOMBRES_SERVICIO = {"", "Donaciones1", "Donaciones2"};
    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: ServidorDonaciones <id_servidor> <ip_otro_servidor>");
            System.out.println("  donde <id_servidor> es 1 o 2");
            System.out.println("  y <ip_otro_servidor> es la IP del otro servidor");
            System.exit(1);
        }
        
        // Parsear ID del servidor (1 o 2)
        int idServidor;
        try {
            idServidor = Integer.parseInt(args[0]);
            if (idServidor != 1 && idServidor != 2) {
                throw new NumberFormatException("El ID debe ser 1 o 2");
            }
        } catch (NumberFormatException e) {
            System.err.println("Error: El ID del servidor debe ser 1 o 2");
            System.exit(1);
            return;
        }

        // Obtener la IP del otro servidor desde los argumentos
        String ipOtroServidor = args[1];
        
        
        // Nombres de servicios
        String nombreServicioLocal = NOMBRES_SERVICIO[idServidor];
        String nombreServicioRemoto = NOMBRES_SERVICIO[3 - idServidor]; // El otro servidor
        
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        
        try {
            // Crear registro RMI
            int puertoRMI = PUERTO_RMI + (idServidor - 1); // 1099 o 1100
            Registry registry = LocateRegistry.createRegistry(puertoRMI);
            
            // Crear instancia de Donaciones
            Donaciones donaciones = new Donaciones(idServidor, ipOtroServidor, nombreServicioRemoto);
            
            // Registrar en RMI
            registry.rebind(nombreServicioLocal, donaciones);
            
            System.out.println("[Servidor" + idServidor + "] Servidor de donaciones listo en el puerto " + puertoRMI);
            System.out.println("[Servidor" + idServidor + "] Nombre del servicio: " + nombreServicioLocal);
            System.out.println("[Servidor" + idServidor + "] Conectado a réplica en: " + ipOtroServidor);
        } catch (Exception e) {
            System.err.println("[Servidor" + idServidor + "] Error al iniciar servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}