import codecs

path = 'app/src/main/java/com/main/sugarbreak/ui/onboarding/OnboardingScreen.kt'
with codecs.open(path, 'r', 'utf-8') as f:
    lines = f.readlines()

new_lines = []
skip = False
for line in lines:
    if 'Box(' in line and 'clip(RoundedCornerShape(50))' in line:
        skip = True
    if skip and 'Spacer(modifier = Modifier.height(14.dp))' in line:
        skip = False
        continue
    if not skip:
        new_lines.append(line)

with codecs.open(path, 'w', 'utf-8') as f:
    f.writelines(new_lines)
print('Removed chip strictly!')
