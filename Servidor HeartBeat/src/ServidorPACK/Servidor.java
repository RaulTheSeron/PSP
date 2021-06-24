package ServidorPACK;

/**
 * Servicio de control de estado de procesos remotos mediante heartbeat. 
 * La función de este servicio es controlar que sus clientes están activos, y 
 * detectar cuándo se han desactivado. Para ello, los clientes se registran en 
 * el servidor cuando arrancan. Una vez se han registrado, envian mensajes al 
 * servidor a intervalos de tiempo regulares (cada 10 segundos). Si al cabo de 
 * 20 segundos el servidor no ha recibido ningún mensaje por parte de un cliente,
 * lo considerará muerto y avisará por pantalla. 
 * 
 * El servidor almacena la información de los clientes, contando el tiempo que
 * ha pasado desde su último mensaje. Para ello utiliza una tabla hash, 
 * representada por un objeto de clase HashMap. Además, el servidor contiene dos
 * hilos de ejecución independientes.El hilo principal se encarga de escuchar por
 * el socket y recibir los "latidos" de los clientes.El hilo secundario monitoriza
 * la tabla de clientes, detectando cuando uno de estos ha muerto.
 * @author Raul Serrano Torres
 */


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class Servidor {

    public static void main(String[] args) {
        try {
            DatagramSocket datagramSocket;
            InetSocketAddress addr = new InetSocketAddress("localhost", 5555);

            //Creamos objeto que gestiona conexiones
            GestorConexiones conexiones = new GestorConexiones();

            //Arrancamos hilo que comprueba conexiones
            conexiones.start();

            System.out.println("Creando servidor heartbeat...");
            
            datagramSocket = new DatagramSocket(addr);

            while (true) {
                
                byte[] mensaje = new byte[25];

               
                DatagramPacket paquete = new DatagramPacket(mensaje, 25);
                datagramSocket.receive(paquete);

                //Llama a metodo de clase GestorConexiones, que a su vez
                //arrancará el hilo encargadod e recibir mensajes.
                conexiones.recibirMensaje(paquete.getPort());

            }

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}