package handlers

import (
	"api-go/models"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"os"
	"testing"

	"github.com/gin-gonic/gin"
)

func TestGetCustomer(t *testing.T) {
	// Crear archivo temporal de clientes
	os.MkdirAll("data", 0755)
	f, _ := os.Create("data/customers.json")
	defer os.Remove("data/customers.json")
	f.WriteString(`[{"customerId":"c1","name":"Test","email":"test@mail.com","active":true}]`)
	f.Close()
	customers = nil // Forzar recarga

	w := httptest.NewRecorder()
	r := gin.Default()
	r.GET("/customers/:id", GetCustomer)

	req, _ := http.NewRequest("GET", "/customers/c1", nil)
	r.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("Esperado 200, obtuvo %d", w.Code)
	}
	var c models.Customer
	json.Unmarshal(w.Body.Bytes(), &c)
	if c.ID != "c1" || c.Name != "Test" {
		t.Errorf("Datos incorrectos: %+v", c)
	}
}
