package models

type Customer struct {
	ID     string `json:"customerId"`
	Name   string `json:"name"`
	Email  string `json:"email"`
	Active bool   `json:"active"`
}