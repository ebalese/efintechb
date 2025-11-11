from fastapi import FastAPI
from pydantic import BaseModel
from transformers import AutoTokenizer
import onnxruntime as ort
import numpy as np

app = FastAPI(title="Sentiment Analysis with ONNX")

ONNX_PATH = "model.onnx"

# Load tokenizer
MODEL_NAME = "distilbert-base-uncased-finetuned-sst-2-english"
tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)

# Load ONNX model
ort_session = ort.InferenceSession(ONNX_PATH)

class TextRequest(BaseModel):
    text: str

@app.post("/sentiment")
def sentiment(req: TextRequest):
    inputs = tokenizer(req.text, return_tensors="np", truncation=True, padding=True)
    outputs = ort_session.run(None, dict(inputs))
    scores = outputs[0][0]
    label_id = int(np.argmax(scores))
    label = "positive" if label_id == 1 else "negative"
    confidence = float(np.max(np.exp(scores) / np.sum(np.exp(scores))))
    return {"text": req.text, "sentiment": label, "confidence": confidence}
