# Border Run — Project Specification

## Overview
Border Run is an Android educational app that teaches geography through interactive quizzes. Target audience: secondary school students (13–17). Built with Kotlin, Jetpack Compose, Material Design 3.

---

## Design System

### Colors
- **Background gradient:** `#DDFCE6` (mint) → `#CCFBF1` (teal) → `#CFFAFE` (cyan) → `#E0F2FE` (sky)
- **Primary:** `#10B981` (emerald green)
- **Secondary:** `#0891B2` (teal)
- **Text heading:** `#064E3B` (deep emerald)
- **Text body:** `#6B7280`
- **Text muted:** `#9CA3AF`
- **Cards:** `rgba(255,255,255,0.55)` with `0.5px solid rgba(255,255,255,0.8)` border
- **Region accents:**
  - Asia: Pink `#EC4899` / gradient `#F472B6 → #EC4899`
  - Europe: Purple `#8B5CF6` / gradient `#A78BFA → #8B5CF6`
  - Africa: Cyan `#06B6D4` / gradient `#22D3EE → #06B6D4`
  - Americas: Amber `#F59E0B` / gradient `#FBBF24 → #F59E0B`
  - Oceania: Green `#10B981` / gradient `#34D399 → #10B981`
- **Success:** `#059669` (green)
- **Error:** `#E11D48` (red)
- **CTA button gradient:** `#10B981 → #0891B2`

### Typography
- **Font:** Nunito (Google Fonts)
- **Weights:** 400 (body), 600 (labels/subtitles), 700 (section headers), 800 (page titles, stat numbers)
- **Sizes:** 23sp titles, 17sp section headers, 15sp body, 13sp subtitles, 11sp captions

### Shape
- **Main cards:** 22dp corner radius
- **Region cards:** 16dp
- **Stat cards:** 16dp
- **Buttons:** 16dp
- **Icon containers:** 12dp
- **Progress bars:** 12dp fill, 12dp track

---

## Screens

### 1. Landing / Home Screen
- Greeting with time-of-day message ("Good Morning", "Good Afternoon", "Good Evening")
- Streak counter pill
- Daily Challenge card with title, question count, time estimate, CTA button
- Region grid (2x2): Asia, Europe, Africa, Americas — each with icon, name, country count
- Weekly Progress: 5 face icons (Mon–Fri) colored by daily performance
- Stats summary row: Total Answered, Accuracy %, Streak
- Bottom nav: Home, Quiz, Stats, Settings

### 2. Quiz Screen
- Top bar: back button with region name, timer, question counter (e.g. "6 / 10")
- Progress bar (gradient fill)
- Question card: content varies by question type (see Question Types below)
- 4 answer options (A/B/C/D) with colored letter badges
- Correct/wrong feedback: green check or red X icon, color shift on selected option
- Points popup on correct answer
- After last question → navigate to Quiz Result Screen

### 3. Quiz Result Screen
- Trophy icon with "Great Job!" / "Good Effort!" / "Keep Practicing!" based on score
- Score ring (circular progress) showing X of Y correct
- Mini stats: Correct, Wrong, Avg Time
- Badge unlock card (if score >= 80% on a region)
- Per-question answer list with checkmark/X
- Buttons: Home, Try Again

### 4. Statistics Screen
- 4 stat cards: Total Answered, Accuracy, Day Streak, Regions Explored
- Accuracy by Region: horizontal bar chart (gradient fills per region color)
- Activity Calendar: grid showing daily performance color-coded (green/purple/amber/red)
- Weak area tip card: highlights lowest-accuracy region with CTA to practice

### 5. Settings Screen
- Location banner: shows status of Local Discovery, describes data usage
- Quiz Preferences section:
  - Difficulty selector (Easy / Medium / Hard chips)
  - Timed mode toggle
  - Sound effects toggle
  - Show hints toggle
- Notifications section:
  - Daily reminder toggle with time display
- Your Data section:
  - Records count, cached countries count, storage location
  - "Clear All My Data" button (with confirmation dialog)

