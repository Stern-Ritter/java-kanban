package utils;

import java.util.ArrayList;
import java.util.List;

public class CustomLinkedList<T> {
    private Node<T> head;
    private Node<T> tail;
    private int size;

    public Node<T> linkLast(T element) {
        if (element == null) {
            throw new IllegalArgumentException("Method is called with null argument.");
        }

        Node<T> addedNode = new Node<>(null, null, element);
        if (head == null) {
            head = addedNode;
            tail = addedNode;
        } else {
            addedNode.setPrevious(tail);
            tail.setNext(addedNode);
            tail = addedNode;
        }
        size += 1;

        return addedNode;
    }

    public T removeFirst() {
        T removedElement = null;

        if (head != null) {
            removedElement = head.getData();
            head = head.getNext();
            if (head != null) {
                head.setPrevious(null);
            }
            size -= 1;
        }

        return removedElement;
    }

    public void removeNode(Node<T> node) {
        if (size == 0) return;
        if (node == null) {
            throw new IllegalArgumentException("Method is called with null argument.");
        }

        Node<T> previousNode = node.getPrevious();
        Node<T> nextNode = node.getNext();

        if (node == head) {
            head = nextNode;
        }
        if (node == tail) {
            tail = previousNode;
        }
        if (nextNode != null) {
            nextNode.setPrevious(previousNode);
        }
        if (previousNode != null) {
            previousNode.setNext(nextNode);
        }

        size -= 1;
    }

    public List<T> getTasks() {
        List<T> elements = new ArrayList<>();
        Node<T> currentNode = head;

        while (currentNode != null) {
            T task = currentNode.getData();
            elements.add(task);
            currentNode = currentNode.getNext();
        }

        return elements;
    }

    public int size() {
        return size;
    }
}
