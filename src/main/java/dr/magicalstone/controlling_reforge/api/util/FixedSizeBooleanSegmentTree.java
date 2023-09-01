package dr.magicalstone.controlling_reforge.api.util;

import javax.annotation.Nonnull;
import java.lang.reflect.Array;
import java.util.*;

/**
 * A segment tree {@link FixedSizeSegmentTree} whose element Type is primitive {@link boolean}.
 * All methods work in the same way as {@link FixedSizeSegmentTree}.
 * There will be other primitive segment trees of {@link double}, but no for {@link byte}, {@link short}, {@link int}, {@link float}, {@link char}.
 */
public class FixedSizeBooleanSegmentTree extends FixedSizeSegmentTree<Boolean> {

    protected final BooleanBinaryOperator operator;
    protected final boolean[] elements;

    public FixedSizeBooleanSegmentTree(BooleanBinaryOperator operator, boolean[] allElements) {
        super(allElements.length, null, (Boolean[]) null);
        this.operator = operator;
        int leafCapacity;
        if(size == 1) {
            leafCapacity = 1;
        } else {
            leafCapacity = (Integer.highestOneBit(size - 1) << 1);
        }
        this.elements = new boolean[leafCapacity * 2];
        indexIterate:
        for (int index = 0; index < size; index ++) {
            int elementRealIndex = 1;
            for (int elementLeftBorder = 0, elementRightBorder = size; elementRightBorder - elementLeftBorder > 1;) {
                if (elementRealIndex >= (leafCapacity / 2)) {
                    boolean leftChild = allElements[index];
                    boolean rightChild = allElements[index + 1];
                    elements[elementRealIndex * 2] = leftChild;
                    elements[elementRealIndex * 2 + 1] = rightChild;
                    elements[elementRealIndex] = operator.applyAsBoolean(leftChild, rightChild);
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
            elements[elementRealIndex] = operator.applyAsBoolean(elements[elementRealIndex * 2], elements[elementRealIndex * 2 + 1]);
        }
    }

    protected FixedSizeBooleanSegmentTree(int size, BooleanBinaryOperator operator, boolean[] elements) {
        super(size, null, (Boolean[]) null);
        this.operator = operator;
        this.elements = elements;
    }

    public boolean setValueAndGetCombination(int index, boolean value) {
        int elementRealIndex = realIndexIndex[index];
        elements[elementRealIndex] = value;
        boolean leftChild;
        boolean rightChild;
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
                leftChild = elements[elementRealIndex] = operator.applyAsBoolean(leftChild, rightChild);
                rightChild = elements[elementRealIndex + 1];
            } else {
                rightChild = elements[elementRealIndex] = operator.applyAsBoolean(leftChild, rightChild);
                leftChild = elements[elementRealIndex - 1];
            }
        }
        return rightChild;
    }

    public boolean set(int index, boolean element) {
        boolean lastValue = get(index);
        setValueAndGetCombination(index, element);
        return lastValue;
    }

    @Override
    public Boolean setValueAndGetCombination(int index, @Nonnull Boolean value) {
        return setValueAndGetCombination(index, (boolean) value);
    }

    @Override
    public Boolean get(int index) {
        return elements[realIndexIndex[index]];
    }

    @Override
    public Boolean getCombination() {
        return elements[1];
    }

