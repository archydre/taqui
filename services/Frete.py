"""
Frete.py

Worker Python que:
  1. Consome mensagens da fila  'freight.calculate.request'
  2. Chama a API do Melhor Envio (sandbox ou produção)
  3. Devolve a lista de cotações em JSON na reply queue do Java
     (padrão RabbitMQ RPC — reply_to + correlation_id)

Variáveis de ambiente:
  CLOUDAMQP_URL         – amqp://user:pass@host/vhost  (obrigatório)
  MELHOR_ENVIO_TOKEN    – Bearer token do Melhor Envio  (obrigatório)
  MELHOR_ENVIO_ENV      – 'sandbox' ou 'production'     (padrão: sandbox)
"""
from dotenv import load_dotenv
load_dotenv()
import json
import logging
import os
import requests
import pika

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
log = logging.getLogger(__name__)



AMQP_URL       = os.environ["CLOUDAMQP_URL"]
TOKEN          = os.environ["MELHOR_ENVIO_TOKEN"]
ENV            = os.environ.get("MELHOR_ENVIO_ENV", "sandbox")
REQUEST_QUEUE  = "freight.calculate.request"

BASE_URLS = {
    "sandbox":    "https://sandbox.melhorenvio.com.br/api/v2/me/shipment/calculate",
    "production": "https://melhorenvio.com.br/api/v2/me/shipment/calculate",
}
API_URL = BASE_URLS[ENV]

HEADERS = {
    "Accept": "application/json",
    "Content-Type": "application/json",
    "Authorization": f"Bearer {TOKEN}",
    "User-Agent": "Taqui Marketplace (contato@taqui.com.br)",
}




def calcular_frete(remetente: dict, destinatario: dict, produtos: list) -> list:
    """
    Chama a API do Melhor Envio e retorna todas as transportadoras disponíveis,
    ordenadas da mais barata para a mais cara.

    remetente / destinatario precisam ter:
        postal_code (str) – ex: "59600000"

    Cada produto precisa ter:
        id, width, height, length (cm), weight (kg),
        insurance_value (R$), quantity
    """
    payload = {
        "from":     {"postal_code": remetente["postal_code"]},
        "to":       {"postal_code": destinatario["postal_code"]},
        "products": produtos,
    }

    response = requests.post(API_URL, json=payload, headers=HEADERS, timeout=15)
    response.raise_for_status()

    resultados = response.json()

    
    disponiveis = [r for r in resultados if "error" not in r]

    
    disponiveis.sort(key=lambda x: float(x.get("custom_price") or x.get("price") or 0))

    return disponiveis




def on_request(channel, method, props, body):

    correlation_id = props.correlation_id
    log.info("Requisição de frete recebida | correlation_id=%s", correlation_id)

    error_header = {}

    try:
        data         = json.loads(body)
        remetente    = data["remetente"]
        destinatario = data["destinatario"]
        produtos     = data["produtos"]

        cotacoes  = calcular_frete(remetente, destinatario, produtos)
        response  = json.dumps(cotacoes, ensure_ascii=False).encode()
        log.info("%d cotações encontradas | correlation_id=%s", len(cotacoes), correlation_id)

    except requests.HTTPError as exc:
        log.error("Erro na API do Melhor Envio: %s", exc)
        response     = b"[]"
        error_header = {"error": f"Melhor Envio API error: {exc.response.status_code}"}

    except (KeyError, json.JSONDecodeError) as exc:
        log.error("Payload inválido: %s", exc)
        response     = b"[]"
        error_header = {"error": f"Payload inválido: {exc}"}

    except Exception as exc:
        log.error("Erro inesperado: %s", exc)
        response     = b"[]"
        error_header = {"error": str(exc)}

    channel.basic_publish(
        exchange="",
        routing_key=props.reply_to,
        properties=pika.BasicProperties(
            correlation_id=correlation_id,
            content_type="application/json",
            headers=error_header or {},
        ),
        body=response,
    )

    channel.basic_ack(delivery_tag=method.delivery_tag)
    log.info("Resposta publicada | reply_to=%s", props.reply_to)


# ── Inicialização ─────────────────────────────────────────────────────────────

def main():
    params     = pika.URLParameters(AMQP_URL)
    connection = pika.BlockingConnection(params)
    channel    = connection.channel()

    channel.queue_declare(queue=REQUEST_QUEUE, durable=True)
    channel.basic_qos(prefetch_count=1)
    channel.basic_consume(queue=REQUEST_QUEUE, on_message_callback=on_request)

    log.info("Freight worker aguardando mensagens na fila '%s' [%s]…", REQUEST_QUEUE, ENV)
    channel.start_consuming()


if __name__ == "__main__":
    main()