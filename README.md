# ТигрБанк - Модуль учета финансов

## О проекте
Консольное приложение для управления личными финансами. Моделирует доменную область финансового учета с соблюдением принципов SOLID, GRASP и использованием паттернов GoF.

### Моделируемые сущности:
- **BankAccount** (id, name, balance) - банковские счета
- **Category** (id, type, name) - категории доходов/расходов
- **Operation** (id, type, bank_account_id, amount, date, description, category_id) - финансовые операции

## РЕАЛИЗОВАННЫЕ ПАТТЕРНЫ ПРОЕКТИРОВАНИЯ

| Паттерн | Реализация | Назначение |
|---------|------------|------------|
| **Фабричный метод** | `AbstractDataHandler.getObjectMapper()` | Создание ObjectMapper для разных форматов файлов |
| **Фасад** | `AccountManager`, `CategoryManager`, `OperationManager`, `AnalyticsService` | Упрощенный интерфейс для работы с подсистемами |
| **Команда** | Пакет `org.example.command`| Инкапсуляция пользовательских сценариев |
| **Шаблонный метод** | `AbstractDataHandler.export()` и `importData()` | Единый алгоритм для импорта/экспорта разных форматов |

## РЕАЛИЗОВАННЫЙ ФУНКЦИОНАЛ

### Основной (обязательный):
- ✔️ CRUD для счетов (создание, чтение, обновление имени, удаление)
- ✔️ CRUD для категорий (создание, чтение, обновление имени, удаление)
- ✔️ CRUD для операций (создание, чтение, удаление)
- ✔️ Автоматический пересчет баланса при создании/удалении операций

### Аналитика:
- ✔️ Разница доходов/расходов за период
- ✔️ Группировка доходов/расходов по категориям
- ✔️ Топ категорий расходов
- ✔️ Динамика по месяцам
- ✔️ Средний чек по категориям

### Импорт/Экспорт:
- ✔️ Экспорт всех данных в JSON/YAML/CSV
- ✔️ Импорт данных из JSON/YAML/CSV
- ✔️ Автоопределение формата по расширению файла
- ✔️ DTO для безопасной сериализации

### Статистика:
- ✔️ Измерение времени выполнения каждой команды
- ✔️ Сбор статистики по всем пользовательским сценариям
- ✔️ Детальный отчет по каждой команде (мин/макс/среднее время)


## ДЕМОНСТРАЦИЯ SOLID ПРИНЦИПОВ

| Принцип | Реализация |
|---------|------------|
| **S** (Single Responsibility) | Каждый класс имеет одну причину для изменения: AccountManager, CategoryManager, OperationManager, AnalyticsService |
| **O** (Open/Closed) | Интерфейсы репозиториев, DataExporter/DataImporter и AbstractDataHandler позволяют добавлять новые реализации без изменения существующего кода |
| **L** (Liskov Substitution) | JsonDataHandler, YamlDataHandler, CsvDataHandler могут заменять AbstractDataHandler |
| **I** (Interface Segregation) | Разделение DataExporter и DataImporter вместо одного общего интерфейса |
| **D** (Dependency Inversion) | Зависимости направлены на интерфейсы (репозитории, DataExporter), а не на конкретные классы |

## ДЕМОНСТРАЦИЯ GRASP ПРИНЦИПОВ

| Принцип | Реализация |
|---------|------------|
| **High Cohesion** (Высокая связность) | Каждый класс сфокусирован на одной задаче: AccountManager управляет только счетами, AnalyticsService только аналитикой |
| **Low Coupling** (Низкая связанность) | Классы связаны через интерфейсы и DI-контейнер, изменения в одном модуле минимально влияют на другие |

## DI-КОНТЕЙНЕР (Google Guice)

```java
// TigerBankModule.java
public class TigerBankModule extends AbstractModule {

    @Override
    protected void configure() {
        // Привязка репозиториев к их in-memory реализациям
        bind(BankAccountRepository.class).to(InMemoryBankAccountRepository.class).in(Singleton.class);
        bind(CategoryRepository.class).to(InMemoryCategoryRepository.class).in(Singleton.class);
        bind(OperationRepository.class).to(InMemoryOperationRepository.class).in(Singleton.class);

        // Привязка менеджеров
        bind(AccountManager.class).in(Singleton.class);
        bind(CategoryManager.class).in(Singleton.class);
        bind(OperationManager.class).in(Singleton.class);

        // Привязка сервисов
        bind(AnalyticsService.class).in(Singleton.class);
        bind(StatisticsService.class).in(Singleton.class);

        // Привязка обработчиков импорта/экспорта
        bind(JsonDataHandler.class).in(Singleton.class);
        bind(YamlDataHandler.class).in(Singleton.class);
        bind(CsvDataHandler.class).in(Singleton.class);
        bind(DataHandlerFactory.class).in(Singleton.class);
    }
}
```

