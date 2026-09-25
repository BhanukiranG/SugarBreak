### 🎨 Prompt for UI/UX Design Generation:

**App Name:** SugarBreak
**App Core Concept:** A mobile application designed to help users track and reduce their added/free sugar consumption through simple daily awareness. It emphasizes a supportive, non-judgmental, and positive approach to habit tracking.
**Design Style & Vibe:** Modern, clean, uplifting, and premium. Use a soft and encouraging color palette (mint greens, soft blues, warm neutrals) to reduce guilt. Implement glassmorphism for cards, large readable typography, and smooth micro-animations. The app should feel native, highly responsive, and user-friendly.

Please design the following 6 core screens and their specific functionalities:

#### 1. Onboarding Flow (5-Step Setup)
*   **Purpose:** Configure the user's tracking rules and set their goals smoothly.
*   **UI Elements:**
    *   A linear progress bar at the top indicating the step (1 through 5).
    *   **Step 1:** Welcome/Introductory text explaining the app's supportive philosophy.
    *   **Step 2 (Rule on slip):** Radio buttons or selectable cards for "What happens if you slip?" Options: *Continue tracking normally, Add a recovery day, Reset my streak*.
    *   **Step 3 (Target Days):** Options to select challenge length: *7, 14, 21, 30, 60, or 90 days*.
    *   **Step 4 (Reminder):** Native-looking time picker to set a daily reminder notification.
    *   **Step 5 (Summary):** A summary of their choices and a prominent "Start Challenge" CTA.
    *   **Navigation:** "Back" and "Next" buttons at the bottom.

#### 2. Home Screen (Dashboard)
*   **Purpose:** The central hub where users see their progress at a glance.
*   **UI Elements:**
    *   **Top Bar:** App branding.
    *   **Greeting:** "Good morning/evening!" based on the time of day.
    *   **Hero Section:** A visually stunning, elevated card displaying the **"Current Streak"** with a massive, bold number for the days (e.g., "12 days"). 
    *   **Progress Text:** Showing current successful days vs target days (e.g., "12 / 30 successful days").
    *   **Primary CTA:** A large, inviting "Check In" button. If the user has already checked in for the day, replace this with a supportive success message card (e.g., "You've checked in today. Keep it up!").
    *   **Bottom Navigation Bar:** Icons for Home, History, Stats, and Settings.

#### 3. Daily Check-In Screen
*   **Purpose:** A frictionless screen to log the day's sugar consumption.
*   **UI Elements:**
    *   **Header:** "How was today?"
    *   **Core Choices:** Two large, side-by-side buttons or selectable zones: **"Stayed on track"** (positive styling) and **"Had sugar"** (neutral/soft styling, avoid harsh reds to prevent guilt).
    *   **Conditional UI:** If "Had sugar" is selected, a slide-down section appears asking "What did you have?" with radio buttons for common reasons.
    *   **Submit Button:** At the bottom to finalize the daily log.
    *   **Post-Submission State:** Replaces the form with a big congratulatory message ("Nice work!") or a supportive message ("That's okay, keep going!") and a button to return Home.

#### 4. History Screen (Calendar View)
*   **Purpose:** To visualize long-term progress over the current month.
*   **UI Elements:**
    *   **Header:** Current Month and Year (e.g., "September 2026").
    *   **Calendar Grid:** A standard 7-column calendar grid.
    *   **Data Visualization:** Each day should be a colored circle indicating the status: 
        *   *Green/Positive Color* = Success
        *   *Soft Red/Orange* = Slip
        *   *Grey* = Skipped
        *   *Outlined/Neutral* = Pending/Future
    *   The grid should be clean, perfectly spaced, and easy to read.

#### 5. Statistics Screen
*   **Purpose:** Deep dive into user analytics.
*   **UI Elements:**
    *   A vertically scrollable dashboard of stat cards.
    *   **Top Cards:** "Current Streak" and "Best Streak" (prominent styling).
    *   **Split Cards:** Side-by-side metrics for "Successful Days" and "Slip Days".
    *   **Success Rate Card:** Showing the percentage of successful days (e.g., "85.5%").
    *   **Challenge Progress Card:** A linear progress bar showing how close they are to finishing their current challenge (e.g., 21-day challenge) with a percentage label.

#### 6. Settings Screen
*   **Purpose:** Manage preferences.
*   **UI Elements:**
    *   Standard native list styling.
    *   **Notifications Section:** A toggle switch for "Daily Reminder" and a clickable row displaying the currently set "Reminder Time" (opens a time picker modal).
    *   **Tracking Section:** Clickable rows showing the current "Tracking Rule" and "Challenge Goal" allowing the user to change their onboarding choices.
