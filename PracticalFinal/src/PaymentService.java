public class PaymentService {
    static BalanceService balanceService = new BalanceService();
    static NotificationService notificationService = new NotificationService();
    static void makePayment(String senderId, String receiverId, double amount){
        balanceService.updateBalance(senderId, -amount);
        balanceService.updateBalance(receiverId, amount);
        notificationService.sendNotification(senderId, amount + "deducted");
        notificationService.sendNotification(senderId, amount + "added");
    }

    public static void main(String[] args) {
        System.out.println(balanceService.getBalance("111"));
        System.out.println(balanceService.getBalance("222"));
        makePayment("111", "222", 100.0);
        System.out.println(balanceService.getBalance("111"));
        System.out.println(balanceService.getBalance("222"));

    }
}
