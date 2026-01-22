#!/bin/sh

# Shebang: indica que este script se debe ejecutar con 'sh' (Bourne shell).
set -e
# 'set -e' indica que el script debe terminar inmediatamente si CUALQUIER comando # devuelve un código de salida distinto de 0.
# Es decir, si algo falla (openssl, mkdir, nginx, etc.), el contenedor no seguirá
# ejecutando comandos "como si nada", sino que fallará de forma explícita.
# Esto es muy importante en entorno DevOps para detectar errores rápidamente.
CERT_DIR=/etc/nginx/certs
# Variable que guarda la ruta donde se almacenarán los certificados y la clave privada.
# En este caso usamos /etc/nginx/certs, que luego montaremos como volumen en Docker
# para poder persistir los certificados entre reinicios de contenedor.
KEY_FILE="$CERT_DIR/server.key"
# Ruta completa al fichero de clave privada del servidor (formato PEM).
# Esta clave NO debe compartirse, es la que se usa para descifrar la información
# cifrada por los clientes con la clave pública del certificado.
CRT_FILE="$CERT_DIR/server.crt"
# Ruta completa al fichero de certificado del servidor (formato X.509 en PEM).
# Este archivo contiene la clave pública y los datos del sujeto (CN, organización, etc.).
# Nginx lo usará junto con 'server.key' para establecer conexiones TLS.

# Comprobamos si NO existe la clave o NO existe el certificado. -f comprueba si un archivo existe y es un archivo r7 # [ ! -f "$KEY_FILE"] verdadero si NO existe la clave. [ ! -f "$CRT_FILE"] → verdadero si NO existe el certificado. # El operador 'I' (OR lógico) hace que entremos en el 'if' si falta cualquiera de los dos.
if [ ! -f "$KEY_FILE" ] || [ ! -f "$CRT_FILE" ]; then
  # Si falta la clave o el certificado, informamos por salida estándar.
  echo "[nginx] Certificado no encontrado, generando uno autofirmado..."
  # Creamos el directorio de certificados, por si no existe.
  # -p evita errores si el directorio ya existe (no pasa nada).
  mkdir -p "$CERT_DIR"
  # Ejecutamos 'openssl req' para generar un certificado autofirmado.
  # Desglose de opciones:
  # - 'req'        → genera una solicitud de certificado (CSR) o un certificado directamente.
  # - -x509        → en vez de generar solo un CSR, genera un certificado X.509 autofirmado.
  # - -nodes       → (no DES) no cifra la clave privada con contraseña. Esto es importante
  #                  en entornos automatizados: Nginx podrá leer la clave sin pedir passphrase.
  # - -days 365    → el certificado será válido durante 365 días.
  # - -newkey rsa:2048 → genera una nueva pareja de claves (privada/pública) RSA de 2048 bits.
  # - -keyout      → fichero donde guardar la clave privada generada.
  # - -out         → fichero donde guardar el certificado (clave pública + metadata).
  # - -subj        → define los campos del sujeto del certificado sin pedirlos de forma interactiva:
  #      /C=ES          → Country (país), ES = España
  #      /ST=Andalucia  → State/Province (Andalucía)
  #      /L=Castilleja de la Cuesta → Localidad (Castilleja de la Cuesta)
  #      /O=TicketLogger → Organización (nombre de la empresa/proyecto)
  #      /OU=DAW        → Unidad organizativa (por ejemplo, "Departamento DAW")
  #      /CN=localhost  → Common Name (FQDN para el que se emite el certificado).
  #                        Aquí usamos 'localhost' porque es para desarrollo local.

  openssl req -x509 -nodes -days 365 \
      -newkey rsa:2048 \
      -keyout "$KEY_FILE" \
      -out "$CRT_FILE" \
      -subj "/C=ES/ST=Andalucia/L=Castilleja de la Cuesta/O=TicketLogger/OU=DAW/CN=localhost"
else
  # Si ya existen tanto la clave como el certificado, no los regeneramos.
  # Esto es importante cuando usamos un volumen para /etc/nginx/certs:
  # así podemos mantener el mismo certificado entre reinicios de contenedor.
  echo "[nginx] Certificado ya existe, reutilizándolo."
fi

# Mensaje informativo de que vamos a lanzar Nginx.
echo "[nginx] Arrancando Nginx..."
# Lanzamos Nginx en primer plano.
#'nginx -g "daemon off;" desactiva el modo demonio (background) y mantiene
#Nginx en foreground, que es lo que Docker espera para mantener el contenedor vivo. El uso de 'exec' es muy importante:
#Reemplaza el proceso del shell (sh) por el proceso de Nginx.
#- Nginx pasa a ser el proceso PID 1 del contenedor.
#Señales como SIGTERM se envían directamente a Nginx, permitiendo
#que se pare correctamente cuando haces 'docker stop'.
exec nginx -g "daemon off;"