### 6. Permission Rationale Screen
- Large location pin icon
- Title: "Local Discovery"
- Description of feature
- 3 info cards: What we use, Why we need it, You stay in control
- 3 privacy checkmarks: never stored, no background tracking, no third parties
- "Allow Approximate Location" button (green)
- "Skip for Now" button (outlined)
- Note: "You can enable this later in settings"

### 7. Explorer Mode Screen (additional)
- Region selector tabs at top
- Scrollable list of country cards with:
  - Flag image
  - Country name and official name
  - Capital, population, area
  - Languages, currencies
  - Subregion, bordering countries
- Search bar to filter countries
- Tap card to see full details

---

## Question Types

### Easy Tier
1. **Flag Identification** — Show flag SVG/image → "Which country's flag is this?" → 4 country name options
2. **Capital Matching** — "What is the capital of [country]?" → 4 capital options
3. **Reverse Capital** — "[Capital] is the capital of which country?" → 4 country options
4. **Region Sorting** — "Which continent is [country] in?" → 4 region options

### Medium Tier
5. **Population Comparison** — Show 2 countries side by side → "Which has a larger population?" → tap one
6. **Area Comparison** — Same format → "Which country is physically larger?"
7. **Language Matching** — "Which of these countries speaks [language]?" → 4 country options
8. **Currency Identification** — "Which country uses the [currency]?" → 4 country options
9. **Subregion Sorting** — "Which subregion is [country] in?" → 3-4 subregion options

### Hard Tier
10. **Border Neighbors** — "Which country does NOT border [country]?" → 4 country options (3 real borders + 1 non-border)
11. **Landlocked Challenge** — "Which of these countries is landlocked?" → 4 country options
12. **Odd One Out** — "Three of these are in [subregion]. Which one isn't?" → 4 country options
13. **True or False** — "[Country] drives on the left side" / "[Country] is landlocked" / "[Capital] is the capital of [country]" → True / False
14. **Multi-Attribute** — "Which [landlocked/island] country in [region] speaks [language]?" → 4 options

---

## Game Modes

### Classic Quiz
- Select a region → select difficulty → 10 questions from that region at that difficulty
- Mixed question types based on difficulty tier
- Timed optional (30s per question)
- Score saved to QUIZ_SESSIONS

### Daily Challenge
- Generated daily by WorkManager
- Themed (e.g. "Island Nations", "Countries Starting with M", "Southeast Asian Capitals")
- 10 questions, medium difficulty, 2x points
- Can only be completed once per day
- Tracked in DAILY_CHALLENGES table

### Streak Mode
- Endless questions, random region and type
- Difficulty escalates every 5 correct answers (Easy → Medium → Hard)
- One wrong answer ends the run
- Tracks high score in stats

### Speed Round
- 20 True/False questions in 60 seconds
- Quick-fire format
- Score = correct answers × time bonus

---

## API

### RestCountries API
- **Base URL:** `https://restcountries.com/v3.1/`
- **Endpoint:** `all?fields=name,capital,flags,population,area,region,subregion,languages,currencies,borders,landlocked,car,timezones`
- **Auth:** None required
- **Rate limit:** No strict limit, but cache aggressively

### Response model (key fields per country)
```json
{
  "name": { "common": "Vietnam", "official": "Socialist Republic of Vietnam" },
  "capital": ["Hanoi"],
  "flags": { "png": "https://flagcdn.com/w320/vn.png", "svg": "https://flagcdn.com/vn.svg" },
  "population": 97338579,
  "area": 331212.0,
  "region": "Asia",
  "subregion": "South-Eastern Asia",
  "languages": { "vie": "Vietnamese" },
  "currencies": { "VND": { "name": "Vietnamese đồng", "symbol": "₫" } },
  "borders": ["CHN", "KHM", "LAO"],
  "landlocked": false,
  "car": { "side": "right" },
  "timezones": ["UTC+07:00"]
}
```

---

## Database (Room)

