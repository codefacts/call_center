import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by someone on 22/11/2015.
 */
public class Main {

    public static List<EnergyUsageData> process(final Collection<EnergyUsageData> data) {
        final Map<EnergyType, Map<YearMonth, BigDecimal>> tempMap = new HashMap<>();
        final List<EnergyUsageData> energyUsageData = data.stream()
                .flatMap(d -> d.getBillsMap().entrySet().stream().map(
                        dd -> new FlatData(d.getType(), dd.getKey(), dd.getValue())))
                .filter(d -> {
                    final Map<YearMonth, BigDecimal> map = tempMap.get(d.type);
                    if (map != null) {
                        final BigDecimal amount = map.get(d.yearMonth);
                        map.put(d.yearMonth, amount.add(d.amount));
                        return false;
                    }
                    return true;
                })
                .map(d -> {
                    final HashMap<YearMonth, BigDecimal> billsMap = new HashMap<>();
                    billsMap.put(d.yearMonth, d.amount);
                    tempMap.put(d.type, billsMap);
                    return new EnergyUsageData(d.type, billsMap);
                    //Assuming Constructor of EnergyUsageData will hold
                    // the reference of billsMap and will not copy billsMap
                })
                .collect(Collectors.toList());
        return energyUsageData;
    }

    static final class FlatData {
        public final EnergyType type;
        public final YearMonth yearMonth;
        public final BigDecimal amount;

        FlatData(EnergyType type, YearMonth yearMonth, BigDecimal amount) {
            this.type = type;
            this.yearMonth = yearMonth;
            this.amount = amount;
        }
    }
}

class EnergyUsageData {
    private EnergyType type;
    private Map<YearMonth, BigDecimal> billsMap;

    public EnergyUsageData(final EnergyType type, final Map<YearMonth, BigDecimal> billsMap) {
        this.setType(type);
        this.setBillsMap(billsMap);
    }

    public EnergyType getType() {
        return type;
    }

    public void setType(EnergyType type) {
        this.type = type;
    }

    public Map<YearMonth, BigDecimal> getBillsMap() {
        return billsMap;
    }

    public void setBillsMap(Map<YearMonth, BigDecimal> billsMap) {
        this.billsMap = billsMap;
    }
}

enum EnergyType {diesel, soyabin}

class YearMonth {
    int year;
    int month;
}