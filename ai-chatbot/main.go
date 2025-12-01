package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net/http"
	"os"
	"strings"

	"github.com/go-redis/redis/v8"
)

var rdb *redis.Client
var geminiKey string
var currentModel string

// Gemini Request/Response structs
type GeminiRequest struct {
	Contents []GeminiContent `json:"contents"`
}

type GeminiContent struct {
	Parts []GeminiPart `json:"parts"`
}

type GeminiPart struct {
	Text string `json:"text"`
}

type GeminiResponse struct {
	Candidates []GeminiCandidate `json:"candidates"`
	Error      *GeminiError      `json:"error,omitempty"`
}

type GeminiCandidate struct {
	Content GeminiContent `json:"content"`
}

type GeminiError struct {
	Code    int    `json:"code"`
	Message string `json:"message"`
	Status  string `json:"status"`
}

// Structs for ListModels
type ListModelsResponse struct {
	Models []GeminiModel `json:"models"`
	Error  *GeminiError  `json:"error,omitempty"`
}

type GeminiModel struct {
	Name                       string   `json:"name"`
	SupportedGenerationMethods []string `json:"supportedGenerationMethods"`
}

func main() {
	redisAddr := os.Getenv("REDIS_ADDR")
	if redisAddr == "" {
		redisAddr = "localhost:6379"
	}

	geminiKey = os.Getenv("GEMINI_API_KEY")
	if geminiKey == "" {
		log.Fatal("GEMINI_API_KEY environment variable is required")
	} else {
		maskedKey := geminiKey
		if len(geminiKey) > 5 {
			maskedKey = geminiKey[:5] + "..."
		}
		log.Printf("Loaded GEMINI_API_KEY: %s", maskedKey)
	}

	// 1. Connect to Redis
	rdb = redis.NewClient(&redis.Options{
		Addr: redisAddr,
	})

	// 2. Find Best Model
	var err error
	currentModel, err = findBestModel()
	if err != nil {
		log.Printf("Warning: Could not auto-detect model: %v. Defaulting to 'models/gemini-1.5-flash'", err)
		currentModel = "models/gemini-1.5-flash"
	} else {
		log.Printf("Selected Gemini Model: %s", currentModel)
	}

	// 3. API Chat
	http.HandleFunc("/chat", handleChat)

	port := "8082"
	log.Printf("AI Chatbot Service running on :%s", port)
	log.Fatal(http.ListenAndServe(":"+port, nil))
}

func findBestModel() (string, error) {
	url := "https://generativelanguage.googleapis.com/v1beta/models?key=" + geminiKey
	resp, err := http.Get(url)
	if err != nil {
		return "", err
	}
	defer resp.Body.Close()

	bodyBytes, _ := io.ReadAll(resp.Body)
	if resp.StatusCode != http.StatusOK {
		return "", fmt.Errorf("API returned status %d: %s", resp.StatusCode, string(bodyBytes))
	}

	var listResp ListModelsResponse
	if err := json.Unmarshal(bodyBytes, &listResp); err != nil {
		return "", err
	}

	if listResp.Error != nil {
		return "", fmt.Errorf("API Error: %s", listResp.Error.Message)
	}

	// Priority: flash > pro > any other that supports generateContent
	var bestModel string
	
	for _, m := range listResp.Models {
		supportsGenerate := false
		for _, method := range m.SupportedGenerationMethods {
			if method == "generateContent" {
				supportsGenerate = true
				break
			}
		}

		if supportsGenerate {
			if strings.Contains(m.Name, "flash") {
				return m.Name, nil // Found flash, return immediately
			}
			if strings.Contains(m.Name, "pro") && bestModel == "" {
				bestModel = m.Name
			}
			if bestModel == "" {
				bestModel = m.Name
			}
		}
	}

	if bestModel != "" {
		return bestModel, nil
	}

	return "", fmt.Errorf("no suitable model found")
}

