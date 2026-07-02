import json
import logging
import os
import smtplib
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText

import pika
from dotenv import load_dotenv
load_dotenv()

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
log = logging.getLogger(__name__)

AMQP_URL         = os.environ["CLOUDAMQP_URL"]
GMAIL_USER       = os.environ["GMAIL_USER"]
GMAIL_PASSWORD   = os.environ["GMAIL_APP_PASSWORD"]
BASE_URL         = os.environ.get("BASE_URL", "http://localhost:3000")
REQUEST_QUEUE      = "email.verify.request"
NOTIFICATION_QUEUE = "notification.email.request"

def build_email_html(nome: str, link: str) -> str:
    return f"""
<!DOCTYPE html>
<html lang="pt-BR">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Verificação de Email – TAQUI</title>
</head>
<body style="margin:0;padding:0;background-color:#f4f4f4;font-family:Arial,sans-serif;">
 
  <table width="100%" cellpadding="0" cellspacing="0" style="background-color:#f4f4f4;padding:40px 0;">
    <tr>
      <td align="center">
        <table width="600" cellpadding="0" cellspacing="0"
               style="background-color:#ffffff;border-radius:8px;overflow:hidden;
                      box-shadow:0 2px 8px rgba(0,0,0,0.08);">
 
          <!-- Cabeçalho -->
          <tr>
            <td align="center"
                style="background-color:#1a1a2e;padding:32px 40px;">
              <h1 style="color:#ffffff;margin:0;font-size:28px;letter-spacing:2px;">
                TAQUI
              </h1>
              <p style="color:#a0a0b0;margin:6px 0 0;font-size:13px;">
                Marketplace
              </p>
            </td>
          </tr>
 
          <!-- Corpo -->
          <tr>
            <td style="padding:40px 40px 24px;">
              <h2 style="color:#1a1a2e;margin:0 0 12px;font-size:22px;">
                Olá, {nome}! 👋
              </h2>
              <p style="color:#444;font-size:15px;line-height:1.6;margin:0 0 24px;">
                Obrigado por se cadastrar no <strong>TAQUI</strong>.<br/>
                Clique no botão abaixo para confirmar seu endereço de email
                e ativar sua conta.
              </p>
 
              <!-- Botão -->
              <table cellpadding="0" cellspacing="0">
                <tr>
                  <td align="center"
                      style="background-color:#4f46e5;border-radius:6px;">
                    <a href="{link}"
                       style="display:inline-block;padding:14px 32px;
                              color:#ffffff;font-size:15px;font-weight:bold;
                              text-decoration:none;letter-spacing:0.5px;">
                      ✅ Verificar meu email
                    </a>
                  </td>
                </tr>
              </table>
 
              <p style="color:#888;font-size:13px;margin:24px 0 0;line-height:1.6;">
                Se o botão não funcionar, copie e cole o link abaixo no navegador:
              </p>
              <p style="word-break:break-all;">
                <a href="{link}"
                   style="color:#4f46e5;font-size:13px;">{link}</a>
              </p>
 
              <hr style="border:none;border-top:1px solid #eee;margin:32px 0;"/>
 
              <p style="color:#aaa;font-size:12px;margin:0;line-height:1.6;">
                Este link expira em <strong>24 horas</strong>.<br/>
                Se você não criou uma conta no TAQUI, ignore este email.
              </p>
            </td>
          </tr>
 
          <!-- Rodapé -->
          <tr>
            <td align="center"
                style="background-color:#f9f9f9;padding:20px 40px;
                       border-top:1px solid #eee;">
              <p style="color:#bbb;font-size:12px;margin:0;">
                © 2025 TAQUI Marketplace · Todos os direitos reservados
              </p>
            </td>
          </tr>
 
        </table>
      </td>
    </tr>
  </table>
 
</body>
</html>
"""

def send_verification_email(dest_email: str, nome: str, token: str):
    link = f"{BASE_URL}/verify?token={token}"

    msg = MIMEMultipart("alternative")
    msg["Subject"] = "Confirme seu email - TAQUI"
    msg["From"]    = GMAIL_USER
    msg["To"]      = dest_email

    html_part = MIMEText(build_email_html(nome, link), "html", "utf-8")
    msg.attach(html_part)

    with smtplib.SMTP_SSL("smtp.gmail.com", 465) as server:
        server.login(GMAIL_USER, GMAIL_PASSWORD)
        server.sendmail(GMAIL_USER, dest_email, msg.as_string())

    log.info("Email enviado para %s | link=%s", dest_email, link)

