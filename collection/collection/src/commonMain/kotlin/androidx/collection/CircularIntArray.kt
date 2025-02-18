/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.collection

import androidx.collection.CollectionPlatformUtils.createIndexOutOfBoundsException
import androidx.collection.internal.requirePrecondition
import kotlin.jvm.JvmOverloads

/**
 * [CircularIntArray] is a circular integer array data structure that provides O(1) random read,
 * O(1) prepend and O(1) append. The CircularIntArray automatically grows its capacity when number
 * of added integers is over its capacity.
 *
 * @param minCapacity the minimum capacity, between 1 and 2^30 inclusive
 * @constructor Creates a circular array with capacity for at least [minCapacity] elements.
 */
public class CircularIntArray @JvmOverloads public constructor(minCapacity: Int = 8) {
    private var elements: IntArray
    private var head = 0
    private var tail = 0
    private var capacityBitmask: Int

    init {
        requirePrecondition(minCapacity >= 1) { "capacity must be >= 1" }
        requirePrecondition(minCapacity <= 2 shl 29) { "capacity must be <= 2^30" }

        // If minCapacity isn't a power of 2, round up to the next highest
        // power of 2.
        val arrayCapacity: Int =
            if (minCapacity.countOneBits() != 1) {
                (minCapacity - 1).takeHighestOneBit() shl 1
            } else {
                minCapacity
            }
        capacityBitmask = arrayCapacity - 1
        elements = IntArray(arrayCapacity)
    }

    private fun doubleCapacity() {
        val n = elements.size
        val r = n - head
        val newCapacity = n shl 1
        if (newCapacity < 0) {
            throw RuntimeException("Max array capacity exceeded")
        }
        val a = IntArray(newCapacity)
        elements.copyInto(destination = a, destinationOffset = 0, startIndex = head, endIndex = n)
        elements.copyInto(destination = a, destinationOffset = r, startIndex = 0, endIndex = head)
        elements = a
        head = 0
        tail = n
        capacityBitmask = newCapacity - 1
    }

    /**
     * Add an integer in front of the [CircularIntArray].
     *
     * @param element [Int] to add.
     */
    public fun addFirst(element: Int) {
        head = (head - 1) and capacityBitmask
        elements[head] = element
        if (head == tail) {
            doubleCapacity()
        }
    }

    /**
     * Add an integer at end of the [CircularIntArray].
     *
     * @param element [Int] to add.
     */
    public fun addLast(element: Int) {
        elements[tail] = element
        tail = (tail + 1) and capacityBitmask
        if (tail == head) {
            doubleCapacity()
        }
    }

    /**
     * Remove first integer from front of the [CircularIntArray] and return it.
     *
     * @return The integer removed.
     * @throws IndexOutOfBoundsException if [CircularIntArray] is empty.
     */
    public fun popFirst(): Int {
        if (head == tail) throw createIndexOutOfBoundsException()
        val result = elements[head]
        head = (head + 1) and capacityBitmask
        return result
    }

    /**
     * Remove last integer from end of the [CircularIntArray] and return it.
     *
     * @return The integer removed.
     * @throws IndexOutOfBoundsException if [CircularIntArray] is empty.
     */
    public fun popLast(): Int {
        if (head == tail) throw createIndexOutOfBoundsException()
        val t = (tail - 1) and capacityBitmask
        val result = elements[t]
        tail = t
        return result
    }

    /** Remove all integers from the [CircularIntArray]. */
    public fun clear() {
        tail = head
    }

    /**
     * Remove multiple integers from front of the [CircularIntArray], ignore when [count] is less
     * than or equals to 0.
     *
     * @param count Number of integers to remove.
     * @throws IndexOutOfBoundsException if numOfElements is larger than [size]
     */
    public fun removeFromStart(count: Int) {
        if (count <= 0) {
            return
        }
        if (count > size()) {
            throw createIndexOutOfBoundsException()
        }
        head = (head + count) and capacityBitmask
    }

    /**
     * Remove multiple elements from end of the [CircularIntArray], ignore when [count] is less than
     * or equals to 0.
     *
     * @param count Number of integers to remove.
     * @throws IndexOutOfBoundsException if [count] is larger than [size]
     */
    public fun removeFromEnd(count: Int) {
        if (count <= 0) {
            return
        }
        if (count > size()) {
            throw createIndexOutOfBoundsException()
        }
        tail = (tail - count) and capacityBitmask
    }

    /**
     * Get first integer of the [CircularIntArray].
     *
     * @return The first integer.
     * @throws [IndexOutOfBoundsException] if [CircularIntArray] is empty.
     */
    public val first: Int
        get() {
            if (head == tail) throw createIndexOutOfBoundsException()
            return elements[head]
        }

    /**
     * Get last integer of the [CircularIntArray].
     *
     * @return The last integer.
     * @throws [IndexOutOfBoundsException] if [CircularIntArray] is empty.
     */
    public val last: Int
        get() {
            if (head == tail) throw createIndexOutOfBoundsException()
            return elements[(tail - 1) and capacityBitmask]
        }

    /**
     * Get nth (0 <= n <= size()-1) integer of the [CircularIntArray].
     *
     * @param index The zero based element index in the [CircularIntArray].
     * @return The nth integer.
     * @throws [IndexOutOfBoundsException] if n < 0 or n >= size().
     */
    public operator fun get(index: Int): Int {
        if (index < 0 || index >= size()) throw createIndexOutOfBoundsException()
        return elements[(head + index) and capacityBitmask]
    }

    /**
     * Get number of integers in the [CircularIntArray].
     *
     * @return Number of integers in the [CircularIntArray].
     */
    public fun size(): Int {
        return (tail - head) and capacityBitmask
    }

    /**
     * Return `true` if [size] is 0.
     *
     * @return `true` if [size] is 0.
     */
    public fun isEmpty(): Boolean = head == tail
}