func handleChat(w http.ResponseWriter, r *http.Request) {
	// Enable CORS
	w.Header().Set("Access-Control-Allow-Origin", "*")
	w.Header().Set("Content-Type", "text/plain; charset=utf-8")

	userQuestion := r.URL.Query().Get("q")
	if userQuestion == "" {
		http.Error(w, "Missing 'q' query parameter", http.StatusBadRequest)
		return
	}

	// STEP 1: Get Context from Redis
	ctx := r.Context()
	var tripsDataBuilder strings.Builder
	
	iter := rdb.Scan(ctx, 0, "trips::*", 10).Iterator()
	foundData := false
	for iter.Next(ctx) {
		key := iter.Val()
		val, err := rdb.Get(ctx, key).Result()
		if err == nil {
			var jsonObj interface{}
			if err := json.Unmarshal([]byte(val), &jsonObj); err == nil {
				prettyJSON, _ := json.MarshalIndent(jsonObj, "", "  ")
				tripsDataBuilder.Write(prettyJSON)
			} else {
				tripsDataBuilder.WriteString(val)
			}
			tripsDataBuilder.WriteString("\n---\n")
			foundData = true
		}
	}
	if err := iter.Err(); err != nil {
		log.Printf("Error scanning Redis keys: %v", err)
	}

	tripsData := tripsDataBuilder.String()
	if !foundData {
		tripsData = "Hiện không có dữ liệu chuyến xe nào được tìm thấy trong hệ thống."
	}

	// STEP 2: Create Prompt
	prompt := fmt.Sprintf(`
        Bạn là nhân viên tư vấn bán vé xe buýt nhiệt tình và chuyên nghiệp.
        Dưới đây là dữ liệu các chuyến xe hiện có (được lấy từ hệ thống):
        
        %s
        
        Khách hàng hỏi: "%s"
        
        Hãy trả lời ngắn gọn, thân thiện, và CHÍNH XÁC dựa trên dữ liệu chuyến xe ở trên.
        - Nếu có chuyến phù hợp, hãy liệt kê chi tiết: Nhà xe, Giờ đi, Giá vé, Loại xe.
        - Nếu không có chuyến phù hợp trong dữ liệu, hãy xin lỗi và nói rõ là không tìm thấy chuyến nào trong hệ thống hiện tại.
        - Đừng bịa ra chuyến xe không có trong dữ liệu.
    `, tripsData, userQuestion)

	// STEP 3: Call Gemini REST API
	url := fmt.Sprintf("https://generativelanguage.googleapis.com/v1beta/%s:generateContent?key=%s", currentModel, geminiKey)
	
	reqBody := GeminiRequest{
		Contents: []GeminiContent{
			{
				Parts: []GeminiPart{
					{Text: prompt},
				},
			},
		},
	}
	
	jsonBody, _ := json.Marshal(reqBody)
	
	resp, err := http.Post(url, "application/json", bytes.NewBuffer(jsonBody))
	if err != nil {
		log.Printf("Error calling Gemini API: %v", err)
		fmt.Fprintf(w, "[Lỗi kết nối với AI: %v]", err)
		return
	}
	defer resp.Body.Close()

	bodyBytes, _ := io.ReadAll(resp.Body)
	
	if resp.StatusCode != http.StatusOK {
		log.Printf("Gemini API Error: Status=%d, Body=%s", resp.StatusCode, string(bodyBytes))
		fmt.Fprintf(w, "[Lỗi từ Gemini AI: %d - %s]", resp.StatusCode, string(bodyBytes))
		return
	}

	var geminiResp GeminiResponse
	if err := json.Unmarshal(bodyBytes, &geminiResp); err != nil {
		log.Printf("Error parsing JSON: %v", err)
		fmt.Fprintf(w, "[Lỗi xử lý dữ liệu AI]")
		return
	}

	if len(geminiResp.Candidates) > 0 && len(geminiResp.Candidates[0].Content.Parts) > 0 {
		fmt.Fprintf(w, "%s", geminiResp.Candidates[0].Content.Parts[0].Text)
	} else {
		fmt.Fprintf(w, "[AI không trả lời]")
	}
}
