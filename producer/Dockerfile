FROM python:3.8-slim
RUN pip install confluent_kafka
WORKDIR /app
COPY producer.py .
EXPOSE 9092
CMD ["python", "producer.py"]