### Table: countries
| Column | Type | Notes |
|--------|------|-------|
| id | String (PK) | Country code (cca3) |
| name | String | Common name |
| officialName | String | Official name |
| capital | String | Primary capital |
| region | String | Continent |
| subregion | String | Sub-continental region |
| flagUrl | String | PNG flag URL |
| population | Long | |
| area | Double | km² |
| languages | String | JSON-encoded list |
| currencies | String | JSON-encoded list |
| borders | String | JSON-encoded list of country codes |
| isLandlocked | Boolean | |
| drivingSide | String | "left" or "right" |
| cachedAt | Long | Timestamp for cache invalidation |

### Table: quiz_sessions
| Column | Type | Notes |
|--------|------|-------|
| id | Int (PK, auto) | |
| gameMode | String | "classic", "daily", "streak", "speed" |
| region | String? | Null for streak/speed modes |
| difficulty | String | "easy", "medium", "hard" |
| totalQuestions | Int | |
| correctAnswers | Int | |
| score | Int | Points earned |
| durationMs | Long | Total quiz time |
| completedAt | Long | Timestamp |

### Table: quiz_answers
| Column | Type | Notes |
|--------|------|-------|
| id | Int (PK, auto) | |
| sessionId | Int (FK → quiz_sessions) | |
| questionType | String | e.g. "flag", "capital", "population_compare" |
| countryId | String (FK → countries) | Primary country in question |
| userAnswer | String | What the user chose |
| correctAnswer | String | The right answer |
| isCorrect | Boolean | |
| timeSpentMs | Long | Time on this question |
| answeredAt | Long | Timestamp |

### Table: user_preferences
| Column | Type | Notes |
|--------|------|-------|
| id | Int (PK, always 1) | Single row |
| difficulty | String | Default: "medium" |
| timedMode | Boolean | Default: true |
| soundEnabled | Boolean | Default: true |
| hintsEnabled | Boolean | Default: false |
| locationEnabled | Boolean | Default: false |
| notificationsEnabled | Boolean | Default: false |
| notificationTime | String | Default: "18:00" |

### Table: daily_challenges
| Column | Type | Notes |
|--------|------|-------|
| id | Int (PK, auto) | |
| title | String | e.g. "Island Nations" |
| description | String | e.g. "Test your knowledge of island countries" |
| region | String? | Null for mixed-region challenges |
| theme | String | e.g. "island", "landlocked", "starting_letter" |
| date | Long | Day timestamp (midnight) |
| completed | Boolean | |
| sessionId | Int? (FK → quiz_sessions) | Linked session when completed |

### Key DAO Queries
```kotlin
// Accuracy by region (last 30 days)
@Query("""
    SELECT c.region, 
           COUNT(*) as total,
           SUM(CASE WHEN qa.isCorrect THEN 1 ELSE 0 END) as correct
    FROM quiz_answers qa 
    JOIN countries c ON qa.countryId = c.id
    WHERE qa.answeredAt > :since
    GROUP BY c.region
""")
fun getAccuracyByRegion(since: Long): Flow<List<RegionAccuracy>>

// Weakest region
@Query("""
    SELECT c.region,
           CAST(SUM(CASE WHEN qa.isCorrect THEN 1 ELSE 0 END) AS FLOAT) / COUNT(*) as accuracy
    FROM quiz_answers qa
    JOIN countries c ON qa.countryId = c.id
    WHERE qa.answeredAt > :since
    GROUP BY c.region
    ORDER BY accuracy ASC
    LIMIT 1
""")
fun getWeakestRegion(since: Long): Flow<RegionAccuracy?>

// Streak calculation
@Query("""
    SELECT COUNT(DISTINCT date(completedAt/1000, 'unixepoch')) 
    FROM quiz_sessions
    WHERE completedAt > :since
""")
fun getActiveDays(since: Long): Flow<Int>

// Weekly activity
@Query("""
    SELECT date(answeredAt/1000, 'unixepoch') as day, COUNT(*) as count
    FROM quiz_answers
    WHERE answeredAt > :weekStart
    GROUP BY day
    ORDER BY day
""")
fun getWeeklyActivity(weekStart: Long): Flow<List<DayActivity>>

// Question type accuracy (for adaptive difficulty)
@Query("""
    SELECT questionType,
           CAST(SUM(CASE WHEN isCorrect THEN 1 ELSE 0 END) AS FLOAT) / COUNT(*) as accuracy
    FROM quiz_answers
    GROUP BY questionType
""")
fun getAccuracyByQuestionType(): Flow<List<QuestionTypeAccuracy>>
```

