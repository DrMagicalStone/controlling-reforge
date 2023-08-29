package dr.magicalstone.controlling_reforge.api.util;

import javax.annotation.Nonnull;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.LongBinaryOperator;


/**
 * A segment tree {@link FixedSizeSegmentTree} whose element Type is primitive {@link long}.
 * All methods work in the same way as {@link FixedSizeSegmentTree}.
 * There will be other primitive segment trees of {@link boolean} and {@link double}, but no for {@link byte}, {@link short}, {@link int}, {@link float}, {@link char}.
 */
public class FixedSizeIntegerSegmentTree extends FixedSizeSegmentTree<Long> {

    protected final LongBinaryOperator operator;
    protected final long[] elements;

    public FixedSizeIntegerSegmentTree(LongBinaryOperator operator, long[] allElements) {
        super(allElements.length, null, (Long[]) null);
        this.operator = operator;
        int leafCapacity;
        if(size == 1) {
            leafCapacity = 1;
        } else {
            leafCapacity = (Integer.highestOneBit(size - 1) << 1);
        }
        this.elements = new long[leafCapacity * 2];
        indexIterate:
        for (int index = 0; index < size; index ++) {
            int elementRealIndex = 1;
            for (int elementLeftBorder = 0, elementRightBorder = size; elementRightBorder - elementLeftBorder > 1;) {
                if (elementRealIndex >= (leafCapacity / 2)) {
                    long leftChild = allElements[index];
                    long rightChild = allElements[index + 1];
                    elements[elementRealIndex * 2] = leftChild;
                    elements[elementRealIndex * 2 + 1] = rightChild;
                    elements[elementRealIndex] = operator.applyAsLong(leftChild, rightChild);
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
            elements[elementRealIndex] = operator.applyAsLong(elements[elementRealIndex * 2], elements[elementRealIndex * 2 + 1]);
        }
    }

    public long setValueAndGetCombination(int index, long value) {
        int elementRealIndex = realIndexIndex[index];
        elements[elementRealIndex] = value;
        long leftChild;
        long rightChild;
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
                leftChild = elements[elementRealIndex] = operator.applyAsLong(leftChild, rightChild);
                rightChild = elements[elementRealIndex + 1];
            } else {
                rightChild = elements[elementRealIndex] = operator.applyAsLong(leftChild, rightChild);
                leftChild = elements[elementRealIndex - 1];
            }
        }
        return rightChild;
    }

    @Override
    public Long setValueAndGetCombination(int index, @Nonnull Long value) {
        return setValueAndGetCombination(index, (long) value);
    }

    @Override
    public Long get(int index) {
        return elements[realIndexIndex[index]];
    }

    @Override
    public Long getCombination() {
        return elements[1];
    }

