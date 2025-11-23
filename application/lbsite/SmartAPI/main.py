from fastapi import FastAPI
from pydantic import BaseModel
from transformers import AutoTokenizer
import onnxruntime as ort
import numpy as np

app = FastAPI(title="ONNX Sentiment Analysis API")

# Load tokenizer from exported files
tokenizer = AutoTokenizer.from_pretrained("./")

# Load ONNX model
session = ort.InferenceSession("model.onnx")

# Pydantic model for input
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
    # Tokenize input
    inputs = tokenizer(req.text, return_tensors="np", truncation=True, padding=True)

    # Run ONNX inference
    logits = session.run(None, dict(inputs))[0][0]

    # Softmax to get probabilities
    exp = np.exp(logits)
    probs = exp / np.sum(exp)

    # Determine label
    label = "positive" if int(np.argmax(probs)) == 1 else "negative"

    return {
        "text": req.text,
        "sentiment": label,
        "confidence": float(np.max(probs))
    }
