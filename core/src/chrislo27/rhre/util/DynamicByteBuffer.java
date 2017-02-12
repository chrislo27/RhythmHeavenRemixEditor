package chrislo27.rhre.util;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A utility class that provides auto-expansion functionality to a byte buffer.
 * It does so by completely wrapping the {@link ByteBuffer} class and adding
 * space checks to put-methods. When a put-method is invoked, a check is made to
 * determine if there is enough room within the buffer to accommodate the data
 * to be added. If so, data is put into the buffer as usual. If not, the buffer
 * is expanded by a defined expansion factor until the new capacity is big
 * enough to contain both the contents of the old (current) buffer and the data
 * to be added. It also is important to recognize that the expansion factor can
 * be changed. The new, expanded buffer has the same byte order as the old
 * buffer. This class only supports the use heap buffers and not direct buffers.
 * This is because the cost of allocating direct buffers is significantly
 * greater than that of a heap buffer, and it would be impractical to constantly
 * re-allocate direct byte buffers.
 * <p>
 * All of the methods that are part of the ByteBuffer class (even those part of
 * {@link java.nio.Buffer}) are also within this class. The functionality of
 * these methods is <b>completely unaltered</b>, and can be used exactly how
 * they would if this was a ByteBuffer. The only slight exceptions to this are
 * the {@link #mark()} and {@link #reset()} methods. A new buffer as a result of
 * expansion does not retain the mark of the previous buffer. If a mark needs to
 * be used, extra care needs to be taken to ensure that the amount of data never
 * exceeds the capacity so that the mark is not lost when the buffer is
 * expanded.
 * </p>
 * <p>
 * An important fact about this class is that it does not extend the ByteBuffer
 * class. This is because the constructors of the ByteBuffer class are not
 * visible when extending the class. As a result, an instance of this class
 * cannot be provided as an argument for a method requiring a ByteBuffer, such
 * as the {@code read(ByteBuffer)} method in the
 * {@link java.nio.channels.SocketChannel} class. Instead, when the internal
 * ByteBuffer instance needs to be retrieved, use the {@link #getByteBuffer()}
 * method. It is also important to recognize that returned ByteBuffer instance
 * does not feature the auto-expand functionality provided by this class.
 * </p>
 * <p>
 * This class is not thread safe. If multiple threads invoke a put-operation on
 * this buffer at the same time, the buffer could expand multiple times if not
 * synchronized properly.
 * </p>
 *
 * @author Martin Tuskevicius https://gist.github.com/DudeMartin/5273469
 * @see ByteBuffer
 */
public class DynamicByteBuffer implements Comparable<ByteBuffer> {

	private ByteBuffer byteBuffer;
	private float expandFactor;

	public DynamicByteBuffer(int initialCapacity, float expandFactor) {
		if (expandFactor < 1) {
			throw new IllegalArgumentException(
					"The expand factor must be greater or equal to 1!");
		}
		this.byteBuffer = ByteBuffer.allocate(initialCapacity);
		this.expandFactor = expandFactor;
	}

	public DynamicByteBuffer(int initialCapacity) {
		this(initialCapacity, 2);
	}

	public int capacity() {
		return byteBuffer.capacity();
	}

	public void clear() {
		byteBuffer.clear();
	}

	public Buffer flip() {
		return byteBuffer.flip();
	}

	public boolean hasRemaining() {
		return byteBuffer.hasRemaining();
	}

	public boolean isReadOnly() {
		return byteBuffer.isReadOnly();
	}

	public int limit() {
		return byteBuffer.limit();
	}

	public Buffer limit(int newLimit) {
		return byteBuffer.limit(newLimit);
	}

	public Buffer mark() {
		return byteBuffer.mark();
	}

	public int position() {
		return byteBuffer.position();
	}

	public Buffer position(int newPosition) {
		return byteBuffer.position(newPosition);
	}

	public int remaining() {
		return byteBuffer.remaining();
	}

	public Buffer reset() {
		return byteBuffer.reset();
	}

	public Buffer rewind() {
		return byteBuffer.rewind();
	}

	public byte[] array() {
		return byteBuffer.array();
	}

	public int arrayOffset() {
		return byteBuffer.arrayOffset();
	}

	public ByteBuffer compact() {
		return byteBuffer.compact();
	}

	public int compareTo(ByteBuffer that) {
		return byteBuffer.compareTo(that);
	}

	public ByteBuffer duplicate() {
		return byteBuffer.duplicate();
	}

	public boolean equals(Object ob) {
		return byteBuffer.equals(ob);
	}

	public byte get() {
		return byteBuffer.get();
	}

	public ByteBuffer get(byte[] dst) {
		return byteBuffer.get(dst);
	}

	public ByteBuffer get(byte[] dst, int offset, int length) {
		return byteBuffer.get(dst, offset, length);
	}

	public byte get(int index) {
		return byteBuffer.get(index);
	}

	public char getChar() {
		return byteBuffer.getChar();
	}

	public char getChar(int index) {
		return byteBuffer.getChar(index);
	}

	public double getDouble() {
		return byteBuffer.getDouble();
	}

	public double getDouble(int index) {
		return byteBuffer.getDouble(index);
	}

	public float getFloat() {
		return byteBuffer.getFloat();
	}

	public float getFloat(int index) {
		return byteBuffer.getFloat(index);
	}

	public int getInt() {
		return byteBuffer.getInt();
	}

	public int getInt(int index) {
		return byteBuffer.getInt(index);
	}

	public long getLong() {
		return byteBuffer.getLong();
	}

	public long getLong(int index) {
		return byteBuffer.getLong(index);
	}

	public short getShort() {
		return byteBuffer.getShort();
	}

	public short getShort(int index) {
		return byteBuffer.getShort(index);
	}

	public boolean hasArray() {
		return byteBuffer.hasArray();
	}

	public boolean isDirect() {
		return byteBuffer.isDirect();
	}

	public ByteOrder order() {
		return byteBuffer.order();
	}

	public ByteBuffer order(ByteOrder bo) {
		return byteBuffer.order(bo);
	}

	public ByteBuffer put(byte b) {
		ensureSpace(1);
		return byteBuffer.put(b);
	}

	public ByteBuffer put(byte[] src) {
		ensureSpace(src.length);
		return byteBuffer.put(src);
	}

	public ByteBuffer put(byte[] src, int offset, int length) {
		ensureSpace(length);
		return byteBuffer.put(src, offset, length);
	}

	public ByteBuffer put(ByteBuffer src) {
		ensureSpace(src.remaining());
		return byteBuffer.put(src);
	}

	public ByteBuffer put(int index, byte b) {
		ensureSpace(1);
		return byteBuffer.put(index, b);
	}

	public ByteBuffer putChar(char value) {
		ensureSpace(2);
		return byteBuffer.putChar(value);
	}

	public ByteBuffer putChar(int index, char value) {
		ensureSpace(2);
		return byteBuffer.putChar(index, value);
	}

	public ByteBuffer putDouble(double value) {
		ensureSpace(8);
		return byteBuffer.putDouble(value);
	}

	public ByteBuffer putDouble(int index, double value) {
		ensureSpace(8);
		return byteBuffer.putDouble(index, value);
	}

	public ByteBuffer putFloat(float value) {
		ensureSpace(4);
		return byteBuffer.putFloat(value);
	}

	public ByteBuffer putFloat(int index, float value) {
		ensureSpace(4);
		return byteBuffer.putFloat(index, value);
	}

	public ByteBuffer putInt(int value) {
		ensureSpace(4);
		return byteBuffer.putInt(value);
	}

	public ByteBuffer putInt(int index, int value) {
		ensureSpace(4);
		return byteBuffer.putInt(index, value);
	}

	public ByteBuffer putLong(int index, long value) {
		ensureSpace(8);
		return byteBuffer.putLong(index, value);
	}

	public ByteBuffer putLong(long value) {
		ensureSpace(8);
		return byteBuffer.putLong(value);
	}

	public ByteBuffer putShort(int index, short value) {
		ensureSpace(2);
		return byteBuffer.putShort(index, value);
	}

	public ByteBuffer putShort(short value) {
		ensureSpace(2);
		return byteBuffer.putShort(value);
	}

	public ByteBuffer slice() {
		return byteBuffer.slice();
	}

	@Override
	public int hashCode() {
		return byteBuffer.hashCode();
	}

	@Override
	public String toString() {
		return byteBuffer.toString();
	}

	public ByteBuffer getByteBuffer() {
		return byteBuffer;
	}

	public void setExpandFactor(float expandFactor) {
		if (expandFactor < 1) {
			throw new IllegalArgumentException(
					"The expand factor must be greater or equal to 1!");
		}
		this.expandFactor = expandFactor;
	}

	public float getExpandFactor() {
		return expandFactor;
	}

	private void ensureSpace(int needed) {
		if (remaining() >= needed) {
			return;
		}
		int newCapacity = (int) (byteBuffer.capacity() * expandFactor);
		while (newCapacity < (byteBuffer.capacity() + needed)) {
			newCapacity *= expandFactor;
		}
		ByteBuffer expanded = ByteBuffer.allocate(newCapacity);
		expanded.order(byteBuffer.order());
		expanded.put(byteBuffer);
		byteBuffer = expanded;
	}
}