---

## Architecture

### Clean Architecture — 3 layers

#### UI Layer (Jetpack Compose + ViewModels)
- Composable screens observe StateFlow from ViewModels
- ViewModels call Use Cases, never repositories directly
- Navigation via NavHost with sealed Screen class
- State modeled as sealed UiState (Loading, Success, Error)

#### Domain Layer (Use Cases + Models + Repository Interfaces)
- Pure Kotlin, no Android dependencies
- Use cases encapsulate single business operations
- Repository interfaces defined here, implemented in data layer

#### Data Layer (Room + Retrofit + WorkManager)
- Repository implementations coordinate local (Room) and remote (Retrofit)
- Offline-first: always read from Room, sync from API in background
- WorkManager handles periodic content sync and daily reminders
- Hilt modules provide all dependencies

### Dependency Injection (Hilt)
- `DatabaseModule`: provides Room database + all DAOs
- `NetworkModule`: provides OkHttpClient + Retrofit + ApiService
- `RepositoryModule`: binds repository interfaces to implementations

### Navigation
- `NavHost` with routes: home, quiz/{region}/{difficulty}, quiz-result/{sessionId}, stats, settings, permission-rationale, explorer
- Deep link: notification tap → quiz/daily/medium
- Nested nav graph for quiz flow (quiz → result)
- Arguments passed as route parameters

### WorkManager
- `ContentSyncWorker`: periodic (24h), constraints = requiresNetwork + requiresBatteryNotLow
  - Fetches all countries from API, upserts into Room
  - Generates next daily challenge
- `DailyReminderWorker`: periodic (24h), scheduled at user's preferred notification time
  - Sends notification with daily challenge title
  - Requires POST_NOTIFICATIONS permission (Android 13+)

### Runtime Permissions
- `ACCESS_COARSE_LOCATION`: for Local Discovery feature
  - Requested via just-in-time rationale screen
  - Used once to determine user's region, not stored
  - Graceful degradation: app works fully without it
- `POST_NOTIFICATIONS` (Android 13+): for daily reminders
  - Requested when user enables notifications in settings
  - Clear rationale provided

---

## Testing Strategy

### Unit Tests (non-GUI) — Required for all bands
- **DAO tests:** In-memory Room database, test complex queries (accuracy by region, streak, weekly activity)
- **Repository tests:** Mock API + in-memory Room, test offline-first sync logic
- **ViewModel tests:** Mock use cases, verify state transitions (Loading → Success → Error)
- **Use case tests:** Test quiz generation logic, scoring, difficulty escalation
- **API parsing tests:** Test CountryDto → Country mapping

Example test scenarios:
```kotlin
// DAO test — accuracy by region
@Test
fun getAccuracyByRegion_returnsCorrectPercentage() {
    // Insert 10 answers for Asia: 8 correct, 2 wrong
    // Assert accuracy = 0.8 for Asia
}

// ViewModel test — state transitions
@Test
fun loadQuiz_emitsLoadingThenSuccess() {
    // Mock use case returns quiz questions
    // Collect StateFlow values
    // Assert first emission is Loading, second is Success with questions
}

// Use case test — quiz generation
@Test
fun generateQuiz_returnsMixedQuestionTypes() {
    // Given 48 Asian countries in repository
    // When generating a medium difficulty quiz
    // Assert 10 questions returned with at least 3 different question types
}

// Repository test — offline-first
@Test
fun getCountries_returnsCachedDataWhenOffline() {
    // Insert countries into Room with recent cachedAt
    // Mock API to throw IOException
    // Assert repository still returns countries from cache
}
```

### GUI Tests (Compose) — Required for HD (85-100%) band
The rubric explicitly requires "unit tests of the model code AND tests of the GUI" for Excellent.

Use `createComposeRule()` with the Compose testing library.

