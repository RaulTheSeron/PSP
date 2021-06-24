package ejercicio2;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Servicio de "operador telefónico". Este mantiene la comunicación con un grupo
 * de clientes, y ayuda a que los clientes puedan comunicarse entre sí sin 
 * necesidad de conocer su dirección IP. Para ello, cuando un cliente se conecta 
 * al servidor indica su identificador personal. Después,cuando envía un mensaje,
 * indica además el identificador personal del cliente al que va destinado. El 
 * servidor lo recoge y lo hace llegar al destinatario correcto.
 * 
 * Se utiliza un hilo dedicado para cada cliente,que escucha por un socket 
 * específico conectado a un cliente concreto. Por otro lado se utiliza una tabla
 * (hashmap) de clientes, que almacena el socket de cada cliente. Cuando el hilo
 * de un cliente recibe un mensaje, busca el socket del destinatario en la tabla 
 * y reenvía el mensaje a su destino.
 * @author raul_
 */

public class operador {

    // Socket servidor
    private static ServerSocket serverSocket = null;
    // Socket cliente
    private static Socket clientSocket = null;

    // Se establece un máximo de maxClientsCount clientes, y se crea un hilo para escuchar a cada cliente
    private static final int maxClientsCount = 10;

    //Mapa con las conexiones de los clientes
    private static HashMap<Integer, clientThread> clientes = new HashMap<Integer, clientThread>();

    public static void main(String args[]) {

        // Puerto
        int portNumber = 5555;

        // Abre el socket servidor en el puerto especificado (5555)
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            System.out.println(e);
        }

