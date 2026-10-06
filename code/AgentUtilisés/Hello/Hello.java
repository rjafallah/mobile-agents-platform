public class Hello extends AgentImpl {

    private int state = 0;

    private final Node n1 = new Node("localhost", 2001);
    private final Node n2 = new Node("localhost", 2002);

    @Override
    public void main() throws MoveException {
        if (state == 0) {
            state = 1;
            System.out.println("[Hello] before move to node1 " + n1);
            move(n1);
        } else if (state == 1) {
            state = 2;
            System.out.println("[Hello] before move to node2 " + n2);
            move(n2);
        } else if (state == 2) {
            state = 3;
            System.out.println("[Hello] before back()");
            back();
        } else {
            System.out.println("[Hello] I am back, notify lock");
            Object lock = getNameServer().get(getClass().getName() + "_lock");
            synchronized (lock) {
                lock.notify();
            }
        }
    }
}
