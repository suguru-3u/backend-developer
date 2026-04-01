package 片方向リスト.問題;

/**
 * 整数で構成される配列 arr が与えられるので、片方向リスト化する getLinkedList という関数を作成してください。
 * <p>
 * 入力のデータ型： integer[] arr
 * 出力のデータ型： SinglyLinkedListNode<integer>
 * <p>
 * getLinkedList([3,2,1,5,6,4]) --> 3➡2➡1➡5➡6➡4➡END
 * getLinkedList([7,8,2,3,1,7,8,11,4,3,2]) --> 7➡8➡2➡3➡1➡7➡8➡11➡4➡3➡2➡END
 * getLinkedList([34,35,64,34,10,2,14,5,353,23,35,63,23]) --> 34➡35➡64➡34➡10➡2➡14➡5➡353➡23➡35➡63➡23➡END
 * getLinkedList([1]) --> 1➡END
 */

class SinglyLinkedListNode<E> {
    public E data;
    public SinglyLinkedListNode<E> next;

    public SinglyLinkedListNode(E data) {
        this.data = data;
        this.next = null;
    }
}

class Solution {
    public static SinglyLinkedListNode<Integer> getLinkedList(int[] arr) {
        SinglyLinkedListNode<Integer> linkedList = new SinglyLinkedListNode<Integer>(arr[0]);

        SinglyLinkedListNode<Integer> currentNode = linkedList;
        for (int i = 1; i < arr.length; i++) {
            currentNode.next = new SinglyLinkedListNode<Integer>(arr[i]);
            currentNode = currentNode.next;
        }

        return linkedList;
    }
}

public class 連結リスト化 {
}

