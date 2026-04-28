import java.util.HashMap;
import java.util.Map;

public class BalanceService {

    HashMap<String, Double> balanceRepo = new HashMap<>(Map.of("111", 1000.0, "222", 1500.0));
    double getBalance(String accountId){
        System.out.println("getting balance for id" + accountId);
        return balanceRepo.get(accountId);
    }

    void updateBalance(String accountId, Double amount){
        double currBalance =  balanceRepo.get(accountId);
        balanceRepo.put(accountId, currBalance + amount);
    }
}
