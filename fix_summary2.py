import codecs
import re

path = 'app/src/main/java/com/main/sugarbreak/ui/onboarding/OnboardingScreen.kt'
with codecs.open(path, 'r', 'utf-8') as f:
    content = f.read()

# Replace rule on slip line
content = re.sub(
    r'SummaryRow\(label = "Rule on slip".*?\)',
    'val rawRuleName = state.selectedRule.name.replace("_", " ").lower()\n                val ruleValue = rawRuleName.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }\n                SummaryRow(label = "Rule on slip", value = ruleValue)',
    content
)

# Replace timeString line
content = re.sub(
    r'val timeString = ".*?padStart.*?"',
    'val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a", java.util.Locale.US)\n                val timeString = java.time.LocalTime.of(state.reminderHour, state.reminderMinute).format(timeFormatter)',
    content
)

# The Kotlin String 'lower()' does not exist, it is 'lowercase()'.
content = content.replace('.lower()', '.lowercase()')

with codecs.open(path, 'w', 'utf-8') as f:
    f.write(content)
print('Updated via regex safely!')
