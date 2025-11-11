from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import math

app = FastAPI()


# Pydantic model for request validation
class BasicOperation(BaseModel):
    a: float
    b: float


@app.get("/health")
def health():
    return {"status": "ok"}


@app.get("/")
def root():
    return {"message": "SmartAPI up"}


@app.post("/math/add")
def add(operation: BasicOperation):
    """Add two numbers"""
    result = math.fsum([operation.a, operation.b])
    return {"operation": "addition", "a": operation.a, "b": operation.b, "result": result}


@app.post("/math/subtract")
def subtract(operation: BasicOperation):
    """Subtract b from a"""
    result = operation.a - operation.b
    return {"operation": "subtraction", "a": operation.a, "b": operation.b, "result": result}


@app.post("/math/multiply")
def multiply(operation: BasicOperation):
    """Multiply two numbers"""
    result = math.prod([operation.a, operation.b])
    return {"operation": "multiplication", "a": operation.a, "b": operation.b, "result": result}


@app.post("/math/divide")
def divide(operation: BasicOperation):
    """Divide a by b"""
    if operation.b == 0:
        raise HTTPException(status_code=400, detail="Cannot divide by zero")
    result = operation.a / operation.b
    return {"operation": "division", "a": operation.a, "b": operation.b, "result": result}
    