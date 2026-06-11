# docs/API.md

## Endpoints da API

### 1. Upload de Imagem
- **Rota:** POST /api/images
- **Criado por:** Backend (aprovado por todos)
- **Envia:** FormData com campo "file"
- **Resposta:** { id: string, status: "processing" }

### 2. Status do Processamento  
- **Rota:** GET /api/images/{id}
- **Resposta:** { status: string, url: string | null }

### 3. Listar Imagens
- **Rota:** GET /api/images
- **Resposta:** [{ id, url, createdAt }]