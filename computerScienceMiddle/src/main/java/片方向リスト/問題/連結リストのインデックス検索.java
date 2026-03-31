package 片方向リスト.問題;

/**
 * 片方向リストの先頭ノード head と自然数 index が与えられるので、片方向リスト内の index 番目の要素の値を返す getIndexValue という関数を作成してください。
 * <p>
 * 入力のデータ型： SinglyLinkedListNode<integer> head, integer index
 * 出力のデータ型： integer
 * <p>
 * getIndexValue(singlyLinkedList([3,2,1,5,6,4]),1) --> 3
 * getIndexValue(singlyLinkedList([7,8,2,3,1,7,8,11,4,3,2]),5) --> 1
 * getIndexValue(singlyLinkedList([34,35,64,34,10,2,14,5,353,23,35,63,23]),7) --> 14
 */

public class 連結リストのインデックス検索 {

    static class SinglyLinkedListNode<E> {
        public E data;
        public SinglyLinkedListNode<E> next;

        public SinglyLinkedListNode(E data) {
            this.data = data;
            this.next = null;
        }
    }

    static class Solution {
        public static int getIndexValue(SinglyLinkedListNode<Integer> head, int index) {
            for (int i = 1; i < index; i++) {
                head = head.next;
            }
            return head.data;
        }
    }
}
