"""
Padrao-Img.py
---------------
Worker Python que:
  1. Consome mensagens da fila  'image.resize.request'
  2. Redimensiona a imagem para 1200x800 (center crop)
  3. Publica a imagem processada na fila de resposta indicada pelo Java
     via propriedade reply_to + correlation_id  (padrão RabbitMQ RPC)

Variáveis de ambiente:
  CLOUDAMQP_URL  – amqp://user:pass@host/vhost  (obrigatório)
"""
from dotenv import load_dotenv
load_dotenv()
import io
import os
import base64
import logging
import pika
from PIL import Image

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
log = logging.getLogger(__name__)



AMQP_URL      = os.environ["CLOUDAMQP_URL"]          
REQUEST_QUEUE = "image.resize.request"

TARGET_WIDTH  = 400
TARGET_HEIGHT = 400
JPEG_QUALITY  = 90




def resize_and_crop(data: bytes) -> bytes:
    image = Image.open(io.BytesIO(data))

    if image.mode != "RGB":
        image = image.convert("RGB")

    target_ratio = TARGET_WIDTH / TARGET_HEIGHT
    src_ratio    = image.width  / image.height

    if src_ratio > target_ratio:
        new_h = TARGET_HEIGHT
        new_w = int(image.width * TARGET_HEIGHT / image.height)
    else:
        new_w = TARGET_WIDTH
        new_h = int(image.height * TARGET_WIDTH / image.width)

    image = image.resize((new_w, new_h), Image.LANCZOS)

    left  = (new_w - TARGET_WIDTH)  // 2
    top   = (new_h - TARGET_HEIGHT) // 2
    image = image.crop((left, top, left + TARGET_WIDTH, top + TARGET_HEIGHT))

    output = io.BytesIO()
    image.save(output, format="JPEG", quality=JPEG_QUALITY, optimize=True)
    return output.getvalue()



def on_request(channel, method, props, body):
    """
    body      – bytes da imagem original (base64-encoded enviado pelo Java)
    reply_to  – nome da fila onde Java está aguardando resposta
    correlation_id – ID que o Java usa para casar requisição ↔ resposta
    """
    log.info("Mensagem recebida | correlation_id=%s", props.correlation_id)

    try:
        # Java envia a imagem como base64 para trafegar como texto no broker
        image_bytes = base64.b64decode(body)
        resized     = resize_and_crop(image_bytes)
        response    = base64.b64encode(resized)
        log.info("Imagem processada com sucesso | correlation_id=%s", props.correlation_id)
    except Exception as exc:
        log.error("Erro ao processar imagem: %s", exc)
        response = b""
        props.headers = {"error": str(exc)}

    channel.basic_publish(
        exchange="",
        routing_key=props.reply_to,
        properties=pika.BasicProperties(
            correlation_id=props.correlation_id,
            content_type="application/octet-stream",
            headers=getattr(props, "headers", None) or {},
        ),
        body=response,
    )

    channel.basic_ack(delivery_tag=method.delivery_tag)
    log.info("Resposta publicada | reply_to=%s", props.reply_to)



def main():
    params     = pika.URLParameters(AMQP_URL)
    connection = pika.BlockingConnection(params)
    channel    = connection.channel()

    channel.queue_declare(queue=REQUEST_QUEUE, durable=True)
    channel.basic_qos(prefetch_count=1)   
    channel.basic_consume(queue=REQUEST_QUEUE, on_message_callback=on_request)

    log.info("Worker aguardando mensagens na fila '%s'…", REQUEST_QUEUE)
    channel.start_consuming()


if __name__ == "__main__":
    main()