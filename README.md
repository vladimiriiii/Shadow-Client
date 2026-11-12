# Shadow Client

Mod Fabric połączony z czterema modułami: BoatFly, Komar, Killaura i MinecraftESP. Projekt używa Fabric Loom 1.14.1, Minecrafta 1.21.4 i Javy 21 LTS. Kod moda znajduje się w plikach `.java` w `src/main/java`.

Lewy Alt otwiera GUI, w którym widać bind przypisany do każdego modułu. Kliknij moduł lewym przyciskiem myszy, aby go włączyć lub wyłączyć. Kliknij go środkowym przyciskiem myszy, a następnie naciśnij klawisz albo przycisk myszy, aby przypisać bind. Backspace usuwa bind, a Escape anuluje przypisywanie lub zamyka GUI.

BoatFly steruje łodzią, Komar podąża Elytrą za najbliższym graczem, Killaura atakuje najbliższego gracza w promieniu 3 bloków, a MinecraftESP rysuje obrysy graczy przez bloki.

## Budowanie

Wymaga JDK 21 i Gradle. W katalogu `ShadowClient` uruchom:

```powershell
gradle build
```

Gotowy plik moda znajdziesz w `build/libs/shadow-client-1.0.0.jar`. Mod wymaga Fabric Loader i Fabric API dla Minecrafta 1.21.4.
