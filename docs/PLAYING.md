# How to play Restaurant Rush

You manage a restaurant for 18 turns: seat customers, take their orders, prepare
food and serve it before they lose patience. The current version is Stage 3,
including the features introduced in Stages 1 and 2. It runs locally in your
IDE's console or Terminal.
Turns advance when you finish your choices, so you can take time to think.

## Launch the game

You need a JDK, version 11 or later.

### From your Java IDE

1. Open the project folder and select JDK 11 or later.
2. Make sure `src` is recognised as the source folder.
3. Open [`src/restaurantrush/Main.java`](../src/restaurantrush/Main.java).
4. Run its `public static void main(String[] args)` method using the IDE's Run
   button. Enter your game commands in the IDE's console.

`Main` creates the game and starts the main turn loop in `GameCLI.run()`.

### From Terminal

These commands should both show a version number:

```sh
java -version
javac -version
```

Open Terminal in the project folder containing `src`, then enter:

```sh
javac -encoding UTF-8 -d out -sourcepath src src/restaurantrush/Main.java
java -cp out restaurantrush.Main
```

The first command compiles the game into the ignored `out` folder. The second
starts a fresh game through `Main.main()`. Look for `Restaurant Rush - Stage 3`,
followed by the menu and the first turn's prompt. Compile again after changing
the source code; otherwise, you can reuse the second command to play again.

## What to type each turn

Enter one command at each prompt and press Return. Use lowercase commands.
`C1` on the display means customer 1: type `seat 1`, not `seat C1`.

| Prompt | Commands | What happens |
| --- | --- | --- |
| Happy Hour | `on` or `off` | `on` activates the once-per-game promotion. |
| Host | `seat 1` or `wait` | Seats a waiting customer at the first free table, or skips the action. |
| Waiter | `seat 1`, `order 1`, `serve 1` or `wait` | Performs one seating, order-taking or serving action, or skips it. |
| Chef | `cook 1` or `wait` | Prepares one unit of that customer's food, or skips the action. |

Replace `1` with the customer ID you want to help. Each staff member gets one
action per turn. The Cashier acts automatically at the end of the turn.
Invalid choices are explained and can be retried without using the action.

Use the Host for seating so the Waiter can take an order or serve food in the
same turn. Customers choose their own meals when the Waiter takes their order.

## Try these first four turns

Read each row from left to right, entering its four commands at the corresponding
prompts. This introduces the controls without playing the whole game for you.

| Turn | Happy Hour | Host | Waiter | Chef |
| --- | --- | --- | --- | --- |
| 1 | `off` | `seat 1` | `order 1` | `cook 1` |
| 2 | `off` | `wait` | `serve 1` | `wait` |
| 3 | `off` | `wait` | `wait` | `wait` |
| 4 | `on` | `seat 2` | `order 2` | `cook 2` |

Customer 1 orders Salad. It finishes cooking on turn 1, can be served on turn 2,
finishes eating on turn 3 and pays automatically at the end of turn 4. Customer 2
is a VIP. Continue from turn 5 by reading the waiting queue, table occupancy and
each customer's order status.

## Timing and useful rules

- There are two tables. New customers arrive on turns 1, 4, 7, 10, 13 and 16.
- Food needs preparation units: Salad 1, Burger 1, Pasta 2, Salad + Pasta combo 3.
  Repeat `cook ID` on later turns until the order is `READY`.
- Food completed this turn can be served from the next turn. Eating and payment
  also take later turns; serving a customer does not immediately earn revenue.
- A table stays occupied until its customer pays or leaves.
- Customers waiting for a table or food lose satisfaction each turn: Regular 8,
  VIP 5, Critic 12. At zero satisfaction, they leave.
- Happy Hour lasts for its activation turn and the following turn. During those
  turns it restores 15 satisfaction to waiting customers, capped at 100, and
  discounts new orders by 20%. A VIP also receives a 10% discount before this.
  An order's bill is fixed when taken.
- Choose `off` on later turns after activating Happy Hour. This keeps the turn
  moving and does not cancel the promotion. You cannot activate it a second time.

## Win, stop or restart

Active orders include timing hints. An unfinished meal's earliest serving turn
assumes you cook it each turn, starting this turn. The payment estimate assumes
you serve it on the suggested turn; the Cashier can still process only one
eligible customer per turn. Dates beyond turn 18 are marked as after closing.
Satisfaction averages display one decimal place, such as `93.7`. The game uses
exact totals when deciding victory.

After turn 18, you win only if all three targets are met:

- At least **4 customers have paid**.
- Revenue is at least **$45.00**.
- Average satisfaction among paid customers is at least **60**.

The final display says `VICTORY` or `DEFEAT` and shows your totals. `Served` can
be higher than `Paid`; victory uses the paid count. Satisfaction is `N/A` until
someone pays.

Use your IDE's Stop button or press **Control+C** in Terminal to stop early.
Run `Main` again in your IDE, or enter `java -cp out restaurantrush.Main` in
Terminal, to restart from turn 1. There is no saved progress. If standard input
ends, the game uses `off` and `wait` for the remaining turns and then shows the
result.

If the compiler cannot find `Main.java`, change into the project folder first.
If Java or `javac` cannot be found, make sure a JDK is installed and available
in that Terminal session. If Java cannot find `restaurantrush.Main`, compile
first and run the launch command from the same project folder.
