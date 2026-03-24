
class ListNode {
    int val;
    ListNode next;
    ListNode() {}
    ListNode(int val) { this.val = val; }
    ListNode(int val, ListNode next) { this.val = val; this.next = next; }

    public ListNode reverseList(ListNode head){

        if (head == null)return null;
        if (head.next == null)return head;
        ListNode Next = head.next;
        ListNode Prev = null;
        ListNode cur = head;
        while (cur != null){
            //先获取下一个节点
            Next = cur.next;
            //当前节点指针反转
            cur.next = Prev;
            //将Prev改为当前节点
            Prev = cur;
            //当前节点变为下一个
            cur = Next;
        }
        return Prev;


    }
    /*
     * 递归法
     *
    public ListNode reverseList(ListNode head) {
        if (head == null || head.next == null) return head;
        ListNode newHead = reverseList(head.next);
        head.next.next = head;
        head.next = null;
        return ___;
    }

     */

}



