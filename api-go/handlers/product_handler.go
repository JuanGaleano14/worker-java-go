package handlers

import (
	"api-go/models"
	"api-go/utils"
	"net/http"

	"github.com/gin-gonic/gin"
)

var products map[string]models.Product

func loadProducts() {
	var productList []models.Product
	err := utils.LoadJSON("data/products.json", &productList)
	if err != nil {
		products = make(map[string]models.Product)
		return
	}
	products = make(map[string]models.Product)
	for _, p := range productList {
		products[p.ID] = p
	}
}

func GetProduct(c *gin.Context) {
	if products == nil {
		loadProducts()
	}
	id := c.Param("id")
	if product, exists := products[id]; exists {
		c.JSON(http.StatusOK, product)
	} else {
		c.JSON(http.StatusNotFound, gin.H{"error": "Product not found"})
	}
}
