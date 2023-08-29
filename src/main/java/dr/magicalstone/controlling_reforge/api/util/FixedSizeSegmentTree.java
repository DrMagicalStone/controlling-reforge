package dr.magicalstone.controlling_reforge.api.util;

import javax.annotation.Nonnull;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BinaryOperator;

/**
 * An implement of segment tree.
 * Elements of a segment tree must be associative but not necessary be commutative. Element type and the operator to elements can be determined.
 * This implement of segment tree allows modifying any element, but doesn't allow adding or removing elements.
 * All elements are in their index.
 * A set of elements with continuously integer index is an interval or segment such as elements with index {2, 3, 4, 5, 6, 7}.
 * This segment can also be expressed by [2, 8). Similar to this example, in this implement, segments are always left closed right open.
 * This implements of segment tree (this tree) allows modifying any element by their index and
 * @param <Type> type of elements
 */
public class FixedSizeSegmentTree<Type> extends AbstractList<Type> implements RandomAccess, Iterable<Type> {

    /**
     * Size of this tree which means how many elements in this tree which can be set.
     */
    protected final int size;

    /**
     * The operator to combine elements.
     */
    protected final BinaryOperator<Type> operator;

    /**
     * All elements of this tree (leaf nodes) and their combination (non-leaf nodes).
     * It contains two types of elements, one is elements of this tree and the other are their combination.
     * By calculating their combinations, calculating the combination of all element of this tree is quicker (in O(log(n)) ).
     */
    protected final Type[] elements;

    /**
     * Contains each element of this tree's index in {@link FixedSizeSegmentTree#elements}.
     */
    protected final int[] realIndexIndex;

    /**
     * Initialize a segment tree and set its operator and all element's value.
     * @param operator the operator to combine elements
     * @param allElements all elements which will be put in this tree. This tree's {@link FixedSizeSegmentTree#size} will be set to this array's length
     */
    public FixedSizeSegmentTree(BinaryOperator<Type> operator, Type[] allElements) {
        this.size = allElements.length;
        this.operator = operator;
        int leafCapacity;
        if(size == 1) {
            leafCapacity = 1;
        } else {
            leafCapacity = (Integer.highestOneBit(size - 1) << 1);
        }
        this.elements = (Type[]) Array.newInstance(allElements.getClass().getComponentType(), leafCapacity * 2);
        realIndexIndex = new int[size];
        indexIterate:
        for (int index = 0; index < size; index ++) {
            int elementRealIndex = 1;
            for (int elementLeftBorder = 0, elementRightBorder = size; elementRightBorder - elementLeftBorder > 1;) {
                if (elementRealIndex >= (leafCapacity / 2)) {
                    Type leftChild = allElements[index];
                    Type rightChild = allElements[index + 1];
                    elements[elementRealIndex * 2] = leftChild;
                    elements[elementRealIndex * 2 + 1] = rightChild;
                    elements[elementRealIndex] = operator.apply(leftChild, rightChild);
                    realIndexIndex[index] = elementRealIndex * 2;
                    realIndexIndex[index + 1] = elementRealIndex * 2 + 1;
                    index = index + 1;
                    continue indexIterate;
                }
                int childElementMiddleBorder = (elementLeftBorder + elementRightBorder) / 2;
                if (index < childElementMiddleBorder) {
                    elementRightBorder = childElementMiddleBorder;
                    elementRealIndex = elementRealIndex * 2;
                } else {
                    elementLeftBorder = childElementMiddleBorder;
                    elementRealIndex = elementRealIndex * 2 + 1;
                }
            }
            realIndexIndex[index] = elementRealIndex;
            elements[elementRealIndex] = allElements[index];
        }
        for (int elementRealIndex = (leafCapacity / 2) - 1; elementRealIndex >= 1; elementRealIndex--) {
            elements[elementRealIndex] = operator.apply(elements[elementRealIndex * 2], elements[elementRealIndex * 2 + 1]);
        }
    }

    /**
     * For child classes.
     * @param size {@link FixedSizeSegmentTree#size}
     * @param operator {@link FixedSizeSegmentTree#operator}
     * @param elements {@link FixedSizeSegmentTree#elements}
     */
    protected FixedSizeSegmentTree(int size, BinaryOperator<Type> operator, Type[] elements) {
        this.size = size;
        this.operator = operator;
        this.elements = elements;
        realIndexIndex = new int[size];
    }


