import java.util.*;

public class Question {
    public static void main(String[] args) {
        System.out.println("test");
        OrderBook OB = new OrderBook();
        while (true) {
            Scanner sc = new Scanner(System.in);
            System.out.print("Enter command: ");
            String command = sc.nextLine();
            if (command.equals("END")) {
                break;
            }
            String[] result = command.split(" ");
            System.out.println(Arrays.toString(result));
            if (result[1].equals("LO") || result[1].equals("IOC") || result[1].equals("FOK")) {
                PricedOrder order = new PricedOrder(result[3], result[2].charAt(0), Integer.parseInt(result[4]),
                        Integer.parseInt(result[5]), result[1]);
                OB.executeOrder(order);
                System.out.println(OB.toString());
            } else if (result[1].equals("MO")) {
                MarketOrder order = new MarketOrder(result[3], result[2].charAt(0), Integer.parseInt(result[4]));
                System.out.println(order);
                OB.executeOrder(order);
                System.out.println(OB.toString());
            } else if (result[0].equals("CXL")) {
                OB.cancelOrder(result[1]);
                System.out.println(OB.toString());
            }
        }

    }
}

class MarketOrder {
    private String id;
    private char side;
    private int quantity;

    public MarketOrder(String id, char side, int quantity) {
        this.id = id;
        this.side = side;
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public char getSide() {
        return side;
    }

    public void setSide(char side) {
        this.side = side;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String toString() {
        return quantity + "#" + id;
    }
}

class PricedOrder extends MarketOrder {
    private int price;
    private String orderType; // LO,IOC,FOK

    public PricedOrder(String id, char side, int quantity, int price, String orderType) {
        super(id, side, quantity);
        this.price = price;
        this.orderType = orderType;
    }

    public int getPrice() {
        return price;
    }

    public String getOrderType() {
        return orderType;
    }

    public String toString() {
        return super.getQuantity() + "@" + price + "#" + super.getId();
    }
}


class OrderBook {
    private ArrayList<PricedOrder> buy_orders; //higher price has higher priority
    private ArrayList<PricedOrder> sell_orders; //lower price has lower priority

    public OrderBook() {
        this.buy_orders = new ArrayList<PricedOrder>();
        this.sell_orders = new ArrayList<PricedOrder>();
    }

    public void executeOrder(MarketOrder order) {
        // determine if limit or market -> buy or sell -> match -> for limit, add excess into OB
        ArrayList<PricedOrder> orders = buy_orders;
        if (order.getSide() == 'B') {
            orders = sell_orders;
        }
        if (order instanceof PricedOrder) { // PricedOrder (limit/ immediate or cancel / fill or kill)
            PricedOrder submittedOrder = (PricedOrder) order;
            if (submittedOrder.getOrderType().equals("LO") || submittedOrder.getOrderType().equals("IOC")) {
                matchPricedOrder(submittedOrder, orders);
//                if (submittedOrder.getSide() == 'B' && orders.size() == 0) {
//                    buy_orders.add(submittedOrder);
//                } else if (submittedOrder.getSide() == 'S' && orders.size() == 0) {
//                    sell_orders.add(submittedOrder);
//                } else {
//                    Iterator<PricedOrder> iter = orders.iterator();
//                    while (iter.hasNext()) {
//                        PricedOrder each_order = iter.next();
//                        if (submittedOrder.getSide() == 'B' && (submittedOrder.getPrice() >= each_order.getPrice())) {
//                            int remaining_quantity = each_order.getQuantity() - submittedOrder.getQuantity();
//                            if (remaining_quantity == 0) {
//                                iter.remove();
//                                submittedOrder.setQuantity(0);
//                            } else if (remaining_quantity > 0) {
//                                each_order.setQuantity(remaining_quantity);
//                                submittedOrder.setQuantity(0);
//                            } else {
//                                iter.remove();
//                                submittedOrder.setQuantity(Math.abs(remaining_quantity));
//                            }
//                        } else if (submittedOrder.getSide() == 'S' && (submittedOrder.getPrice() <= each_order.getPrice())) {
//                            int remaining_quantity = each_order.getQuantity() - submittedOrder.getQuantity();
//                            if (remaining_quantity == 0) {
//                                iter.remove();
//                                submittedOrder.setQuantity(0);
//                            } else if (remaining_quantity > 0) {
//                                each_order.setQuantity(remaining_quantity);
//                                submittedOrder.setQuantity(0);
//                            } else {
//                                iter.remove();
//                                submittedOrder.setQuantity(Math.abs(remaining_quantity));
//                            }
//                        } else {
//                            break;
//                        }
//                    }
//                    // add buy order to OB
//                    if (submittedOrder.getQuantity() != 0 && submittedOrder.getOrderType().equals("LO")) {
//                        if (submittedOrder.getSide() == 'B') {
//                            buy_orders.add(submittedOrder);
//                        } else {
//                            sell_orders.add(submittedOrder);
//                        }
//                    }
//                }
//                if (submittedOrder.getSide() == 'B') {
//                    buy_orders.sort((o1, o2) -> Integer.valueOf(o2.getPrice()).compareTo(Integer.valueOf(o1.getPrice())));
//                } else {
//                    sell_orders.sort((o1, o2) -> Integer.valueOf(o1.getPrice()).compareTo(Integer.valueOf(o2.getPrice())));
//                }
            } else if (submittedOrder.getOrderType().equals("FOK")) {
                ArrayList<PricedOrder> order_clone = (ArrayList) orders.clone();
                int remaining_quantity = matchPricedOrder(submittedOrder,order_clone); //test if order is complete on
                // clone
                if (remaining_quantity==0){
                    matchPricedOrder(submittedOrder,orders); //actual matching
                }

            } else { //MarketOrder
                Iterator<PricedOrder> iter = orders.iterator();
                while (iter.hasNext()) {
                    System.out.println("here");
                    PricedOrder each_order = iter.next();
                    int remaining_quantity = each_order.getQuantity() - order.getQuantity();
                    if (remaining_quantity == 0) {
                        iter.remove();
                        break;
                    } else if (remaining_quantity > 0) { // OB quantity > marketorder
                        each_order.setQuantity(remaining_quantity);
                        break;
                    } else { //marketorder > OB order
                        iter.remove();
                        order.setQuantity(Math.abs(remaining_quantity));
                    }
                }
            }
        }
    }

        public int matchPricedOrder (PricedOrder submittedOrder, ArrayList < PricedOrder > orders){
            if (submittedOrder.getSide() == 'B' && orders.size() == 0 && !submittedOrder.getOrderType().equals("FOK")) {
                buy_orders.add(submittedOrder);
            } else if (submittedOrder.getSide() == 'S' && orders.size() == 0 && !submittedOrder.getOrderType().equals("FOK")) {
                sell_orders.add(submittedOrder);
            } else if (submittedOrder.getOrderType().equals("FOK") && orders.size() == 0){
                return -1;
            } else {
                Iterator<PricedOrder> iter = orders.iterator();
                while (iter.hasNext()) {
                    PricedOrder each_order = iter.next();
                    if (submittedOrder.getSide() == 'B' && (submittedOrder.getPrice() >= each_order.getPrice())) {
                        int remaining_quantity = each_order.getQuantity() - submittedOrder.getQuantity();
                        if (remaining_quantity == 0) {
                            iter.remove();
                            submittedOrder.setQuantity(0);
                        } else if (remaining_quantity > 0) {
                            each_order.setQuantity(remaining_quantity);
                            submittedOrder.setQuantity(0);
                        } else {
                            iter.remove();
                            submittedOrder.setQuantity(Math.abs(remaining_quantity));
                        }
                    } else if (submittedOrder.getSide() == 'S' && (submittedOrder.getPrice() <= each_order.getPrice())) {
                        int remaining_quantity = each_order.getQuantity() - submittedOrder.getQuantity();
                        if (remaining_quantity == 0) {
                            iter.remove();
                            submittedOrder.setQuantity(0);
                        } else if (remaining_quantity > 0) {
                            each_order.setQuantity(remaining_quantity);
                            submittedOrder.setQuantity(0);
                        } else {
                            iter.remove();
                            submittedOrder.setQuantity(Math.abs(remaining_quantity));
                        }
                    } else {
                        break;
                    }
                }
                // if FOK and remaining quantity not 0, return
                if (submittedOrder.getQuantity() != 0 && submittedOrder.getOrderType().equals("FOK")) {
                    return submittedOrder.getQuantity();
                }
                // add buy order to OB
                if (submittedOrder.getQuantity() != 0 && submittedOrder.getOrderType().equals("LO")) {
                    if (submittedOrder.getSide() == 'B') {
                        buy_orders.add(submittedOrder);
                    } else {
                        sell_orders.add(submittedOrder);
                    }
                }
            }
            if (submittedOrder.getSide() == 'B') {
                buy_orders.sort((o1, o2) -> Integer.valueOf(o2.getPrice()).compareTo(Integer.valueOf(o1.getPrice())));
            } else {
                sell_orders.sort((o1, o2) -> Integer.valueOf(o1.getPrice()).compareTo(Integer.valueOf(o2.getPrice())));
            }
            return 0;
        }

        public void cancelOrder (String orderID){
            ArrayList<PricedOrder> totalList = new ArrayList<>();
            totalList.addAll(buy_orders);
            totalList.addAll(sell_orders);
            for (PricedOrder each_order : totalList) {
                if (orderID.equals(each_order.getId())) {
                    if (each_order.getSide() == 'B') {
                        buy_orders.remove(each_order);
                    } else {
                        sell_orders.remove(each_order);
                    }
                    break;
                }
            }

        }

        public String toString () {
            String returnString = "B:";
            for (PricedOrder order : buy_orders) {
                returnString += " " + order.toString();
            }
            returnString += "\nS:";
            for (PricedOrder order : sell_orders) {
                returnString += " " + order.toString();
            }
            return returnString;
        }
    }

//            if (submittedOrder.getSide() == 'B'){ //BUY
//                if (sell_orders.size() == 0){
//                    buy_orders.add(submittedOrder);
//                }
//                else{
//                    for (PricedOrder sell_order: sell_orders){
//                        if (submittedOrder.getPrice() >= sell_order.getPrice()){
//                            int remaining_quantity = sell_order.getQuantity() - submittedOrder.getQuantity();
//                            if (remaining_quantity == 0){
//                                sell_orders.remove(sell_order);
//                                submittedOrder.setQuantity(0);
//                            }
//                            else if (remaining_quantity > 0){
//                                sell_order.setQuantity(remaining_quantity);
//                                submittedOrder.setQuantity(0);
//                            }
//                            else{
//                                sell_orders.remove(sell_order);
//                                submittedOrder.setQuantity(Math.abs(remaining_quantity));
//                            }
//                        }
//                        else{
//                            break;
//                        }
//                    }
//                    // add buy order to OB
//                    if (submittedOrder.getQuantity()!=0){
//                        buy_orders.add(submittedOrder);
//                    }
//               }
//                buy_orders.sort((o1, o2)-> Integer.valueOf(o2.getPrice()).compareTo(Integer.valueOf(o1.getPrice())));
//            }
//            else{ //SELL
//                if (buy_orders.size() == 0){
//                    sell_orders.add(submittedOrder);
//                }
//                else{
//                    for (PricedOrder buy_order: buy_orders){
//                        if (submittedOrder.getPrice() <= buy_order.getPrice()){
//                            int remaining_quantity = buy_order.getQuantity() - submittedOrder.getQuantity();
//                            if (remaining_quantity == 0){
//                                buy_orders.remove(buy_order);
//                                submittedOrder.setQuantity(0);
//                            }
//                            else if (remaining_quantity > 0){
//                                buy_order.setQuantity(remaining_quantity);
//                                submittedOrder.setQuantity(0);
//                            }
//                            else{
//                                buy_orders.remove(order);
//                                submittedOrder.setQuantity(Math.abs(remaining_quantity));
//                            }
//                        }
//                        else{
//                            break;
//                        }
//                    }
//                    // add sell order to OB
//                    if (submittedOrder.getQuantity()!=0){
//                        sell_orders.add(submittedOrder);
//                    }
//                }
//                sell_orders.sort((o1, o2)-> Integer.valueOf(o1.getPrice()).compareTo(Integer.valueOf(o2.getPrice())));
