package スタック;

class Node {
    public int data;
    public Node next;

    public Node(int data) {
        this.data = data;
        this.next = null;
    }
}

class Stack {
    public Node head;

    public Stack() {
        this.head = null;
    }

    public void push(int data) {
        Node temp = this.head;
        this.head = new Node(data);
        this.head.next = temp;
    }

    public Integer pop() {
        if (this.head == null) return null;
        Node temp = this.head;
        this.head = this.head.next;
        return temp.data;
    }

    public Integer peek() {
        if (this.head == null) return null;
        return this.head.data;
    }
}

class Sample {

    public static void main(String[] args) {

        Stack stack = new Stack();
        stack.push(2);
        System.out.println(stack.peek());

        stack.push(4);
        stack.push(3);
        stack.push(1);
        stack.pop();
        System.out.println(stack.peek());
    }
}