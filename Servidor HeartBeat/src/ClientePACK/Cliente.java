package ClientePACK;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public class Cliente {

    public static void main(String[] args) {
        
        int puerto =  5000 + (int)Math.floor(Math.random()*1000);
        InetSocketAddress addrSocket = new InetSocketAddress("localhost", puerto);

        System.out.println("Creando socket datagram emisor");
        DatagramSocket datagramSocket;
        
        try {
            datagramSocket = new DatagramSocket(addrSocket);

            /*He usado un for porque al usar un bucle while(true) cuando el cliente
            era expulsado por inactividad, volvía a conectarse ya que no paraba de
            enviar mensajes. Esto también le pasa al ejemplo heartBeat del foro
            No se si es un fallo o realmente debe comportarse así, pero nunca
            va a parar de volver a concetarse al server aunque este lo haya expulsado.
            Es decir, lo expulsará y se volverá a conectar en menos de 4 seg.
            Creo que así se logra ver de forma más clara el comportamiento del servidor.*/
            
            for (int i=0; i<10; i++) {
                //Envia mensajes cada 4 segundos como máximo
                //(Se ha reducido el intervalo para no esperar tanto)
                Thread.sleep((long) (Math.random() * 4000));
                
                /*Si aumentas el intervalo, verás que el servidor tira al cliente
                porque no recibe los mensajes en menos de 3.5 seg. Despues, el mismo
                cliente se volverá a enviar informacion al server y este lo detectará
                como nuevo cliente. Para que esto no sucediera tendría que emplearse 
                socket stream como en el ejercicio2.*/
                System.out.println("Enviando heartbeat...("+i+")");
                String mensaje = "estoy vivooo";

                InetAddress addr = InetAddress.getByName("localhost");
                DatagramPacket datagrama = new DatagramPacket(mensaje.getBytes(), mensaje.getBytes().length, addr, 5555);
                datagramSocket.send(datagrama);
            }
            System.out.println("Finalizado!");

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
