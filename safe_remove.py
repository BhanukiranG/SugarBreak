import codecs
import re

path = 'app/src/main/java/com/main/sugarbreak/ui/onboarding/OnboardingScreen.kt'
with codecs.open(path, 'r', 'utf-8') as f:
    lines = f.readlines()

new_lines = []
skip = False
for line in lines:
    if 'GlassBox(shape = RoundedCornerShape(50)) {' in line and 'Column {' in new_lines[-1]:
        skip = True
    if skip and 'Spacer(modifier = Modifier.height(14.dp))' in line:
        skip = False
        continue
    if not skip:
        new_lines.append(line)

with codecs.open(path, 'w', 'utf-8') as f:
    f.writelines(new_lines)
print('Removed chip safely!')
