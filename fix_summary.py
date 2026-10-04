import codecs
import re

path = 'app/src/main/java/com/main/sugarbreak/ui/onboarding/OnboardingScreen.kt'
with codecs.open(path, 'r', 'utf-8') as f:
    content = f.read()

search = '''                SummaryRow(label = "Rule on slip", value = state.selectedRule.name.replace("_", " "))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                val timeString = "\:\"
                SummaryRow(label = "Daily Reminder", value = timeString)'''

replace = '''                val rawRuleName = state.selectedRule.name.replace("_", " ").lowercase()
                val ruleValue = rawRuleName.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                SummaryRow(label = "Rule on slip", value = ruleValue)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a", java.util.Locale.US)
                val timeString = java.time.LocalTime.of(state.reminderHour, state.reminderMinute).format(timeFormatter)
                SummaryRow(label = "Daily Reminder", value = timeString)'''

content = content.replace(search, replace)

with codecs.open(path, 'w', 'utf-8') as f:
    f.write(content)
print('Updated Summary logic!')