    @Override
    public Boolean getCombination(int segmentLeftBorder, int segmentRightBorder) {
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

    private boolean getCombinationLOutRIn(int segmentRightBorder, int leftBorder, int rightBorder, int elementRealIndex) {
        int currentLeftBorder = leftBorder;
        int currentRightBorder = rightBorder;
        int currentElementRealIndex = elementRealIndex;
        boolean combination = false;
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
                combination = operator.applyAsBoolean(combination, elements[2 * currentElementRealIndex]);
            }
            if (segmentRightBorder == currentChildElementMiddleBorder) {
                return combination;
            }
            currentLeftBorder = currentChildElementMiddleBorder;
            currentElementRealIndex = 2 * currentElementRealIndex + 1;
        }
    }

    private boolean getCombinationLInROut(int segmentLeftBorder, int leftBorder, int rightBorder, int elementRealIndex) {
        int currentLeftBorder = leftBorder;
        int currentRightBorder = rightBorder;
        int currentElementRealIndex = elementRealIndex;
        boolean combination = false;
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
                combination = operator.applyAsBoolean(elements[2 * currentElementRealIndex + 1], combination);
            }
            if (segmentLeftBorder >= currentChildElementMiddleBorder) {
                return combination;
            }
            currentRightBorder = currentChildElementMiddleBorder;
            currentElementRealIndex = 2 * currentElementRealIndex;
        }
    }

    private boolean getCombinationLInRIn(int segmentLeftBorder, int segmentRightBorder) {
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
            boolean leftChild = getCombinationLInROut(segmentLeftBorder, currentLeftBorder, currentChildElementMiddleBorder, currentElementRealIndex * 2);
            boolean rightChild = getCombinationLOutRIn(segmentRightBorder, currentChildElementMiddleBorder, currentRightBorder, currentElementRealIndex * 2 + 1);
            return operator.applyAsBoolean(leftChild, rightChild);
        }
    }

    @Override
    public ListIterator<Boolean> listIterator() {
        return new FixedSizeBooleanSegmentTree.Iter();
    }

    @Override
    public ListIterator<Boolean> listIterator(int index) {
        return new FixedSizeBooleanSegmentTree.Iter(index);
    }

    @Override
    public FixedSizeBooleanSegmentTree subList(int fromIndex, int toIndex) {
        return new FixedSizeBooleanSegmentTree.SubList(fromIndex, toIndex - fromIndex);
    }

    @Override
    public Object[] toArray() {
        Object[] copy = new Boolean[size];
        for(int i = 0; i < size; i++) {
            copy[i] = elements[realIndexIndex[i]];
        }
        return copy;
    }

    public boolean[] toArrayPrimary() {
        boolean[] copy = new boolean[size];
        for(int i = 0; i < size; i++) {
            copy[i] = elements[realIndexIndex[i]];
        }
        return copy;
    }

    @Override
    public <ArrayType> ArrayType[] toArray(ArrayType[] container) {
        if (!container.getClass().getComponentType().equals(Boolean.class)) {
            throw new ClassCastException("The only type of container can be accessed is Boolean[].");
        }
        Boolean[] array;
        if (container.length < size) {
            array = (Boolean[]) Array.newInstance(container.getClass().getComponentType(), size);
        } else {
            array = (Boolean[]) container;
        }
        for(int i = 0; i < size; i++) {
            array[i] = elements[realIndexIndex[i]];
        }
        return (ArrayType[]) array;
    }

    public boolean[] toArray(boolean[] container) {
        boolean[] array;
        if (container.length < size) {
            array = new boolean[size];
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
        stringBuilder.append("boolean").append(": {");
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

    protected class Iter implements ListIterator<Boolean> {
        private int currentElementIndex;
        private final boolean[] elements = FixedSizeBooleanSegmentTree.this.elements;
        private final int[] realIndexIndex = FixedSizeBooleanSegmentTree.this.realIndexIndex;

        private final int leftBorder;

        private final int rightBorderMinus1;

        Iter() {
            currentElementIndex = -1;
            leftBorder = 0;
            rightBorderMinus1 = FixedSizeBooleanSegmentTree.this.size - 1;
        }

        Iter(int currentElementIndex) {
            this.currentElementIndex = currentElementIndex - 1;
            leftBorder = 0;
            rightBorderMinus1 = FixedSizeBooleanSegmentTree.this.size - 1;
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
        public Boolean next() {
            currentElementIndex++;
            return elements[realIndexIndex[currentElementIndex]];
        }

        @Override
        public boolean hasPrevious() {
            return currentElementIndex > leftBorder;
        }

        @Override
        public Boolean previous() {
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
        public void set(Boolean value) {
            setValueAndGetCombination(currentElementIndex, value);
        }

        @Override
        public void add(Boolean type) {
            throw new UnsupportedOperationException();
        }
    }

    protected class SubList extends FixedSizeBooleanSegmentTree {

        private final int indexOffset;

        private final int size;

        private final boolean[] elements = FixedSizeBooleanSegmentTree.this.elements;
        private final int[] realIndexIndex = FixedSizeBooleanSegmentTree.this.realIndexIndex;

        SubList(int indexOffset, int size) {
            super(size, null, FixedSizeBooleanSegmentTree.this.elements);
            this.indexOffset = indexOffset;
            this.size = size;
        }

        @Override
        public boolean set(int index, boolean element) {
            checkIndex(index);
            return FixedSizeBooleanSegmentTree.this.set(index + indexOffset, element);
        }

        @Override
        public boolean setValueAndGetCombination(int index, boolean value) {
            checkIndex(index);
            return FixedSizeBooleanSegmentTree.this.setValueAndGetCombination(index + indexOffset, value);
        }

        @Override
        public Boolean getCombination() {
            return FixedSizeBooleanSegmentTree.this.getCombination(indexOffset, indexOffset + size);
        }

        @Override
        public Boolean getCombination(int segmentLeftBorder, int segmentRightBorder) {
            checkRange(segmentLeftBorder, segmentRightBorder);
            return FixedSizeBooleanSegmentTree.this.getCombination(segmentLeftBorder + indexOffset, segmentRightBorder + indexOffset);
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
        public ListIterator<Boolean> listIterator() {
            return new FixedSizeBooleanSegmentTree.Iter(indexOffset, indexOffset, indexOffset + size);
        }

        @Override
        public ListIterator<Boolean> listIterator(int index) {
            return new FixedSizeBooleanSegmentTree.Iter(indexOffset + index, indexOffset, indexOffset + size);
        }

        @Override
        public FixedSizeBooleanSegmentTree subList(int fromIndex, int toIndex) {
            checkRange(fromIndex, toIndex);
            return new FixedSizeBooleanSegmentTree.SubList(indexOffset + fromIndex, toIndex - fromIndex);
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

        public boolean[] toArrayPrimary() {
            boolean[] copy = new boolean[size];
            for(int i = indexOffset; i < size + indexOffset; i++) {
                copy[i] = elements[realIndexIndex[i]];
            }
            return copy;
        }

        @Override
        public <ArrayType> ArrayType[] toArray(ArrayType[] container) {
            if (!container.getClass().getComponentType().equals(Boolean.class)) {
                throw new ClassCastException("The only type of container can be accessed is Boolean[].");
            }
            Boolean[] array;
            if (container.length < size) {
                array = (Boolean[]) Array.newInstance(container.getClass().getComponentType(), size);
            } else {
                array = (Boolean[]) container;
            }
            for(int i = indexOffset; i < size + indexOffset; i++) {
                array[i] = elements[realIndexIndex[i]];
            }
            return (ArrayType[]) array;
        }

        @Override
        public Boolean get(int index) {
            checkIndex(index);
            return FixedSizeBooleanSegmentTree.this.get(index + indexOffset);
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public String toString() {
            return "section [" + indexOffset + ", " + indexOffset + size + ") of " + FixedSizeBooleanSegmentTree.this;
        }

        private void checkIndex(int index) throws IndexOutOfBoundsException {
            if (index + indexOffset >= size) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
            }
        }

        private void checkRange(int segmentLeftBorder, int segmentRightBorder) throws IndexOutOfBoundsException {
            if (segmentLeftBorder >= segmentRightBorder) {
                throw new IndexOutOfBoundsException("SegmentLeftBorder: " + segmentLeftBorder + "SegmentRightBorder: " + segmentRightBorder + " Illegal Segment.");
            }
            if (segmentLeftBorder < 0) {
                throw new IndexOutOfBoundsException("SegmentLeftBorder: " + segmentLeftBorder);
            }
            if (segmentRightBorder + indexOffset >= size) {
                throw new IndexOutOfBoundsException("SegmentRightBorder: " + segmentRightBorder + ", Size: " + size);
            }
        }
    }
}