        //Dejamos el server arrancado indefinidamente.
        while (true) {
            try {
                // Por cada conexión nueva que acepto en el socket cliente. 
                // El método accept escucha conexiones y las acepta.
                clientSocket = serverSocket.accept();
                int i = 0;

                // Busco si existe algún hueco (=null) en la lista de hilos-cliente y si lo hay, entonces creo un hilo nuevo,
                // y le envío al hilo el identificador del socket que escucha la conexión y la lista de hilos-cliente.
                for (i = 0; i < maxClientsCount; i++) {
                    if (clientes.get(i) == null) {
                        clientes.put(i, new clientThread(clientSocket, clientes)); //Guardamos id y socket del cliente en el mapa
                        clientes.get(i).start();
                        break; //sale del for en cuando se rellena un espacio
                    }
                }

                // Si no quedan huecos en la lista de hilos-cliente, entonces el servidor está al máximo de capacidad.
                if (i == maxClientsCount) {
                    // Envío un mensaje informando al cliente de que ya no queda sitio en el servidor, y termino la conexión.
                    PrintStream os = new PrintStream(clientSocket.getOutputStream());
                    os.println("No queda sitio, lo siento.");
                    os.close();
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
}

/*
 * Hilo cliente. Este hilo abre los streams de entrada/salida para comunicarse, 
 * pregunta al usuario por su nombre, e informa a todos los demás clientes
 * conectados de que alguien nuevo se ha unido. Por cada mensaje que recibe,
 * lo reenvía a todos los clientes.
 */
class clientThread extends Thread {

    // Stream que recibe datos del cliente
    private DataInputStream is = null;

    // Stream que envía datos al cliente
    private PrintStream os = null;

    // Socket de conexión con el cliente
    private Socket clientSocket = null;

    //Mapa con ids de los clientes y su socket.
    private HashMap<Integer, clientThread> clientes;
    // Máximo de clientes permitidos
    private int maxClientsCount = 10;

    // Constructor del hilo, recibe el identificador de socket y la lista de hilos-cliente (usuarios conectados)
    public clientThread(Socket clientSocket, HashMap<Integer, clientThread> clientes) {
        this.clientSocket = clientSocket;
        this.clientes = clientes;

    }

    public void run() {
        int maxClientsCount = this.maxClientsCount;
        int id=0;
        int destinatario = -1;
        String line = "";
        boolean destinoValido = false;
        
        try {
            // Inicializa los streams, los conecta con el socket del cliente
            is = new DataInputStream(clientSocket.getInputStream());
            os = new PrintStream(clientSocket.getOutputStream());
            String name;
            os.println("Por favor, introduce tu nombre: ");
            name = is.readLine().trim();

            os.println("Bienvenido " + name + "!\nPara salir, escribe /quit.");
            
            synchronized (this) {
                // Informo a los demás clientes de que un usuario nuevo se ha conectado
                for (int i = 0; i < maxClientsCount; i++) {
                    if (this.clientes.get(i) != null && this.clientes.get(i) != this) {
                        clientes.get(i).os.println("\n" + name + " acaba de entrar.");
                    }
                    else if(this.clientes.get(i)==this){
                        id = i;
                    }
                }
            }

            //Bucle para elegir destinatario
            while (true) {

                if (this.clientes.size() == 1) {
                    /*El cliente deberá esperar a que se conecte alguien más.
                    Podrá escribir /quit, pero no tendrá efecto hasta que se conecte
                    alquien más y se lea la línea enviada. El problema que impide salir
                    aqui es que si el server queda esperando aqui un mensaje que al final no
                    llega (porque el cliente no escribe nada) no se le mostrará la lista
                    de posibles destinatarios cuando aparezca alguien nuevo.*/
                    os.println("Espere un momento " + name + ", aun no hay mas usuarios conectados");
                    Thread.sleep(5000);
                    
                } else {
                    os.println("Lista de destinatarios:");
                    Integer[] claves = clientes.keySet().toArray(new Integer[0]);

                    for (int i = 0; i < claves.length; i++) {
                        if (this.clientes.get(i) != null && this.clientes.get(i) != this) //Para que no muestre su propio id.
                        {
                            os.println("Id: " + claves[i] + ")");
                        }
                    }

                    os.println("Elija destinatario: ");
                    line = is.readLine();
                    try{
                        destinatario = Integer.parseInt(line);
                    }catch(Exception ex){
                       if(line.startsWith("/quit"))
                           break;
                       else
                           os.println("Destinatario invalido, intentelo de nuevo.");
                    }

                    //Comprueba que dicho destinatario existe
                    for (int i = 0; i < claves.length; i++) {
                        if (claves[i] == destinatario) {
                            destinoValido = true;
                        }
                    }
                }
                if (destinoValido == true) {
                    os.println("Conectando con destinatario...");
                    os.println("Para elegir uno nuevo, escriba /nuevo");
                    os.println("Conectado!");
                    
                    //Bucle para enviar mensajes al destinatario
                    while (true) {

                        line = is.readLine();

                        // Si el detinatario se sale... se ha salido el destinatario
                        synchronized(this){
                            if ((this.clientes.get(destinatario)==null)){
                                os.println("Se ha salido el destinatario!");
                                break;
                            }
                        }
                        
                        // Si recibo /quit o /nuevo , entonces salgo del bucle
                        // Con /quit, termina el hilo
                        // Con /nuevo, vuelve a la parte donde se elige destinatario.
                        if(line.startsWith("/quit") || line.startsWith("/nuevo")){
                            break;
                        }


                        //Envio mensaje al destinatario.
                        synchronized (this) {
                            this.clientes.get(destinatario).os.println(name +"("+id+")"+" dice: " + line);
                        }
                    }
                    destinoValido = false;
                    //Si se ha escrito /quit, terminamos el
                    if (line.startsWith("/quit")) {
                        break;
                    }
                }
            }

            //Si se ha escrito /quit y se ha salido del bucle
            // Informo a los demás usuarios de que estoy abandonando la sal
            synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (this.clientes.get(i) != null && this.clientes.get(i) != this) {
                        this.clientes.get(i).os.println("El usuario " + name + " (ID:" + destinatario + ")" + " ha dejado la sala de chat.");
                    }
                }
            }
            //Enviamos *** chao al cliente, lo cual lo hace salir
            os.println("*** chao " + name);

            /*
            * Antes de terminar, pongo null en mi posición de la lista de hilos-cliente, para indicar al programa principal
	    * que voy a dejar un hueco en la lista de hilos-cliente donde puede aceptar a un nuevo cliente.
             */
            synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (this.clientes.get(i) == this) {
                        this.clientes.remove(i);
                    }
                }
            }
            /*
            * Cierra streams de entrada/salida y el socket
             */
            is.close();
            os.close();
            clientSocket.close();
        } catch (IOException e) {
        } catch (InterruptedException ex) {
            Logger.getLogger(clientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