Example test scenarios:
```kotlin
// Quiz screen — answer selection
@Test
fun selectCorrectAnswer_showsGreenFeedback() {
    composeTestRule.setContent { QuizScreen(...) }
    // Find the correct answer option by text
    composeTestRule.onNodeWithText("Vietnam").performClick()
    // Assert the +10 points feedback is displayed
    composeTestRule.onNodeWithText("+10 points").assertIsDisplayed()
}

// Quiz screen — progress advances
@Test
fun answerQuestion_advancesProgressCounter() {
    composeTestRule.setContent { QuizScreen(...) }
    // Assert shows "1 / 10"
    composeTestRule.onNodeWithText("1 / 10").assertIsDisplayed()
    // Click an answer
    composeTestRule.onNodeWithText("Vietnam").performClick()
    // Wait for next question
    // Assert shows "2 / 10"
    composeTestRule.onNodeWithText("2 / 10").assertIsDisplayed()
}

// Settings screen — toggle persistence
@Test
fun toggleTimedMode_updatesPreferences() {
    composeTestRule.setContent { SettingsScreen(...) }
    composeTestRule.onNodeWithText("Timed mode").performClick()
    // Assert ViewModel state reflects change
}

// Navigation — region selection navigates to quiz
@Test
fun tapRegion_navigatesToQuizScreen() {
    composeTestRule.setContent { BorderRunNavGraph(...) }
    composeTestRule.onNodeWithText("Asia").performClick()
    // Assert quiz screen is displayed
    composeTestRule.onNodeWithText("/ 10").assertIsDisplayed()
}

// Home screen — daily challenge card renders
@Test
fun homeScreen_displaysDailyChallengeCard() {
    composeTestRule.setContent { HomeScreen(...) }
    composeTestRule.onNodeWithText("Daily Challenge").assertIsDisplayed()
    composeTestRule.onNodeWithText("Start Challenge").assertIsDisplayed()
}
```

### Testing Dependencies
```kotlin
// build.gradle.kts (app)
testImplementation("junit:junit:4.13.2")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
testImplementation("androidx.room:room-testing:2.6.1")
testImplementation("io.mockk:mockk:1.13.9")
testImplementation("app.cash.turbine:turbine:1.0.0") // Flow testing

androidTestImplementation("androidx.compose.ui:ui-test-junit4")
androidTestImplementation("androidx.test.ext:junit:1.1.5")
androidTestImplementation("androidx.test:runner:1.5.2")
debugImplementation("androidx.compose.ui:ui-test-manifest")
```

---

## Code Quality Guidelines

The rubric awards 10% for code quality. Ensure Claude Code follows these:

- **Naming conventions:** camelCase for functions/variables, PascalCase for classes, SCREAMING_SNAKE for constants
- **KDoc comments** on all public classes and functions:
  ```kotlin
  /**
   * Generates a quiz with [questionCount] questions for the given [region] and [difficulty].
   * Questions are randomly selected across available question types for the difficulty tier.
   * @return List of [QuizQuestion] ready for presentation
   */
  suspend fun generateQuiz(region: String, difficulty: String, questionCount: Int = 10): List<QuizQuestion>
  ```
- **File organization:** One class per file, logically grouped in packages
- **No magic numbers:** Use named constants (`const val DEFAULT_QUIZ_LENGTH = 10`)
- **Consistent formatting:** Standard Kotlin code style (ktlint compatible)
- **Meaningful variable names:** `countriesByRegion` not `list1`, `accuracyPercentage` not `acc`

When prompting Claude Code, add: "Include KDoc comments on all public classes and functions. Use meaningful variable names and named constants instead of magic numbers."

---

## Responsive Design

The UI rubric mentions "works smoothly on different devices." Ensure:

- Use `fillMaxWidth()` instead of hardcoded widths
- Use `WindowSizeClass` for adaptive layouts if needed
- Test on both phone and tablet emulator sizes
- Use `dp` and `sp` units consistently (never `px`)
- LazyColumn for scrollable content (stats, explorer mode)
- Proper padding with `WindowInsets` for system bars

---

