# Use a base image
FROM registry:2

# Set up environment variables
ENV REGISTRY_HTTP_ADDR=0.0.0.0:443
ENV REGISTRY_HTTP_TLS_CERTIFICATE=/certs/domain.crt
ENV REGISTRY_HTTP_TLS_KEY=/certs/domain.key

# Expose the necessary port
EXPOSE 443

# Create a directory for storing certificates
RUN mkdir -p /certs

# Copy SSL certificates from the host to the container
# Assuming that domain.crt and domain.key are present in the same directory as Dockerfile
COPY ./certs/domain.crt /certs/domain.crt
COPY ./certs/domain.key /certs/domain.key
