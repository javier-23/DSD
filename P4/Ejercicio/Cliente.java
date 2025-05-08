import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;
import java.util.Random;

public class Cliente {
    private static Scanner scanner = new Scanner(System.in);
    private static String nombreEntidad = null;
    private static DonacionesInterfaz servidor = null;
    private static Random random = new Random();
    private static int servidorActual = random.nextInt(2) + 1; // ID del servidor aleatorio
    private static int servidorAsignado = 0;
    private static String nombreServidor = null;
    private static String ipServidor = null;

    // Nombres de los servicios correspondientes a cada ID de servidor
    private static final String[] NOMBRES_SERVICIO = {"", "Donaciones1", "Donaciones2"};
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: Cliente <ip_servidor>");
            System.exit(1);
        }
        
        ipServidor = args[0];
        
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        
        try {
            // Intentar conectar a un servidor por defecto primero
            conectarServidor(ipServidor, NOMBRES_SERVICIO[servidorActual]);
            
            boolean salir = false;
            while (!salir) {
                mostrarMenu();
                int opcion = Integer.parseInt(scanner.nextLine());
                
                switch (opcion) {
                    case 1:
                        registrarEntidad();
                        break;
                    case 2:
                        asegurarConexionCorrecta(ipServidor);
                        realizarDonacion();
                        break;
                    case 3:
                        asegurarConexionCorrecta(ipServidor);
                        consultarTotalDonado();
                        break;
                    case 4:
                        asegurarConexionCorrecta(ipServidor);
                        listarDonantes();
                        break;
                    case 5:
                        asegurarConexionCorrecta(ipServidor);
                        verTopDonantes();
                        break;
                    case 6:
                        asegurarConexionCorrecta(ipServidor);
                        consultarPromedioDonaciones();
                        break;
                    case 0:
                        salir = true;
                        System.out.println("Saliendo del sistema.");
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
                
                System.out.println("\nPresiona Enter para continuar...");
                scanner.nextLine();
            }
        } catch (Exception e) {
            System.err.println("Error en el cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void conectarServidor(String ipServidor, String nombreServidor) {
        try {
            int puerto = 1099; // Puerto por defecto para Servidor 1
            if (nombreServidor.equals(NOMBRES_SERVICIO[2])) {
             puerto = 1100; // Puerto para Servidor 2
            }

            Registry registry = LocateRegistry.getRegistry(ipServidor, puerto);
            servidor = (DonacionesInterfaz) registry.lookup(nombreServidor);
            Cliente.nombreServidor = nombreServidor;
        } catch (Exception e) {
            System.err.println("Error al conectar con " + nombreServidor + ": " + e.getMessage());
        }
    }
    
    private static void mostrarMenu() {
        System.out.println("\n===== SISTEMA DE DONACIONES =====");
        if (nombreEntidad != null) {
            System.out.println("Cliente: " + nombreEntidad);
            System.out.println("Servidor: " + NOMBRES_SERVICIO[servidorAsignado]);
        } else {
            System.out.println("No registrado");
            System.out.println("Servidor inicial: " + NOMBRES_SERVICIO[servidorActual]);
        }
        System.out.println("1. Registrarse");
        System.out.println("2. Realizar donación");
        System.out.println("3. Consultar total donado");
        System.out.println("4. Listar donantes");
        System.out.println("5. Ver top donantes");
        System.out.println("6. Consultar promedio de donaciones");
        System.out.println("0. Salir");
        System.out.print("Seleccione una opción: ");
    }
    
    private static void registrarEntidad() {
        if (nombreEntidad != null) {
            System.out.println("Ya estás registrado como: " + nombreEntidad);
            return;
        }
        
        try {
            System.out.print("Introduce nombre del cliente: ");
            String nombre = scanner.nextLine();

            //ID del servidor al que se conectará el cliente
            int idServidor = servidor.registrarCliente(nombre); // 1 o 2
            
            if (idServidor != 0) {
                nombreEntidad = nombre;
                servidorAsignado = idServidor;
                System.out.println("Registro exitoso como: " + nombreEntidad);
                System.out.println("Servidor asignado: " + servidorAsignado);

                // Si nos asignaron a un servidor diferente al actual, reconectamos
                if (servidorAsignado != servidorActual) {
                    System.out.println("Conectando al servidor asignado...");
                    conectarServidor(ipServidor, NOMBRES_SERVICIO[servidorAsignado]);
                }

            } else {
                System.out.println("Error en el registro. La entidad ya existe o no se pudo registrar.");
            }
        } catch (Exception e) {
            System.err.println("Error al registrar: " + e.getMessage());
        }
    }
    
    private static void realizarDonacion() {
        if (nombreEntidad == null) {
            System.out.println("Debes registrarte primero.");
            return;
        }
        
        try {
            System.out.print("Introduce cantidad a donar: ");
            double cantidad = Double.parseDouble(scanner.nextLine());
            
            if (cantidad <= 0) {
                System.out.println("La cantidad debe ser mayor que cero.");
                return;
            }
            
            if (servidor.donar(nombreEntidad, cantidad)) {
                System.out.println("Donación de " + cantidad + " realizada con éxito.");
            } else {
                System.out.println("Error al realizar la donación.");
            }
        } catch (Exception e) {
            System.err.println("Error al donar: " + e.getMessage());
        }
    }
    
    private static void consultarTotalDonado() {
        if (nombreEntidad == null){
            System.out.println("Debes registrarte primero.");
            return;
        }
        
        try {
            double total = servidor.consultarTotalDonado(nombreEntidad);
            System.out.println("Total donado en ambos servidores: " + total);
        } catch (RemoteException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private static void listarDonantes() {
        if (nombreEntidad == null) {
            System.out.println("Debes registrarte primero.");
            return;
        }
        
        try {
            List<String> donantes = servidor.listadoDonantes(nombreEntidad);
            
            if (donantes.isEmpty()) {
                System.out.println("No hay donantes registrados.");
            } else {
                System.out.println("Lista de donantes:");
                for (String donante : donantes) {
                    System.out.println("- " + donante);
                }
                System.out.println("Total de donantes: " + donantes.size());
            }
        } catch (RemoteException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Añadir este método para asegurar la conexión con el servidor asignado
    private static void asegurarConexionCorrecta(String ipServidor) {
        if (servidorAsignado != 0) {
            String nombreServicioAsignado = NOMBRES_SERVICIO[servidorAsignado];
            
            // Verificar si el servidor actual es el correcto
            if (nombreServidor == null || !nombreServicioAsignado.equals(nombreServidor)) {
                System.out.println("Reconectando con el servidor asignado: " + nombreServicioAsignado);
                conectarServidor(ipServidor, nombreServicioAsignado);
            }
        }
    }

    private static void verTopDonantes() {
        try {
            System.out.print("¿Cuántos top donantes desea ver? ");
            int cantidad = scanner.nextInt();
            scanner.nextLine(); // Consumir nueva línea
            
            List<String> topDonantes = servidor.obtenerTopDonantes(cantidad);
            
            System.out.println("\n===== TOP " + cantidad + " DONANTES =====");
            for (int i = 0; i < topDonantes.size(); i++) {
                System.out.println((i + 1) + ". " + topDonantes.get(i));
            }
            
            if (topDonantes.isEmpty()) {
                System.out.println("No hay donantes registrados aún.");
            }
        } catch (Exception e) {
            System.err.println("Error al obtener top donantes: " + e.getMessage());
        }
    }

    private static void consultarPromedioDonaciones() {
        try {
            double promedio = servidor.obtenerPromedioDonadoPorEntidad();
            System.out.println("\nPromedio de donaciones por entidad: " + promedio + "€");
        } catch (Exception e) {
            System.err.println("Error al consultar promedio: " + e.getMessage());
        }
    }

}