## Ethics-to-Design Mapping (CP5307)

These are the explicit connections between your Assessment 2 research and Border Run's design decisions. Reference these in your self-reflection:

| Assessment 2 Principle | Border Run Implementation | ACS Reference |
|---|---|---|
| Data minimisation | `ACCESS_COARSE_LOCATION` not FINE; location used once, never stored | ACS S4 |
| Informed consent | Permission rationale screen with plain-language explanation before system dialog | ACS S5, Clause 1.2.3 |
| Transparency | Settings screen shows exactly what data is stored and where | ACS S5 |
| User autonomy | All features work without permissions; "Skip" always available; clear data anytime | Clause 1.2.1 |
| Privacy by default | Location OFF by default, notifications OFF by default | ACS S4 |
| No dark patterns | No guilt-tripping if permissions denied; no manipulative streaks/notifications | Clause 1.2.3 |
| Age-appropriate design | Content suitable for 13-17; no addictive gambling mechanics; educational focus | Clause 1.2.2 |
| Professional trade-off | Chose WorkManager with constraints over continuous polling — less invasive, battery-efficient | ACS S4, Clause 1.2.4 |
| Third-party SDK audit | No advertising SDKs; only RestCountries API (free, no tracking) | Enck et al. (2014) |

---

## GitHub Strategy

### Setup (do this FIRST before any coding)
```bash
cd your-project-folder
git init
git add .
git commit -m "Initial project scaffold from Android Studio"
git remote add origin https://github.com/yourusername/borderrun.git
git push -u origin main
```

### Commit milestones (aim for 15-25+ commits minimum)
1. `Initial project scaffold from Android Studio`
2. `Add project dependencies (Hilt, Room, Retrofit, Navigation, WorkManager)`
3. `Set up theme: colors, typography (Nunito), gradient background`
4. `Create Room database entities and DAOs`
5. `Implement Retrofit API service and CountryDto model`
6. `Set up Hilt dependency injection modules`
7. `Implement repository layer with offline-first sync`
8. `Create navigation graph with screen routes`
9. `Build Home screen UI and ViewModel`
10. `Build Quiz screen with question rendering`
11. `Implement quiz generation logic (all 14 question types)`
12. `Build Quiz Result screen`
13. `Build Statistics screen with region accuracy bars`
14. `Build Settings screen with privacy dashboard`
15. `Implement permission rationale flow`
16. `Add WorkManager content sync worker`
17. `Add WorkManager daily reminder worker`
18. `Build Explorer mode screen`
19. `Implement Streak and Speed Round game modes`
20. `Add unit tests (DAO, Repository, ViewModel, UseCase)`
21. `Add Compose GUI tests`
22. `Add README with screenshots and documentation`
23. `Final polish and bug fixes`

### README template
```markdown
# Border Run 🌍

A geography quiz app for secondary school students built with Kotlin and Jetpack Compose.

## Screenshots
[Include 4-6 screenshots of key screens]

## Features
- 14 question types across 3 difficulty tiers
- 4 game modes: Classic, Daily Challenge, Streak, Speed Round
- Offline-first with automatic content sync
- Location-aware Local Discovery mode
- Comprehensive progress tracking and statistics
- Privacy-first design with transparent data handling

## Architecture
- Clean Architecture (UI / Domain / Data layers)
- Jetpack Compose with Material Design 3
- Room database with 5 tables and complex queries
- Retrofit with RestCountries API
- Hilt dependency injection
- WorkManager for background sync
- Navigation Component with deep linking

## Tech Stack
- Kotlin
- Jetpack Compose + Material 3
- Room Database
- Retrofit + OkHttp
- Hilt (Dependency Injection)
- WorkManager
- Navigation Component
- JUnit + Mockk + Compose Testing

## API
- [RestCountries API](https://restcountries.com/) — free, no auth required

## Setup
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle
4. Run on emulator or device (API 26+)

## Ethical Design
This app implements privacy-by-design principles informed by the ACS Code of Ethics:
- Coarse location only, never stored
- All data stored locally on device
- Transparent data dashboard in Settings
- No third-party tracking or advertising SDKs

## AI Declaration
This project was developed with assistance from Claude (Anthropic).
See the AI Declaration form submitted with the assignment.
```

