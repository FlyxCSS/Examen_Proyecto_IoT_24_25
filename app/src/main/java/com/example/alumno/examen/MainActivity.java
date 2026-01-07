package com.example.alumno.examen;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // 2. JAVA
    static int ANYO[] = {2004, 2008, 2011, 2016, 2018};
    static String PARTIDO[] = {"PSOE", "PSOE", "PP", "PP", "PSOE"};
    static String PRESIDENTE[] = {"Zapatero", "Zapatero", "Rajoy", "Rajoy", "Sánchez"};

    // 4. ESCRITURA DE DATOS
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // 5. RECYCLERVIEW
    private Adaptador adaptador;

    // 7. MQTT
    private static final String BROKER = "tcp://broker.hivemq.com:1883";
    private static final String TOPIC = "practica/Useche";
    private MqttClient client;
    private MqttConnectOptions options;
    private boolean isReconnecting = false;
    int contadorMqtt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 2. JAVA b)
        Map<String,Eleccion> mapaElecciones = Eleccion.convertirListasAMap(ANYO, PARTIDO, PRESIDENTE);
        Log.d("JAVA b)", mapaElecciones.toString());

        // 3. ALGORITMO a)
        Map<String, Integer> añosPresidentes = new HashMap<>();
        for (String presidente : PRESIDENTE) { //Para cada presidente en PRESIDENTE
            //Si el presidente ya está en el mapa, sumamos 1 a su valor
            if (añosPresidentes.containsKey(presidente)) {
                añosPresidentes.put(presidente, añosPresidentes.get(presidente) + 1);
            } else { //Si no está, lo añadimos con valor 1
                añosPresidentes.put(presidente, 1);
            }
        }
        Log.d("ALGORITMO a)", añosPresidentes.toString());

        // 3. ALGORITMO b)
        Map<String, Integer> anyosGobierno = new HashMap<>();
        for (int i = 0; i < ANYO.length; i++) {
            String partido = PARTIDO[i];
            int inicio = ANYO[i];
            int fin;
            if (i < ANYO.length - 1) {
                fin = ANYO[i + 1];
            } else {
                fin = 2025;
            }
            int duracion = fin - inicio;
            if (anyosGobierno.containsKey(partido)) {
                anyosGobierno.put(partido, anyosGobierno.get(partido) + duracion);
            } else {
                anyosGobierno.put(partido, duracion);
            }
        }
        Log.d("ALGORITMO b)", anyosGobierno.toString());
        
        // 3. ALGORITMO b) (alternativa)
        Map<String, Integer> anyosGobierno2 = new HashMap<>();
        for (int i = 0; i < ANYO.length; i++) {
            int fin = (i < ANYO.length - 1) ? ANYO[i + 1] : 2025;
            anyosGobierno2.put(PARTIDO[i], anyosGobierno2.getOrDefault(PARTIDO[i], 0) + (fin - ANYO[i]));
        }
        Log.d("ALGORITMO b)", anyosGobierno2.toString());
        
        // 4. ESCRITURA DE DATOS
        for (Map.Entry<String, Eleccion> entry : mapaElecciones.entrySet()) {
            db.collection("Elecciones").document(entry.getKey()).set(entry.getValue());
        }

        // 5. RECYCLERVIEW
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        Query query = FirebaseFirestore.getInstance().collection("Elecciones");
        FirestoreRecyclerOptions<Eleccion> opciones = new FirestoreRecyclerOptions.Builder<Eleccion>().setQuery(query, Eleccion.class).build();
        adaptador = new Adaptador(opciones);
        recyclerView.setAdapter(adaptador);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 7. MQTT
        new Thread(() -> setupMQTT()).start();
    }

    // 5. RECYCLERVIEW
    @Override
    protected void onStart() {
        super.onStart();
        adaptador.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adaptador.stopListening();
    }

    // 7. MQTT
    private void setupMQTT() {
        try {
            // Generar un clientId único
            String clientId = MqttClient.generateClientId();
            client = new MqttClient(BROKER, clientId, null);

            // Configurar opciones de conexión
            options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);

            // Establecer el callback para manejar los eventos de MQTT
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.e("MQTT", "Conexión perdida: " + cause.getMessage());
                    reconnectMQTT();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // Log para depuración
                    Log.i("MQTT", "Mensaje recibido en el topic " + topic + ": " + message.toString());

                    // Obtener el mensaje como texto
                    final String receivedMessage = message.toString();

                    // Actualizar el EditText en el hilo principal
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Obtener el EditText por su ID y actualizar el texto
                            TextView mensaje = findViewById(R.id.mensaje);
                            mensaje.setText("Mensaje Recibido: " + receivedMessage);
                            TextView contador = findViewById(R.id.contador);
                            contadorMqtt++;
                            contador.setText("Contador: " + contadorMqtt);
                        }
                    });
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.i("MQTT", "Mensaje entregado");
                }
            });
            // Conectar al broker
            client.connect(options);
            Log.i("MQTT", "Conexión al broker MQTT exitosa.");
            // Suscribirse al topic
            client.subscribe(TOPIC);
        } catch (MqttException e) {
            Log.e("MQTT", "Error al conectar al broker: " + e.getMessage(), e);
            reconnectMQTT();
        }
    }

    private void reconnectMQTT() {
        if (!isReconnecting) {
            isReconnecting = true;
            try {
                if (!client.isConnected()) {
                    Log.i("MQTT", "Intentando reconectar...");
                    client.connect(options);
                    Log.i("MQTT", "Reconectado al broker MQTT.");
                }
            } catch (MqttException e) {
                Log.e("MQTT", "Error al reconectar: " + e.getMessage(), e);
                try {
                    Thread.sleep(2000); // Espera antes de intentar reconectar nuevamente
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                reconnectMQTT(); // Intentar reconectar de nuevo
            } finally {
                isReconnecting = false;
            }
        } else {
            Log.w("MQTT", "Ya hay una reconexión en progreso.");
        }
    }
}