    @Override
    public Long getCombination(int segmentLeftBorder, int segmentRightBorder) {
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

    private long getCombinationLOutRIn(int segmentRightBorder, int leftBorder, int rightBorder, int elementRealIndex) {
        int currentLeftBorder = leftBorder;
        int currentRightBorder = rightBorder;
        int currentElementRealIndex = elementRealIndex;
        long combination = 0;
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
                combination = operator.applyAsLong(combination, elements[2 * currentElementRealIndex]);
            }
            if (segmentRightBorder == currentChildElementMiddleBorder) {
                return combination;
            }
            currentLeftBorder = currentChildElementMiddleBorder;
            currentElementRealIndex = 2 * currentElementRealIndex + 1;
        }
    }

    private long getCombinationLInROut(int segmentLeftBorder, int leftBorder, int rightBorder, int elementRealIndex) {
        int currentLeftBorder = leftBorder;
        int currentRightBorder = rightBorder;
        int currentElementRealIndex = elementRealIndex;
        long combination = 0;
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
                combination = operator.applyAsLong(elements[2 * currentElementRealIndex + 1], combination);
            }
            if (segmentLeftBorder >= currentChildElementMiddleBorder) {
                return combination;
            }
            currentRightBorder = currentChildElementMiddleBorder;
            currentElementRealIndex = 2 * currentElementRealIndex;
        }
    }

    private long getCombinationLInRIn(int segmentLeftBorder, int segmentRightBorder) {
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
            long leftChild = getCombinationLInROut(segmentLeftBorder, currentLeftBorder, currentChildElementMiddleBorder, currentElementRealIndex * 2);
            long rightChild = getCombinationLOutRIn(segmentRightBorder, currentChildElementMiddleBorder, currentRightBorder, currentElementRealIndex * 2 + 1);
            return operator.applyAsLong(leftChild, rightChild);
        }
    }

    @Override
    public Object[] toArray() {
        Object[] copy = new Long[size];
        for(int i = 0; i < size; i++) {
            copy[i] = elements[realIndexIndex[i]];
        }
        return copy;
    }

    public long[] toArrayPrimary() {
        long[] copy = new long[size];
        for(int i = 0; i < size; i++) {
            copy[i] = elements[realIndexIndex[i]];
        }
        return copy;
    }

    @Override
    public <ArrayType> ArrayType[] toArray(ArrayType[] container) {
        if (!container.getClass().getComponentType().equals(Long.class)) {
            throw new ClassCastException("The only type of container can be accessed is Long[].");
        }
        Long[] array;
        if (container.length < size) {
            array = (Long[]) Array.newInstance(container.getClass().getComponentType(), size);
        } else {
            array = (Long[]) container;
        }
        for(int i = 0; i < size; i++) {
            array[i] = elements[realIndexIndex[i]];
        }
        return (ArrayType[]) array;
    }

    public long[] toArray(long[] container) {
        long[] array;
        if (container.length < size) {
            array = new long[size];
        } else {
            array = container;
        }
        for(int i = 0; i < size; i++) {
            array[i] = elements[realIndexIndex[i]];
        }
        return array;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(Math.max(16, size * 4 - 1));
        stringBuilder.append("long").append(": {");
        partToString(0, size, 1, stringBuilder);
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    @Override
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

    protected class Iter implements ListIterator<Long> {
        private int currentElementIndex;
        private final long[] elements = FixedSizeIntegerSegmentTree.this.elements;
        private final int[] realIndexIndex = FixedSizeIntegerSegmentTree.this.realIndexIndex;

        private final int leftBorder;

        private final int rightBorderMinus1;

        Iter() {
            currentElementIndex = -1;
            leftBorder = 0;
            rightBorderMinus1 = FixedSizeIntegerSegmentTree.this.size - 1;
        }

        Iter(int currentElementIndex) {
            this.currentElementIndex = currentElementIndex - 1;
            leftBorder = 0;
            rightBorderMinus1 = FixedSizeIntegerSegmentTree.this.size - 1;
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
        public Long next() {
            currentElementIndex++;
            return elements[realIndexIndex[currentElementIndex]];
        }

        @Override
        public boolean hasPrevious() {
            return currentElementIndex > leftBorder;
        }

        @Override
        public Long previous() {
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
        public void set(Long value) {
            setValueAndGetCombination(currentElementIndex, value);
        }

        @Override
        public void add(Long type) {
            throw new UnsupportedOperationException();
        }
    }

    protected class SubList extends AbstractList<Long> {


        private final int indexOffset;

        private final int size;

        private final long[] elements = FixedSizeIntegerSegmentTree.this.elements;
        private final int[] realIndexIndex = FixedSizeIntegerSegmentTree.this.realIndexIndex;

        SubList(int indexOffset, int size) {
            this.indexOffset = indexOffset;
            this.size = size;
        }

        @Override
        public boolean add(Long type) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Long set(int index, Long element) {
            if (index + indexOffset >= size) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
            }
            return setValueAndGetCombination(index + indexOffset, element);
        }

        @Override
        public Long remove(int index) {
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
        public boolean addAll(int index, Collection<? extends Long> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterator<Long> iterator() {
            return new FixedSizeIntegerSegmentTree.Iter(indexOffset, indexOffset, indexOffset + size);
        }

        @Override
        public ListIterator<Long> listIterator() {
            return new FixedSizeIntegerSegmentTree.Iter(indexOffset, indexOffset, indexOffset + size);
        }

        @Override
        public ListIterator<Long> listIterator(int index) {
            return new FixedSizeIntegerSegmentTree.Iter(indexOffset + index, indexOffset, indexOffset + size);
        }

        @Override
        public List<Long> subList(int fromIndex, int toIndex) {
            return new FixedSizeIntegerSegmentTree.SubList(indexOffset + fromIndex, toIndex - fromIndex);
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
            Object[] copy = new Long[size];
            for(int i = indexOffset; i < size + indexOffset; i++) {
                copy[i] = elements[realIndexIndex[i]];
            }
            return copy;
        }

        public long[] toArrayPrimary() {
            long[] copy = new long[size];
            for(int i = indexOffset; i < size + indexOffset; i++) {
                copy[i] = elements[realIndexIndex[i]];
            }
            return copy;
        }

        @Override
        public <ArrayType> ArrayType[] toArray(ArrayType[] container) {
            if (!container.getClass().getComponentType().equals(Long.class)) {
                throw new ClassCastException("The only type of container can be accessed is Long[].");
            }
            Long[] array;
            if (container.length < size) {
                array = (Long[]) Array.newInstance(container.getClass().getComponentType(), size);
            } else {
                array = (Long[]) container;
            }
            for(int i = indexOffset; i < size + indexOffset; i++) {
                array[i] = elements[realIndexIndex[i]];
            }
            return (ArrayType[]) array;
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
        public boolean addAll(Collection<? extends Long> c) {
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
        public Long get(int index) {
            return FixedSizeIntegerSegmentTree.this.get(index + indexOffset);
        }

        @Override
        public int size() {
            return size;
        }
    }
}
