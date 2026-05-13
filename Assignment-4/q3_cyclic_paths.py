"""
Project Euler - Problem 312: Cyclic Paths on Sierpinski Graphs

The recurrence relation for C(n):
  C(1) = C(2) = 1
  C(n) = 3 * C(n-1)^3   for n >= 3

Verify:
  C(3) = 3 * 1^3 = 3  -- wrong, expected 8

Correct recurrence derived from known values:
  C(2) = 1
  C(3) = 8        => C(3) = 8 * C(2)^3 = 8
  C(4) = ?        => need to find multiplier

Actually the recurrence is:
  C(n) = C(n-1)^3 * 3  for n >= 3  with C(2)=1, C(3)=8

Let's re-examine: if C(n) = a * C(n-1)^3
  C(3) = a * C(2)^3 = a * 1 = a => a = 8
  C(4) = 8 * C(3)^3 = 8 * 512 = 4096
  C(5) = 8 * C(4)^3 = 8 * 4096^3 = 8 * 68719476736 = 549755813888  -- wrong, expected 71328803586048

So the recurrence is NOT simply a * C(n-1)^3.

From the verified values:
  C(5) = 71328803586048
  C(4) = ?

Let's find C(4) such that C(5) = f(C(4)):
  71328803586048 = 3 * C(4)^3  => C(4)^3 = 23776267862016 => C(4) = 2874 (approx)
  2874^3 = 23756757624  -- no

Try C(5) = C(4)^3 * k:
  Need C(4) first. Let's try the transfer matrix approach.

The correct recurrence (well-known for this problem) is:
  C(n) = 3 * C(n-1)^3  for n >= 3, with C(2) = 1

But C(3) = 3*1 = 3, not 8. So there must be a base case shift.

Actually C(1)=1, C(2)=1, and the recurrence starts differently.
Let's try: C(n+1) = 3 * C(n)^3
  C(2) = 1
  C(3) = 3 * 1 = 3  -- still wrong

The ACTUAL recurrence for this problem (from mathematical literature) is:
  C(n) = C(n-1)^3 * 3  but with C(2) = 8/3... doesn't make sense for integers.

Let me just brute-force verify small cases and find the pattern.
"""

def find_recurrence():
    # Known: C(1)=1, C(2)=1, C(3)=8, C(5)=71328803586048
    # C(5)/C(3)^3 = 71328803586048 / 512 = 139314069504
    # C(3)/C(2)^3 = 8
    # C(3)/C(1)^3 = 8
    # So C(n) = 8 * C(n-1)^3 doesn't hold for C(5)

    # Try C(n) = C(n-1)^3 * C(n-2)^? 
    # C(3) = C(2)^3 * C(1)^? = 1 * 1 = 1... no

    # The recurrence from the OEIS / PE community for this problem:
    # Let f(n) = C(n). Then:
    # f(n) = 3 * f(n-1)^3  is wrong.
    # 
    # Correct: using the "junction" analysis of Sierpinski graphs,
    # the number of Hamiltonian cycles satisfies:
    # C(n) = C(n-1)^3 * 3  -- but only if we define things carefully
    #
    # Let's verify C(4):
    # If C(4) = 3 * C(3)^3 = 3 * 512 = 1536
    # Then C(5) = 3 * C(4)^3 = 3 * 1536^3 = 3 * 3623878656 = ... 
    c4 = 3 * (8**3)
    c5 = 3 * (c4**3)
    print(f"C(4) = {c4}")
    print(f"C(5) = {c5}")
    print(f"Expected C(5) = 71328803586048")
    print(f"Match: {c5 == 71328803586048}")

find_recurrence()
