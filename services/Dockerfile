# Worker de imagem: consome image.resize.request, redimensiona 400x400, responde via RPC.
FROM python:3.12-slim
WORKDIR /app
ENV PYTHONUNBUFFERED=1
RUN pip install --no-cache-dir pika Pillow python-dotenv
COPY Padrao-Img.py ./
CMD ["python", "Padrao-Img.py"]
