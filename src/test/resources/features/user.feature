#language: de
Funktionalität: Erstellen eines neuen Benutzers

  Szenario: Ein neuer Benutzer wird erfolgreich erstellt
    Wenn ich einen neuen Benutzer mit den folgenden Daten erstelle:
      | UserId                                   | Nickname | E-Mail          |
      | f2bc9bb5-76ba-4b31-8767-f8a530611782     | Spruhs   | spruhs@test.com |
    Dann sollte die Antwort den HTTP-Statuscode 201 zurückgeben
    Und die Antwort enthält die Id des neu erstellten Benutzers
    Und der Benutzer sollte erfolgreich in der Datenbank erstellt werden

  Szenario: Ein neuer Benutzer kann nicht doppelt erstellt werden
    Wenn ich einen neuen Benutzer mit den folgenden Daten erstelle:
      | UserId                                   | Nickname | E-Mail          |
      | f2bc9bb5-76ba-4b31-8767-f8a530611782     | Spruhs   | spruhs@test.com |
    Dann sollte die Antwort den HTTP-Statuscode 201 zurückgeben
    Wenn ich einen neuen Benutzer mit den folgenden Daten erstelle:
      | UserId                                   | Nickname | E-Mail          |
      | f2bc9bb5-76ba-4b31-8767-f8a530611782     | Spruhs   | andere@test.com |
    Dann sollte die Antwort den HTTP-Statuscode 400 zurückgeben
    Wenn ich einen neuen Benutzer mit den folgenden Daten erstelle:
      | UserId                                   | Nickname | E-Mail          |
      | f2bc9bb5-76ba-4b31-8767-f8a530611783     | Spruhs   | spruhs@test.com |
    Dann sollte die Antwort den HTTP-Statuscode 400 zurückgeben

  Szenario: Ein Benutzer kann sein Nickname ändern
    Wenn ich einen neuen Benutzer mit den folgenden Daten erstelle:
      | UserId                                   | Nickname | E-Mail          |
      | f2bc9bb5-76ba-4b31-8767-f8a530611782     | Spruhs   | spruhs@test.com |
    Dann sollte die Antwort den HTTP-Statuscode 201 zurückgeben
    Wenn ich den Nickname des Benutzers mit der Id "f2bc9bb5-76ba-4b31-8767-f8a530611782" auf "Spruhs2" ändere
    Dann sollte die Antwort den HTTP-Statuscode 200 zurückgeben
    Und der Benutzer sollte erfolgreich in der Datenbank aktualisiert werden