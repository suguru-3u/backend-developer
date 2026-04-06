package 片方向リスト.問題;

/**
 * 先頭と末尾に挿入
 *
 * 連結リストの先頭 head と整数 data が与えられるので、リストの先頭と末尾にデータを挿入した新しい連結リストを返す、insertHeadTail という関数を作成してください。
 */

public class 先頭と末尾に挿入 {
}

class SinglyLinkedListNode<E>{
    public E data;
    public SinglyLinkedListNode<E> next;

    public SinglyLinkedListNode(E data){
        this.data = data;
        this.next = null;
    }
}

class Solution{
    public static SinglyLinkedListNode<Integer> insertHeadTail(SinglyLinkedListNode<Integer> head, int data){
        SinglyLinkedListNode<Integer> newNode = new SinglyLinkedListNode<Integer>(data);
        newNode.next = head;

        if(head == null){
            return newNode;
        }

        // 末尾に追加するノードを作成
        SinglyLinkedListNode<Integer> newTailNode = new SinglyLinkedListNode<>(data);

        SinglyLinkedListNode<Integer> current = head;
        while(current.next != null){
            current = current.next;
        }
        current.next = newTailNode;

        return newNode;
    }
}