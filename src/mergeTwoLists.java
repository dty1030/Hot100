
/**/
/*
list1和 list2 均按照非递减顺序排列--
*
 */
class mergeTwoLists {
    public ListNode mergeTwoLists(ListNode list1, ListNode list2) {
        //Boarder case:

        if (list1 == null && list2 == null)return null;
        if (list1 == null)return list2;
        if (list2 == null)return list1;

        ListNode current = list1.val > list2.val ? list2 : list1;
        ListNode cur1 = list1;
        ListNode cur2 = list2;
        ListNode startNode = list1.val > list2.val ? list2 : list1;
        if (startNode == list1)cur1 = list1.next;
        if (startNode == list2)cur2 = list2.next;
        while (cur1 != null && cur2 != null){

            if (cur1.val > cur2.val) {
                current.next = cur2;
                current = cur2;
                cur2 = cur2.next;
            }
            else {
                current.next = cur1;
                current = cur1;
                cur1 = cur1.next;
            }

        }
        while (cur1 != null){
                current.next = cur1;
                current = cur1;
                cur1 = cur1.next;
            }
        while (cur2 != null){
            current.next = cur2;
            current = cur2;
            cur2 = cur2.next;
        }
        return startNode;
    }
}