    /**
     * Set the element at index and get combination of all elements (elements in segment [0, size) ).
     * @param index index of the element to set
     * @param value new value of the element
     * @return combination of all elements
     */
    public Type setValueAndGetCombination(int index, @Nonnull Type value) {
        if (index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        int elementRealIndex = realIndexIndex[index];
        elements[elementRealIndex] = value;
        Type leftChild;
        Type rightChild;
        if ((elementRealIndex & 1) == 0) {
            leftChild = value;
            rightChild = elements[elementRealIndex + 1];
        } else {
            rightChild = value;
            leftChild = elements[elementRealIndex - 1];
        }
        while (elementRealIndex > 1) {
            elementRealIndex /= 2;
            if ((elementRealIndex & 1) == 0) {
                leftChild = elements[elementRealIndex] = operator.apply(leftChild, rightChild);
                rightChild = elements[elementRealIndex + 1];
            } else {
                rightChild = elements[elementRealIndex] = operator.apply(leftChild, rightChild);
                leftChild = elements[elementRealIndex - 1];
            }
        }
        return rightChild;
    }

    /**
     * Get the element at the index.
     * @param index index of the element to return
     * @return the element at the index
     */
    @Override
    public Type get(int index) {
        return elements[realIndexIndex[index]];
    }

    /**
     * Get combination of all elements (elements in segment [0, size) ).
     * @return combination of all elements
     */
    public Type getCombination() {
        return elements[1];
    }

    /**
     * Get combination of all elements (elements in segment [segmentLeftBorder, segmentRightBorder) ).
     * @param segmentLeftBorder left border of the segment of element's combination
     * @param segmentRightBorder right border of the segment of element's combination
     * @return combination of all elements (elements in segment [segmentLeftBorder, segmentRightBorder) )
     */
    public Type getCombination(int segmentLeftBorder, int segmentRightBorder) {
        if (segmentLeftBorder < 0 || segmentRightBorder <= segmentLeftBorder || segmentRightBorder > size) {
            throw new IllegalArgumentException("segmentLeftBorder and segmentRightBorder should larger than 0 and less than the tree's size and segmentRightBorder should larger than segmentLeftBorder.");
        }
        if (segmentLeftBorder == 0) {
            if (segmentRightBorder == size) {
                return elements[1];
            } else {
                return getCombinationLOutRIn(segmentRightBorder, 0, size, 1);
            }
        } else {
            if (segmentRightBorder == size) {
                return getCombinationLInROut(segmentLeftBorder, 0, size, 1);
            } else {
                return getCombinationLInRIn(segmentLeftBorder, segmentRightBorder);
            }
        }
    }

    private Type getCombinationLOutRIn(int segmentRightBorder, int leftBorder, int rightBorder, int elementRealIndex) {
        int currentLeftBorder = leftBorder;
        int currentRightBorder = rightBorder;
        int currentElementRealIndex = elementRealIndex;
        Type combination = null;
        while (true) {
            int currentChildElementMiddleBorder = (currentLeftBorder + currentRightBorder) / 2;
            if (segmentRightBorder < currentChildElementMiddleBorder) {
                currentRightBorder = currentChildElementMiddleBorder;
                currentElementRealIndex = 2 * currentElementRealIndex;
                continue;
            }
            if (currentLeftBorder == leftBorder) {
                combination = elements[2 * currentElementRealIndex];
            } else {
                combination = operator.apply(combination, elements[2 * currentElementRealIndex]);
            }
            if (segmentRightBorder == currentChildElementMiddleBorder) {
                return combination;
            }
            currentLeftBorder = currentChildElementMiddleBorder;
            currentElementRealIndex = 2 * currentElementRealIndex + 1;
        }
    }

    private Type getCombinationLInROut(int segmentLeftBorder, int leftBorder, int rightBorder, int elementRealIndex) {
        int currentLeftBorder = leftBorder;
        int currentRightBorder = rightBorder;
        int currentElementRealIndex = elementRealIndex;
        Type combination = null;
        while (true) {
            int currentChildElementMiddleBorder = (currentLeftBorder + currentRightBorder) / 2;
            if (segmentLeftBorder > currentChildElementMiddleBorder) {
                currentLeftBorder = currentChildElementMiddleBorder;
                currentElementRealIndex = 2 * currentElementRealIndex + 1;
                continue;
            }
            if (currentRightBorder == rightBorder) {
                combination = elements[2 * currentElementRealIndex + 1];
            } else {
                combination = operator.apply(elements[2 * currentElementRealIndex + 1], combination);
            }
            if (segmentLeftBorder >= currentChildElementMiddleBorder) {
                return combination;
            }
            currentRightBorder = currentChildElementMiddleBorder;
            currentElementRealIndex = 2 * currentElementRealIndex;
        }
    }

    private Type getCombinationLInRIn(int segmentLeftBorder, int segmentRightBorder) {
        int currentLeftBorder = 0;
        int currentRightBorder = size;
        int currentElementRealIndex = 1;
        while (true) {
            int currentChildElementMiddleBorder = (currentLeftBorder + currentRightBorder) / 2;
            if (segmentRightBorder == currentChildElementMiddleBorder) {
                return getCombinationLInROut(segmentLeftBorder, currentLeftBorder, currentChildElementMiddleBorder, currentElementRealIndex * 2);
            }
            if (segmentRightBorder < currentChildElementMiddleBorder) {
                currentRightBorder = currentChildElementMiddleBorder;
                currentElementRealIndex = currentElementRealIndex * 2;
                continue;
            }
            if (segmentLeftBorder == currentChildElementMiddleBorder) {
                return getCombinationLOutRIn(segmentRightBorder, currentChildElementMiddleBorder, currentRightBorder, currentElementRealIndex * 2 + 1);
            }
            if (segmentLeftBorder > currentChildElementMiddleBorder) {
                currentLeftBorder = currentChildElementMiddleBorder;
                currentElementRealIndex = currentElementRealIndex * 2 + 1;
                continue;
            }
            Type leftChild = getCombinationLInROut(segmentLeftBorder, currentLeftBorder, currentChildElementMiddleBorder, currentElementRealIndex * 2);
            Type rightChild = getCombinationLOutRIn(segmentRightBorder, currentChildElementMiddleBorder, currentRightBorder, currentElementRealIndex * 2 + 1);
            return operator.apply(leftChild, rightChild);
        }
    }

    /**
     * Get {@link FixedSizeSegmentTree#size} of this segment tree.
     * @return {@link FixedSizeSegmentTree#size}
     */
    public int size() {
        return size;
    }

    /**
     * Get an array with all elements in this segment tree.
     * @return an {@link Object} array with all elements in this segment tree
     */
    public Object[] toArray() {
        Object[] copy = new Object[size];
        for(int i = 0; i < size; i++) {
            copy[i] = elements[realIndexIndex[i]];
        }
        return copy;
    }

    @Override
    public <ArrayType> ArrayType[] toArray(ArrayType[] container) {
        ArrayType[] array;
        if (container.length < size) {
            array = (ArrayType[]) Array.newInstance(container.getClass().getComponentType(), size);
        } else {
            array = container;
        }
        for(int i = 0; i < size; i++) {
            array[i] = (ArrayType) elements[realIndexIndex[i]];
        }
        return array;
    }

    /**
     * Get an {@link Iterator} of all elements. Actually it is always a {@link ListIterator}.
     * @return an {@link Iterator} of all elements.
     */
    @Override
    public Iterator<Type> iterator() {
        return listIterator();
    }

    /**
     * This segment tree is also a {@link List}. To add an element to this list.
     * But, because this implement of segment tree doesn't allow adding or removing elements, it always does nothing and throws an {@link UnsupportedOperationException}.
     * @param type element whose presence in this collection is to be ensured
     * @return Always throw throws an {@link UnsupportedOperationException}, never return.
     */
    @Override
    public boolean add(Type type) {
        throw new UnsupportedOperationException();
    }

    /**
     * This segment tree is also a {@link List}. To add an element to this list.
     * But, because this implement of segment tree doesn't allow adding or removing elements, it always does nothing and throws an {@link UnsupportedOperationException}.
     * @param index index at which the specified element is to be inserted
     * @param element element to be inserted
     */
    @Override
    public void add(int index, Type element) {
        throw new UnsupportedOperationException();
    }

    /**
     * The same as {@link FixedSizeSegmentTree#setValueAndGetCombination(int, Object)}, but returns the last value at the index.
     * @param index index of the element to replace
     * @param element element to be stored at the specified position
     * @return the last value at the index.
     */
    @Override
    public Type set(int index, Type element) {
        Type lastValue = get(index);
        setValueAndGetCombination(index, element);
        return lastValue;
    }

    /**
     * This segment tree is also a {@link List}. To remove an object from this list.
     * But, because this implement of segment tree doesn't allow adding or removing elements, it always does nothing and throws an {@link UnsupportedOperationException}.
     * @param o element to be removed from this list, if present
     * @return Always throw throws an {@link UnsupportedOperationException}, never return.
     */
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }


