# language: de
Funktionalität: Beispiel-E2E-Test

  Szenario: Health-Endpunkt ist erreichbar
    Angenommen der Service ist erreichbar
    Wenn ich den Health-Endpunkt aufrufe
    Dann erhalte ich den HTTP-Statuscode 200

  Szenario: Health-Endpunkt gibt den Status UP zurück
    Angenommen der Service ist erreichbar
    Wenn ich den Health-Endpunkt aufrufe
    Dann erhalte ich den HTTP-Statuscode 200
    Und die Antwort enthält den Status "UP"
