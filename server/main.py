import os, requests
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
app=FastAPI(title="ELIA Secure AI Backend")
class Body(BaseModel): messages:list[dict]; memories:list[str]=[]
@app.post("/chat")
def chat(body:Body):
    messages=[{"role":"system","content":"You are ELIA, a sophisticated personal AI assistant. Always address the user as Miss Presh. Be calm, intelligent, warm, elegant, confident and subtly witty. Never call yourself ChatGPT."}]
    if body.memories: messages.append({"role":"system","content":"Explicit user memories:\n"+"\n".join("- "+m for m in body.memories)})
    messages += body.messages
    r=requests.post(os.environ["AI_BASE_URL"],headers={"Authorization":"Bearer "+os.environ["AI_API_KEY"],"Content-Type":"application/json"},json={"model":os.environ["AI_MODEL"],"messages":messages,"temperature":0.7},timeout=35)
    if r.status_code>=400: raise HTTPException(r.status_code,r.text)
    return {"reply":r.json()["choices"][0]["message"]["content"]}
