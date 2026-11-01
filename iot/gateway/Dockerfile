FROM python:3.12-slim
WORKDIR /srv
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY gateway/tradutor.py gateway/gateway.py ./
EXPOSE 9101
CMD ["python", "-u", "gateway.py"]
