import restaurantrush.application.*;
import restaurantrush.domain.*;

/** Dependency-free regression checks; deliberately asserts rules beyond transcript equality. */
public final class RuleChecks {
    private static int checks;
    private static void check(boolean condition, String label) {
        if (!condition) { throw new AssertionError(label); }
        checks++;
        System.out.println("PASS " + label);
    }
    private static void rejects(Runnable action, String label) {
        try { action.run(); } catch (IllegalStateException | IllegalArgumentException expected) {
            check(true, label); return;
        }
        throw new AssertionError(label);
    }
    private static void idle(GameController game) {
        game.waitHost(); game.waitWaiter(); game.waitChef(); game.finishTurn();
    }
    public static void main(String[] args) {
        ComboMeal combo = new ComboMeal(MenuItem.SALAD, MenuItem.PASTA);
        check(combo.getPriceCents() == 2430 && combo.getPreparationUnits() == 3, "combo 2430 cents / 3 units");
        HappyHour happy = new HappyHour();
        PricingService pricing = new PricingService(happy);
        check(pricing.quote(new VIPCustomer(2, 1), MenuItem.BURGER) == 1350, "VIP alone 1350 cents");
        happy.startTurn(1, true);
        check(happy.getRemainingTurns() == 2, "Happy Hour starts with two turns");
        check(pricing.quote(new VIPCustomer(2, 1), MenuItem.BURGER) == 1080, "VIP then Happy Hour 1080 cents");
        check(pricing.quote(new Customer(1, 1), combo) == 1944, "combo plus Happy Hour 1944 cents");
        Order discounted = new Order(MenuItem.BURGER, pricing.quote(new VIPCustomer(2, 1), MenuItem.BURGER));
        happy.startTurn(2, false);
        check(happy.getRemainingTurns() == 1, "Happy Hour second turn");
        happy.startTurn(3, false);
        check(!happy.isActive(), "Happy Hour expires");
        check(discounted.getBillCents() == 1080, "discounted order remains frozen after expiry");
        rejects(() -> happy.startTurn(4, true), "Happy Hour cannot be reused");
        happy.startTurn(4, false);
        check(!happy.isActive(), "failed activation does not advance turn");
        Customer regular = new Customer(1, 1);
        Customer vip = new VIPCustomer(2, 1);
        Customer critic = new CriticCustomer(3, 1);
        regular.losePatience(); vip.losePatience(); critic.losePatience();
        check(regular.getSatisfaction() == 92 && vip.getSatisfaction() == 95 && critic.getSatisfaction() == 88,
                "polymorphic losses 8 / 5 / 12");
        regular.recoverSatisfaction(15);
        check(regular.getSatisfaction() == 100, "recovery capped at 100");
        check(critic.chooseMeal(Menu.withCombo()).getPriceCents() == 2430, "Critic selects longest preparation combo");
        Customer abandoned = new Customer(1, 1);
        abandoned.seat(); abandoned.placeOrder(new Order(MenuItem.SALAD, 900));
        for (int i = 0; i < 13; i++) { abandoned.losePatience(); }
        check(abandoned.getStatus().equals(CustomerStatus.LEFT), "unserved customer abandons at zero");
        check(abandoned.getOrder().getStatus().equals(OrderStatus.CANCELLED), "abandonment cancels unpaid order");
        abandoned.recoverSatisfaction(15);
        check(abandoned.getSatisfaction() == 0, "departed customer immune to recovery");

        // Worked regular Pasta timeline: seat t1, order/cook t2, ready t3,
        // serve t4, READY_TO_PAY t5, pay t6. Waiting loss is applied t1-t3.
        Customer pasta = new Customer(3, 1);
        Table table = new Table(1);
        Waiter waiter = new Waiter(new PricingService(new HappyHour()));
        Chef chef = new Chef();
        waiter.seat(pasta, table); pasta.losePatience();
        rejects(() -> waiter.takeOrder(pasta, new Menu()), "Waiter one task per turn");
        waiter.startTurn(); waiter.takeOrder(pasta, new Menu()); chef.prepare(pasta, 2);
        pasta.losePatience();
        check(pasta.getOrder().getRemainingUnits() == 1, "Pasta t2 one unit remains");
        rejects(() -> chef.prepare(pasta, 2), "Chef one unit per turn");
        chef.startTurn(); chef.prepare(pasta, 3); pasta.losePatience();
        check(pasta.getOrder().getStatus().equals(OrderStatus.READY), "Pasta t3 READY");
        waiter.startTurn();
        rejects(() -> waiter.serve(pasta, 3), "cannot serve in readiness turn");
        check(waiter.isAvailable(), "invalid service preserves task");
        waiter.serve(pasta, 4); pasta.finishEating(4); pasta.losePatience();
        check(pasta.getStatus().equals(CustomerStatus.EATING), "Pasta t4 EATING");
        check(pasta.getSatisfaction() == 76, "Pasta waits exactly three turns");
        check(!table.isFree(), "table occupied until payment");
        pasta.finishEating(5);
        check(pasta.getStatus().equals(CustomerStatus.READY_TO_PAY) && !pasta.canPay(5), "Pasta t5 READY_TO_PAY not chargeable");
        rejects(() -> pasta.pay(5), "cannot charge in READY_TO_PAY transition turn");
        PaymentRecord payment = pasta.pay(6); table.releaseIfFinished();
        check(payment.getBillCents() == 1800 && payment.getSatisfaction() == 76, "Pasta t6 payment 1800 cents / 76 satisfaction");
        check(table.isFree(), "payment releases table");
        pasta.recoverSatisfaction(15); pasta.losePatience();
        check(pasta.getSatisfaction() == 76 && payment.getSatisfaction() == 76, "paid customer and payment snapshot immune");

        GameController game = new GameController(); game.beginTurn();
        rejects(() -> game.seatWithHost(99), "invalid Host ID rejects");
        check(game.getPhase().equals(TurnPhase.HOST), "invalid Host action preserves phase");
        game.seatWithHost(1);
        rejects(() -> game.serveCustomer(1), "invalid Waiter action rejects");
        check(game.getPhase().equals(TurnPhase.WAITER), "invalid Waiter action preserves phase");
        game.takeOrder(1); game.prepareOrder(1); game.finishTurn();
        check(game.getCustomers().get(0).getOrder().getStatus().equals(OrderStatus.READY), "Host seat plus Waiter order in same turn");
        check(game.getPaidCount() == 0 && game.getRevenueCents() == 0, "unpaid order excluded from revenue");
        game.beginTurn(true); game.waitHost(); game.serveCustomer(1); game.waitChef(); game.finishTurn();
        check(game.getCustomers().get(0).getOrder().getBillCents() == 900, "prior order frozen through Happy Hour activation");
        game.beginTurn(); idle(game);
        check(game.getPaidCount() == 0, "controller does not collect prematurely");
        game.beginTurn(); idle(game);
        check(game.getPaidCount() == 1 && game.getRevenueCents() == 900 && game.getPaidSatisfactionTotal() == 100,
                "controller payment records recovered satisfaction");
        check(new GameResult(4, 4500, 240).isVictory(), "exact victory boundaries");
        check(!new GameResult(3, 4500, 240).isVictory(), "paid count threshold");
        check(!new GameResult(4, 4499, 240).isVictory(), "revenue threshold");
        check(!new GameResult(4, 4500, 239).isVictory(), "unrounded satisfaction threshold");
        System.out.println("CHECKS=" + checks);
    }
}