    /**
     * This segment tree is also a {@link List}. To add elements to this list.
     * But, because this implement of segment tree doesn't allow adding or removing elements, it always does nothing and throws an {@link UnsupportedOperationException}.
     * @param c collection containing elements to be added to this collection
     * @return Always throw throws an {@link UnsupportedOperationException}, never return.
     */
    @Override
    public boolean addAll(Collection<? extends Type> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * This segment tree is also a {@link List}. To remove elements from this list.
     * But, because this implement of segment tree doesn't allow adding or removing elements, it always does nothing and throws an {@link UnsupportedOperationException}.
     * @param index index at which to insert the first element from the
     *              specified collection
     * @param c collection containing elements to be added to this list
     * @return Always throw throws an {@link UnsupportedOperationException}, never return.
     */
    @Override
    public boolean addAll(int index, Collection<? extends Type> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * This segment tree is also a {@link List}. To remove elements from this list.
     * But, because this implement of segment tree doesn't allow adding or removing elements, it always does nothing and throws an {@link UnsupportedOperationException}.
     * @param c collection containing elements to be removed from this list
     * @return Always throw throws an {@link UnsupportedOperationException}, never return.
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * This segment tree is also a {@link List}. To remove elements from this list.
     * But, because this implement of segment tree doesn't allow adding or removing elements, it always does nothing and throws an {@link UnsupportedOperationException}.
     * @param c collection containing elements to be retained in this list
     * @return Always throw throws an {@link UnsupportedOperationException}, never return.
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * This segment tree is also a {@link List}. To remove all elements from this list.
     * But, because this implement of segment tree doesn't allow adding or removing elements, it always does nothing and throws an {@link UnsupportedOperationException}.
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    /**
     * See {@link List#contains(Object)}.
     * @param o element whose presence in this list is to be tested
     * @return <tt>true</tt> if this list contains the specified element
     */
    @Override
    public boolean contains(Object o) {
        for (int i = 0; i < size; i++) {
            if (Objects.equals(elements[realIndexIndex[i]], o)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This segment tree is also a {@link List}. To remove elements from this list.
     * But, because this implement of segment tree doesn't allow adding or removing elements, it always does nothing and throws an {@link UnsupportedOperationException}.
     * @param index the index of the element to be removed
     * @return Always throw throws an {@link UnsupportedOperationException}, never return.
     */
    @Override
    public Type remove(int index) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get an {@link ListIterator} of all elements.
     * @return an {@link ListIterator} of all elements
     */
    @Override
    public ListIterator<Type> listIterator() {
        return new Iter();
    }

    /**
     * Get an {@link ListIterator} of all elements.
     * @param index index of the first element to be returned from the
     *        list iterator (by a call to {@link ListIterator#next next})
     * @return an {@link ListIterator} of all elements
     */
    @Override
    public ListIterator<Type> listIterator(int index) {
        return new Iter(index);
    }

    /**
     * This segment tree is also a {@link List}. Get a sub list of this list.
     * See {@link AbstractList#subList(int, int)}.
     * @param fromIndex low endpoint (inclusive) of the subList
     * @param toIndex high endpoint (exclusive) of the subList
     * @return a sub list of this list
     */
    @Override
    public List<Type> subList(int fromIndex, int toIndex) {
        return new SubList(fromIndex, toIndex - fromIndex);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(Math.max(16, size * 4 - 1));
        stringBuilder.append(getClass().getSimpleName()).append(": {");
        partToString(0, size, 1, stringBuilder);
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    protected void partToString(int currentLeftBorder, int currentRightBorder, int currentElementRealIndex, StringBuilder stringBuilder) {
        if (currentRightBorder - currentLeftBorder == 1) {
            stringBuilder.append(elements[currentElementRealIndex]);
        } else {
            int currentChildElementMiddleBorder = (currentLeftBorder + currentRightBorder) / 2;
            stringBuilder.append(elements[currentElementRealIndex]).append(" = {");
            partToString(currentLeftBorder, currentChildElementMiddleBorder, currentElementRealIndex * 2, stringBuilder);
            stringBuilder.append(", ");
            partToString(currentChildElementMiddleBorder, currentRightBorder, currentElementRealIndex * 2 + 1, stringBuilder);
            stringBuilder.append("}");
        }
    }

    protected class Iter implements ListIterator<Type> {
        private int currentElementIndex;
        private final Type[] elements = FixedSizeSegmentTree.this.elements;
        private final int[] realIndexIndex = FixedSizeSegmentTree.this.realIndexIndex;

        private final int leftBorder;

        private final int rightBorderMinus1;

        Iter() {
            currentElementIndex = -1;
            leftBorder = 0;
            rightBorderMinus1 = FixedSizeSegmentTree.this.size - 1;
        }

        Iter(int currentElementIndex) {
            this.currentElementIndex = currentElementIndex - 1;
            leftBorder = 0;
            rightBorderMinus1 = FixedSizeSegmentTree.this.size - 1;
        }

        Iter(int currentElementIndex, int leftBorder, int rightBorder) {
            this.currentElementIndex = currentElementIndex - 1;
            this.leftBorder = leftBorder;
            this.rightBorderMinus1 = rightBorder - 1;
        }

        @Override
        public boolean hasNext() {
            return currentElementIndex < rightBorderMinus1;
        }

        @Override
        public Type next() {
            currentElementIndex++;
            return elements[realIndexIndex[currentElementIndex]];
        }

        @Override
        public boolean hasPrevious() {
            return currentElementIndex > leftBorder;
        }

        @Override
        public Type previous() {
            currentElementIndex--;
            return elements[realIndexIndex[currentElementIndex]];
        }

        @Override
        public int nextIndex() {
            return currentElementIndex + 1;
        }

        @Override
        public int previousIndex() {
            return currentElementIndex - 1;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(Type value) {
            setValueAndGetCombination(currentElementIndex, value);
        }

        @Override
        public void add(Type type) {
            throw new UnsupportedOperationException();
        }
    }

    protected class SubList extends AbstractList<Type> {

        private final int indexOffset;

        private final int size;

        private final Type[] elements = FixedSizeSegmentTree.this.elements;
        private final int[] realIndexIndex = FixedSizeSegmentTree.this.realIndexIndex;

        SubList(int indexOffset, int size) {
            this.indexOffset = indexOffset;
            this.size = size;
        }

        @Override
        public boolean add(Type type) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Type set(int index, Type element) {
            if (index + indexOffset >= size) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
            }
            return setValueAndGetCombination(index + indexOffset, element);
        }

        @Override
        public Type remove(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int indexOf(Object o) {
            for (int i = indexOffset; i < size + indexOffset; i++) {
                if (Objects.equals(elements[realIndexIndex[i]], o)) {
                    return indexOffset;
                }
            }
            return -1;
        }

        @Override
        public int lastIndexOf(Object o) {
            for (int i = size + indexOffset - 1; i >= indexOffset; i--) {
                if (Objects.equals(elements[realIndexIndex[i]], o)) {
                    return indexOffset;
                }
            }
            return -1;
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(int index, Collection<? extends Type> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterator<Type> iterator() {
            return new Iter(indexOffset, indexOffset, indexOffset + size);
        }

        @Override
        public ListIterator<Type> listIterator() {
            return new Iter(indexOffset, indexOffset, indexOffset + size);
        }

        @Override
        public ListIterator<Type> listIterator(int index) {
            return new Iter(indexOffset + index, indexOffset, indexOffset + size);
        }

        @Override
        public List<Type> subList(int fromIndex, int toIndex) {
            return new SubList(indexOffset + fromIndex, toIndex - fromIndex);
        }

        @Override
        protected void removeRange(int fromIndex, int toIndex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isEmpty() {
            return super.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            for (int i = indexOffset; i < size + indexOffset; i++) {
                if (Objects.equals(elements[realIndexIndex[i]], o)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Object[] toArray() {
            Object[] copy = new Object[size];
            for(int i = indexOffset; i < size + indexOffset; i++) {
                copy[i] = elements[realIndexIndex[i]];
            }
            return copy;
        }

        @Override
        public <ArrayType> ArrayType[] toArray(ArrayType[] container) {
            ArrayType[] array;
            if (container.length < size) {
                array = (ArrayType[]) Array.newInstance(container.getClass().getComponentType(), size);
            } else {
                array = container;
            }
            for(int i = indexOffset; i < size + indexOffset; i++) {
                array[i] = (ArrayType) elements[realIndexIndex[i]];
            }
            return container;
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return super.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends Type> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Type get(int index) {
            return FixedSizeSegmentTree.this.get(index + indexOffset);
        }

        @Override
        public int size() {
            return size;
        }
    }
}
