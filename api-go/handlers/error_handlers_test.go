package handlers

import (
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
)

func TestGetCustomer_NotFound(t *testing.T) {
	customers = nil
	w := httptest.NewRecorder()
	r := gin.Default()
	r.GET("/customers/:id", GetCustomer)

	req, _ := http.NewRequest("GET", "/customers/xxx", nil)
	r.ServeHTTP(w, req)

	if w.Code != http.StatusNotFound {
		t.Errorf("Esperado 404, obtuvo %d", w.Code)
	}
}

func TestGetProduct_NotFound(t *testing.T) {
	products = nil
	w := httptest.NewRecorder()
	r := gin.Default()
	r.GET("/products/:id", GetProduct)

	req, _ := http.NewRequest("GET", "/products/xxx", nil)
	r.ServeHTTP(w, req)

	if w.Code != http.StatusNotFound {
		t.Errorf("Esperado 404, obtuvo %d", w.Code)
	}
}
