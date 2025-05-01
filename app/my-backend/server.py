from fastapi import FastAPI
from gradio_client import Client
from pydantic import BaseModel

app = FastAPI()

client = Client("olzhasstt/MediFlow")

class RequestData(BaseModel):
    message: str
    system_message: str
    max_tokens: int
    temperature: float
    top_p: float
    language_choice: str

@app.post("/ask-ai")
async def ask_ai(request_data: RequestData):
    result = client.predict(
        message=request_data.message,
        system_message=request_data.system_message,
        max_tokens=request_data.max_tokens,
        temperature=request_data.temperature,
        top_p=request_data.top_p,
        language_choice=request_data.language_choice,
        api_name="/predict"
    )
    return {"answer": result}

if __name__ == "__main__":
    uvicorn.run("app:app", host="0.0.0.0", port=8000, reload=True)
