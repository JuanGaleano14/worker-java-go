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

func TestGetProduct(t *testing.T) {
	// Crear archivo temporal de productos
	os.MkdirAll("data", 0755)
	f, _ := os.Create("data/products.json")
	defer os.Remove("data/products.json")
	f.WriteString(`[{"productId":"p1","name":"Prod","description":"Desc","price":10.5}]`)
	f.Close()
	products = nil // Forzar recarga

	w := httptest.NewRecorder()
	r := gin.Default()
	r.GET("/products/:id", GetProduct)

	req, _ := http.NewRequest("GET", "/products/p1", nil)
	r.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("Esperado 200, obtuvo %d", w.Code)
	}
	var p models.Product
	json.Unmarshal(w.Body.Bytes(), &p)
	if p.ID != "p1" || p.Name != "Prod" {
		t.Errorf("Datos incorrectos: %+v", p)
	}
}
