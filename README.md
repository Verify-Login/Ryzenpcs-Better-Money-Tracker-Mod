# Ryzenpcs Money Tracker Mod

Client-side Fabric mod (Minecraft 1.21.11). When someone pays you more than $3M, that chat message is faded from black to orange.

## Build
1. Install JDK 21 and Gradle.
2. Run `gradle wrapper` once, then `./gradlew build`.
3. The jar is in `build/libs/`. Put it in your `mods` folder with Fabric API.

## Customize
Edit `DonutPayHighlighter.java`:
- `THRESHOLD` – minimum amount
- `PAY_PATTERN` – regex for the server's pay message
- `START_COLOR` / `END_COLOR` – gradient colors

## LIVE HUD
A boxed pill at the top middle of the screen: pulsing green LIVE dot plus your session total
(money received minus money sent) with an arrow, red when negative, green when positive,
e.g. `LIVE | v -102.6M`. Reset it in-game with `/dpreset` (client-side only).

## Pay notification
When someone pays you, an animated card slides in at the top right showing the payer and
amount (`+$3.5M`) with a shimmer and countdown bar. It only appears for pays over $2.9M
(the same threshold as the orange text fade; change `THRESHOLD`). Tiers: $2.9M-$25M green
"PAYMENT RECEIVED", $25M-$99M gold "MEDIUM PAYMENT", $99M and up magenta "MEGA PAYMENT".
(Edit `MEDIUM_MIN` / `MEGA_MIN` in `PayNotifier.java`.) Up to 4 stack at once.

Patterns: `PAY_PATTERN` (group 1 = player, 2 = number, 3 = K/M/B/T) and `SENT_PATTERN` in
`DonutPayHighlighter.java` are guesses; edit them to match real chat lines.

The mod is read-only: it only reads chat and never sends commands or automates anything.
