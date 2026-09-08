# Secure ELIA AI backend
The APK never contains a private model/API key. Deploy this backend on infrastructure you control, then put its `/chat` URL into ELIA > Settings.

Example environment variables:
AI_BASE_URL=https://YOUR-AI-PROVIDER/v1/chat/completions
AI_API_KEY=YOUR_PRIVATE_KEY
AI_MODEL=YOUR_MODEL

Run the example with Python 3.10+:
pip install fastapi uvicorn requests
uvicorn main:app --host 0.0.0.0 --port 8080
