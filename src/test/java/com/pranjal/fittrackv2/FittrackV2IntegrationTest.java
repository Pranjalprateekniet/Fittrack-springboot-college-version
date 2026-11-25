package com.pranjal.fittrackv2;

import com.pranjal.fittrackv2.nutrition.repository.FoodNutritionRepository;
import com.pranjal.fittrackv2.nutrition.repository.FoodServingRepository;
import com.pranjal.fittrackv2.nutrition.service.FoodNutritionDataLoader;
import com.pranjal.fittrackv2.nutrition.service.FoodServingDataLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.springframework.mock.web.MockHttpSession;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FittrackV2IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FoodNutritionDataLoader foodNutritionDataLoader;

    @Autowired
    private FoodServingDataLoader foodServingDataLoader;

    @Autowired
    private FoodNutritionRepository foodNutritionRepository;

    @Autowired
    private FoodServingRepository foodServingRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void completeUserJourneyTest() throws Exception {
        // 1. Protected endpoints reject unauthenticated requests
        mockMvc.perform(get("/api/goals"))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(get("/api/workouts"))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(get("/api/nutrition/search").param("query", "egg"))
                .andExpect(status().is4xxClientError());

        // 2. Register a new account (using "name" alias as sent by frontend)
        String registerPayload = """
                {
                  "name": "Demo Student",
                  "email": "student@fittrack.edu",
                  "password": "password123"
                }
                """;
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("User registered successfully")));

        // 3. Login and verify JWT + RefreshToken
        String loginPayload = """
                {
                  "email": "student@fittrack.edu",
                  "password": "password123"
                }
                """;
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(emptyOrNullString())))
                .andExpect(jsonPath("$.refreshToken", not(emptyOrNullString())))
                .andExpect(jsonPath("$.fullName").value("Demo Student"))
                .andExpect(jsonPath("$.email").value("student@fittrack.edu"))
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String accessToken = loginJson.get("accessToken").asText();
        String refreshToken = loginJson.get("refreshToken").asText();

        // 4. Refresh token endpoint
        String refreshPayload = """
                {
                  "refreshToken": "%s"
                }
                """.formatted(refreshToken);
        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(emptyOrNullString())))
                .andExpect(jsonPath("$.refreshToken", not(emptyOrNullString())))
                .andReturn();

        accessToken = objectMapper.readTree(refreshResult.getResponse().getContentAsString())
                .get("accessToken").asText();
        String authHeader = "Bearer " + accessToken;

        // 5. Create Goal & verify BMI, BMR (Mifflin-St Jeor), TDEE
        String goalPayload = """
                {
                  "goalType": "WEIGHT_LOSS",
                  "goalPace": "MODERATE",
                  "gender": "MALE",
                  "activityLevel": "MODERATELY_ACTIVE",
                  "weightKg": 80.0,
                  "heightCm": 180.0,
                  "targetWeightKg": 74.0,
                  "age": 22
                }
                """;
        mockMvc.perform(post("/api/goals")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(goalPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bmi").value(24.7))
                .andExpect(jsonPath("$.bmiCategory").value("NORMAL"))
                .andExpect(jsonPath("$.bmr").value(1820.0))
                .andExpect(jsonPath("$.tdee").value(2821.0))
                .andExpect(jsonPath("$.targetCalories").value(2321));

        // 6. Get Goal
        mockMvc.perform(get("/api/goals").header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goalType").value("WEIGHT_LOSS"));

        // 7. Create, Get, Update, Delete Workout
        String workoutPayload = """
                {
                  "category": "STRENGTH",
                  "exercise": "BENCH_PRESS",
                  "sets": 3,
                  "reps": 10,
                  "intensity": "MODERATE",
                  "workoutDate": "2026-11-25"
                }
                """;
        mockMvc.perform(post("/api/workouts")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(workoutPayload))
                .andExpect(status().isOk());

        MvcResult workoutsResult = mockMvc.perform(get("/api/workouts")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].exercise").value("BENCH_PRESS"))
                .andExpect(jsonPath("$[0].caloriesBurned", greaterThan(0.0)))
                .andReturn();

        String workoutId = objectMapper.readTree(workoutsResult.getResponse().getContentAsString())
                .get(0).get("id").asText();

        mockMvc.perform(delete("/api/workouts/" + workoutId)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk());

        // 8. Search Food (both /api/nutrition/search and /api/food/search)
        MvcResult searchResult = mockMvc.perform(get("/api/nutrition/search")
                        .header("Authorization", authHeader)
                        .param("query", "egg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andReturn();

        mockMvc.perform(get("/api/food/search")
                        .header("Authorization", authHeader)
                        .param("query", "egg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));

        long foodId = objectMapper.readTree(searchResult.getResponse().getContentAsString())
                .get(0).get("id").asLong();

        // 9. Log Food, Get Today Logs & Summary, Delete Food Log
        String foodLogPayload = """
                {
                  "foodId": %d,
                  "gramsConsumed": 100.0,
                  "mealType": "BREAKFAST"
                }
                """.formatted(foodId);
        mockMvc.perform(post("/api/food/log")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(foodLogPayload))
                .andExpect(status().isOk());

        MvcResult logsResult = mockMvc.perform(get("/api/food/logs/today")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andReturn();

        mockMvc.perform(get("/api/food/summary/today")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCalories", greaterThan(0.0)));

        long logId = objectMapper.readTree(logsResult.getResponse().getContentAsString())
                .get(0).get("logId").asLong();

        mockMvc.perform(delete("/api/food/log/" + logId)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk());
    }

    @Test
    void thymeleafWebApplicationJourneyTest() throws Exception {
        // 1. Public auth pages load
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("FitTrack V2")));
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Create Account")));

        // 2. Protected web pages redirect to /login when unauthenticated
        for (String route : new String[]{"/dashboard", "/goals", "/workouts", "/nutrition", "/food-log", "/profile"}) {
            mockMvc.perform(get(route))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"));
        }

        // 3. Register via Thymeleaf form (with CSRF)
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fullName", "Web Demo User")
                        .param("email", "webdemo@fittrack.edu")
                        .param("password", "secret123")
                        .param("confirmPassword", "secret123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        // 4. Login via Spring Security formLogin (with CSRF)
        MvcResult loginResult = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("email", "webdemo@fittrack.edu")
                        .param("password", "secret123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        // 5. Save Goal via /goals POST
        mockMvc.perform(post("/goals")
                        .session(session)
                        .with(csrf())
                        .param("goalType", "WEIGHT_LOSS")
                        .param("goalPace", "MODERATE")
                        .param("gender", "MALE")
                        .param("activityLevel", "MODERATELY_ACTIVE")
                        .param("weightKg", "80.0")
                        .param("targetWeightKg", "74.0")
                        .param("heightCm", "180.0")
                        .param("age", "22"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/goals"));

        // 6. Verify Dashboard & Goals & Profile render real BMI, BMR, TDEE
        mockMvc.perform(get("/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Web Demo User")))
                .andExpect(content().string(containsString("1820")))
                .andExpect(content().string(containsString("2821")));

        mockMvc.perform(get("/goals").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("24.7")));

        mockMvc.perform(get("/profile").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("webdemo@fittrack.edu")));

        // 7. Create & view Workout on /workouts
        mockMvc.perform(post("/workouts")
                        .session(session)
                        .with(csrf())
                        .param("category", "CARDIO")
                        .param("exercise", "RUNNING")
                        .param("intensity", "MODERATE")
                        .param("durationMinutes", "30")
                        .param("workoutDate", "2026-11-25"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/workouts"));

        mockMvc.perform(get("/workouts").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("RUNNING")));

        // 8. Search Nutrition on /nutrition and Food Log on /food-log
        mockMvc.perform(get("/nutrition").session(session).param("query", "egg"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Search Results")));

        mockMvc.perform(get("/food-log").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Daily Food Log")));

        // 9. Logout invalidates session and blocks protected pages
        mockMvc.perform(post("/logout").session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"));

        mockMvc.perform(get("/dashboard").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void securityCsrfCrossUserAndLoaderIdempotencyTest() throws Exception {
        // 1. DataLoader idempotency: repeated execution must not duplicate records
        long initialFoods = foodNutritionRepository.count();
        long initialServings = foodServingRepository.count();
        Assertions.assertTrue(initialFoods > 1000, "Expected nutrition dataset to be loaded");
        Assertions.assertTrue(initialServings > 0, "Expected serving presets to be loaded");

        foodNutritionDataLoader.loadData();
        foodServingDataLoader.loadServings();
        Assertions.assertEquals(initialFoods, foodNutritionRepository.count(), "No duplicate foods on restart");
        Assertions.assertEquals(initialServings, foodServingRepository.count(), "No duplicate servings on restart");

        // 2. Register User A and User B via API
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"User A","email":"usera@fittrack.edu","password":"password123"}
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"User B","email":"userb@fittrack.edu","password":"password123"}
                                """))
                .andExpect(status().isOk());

        String tokenA = objectMapper.readTree(
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"email":"usera@fittrack.edu","password":"password123"}
                                        """))
                        .andReturn().getResponse().getContentAsString()
        ).get("accessToken").asText();

        String tokenB = objectMapper.readTree(
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"email":"userb@fittrack.edu","password":"password123"}
                                        """))
                        .andReturn().getResponse().getContentAsString()
        ).get("accessToken").asText();

        // User A creates a workout
        mockMvc.perform(post("/api/workouts")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"category":"CARDIO","exercise":"WALKING","intensity":"LIGHT","durationMinutes":20,"workoutDate":"2026-11-25"}
                                """))
                .andExpect(status().isOk());

        String workoutIdA = objectMapper.readTree(
                mockMvc.perform(get("/api/workouts").header("Authorization", "Bearer " + tokenA))
                        .andReturn().getResponse().getContentAsString()
        ).get(0).get("id").asText();

        // 3. Cross-user isolation: User B cannot delete User A's workout (403 Forbidden)
        mockMvc.perform(delete("/api/workouts/" + workoutIdA)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());

        // 4. CSRF protection: browser POST without CSRF token must be rejected (403 Forbidden)
        MvcResult webLogin = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("email", "usera@fittrack.edu")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpSession sessionA = (MockHttpSession) webLogin.getRequest().getSession(false);

        mockMvc.perform(post("/workouts")
                        .session(sessionA)
                        .param("category", "CARDIO")
                        .param("exercise", "WALKING")
                        .param("intensity", "LIGHT")
                        .param("durationMinutes", "15"))
                .andExpect(status().isForbidden());
    }
}

