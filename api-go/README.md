# API Go - Ejemplo CRUD básico

Autor: Juan Pablo Galeano Salguero

## Descripción

Esta API está desarrollada en Go utilizando el framework Gin. Permite consultar información de clientes y productos, cuyos datos se almacenan en archivos JSON dentro del proyecto.

## Estructura del proyecto

```
api-go/
├── data/
│   ├── customers.json
│   └── products.json
├── handlers/
│   ├── customer_handler.go
│   ├── product_handler.go
│   ├── customer_handler_test.go
│   ├── product_handler_test.go
│   └── error_handlers_test.go
├── models/
│   ├── customer.go
│   ├── product.go
│   └── models_test.go
├── utils/
│   ├── json_loader.go
│   └── json_loader_test.go
├── main.go
├── go.mod
├── go.sum
```

## Endpoints

- `GET /customers/:id` - Obtiene un cliente por ID
- `GET /products/:id` - Obtiene un producto por ID

## Ejecución

1. Instala las dependencias:

```sh
go mod tidy
```

2. Ejecuta la API:

```sh
go run main.go
```

La API estará disponible en `http://localhost:8080`.

## Pruebas unitarias y cobertura

Para ejecutar todas las pruebas y ver la cobertura:

```sh
go test -cover ./...
```

Para un reporte visual de cobertura:

```sh
go test -coverprofile=coverage.out ./...
go tool cover -html=coverage.out
```

## Notas
- Los datos de clientes y productos se cargan dinámicamente desde archivos JSON.
- El código incluye pruebas unitarias para los handlers, utilidades y modelos.
- Puedes modificar los archivos en `data/` para cambiar la información servida por la API.

---

Desarrollado por Juan Pablo Galeano Salguero
