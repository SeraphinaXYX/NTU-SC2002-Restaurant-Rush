# Restaurant Rush design

This describes Stage 3. Private fields hold state; public methods
perform the permitted operations. The CLI reads choices and displays results.
The controller coordinates turns; domain objects keep their own rules.
`Main.main()` is the Java entry point. It creates the controller and console UI,
then calls `GameCLI.run()`, whose loop runs the game until turn 18 finishes.
The console reads cooking and service timestamps through getters to explain
earliest service and payment turns. Estimates state their cooking assumptions
and flag payments beyond closing. Displayed satisfaction uses integer arithmetic
to round to one decimal place; the victory calculation keeps the exact totals.

## Course concepts

| Topic | Application |
| --- | --- |
| Classes, objects and encapsulation (Chapter 2) | Customer, Order and Table expose operations instead of public mutable fields. |
| Object composition (Chapter 2) | A Customer has an Order; GameController owns the game-session customers, tables and staff. |
| Inheritance and abstract classes (Chapter 3) | Staff shares the task allowance; its subclasses provide their own tasks. |
| Exception handling (Chapter 4) | Invalid choices are explained and retried without consuming a staff task. |
| ArrayList and ordinary loops (Chapter 5) | Hold and traverse the small collections of game objects. |
| Polymorphism (Chapter 5) | Customer references call the appropriate VIP or Critic overrides. |
| Composition and inheritance | ComboMeal is a MenuItem containing two existing MenuItem components. |

## Class diagram

Fields and important operations match the Java source. State names are String
constants declared with static final, not enums. Shared menu components use aggregation.
The PDF report presents the same model in complementary control, customer, staff
and menu views so that signatures remain readable. The controller owns one of each
staff role, one menu, arrival schedule and Happy Hour state, two tables, up to six
customers and up to five payment records. A table and a customer each participate
in at most one current occupancy association; tables do not own customers.

