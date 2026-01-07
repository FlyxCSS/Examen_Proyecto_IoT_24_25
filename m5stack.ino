#include <WiFi.h>
#include <PubSubClient.h>

// Configuración WiFi
const char* ssid = "...";           // Nombre de la red WiFi
const char* password = "...";         // Contraseña de la red WiFi

// Configuración MQTT
const char* mqtt_broker = "broker.hivemq.com";  // Dirección del broker MQTT
const char* topic = "examen/Alumno";  // Tópico donde publicaremos los valores
WiFiClient espClient;                          // Cliente WiFi
PubSubClient client(espClient);                // Cliente MQTT
int contador = 1;                              // Variable para controlar el número a publicar
unsigned long previousMillis = 0;            // Temporizador para actualizaciones
const long interval = 3000;

// Conexión al Wifi
void setup_wifi() {
    delay(10);
    Serial.print("Conectando a WiFi...");
    WiFi.begin(ssid, password);
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print(".");
    }
    Serial.println("\nConexión a WiFi exitosa");
    Serial.print("Dirección IP asignada: ");
    Serial.println(WiFi.localIP());
}

// Reconexión al broker MQTT si se desconecta
void reconnect() {
    while (!client.connected()) {
        Serial.print("Intentando conectar al broker MQTT...");
        if (client.connect("M5StackClient")) {   // Conectar al broker con ID del cliente
            Serial.println("Conexión exitosa");
        } else {
            Serial.print("Error: ");
            Serial.println(client.state());
            delay(5000);  // Intentar de nuevo después de 5 segundos
        }
    }
}
void setup() {
    M5.begin();                             // Inicializa el M5Stack
    Serial.begin(115200);                   // Inicializa la comunicación serial
    setup_wifi();                           // Conecta al WiFi
    client.setServer(mqtt_broker, 1883);    // Configura el broker MQTT

    // Configuración inicial de la pantalla
    M5.Lcd.setTextSize(2);
    M5.Lcd.fillScreen(BLACK);               // Limpiar la pantalla al inicio
    M5.Lcd.setCursor(10, 10);
    M5.Lcd.setTextColor(WHITE);             // Color del título
    M5.Lcd.print("Examen IoT 2024 - Publicar valor:");
}

void loop() {
    if (!client.connected()) {              // Verificar conexión con el broker MQTT
        reconnect();                          // Intentar reconectar si es necesario
    }
    client.loop();                          // Procesar los mensajes entrantes de MQTT
    M5.update();                            // Actualizar el estado del botón
    // Con un intervalo de 3 segundos
    if (millis() - previousMillis >= interval) {
        previousMillis = millis();
        String payload = String(contador);
        // Publicar el valor en el tópico
        if (client.publish(topic, payload.c_str())) {
            Serial.print("Publicando: ");
            Serial.println(payload);  // Mostrar el valor publicado en el monitor serial
        }
        // Incrementar el contador para la siguiente pulsación
        contador++;
        // Mostrar el número en la pantalla
        M5.Lcd.fillRect(10, 100, 300, 30, BLACK);  // Limpiar la parte de la pantalla donde se muestra el número
        M5.Lcd.setCursor(10, 100);
        M5.Lcd.setTextColor(GREEN, BLACK);
        M5.Lcd.print("Valor publicado: ");
        M5.Lcd.print(contador - 1);  // Mostrar el valor que se publicó
        delay(500);  // Pausar para evitar múltiples lecturas rápidas del botón
    }
}