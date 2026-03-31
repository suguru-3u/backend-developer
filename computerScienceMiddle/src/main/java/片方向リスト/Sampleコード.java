package 片方向リスト;

public class Sampleコード {

    static class Node {
        public int data;
        public Node next;

        public Node(int data) {
            this.data = data;
            // nextを割り当てないでください。オブジェクト変数はヒープアドレスへの参照を保持するだけで、アクセス演算子「.」を使ってデータにアクセスします。オブジェクトが何も代入されていない場合は、何も指していないのでnullを保持します。
        }
    }

    // 先頭のノードを保持するコンテナで、リスト自体を表します。
    static class SinglyLinkedList {
        public Node head;

        public SinglyLinkedList(Node head) {
            this.head = head;
        }
    }

    public static void main(String[] args) {
        // 3つのノードを作成します。
        Node node1 = new Node(4);
        Node node2 = new Node(65);
        Node node3 = new Node(24);

        SinglyLinkedList numList = new SinglyLinkedList(node1);

        // リストに他のリストを追加します。
        // nodeはオブジェクトなので、=は値ではなく、メモリアドレスを指している点に注意してください。
        numList.head.next = node2;
        numList.head.next.next = node3;

        // 連結リストを反復します。
        // 反復によって、現在のノードは次のノードになります。この処理を最後のノードまで繰り返します。
        Node currentNode = numList.head;
        while (currentNode != null) {
            // 現在のノードを出力します。
            System.out.println(currentNode.data);
            currentNode = currentNode.next;
        }
    }
}
