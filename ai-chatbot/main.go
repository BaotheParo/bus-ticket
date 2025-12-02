package main

import (
	"context"
	"database/sql"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"os"
	"strings"
	"time"

	"github.com/google/generative-ai-go/genai"
	_ "github.com/lib/pq"
	"google.golang.org/api/iterator"
	"google.golang.org/api/option"
)

var (
	db            *sql.DB
	geminiModel   *genai.GenerativeModel
	geminiContext context.Context
)

type ChatRequest struct {
	Message string `json:"message"`
}

func main() {
	// 1. Load Config
	apiKey := os.Getenv("GEMINI_API_KEY")
	if apiKey == "" {
		log.Fatal("GEMINI_API_KEY environment variable is required")
	}

	dbHost := os.Getenv("DB_HOST")
	dbPort := os.Getenv("DB_PORT")
	dbUser := os.Getenv("DB_USER")
	dbPassword := os.Getenv("DB_PASSWORD")
	dbName := os.Getenv("DB_NAME")

	connStr := fmt.Sprintf("host=%s port=%s user=%s password=%s dbname=%s sslmode=disable",
		dbHost, dbPort, dbUser, dbPassword, dbName)

	// 2. Connect to DB
	var err error
	db, err = sql.Open("postgres", connStr)
	if err != nil {
		log.Fatalf("Failed to connect to database: %v", err)
	}
	defer db.Close()

	if err = db.Ping(); err != nil {
		log.Fatalf("Failed to ping database: %v", err)
	}
	log.Println("Connected to PostgreSQL successfully")

	// 3. Initialize Gemini
	geminiContext = context.Background()
	client, err := genai.NewClient(geminiContext, option.WithAPIKey(apiKey))
	if err != nil {
		log.Fatalf("Failed to create Gemini client: %v", err)
	}
	defer client.Close()

	// List models and pick the best one
	iter := client.ListModels(geminiContext)
	var availableModels []string
	for {
		m, err := iter.Next()
		if err == iterator.Done {
			break
		}
		if err != nil {
			log.Printf("Error listing models: %v", err)
			break
		}
		log.Printf("Found model: %s", m.Name)
		if strings.Contains(m.Name, "gemini") && m.SupportedGenerationMethods != nil {
			for _, method := range m.SupportedGenerationMethods {
				if method == "generateContent" {
					availableModels = append(availableModels, m.Name)
					break
				}
			}
		}
	}

	var modelName string
	
	// Priority list
	priorities := []string{
		"gemini-2.5-flash",
		"gemini-2.0-flash",
		"gemini-1.5-flash",
		"gemini-flash",
	}

	for _, p := range priorities {
		for _, m := range availableModels {
			if strings.Contains(m, p) {
				modelName = m
				break
			}
		}
		if modelName != "" {
			break
		}
	}

	// Fallback to any "flash" model if specific ones aren't found
	if modelName == "" {
		for _, m := range availableModels {
			if strings.Contains(m, "flash") {
				modelName = m
				break
			}
		}
	}

	// Fallback to any gemini model
	if modelName == "" && len(availableModels) > 0 {
		modelName = availableModels[0]
	}

	if modelName == "" {
		log.Println("No suitable model found, falling back to gemini-pro")
		modelName = "gemini-pro"
	} else {
		modelName = strings.TrimPrefix(modelName, "models/")
	}
	
	log.Printf("Selected model: %s", modelName)
	geminiModel = client.GenerativeModel(modelName)

	// 4. Start HTTP Server
	http.HandleFunc("/chat", handleChat)
	log.Println("Chatbot service listening on :8082")
	log.Fatal(http.ListenAndServe(":8082", nil))
}

func handleChat(w http.ResponseWriter, r *http.Request) {
	// Enable CORS
	w.Header().Set("Access-Control-Allow-Origin", "*")
	w.Header().Set("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
	w.Header().Set("Access-Control-Allow-Headers", "Content-Type")

	if r.Method == "OPTIONS" {
		w.WriteHeader(http.StatusOK)
		return
	}

	var userQuery string

	if r.Method == "GET" {
		userQuery = r.URL.Query().Get("q")
		if userQuery == "" {
			http.Error(w, "Missing 'q' parameter", http.StatusBadRequest)
			return
		}
	} else if r.Method == "POST" {
		var req ChatRequest
		if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
			http.Error(w, "Invalid request body", http.StatusBadRequest)
			return
		}
		userQuery = req.Message
	} else {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	log.Printf("Received query: %s", userQuery)

	// Fetch trips from DB
	tripsData, err := fetchTripsFromDB()
	if err != nil {
		log.Printf("Error fetching trips from DB: %v", err)
		http.Error(w, "Error fetching data", http.StatusInternalServerError)
		return
	}

	// Construct Prompt
	prompt := fmt.Sprintf(`
You are a helpful assistant for a bus ticket booking platform.
Here is the list of currently PUBLISHED trips available in our system:
%s

User Question: "%s"

Please answer the user's question based ONLY on the trip data provided above.
If the user asks about a trip that is not in the list, say there are no such trips available.
Format the answer nicely.
`, tripsData, userQuery)

	// Call Gemini
	resp, err := geminiModel.GenerateContent(geminiContext, genai.Text(prompt))
	if err != nil {
		log.Printf("Error calling Gemini API: %v", err)
		http.Error(w, "Error generating response", http.StatusInternalServerError)
		return
	}

	if len(resp.Candidates) == 0 || len(resp.Candidates[0].Content.Parts) == 0 {
		http.Error(w, "No response from AI", http.StatusInternalServerError)
		return
	}

	aiResponse := fmt.Sprintf("%s", resp.Candidates[0].Content.Parts[0])

	// Stream response (simulated by just writing the whole thing for now, or actual streaming if client supports it)
	// For simplicity in this refactor, we just write the text.
	// If the client expects a stream, we should set Flusher.
	
	flusher, ok := w.(http.Flusher)
	if !ok {
		http.Error(w, "Streaming not supported", http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "text/plain; charset=utf-8")
	w.Header().Set("Transfer-Encoding", "chunked")

	// Write response chunks
	chunkSize := 10
	runes := []rune(aiResponse)
	for i := 0; i < len(runes); i += chunkSize {
		end := i + chunkSize
		if end > len(runes) {
			end = len(runes)
		}
		fmt.Fprintf(w, "%s", string(runes[i:end]))
		flusher.Flush()
		time.Sleep(50 * time.Millisecond) // Simulate typing effect
	}
}

func fetchTripsFromDB() (string, error) {
	query := `
		SELECT route_name, departure_point, destination, departure_time, base_price, bus_type_id
		FROM trips
		WHERE status = 'PUBLISHED'
	`
	rows, err := db.Query(query)
	if err != nil {
		return "", err
	}
	defer rows.Close()

	var sb strings.Builder
	for rows.Next() {
		var routeName, depPoint, dest string
		var depTime time.Time
		var price float64
		var busTypeId string // Just fetching ID for now, could join for name

		if err := rows.Scan(&routeName, &depPoint, &dest, &depTime, &price, &busTypeId); err != nil {
			return "", err
		}
		sb.WriteString(fmt.Sprintf("- Route: %s, From: %s, To: %s, Time: %s, Price: %.0f\n",
			routeName, depPoint, dest, depTime.Format("2006-01-02 15:04"), price))
	}
	
	if sb.Len() == 0 {
		return "No published trips found.", nil
	}

	return sb.String(), nil
}
