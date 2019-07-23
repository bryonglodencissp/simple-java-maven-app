package com.mycompany.app;
import java.util.ArrayList;

/**
 * Hello world!
 */
public class App
{

    private final String message = "Hello World!";

    public App() {}

    public static void main(String[] args) {
        System.out.println(new App().getMessage());
    }

    private final String getMessage() {
        return message;
    }
}

class BadLockObjectExamples {
    // This is the most correct way to do this. Create an immutable object of
    // type object which is used only as a lock. Do this instead of any of the
    // examples that follow.
    private final Object myLock = new Object();
    public void TheCorrectWay() {
        synchronized(myLock) {
            // ... some critical section ... 
        }
    }
    // Yes, Java will let you do this, but it is a very bad idea. String
    // literals are centrally interned and could also be locked on 
    // by a library,
    // causing you to potentially have deadlocks or lock collisions 
    // with other code.
    public void DontLockOnStringLiterals() {
        synchronized("") {}
    }

    // This is also a bad idea, for the same reason as the above.
    String strLock = "";
    public void DontLockOnFieldsInitializedWStringLiterals() {
        synchronized(strLock) {
        }
    }

    // String.intern returns the canonical, centrally stored copy of a string.
    // It suffers from the same problems as the above.
    public void DontLockOnInternedStrings(String someStr) {
        synchronized(someStr.intern()) {
        }
    }

    // This is a bad idea for the same reason as locking on the empty string.
    // Boxed integers within a certain range are guaranteed to be stored in 
    // the same central location. Thus, you can have locking collisions 
    // with libraries.
    public void DontLockOnBoxedIntegers() {
        synchronized((Integer) 0) {
        }
    }

    // This is even worse. If someVal can be a value outside of the small range
    // where aliasing is guaranteed, the aliasing behavior of the boxed integer
    // is not guaranteed at all. It may work differently on different systems
    // or between different versions of the JVM.
    public void DontLockOnBoxedIntegers2(int someVal) {
        synchronized((Integer) someVal) {
        }
    }

    // For floats, doubles, and other boxable types, there is no range in which
    // the aliasing of a boxed value is guaranteed.
    public void DontLockOnFloatsOrDoubles() {
        synchronized((Float) 0.0f) {
        }
    }

    // BAD_LOCK_OBJECT will notice if a box happens in a field.
    Integer intLock = 5;
    public void FieldBoxedInt() {
        synchronized(intLock) {
        }
    }
   // The object created in the synchronized statement can only be accessed by
   // one thread. Locking upon it will do nothing.
    public void DontLockOnObjectsThatCanOnlyBeAccessedByOneThread() {
        synchronized(new Object()) {
        }
    }

    // One thread can initialize myList to some value and enter
    // the critical section. Then a second thread can modify myList and enter
    // the critical section. This will likely cause race conditions and
    // corrupted data. In this case, it can cause part of the items[] array to
    // be added to the old contents of myList, and part to the new contents of
    // myList.
    ArrayList<Object> myList;
    public void DontMutateLockedFields(Object[] items) {
        if(myList == null) {
            myList = new ArrayList<Object>();
        }
        synchronized(myList) {
            for(Object item : items) {
                myList.add(item);
            }
        }
    }

    // By assigning myList in a critical section guarded by myList, this code is
    // allowing other threads to enter the critical section by acquiring a lock
    // on a different object. This breaks the protections that locking on
    // myList would provide.
    public void DontGuardAMutableFieldByLockingOnThatField() {
        synchronized(myList) {
            myList = new ArrayList<Object>();
            // ... other critical section operations ... 
        }
    }
}

class BCWCExamples  {
    public Object lock;

    boolean someCondition;

    public void NoChecking() throws InterruptedException {
        synchronized(lock) {
            //Defect due to not checking a wait condition at all
            lock.wait();  
        }
    }

    public void IfCheck() throws InterruptedException {
        synchronized(lock) {
            // Defect due to not checking the wait condition with a loop. 
            // If the wait is woken up by a spurious wakeup, we may continue
            // without someCondition becoming true.
            if(!someCondition) { 
                lock.wait();
            }
        }
    }

    public void OutsideLockLoop() throws InterruptedException {
        // Defect. It is possible for someCondition to become true after
        // the check but before acquiring the lock. This would cause this thread
        // to wait unnecessarily, potentially for quite a long time.
        while(!someCondition) {
            synchronized(lock) {
                lock.wait();
            }
        }
    }

    public void Correct() throws InterruptedException {
        // Correct checking of the wait condition. The condition is checked
        // before waiting inside the locked region, and is rechecked after wait
        // returns.
        synchronized(lock) {
            while(!someCondition) {
                lock.wait();
            }
        }
    }
}
