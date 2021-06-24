package ServidorPACK;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.Timer;

/**
 * Clase encargada que gestionar las conexiones y comprobar que clientes están 
 * inactivos
 * @author raul_
 */
public class GestorConexiones extends Thread {

    private static HashMap<Integer, Long> conexiones;
    private Timer comprobador;

    //Constructor
    public GestorConexiones() {
        this.conexiones = new HashMap<Integer, Long>();

        //Comprueba conexiones cada 2 seg para agilizar el proceso
        comprobador = new Timer(2000, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                comprobarConexiones();
            }

        });

    }

    public void comprobarConexiones() {  
        Long currentTime = System.currentTimeMillis();
        Integer[] claves = conexiones.keySet().toArray(new Integer[0]);
        Long tiempoCliente;

        for (int i = 0; i < claves.length; i++) {
            tiempoCliente = conexiones.get(claves[i]);

            //Si lleva más de 3.5 seg sin enviar mensajes (de nuevo,para no esperar tanto)
            if (currentTime - tiempoCliente > 3000) {
                matarCliente(claves[i], i);
                System.out.println("El cliente " + i + " expulsado por inactividad");
            } else {
                System.out.println("El cliente " + i + " está vivo");
            }
        }

    }

    //Hilo que comprueba conexiones
    @Override
    public void run() {
        comprobador.start();
    }

    //Arranca hilo encargado de recibir mensajes
    public synchronized void recibirMensaje(int puerto) {
        Recibidor recibidor = new Recibidor(conexiones,puerto);
        recibidor.start();
    }

    public synchronized void matarCliente(int puerto, int i) {
        System.out.println("El cliente " + i + " está muerto");
        conexiones.remove(puerto);
    }

}