Все зависимости внедряются через конструкторы с аннотацией `@Inject`:
```java
@Inject
public ConsoleApp(AccountManager accountManager, CategoryManager categoryManager, ...) {
    this.accountManager = accountManager;
    // ...
}
```

## МОДУЛЬНОЕ ТЕСТИРОВАНИЕ

**Покрытие тестами всех ключевых компонентов:**
- `AccountManagerTest` - тестирование CRUD для счетов
- `CategoryManagerTest` - тестирование CRUD для категорий
- `OperationManagerTest` - тестирование CRUD для операций
- `AnalyticsServiceTest` - тестирование всех аналитических функций

**Использование Mockito** для изоляции тестируемых компонентов:
```java
@ExtendWith(MockitoExtension.class)
class AccountManagerTest {
    @Mock private BankAccountRepository accountRepository;
    @InjectMocks private AccountManager accountManager;
    // ...
}
```

## ИМПОРТ/ЭКСПОРТ ДАННЫХ

Реализована полноценная поддержка импорта/экспорта данных в трех форматах с использованием паттернов Фабричный метод и Шаблонный метод.

| Формат | Класс | Библиотека | Расширения |
|--------|-------|------------|------------|
| **JSON** | `JsonDataHandler` | Jackson | .json |
| **YAML** | `YamlDataHandler` | Jackson-dataformat-yaml | .yaml, .yml |
| **CSV** | `CsvDataHandler` | OpenCSV | .csv |

**DataHandlerFactory** автоматически выбирает нужный обработчик по расширению файла.


## ПРОБЛЕМЫ ПРИ РАСШИРЕНИИ ФУНКЦИОНАЛА

1. **Валидация в менеджерах** - при добавлении новых правил валидации придется изменять существующие классы
2. **Отсутствие транзакционности** - при создании операции и обновлении баланса возможна рассогласованность при сбоях
3. **Прямая работа с репозиториями в некоторых местах** - сложно добавить кэширование без модификации сервисов
4. **Консольный UI** - при добавлении графического интерфейса потребуется рефакторинг, так как логика ввода смешана с командами

## АРГУМЕНТЫ ЗА АБСТРАКЦИИ

| Абстракция | Что улучшила |
|------------|--------------|
| **Интерфейсы репозиториев** | Позволяют менять способ хранения данных (in-memory → БД) без изменения бизнес-логики |
| **DataExporter/DataImporter** | Легко добавлять новые форматы файлов (например, XML) без изменения существующего кода |
| **AbstractDataHandler** | Устраняет дублирование кода для импорта/экспорта разных форматов |
| **Менеджеры (фасады)** | Инкапсулируют бизнес-правила, упрощают тестирование и клиентский код |
| **Команды** | Позволяют легко добавить сквозную функциональность (логирование, измерение времени, транзакции) |
| **DI-контейнер** | Обеспечивает слабую связанность и централизованное управление зависимостями |

## ЕДИНЫЙ CODE STYLE

- **Именование**: camelCase для методов/переменных, PascalCase для классов
- **Константы**: UPPER_SNAKE_CASE
- **Аннотации**: на отдельных строках перед методом/классом

## КЛЮЧЕВЫЕ ОСОБЕННОСТИ СИСТЕМЫ

1. **Чистая архитектура** с разделением на слои (модели, репозитории, сервисы, команды, DTO)
2. **4 паттерна GoF** (Фабричный метод, Фасад, Команда, Шаблонный метод)
3. **SOLID + GRASP** - все принципы соблюдены
4. **DI-контейнер** Google Guice для управления зависимостями
5. **Модульное тестирование** с Mockito
6. **Измерение времени** выполнения всех пользовательских сценариев
7. **Полный CRUD** для всех сущностей
8. **Импорт/экспорт** в 3 формата (JSON, YAML, CSV)
