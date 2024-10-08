package createuser

import (
	"bytes"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"

	"github.com/FSpruhs/kick-app/backend/user/internal/application/commands"
	"github.com/FSpruhs/kick-app/backend/user/internal/application/queries"
	"github.com/FSpruhs/kick-app/backend/user/internal/domain"
)

type MockApp struct{}

func (m MockApp) GetUserAll(cmd *queries.GetUserAll) ([]*domain.User, error) {
	panic("implement me")
}

func (m MockApp) MessageRead(cmd *commands.MessageRead) error {
	//TODO implement me
	panic("implement me")
}

func (m MockApp) GetUser(cmd *queries.GetUser) (*domain.User, error) {
	panic("implement me")
}

func (m MockApp) GetUserMessages(cmd *queries.GetUserMessages) ([]*domain.Message, error) {
	panic("implement me")
}

func (m MockApp) GetUsersByIDs(cmd *queries.GetUsersByIDs) ([]*domain.User, error) {
	panic("implement me")
}

func (m MockApp) LoginUser(cmd *commands.LoginUser) (*domain.User, error) {
	panic("just a mock, not implemented")
}

func (m MockApp) CreateUser(cmd *commands.CreateUser) (*domain.User, error) {
	return &domain.User{
		ID:       "123",
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
		ID:        "123",
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
