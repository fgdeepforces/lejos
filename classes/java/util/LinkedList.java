package java.util;

/**
 * 
 * @author Michael Mirwaldt
 *
 */
public class LinkedList<E> extends AbstractList<E> {
	
	//TODO review

	protected transient final Entry<E> headerEntry = new Entry<E>(null, null, null);
	
	protected transient int size = 0;
	
	protected transient int modCount = 0;
	
	static class Entry<E> {
		E element;
		Entry<E> previousEntry;
		Entry<E> nextEntry;
		
		private Entry(E elem, Entry<E> nextEntry, Entry<E> previousEntry) {
			super();
			this.element = elem;
			this.previousEntry = previousEntry;
			this.nextEntry = nextEntry;
		}		
	}
	
	public LinkedList() {
		super();
		headerEntry.nextEntry = headerEntry.previousEntry = headerEntry;
	}
	
    public LinkedList(Collection<? extends E> c) {
    	this();
    	addAll(c);
    }

	public boolean add(E e) {
		addBefore(e, headerEntry);
		return true;
	}

	public boolean remove(Object o) {
        if (o==null) {
            for (Entry<E> e = headerEntry.nextEntry; e != headerEntry; e = e.nextEntry) {
                if (e.element==null) {
                    remove(e);
                    return true;
                }
            }
        } else {
            for (Entry<E> e = headerEntry.nextEntry; e != headerEntry; e = e.nextEntry) {
                if (o.equals(e.element)) {
                    remove(e);
                    return true;
                }
            }
        }
        return false;
	}

	public int size() {
		return size;
	}

	public void clear() {
        Entry<E> e = headerEntry.nextEntry;
        while (e != headerEntry) {
            Entry<E> next = e.nextEntry;
            e.nextEntry = e.previousEntry = null;
            e.element = null;
            e = next;
        }
        headerEntry.nextEntry = headerEntry.previousEntry = headerEntry;
        size = 0;
        modCount++;
	}

	public void add(int index, E e) {
		addBefore(e, (index==size ? headerEntry : entry(index)));	
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return addAll(size, c);
	}
	
