# CDTV — CloudStream Plugin Repository

A [CloudStream 3](https://github.com/recloudstream/cloudstream) plugin repository for **BDIX Live TV** channels available at `http://30.30.30.30/`.

> **Note:** This plugin only works if you are connected to a **BDIX-peered ISP** in Bangladesh. It will **not** work on mobile data or non-BDIX internet connections.

---

## 📺 Available Plugin

### CDTV (BDIX Live TV)
Live streaming of Bangladeshi and satellite TV channels via the BDIX local network.

**Categories:**
- 🏆 Sports — T Sports, GTV, Star Sports 1/2, Sony Ten 1/2/3, Star Sports Select, A Sports, PTV Sports
- 🇧🇩 Bangla — Jamuna TV, Somoy TV, Channel I, Masranga, DBC News, ATN Bangla, ATN News, Independent TV, Zee Bangla, Star Jalsha, Jalsha Movies, Colors Bangla, Sony Aath, Enterr10 Bangla
- 🇮🇳 Hindi — Star Plus, Star Gold, Sony Entertainment, Colors Hindi, Zee Cinema, Sony Max HD
- 🌐 English — Star Movies, Sony Pix, Discovery, National Geographic
- 📺 Others — Nickelodeon, Disney Junior, 9X Jalwa, Sangeet Bangla

---

## 📲 Add this Repository to CloudStream

1. Open **CloudStream 3** on your Android device
2. Go to **Settings → Extensions → Add repository**
3. Paste this URL:

```
https://raw.githubusercontent.com/Avishek-058/cdtv/builds/plugins.json
```

---

## 🛠 Developer Setup

### Prerequisites
- Android Studio / IntelliJ IDEA
- JDK 17
- Android SDK

### Build Locally

```bash
# Windows
.\gradlew.bat CdtvProvider:make

# Linux / macOS
./gradlew CdtvProvider:make
```

The built `.cs3` file will appear in `CdtvProvider/build/`.

### Repository Structure

```
cdtv/
├── CdtvProvider/
│   ├── build.gradle.kts         # Plugin metadata
│   └── src/main/kotlin/com/cdtv/
│       ├── CdtvPlugin.kt        # Plugin entry point
│       └── CdtvProvider.kt      # Main provider logic
├── .github/workflows/build.yml  # Auto-build on push to main/master
└── build.gradle.kts             # Root build config
```

### How It Works

The plugin scrapes the channel list from `http://30.30.30.30/`, parses each channel's stream ID, and delivers a direct **HLS (m3u8)** stream URL from the Flussonic media server at `http://30.30.30.30:8088/<stream_id>/index.m3u8`.

---

## 📦 GitHub Actions

On every push to `main`/`master`, GitHub Actions automatically:
1. Builds all `.cs3` plugin files
2. Generates `plugins.json`
3. Pushes them to the `builds` branch

Make sure to:
- Enable **"Read and write permissions"** under *Settings → Actions → General → Workflow permissions*
- Create a `builds` branch (the workflow will push to it)

---

*Built with ❤️ for the BDIX community in Bangladesh*
