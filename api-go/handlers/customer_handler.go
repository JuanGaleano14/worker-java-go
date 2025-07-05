package handlers

import (
	"api-go/models"
	"api-go/utils"
	"net/http"

	"github.com/gin-gonic/gin"
)

var customers map[string]models.Customer

func loadCustomers() {
	var customerList []models.Customer
	err := utils.LoadJSON("data/customers.json", &customerList)
	if err != nil {
		customers = make(map[string]models.Customer)
		return
	}
	customers = make(map[string]models.Customer)
	for _, c := range customerList {
		customers[c.ID] = c
	}
}

func GetCustomer(c *gin.Context) {
	if customers == nil {
		loadCustomers()
	}
	id := c.Param("id")
	if customer, exists := customers[id]; exists {
		c.JSON(http.StatusOK, customer)
	} else {
		c.JSON(http.StatusNotFound, gin.H{"error": "Customer not found"})
	}
}
