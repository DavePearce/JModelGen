
**Example 1**

_Generate 10 integers uniformly at random between -10000 and 10000_

```
Domain.Small<Integer> ints = Domains.Sample(Domains.Int(-10000, 10000), 10);
//
for(int i=0;i!=ints.size();++i) {
	System.out.print(ints.get(i) + " ");
}
System.out.println();
```

**Example 2**

_Generate 10 arrays of of lengths 1 .. 3 using above integers_

```
Domain.Small<Integer[]> arrays = Domains.Sample(Domains.Array(1, 3, ints), 10);
//
for(int i=0;i!=arrays.size();++i) {
	System.out.print(arrays.get(i) + " ");
}
System.out.println();
```

**Example 3**

_Generate products of ints and booleans using above integers_

```
Domain.Big<Object[]> items = Domains.Product(new Domain.Big[] { ints, Domains.BOOL });
//
for(Object[] item : items) {
	System.out.print(Arrays.toString(item) + " ");
}
System.out.println();
```

**Example 4**

_Generate arrays of sorted integers__

```
private static class Constraint implements Walker.State<Integer> {
	private final int min;
	private final int max;

	public Constraint(int min, int max) {
		this.min = min;
		this.max = max;
	}

	@Override
	public Walker<Integer> construct() {
		return Walkers.Adaptor(Domains.Int(min, max));
	}

	@Override
	public State<Integer> transfer(Integer item) {
		return item <= min ? this : new Constraint(item, max);
	}
}

Walker.State<Integer> seed = new Constraint(0,2);
Walker<List<Integer>> walker = Walkers.Product(0, 4, seed, (items) -> items);
//
for(List<Integer> item : walker) {
	System.out.println(item);
}
```