```mermaid
classDiagram
    class Main {
        +main(String[] args) void$
    }
    class ArrivalSchedule {
        +arrivalAt(int turn) Customer
    }
    class GameController {
        +int STAGE$
        +int MAX_TURNS$
        -Menu menu
        -ArrayList~Customer~ customers
        -ArrayList~Table~ tables
        -ArrayList~PaymentRecord~ payments
        -ArrivalSchedule arrivals
        -HappyHour happyHour
        -Host host
        -Waiter waiter
        -Chef chef
        -Cashier cashier
        -int turn
        -String phase
        +beginTurn() void
        +beginTurn(boolean activateHappyHour) void
        +seatWithHost(int id) void
        +waitHost() void
        +seatCustomer(int id) void
        +takeOrder(int id) void
        +serveCustomer(int id) void
        +waitWaiter() void
        +prepareOrder(int id) void
        +waitChef() void
        +finishTurn() void
        +getResult() GameResult
    }
    class GameResult {
        -int paidCustomers
        -int revenueCents
        -int satisfactionTotal
        +isVictory() boolean
    }
    class TurnPhase {
        +String BETWEEN_TURNS$
        +String HOST$
        +String WAITER$
        +String CHEF$
        +String END_OF_TURN$
        +String FINISHED$
    }
    class Cashier {
        +collectOldest(ArrayList~Customer~ customers, int turn) PaymentRecord
    }
    Staff <|-- Cashier
    class Chef {
        +prepare(Customer customer, int turn) void
    }
    Staff <|-- Chef
    class ComboMeal {
        -MenuItem first
        -MenuItem second
        +getComponents() ArrayList~MenuItem~
    }
    MenuItem <|-- ComboMeal
    class CriticCustomer {
        +getWaitingLoss() int
        +chooseMeal(Menu menu) MenuItem
    }
    Customer <|-- CriticCustomer
    class Customer {
        -int id
        -int arrivalTurn
        -int satisfaction
        -String status
        -Order order
        -int servedTurn
        -int readyToPayTurn
        +chooseMeal(Menu menu) MenuItem
        +seat() void
        +placeOrder(Order newOrder) void
        +serve(int turn) void
        +finishEating(int turn) void
        +canPay(int turn) boolean
        +pay(int turn) PaymentRecord
        +losePatience() void
        +recoverSatisfaction(int amount) void
        +getServedTurn() int
        +getWaitingLoss() int
        +discountedBill(int baseCents) int
    }
    class CustomerStatus {
        +String WAITING$
        +String SEATED$
        +String EATING$
        +String READY_TO_PAY$
        +String PAID$
        +String LEFT$
    }
    class HappyHour {
        -int currentTurn
        -int activationTurn
        +startTurn(int turn, boolean activate) void
        +hasBeenUsed() boolean
        +isActive() boolean
        +getRemainingTurns() int
    }
    class Host {
        +seat(Customer customer, Table table) void
    }
    Staff <|-- Host
    class Menu {
        -ArrayList~MenuItem~ items
        +withCombo() Menu$
        +getItems() ArrayList~MenuItem~
    }
    class MenuItem {
        +MenuItem SALAD$
        +MenuItem BURGER$
        +MenuItem PASTA$
        -String name
        -int priceCents
        -int preparationUnits
    }
    class Order {
        -MenuItem item
        -int billCents
        -int remainingUnits
        -int readyTurn
        -String status
        +prepare(int turn) void
        +serve(int turn) void
        +markPaid() void
        +cancel() void
        +getReadyTurn() int
    }
    class OrderStatus {
        +String TAKEN$
        +String PREPARING$
        +String READY$
        +String SERVED$
        +String PAID$
        +String CANCELLED$
    }
    class PaymentRecord {
        -int customerId
        -int billCents
        -int satisfaction
        -int turn
    }
    class PricingService {
        -HappyHour happyHour
        +quote(Customer customer, MenuItem item) int
    }
    class Staff {
        <<abstract>>
        -boolean available
        +startTurn() void
        +isAvailable() boolean
        #requireAvailable() void
        #completeTask() void
    }
    class Table {
        -int id
        -Customer occupant
        +seat(Customer customer) void
        +releaseIfFinished() void
    }
    class VIPCustomer {
        +getWaitingLoss() int
        +discountedBill(int baseCents) int
    }
    Customer <|-- VIPCustomer
    class Waiter {
        -PricingService pricing
        +seat(Customer customer, Table table) void
        +takeOrder(Customer customer, Menu menu) void
        +serve(Customer customer, int turn) void
    }
    Staff <|-- Waiter
    class GameCLI {
        -GameController game
        -Scanner input
        -boolean inputEnded
        +run() void
        -beginTurn() void
    }
    Main ..> GameCLI
    GameCLI --> "1" GameController
    GameController *-- "0..6" Customer
    GameController *-- "2" Table
    GameController *-- "0..5" PaymentRecord
    GameController *-- "1" Menu
    GameController *-- "1" Waiter
    GameController *-- "1" Chef
    GameController *-- "1" Cashier
    GameController *-- "1" Host
    GameController *-- "1" HappyHour
    GameController *-- "1" ArrivalSchedule
    GameController ..> GameResult
    GameController ..> TurnPhase
    Customer *-- "0..1" Order
    Customer ..> CustomerStatus
    Order ..> OrderStatus
    Order --> "1" MenuItem
    Table --> "0..1" Customer : occupant
    Menu o-- "3..4" MenuItem
    ComboMeal o-- "2" MenuItem : components
    Waiter --> "1" PricingService
    PricingService --> "1" HappyHour
    Waiter ..> Table : seats
    Host ..> Table : seats
    Chef ..> Order : prepares
    Cashier ..> Customer : pays
    ArrivalSchedule ..> Customer : creates
```

## Sequence diagram

In this scenario C1 occupies the first table and the second table is free. The manager activates
Happy Hour on turn 4 and uses the Host to seat VIP C2,
leaving the Waiter free to take the order that turn. Using the Waiter only for
seating would delay the earliest order and payment by one turn.

