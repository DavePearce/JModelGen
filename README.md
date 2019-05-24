
** Example 1**

_Generate 10 integers uniformly at random between -10000 and 10000_

```
Domain<Integer> ints = Domains.Sample(Domains.Int(-10000, 10000), 10);
//
for(int i=0;i!=ints.size();++i) {
	System.out.print(ints.get(i) + " ");
}
System.out.println();
```

**Example 2**

_Generate 10 lists of of lengths 1 .. 3 using above integers_

```
Domain<List<Integer>> lists = Domains.Sample(Domains.List(1, 3, ints), 10);
//
for(int i=0;i!=lists.size();++i) {
	System.out.print(lists.get(i) + " ");
}
System.out.println();
```

**Example 3**

_Generate products of ints and booleans using above integers_

```
Domain<Object[]> items = Domains.Product(new Domain[] { ints, Domains.Bool() });
//
for(int i=0;i!=items.size();++i) {
	System.out.print(Arrays.toString(items.get(i)) + " ");
}
System.out.println();
```

**Example 4**

_Generate products meeting type invariant where boolean being true signals that integer is non-negative_

```
Domain<Object[]> citems = Domains.Constrained(items,
xs -> ((Boolean) xs[1]) == false || ((Integer) xs[0]) >= 0);
//
for(int i=0;i!=citems.size();++i) {
	System.out.print(Arrays.toString(citems.get(i)) + " ");
}
System.out.println();
```