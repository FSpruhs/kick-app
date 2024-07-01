package createUser

import (
	"bytes"
	"encoding/json"
	"github.com/FSpruhs/kick-app/backend/user/internal/application/commands"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/stretchr/testify/assert"

	"github.com/FSpruhs/kick-app/backend/user/internal/domain"
	"github.com/gin-gonic/gin"
)

type MockApp struct{}

func (m MockApp) LoginUser(cmd commands.LoginUser) (*domain.User, error) {
	panic("just a mock, not implemented")
}

func (m MockApp) CreateUser(cmd *commands.CreateUser) (*domain.User, error) {
	return &domain.User{
		Id:       "123",
		FullName: cmd.FullName,
		Email:    cmd.Email,
		NickName: cmd.Nickname,
		Groups:   []string{},
	}, nil
}

func TestHandle_Success(t *testing.T) {
	gin.SetMode(gin.TestMode)
	router := gin.New()

	mockApp := MockApp{}
	handler := Handle(mockApp)
	router.POST("/create-user", handler)

	jsonPayload := `{
		"firstName": "John",
		"lastName": "Doe",
		"email": "john.doe@example.com",
		"password": "Abcd123",
		"nickName": "johndoe"
	}`

	req, err := http.NewRequest("POST", "/create-user", bytes.NewBufferString(jsonPayload))
	if err != nil {
		t.Fatalf("Failed to create request: %v", err)
	}

	req.Header.Set("Content-Type", "application/json")
	rec := httptest.NewRecorder()
	router.ServeHTTP(rec, req)

	assert.Equal(t, http.StatusCreated, rec.Code, "Expected status 201 Created, got %v", rec.Code)

	var response Response
	err = json.Unmarshal(rec.Body.Bytes(), &response)
	if err != nil {
		t.Fatalf("Failed to unmarshal JSON response: %v", err)
	}

	expectedResponse := Response{
		Id:        "123",
		FirstName: "John",
		LastName:  "Doe",
		Email:     "john.doe@example.com",
		NickName:  "johndoe",
		Groups:    []string{},
	}

	assert.Equal(t, expectedResponse, response, "Expected response %+v, got %+v", expectedResponse, response)
}

func TestHandle_BadRequest(t *testing.T) {
	gin.SetMode(gin.TestMode)
	router := gin.New()

	mockApp := MockApp{}
	handler := Handle(mockApp)
	router.POST("/create-user", handler)

	invalidJsonPayload := `{ "invalid": "json" }`

	req, err := http.NewRequest("POST", "/create-user", bytes.NewBufferString(invalidJsonPayload))
	if err != nil {
		t.Fatalf("Failed to create request: %v", err)
	}

	req.Header.Set("Content-Type", "application/json")
	rec := httptest.NewRecorder()
	router.ServeHTTP(rec, req)

	assert.Equal(t, http.StatusBadRequest, rec.Code, "Expected status 400 Bad Request, got %v", rec.Code)
}

func TestHandle_ValidationFailure(t *testing.T) {
	gin.SetMode(gin.TestMode)
	router := gin.New()

	mockApp := MockApp{}
	handler := Handle(mockApp)
	router.POST("/create-user", handler)

	invalidJsonPayload := `{
		"firstName": "",
		"lastName": "Doe",
		"email": "john.doe@example.com",
		"password": "Abcd123",
		"nickName": "johndoe"
	}`

	req, err := http.NewRequest("POST", "/create-user", bytes.NewBufferString(invalidJsonPayload))
	if err != nil {
		t.Fatalf("Failed to create request: %v", err)
	}

	req.Header.Set("Content-Type", "application/json")
	rec := httptest.NewRecorder()
	router.ServeHTTP(rec, req)

	assert.Equal(t, http.StatusBadRequest, rec.Code, "Expected status 400 Bad Request, got %v", rec.Code)
}
