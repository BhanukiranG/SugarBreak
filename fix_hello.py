import codecs
import re

path = 'app/src/main/java/com/main/sugarbreak/ui/onboarding/OnboardingScreen.kt'
with codecs.open(path, 'r', 'utf-8') as f:
    content = f.read()

content = re.sub(r'text = "Hello, \.*?"', 'text = "Hello, \!"', content)

with codecs.open(path, 'w', 'utf-8') as f:
    f.write(content)
print('Updated Onboarding greeting strictly via file!')
