from fastapi import FastAPI
from pydantic import BaseModel
from transformers import AutoTokenizer, AutoModelForSequenceClassification
import onnxruntime as ort
import numpy as np
import os

app = FastAPI(title="Sentiment Analysis with ONNX")

MODEL_NAME = "distilbert-base-uncased-finetuned-sst-2-english"
ONNX_PATH = "model.onnx"

# Only export the model to ONNX once (if not already done)
if not os.path.exists(ONNX_PATH):
    from transformers.onnx import export
    from transformers.utils import logging
    from pathlib import Path

    logging.set_verbosity_error()
    tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)
    model = AutoModelForSequenceClassification.from_pretrained(MODEL_NAME)

    onnx_path = Path(ONNX_PATH)
    export(preprocessor=tokenizer, model=model, config=model.config, opset=13, output=onnx_path)

# Load ONNX model and tokenizer
tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)
ort_session = ort.InferenceSession(ONNX_PATH)

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
    inputs = tokenizer(req.text, return_tensors="np", truncation=True, padding=True)
    outputs = ort_session.run(None, dict(inputs))
    scores = outputs[0][0]
    label_id = int(np.argmax(scores))
    label = "positive" if label_id == 1 else "negative"
    confidence = float(np.max(np.exp(scores) / np.sum(np.exp(scores))))
    return {"text": req.text, "sentiment": label, "confidence": confidence}
