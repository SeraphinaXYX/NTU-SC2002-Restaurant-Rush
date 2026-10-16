# SC2002 Restaurant Rush

Java command-line restaurant management game — Stage 2.

## Build and play

Requires JDK 11 or later.

```sh
javac -encoding UTF-8 -d out -sourcepath src src/restaurantrush/Main.java
java -cp out restaurantrush.Main
```

## Game rules

Two tables and arrivals on turns 1, 4, 7, 10, 13 and 16.
Customers choose their meals from Salad, Burger, Pasta and Salad + Pasta.
Arrival 2 is a VIP: patience loss 5 and a 10% discount. Arrival 3 is a Critic:
patience loss 12 and a preference for the meal with the most preparation units.
Other customers are Regular, with patience loss 8.
The Waiter seats customers, takes orders and serves food.
The Chef prepares orders. The Cashier handles payments.
After all 18 turns, victory requires at least four paid customers, $45.00 revenue
and average satisfaction of paid customers of at least 60.