---

## Self-Reflection Outline (1000 words, Gibbs' Reflective Cycle)

Write this AFTER the app is built. Submit as PDF.

### Description (~150 words)
- Border Run: geography quiz app for secondary students
- Built with Kotlin, Jetpack Compose, Room, Retrofit
- Assessment 2 focused on privacy & location tracking in social media apps
- Key ACS principles: S4 (Protect Privacy), S5 (Informed Consent), Clause 1.2.1 (Public Interest), Clause 1.2.3 (Honesty)

### Feelings (~150 words)
- Initial reaction to applying ethics research to actual code
- Tension between wanting rich features vs. data minimisation
- How it felt to implement permission rationale (was it natural or forced?)
- Confidence/uncertainty around architectural decisions
- Experience using AI-assisted development (Claude)

### Evaluation (~200 words)
- What went well: offline-first architecture, permission flow, privacy dashboard
- What didn't: challenges with Room queries, testing, time management
- Ethical successes: coarse location choice, transparent data display, no dark patterns
- Ethical gaps: anything you wish you'd done differently

### Analysis (~200 words)
- WHY the permission rationale screen works (connects to Assessment 2 research on just-in-time consent)
- WHY coarse over fine location (ACS S4 data minimisation — directly from your presentation)
- HOW Android platform constraints shaped ethical outcomes (background execution limits align with privacy)
- HOW WorkManager constraints prevent battery-invasive behaviour
- Trade-off analysis: feature richness vs. permission minimisation

### Conclusion (~150 words)
- Ethics is not separate from technical practice — it IS technical practice
- Android's permission model provides scaffolding but responsibility lies with the developer
- Privacy-by-design defaults matter more than privacy-by-option
- AI-assisted development still requires ethical judgement from the developer

### Action Plan (~150 words)
- Future apps: conduct Privacy Impact Assessment before architecture design
- Audit all third-party dependencies for data collection
- Implement privacy dashboards as standard practice
- Apply accessibility standards (TalkBack, content descriptions) more thoroughly
- Continue using Gibbs' Cycle for reflective practice in professional work

---

## Submission Checklist

Before submitting, verify ALL of the following:

### App
- [ ] App compiles and runs without crashes
- [ ] All 4 core screens functional (Landing, Quiz, Settings, Stats)
- [ ] API integration works (countries load from RestCountries)
- [ ] Room database persists data across app restarts
- [ ] Navigation works correctly between all screens
- [ ] WorkManager workers are registered and functional
- [ ] Runtime permissions handled with rationale (COARSE_LOCATION, POST_NOTIFICATIONS)
- [ ] Settings toggles persist preferences
- [ ] Quiz generates questions correctly across all types
- [ ] Stats screen shows accurate data from Room queries
- [ ] App handles offline state gracefully (cached data)
- [ ] App handles permission denial gracefully (no crashes, features degrade)

### Code Quality
- [ ] KDoc comments on public classes and functions
- [ ] Consistent naming conventions
- [ ] No hardcoded strings (use string resources for UI text)
- [ ] No magic numbers (use named constants)
- [ ] Clean architecture separation maintained

### Testing
- [ ] Unit tests pass (DAO, Repository, ViewModel, UseCase)
- [ ] GUI tests pass (Compose interaction tests)
- [ ] Tests cover meaningful scenarios, not just trivial assertions

### GitHub
- [ ] 15+ commits showing continuous progress
- [ ] README with screenshots and documentation
- [ ] Repository shared with lecturer and subject coordinator
- [ ] .gitignore properly excludes build files

### Submission Files
- [ ] `.zip` file exported from Android Studio (File → Export → Export to zip file)
- [ ] `.pdf` self-reflection (1000 words, Gibbs' Reflective Cycle)
- [ ] GitHub repository link provided in submission
- [ ] JCU Declaration of AI-Generated Material form completed and submitted
- [ ] All files uploaded to LearnJCU Assignment 3 dropbox
