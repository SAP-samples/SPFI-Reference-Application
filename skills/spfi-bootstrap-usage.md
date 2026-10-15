# How to Use the SPFI Bootstrap Skill

This guide explains how to use the `spfi-bootstrap` skill to generate a complete SPFI reference application in your preferred programming language.

---

## Prerequisites

- [Claude Code](https://claude.ai/code) installed (`npm install -g @anthropic-ai/claude-code` or download the desktop app)
- The `spfi-bootstrap.md` skill file (provided by your SAP contact)

---

## Step 1: Create a project directory

Create an empty directory for your new application:

```bash
mkdir my-spfi-app
cd my-spfi-app
```

---

## Step 2: Install the skill

Create the `.claude/commands` directory and copy the skill file into it:

```bash
mkdir -p .claude/commands
cp /path/to/spfi-bootstrap.md .claude/commands/
```

Your directory should look like this:

```
my-spfi-app/
└── .claude/
    └── commands/
        └── spfi-bootstrap.md
```

---

## Step 3: Open the project in Claude Code

Open Claude Code in your project directory:

```bash
claude .
```

Or open the directory in your IDE (VS Code, JetBrains) and launch Claude Code from the IDE extension.

---

## Step 4: Run the skill

In the Claude Code chat, type:

```
/spfi-bootstrap
```

Claude will ask which language you want. Select from the list or type your own.

You can also pass the language directly as an argument to skip the prompt:

```
/spfi-bootstrap Python
```

| Language | Framework used |
|----------|---------------|
| Python | FastAPI |
| Node.js / JavaScript | Express |
| TypeScript | Express + TypeScript |
| Go | Gin |
| Java | Spring Boot |
| C# | ASP.NET Core |
| Ruby | Sinatra |
| PHP | Slim |

Any other language is also supported — Claude will pick the most idiomatic REST framework.

---

## Step 5: Wait for generation to complete

Claude will generate all required files. This takes 2–5 minutes. Do not interrupt.

Generated files include:
- All 7 SPFI tenant API endpoints
- Background worker for async state simulation
- File-based JSON storage (ready to replace with your own DB)
- Swagger UI for interactive testing
- Postman collection
- `../README.md` and `GETTING_STARTED.md`

---

## Step 6: Install dependencies and run

Follow the instructions in the generated `../README.md`. Quick start for each language:

**Python:**
```bash
python3 -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
PROFILE=local python3 main.py
```

**Node.js:**
```bash
npm install
PROFILE=local node index.js
```

**Go:**
```bash
go mod tidy
PROFILE=local go run .
```

**Java:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

---

## Step 7: Verify the app is running

```bash
curl http://localhost:8080/health
# Expected: { "status": "UP" }
```

---

## Step 8: Explore the API

Open Swagger UI in your browser:

```
http://localhost:8080/swagger
```

Or follow the step-by-step curl walkthrough in `GETTING_STARTED.md`.

---

## Step 9: Replace the storage layer

The generated app stores tenants in a JSON file (`./data/tenants.json`). Every storage read/write point is marked with:

```
// Custom implementation can go here
```

Search for this comment in the storage layer file and replace the file-based implementation with your own database, cache, or storage system.

---

## Notes

- **Local mode** (`PROFILE=local`): plain HTTP, no client certificates required. Use for development only.
- **Default mode** (`PROFILE=default`): HTTPS with mutual TLS (mTLS). Required for production. See `../README.md` for certificate setup instructions.
- The `HEADER_RETRY_AFTER_SECONDS` env var (default: `120`) controls how long the operator waits between status polls.
- The `WORKER_DELAY_SECONDS` env var (default: `5`) controls how long the simulated async worker takes to complete an operation.
