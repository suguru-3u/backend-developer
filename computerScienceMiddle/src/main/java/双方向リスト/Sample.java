package 双方向リスト;

class Node {
    // 前後を追跡します。
    public int data;
    public Node prev;
    public Node next;

    public Node(int data) {
        this.data = data;
    }
}

// リストは少なくとも1つのノードを持っている必要があります。
// ヌルリストをサポートしたい場合は、それに応じてコードを追加してください。
class DoublyLinkedList {
    public Node head;
    public Node tail;

    public DoublyLinkedList(int[] arr) {
        // 今回は末尾を追跡します。
        if (arr.length <= 0) {
            this.head = null;
            this.tail = null;
            return;
        }

        this.head = new Node(arr[0]);
        Node currentNode = this.head;
        for (int i = 1; i < arr.length; i++) {
            currentNode.next = new Node(arr[i]);
            // 次のノードの前のノードをcurrent Nodeに割り当てます。
            currentNode.next.prev = currentNode;
            currentNode = currentNode.next;
        }
        // このcurrent Nodeは最後のnodeです。
        this.tail = currentNode;
    }

    public void printList() {
        Node iterator = this.head;
        String str = "";
        while (iterator != null) {
            str += iterator.data + " ";
            iterator = iterator.next;
        }
        System.out.println("[" + str + "]");
    }
}

class Sample {
    public static void main(String[] args) {
        int[] arr = new int[]{1, 2, 3, 4, 5, 6, 7};
        DoublyLinkedList numList = new DoublyLinkedList(arr);

        numList.printList();
        System.out.println(numList.head.data);
        System.out.println(numList.head.next.data);
        System.out.println(numList.head.next.prev.data);

        System.out.println(numList.tail.data);
        System.out.println(numList.tail.prev.data);
    }
}

class MyClass {
    public static void main(String[] args) {
        Stack s1 = new Stack();

        s1.push(2);

        System.out.println(s1.peek());

        s1.push(4);
        s1.push(3);
        s1.push(1);

        System.out.println(s1.pop());
        System.out.println(s1.peek());


        Stack s2 = new Stack();

        System.out.println(s2.pop());

        s2.pop();

        s2.push(9);
        s2.push(8);

        System.out.println(s2.peek());
    }
}

class Item {
    public int data;
    public Item next;

    public Item(int data) {
        this.data = data;
        this.next = null
    }
}

class Stack {
    public Item head;

    public Stack() {
        this.head = null;
    }

    public void push(int data) {
        Item tmp = this.head;
        this.head = new Item(data);
        this.head.next = tmp;
    }

    public int peek() {
        return this.head.data;
    }

    public int pop() {
        Item tmp = this.head;
        this.head = this.head.next;
        return tmp.data;
    }
}
