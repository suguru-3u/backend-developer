package 片方向リスト.問題;

/**
 * 片方向リストの先頭ノード head が与えられるので、偶数番目の値を 2 倍にし、それぞれの要素の後ろに追加し、連結リストを返す doubleEvenNumber という関数を作成してください。
 * <p>
 * 入力のデータ型： SinglyLinkedListNode<integer> head
 * 出力のデータ型： SinglyLinkedListNode<integer>
 * <p>
 * doubleEvenNumber(singlyLinkedList([3,2,1,5,6,4])) --> 3➡6➡2➡1➡2➡5➡6➡12➡4➡END
 * doubleEvenNumber(singlyLinkedList([3])) --> 3➡6➡END
 * doubleEvenNumber(singlyLinkedList([3,1])) --> 3➡6➡1➡END
 * doubleEvenNumber(singlyLinkedList([3,1,5])) --> 3➡6➡1➡5➡10➡END
 */

public class 偶数番目を2倍 {
}

class SinglyLinkedListNode<E> {
    public E data;
    public SinglyLinkedListNode<E> next;

    public SinglyLinkedListNode(E data) {
        this.data = data;
        this.next = null;
    }

    // addNodeなどの関数を作成しても良かったかもしれない。。そうすることでSinglyLinkedListNode関数をもう少しすっきりさせることができた。。
}

class Solution {
    public static SinglyLinkedListNode<Integer> doubleEvenNumber(SinglyLinkedListNode<Integer> head) {
        // 関数を完成させてください
        int index = 1;
        SinglyLinkedListNode<Integer> current = head;
        while (current != null) {
            index++;
            if(index % 2 == 0) {
                SinglyLinkedListNode<Integer> evenNum = new SinglyLinkedListNode<Integer>(current.data * 2);
                evenNum.next = current.next;
                current.next = evenNum;
                current = evenNum.next;
            } else {
                current = current.next;
            }
        }
        return head;
    }
}
