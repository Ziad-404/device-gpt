# Contributing to DeviceGPT

Thank you for your interest in contributing to DeviceGPT! This document provides guidelines and instructions for contributing.

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Coding Standards](#coding-standards)
- [Testing Requirements](#testing-requirements)
- [Pull Request Process](#pull-request-process)
- [Issue Reporting](#issue-reporting)

## 📜 Code of Conduct

- Be respectful and inclusive
- Welcome newcomers and help them learn
- Focus on constructive feedback
- Respect different viewpoints and experiences

## 🚀 Getting Started

1. **Fork the repository**
   ```bash
   git clone https://github.com/yourusername/debugger.git
   cd debugger
   ```

2. **Set up your development environment**
   - Follow the setup instructions in [README.md](README.md)
   - Ensure all tests pass: `./gradlew :app:testDebugUnitTest`

3. **Create a branch**
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/your-bug-fix
   ```

## 🔄 Development Workflow

### Branch Naming Convention

- `feature/` - New features
- `fix/` - Bug fixes
- `docs/` - Documentation updates
- `refactor/` - Code refactoring
- `test/` - Test additions/updates
- `chore/` - Maintenance tasks

### Commit Messages

Follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Test additions/updates
- `chore`: Maintenance tasks

**Examples:**
```
feat(power): Add camera power consumption measurement
fix(ui): Fix crash in power consumption card
docs(readme): Update setup instructions
```

## 📝 Coding Standards

### Kotlin Style Guide

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Keep functions small and focused
- Add KDoc comments for public functions and classes
- Use `val` instead of `var` when possible

### Code Formatting

- Use 4 spaces for indentation
- Maximum line length: 120 characters
- Use trailing commas in multi-line declarations
- Run `./gradlew :app:ktlintFormat` before committing

### Architecture Guidelines

- Follow MVVM pattern
- Keep UI logic in Composables
- Business logic in ViewModels or utility classes
- Use sealed classes for state management
- Prefer composition over inheritance

### Example Code Structure

```kotlin
/**
 * Brief description of what this class/function does
 *
 * @param param1 Description of param1
 * @return Description of return value
 */
class ExampleClass {
    private val privateProperty: String = "value"
    
    fun publicFunction(param1: String): Result {
        // Implementation
    }
}
```

## 🧪 Testing Requirements

### Test Coverage

- All new features must include tests
- Bug fixes must include regression tests
- Aim for >80% code coverage for new code
- Update existing tests when modifying functionality

### Writing Tests

**Unit Tests** (`app/src/test/`):
```kotlin
@Test
fun `test function name with expected behavior`() {
    // Arrange
    val input = "test"
    
    // Act
    val result = functionUnderTest(input)
    
    // Assert
    assertEquals(expected, result)
}
```

**UI Tests** (`app/src/androidTest/`):
```kotlin
@Test
fun testUIComponent() {
    composeTestRule.setContent {
        YourComposable()
    }
    
    composeTestRule.onNodeWithText("Expected Text")
        .assertIsDisplayed()
}
```

### Running Tests

```bash
# Run all unit tests
./gradlew :app:testDebugUnitTest

# Run specific test class
./gradlew :app:testDebugUnitTest --tests "com.teamz.lab.debugger.YourTestClass"

# Run UI tests
./gradlew :app:connectedAndroidTest

# Check coverage
./gradlew :app:testDebugUnitTest
./gradlew :app:jacocoTestReport
```

## 🔍 Pull Request Process

### Before Submitting

1. **Update Documentation**
   - Update README.md if needed
   - Add/update code comments
   - Update CHANGELOG.md (if applicable)

2. **Run Tests**
   ```bash
   ./gradlew :app:testDebugUnitTest
   ./gradlew :app:connectedAndroidTest
   ```

3. **Check Code Quality**
   ```bash
   ./gradlew :app:ktlintCheck
   ./gradlew :app:lint
   ```

4. **Test on Device**
   - Test on at least one physical device
   - Test on different Android versions if possible

### PR Checklist

- [ ] Code follows the project's style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex code
- [ ] Documentation updated
- [ ] Tests added/updated and passing
- [ ] No new warnings introduced
- [ ] Tested on device/emulator
- [ ] PR description is clear and detailed

### PR Description Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
How was this tested?

## Screenshots (if applicable)
Add screenshots here

## Checklist
- [ ] Tests pass
- [ ] Documentation updated
- [ ] Code follows style guidelines
```

### Review Process

- Maintainers will review your PR
- Address review comments promptly
- Be open to feedback and suggestions
- Keep PRs focused and reasonably sized

## 🐛 Issue Reporting

### Before Creating an Issue

1. Check if the issue already exists
2. Search closed issues for similar problems
3. Verify the issue on the latest version

### Issue Template

```markdown
**Describe the bug**
A clear description of what the bug is.

**To Reproduce**
Steps to reproduce:
1. Go to '...'
2. Click on '...'
3. See error

**Expected behavior**
What you expected to happen.

**Screenshots**
If applicable, add screenshots.

**Device Information**
- Device: [e.g., Samsung Galaxy S21]
- Android Version: [e.g., Android 13]
- App Version: [e.g., 3.0.1]

**Additional context**
Any other relevant information.
```

### Issue Labels

- `bug` - Something isn't working
- `feature` - New feature request
- `enhancement` - Improvement to existing feature
- `documentation` - Documentation improvements
- `question` - Questions or discussions
- `help wanted` - Extra attention needed

## 🎯 Areas for Contribution

### High Priority

- Power consumption measurement improvements
- UI/UX enhancements
- Performance optimizations
- Test coverage improvements
- Documentation updates

### Good for Beginners

- Documentation improvements
- UI polish and animations
- Test additions
- Bug fixes with clear reproduction steps
- Translation/localization

## 📚 Resources

- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Android Developer Guide](https://developer.android.com/guide)
- [Testing Guide](TESTING_GUIDE.md)

## ❓ Questions?

- Open a [GitHub Discussion](https://github.com/yourusername/debugger/discussions)
- Check existing issues and discussions
- Review the codebase and documentation

## 🙏 Thank You!

Your contributions make DeviceGPT better for everyone. Thank you for taking the time to contribute!

---

**Happy Coding! 🚀**

