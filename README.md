# Shadow Client

Klientowy mod Fabric dla Minecrafta 1.21.4, z Fabric Loom 1.14.1 i Java 21 LTS. Kod moda jest w jednym projekcie i zawiera GUI oraz moduły BoatFly, Komar, Killaura i MinecraftESP.

## Sterowanie

- Lewy Alt otwiera GUI.
- LPM na module włącza lub wyłącza go.
- ŚPM na module, a potem klawisz lub przycisk myszy, przypisuje bind. Backspace usuwa bind, a Escape anuluje przypisywanie.
- PPM na module Komar zmienia odstęp między użyciem fajerwerków. Ustawienie przechodzi od 1 do 10 sekund i zaczyna ponownie od 1.

Komar płynniej naprowadza Elytrę, przewiduje ruch celu i używa rakiet z paska szybkiego dostępu w wybranym odstępie, gdy szybuje za celem.

## Budowanie

Wymaga JDK 21 i Gradle. W katalogu projektu uruchom:

```powershell
gradle build
```

Plik moda znajdziesz w `build/libs/shadow-client-1.0.0.jar`. Wymaga Fabric Loader i Fabric API dla Minecrafta 1.21.4.
