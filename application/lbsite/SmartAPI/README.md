# SmartAPI

Lightweight FastAPI service providing simple health and math endpoints.

## Requirements
- Python 3.12
- pip

## Run locally
```bash
# from application/lbsite/SmartAPI
python -m venv .venv && source .venv/bin/activate  # Windows: .venv\Scripts\activate
pip install -r requirements.txt
uvicorn main:app --reload --host 0.0.0.0 --port 8080
```

## Docker
```bash
# Build
DOCKER_IMAGE=${DOCKER_HUB_USERNAME:-balese}/smartapi:dev
docker build -t "$DOCKER_IMAGE" .

# Run
docker run --rm -p 8080:8080 "$DOCKER_IMAGE"
```

## Endpoints
- GET /            -> { "message": "SmartAPI up" }
- GET /health      -> { "status": "ok" }
- POST /math/add        { a, b }
- POST /math/subtract   { a, b }
- POST /math/multiply   { a, b }
- POST /math/divide     { a, b }  (400 on divide-by-zero)

Example:
```bash
curl -s http://localhost:8080/health
curl -s -X POST http://localhost:8080/math/add -H 'Content-Type: application/json' -d '{"a":2,"b":3}'
```

## CI/CD
- CI workflow: .github/workflows/smart-api-ci-tst.yml
- Image repo: docker.io/balese/smartapi (tags: latest, sha)
- Promotion: .github/workflows/promote-smart-api-ci-tst.yml updates infrastructure/helm/lbsite/values-tst.yaml with latest@<digest> for Argo CD sync.