def build_notification_html(nome: str, subject: str, body: str) -> str:
    link = f"{BASE_URL}/notificacoes"
    return f"""
<!DOCTYPE html>
<html lang="pt-BR">
<head><meta charset="UTF-8" /><title>{subject} – TAQUI</title></head>
<body style="margin:0;padding:0;background-color:#f4f4f4;font-family:Arial,sans-serif;">
  <table width="100%" cellpadding="0" cellspacing="0" style="background-color:#f4f4f4;padding:40px 0;">
    <tr><td align="center">
      <table width="600" cellpadding="0" cellspacing="0"
             style="background-color:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
        <tr><td align="center" style="background-color:#1a1a2e;padding:32px 40px;">
          <h1 style="color:#ffffff;margin:0;font-size:28px;letter-spacing:2px;">TAQUI</h1>
          <p style="color:#a0a0b0;margin:6px 0 0;font-size:13px;">Marketplace</p>
        </td></tr>
        <tr><td style="padding:40px 40px 24px;">
          <h2 style="color:#1a1a2e;margin:0 0 12px;font-size:22px;">Olá, {nome}! 👋</h2>
          <p style="color:#444;font-size:16px;line-height:1.6;margin:0 0 24px;"><strong>{body}</strong></p>
          <table cellpadding="0" cellspacing="0"><tr>
            <td align="center" style="background-color:#4f46e5;border-radius:6px;">
              <a href="{link}" style="display:inline-block;padding:14px 32px;color:#ffffff;font-size:15px;font-weight:bold;text-decoration:none;letter-spacing:0.5px;">Ver no taqui</a>
            </td>
          </tr></table>
        </td></tr>
        <tr><td align="center" style="background-color:#f9f9f9;padding:20px 40px;border-top:1px solid #eee;">
          <p style="color:#bbb;font-size:12px;margin:0;">© TAQUI Marketplace</p>
        </td></tr>
      </table>
    </td></tr>
  </table>
</body>
</html>
"""

def send_notification_email(dest_email: str, nome: str, subject: str, body: str):
    msg = MIMEMultipart("alternative")
    msg["Subject"] = subject
    msg["From"]    = GMAIL_USER
    msg["To"]      = dest_email
    msg.attach(MIMEText(build_notification_html(nome, subject, body), "html", "utf-8"))

    with smtplib.SMTP_SSL("smtp.gmail.com", 465) as server:
        server.login(GMAIL_USER, GMAIL_PASSWORD)
        server.sendmail(GMAIL_USER, dest_email, msg.as_string())

    log.info("Email de notificação enviado para %s | assunto=%s", dest_email, subject)

def on_notification(channel, method, props, body):
    # Fire-and-forget: sem reply. Best-effort — em erro loga e dá ack (não reenfileira o
    # veneno); perder um email de notificação é aceitável.
    try:
        data = json.loads(body)
        send_notification_email(
            data["email"],
            data.get("nome", "usuário"),
            data.get("subject", "Você tem uma novidade no taqui"),
            data.get("body", ""),
        )
    except Exception as exc:
        log.error("Erro ao enviar email de notificação: %s", exc)

    channel.basic_ack(delivery_tag=method.delivery_tag)


def on_request(channel, method, props, body):

    correlation_id = props.correlation_id
    log.info("Requisição de verificação recebida | correlation_id=%s", correlation_id)

    error_header = {}

    try:
        data   = json.loads(body)
        email  = data["email"]
        nome   = data.get("nome", "usuário")
        token  = data["token"]

        send_verification_email(email, nome, token)

        response = json.dumps({"status": "sent", "email": email}).encode()

    except Exception as exc:
        log.error("Error ao enviar email: %s", exc)
        response       = json.dumps({"status": "error"}).encode()
        error_header   = {"error": str(exc)}

    channel.basic_publish(
        exchange="",
        routing_key=props.reply_to,
        properties=pika.BasicProperties(
            correlation_id=correlation_id,
            content_type="application/json",
            headers=error_header or {}
        ),
        body=response,
    )

    channel.basic_ack(delivery_tag=method.delivery_tag)
    log.info("Confirmação publicada | reply_to=%s", props.reply_to)


def main():
    params     = pika.URLParameters(AMQP_URL)
    connection = pika.BlockingConnection(params)
    channel    = connection.channel()

    channel.queue_declare(queue=REQUEST_QUEUE, durable=True)
    channel.queue_declare(queue=NOTIFICATION_QUEUE, durable=True)
    channel.basic_qos(prefetch_count=1)
    channel.basic_consume(queue=REQUEST_QUEUE, on_message_callback=on_request)
    channel.basic_consume(queue=NOTIFICATION_QUEUE, on_message_callback=on_notification)

    log.info("Email worker aguardando em '%s' e '%s'.....", REQUEST_QUEUE, NOTIFICATION_QUEUE)
    channel.start_consuming()

if __name__ == "__main__":
    main()