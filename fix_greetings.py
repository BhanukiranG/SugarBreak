import codecs
import re

path = 'app/src/main/java/com/main/sugarbreak/ui/home/HomeScreen.kt'
with codecs.open(path, 'r', 'utf-8') as f:
    content = f.read()

content = re.sub(r'Good morning\.*', 'Good morning\!"', content)
content = re.sub(r'Good afternoon\.*', 'Good afternoon\!"', content)
content = re.sub(r'Good evening\.*', 'Good evening\!"', content)

with codecs.open(path, 'w', 'utf-8') as f:
    f.write(content)
print('Updated greetings perfectly!')
