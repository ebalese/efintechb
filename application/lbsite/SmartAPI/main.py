from fastapi import FastAPI
from pydantic import BaseModel
from transformers import pipeline

app = FastAPI(title="Sentiment Analysis API")

# Load a pre-trained sentiment analysis model
# This will download the model the first time it runs
sentiment_pipeline = pipeline("sentiment-analysis", model="distilbert-base-uncased-finetuned-sst-2-english")

# Request model
class TextRequest(BaseModel):
    text: str

@app.get("/health")
def health():
    return {"status": "ok"}

@app.get("/")
def root():
    return {"message": "SmartAPI up"}

@app.post("/sentiment")
def sentiment(req: TextRequest):
    """Return sentiment prediction for input text"""
    result = sentiment_pipeline(req.text)[0]  # returns [{'label': 'POSITIVE', 'score': 0.999}]
    return {"text": req.text, "sentiment": result['label'].lower(), "score": float(result['score'])}
