import codecs

path = 'app/src/test/java/com/main/sugarbreak/ui/onboarding/OnboardingViewModelTest.kt'
with codecs.open(path, 'r', 'utf-8') as f:
    content = f.read()

missing_methods = '''
        var isDarkMode: Boolean? = null
        override fun getIsDarkMode(): Flow<Boolean?> = flowOf(isDarkMode)
        override suspend fun setIsDarkMode(isDark: Boolean?) {
            isDarkMode = isDark
        }
'''

content = content.replace('var userName = ""', 'var userName = ""' + missing_methods)

with codecs.open(path, 'w', 'utf-8') as f:
    f.write(content)
print('Added missing mock methods!')
