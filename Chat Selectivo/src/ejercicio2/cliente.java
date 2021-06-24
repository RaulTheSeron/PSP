package ejercicio2;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 * @author Anabel
 */
public class cliente implements Runnable {
    
    // Socket cliente
	private static Socket clientSocket = null;
	// Stream para enviar datos al servidor
	private static PrintStream os = null;
	// Stream para leer datos desde el servidor
	private static DataInputStream is = null;

	// Buffer para leer teclado
	private static BufferedReader inputLine = null;
	// Variable para indicar si aún sigo a la escucha del servidor
	private static boolean closed = false;

	public static void main(String[] args) throws IOException {

		// Puerto
		int portNumber = 5555;
		// Host
		String host = "127.0.0.1"; //localhost

		/*
		 * Abre un nuevo socket para conectar con el host mediante el puerto dado.  Abre input y output streams.
		 */
		try {
			clientSocket = new Socket(host, portNumber);
			inputLine = new BufferedReader(new InputStreamReader(System.in));
			os = new PrintStream(clientSocket.getOutputStream());
			is = new DataInputStream(clientSocket.getInputStream());
		} catch (UnknownHostException e) {
			System.err.println("No tengo conexión con el host: " + host);
		} catch (IOException e) {
			System.err.println("Error de comunicación con el host: "
					+ host);
		}

		/*
		 * Si la conexión ha ido bien, entonces creo un hilo para escuchar al servidor, y voy enviando mensajes al servidor mientras el hilo siga vivo.
		 */
		if (clientSocket != null && os != null && is != null) {
			try {

				// Crea el hilo que permanecerá a la escucha del servidor.
				new Thread(new cliente()).start();

				// Mientras que la conexión no se cierre, escribo en el os stream los datos que se van enviado al servidor, quitando espacios dobles.
				while (!closed) {
					os.println(inputLine.readLine().trim());
				}

				// Si closed es true, entonces el hilo que escucha al servidor ha terminado, y tengo que cerrar los streams y el socket
				os.close();
				is.close();
				clientSocket.close();
			} catch (IOException e) {
				System.err.println("IOException:  " + e);
			}
		}
	}

	/*
	 * EL hilo Cliente queda en escucha permanente del servidor, hasta recibir de este la cadena "*** chao"
	 * Mientras tanto, imprime todo lo que recibe.
	 */
	public void run() {
		/*
		 * El hilo termina cuando recibo "*** chao" desde el servidor
		 */
		String msg;
		try {

			// Mientras aparezca algo que leer desde el inputstream 
                        //conectado al servidor, muestro el mensaje y compruebo si debo salir
			while ((msg = is.readLine()) != null) {
				System.out.println(msg);
				if (msg.contains("*** chao"))
					break;
			}

			// Uso esta variable para indicar al programa principal que el hilo ha terminado
			closed = true;
		} catch (IOException e) {
			System.err.println("IOException:  " + e);
		}
	}
}