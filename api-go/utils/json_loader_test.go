package utils

import (
	"os"
	"testing"
)

type testStruct struct {
	ID   string `json:"id"`
	Name string `json:"name"`
}

func TestLoadJSON(t *testing.T) {
	// Crear archivo temporal con datos de prueba
	file, err := os.CreateTemp("", "testdata_*.json")
	if err != nil {
		t.Fatalf("No se pudo crear archivo temporal: %v", err)
	}
	defer os.Remove(file.Name())
	file.WriteString(`[{"id":"1","name":"Test"}]`)
	file.Close()

	var data []testStruct
	err = LoadJSON(file.Name(), &data)
	if err != nil {
		t.Errorf("Error al cargar JSON: %v", err)
	}
	if len(data) != 1 || data[0].ID != "1" || data[0].Name != "Test" {
		t.Errorf("Datos incorrectos: %+v", data)
	}
}

func TestLoadJSON_FileNotFound(t *testing.T) {
	var data []testStruct
	err := LoadJSON("no_existe.json", &data)
	if err == nil {
		t.Error("Se esperaba error por archivo inexistente")
	}
}

func TestLoadJSON_InvalidJSON(t *testing.T) {
	file, err := os.CreateTemp("", "badjson_*.json")
	if err != nil {
		t.Fatalf("No se pudo crear archivo temporal: %v", err)
	}
	defer os.Remove(file.Name())
	file.WriteString("{mal json}")
	file.Close()

	var data []testStruct
	err = LoadJSON(file.Name(), &data)
	if err == nil {
		t.Error("Se esperaba error por JSON inválido")
	}
}
