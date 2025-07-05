package models

import "testing"

func TestCustomerStruct(t *testing.T) {
	c := Customer{ID: "1", Name: "Test", Email: "a@b.com", Active: true}
	if c.ID != "1" || c.Name != "Test" || c.Email != "a@b.com" || !c.Active {
		t.Errorf("Customer struct no se inicializó correctamente: %+v", c)
	}
}

func TestProductStruct(t *testing.T) {
	p := Product{ID: "1", Name: "Prod", Description: "Desc", Price: 10.5}
	if p.ID != "1" || p.Name != "Prod" || p.Description != "Desc" || p.Price != 10.5 {
		t.Errorf("Product struct no se inicializó correctamente: %+v", p)
	}
}
