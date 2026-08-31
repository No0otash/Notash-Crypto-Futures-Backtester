# ALVEX Reference UI Architecture

This document records the approved visual direction from the supplied 12-screen reference. It is a code-level UI target, not a mockup and not an instruction to delete existing functionality.

## Visual system
- Deep navy/black background.
- Purple primary brand surfaces with green positive/live states and red risk states.
- Rounded cards with strong hierarchy and compact information density.
- ALVEX brand mark and dedicated semantic icons.
- Five-part bottom navigation: Home, AI Radar, Markets, Workspace, More.
- English remains the product/master language; Persian is presented in the navigation and support copy.

## Information architecture
1. Login / authentication gate.
2. Home / Market Command Center.
3. AI Market Radar.
4. Markets and trading terminal.
5. Whale Intelligence.
6. Pump/Dump Radar.
7. Meme & Shitcoin Scanner.
8. Coin Intelligence and Tokenomics.
9. AI Backtest Analyst.
10. Settings: account/security, notifications, language, privacy, support and about.

## Implementation rule
The reference shell is an additional presentation layer. The existing `ProfessionalTerminal` remains available and is opened from the shell's terminal/workspace actions. Live market cards use the existing market repository; AI Radar presentation uses the existing provider-neutral `AiRadarEngine`. No fabricated market data is generated when providers return no data.

## Verification gate
`gradle testDebugUnitTest` and `gradle assembleDebug` must pass before this UI slice is considered build-complete.