	public boolean addAll(int index, Collection<? extends E> c) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index: "+index+
                                                ", Size: "+size);
        Object[] a = c.toArray();
        int numNew = a.length;
        if (numNew==0)
            return false;
        
        modCount++;

        Entry<E> successor = (index==size ? headerEntry : entry(index));
        Entry<E> predecessor = successor.previousEntry;
        for (int i=0; i<numNew; i++) {
            @SuppressWarnings("unchecked")
			Entry<E> e = new Entry<E>((E)a[i], successor, predecessor);
            predecessor.nextEntry = e;
            predecessor = e;
        }
        successor.previousEntry = predecessor;

        size += numNew;
        return true;
	}

	public E get(int index) {
		return entry(index).element;
	}

	public E set(int index, E e) {
		Entry<E> entry = entry(index);
		E oldValue = entry.element;
		entry.element = e;
		return oldValue;
	}

	public E remove(int index) {
		Entry<E> e = entry(index);
		return remove(e);
	}

	private E remove(Entry<E> e) {
		if (e == headerEntry)
		    throw new NoSuchElementException();

	    E result = e.element;
		e.previousEntry.nextEntry = e.nextEntry;
		e.nextEntry.previousEntry = e.previousEntry;
        e.nextEntry = e.previousEntry = null;
        e.element = null;
		size--;
		modCount++;
	    
		return result;
	}

	public int indexOf(Object o) {
		int index = 0;
        if (o==null) {
            for (Entry<E> e = headerEntry.nextEntry; e != headerEntry; e = e.nextEntry) {
                if (e.element==null)
                    return index;
                index++;
            }
        } 
        else {
            for (Entry<E> e = headerEntry.nextEntry; e != headerEntry; e = e.nextEntry) {
                if (o.equals(e.element))
                    return index;
                index++;
            }
        }
        return -1;
	}

	public int lastIndexOf(Object o) {
        int index = size;
        if (o==null) {
            for (Entry<E> e = headerEntry.previousEntry; e != headerEntry; e = e.previousEntry) {
                index--;
                if (e.element==null)
                    return index;
            }
        } 
        else {
            for (Entry<E> e = headerEntry.previousEntry; e != headerEntry; e = e.previousEntry) {
                index--;
                if (o.equals(e.element))
                    return index;
            }
        }
        return -1;
	}

	public ListIterator<E> listIterator(int index) {
		Entry<E> e = entry(index);
		return new MyIterator(e.previousEntry, index);
	}

	public List<E> subList(int start, int end) {
		throw new UnsupportedOperationException();
	}
	
    protected Entry<E> addBefore(E e, Entry<E> entry) {
    	Entry<E> newEntry = new Entry<E>(e, entry, entry.previousEntry);
    	newEntry.previousEntry.nextEntry = newEntry;
    	newEntry.nextEntry.previousEntry = newEntry;
    	size++;
    	modCount++;
    	return newEntry;
   }
    
    /**
     * Returns the indexed entry.
     */
    protected Entry<E> entry(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Index: "+index+
                                                ", Size: "+size);
        Entry<E> e = headerEntry;
        if (index < (size/2)) {
            for (int i = 0; i <= index; i++)
                e = e.nextEntry;
        } else {
            for (int i = size; i > index; i--)
                e = e.previousEntry;
        }
        return e;
    }
    
    private class MyIterator implements ListIterator<E>
	{
		private Entry<E> curEntry;
		private int curPos;
		private int modcount;

		public MyIterator(Entry<E> curEntry, int curPos)
		{
			this.curEntry = curEntry;
			this.curPos = curPos;
			this.modcount = LinkedList.this.modCount;
		}
		
		private void checkModCount()
		{
			if (this.modcount != LinkedList.this.modCount)
				throw new ConcurrentModificationException();
		}

		public boolean hasNext()
		{
			return curEntry.nextEntry != LinkedList.this.headerEntry;
		}

		public boolean hasPrevious()
		{
			return curEntry.previousEntry != LinkedList.this.headerEntry;
		}

		public E next()
		{
			this.checkModCount();
			Entry<E> newEntry = curEntry.nextEntry;
			if (newEntry == LinkedList.this.headerEntry)
				throw new NoSuchElementException();
			
			this.curPos++;
			this.curEntry = newEntry;
			return newEntry.element;
		}

		public E previous()
		{
			this.checkModCount();
			Entry<E> newEntry = curEntry.previousEntry;
			if (newEntry == LinkedList.this.headerEntry)
				throw new NoSuchElementException();
			
			this.curPos--;
			this.curEntry = newEntry;
			return newEntry.element;
		}

		public void remove()
		{
			this.checkModCount();
			if (curEntry == LinkedList.this.headerEntry)
				// TODO check with JDK implementation, documentation is much more strict when to throw this 
				throw new IllegalStateException();
			
			Entry<E> oldEntry = this.curEntry;
			this.curEntry = oldEntry.previousEntry;
			LinkedList.this.remove(oldEntry);
			this.modcount = LinkedList.this.modCount;
		}
		
		public void set(E e)
		{
			this.checkModCount();
			if (curEntry == LinkedList.this.headerEntry)
				// TODO check with JDK implementation, documentation is much more strict when to throw this 
				throw new IllegalStateException();
	
			this.curEntry.element = e;
		}

		public void add(E e)
		{
			this.checkModCount();
			LinkedList.this.addBefore(e, this.curEntry.nextEntry);
			this.modcount = LinkedList.this.modCount;
		}

		public int nextIndex()
		{
			return this.curPos;
		}

		public int previousIndex()
		{
			return this.curPos - 1;
		}

	}

}
