FROM python:3.8-slim

RUN pip install confluent_kafka

WORKDIR /app

COPY ./consumer.py /app/consumer.py

EXPOSE 9092

CMD ["python", "consumer.py"]