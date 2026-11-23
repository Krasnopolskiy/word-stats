# word-stats

A console program that reads a UTF-8 text file and prints its line, word and character counts and its most
frequent words.

```bash
./gradlew jar                                        # gradlew.bat on Windows
java -jar build/libs/word-stats.jar notes.txt --top 3
```

```text
File: notes.txt
Lines: 3
Words: 18
Characters: 90
Top 3 words:
  1. the  4
  2. dog  2
  3. fox  2
```

You need JDK 21 or newer, for example Temurin from adoptium.net. The program has no runtime dependencies, so the
same jar runs on Linux, macOS and Windows. Gradle downloads itself and JUnit on the first run.

## Usage

```text
java -jar word-stats.jar <file> [--top N]
```

| Argument | Meaning |
|---|---|
| `<file>` | a UTF-8 text file |
| `--top N` | how many frequent words to show, from 1 to 1000, default 10 |
| `--help` | print the usage |

A word is a run of letters and digits in any alphabet. An apostrophe or a hyphen between letters stays inside the
word, so `don't` and `well-known` count as one word each. The program ignores case. Characters count every character
of the file, line breaks included. Words with the same count go in alphabetical order.

Exit codes: `0` success, `1` the file is missing, unreadable or not UTF-8, `2` bad arguments.

## Code

| Class | Role |
|---|---|
| `Main` | entry point: reads the file, prints the result, maps errors to exit codes |
| `Options` | parses and checks the arguments |
| `TextAnalyzer` | counts lines, words and characters and picks the top words |
| `StatsFormatter` | turns the result into text |
| `TextStats`, `WordCount` | records with the result |

## Testing

```bash
./gradlew test     # unit tests
./gradlew build    # compile, test and build the jar
```

The suite has 22 tests. `TextAnalyzerTest` covers counting, case, Cyrillic text, apostrophes, hyphens and ordering.
`MainTest` runs the program on temporary files and checks the output, error messages and exit codes. The report
lands in `build/reports/tests/test/index.html`.

## CI/CD

`.github/workflows/ci-cd.yml` runs on each push and pull request. The job sets up JDK 21, restores the Gradle cache,
downloads the dependencies, builds and tests the project and uploads `word-stats.jar` as the `word-stats` artifact.
You can download the jar from the run page under Artifacts.

The cache holds `~/.gradle/caches` and `~/.gradle/wrapper`. Its key comes from the OS and a hash of the build files,
so a run downloads Gradle and JUnit again only after a change to `build.gradle.kts`, `settings.gradle.kts` or the
wrapper version.

## Docker

`Dockerfile` is a two-stage build. The build stage runs on `eclipse-temurin:21-jdk`, downloads the dependencies and
builds the jar with the Gradle wrapper. The runtime stage starts from `eclipse-temurin:21-jre-alpine` and copies in
only the jar and a sample file, so the sources, the Gradle wrapper and the JDK never reach the final image. The
image runs as the unprivileged `app` user and is about 214 MB.

```bash
docker build -t word-stats .
docker run --rm word-stats                       # analyses the bundled sample
docker run --rm -v "$PWD/notes.txt:/data/in.txt:ro" word-stats /data/in.txt --top 3
```

The `docker` job in the workflow builds the image after the tests pass and pushes it to the GitHub Container
Registry. `docker/metadata-action` tags each image by branch, by the short commit SHA (`sha-<hash>`) and, on `main`,
with `latest`; a Git tag like `v1.2.3` also adds the version tags. Pulls are public:

```bash
docker pull ghcr.io/krasnopolskiy/word-stats:latest
docker run --rm ghcr.io/krasnopolskiy/word-stats:latest
```
