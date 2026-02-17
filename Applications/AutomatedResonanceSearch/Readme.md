# AutomatedResonanceSearch

This application and server are used to find a SmarthPhone MEMS resonance frequency. The default sensor is a gyroscope, but it can be changed programmatically.

Run the server on a PC with speakers connected. Place the phone near the speaker—about 1m. Low-amplitude speakers may require closer proximity.

First, pay attention to changing the application's host address to your PC and ensure your phone can reach the server—for example, through shared Wi-Fi.

Open the application and press start. The application will communicate with the server to sweep through programmable ranges of sound and try to identify statistical anomalies for each frequency.

Note: Do not touch the phone during the experiment; it will affect the results!

When such an anomaly is found, the phone will try to record a more extended sampling of the anomaly and send it to the server.

The experiment can take up to 15 minutes, depending on your sweep range, the time per sub-range measured, and the incrementation parameter (all programmable parameters).

The sound may not be pleasant, but you may rock your phone in the end!

To start the results server
```bash
uvicorn http_measure:app --reload --host 0.0.0.0
```

To view results
```bash
python3 tui_view_resuls.py
```
