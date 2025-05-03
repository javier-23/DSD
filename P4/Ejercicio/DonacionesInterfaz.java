// Declaración de la Interfaz

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface DonacionesInterfaz extends Remote {
    
    // Método para registrar un cliente
    int registrarCliente(String nombreEntidad) throws RemoteException;

    // Realiza una donación
    boolean donar(String nombreEntidad, double cantidad) throws RemoteException;
  
    //Consultas
    double consultarTotalDonado(String nombreEntidad) throws RemoteException;
    List<String> listadoDonantes(String nombreEntidad) throws RemoteException;

    // Métodos para comunicación entre servidores
    boolean verificarEntidadRegistrada(String nombreEntidad) throws RemoteException;
    int getNumeroEntidades() throws RemoteException;
    void sincronizarRegistro(String nombreEntidad) throws RemoteException;
    double getSubtotalLocal() throws RemoteException;
    List<String> getDonantes() throws RemoteException;
    int getIdServidor() throws RemoteException;

    // Métodos para exclusión mutua distribuida
    void solicitarAcceso(int relojSolicitante, int idServidor) throws RemoteException;
    void liberarAcceso(int relojLiberacion, int idServidor) throws RemoteException;
    void responderSolicitud(int relojRespuesta) throws RemoteException;

}