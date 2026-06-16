import requests

TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI5ODk3IiwianRpIjoiNGU4MzZhNmRlNWIyYzA2MDk1NzIzZDIyNTY3NDFlZGFkODUyMGEwMjdiNTRlZDg1YTY5ZGU3MWZiNGM3MjYwMjRkOWM2OGMxNDgzZjllMzkiLCJpYXQiOjE3ODE1NjcwNTkuMTc4NDE0LCJuYmYiOjE3ODE1NjcwNTkuMTc4NDE3LCJleHAiOjE3ODQxNTkwNTkuMTUzNzkxLCJzdWIiOiJhMjA4NGYwYy05YjZjLTQ4ZmUtOTg0Yy1mMzM1MzIwZDIxMDYiLCJzY29wZXMiOlsic2hpcHBpbmctY2FsY3VsYXRlIl19.gOi1-Fvena9RlyRTckmOAOkuMcweFPNzKxvugXASM2Oy2UNlDXByIN-FmaG-PlHfiMrivDujwEenBlaHivbpBsSNKy1-Uc-hY5xcNWTfB7U_nznY_DHO0b5P61I97IkoYSmhW07cZzKgKBPThKcNFhz0ArYjdBeMFhqyi1deXnLd-8Bs-KxIpIW1wzGGamcYXIchUQxWoiqL1yZL9WV-ugNKA6jJk-04GSl0h8vgCmvFRjwQEB7tY1OPKh-0qKkM7GYGNvsq2SVdLDxqjmHUUpibTZFIbL9PljUIOjlLut8u5thK_Czhzn2xWYvClg2xO84QBirVwlEC-OUY9Xnu1cKpCIQ5XRuBrbUGo2XAStqnW0XS4HfF_k_ab3f8Kwd2tOh4K6qRnZxcK_Yn62s3if6FVM77W1c1I-8NCGhHWuMG23dWksLf56KmBqFFVvXMHYZHUB5EQBm6RFcYLvtwm4kbAkbh5b1NfOy37w-Ras5f3sMZnRj00K0eivRf25iMhTHRMBDjfFaNZwiu6aMtVHqeUhEqarjrBSFg3Fgx_52blG_ObUUrBrEVxnWNNn5ezJRkViUK4nO3_p8HEsDSxfajyYK7Cp7s7vnxOEHdCOMRDNf4qmIwI2dpLc1ulualJ1IlkChOErGyGK3NfsyDvz7E_G8vXSXIup5YkJZLusw"

headers = {
    "Accept": "application/json",
    "Content-Type": "application/json",
    "Authorization": f"Bearer {TOKEN}",
    "User-Agent": "TAQUI (guilherme.pereira.dantas39@gmail.com)",
}

payload = {
    "from": {"postal_code": "01310100"},
    "to": {"postal_code": "59600000"},
    "products": [
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
}

response = requests.post(
    "https://sandbox.melhorenvio.com.br/api/v2/me/shipment/calculate",
    json=payload,
    headers=headers
)

print(response.json())