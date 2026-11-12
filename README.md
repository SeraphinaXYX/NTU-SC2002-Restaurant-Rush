# SC2002 Restaurant Rush

**Repository:** https://github.com/SeraphinaXYX/NTU-SC2002-Restaurant-Rush

Java command-line restaurant management game. This version implements Stage 3.

[Launch instructions and beginner's playing guide](docs/PLAYING.md)

## Build and play

Requires JDK 11 or later. Open the project in your Java IDE, use `src` as the
source folder, then run the `main` method in
[`src/restaurantrush/Main.java`](src/restaurantrush/Main.java).

To compile and launch directly from Terminal in this repository:

```sh
javac -encoding UTF-8 -d out -sourcepath src src/restaurantrush/Main.java
java -cp out restaurantrush.Main
```

`Main.main()` creates the game and starts the turn loop in `GameCLI.run()`.
The compiled classes go into the ignored `out` folder. The game runs offline.
Enter a customer number after a task, such as order 1.
At turn start choose on to activate Happy Hour, or off to continue. Off does not
cancel an active Happy Hour. It lasts two turns and can be activated only once.
The Host accepts seat ID or wait before the Waiter acts.
The Waiter accepts seat ID, order ID, serve ID or wait. The Chef accepts cook ID
or wait. The first free table is used. Invalid choices can be retried without
using a task. If input ends, remaining staff choices wait until turn 18.
Timing hints explain earliest serving and payment turns, including payments
after closing. Satisfaction averages display one decimal place.

## Game rules

The base setting has two tables and arrivals on turns 1, 4, 7, 10, 13 and 16.
Customers choose their meals. Regular customers cycle through Salad, Burger,
Pasta and Salad + Pasta by arrival ID.
Arrival 2 is a VIP: patience loss 5 and a 10% discount. Arrival 3 is a Critic:
patience loss 12 and a preference for the meal with the most preparation units,
then highest price if tied. Other customers are Regular, with patience loss 8.
The Salad + Pasta combo costs $24.30 and requires three preparation units.
Apply the VIP discount before Happy Hour. Each percentage discount rounds to the
nearest cent, with half cents rounded up. Bills are locked when the order is taken.
Happy Hour gives 20% off new orders and restores 15 satisfaction at turn start
to customers still waiting for a table or food, capped at 100.
A prepared dish can be served next turn at the earliest. Eating takes a full
turn; payment follows on a later turn. The Cashier handles the oldest eligible
customer, breaking ties by arrival ID. Tables stay occupied until payment or departure.
After all 18 turns, victory requires at least four paid customers, $45.00 revenue
and average satisfaction of paid customers of at least 60.

## Verification

```sh
python3 tests/run_checks.py
```

The test runner additionally needs Python 3 and Git. The game itself needs only JDK 11+.
[TESTING.md](TESTING.md) explains ten reproducible CLI runs and 43 independent rule checks.
Inputs, expected transcripts and exact-source receipts are in [`tests/`](tests/).

The [PDF report](report/SC2002_Restaurant_Rush_Report.pdf) covers class and sequence diagrams,
design principles, stage changes, functional evidence and reflections.
[Report sources and build notes](report/README.md) and [editable design notes](docs/DESIGN.md)
are included.

## Stages and tags

| Stage | Git tag | Content |
| --- | --- | --- |
| Stage 1 | `stage-1` | Regular customers, menu, Waiter, Chef, Cashier and complete 18-turn game. |
| Stage 2 | `stage-2` | VIP and Critic customers using the same service flow. |
| Stage 3 | `stage-3` | Host seating, ComboMeal, Happy Hour, order announcements and reproducible checks. |

All three annotated tags are published on the remote repository. Each tagged commit compiles and runs independently.

## Stage 1 to Stage 2

[Compare stages](https://github.com/SeraphinaXYX/NTU-SC2002-Restaurant-Rush/compare/stage-1...stage-2).

| Change | Files | Reason |
| --- | --- | --- |
| New | `ArrivalSchedule.java`; `VIPCustomer.java`; `CriticCustomer.java` | Separate arrival construction and add polymorphic customer behaviour. |
| Changed | `GameController.java` | Delegate scheduled customer creation. |
| Changed | `Customer.java`; `Waiter.java` | Add and invoke the customer discount operation. |

## Stage 2 to Stage 3

[Compare stages](https://github.com/SeraphinaXYX/NTU-SC2002-Restaurant-Rush/compare/stage-2...stage-3).

| Change | Files | Reason |
| --- | --- | --- |
| New | `ComboMeal.java`; `Host.java`; `HappyHour.java`; `PricingService.java` | Add composed meals, additional seating and the timed order-price policy. |
| Changed | `GameController.java`; `TurnPhase.java` | Coordinate Host actions and turn-start policy effects. |
| Changed | `Menu.java`; `Customer.java`; `Waiter.java` | Register the combo, recover eligible satisfaction and quote bills at ordering. |
| Changed | `Order.java`; `GameCLI.java` | Expose read-only timestamps; collect Stage 3 choices. |

## Video

Approximately 15-minute group presentation video.

| Segment | Required content | Presenter / timestamp |
| --- | --- | --- |
| 1 | Launch from the submitted project | Liang Rui, 0:00 |
| 2 | Meaningful choices with constrained staff | Liang Rui, 2:05 |
| 3 | READY → SERVED → payment | Xie Yixuan, 4:10 |
| 4 | VIP or Critic behaviour | Xie Yixuan, 6:20 |
| 5 | Stage 3 feature (Host / ComboMeal / Happy Hour) | Angel Chua, 8:35 |
| 6 | Reproducible victory and defeat | Angel Chua, 10:45 |
| 7 | Stage comparisons and final diagrams | Zhu Yuean, 12:00 |

## AI history

Each member's project-related AI chat history is on their own branch under `ai-history/`.

| Member | Branch | Stages covered |
| --- | --- | --- |
| Liang Rui | [`member/liang-rui`](../../tree/member/liang-rui/ai-history) | Stage 1 (×2), Stage 2, Stage 3 |
| Xie Yixuan | [`member/xie-yixuan`](../../tree/member/xie-yixuan/ai-history) | Stage 1, Stage 2 (×2), Stage 3 |
| Angel Chua | [`member/angel-chua`](../../tree/member/angel-chua/ai-history) | Stage 2, Stage 3 (×3) |
| Zhu Yuean | [`member/zhu-yuean`](../../tree/member/zhu-yuean/ai-history) | Stage 1, Stage 2, Stage 3 |
