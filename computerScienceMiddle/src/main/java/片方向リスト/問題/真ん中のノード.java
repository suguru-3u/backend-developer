package 片方向リスト.問題;

/**
 * 片方向リストの先頭 head が与えられるので、真ん中のノードを返す、middleNode という関数を作成してください。真ん中のノードが 2 つ存在する場合は 2 つ目のノードを返してください。
 *
 * 例えば、入力が [1,2,3,4,5,6,7,8] の場合、真ん中のノードは 4 と 5 の 2 つになります。この場合、2 つ目を優先させるので、答えは [5,6,7,8] になります。
 */

public class 真ん中のノード {
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
    public static SinglyLinkedListNode<Integer> middleNode(SinglyLinkedListNode<Integer> head){
        int count = 0;
        SinglyLinkedListNode<Integer> current = head;
        while(current != null){
            count++;
            current = current.next;
        }

        int middleNum = count / 2 + 1;

        count = 0;
        current = head;
        for(int i = 1; i < middleNum ; i++){
            current = current.next;
        }

        return current;
    }
}

