import requests

MELHOR_ENVIO_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI5ODk3IiwianRpIjoiNGU4MzZhNmRlNWIyYzA2MDk1NzIzZDIyNTY3NDFlZGFkODUyMGEwMjdiNTRlZDg1YTY5ZGU3MWZiNGM3MjYwMjRkOWM2OGMxNDgzZjllMzkiLCJpYXQiOjE3ODE1NjcwNTkuMTc4NDE0LCJuYmYiOjE3ODE1NjcwNTkuMTc4NDE3LCJleHAiOjE3ODQxNTkwNTkuMTUzNzkxLCJzdWIiOiJhMjA4NGYwYy05YjZjLTQ4ZmUtOTg0Yy1mMzM1MzIwZDIxMDYiLCJzY29wZXMiOlsic2hpcHBpbmctY2FsY3VsYXRlIl19.gOi1-Fvena9RlyRTckmOAOkuMcweFPNzKxvugXASM2Oy2UNlDXByIN-FmaG-PlHfiMrivDujwEenBlaHivbpBsSNKy1-Uc-hY5xcNWTfB7U_nznY_DHO0b5P61I97IkoYSmhW07cZzKgKBPThKcNFhz0ArYjdBeMFhqyi1deXnLd-8Bs-KxIpIW1wzGGamcYXIchUQxWoiqL1yZL9WV-ugNKA6jJk-04GSl0h8vgCmvFRjwQEB7tY1OPKh-0qKkM7GYGNvsq2SVdLDxqjmHUUpibTZFIbL9PljUIOjlLut8u5thK_Czhzn2xWYvClg2xO84QBirVwlEC-OUY9Xnu1cKpCIQ5XRuBrbUGo2XAStqnW0XS4HfF_k_ab3f8Kwd2tOh4K6qRnZxcK_Yn62s3if6FVM77W1c1I-8NCGhHWuMG23dWksLf56KmBqFFVvXMHYZHUB5EQBm6RFcYLvtwm4kbAkbh5b1NfOy37w-Ras5f3sMZnRj00K0eivRf25iMhTHRMBDjfFaNZwiu6aMtVHqeUhEqarjrBSFg3Fgx_52blG_ObUUrBrEVxnWNNn5ezJRkViUK4nO3_p8HEsDSxfajyYK7Cp7s7vnxOEHdCOMRDNf4qmIwI2dpLc1ulualJ1IlkChOErGyGK3NfsyDvz7E_G8vXSXIup5YkJZLusw"
BASE_URL = "https://sandbox.melhorenvio.com.br/api/v2/me/shipment/calculate"

def calcular_frete(remetente: dict, destinatario: dict, produtos: list) ->list:
    """
    Calcula o frete entre remetente e destinatário.

    remetente e destinatario precisam ter pelo menos:
        - postal_code: CEP (string, ex: "59600000")

    Cada produto na lista deve ter:
        - id, width, height, length (cm), weight (kg), insurance_value, quantity
    """
    headers = {
        "Accept": "application/json",
        "Content-Type": "application/json",
        "Authorization": f"Bearer {MELHOR_ENVIO_TOKEN}",
        "User-Agent": "Marketplace Exemplo (contato@exemplo.com)",
    }

    payload = {
        "from": {"postal_code": remetente["postal_code"]},
        "to": {"postal_code": destinatario["postal_code"]},
        "products": produtos,
    }

    response = requests.post(BASE_URL, json=payload, headers=headers)
    response.raise_for_status()

    resultados = response.json()

    disponiveis = [r for r in resultados if "error" not in r]

    disponiveis.sort(key=lambda x: x.get("custom_price", x.get("price", 0)))

    return disponiveis

def exibir_cotacoes(cotacoes: list):
    for c in cotacoes:
        nome = c.get("name", "Desconhecido")
        preco = c.get("custom_price", c.get("delivery_time", "N/A"))
        prazo = c.get("custom_delivery_time", c.get("delivery_time", "N/A)"))
        print(f"{nome}: R$ {preco} | Prazo: {prazo} dias úteis")


if __name__ == "__main__":
    remetente = {"postal_code": "01310100"}

    destinatario = {"postal_code": "20040020"}
    produtos = [
        {
            "id": "produto-001",
            "width": 20,
            "height": 10,
            "length": 30,
            "weight": 1.5,
            "insurance_value": 150.00,
            "quantity": 1,
        }
    ]

    cotacoes = calcular_frete(remetente, destinatario, produtos)
    exibir_cotacoes(cotacoes)