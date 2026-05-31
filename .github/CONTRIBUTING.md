## How to contribute to Recents

- Issues
  - verify if issue wasn't already reported in [Issues](github.com/tymwitko/Recents/issues)
  - if not, provide the details according to the template
- Code contribution
  - LLM usage:
    - please do not submit AI-generated code
  - architecture: MVVM
    - every new feature should have its `ViewModel`, aggregating functionality from different backend classes
    - use koin and add entries to the existing `KoinModule`
    - utilize existing classes whenever possible, you can extract feature-specific classes to the common package if needed
