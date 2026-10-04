import codecs
import re

path = 'app/src/main/java/com/main/sugarbreak/ui/onboarding/OnboardingScreen.kt'
with codecs.open(path, 'r', 'utf-8') as f:
    content = f.read()

content = re.sub(
    r'GlassBox\(shape = RoundedCornerShape\(50\)\) \{.*?Text\(\s*text = "Gentle & Sustainable".*?\}\s*\}\s*Spacer\(modifier = Modifier\.height\(14\.dp\)\)\s*',
    '',
    content,
    flags=re.DOTALL
)

with codecs.open(path, 'w', 'utf-8') as f:
    f.write(content)
print('Removed GlassBox chip!')
