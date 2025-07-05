package utils

import (
	"encoding/json"
	"os"
)

// LoadJSON lectura de JSON dinamico.
func LoadJSON(path string, out interface{}) error {
	file, err := os.Open(path)
	if err != nil {
		return err
	}
	defer file.Close()
	return json.NewDecoder(file).Decode(out)
}
