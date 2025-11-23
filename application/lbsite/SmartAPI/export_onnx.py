import torch
from transformers import AutoTokenizer, AutoModelForSequenceClassification

MODEL = "distilbert-base-uncased-finetuned-sst-2-english"
tokenizer = AutoTokenizer.from_pretrained(MODEL)
model = AutoModelForSequenceClassification.from_pretrained(MODEL)

# Example input
text = "This is great!"
inputs = tokenizer(text, return_tensors="pt")

# Export to ONNX
torch.onnx.export(
    model,
    (inputs["input_ids"], inputs["attention_mask"]),
    "model.onnx",
    input_names=["input_ids", "attention_mask"],
    output_names=["logits"],
    dynamic_axes={
        "input_ids": {0: "batch", 1: "seq_len"},
        "attention_mask": {0: "batch", 1: "seq_len"},
        "logits": {0: "batch"}
    },
    opset_version=13
)

# Save tokenizer for runtime
tokenizer.save_pretrained(".")
model.config.save_pretrained(".")
print("ONNX export complete!")
