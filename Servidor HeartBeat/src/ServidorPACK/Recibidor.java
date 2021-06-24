
package ServidorPACK;

import java.util.HashMap;

/**
 * Clase encargada de recibir los mensajes del servidor.
 * @author raul_
 */
public class Recibidor extends Thread {

    private HashMap<Integer, Long> conexiones;
    private int puerto;
    
    public Recibidor(HashMap<Integer, Long> conexiones,int puerto) {
        this.conexiones = conexiones;
        this.puerto = puerto;
    }

    @Override
    public void run() {
        recibirMensaje(puerto);
    }
    
    
    public synchronized void recibirMensaje(int puerto) {
        if (!conexiones.containsKey(puerto)) {
            System.out.println("Nuevo cliente conectado...");
            conexiones.put(puerto, System.currentTimeMillis());
        } else {
            System.out.println("Cliente actualizado...");
            conexiones.replace(puerto, System.currentTimeMillis());
        }
    }    
    
}
