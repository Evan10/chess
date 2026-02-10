# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

New Sequence Diagram 
https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdADZM9qBACu2AMQALADMABwATACcIDD+yPYAFmA6CD6GAEoo9kiqFnJIEGiYiKikALQAfOSUNFAAXDAA2gAKAPJkACoAujAA9D4GUAA6aADeAEQDlGjAALYo43XjMOMANCu46gDu0ByLy2srKLPASAj7KwC+mMK1MJWs7FyUDRNTUDPzF4fjm6o7UD2SxW63Gx1O52B42ubE43FgD1uogaUCyOTAlAAFJlsrlKJkAI5pXIAShuNVE9yqsnkShU6ga9hQYAAqoNMe9PigyTTFMo1KoqUYdHUAGJITgwNmUXkwHSWGCcuZiHSo4AAaylgxgWyQYASisGXJgwAQao4CpQAA90RpeXSBfdERSVA1pVBeeSRConVVbi8YAozShgBaOhr0ABRK0qbAEQpeu5lB4lcwNQJOYIjCbzdTAJmLFaRqDeeqG6bKk3B0MK+Tq9DQsycTD2-nqX3Vb0oBpoHwIBCJykPVv01R1EBqjHujmDXk87QO9sPYx1BQcDhamXaQc+4cLttjichjEKHz6zHAM8JOct-ejoUrtcb0-6z1I3cPWHPMs49H4tR9lgX7wh2-plm8RrKssDQHKCl76h0ED1mg0ErFciaUB2qYYA04ROE42aTJBXwwDBIIrPBCSIchqEHNc6AcKYXi+AE0DsEysSinAkbSHACgwAAMhA2RFNhzDOtQAYtO03R9AY6gFGg2ZKvM6x-ACHDXGBn5PPCrxERWJFkT86m7LR6HAZQlTvt2MAIMJEqYkJImEsSYBkjZlQjgKjLMtOKncretIHpUK7ipK7qyvK5YfMqQV8qO1kut2kXbp5FQ2eOCQoCA6rNPo-y7JipmAqsqXyB5yVeXePkwEecgoC+CQzoZgXeeooUik+gZXp6llQAAPMmmXORKmSqIBGGDcm-V1BBrXfHBV7UQ2ULXGJQ1lGBdR4U4MDzbFXxQktCFIatdFNoxnjeH4-heCg6CxPEST3Y9zm+FgYlCttTTSJGAmRh0kY9L08mqIpIyUStRTaRUs0TFDZ0oWtmD9UlXZ1PZ9gfU5wkfa5ajuTuKDVcFo4oscEA0E10MXstSPzmTAqdRTsxU4YNNI8TyZw7pLzvWe42TWj6O1HNMBaTUVmVGJuH4ftEuXUxN0BKiG7+NgEqagJ6IwAA4sqGhfRJYtNHrgMg-YyqQ-TyFTZU8PjIjNEo2jGXJZj6IG7mTle4bBOktzFTtWOMBMqy7LO+gjMJczFQrmHzKbh62iGhKjhR0UIc85l5XAEHIe+WAQYIN7aiYjHi6qCzifMMguQwBAABmVYIGHcXZ6LyJlzruTcw7fP1L3YBl6oQsIEBg851L4ErFbuaFiMjQTPPKAAJLSIWACM4TBIEoJbAkeooO6XL7IcIzjCkoDqqfUFLD8q8AHLKg-lwwF0kuSVAPOy7tCvjFXqoReaBl6AOVBvbeu994rEPsfO+R1xgXxQtfXKCCFgP1BM-V+SD36fyVtdFi-gOAAHZIhOBQHtfwkZghwG4m4eAk5DBlxgMUZA5hvozwaNJToltrYzFtugbM2D5hf1qAPOELwV7Khfog8iTtBHIwum7TK9UMRl0xHAJhZcA5E3SoXWu05M6VxCvHEUtdk6ygGOnE0ij4pV2nhjPOBcaoMjqto5UmIREoDKrObQJjEpmIaKGDcZdPSd3dhjMJaUqoSO-HULRx4UA6IAhPVGU9RZSLnhAzeDQd570VtpGW7CcIwH-qMbJ8xIF5OgYrBiphCG3UsCgfsEAthPSQIkMAzTWntIAFIQAlPrSs-hUHqjYaUcSfouFNGaCyWSvRV421OshbM2AEDAGaVAOAEB7JQHWKvDeYirK80kfUBGijFobA2VsnZezCywUqevXJyip6RKoMiAZEoNFfLQCkokhNKpdlJrHNx4cjGKICXHBO4dLGp2sQ4WxKz0D2JCl3V0cL5AuKZgycOGjDnSChR1IJFjonyDToiglqLErou7GS-O6VMp+C0Oo5UvJMT0tWAFVY6zNmUDudAIFHyVAgqrnUZlDUNH0qFaIHms1fkpImmkkW7yAwTF5bc3Z0ArngKqS89CRSKh-3luqm5-KtVQB1QS749FmyNICF4TZHSumOoVIgEMsBgDYHWYQfIhRWHG2md-bhf0AZAxBsYe2pz4kTA4C05kGDXlnNpeObgeBeR6AMBXbFoLDxppTvIAAQvoFA2bs5BOkPGjEwoYDyBSGITycS9KVu4Bicek9k2ZPOccn+xTJk7RNdcJWQA

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```
