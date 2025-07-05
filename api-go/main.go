package main

import (
	"api-go/handlers"

	"github.com/gin-gonic/gin"
)

func main() {
	r := gin.Default()

	r.GET("/products/:id", handlers.GetProduct)
	r.GET("/customers/:id", handlers.GetCustomer)

	r.Run(":8080")
}
