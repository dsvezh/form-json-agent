# Form JSON Agent 2.0

Шаблон проекта на `Java + Gradle + JUnit + Playwright` для задачи:

- открыть страницу с формой,
- прочитать обычные поля и кастомные dropdown,
- обнаружить простые зависимости между списками,
- сгенерировать набор сценариев заполнения,
- собрать nested JSON по mapping-правилам,
- сохранить схему формы, сетевые запросы и итоговые payload.

## Что внутри

- `src/main/java/com/example/agent/App.java`
  Главная точка входа.
- `src/main/java/com/example/agent/browser/FormScanner.java`
  Сканирует форму и строит `FormSchema`.
- `src/main/java/com/example/agent/browser/NetworkInterceptor.java`
  Перехватывает JSON-запросы страницы.
- `src/main/java/com/example/agent/generator/CombinationGenerator.java`
  Строит комбинации значений с учетом зависимых dropdown.
- `src/main/java/com/example/agent/generator/JsonPayloadBuilder.java`
  Превращает плоские значения в nested JSON.
- `src/main/resources/application.properties`
  Основные настройки запуска.
- `src/main/resources/mapping-rules.json`
  Правила, по которым поля формы мапятся в итоговый JSON.

## Как настроить

1. Укажи реальный URL в `application.properties`.
2. Укажи точный CSS-селектор формы в `agent.formSelector`.
3. При необходимости поправь `mapping-rules.json`.
4. Если нужен видимый браузер, выставь `agent.headless=false`.

## Как запускать

```powershell
gradle run
```

## Как запускать тесты

```powershell
gradle test
```

## Что сохранить после первого запуска

В папке `outputs` появятся:

- `form-schema.json`
- `mapping-rules.json`
- `network-captures.json`
- `generated-payloads.json`

## Важная заметка по Playwright

Для работы Playwright нужны браузеры. Если они еще не установлены, установи их отдельно.
В зависимости от окружения это можно сделать через Playwright CLI или через первый запуск проекта.

## Важная заметка по Gradle

Если Gradle в системе падает на старте с ошибкой про `native-platform.dll`,
это проблема локального окружения Gradle, а не этого шаблона.
Обычно помогает:

- обновить локальный Gradle,
- использовать Gradle Wrapper,
- очистить Gradle cache,
- проверить права на папки пользователя и временные директории.
