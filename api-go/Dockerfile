# Dockerfile para API Go
FROM golang:1.24-alpine as builder
WORKDIR /app
COPY . .
RUN go mod tidy && go build -o api-go main.go

FROM alpine:3.19
WORKDIR /app
COPY --from=builder /app/api-go ./api-go
COPY data ./data
EXPOSE 8080
CMD ["./api-go"]