```mermaid
sequenceDiagram
    actor Manager
    participant CLI as GameCLI
    participant G as GameController
    participant A as ArrivalSchedule
    participant H as HappyHour
    participant Host
    participant T as Table
    participant W as Waiter
    participant P as PricingService
    participant Chef
    participant Cash as Cashier
    Manager->>CLI: activate Happy Hour, seat C2, order C2
    CLI->>G: beginTurn(true) on turn 4
    G->>H: startTurn(4, true)
    G->>A: arrivalAt(4)
    create participant C as VIPCustomer C2
    A->>C: new VIPCustomer(2, 4)
    A-->>G: C2
    G->>C: recoverSatisfaction(15), capped at 100
    CLI->>G: seatWithHost(2)
    G->>Host: seat(C2, table)
    Host->>T: seat(C2)
    T->>C: seat()
    CLI->>G: takeOrder(2)
    G->>W: takeOrder(C2, menu)
    W->>C: chooseMeal(menu)
    C-->>W: Burger
    W->>P: quote(C2, Burger)
    P->>C: discountedBill(1500)
    C-->>P: 1350
    P->>H: isActive()
    H-->>P: true
    P-->>W: 1080 cents
    create participant O as Order
    W->>O: new Order(Burger, 1080)
    W->>C: placeOrder(order)
    CLI->>G: prepareOrder(2)
    G->>Chef: prepare(C2, 4)
    Chef->>O: prepare(4)
    Note right of O: READY, serving allowed next turn at earliest
    CLI->>G: finishTurn()
    G->>C: losePatience(), now 95
    Note over CLI,G: Turn 5
    CLI->>G: beginTurn(false)
    G->>H: startTurn(5, false)
    G->>C: recoverSatisfaction(15), now 100
    CLI->>G: waitHost(), serveCustomer(2)
    G->>W: serve(C2, 5)
    W->>C: serve(5)
    C->>O: serve(5), now SERVED
    CLI->>G: waitChef(), finishTurn()
    G->>C: finishEating(5), remains EATING
    Note over CLI,G: Turn 6, Happy Hour expires
    CLI->>G: beginTurn(false), role choices, finishTurn()
    G->>C: finishEating(6), now READY_TO_PAY
    G->>Cash: collectOldest(customers, 6)
    Cash->>C: canPay(6)
    C-->>Cash: false, became ready this turn
    Note over CLI,G: Turn 7, after role choices; C2 is oldest eligible payer
    CLI->>G: finishTurn()
    G->>Cash: collectOldest(customers, 7)
    Cash->>C: canPay(7)
    C-->>Cash: true, ready since turn 6
    Cash->>C: pay(7)
    C->>O: markPaid()
    C-->>Cash: PaymentRecord, 1080 cents and satisfaction 100
    Cash-->>G: payment record
    G->>T: releaseIfFinished()
    loop Remaining turns through 18
        Manager->>CLI: staff choices
        CLI->>G: beginTurn, role choices, finishTurn
    end
    CLI->>G: getResult()
    create participant Result as GameResult
    G->>Result: new GameResult(paid, revenue, satisfactionTotal)
    G-->>CLI: result
    CLI->>Result: isVictory()
    Result-->>CLI: threshold comparison
    alt paid >= 4 and revenue >= 4500 and satisfactionTotal >= 60 * paid
        CLI-->>Manager: FINAL: VICTORY
    else at least one threshold fails
        CLI-->>Manager: FINAL: DEFEAT
    end
```

## Design choices

Each class has a specific responsibility. Staff shares only the one-task
allowance, while the roles keep distinct operations. Customer and Order use
separate states because eating/payment eligibility differs from food readiness.
Private timestamps enforce the required delays. The waiting queue is derived
from waiting customers in arrival order, avoiding a second membership list.

An alternative was to put VIP and Critic behaviour in controller type checks.
Instead, calls through Customer references select the right overridden methods.
The same seating, serving and payment flow applies to every customer type.

The Host and Waiter reuse Table.seat. HappyHour stores timing; PricingService
calculates the bill only when ordering. ComboMeal keeps two existing menu objects.
This adds the new behaviour without changing the Chef or Cashier.

Ordinary loops are sufficient for six customers. Returned ArrayLists are copies
of the containers; they refer to the same domain objects. The CLI reads those
objects and requests changes through the controller. A larger system could need
additional boundaries, but this assignment does not require a framework.
