// CountPOAServant.java: The Count Implementation
class CountPOAServant extends CounterPOA.CountPOA
{
  private int sum;

  // Constructors
  CountPOAServant()
  { super();
    System.out.println("Count Object Created");
    sum = 0;
  }

  // get sum
  public synchronized int sum()
  { return sum;
  }

  // set sum
  public synchronized void sum(int val)
  { sum = val;
  }

  // increment method
  public synchronized int increment()
  { sum++;
    return sum;
  }
}
